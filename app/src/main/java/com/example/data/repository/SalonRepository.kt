package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Appointment
import com.example.data.model.Customer
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SalonRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  fun observeCustomers(userId: String): Flow<List<Customer>> = flow {
    if (userId.isBlank()) {
      emit(emptyList())
      return@flow
    }
    val path = "users/$userId/customers"
    emitAll(
      db.collection("users").document(userId).collection("customers")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(Customer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw (firestoreError ?: error)
        }
    )
  }

  fun observeAppointments(userId: String): Flow<List<Appointment>> = flow {
    if (userId.isBlank()) {
      emit(emptyList())
      return@flow
    }
    val path = "users/$userId/appointments"
    emitAll(
      db.collection("users").document(userId).collection("appointments")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(Appointment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw (firestoreError ?: error)
        }
    )
  }

  suspend fun addCustomer(customer: Customer): String {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("customers")
    val docRef = if (customer.id.isNotBlank()) collectionRef.document(customer.id) else collectionRef.document()
    val payload = customer.toCreateMap(uid)

    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updateCustomer(customer: Customer) {
    val uid = requireUserId()
    require(customer.id.isNotBlank()) { "Customer ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("customers").document(customer.id)
    val payload = customer.toUpdateMap()

    try {
      docRef.update(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteCustomer(customerId: String) {
    val uid = requireUserId()
    require(customerId.isNotBlank()) { "Customer ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("customers").document(customerId)

    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun addAppointment(appointment: Appointment): String {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("appointments")
    val docRef = if (appointment.id.isNotBlank()) collectionRef.document(appointment.id) else collectionRef.document()
    val payload = appointment.toCreateMap(uid)

    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String) {
    val uid = requireUserId()
    require(appointmentId.isNotBlank()) { "Appointment ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("appointments").document(appointmentId)

    try {
      docRef.update(
        mapOf(
          "status" to newStatus,
          "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteAppointment(appointmentId: String) {
    val uid = requireUserId()
    require(appointmentId.isNotBlank()) { "Appointment ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("appointments").document(appointmentId)

    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun seedDummyData(userId: String) {
    val uid = requireUserId()
    require(uid == userId) { "Authenticated user ID mismatch" }

    val sampleClients = listOf(
      Triple(
        Customer(
          id = "dummy_cust_1",
          userId = uid,
          name = "Sophia Martinez",
          phoneNo = "+1 (555) 234-8901",
          dob = "1995-06-12",
          email = "sophia.martinez@gmail.com",
          address = "450 Broadway Ave, Apt 4B, New York, NY",
          preferredStylist = "Elena Vance",
          notes = "Prefers organic plant-based hair dye. Sulfate free shampoo.",
          totalVisits = 5L
        ),
        Appointment(
          id = "dummy_apt_1",
          userId = uid,
          customerId = "dummy_cust_1",
          customerName = "Sophia Martinez",
          customerPhone = "+1 (555) 234-8901",
          serviceName = "Balayage Hair Color & Blowout",
          price = 220L,
          stylist = "Elena Vance",
          appointmentDate = "2026-10-25",
          appointmentTime = "11:30",
          status = "Confirmed",
          notes = "Golden honey tones with face framing highlights"
        ),
        Unit
      ),
      Triple(
        Customer(
          id = "dummy_cust_2",
          userId = uid,
          name = "Alexander Wright",
          phoneNo = "+1 (555) 345-6789",
          dob = "1988-11-23",
          email = "a.wright@corporate.io",
          address = "1280 Sunset Blvd, Los Angeles, CA",
          preferredStylist = "Marcus Bell",
          notes = "Gentleman's precision taper fade and hot towel beard oil.",
          totalVisits = 8L
        ),
        Appointment(
          id = "dummy_apt_2",
          userId = uid,
          customerId = "dummy_cust_2",
          customerName = "Alexander Wright",
          customerPhone = "+1 (555) 345-6789",
          serviceName = "Signature Beard Sculpt & Haircut",
          price = 85L,
          stylist = "Marcus Bell",
          appointmentDate = "2026-10-26",
          appointmentTime = "14:00",
          status = "Confirmed",
          notes = "Straight razor neck shave requested"
        ),
        Unit
      ),
      Triple(
        Customer(
          id = "dummy_cust_3",
          userId = uid,
          name = "Isabella Chen",
          phoneNo = "+1 (555) 456-7890",
          dob = "1998-03-09",
          email = "isabella.chen@design.co",
          address = "782 Elmwood Drive, Chicago, IL",
          preferredStylist = "Sophie Lin",
          notes = "Japanese Keratin smoothing treatment. Sensitive scalp.",
          totalVisits = 3L
        ),
        Appointment(
          id = "dummy_apt_3",
          userId = uid,
          customerId = "dummy_cust_3",
          customerName = "Isabella Chen",
          customerPhone = "+1 (555) 456-7890",
          serviceName = "Keratin Silk Smoothing & Treatment",
          price = 290L,
          stylist = "Sophie Lin",
          appointmentDate = "2026-10-27",
          appointmentTime = "10:00",
          status = "Pending",
          notes = "Pre-wash with clarifying salon detox"
        ),
        Unit
      ),
      Triple(
        Customer(
          id = "dummy_cust_4",
          userId = uid,
          name = "Lucas Vance",
          phoneNo = "+1 (555) 567-8901",
          dob = "1992-09-17",
          email = "lucas.vance@techhub.com",
          address = "310 Pine Street, Seattle, WA",
          preferredStylist = "David Kim",
          notes = "Deep scalp massage detox and matte styling paste.",
          totalVisits = 4L
        ),
        Appointment(
          id = "dummy_apt_4",
          userId = uid,
          customerId = "dummy_cust_4",
          customerName = "Lucas Vance",
          customerPhone = "+1 (555) 567-8901",
          serviceName = "Scalp Detox & Modern Textured Cut",
          price = 95L,
          stylist = "David Kim",
          appointmentDate = "2026-10-28",
          appointmentTime = "16:30",
          status = "Confirmed",
          notes = "Low fade with textured scissor top"
        ),
        Unit
      ),
      Triple(
        Customer(
          id = "dummy_cust_5",
          userId = uid,
          name = "Olivia Taylor",
          phoneNo = "+1 (555) 678-9012",
          dob = "1990-12-04",
          email = "olivia.taylor@studio.org",
          address = "155 Ocean Avenue, Miami, FL",
          preferredStylist = "Elena Vance",
          notes = "HydraFacial Glow and Champagne Spa Gel Manicure.",
          totalVisits = 6L
        ),
        Appointment(
          id = "dummy_apt_5",
          userId = uid,
          customerId = "dummy_cust_5",
          customerName = "Olivia Taylor",
          customerPhone = "+1 (555) 678-9012",
          serviceName = "HydraFacial Glow & Spa Manicure",
          price = 175L,
          stylist = "Elena Vance",
          appointmentDate = "2026-10-29",
          appointmentTime = "13:00",
          status = "Pending",
          notes = "French tip manicure with cuticle treatment"
        ),
        Unit
      )
    )

    for ((customer, appointment, _) in sampleClients) {
      addCustomer(customer)
      addAppointment(appointment)
    }
  }
}
