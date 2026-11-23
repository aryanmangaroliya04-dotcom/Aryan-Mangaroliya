package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class Appointment(
  @DocumentId
  val id: String = "",
  val userId: String = "",
  val customerId: String = "",
  val customerName: String = "",
  val customerPhone: String = "",
  val serviceName: String = "",
  val price: Long = 0L,
  val stylist: String = "",
  val appointmentDate: String = "",
  val appointmentTime: String = "",
  val status: String = "Pending", // Pending, Confirmed, Completed, Cancelled
  val notes: String = "",
  @ServerTimestamp
  val createdAt: Timestamp? = null,
  @ServerTimestamp
  val updatedAt: Timestamp? = null
) {
  fun toCreateMap(uid: String): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
      "userId" to uid,
      "customerName" to customerName.trim(),
      "serviceName" to serviceName.trim(),
      "appointmentDate" to appointmentDate.trim(),
      "appointmentTime" to appointmentTime.trim(),
      "status" to status,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (customerId.isNotBlank()) map["customerId"] = customerId.trim()
    if (customerPhone.isNotBlank()) map["customerPhone"] = customerPhone.trim()
    if (price > 0L) map["price"] = price
    if (stylist.isNotBlank()) map["stylist"] = stylist.trim()
    if (notes.isNotBlank()) map["notes"] = notes.trim()
    return map
  }

  fun toUpdateMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
      "customerName" to customerName.trim(),
      "serviceName" to serviceName.trim(),
      "appointmentDate" to appointmentDate.trim(),
      "appointmentTime" to appointmentTime.trim(),
      "status" to status,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (customerId.isNotBlank()) map["customerId"] = customerId.trim()
    if (customerPhone.isNotBlank()) map["customerPhone"] = customerPhone.trim()
    map["price"] = price
    if (stylist.isNotBlank()) map["stylist"] = stylist.trim()
    if (notes.isNotBlank()) map["notes"] = notes.trim()
    return map
  }
}
