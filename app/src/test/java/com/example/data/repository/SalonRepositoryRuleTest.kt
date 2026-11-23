package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Appointment
import com.example.data.model.Customer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SalonRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun authenticatedUser_canAddAndObserveCustomer() = runBlocking {
    val uid = signInTestUser("stylist_test@example.com")
    val repository = SalonRepository(firestore)

    val customer = Customer(
      id = "cust_test_1",
      userId = uid,
      name = "Clara Oswald",
      phoneNo = "+1-555-8899",
      dob = "1993-08-20",
      email = "clara.oswald@example.com",
      address = "42 Baker Street",
      preferredStylist = "Elena Vance",
      notes = "Prefers sulfate-free organic wash",
      totalVisits = 1L
    )

    val id = repository.addCustomer(customer)
    assertEquals("cust_test_1", id)

    val customers = repository.observeCustomers(uid).first()
    assertTrue(customers.any { it.name == "Clara Oswald" && it.phoneNo == "+1-555-8899" })
  }

  @Test
  fun authenticatedUser_canAddAndObserveAppointment() = runBlocking {
    val uid = signInTestUser("barber_test@example.com")
    val repository = SalonRepository(firestore)

    val appointment = Appointment(
      id = "apt_test_1",
      userId = uid,
      customerId = "cust_test_1",
      customerName = "James Bond",
      customerPhone = "+1-555-0077",
      serviceName = "Gentleman Hot Towel Shave",
      price = 75L,
      stylist = "Marcus Bell",
      appointmentDate = "2026-10-30",
      appointmentTime = "15:00",
      status = "Confirmed",
      notes = "VIP appointment"
    )

    val id = repository.addAppointment(appointment)
    assertEquals("apt_test_1", id)

    val appointments = repository.observeAppointments(uid).first()
    assertTrue(appointments.any { it.serviceName == "Gentleman Hot Towel Shave" })
  }

  @Test
  fun unauthenticatedUser_failsFastOnRepositoryOperations() = runBlocking {
    auth.signOut()
    val repository = SalonRepository(firestore)

    try {
      repository.addCustomer(Customer(name = "Ghost Client", phoneNo = "123456", email = "ghost@example.com"))
      fail("Expected IllegalStateException for unauthenticated write")
    } catch (e: IllegalStateException) {
      assertTrue(e.message?.contains("User must be signed in with Google") == true)
    }
  }
}
