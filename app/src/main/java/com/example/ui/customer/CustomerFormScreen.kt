package com.example.ui.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.Customer
import com.example.data.repository.SalonRepository
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

val STYLISTS = listOf(
  "Elena Vance (Master Colorist)",
  "Marcus Bell (Master Barber)",
  "Sophie Lin (Hair Restoration)",
  "David Kim (Styling Director)"
)

val SALON_SERVICES = listOf(
  Pair("Balayage Hair Color & Blowout", 220L),
  Pair("Signature Haircut & Style", 85L),
  Pair("Gentleman's Beard Sculpt & Cut", 75L),
  Pair("Keratin Silk Smoothing", 290L),
  Pair("HydraFacial Glow Treatment", 160L),
  Pair("Champagne Spa Gel Manicure", 65L),
  Pair("Deep Scalp Detox & Massage", 95L)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
  userId: String,
  repository: SalonRepository,
  onNavigateToDirectory: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()

  // Form Fields
  var name by remember { mutableStateOf("") }
  var phoneNo by remember { mutableStateOf("") }
  var dob by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var preferredStylist by remember { mutableStateOf(STYLISTS.first()) }
  var notes by remember { mutableStateOf("") }

  // Initial Appointment option
  var scheduleAppointment by remember { mutableStateOf(true) }
  var selectedService by remember { mutableStateOf(SALON_SERVICES.first()) }
  var appointmentDate by remember { mutableStateOf("2026-10-26") }
  var appointmentTime by remember { mutableStateOf("14:00") }

  // Form State
  var isSubmitting by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }

  val canSubmit = name.isNotBlank() && phoneNo.isNotBlank() && email.isNotBlank() && !isSubmitting

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF131017))
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 24.dp)
  ) {
    // Header
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(Color(0xFF282035)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = null,
          tint = GoldPrimary,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(
          text = "Client Application",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Customer Profile & Salon Appointment Form",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Form Container
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1726)),
      border = BorderStroke(1.dp, Color(0xFF3B2E4E))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text(
          text = "PERSONAL & CONTACT DETAILS",
          style = MaterialTheme.typography.labelSmall,
          color = GoldPrimary,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Customer Full Name *") },
          placeholder = { Text("e.g. Evelyn Harper") },
          leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_name_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFF45365A),
            focusedLabelColor = GoldPrimary,
            focusedTextColor = Color(0xFFF7F3FA),
            unfocusedTextColor = Color(0xFFF7F3FA)
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Phone No
        OutlinedTextField(
          value = phoneNo,
          onValueChange = { phoneNo = it },
          label = { Text("Phone Number *") },
          placeholder = { Text("+1 (555) 000-0000") },
          leadingIcon = {
            Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_phone_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFF45365A),
            focusedLabelColor = GoldPrimary,
            focusedTextColor = Color(0xFFF7F3FA),
            unfocusedTextColor = Color(0xFFF7F3FA)
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. DOB (Date of Birth)
        OutlinedTextField(
          value = dob,
          onValueChange = { dob = it },
          label = { Text("Date of Birth (DOB)") },
          placeholder = { Text("YYYY-MM-DD (e.g. 1996-05-18)") },
          leadingIcon = {
            Icon(Icons.Default.Cake, contentDescription = null, tint = GoldPrimary)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_dob_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFF45365A),
            focusedLabelColor = GoldPrimary,
            focusedTextColor = Color(0xFFF7F3FA),
            unfocusedTextColor = Color(0xFFF7F3FA)
          )
        )

        // Quick DOB helper chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("1995-04-12", "1998-08-25", "1990-11-30", "2001-02-14").forEach { sampleDob ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2B2238))
                .clickable { dob = sampleDob }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = sampleDob,
                style = MaterialTheme.typography.labelSmall,
                color = GoldSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Email
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email Address *") },
          placeholder = { Text("client@example.com") },
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_email_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFF45365A),
            focusedLabelColor = GoldPrimary,
            focusedTextColor = Color(0xFFF7F3FA),
            unfocusedTextColor = Color(0xFFF7F3FA)
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Address
        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Physical Address") },
          placeholder = { Text("e.g. 742 Evergreen Terrace, Suite 300") },
          leadingIcon = {
            Icon(Icons.Default.Home, contentDescription = null, tint = GoldPrimary)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_address_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFF45365A),
            focusedLabelColor = GoldPrimary,
            focusedTextColor = Color(0xFFF7F3FA),
            unfocusedTextColor = Color(0xFFF7F3FA)
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preferred Stylist
        Text(
          text = "PREFERRED STYLIST",
          style = MaterialTheme.typography.labelSmall,
          color = GoldPrimary,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(STYLISTS) { stylist ->
            FilterChip(
              selected = preferredStylist == stylist,
              onClick = { preferredStylist = stylist },
              label = {
                Text(
                  text = stylist.substringBefore(" ("),
                  style = MaterialTheme.typography.bodySmall
                )
              },
              leadingIcon = if (preferredStylist == stylist) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
              } else null,
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF3D2F13),
                selectedLabelColor = GoldSecondary,
                containerColor = Color(0xFF282035),
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              border = BorderStroke(
                1.dp,
                if (preferredStylist == stylist) GoldPrimary else Color(0xFF45365A)
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Notes & Preferences
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Hair & Skin Notes / Special Requests") },
          placeholder = { Text("e.g. Sensitive scalp, allergy to ammonia, prefers tea") },
          leadingIcon = {
            Icon(Icons.Default.Notes, contentDescription = null, tint = GoldPrimary)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_notes_input"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFF45365A),
            focusedLabelColor = GoldPrimary,
            focusedTextColor = Color(0xFFF7F3FA),
            unfocusedTextColor = Color(0xFFF7F3FA)
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Initial Appointment Booking Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1726)),
      border = BorderStroke(1.dp, Color(0xFF3B2E4E))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "SCHEDULE INITIAL APPOINTMENT",
              style = MaterialTheme.typography.labelSmall,
              color = GoldPrimary,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            Text(
              text = "Automatically create a salon booking",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = scheduleAppointment,
            onCheckedChange = { scheduleAppointment = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = GoldPrimary,
              checkedTrackColor = Color(0xFF3D2F13),
              uncheckedThumbColor = Color(0xFF8E8398),
              uncheckedTrackColor = Color(0xFF282035)
            )
          )
        }

        AnimatedVisibility(visible = scheduleAppointment) {
          Column(modifier = Modifier.padding(top = 14.dp)) {
            Text(
              text = "Select Salon Service",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            SALON_SERVICES.forEach { servicePair ->
              val isSelected = selectedService == servicePair
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) Color(0xFF33271A) else Color(0xFF251E30))
                  .clickable { selectedService = servicePair }
                  .border(
                    1.dp,
                    if (isSelected) GoldPrimary else Color(0xFF3D314F),
                    RoundedCornerShape(8.dp)
                  )
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = null,
                    tint = if (isSelected) GoldPrimary else Color(0xFF8E8398),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = servicePair.first,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) GoldSecondary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                }
                Text(
                  text = "$${servicePair.second}",
                  style = MaterialTheme.typography.labelMedium,
                  color = GoldPrimary,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              OutlinedTextField(
                value = appointmentDate,
                onValueChange = { appointmentDate = it },
                label = { Text("Date (YYYY-MM-DD)") },
                leadingIcon = {
                  Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GoldPrimary)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GoldPrimary,
                  unfocusedBorderColor = Color(0xFF45365A),
                  focusedLabelColor = GoldPrimary,
                  focusedTextColor = Color(0xFFF7F3FA),
                  unfocusedTextColor = Color(0xFFF7F3FA)
                )
              )

              OutlinedTextField(
                value = appointmentTime,
                onValueChange = { appointmentTime = it },
                label = { Text("Time (HH:MM)") },
                leadingIcon = {
                  Icon(Icons.Default.Schedule, contentDescription = null, tint = GoldPrimary)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GoldPrimary,
                  unfocusedBorderColor = Color(0xFF45365A),
                  focusedLabelColor = GoldPrimary,
                  focusedTextColor = Color(0xFFF7F3FA),
                  unfocusedTextColor = Color(0xFFF7F3FA)
                )
              )
            }
          }
        }
      }
    }

    if (errorMessage != null) {
      Spacer(modifier = Modifier.height(14.dp))
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF381519)),
        border = BorderStroke(1.dp, Color(0xFF8B2B36))
      ) {
        Text(
          text = errorMessage ?: "",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFFFB4AB),
          modifier = Modifier.padding(12.dp)
        )
      }
    }

    if (successMessage != null) {
      Spacer(modifier = Modifier.height(14.dp))
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF143021)),
        border = BorderStroke(1.dp, Color(0xFF2E7D4F))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4EBA87))
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = successMessage ?: "",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFB4F2D0),
              fontWeight = FontWeight.Bold
            )
          }
          TextButton(onClick = onNavigateToDirectory) {
            Text("View in List", color = GoldPrimary)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Submit Application Button
    Button(
      onClick = {
        val trimmedName = name.trim()
        val trimmedPhone = phoneNo.trim()
        val trimmedEmail = email.trim()
        val trimmedDate = appointmentDate.trim()
        val trimmedTime = appointmentTime.trim()
        val trimmedNotes = notes.trim()

        val validationError = when {
          trimmedName.isEmpty() || trimmedName.length > 100 -> "Customer name must be between 1 and 100 characters."
          trimmedPhone.length < 5 || trimmedPhone.length > 25 -> "Phone number must be between 5 and 25 characters."
          trimmedEmail.length < 3 || trimmedEmail.length > 120 -> "Email address must be between 3 and 120 characters."
          trimmedNotes.length > 500 -> "Notes must be 500 characters or fewer."
          scheduleAppointment && (trimmedDate.length < 4 || trimmedDate.length > 20) -> "Appointment date must be between 4 and 20 characters."
          scheduleAppointment && (trimmedTime.length < 2 || trimmedTime.length > 20) -> "Appointment time must be between 2 and 20 characters."
          else -> null
        }

        if (validationError != null) {
          errorMessage = validationError
          return@Button
        }

        isSubmitting = true
        errorMessage = null
        successMessage = null

        scope.launch {
          try {
            val stylistName = preferredStylist.substringBefore(" (")
            val newCustomer = Customer(
              userId = userId,
              name = trimmedName,
              phoneNo = trimmedPhone,
              dob = dob.trim().take(20),
              email = trimmedEmail,
              address = address.trim().take(200),
              preferredStylist = stylistName.take(100),
              notes = trimmedNotes,
              totalVisits = if (scheduleAppointment) 1L else 0L
            )

            val createdCustId = repository.addCustomer(newCustomer)

            if (scheduleAppointment) {
              val newAppointment = Appointment(
                userId = userId,
                customerId = createdCustId,
                customerName = trimmedName,
                customerPhone = trimmedPhone,
                serviceName = selectedService.first,
                price = selectedService.second,
                stylist = stylistName.take(100),
                appointmentDate = trimmedDate,
                appointmentTime = trimmedTime,
                status = "Confirmed",
                notes = trimmedNotes
              )
              repository.addAppointment(newAppointment)
            }

            isSubmitting = false
            successMessage = "Application submitted! $trimmedName successfully registered in Firebase."
            // Reset fields
            name = ""
            phoneNo = ""
            dob = ""
            email = ""
            address = ""
            notes = ""
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            isSubmitting = false
            errorMessage = e.localizedMessage ?: "Failed to save customer to Firebase"
          }
        }
      },
      enabled = canSubmit,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("submit_customer_button"),
      shape = RoundedCornerShape(26.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = GoldPrimary,
        contentColor = Color(0xFF1E1400)
      )
    ) {
      if (isSubmitting) {
        CircularProgressIndicator(
          modifier = Modifier.size(22.dp),
          color = Color(0xFF1E1400),
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Saving to Firebase...", fontWeight = FontWeight.Bold)
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Submit Application & Save to Firebase",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))
  }
}
