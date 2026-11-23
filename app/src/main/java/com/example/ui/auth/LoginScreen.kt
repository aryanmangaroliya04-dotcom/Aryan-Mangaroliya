package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary

val PlmtBackgroundBrush = Brush.verticalGradient(
  listOf(Color(0xFF1B1524), Color(0xFF120E19), Color(0xFF0D0A12))
)

@Composable
fun LoginScreen(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Attempt silent auto sign-in once when screen loads
  LaunchedEffect(Unit) {
    AuthHelper.attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(PlmtBackgroundBrush)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 36.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Luxury Salon Emblem
      Box(
        modifier = Modifier
          .size(88.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(Color(0xFFD4AF37), Color(0xFF8B6516))
            )
          )
          .border(2.dp, GoldSecondary, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ContentCut,
          contentDescription = "Salon Luxe Scissors",
          tint = Color(0xFF1A1423),
          modifier = Modifier.size(44.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "SALON LUXE",
        style = MaterialTheme.typography.displayMedium,
        color = GoldPrimary,
        letterSpacing = 3.sp,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "Haute Coiffure & Aesthetic Atelier",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Capabilities Overview Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color(0xFF1F182A)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B2F4E))
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "SALON MANAGEMENT SUITE",
            style = MaterialTheme.typography.labelMedium,
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )
          Spacer(modifier = Modifier.height(14.dp))

          FeatureItem(
            icon = Icons.Default.PersonAdd,
            title = "Customer Application Form",
            desc = "Full client registration with Name, Phone, DOB, Email & Address"
          )
          Spacer(modifier = Modifier.height(10.dp))
          FeatureItem(
            icon = Icons.Default.Sync,
            title = "Connected to Firebase",
            desc = "Live multi-user Firestore database with zero-trust security rules"
          )
          Spacer(modifier = Modifier.height(10.dp))
          FeatureItem(
            icon = Icons.Default.DataObject,
            title = "Dummy Data & SQL Studio",
            desc = "Seed realistic clients and run SQL queries against synced database"
          )
          Spacer(modifier = Modifier.height(10.dp))
          FeatureItem(
            icon = Icons.Default.Html,
            title = "HTML & CSS Client Cards",
            desc = "Inspect and render client profiles with custom CSS styling"
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Sign In Button
      Button(
        onClick = {
          isLoading = true
          errorMessage = null
          AuthHelper.onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
              isLoading = false
              onAuthSuccess()
            },
            onAuthError = { error ->
              isLoading = false
              errorMessage = error
            },
            scope = scope,
            onAuthCancelled = {
              isLoading = false
            }
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("login_button"),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = GoldPrimary,
          contentColor = Color(0xFF1E1400)
        ),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color(0xFF1E1400),
            strokeWidth = 2.5.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Signing in...",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = Color.White,
              modifier = Modifier.size(26.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "G",
                  color = Color(0xFF4285F4),
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Sign in with Google",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF381519)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B2B36))
        ) {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFFB4AB),
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
      Text(
        text = "Secure Google Authentication & Firestore Cloud Protection",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun FeatureItem(
  icon: ImageVector,
  title: String,
  desc: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFF2C223C)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = GoldPrimary,
        modifier = Modifier.size(18.dp)
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = desc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
