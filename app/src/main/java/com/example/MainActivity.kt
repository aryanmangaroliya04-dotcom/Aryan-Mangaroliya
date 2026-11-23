package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.local.SalonSqlHelper
import com.example.data.repository.SalonRepository
import com.example.ui.auth.AuthHelper
import com.example.ui.auth.LoginScreen
import com.example.ui.customer.CustomerFormScreen
import com.example.ui.directory.CustomerDirectoryScreen
import com.example.ui.sql.SqlInspectorScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        AppContent()
      }
    }
  }
}

@Composable
fun AppContent() {
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  val user = currentUser
  if (user == null) {
    LoginScreen(
      onAuthSuccess = {
        currentUser = Firebase.auth.currentUser
      }
    )
  } else {
    key(user.uid) {
      SalonMainScreen(
        userId = user.uid,
        userEmail = user.email ?: "Stylist",
        displayName = user.displayName ?: "Stylist"
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonMainScreen(
  userId: String,
  userEmail: String,
  displayName: String
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  val repository = remember { SalonRepository(context) }
  val sqlHelper = remember { SalonSqlHelper(context) }

  var selectedTab by remember { mutableIntStateOf(0) }

  // Custom back handling to return to the first tab
  if (selectedTab != 0) {
    BackHandler {
      selectedTab = 0
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E243D))
                .border(1.dp, GoldPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.ContentCut,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SALON LUXE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                letterSpacing = 1.sp
              )
              Text(
                text = userEmail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = {
              AuthHelper.signOut(
                context = context,
                credentialManager = credentialManager,
                onSignOutComplete = {},
                scope = scope
              )
            },
            modifier = Modifier.testTag("sign_out_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = "Sign Out",
              tint = Color(0xFFC7BCCE)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFF17121E)
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = Color(0xFF17121E),
        contentColor = GoldPrimary
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Application Form") },
          label = { Text("Application Form", style = MaterialTheme.typography.labelSmall) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1B1400),
            selectedTextColor = GoldPrimary,
            indicatorColor = GoldPrimary,
            unselectedIconColor = Color(0xFF8E8398),
            unselectedTextColor = Color(0xFF8E8398)
          ),
          modifier = Modifier.testTag("nav_item_form")
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(Icons.Default.Groups, contentDescription = "Clients & Bookings") },
          label = { Text("Clients & Bookings", style = MaterialTheme.typography.labelSmall) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1B1400),
            selectedTextColor = GoldPrimary,
            indicatorColor = GoldPrimary,
            unselectedIconColor = Color(0xFF8E8398),
            unselectedTextColor = Color(0xFF8E8398)
          ),
          modifier = Modifier.testTag("nav_item_directory")
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = { Icon(Icons.Default.DataObject, contentDescription = "SQL Studio") },
          label = { Text("SQL Studio", style = MaterialTheme.typography.labelSmall) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF1B1400),
            selectedTextColor = GoldPrimary,
            indicatorColor = GoldPrimary,
            unselectedIconColor = Color(0xFF8E8398),
            unselectedTextColor = Color(0xFF8E8398)
          ),
          modifier = Modifier.testTag("nav_item_sql")
        )
      }
    },
    contentWindowInsets = WindowInsets(0.dp)
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (selectedTab) {
        0 -> CustomerFormScreen(
          userId = userId,
          repository = repository,
          onNavigateToDirectory = { selectedTab = 1 }
        )
        1 -> CustomerDirectoryScreen(
          userId = userId,
          repository = repository,
          sqlHelper = sqlHelper,
          onNavigateToForm = { selectedTab = 0 }
        )
        2 -> SqlInspectorScreen(
          sqlHelper = sqlHelper
        )
      }
    }
  }
}
