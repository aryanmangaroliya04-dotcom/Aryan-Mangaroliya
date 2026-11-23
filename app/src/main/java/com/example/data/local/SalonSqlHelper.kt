package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.data.model.Appointment
import com.example.data.model.Customer

data class SqlQueryResult(
  val success: Boolean,
  val sql: String,
  val columns: List<String> = emptyList(),
  val rows: List<List<String>> = emptyList(),
  val rowCount: Int = 0,
  val executionTimeMs: Long = 0L,
  val errorMessage: String? = null
)

class SalonSqlHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  companion object {
    const val DATABASE_NAME = "salon_luxe_local.db"
    const val DATABASE_VERSION = 1

    const val TABLE_CUSTOMERS = "customers"
    const val TABLE_APPOINTMENTS = "appointments"

    val PRESETS = listOf(
      "SELECT name, phone_no, email, preferred_stylist FROM customers ORDER BY name ASC;",
      "SELECT service_name, COUNT(*) AS total_bookings, SUM(price) AS revenue FROM appointments GROUP BY service_name;",
      "SELECT c.name, c.phone_no, a.service_name, a.appointment_date, a.status FROM customers c JOIN appointments a ON c.id = a.customer_id;",
      "SELECT * FROM customers WHERE email LIKE '%@gmail.com' OR email LIKE '%@example.com';",
      "SELECT preferred_stylist, COUNT(*) AS client_count FROM customers GROUP BY preferred_stylist;"
    )
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS $TABLE_CUSTOMERS (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        phone_no TEXT NOT NULL,
        dob TEXT,
        email TEXT NOT NULL,
        address TEXT,
        preferred_stylist TEXT,
        notes TEXT,
        total_visits INTEGER DEFAULT 0
      );
      """.trimIndent()
    )

    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS $TABLE_APPOINTMENTS (
        id TEXT PRIMARY KEY,
        customer_id TEXT,
        customer_name TEXT NOT NULL,
        customer_phone TEXT,
        service_name TEXT NOT NULL,
        price INTEGER DEFAULT 0,
        stylist TEXT,
        appointment_date TEXT NOT NULL,
        appointment_time TEXT NOT NULL,
        status TEXT NOT NULL,
        notes TEXT
      );
      """.trimIndent()
    )
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS $TABLE_CUSTOMERS")
    db.execSQL("DROP TABLE IF EXISTS $TABLE_APPOINTMENTS")
    onCreate(db)
  }

  fun syncWithFirebase(customers: List<Customer>, appointments: List<Appointment>) {
    val db = writableDatabase
    db.beginTransaction()
    try {
      db.delete(TABLE_CUSTOMERS, null, null)
      for (c in customers) {
        val cv = ContentValues().apply {
          put("id", c.id)
          put("name", c.name)
          put("phone_no", c.phoneNo)
          put("dob", c.dob)
          put("email", c.email)
          put("address", c.address)
          put("preferred_stylist", c.preferredStylist)
          put("notes", c.notes)
          put("total_visits", c.totalVisits)
        }
        db.insertWithOnConflict(TABLE_CUSTOMERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
      }

      db.delete(TABLE_APPOINTMENTS, null, null)
      for (a in appointments) {
        val cv = ContentValues().apply {
          put("id", a.id)
          put("customer_id", a.customerId)
          put("customer_name", a.customerName)
          put("customer_phone", a.customerPhone)
          put("service_name", a.serviceName)
          put("price", a.price)
          put("stylist", a.stylist)
          put("appointment_date", a.appointmentDate)
          put("appointment_time", a.appointmentTime)
          put("status", a.status)
          put("notes", a.notes)
        }
        db.insertWithOnConflict(TABLE_APPOINTMENTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
      }
      db.setTransactionSuccessful()
    } finally {
      db.endTransaction()
    }
  }

  fun executeRawQuery(sqlQuery: String): SqlQueryResult {
    val trimmed = sqlQuery.trim().trimEnd(';')
    val startTime = System.currentTimeMillis()
    val db = readableDatabase

    return try {
      val isSelect = trimmed.startsWith("SELECT", ignoreCase = true) ||
                     trimmed.startsWith("PRAGMA", ignoreCase = true) ||
                     trimmed.startsWith("EXPLAIN", ignoreCase = true)

      if (isSelect) {
        val cursor: Cursor = db.rawQuery(trimmed, null)
        cursor.use { c ->
          val columns = c.columnNames.toList()
          val rows = mutableListOf<List<String>>()
          while (c.moveToNext()) {
            val row = mutableListOf<String>()
            for (i in 0 until c.columnCount) {
              row.add(c.getString(i) ?: "NULL")
            }
            rows.add(row)
          }
          val duration = System.currentTimeMillis() - startTime
          SqlQueryResult(
            success = true,
            sql = trimmed,
            columns = columns,
            rows = rows,
            rowCount = rows.size,
            executionTimeMs = duration
          )
        }
      } else {
        val wdb = writableDatabase
        wdb.execSQL(trimmed)
        val duration = System.currentTimeMillis() - startTime
        SqlQueryResult(
          success = true,
          sql = trimmed,
          columns = listOf("Status"),
          rows = listOf(listOf("Command executed successfully")),
          rowCount = 1,
          executionTimeMs = duration
        )
      }
    } catch (e: Exception) {
      val duration = System.currentTimeMillis() - startTime
      SqlQueryResult(
        success = false,
        sql = trimmed,
        errorMessage = e.message ?: "SQL execution error",
        executionTimeMs = duration
      )
    }
  }
}
