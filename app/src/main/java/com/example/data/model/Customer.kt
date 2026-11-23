package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class Customer(
  @DocumentId
  val id: String = "",
  val userId: String = "",
  val name: String = "",
  val phoneNo: String = "",
  val dob: String = "",
  val email: String = "",
  val address: String = "",
  val preferredStylist: String = "",
  val notes: String = "",
  val totalVisits: Long = 0L,
  @ServerTimestamp
  val createdAt: Timestamp? = null,
  @ServerTimestamp
  val updatedAt: Timestamp? = null
) {
  fun toCreateMap(uid: String): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
      "userId" to uid,
      "name" to name.trim(),
      "phoneNo" to phoneNo.trim(),
      "email" to email.trim(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (dob.isNotBlank()) map["dob"] = dob.trim()
    if (address.isNotBlank()) map["address"] = address.trim()
    if (preferredStylist.isNotBlank()) map["preferredStylist"] = preferredStylist.trim()
    if (notes.isNotBlank()) map["notes"] = notes.trim()
    if (totalVisits > 0L) map["totalVisits"] = totalVisits
    return map
  }

  fun toUpdateMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
      "name" to name.trim(),
      "phoneNo" to phoneNo.trim(),
      "email" to email.trim(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (dob.isNotBlank()) map["dob"] = dob.trim()
    if (address.isNotBlank()) map["address"] = address.trim()
    if (preferredStylist.isNotBlank()) map["preferredStylist"] = preferredStylist.trim()
    if (notes.isNotBlank()) map["notes"] = notes.trim()
    map["totalVisits"] = totalVisits
    return map
  }
}
