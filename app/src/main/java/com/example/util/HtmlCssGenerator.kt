package com.example.util

import com.example.data.model.Appointment
import com.example.data.model.Customer

object HtmlCssGenerator {

  fun generateCustomerHtml(customer: Customer, appointment: Appointment? = null): String {
    val statusColor = when (appointment?.status) {
      "Confirmed" -> "#4EBA87"
      "Completed" -> "#6B9BFA"
      "Cancelled" -> "#E05353"
      else -> "#E5A93B"
    }

    return """
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${customer.name} - Salon Luxe Client Profile</title>
        <style>
          :root {
            --bg-color: #141118;
            --card-bg: #1F1927;
            --card-border: #3A2F49;
            --gold-accent: #D4AF37;
            --gold-light: #F4E2AA;
            --text-main: #F7F3FA;
            --text-muted: #BBB0C4;
            --badge-bg: #2E253A;
          }
          * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
          }
          body {
            background-color: var(--bg-color);
            color: var(--text-main);
            padding: 20px;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
          }
          .salon-card {
            background: linear-gradient(145deg, #241D2E, #191421);
            border: 1px solid var(--card-border);
            border-radius: 16px;
            width: 100%;
            max-width: 440px;
            padding: 24px;
            box-shadow: 0 12px 30px rgba(0, 0, 0, 0.45);
            position: relative;
            overflow: hidden;
          }
          .salon-card::before {
            content: "";
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #D4AF37, #F4E2AA, #AA8220);
          }
          .header {
            display: flex;
            align-items: center;
            gap: 16px;
            margin-bottom: 20px;
          }
          .avatar {
            width: 58px;
            height: 58px;
            border-radius: 50%;
            background: linear-gradient(135deg, #D4AF37, #856417);
            color: #141118;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            font-weight: 700;
            border: 2px solid var(--gold-light);
          }
          .title-area h2 {
            font-size: 20px;
            font-weight: 700;
            color: var(--text-main);
            letter-spacing: 0.3px;
          }
          .title-area p {
            font-size: 13px;
            color: var(--gold-accent);
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 600;
          }
          .field-group {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
            margin-bottom: 18px;
            background: var(--badge-bg);
            padding: 14px;
            border-radius: 10px;
            border: 1px solid rgba(212, 175, 55, 0.15);
          }
          .field {
            display: flex;
            flex-direction: column;
          }
          .field.full {
            grid-column: span 2;
          }
          .label {
            font-size: 11px;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 3px;
          }
          .value {
            font-size: 13px;
            font-weight: 500;
            color: var(--text-main);
            word-break: break-word;
          }
          .booking-box {
            background: #191422;
            border: 1px dashed var(--gold-accent);
            border-radius: 10px;
            padding: 14px;
            margin-top: 14px;
          }
          .booking-title {
            font-size: 12px;
            color: var(--gold-light);
            text-transform: uppercase;
            letter-spacing: 0.8px;
            margin-bottom: 8px;
            display: flex;
            justify-content: space-between;
          }
          .status-tag {
            background: $statusColor;
            color: #141118;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
          }
          .notes-box {
            font-size: 12px;
            color: var(--text-muted);
            font-style: italic;
            margin-top: 12px;
            padding: 8px 12px;
            background: rgba(255, 255, 255, 0.03);
            border-left: 3px solid var(--gold-accent);
            border-radius: 4px;
          }
          .footer-brand {
            margin-top: 18px;
            text-align: right;
            font-size: 11px;
            color: var(--gold-accent);
            opacity: 0.8;
            letter-spacing: 1.5px;
          }
        </style>
      </head>
      <body>
        <div class="salon-card">
          <div class="header">
            <div class="avatar">${customer.name.take(1).uppercase()}</div>
            <div class="title-area">
              <h2>${customer.name}</h2>
              <p>Verified Salon Client</p>
            </div>
          </div>

          <div class="field-group">
            <div class="field">
              <span class="label">Phone</span>
              <span class="value">${customer.phoneNo}</span>
            </div>
            <div class="field">
              <span class="label">Date of Birth</span>
              <span class="value">${customer.dob.ifBlank { "Not provided" }}</span>
            </div>
            <div class="field full">
              <span class="label">Email Address</span>
              <span class="value">${customer.email}</span>
            </div>
            <div class="field full">
              <span class="label">Address</span>
              <span class="value">${customer.address.ifBlank { "On File" }}</span>
            </div>
            <div class="field">
              <span class="label">Preferred Stylist</span>
              <span class="value" style="color: var(--gold-light);">${customer.preferredStylist.ifBlank { "Any Senior Stylist" }}</span>
            </div>
            <div class="field">
              <span class="label">Total Visits</span>
              <span class="value">${customer.totalVisits} visits</span>
            </div>
          </div>

          ${
            if (appointment != null) {
              """
              <div class="booking-box">
                <div class="booking-title">
                  <span>Upcoming Appointment</span>
                  <span class="status-tag">${appointment.status}</span>
                </div>
                <div style="font-size: 14px; font-weight: 600; color: var(--gold-light); margin-bottom: 4px;">
                  ${appointment.serviceName} (${'$'}${appointment.price})
                </div>
                <div style="font-size: 12px; color: var(--text-muted);">
                  📅 ${appointment.appointmentDate} at ⏰ ${appointment.appointmentTime} &bull; Stylist: ${appointment.stylist}
                </div>
              </div>
              """.trimIndent()
            } else ""
          }

          ${
            if (customer.notes.isNotBlank()) {
              """<div class="notes-box">&ldquo;${customer.notes}&rdquo;</div>"""
            } else ""
          }

          <div class="footer-brand">&bull; SALON LUXE ATELIER &bull;</div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }
}
