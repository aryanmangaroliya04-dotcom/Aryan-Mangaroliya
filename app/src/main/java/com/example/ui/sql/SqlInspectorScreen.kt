package com.example.ui.sql

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SalonSqlHelper
import com.example.data.local.SqlQueryResult
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary

@Composable
fun SqlInspectorScreen(
  sqlHelper: SalonSqlHelper,
  modifier: Modifier = Modifier
) {
  var sqlInput by remember {
    mutableStateOf(SalonSqlHelper.PRESETS.first())
  }
  var queryResult by remember { mutableStateOf<SqlQueryResult?>(null) }
  var isSchemaExpanded by remember { mutableStateOf(false) }

  // Execute initial preset query on load
  LaunchedEffect(Unit) {
    queryResult = sqlHelper.executeRawQuery(sqlInput)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF131017))
      .padding(16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF282035)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DataObject,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "SQL Query Studio",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Query SQLite cache synced from Firebase",
            style = MaterialTheme.typography.bodySmall,
            color = GoldSecondary
          )
        }
      }

      Button(
        onClick = { isSchemaExpanded = !isSchemaExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFF2B213B),
          contentColor = GoldSecondary
        ),
        border = BorderStroke(1.dp, Color(0xFF4C3B63))
      ) {
        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isSchemaExpanded) "Hide DDL" else "Schema", style = MaterialTheme.typography.labelSmall)
      }
    }

    if (isSchemaExpanded) {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1525)),
        border = BorderStroke(1.dp, Color(0xFF413357))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text("DATABASE TABLES & COLUMNS", style = MaterialTheme.typography.labelSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "• customers (id, name, phone_no, dob, email, address, preferred_stylist, notes, total_visits)\n• appointments (id, customer_id, customer_name, customer_phone, service_name, price, stylist, appointment_date, appointment_time, status, notes)",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Color(0xFFD4C8E0)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Preset Queries Chips
    Text(
      text = "PRESET SQL QUERIES",
      style = MaterialTheme.typography.labelSmall,
      color = GoldPrimary,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(6.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(SalonSqlHelper.PRESETS) { preset ->
        val label = when {
          preset.contains("GROUP BY service_name") -> "Revenue by Service"
          preset.contains("JOIN") -> "Customer & Bookings Join"
          preset.contains("email LIKE") -> "Search by Email"
          preset.contains("GROUP BY preferred_stylist") -> "Clients per Stylist"
          else -> "All Customers List"
        }
        FilterChip(
          selected = sqlInput == preset,
          onClick = {
            sqlInput = preset
            queryResult = sqlHelper.executeRawQuery(preset)
          },
          label = { Text(label, style = MaterialTheme.typography.labelSmall) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF382914),
            selectedLabelColor = GoldSecondary,
            containerColor = Color(0xFF221A2E),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          ),
          border = BorderStroke(1.dp, if (sqlInput == preset) GoldPrimary else Color(0xFF3B2E4E))
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Query Input Field
    OutlinedTextField(
      value = sqlInput,
      onValueChange = { sqlInput = it },
      label = { Text("SQL Statement") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("sql_query_input"),
      maxLines = 3,
      shape = RoundedCornerShape(12.dp),
      textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldPrimary,
        unfocusedBorderColor = Color(0xFF45365A),
        focusedTextColor = Color(0xFFF7F3FA),
        unfocusedTextColor = Color(0xFFF7F3FA)
      )
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Execute Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = queryResult?.let { "${it.rowCount} rows in ${it.executionTimeMs}ms" } ?: "",
        style = MaterialTheme.typography.labelSmall,
        color = GoldSecondary
      )

      Button(
        onClick = {
          queryResult = sqlHelper.executeRawQuery(sqlInput)
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldPrimary,
          contentColor = Color(0xFF1E1400)
        ),
        modifier = Modifier.testTag("run_sql_button")
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Execute Query", fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Query Results Grid
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1423)),
      border = BorderStroke(1.dp, Color(0xFF3B2D4F))
    ) {
      val res = queryResult
      if (res == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Run a query to inspect data", color = Color.Gray)
        }
      } else if (!res.success) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("SQL ERROR", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE05353), fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(res.errorMessage ?: "Unknown error", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFB4AB))
        }
      } else if (res.rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Query returned 0 rows", color = Color.Gray)
        }
      } else {
        // Tabular Results
        Box(
          modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
        ) {
          Column {
            // Header Row
            Row(
              modifier = Modifier
                .background(Color(0xFF261D33))
                .border(BorderStroke(1.dp, Color(0xFF3E3152)))
                .padding(vertical = 10.dp)
            ) {
              res.columns.forEach { col ->
                Box(
                  modifier = Modifier
                    .width(160.dp)
                    .padding(horizontal = 12.dp)
                ) {
                  Text(
                    text = col.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Data Rows
            res.rows.forEachIndexed { index, row ->
              val rowBg = if (index % 2 == 0) Color(0xFF1B1525) else Color(0xFF16111E)
              Row(
                modifier = Modifier
                  .background(rowBg)
                  .padding(vertical = 8.dp)
              ) {
                row.forEach { cell ->
                  Box(
                    modifier = Modifier
                      .width(160.dp)
                      .padding(horizontal = 12.dp)
                  ) {
                    Text(
                      text = cell,
                      style = MaterialTheme.typography.bodySmall,
                      color = if (cell == "NULL") Color.Gray else Color(0xFFF7F3FA)
                    )
                  }
                }
              }
              HorizontalDivider(color = Color(0xFF251E30), thickness = 0.5.dp)
            }
          }
        }
      }
    }
  }
}
