package com.example.ui.directory

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SalonSqlHelper
import com.example.data.model.Appointment
import com.example.data.model.Customer
import com.example.data.repository.SalonRepository
import com.example.ui.customer.SALON_SERVICES
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusConfirmed
import com.example.ui.theme.StatusPending
import com.example.util.HtmlCssGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Composable
fun CustomerDirectoryScreen(
  userId: String,
  repository: SalonRepository,
  sqlHelper: SalonSqlHelper,
  onNavigateToForm: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()

  var observerError by remember { mutableStateOf<String?>(null) }

  // Collect real-time Firestore flows with stream error protection
  val customers by remember(userId, repository) {
    repository.observeCustomers(userId).catch { error ->
      if (error is CancellationException) throw error
      observerError = error.localizedMessage ?: "Unable to sync clients from Firebase."
    }
  }.collectAsState(initial = emptyList())

  val appointments by remember(userId, repository) {
    repository.observeAppointments(userId).catch { error ->
      if (error is CancellationException) throw error
      observerError = error.localizedMessage ?: "Unable to sync appointments from Firebase."
    }
  }.collectAsState(initial = emptyList())

  // Keep SQLite synchronized with Firestore records
  androidx.compose.runtime.LaunchedEffect(customers, appointments) {
    if (customers.isNotEmpty() || appointments.isNotEmpty()) {
      sqlHelper.syncWithFirebase(customers, appointments)
    }
  }

  var selectedTab by remember { mutableIntStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }
  var stylistFilter by remember { mutableStateOf("All") }

  // Action Dialog States
  var isSeedingData by remember { mutableStateOf(false) }
  var customerForHtmlDialog by remember { mutableStateOf<Customer?>(null) }
  var customerForBookingDialog by remember { mutableStateOf<Customer?>(null) }
  var snackbarMessage by remember { mutableStateOf<String?>(null) }

  val filteredCustomers = customers.filter { c ->
    val matchesSearch = searchQuery.isBlank() ||
      c.name.contains(searchQuery, ignoreCase = true) ||
      c.phoneNo.contains(searchQuery, ignoreCase = true) ||
      c.email.contains(searchQuery, ignoreCase = true) ||
      c.address.contains(searchQuery, ignoreCase = true)

    val matchesStylist = stylistFilter == "All" || c.preferredStylist.contains(stylistFilter, ignoreCase = true)
    matchesSearch && matchesStylist
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF131017))
  ) {
    // Top Bar Actions
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Client Roster & Bookings",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${customers.size} clients & ${appointments.size} appointments in Firebase",
          style = MaterialTheme.typography.bodySmall,
          color = GoldSecondary
        )
      }

      // Seed Dummy Data Button
      Button(
        onClick = {
          isSeedingData = true
          scope.launch {
            try {
              repository.seedDummyData(userId)
              snackbarMessage = "5 realistic dummy clients seeded to Firebase!"
            } catch (e: Exception) {
              snackbarMessage = "Seed failed: ${e.message}"
            } finally {
              isSeedingData = false
            }
          }
        },
        enabled = !isSeedingData,
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF332714),
          contentColor = GoldPrimary
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GoldPrimary),
        modifier = Modifier.testTag("seed_dummy_data_button")
      ) {
        if (isSeedingData) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GoldPrimary, strokeWidth = 2.dp)
        } else {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Seed Dummy Data", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
      }
    }

    val statusBanner = observerError ?: snackbarMessage
    if (statusBanner != null) {
      val isErrorBanner = observerError != null || statusBanner.contains("failed", ignoreCase = true)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isErrorBanner) Color(0xFF381519) else Color(0xFF1F2A22)
        ),
        border = BorderStroke(1.dp, if (isErrorBanner) Color(0xFF8B2B36) else Color(0xFF2E7D4F))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = statusBanner,
            style = MaterialTheme.typography.bodySmall,
            color = if (isErrorBanner) Color(0xFFFFB4AB) else Color(0xFFB4F2D0),
            modifier = Modifier.weight(1f)
          )
          IconButton(
            onClick = {
              observerError = null
              snackbarMessage = null
            },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Dismiss message",
              tint = Color(0xFFC7BCCE),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = Color(0xFF1A1422),
      contentColor = GoldPrimary,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = GoldPrimary,
          height = 3.dp
        )
      }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text("Customers (${filteredCustomers.size})", fontWeight = FontWeight.SemiBold)
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text("Appointments (${appointments.size})", fontWeight = FontWeight.SemiBold)
        }
      )
    }

    if (selectedTab == 0) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by name, phone, email, address...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
        trailingIcon = if (searchQuery.isNotEmpty()) {
          {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.Gray)
            }
          }
        } else null,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .testTag("customer_search_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GoldPrimary,
          unfocusedBorderColor = Color(0xFF382A4A),
          focusedTextColor = Color(0xFFF7F3FA),
          unfocusedTextColor = Color(0xFFF7F3FA)
        )
      )

      // Stylist Filter Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val stylists = listOf("All", "Elena", "Marcus", "Sophie", "David")
        items(stylists) { s ->
          FilterChip(
            selected = stylistFilter == s,
            onClick = { stylistFilter = s },
            label = { Text(s, style = MaterialTheme.typography.labelSmall) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF3D2F13),
              selectedLabelColor = GoldSecondary,
              containerColor = Color(0xFF221A2D),
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = BorderStroke(1.dp, if (stylistFilter == s) GoldPrimary else Color(0xFF3A2D4A))
          )
        }
      }

      // Customer List
      if (filteredCustomers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = Color(0xFF5D4D70),
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = if (customers.isEmpty()) "No clients found in Firebase" else "No matching clients",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (customers.isEmpty()) "Tap 'Seed Dummy Data' or register a new customer in the Application Form." else "Try adjusting your search query or filter.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF8E8398)
            )
            if (customers.isEmpty()) {
              Spacer(modifier = Modifier.height(18.dp))
              Button(
                onClick = onNavigateToForm,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF1E1400))
              ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Customer Form")
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredCustomers, key = { it.id }) { customer ->
            CustomerCardItem(
              customer = customer,
              onBookClick = { customerForBookingDialog = customer },
              onHtmlClick = { customerForHtmlDialog = customer },
              onDeleteClick = {
                scope.launch {
                  try {
                    repository.deleteCustomer(customer.id)
                    snackbarMessage = "${customer.name} removed from Firebase"
                  } catch (e: Exception) {
                    snackbarMessage = "Delete failed: ${e.message}"
                  }
                }
              }
            )
          }
          item {
            Spacer(modifier = Modifier.height(72.dp))
          }
        }
      }
    } else {
      // Appointments Tab
      if (appointments.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = Color(0xFF5D4D70),
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "No appointments booked yet",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Schedule an appointment from the Customer Form or click 'Seed Dummy Data'.",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF8E8398)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(appointments, key = { it.id }) { appointment ->
            AppointmentCardItem(
              appointment = appointment,
              onStatusChange = { newStatus ->
                scope.launch {
                  try {
                    repository.updateAppointmentStatus(appointment.id, newStatus)
                    snackbarMessage = "Booking status updated to $newStatus"
                  } catch (e: Exception) {
                    snackbarMessage = "Update failed: ${e.message}"
                  }
                }
              },
              onDelete = {
                scope.launch {
                  try {
                    repository.deleteAppointment(appointment.id)
                    snackbarMessage = "Appointment deleted"
                  } catch (e: Exception) {
                    snackbarMessage = "Delete failed: ${e.message}"
                  }
                }
              }
            )
          }
          item {
            Spacer(modifier = Modifier.height(72.dp))
          }
        }
      }
    }
  }

  // HTML & CSS Preview Dialog
  customerForHtmlDialog?.let { customer ->
    val appointmentForCustomer = appointments.firstOrNull { it.customerId == customer.id }
    HtmlCssCardDialog(
      customer = customer,
      appointment = appointmentForCustomer,
      onDismiss = { customerForHtmlDialog = null }
    )
  }

  // Quick Booking Dialog
  customerForBookingDialog?.let { customer ->
    QuickBookingDialog(
      customer = customer,
      onDismiss = { customerForBookingDialog = null },
      onConfirm = { service, price, stylist, date, time, notes ->
        scope.launch {
          try {
            val apt = Appointment(
              userId = userId,
              customerId = customer.id,
              customerName = customer.name,
              customerPhone = customer.phoneNo,
              serviceName = service,
              price = price,
              stylist = stylist,
              appointmentDate = date,
              appointmentTime = time,
              status = "Confirmed",
              notes = notes
            )
            repository.addAppointment(apt)
            // Increment customer total visits
            repository.updateCustomer(customer.copy(totalVisits = customer.totalVisits + 1))
            snackbarMessage = "Appointment confirmed for ${customer.name}!"
            customerForBookingDialog = null
          } catch (e: Exception) {
            snackbarMessage = "Booking failed: ${e.message}"
          }
        }
      }
    )
  }
}

@Composable
private fun CustomerCardItem(
  customer: Customer,
  onBookClick: () -> Unit,
  onHtmlClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1726)),
    border = BorderStroke(1.dp, Color(0xFF3A2E4B))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Initials Avatar
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                  listOf(GoldPrimary, Color(0xFF8B6516))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = customer.name.take(1).uppercase(),
              style = MaterialTheme.typography.titleMedium,
              color = Color(0xFF1A1423),
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = customer.name,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = customer.preferredStylist.ifBlank { "Any Senior Stylist" },
              style = MaterialTheme.typography.bodySmall,
              color = GoldSecondary
            )
          }
        }

        // Visits badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C223A))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${customer.totalVisits} visits",
            style = MaterialTheme.typography.labelSmall,
            color = GoldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = Color(0xFF2D243A), thickness = 1.dp)
      Spacer(modifier = Modifier.height(10.dp))

      // Contact Details Grid
      Row(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF8E8398), modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = customer.phoneNo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        if (customer.dob.isNotBlank()) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFF8E8398), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = customer.dob,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF8E8398), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = customer.email,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (customer.address.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF8E8398), modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = customer.address,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      if (customer.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF241C2E))
            .padding(8.dp)
        ) {
          Text(
            text = "“${customer.notes}”",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFC7BCCE)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Book Appointment
        Button(
          onClick = onBookClick,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1E1400)
          )
        ) {
          Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Book", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }

        // HTML/CSS Card Viewer
        OutlinedButton(
          onClick = onHtmlClick,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, Color(0xFF5E4973)),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSecondary)
        ) {
          Icon(Icons.Default.Html, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("HTML/CSS", style = MaterialTheme.typography.labelSmall)
        }

        // Delete
        IconButton(
          onClick = onDeleteClick,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete customer",
            tint = Color(0xFFE05353),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun AppointmentCardItem(
  appointment: Appointment,
  onStatusChange: (String) -> Unit,
  onDelete: () -> Unit
) {
  val statusColor = when (appointment.status) {
    "Confirmed" -> StatusConfirmed
    "Completed" -> StatusCompleted
    "Cancelled" -> StatusCancelled
    else -> StatusPending
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1828)),
    border = BorderStroke(1.dp, Color(0xFF3A2D4B))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = appointment.customerName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = appointment.serviceName,
            style = MaterialTheme.typography.bodyMedium,
            color = GoldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(statusColor.copy(alpha = 0.2f))
            .border(1.dp, statusColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = appointment.status,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF8E8398), modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${appointment.appointmentDate} at ${appointment.appointmentTime}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Text(
          text = "$${appointment.price}",
          style = MaterialTheme.typography.titleSmall,
          color = GoldSecondary,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color(0xFF8E8398), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Stylist: ${appointment.stylist}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = Color(0xFF2E243A), thickness = 1.dp)
      Spacer(modifier = Modifier.height(8.dp))

      // Quick Status Changers
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        listOf("Confirmed", "Completed", "Cancelled").forEach { status ->
          if (appointment.status != status) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF292036))
                .clickable { onStatusChange(status) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Mark $status",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE05353), modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
private fun HtmlCssCardDialog(
  customer: Customer,
  appointment: Appointment?,
  onDismiss: () -> Unit
) {
  var showSourceCode by remember { mutableStateOf(false) }
  val htmlCode = remember(customer, appointment) {
    HtmlCssGenerator.generateCustomerHtml(customer, appointment)
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Client HTML/CSS Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row {
          TextButton(onClick = { showSourceCode = !showSourceCode }) {
            Text(if (showSourceCode) "View Card" else "View Source", color = GoldPrimary)
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
          }
        }
      }
    },
    text = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(380.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF14101A))
          .border(1.dp, Color(0xFF3C2E4E), RoundedCornerShape(12.dp))
          .padding(14.dp)
      ) {
        if (showSourceCode) {
          Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
              text = htmlCode,
              style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
              color = Color(0xFFD4C8E0)
            )
          }
        } else {
          // Native representation styled according to the HTML/CSS specs
          Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(50.dp)
                  .clip(CircleShape)
                  .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(GoldPrimary, Color(0xFF8B6516)))),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = customer.name.take(1).uppercase(),
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF141118)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("VERIFIED SALON CLIENT", style = MaterialTheme.typography.labelSmall, color = GoldPrimary, letterSpacing = 1.sp)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF241C2E))
                .padding(12.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("PHONE: ${customer.phoneNo}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF7F3FA))
                Text("DOB: ${customer.dob.ifBlank { "Not specified" }}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF7F3FA))
                Text("EMAIL: ${customer.email}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF7F3FA))
                Text("ADDRESS: ${customer.address.ifBlank { "On File" }}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF7F3FA))
                Text("STYLIST: ${customer.preferredStylist.ifBlank { "Senior Stylist" }}", style = MaterialTheme.typography.bodySmall, color = GoldSecondary)
                Text("VISITS: ${customer.totalVisits}", style = MaterialTheme.typography.bodySmall, color = GoldPrimary)
              }
            }

            if (appointment != null) {
              Spacer(modifier = Modifier.height(10.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                  .background(Color(0xFF1B1525))
                  .padding(10.dp)
              ) {
                Column {
                  Text("UPCOMING BOOKING: ${appointment.serviceName}", style = MaterialTheme.typography.labelMedium, color = GoldSecondary, fontWeight = FontWeight.Bold)
                  Text("Date: ${appointment.appointmentDate} at ${appointment.appointmentTime} ($${appointment.price})", style = MaterialTheme.typography.bodySmall, color = Color.White)
                  Text("Status: ${appointment.status}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4EBA87))
                }
              }
            }

            if (customer.notes.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text("“${customer.notes}”", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC7BCCE), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF1E1400))
      ) {
        Text("Done")
      }
    },
    containerColor = Color(0xFF1F1828)
  )
}

@Composable
private fun QuickBookingDialog(
  customer: Customer,
  onDismiss: () -> Unit,
  onConfirm: (service: String, price: Long, stylist: String, date: String, time: String, notes: String) -> Unit
) {
  var selectedService by remember { mutableStateOf(SALON_SERVICES.first()) }
  var date by remember { mutableStateOf("2026-10-27") }
  var time by remember { mutableStateOf("15:00") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Book Appointment for ${customer.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text("Select Service", style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        SALON_SERVICES.forEach { servicePair ->
          val isSelected = selectedService == servicePair
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) Color(0xFF332714) else Color(0xFF282035))
              .clickable { selectedService = servicePair }
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(servicePair.first, style = MaterialTheme.typography.bodySmall, color = if (isSelected) GoldSecondary else Color.White)
            Text("$${servicePair.second}", style = MaterialTheme.typography.labelMedium, color = GoldPrimary, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Special Requests") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            selectedService.first,
            selectedService.second,
            customer.preferredStylist.ifBlank { "Elena Vance" },
            date,
            time,
            notes
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF1E1400))
      ) {
        Text("Confirm Booking")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Color.Gray)
      }
    },
    containerColor = Color(0xFF1E1727)
  )
}
