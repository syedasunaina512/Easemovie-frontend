package com.sakeena.easemovieapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AppearanceActivity : ComponentActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
            val initialDarkMode = prefs.getBoolean("darkMode", false)
            
            AppTheme(darkTheme = initialDarkMode) {
                AppearanceScreen(
                    onBack = { finish() },
                    onThemeChange = { recreate() },
                    userId = userId,
                    firestore = firestore
                )
            }
        }
    }
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF3A7BD5),
            surface = Color(0xFF121212),
            onSurface = Color.White,
            background = Color(0xFF0D1117)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF3A7BD5),
            surface = Color.White,
            onSurface = Color.Black,
            background = Color(0xFFF9FAFF)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    onThemeChange: () -> Unit,
    userId: String,
    firestore: FirebaseFirestore
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) }
    
    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("darkMode", false)) }
    var selectedTheme by remember { mutableStateOf(prefs.getString("theme", "purple") ?: "purple") }

    LaunchedEffect(Unit) {
        firestore.collection("users").document(userId)
            .collection("settings").document("appearance")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firestoreDarkMode = document.getBoolean("darkMode") ?: isDarkMode
                    val firestoreTheme = document.getString("theme") ?: selectedTheme

                    if (firestoreDarkMode != isDarkMode) {
                        isDarkMode = firestoreDarkMode
                        prefs.edit().putBoolean("darkMode", firestoreDarkMode).apply()
                        applyDarkMode(firestoreDarkMode)
                        onThemeChange()
                    }

                    if (firestoreTheme != selectedTheme) {
                        selectedTheme = firestoreTheme
                        prefs.edit().putString("theme", firestoreTheme).apply()
                        onThemeChange()
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Display",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Mode", 
                            fontSize = 16.sp, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Reduce eye strain at night", 
                            fontSize = 12.sp, 
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { checked ->
                            isDarkMode = checked
                            prefs.edit().putBoolean("darkMode", checked).apply()
                            updateFirestore(firestore, userId, "darkMode", checked)
                            applyDarkMode(checked)
                            onThemeChange()
                        }
                    )
                }
            }

            Text(
                text = "Themes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Choose Theme",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ThemeOption(
                        label = "Default Theme (Purple)",
                        isSelected = selectedTheme == "purple",
                        enabled = false,
                        onClick = { }
                    )
                    ThemeOption(
                        label = "Blue Theme",
                        isSelected = selectedTheme == "blue",
                        enabled = false,
                        onClick = { }
                    )
                    ThemeOption(
                        label = "Green Theme",
                        isSelected = selectedTheme == "green",
                        enabled = false,
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOption(label: String, isSelected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected, 
            onClick = if (enabled) onClick else null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                disabledSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label, 
            fontSize = 15.sp, 
            color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
        
        if (!enabled) {
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "LOCKED",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun applyDarkMode(enabled: Boolean) {
    AppCompatDelegate.setDefaultNightMode(
        if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    )
}

private fun updateFirestore(firestore: FirebaseFirestore, userId: String, key: String, value: Any) {
    val data = mapOf(key to value)
    firestore.collection("users").document(userId)
        .collection("settings").document("appearance")
        .set(data, SetOptions.merge())
        .addOnFailureListener { e ->
            Log.e("AppearanceActivity", "Error updating appearance in Firestore", e)
        }
}
