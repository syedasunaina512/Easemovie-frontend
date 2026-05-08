package com.sakeena.easemovieapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color(0xFFF7F9FC),
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    )
                )
            },
            bottomBar = { HomeBottomNavigation(selectedTab = "Profile") },
            containerColor = Color(0xFFF7F9FC)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ProfileScreenContent(
                    name = "Sakeena",
                    email = "sakeena@example.com",
                    profileImageUri = "",
                    onImageClick = {},
                    onCameraClick = {},
                    onMenuItemClick = {},
                    onLogoutClick = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // State for profile data
    var name by remember { mutableStateOf("User Name") }
    var email by remember { mutableStateOf("email@gmail.com") }
    var profileImageUri by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Refresh State
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val loadData = {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val sharedPref = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val profilePrefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)

        if (currentUser != null) {
            name = currentUser.displayName ?: sharedPref.getString("name", "User Name") ?: "User Name"
            email = currentUser.email ?: sharedPref.getString("email", "email@gmail.com") ?: "email@gmail.com"
            profileImageUri = currentUser.photoUrl?.toString() ?: profilePrefs.getString("image", "") ?: ""
        } else {
            name = sharedPref.getString("name", "User Name") ?: "User Name"
            email = sharedPref.getString("email", "email@gmail.com") ?: "email@gmail.com"
            profileImageUri = profilePrefs.getString("image", "") ?: ""
        }
    }

    // Handle Refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            loadData()
            delay(1500) // Visual feedback
            isRefreshing = false
        }
    }

    // Initial Load
    LaunchedEffect(Unit) {
        loadData()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout from EaseMovie?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // Firebase Sign out
                        FirebaseAuth.getInstance().signOut()
                        
                        // Google Sign out (if applicable)
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            // Navigate to Login
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as Activity).finish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Launchers
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let {
                profileImageUri = it.toString()
                saveImageInActivity(context, it.toString())
                uploadImageToFirebase(context, it)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.extras?.getParcelable("data", Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.extras?.get("data") as? Bitmap
            }
            bitmap?.let {
                val uri = saveBitmapToStorageInActivity(context, it)
                startCropInActivity(context as Activity, uri, cropLauncher)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { startCropInActivity(context as Activity, it, cropLauncher) }
        }
    }

    // Load data initially
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val profilePrefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
        
        name = sharedPref.getString("name", "User Name") ?: "User Name"
        email = sharedPref.getString("email", "email@gmail.com") ?: "email@gmail.com"
        profileImageUri = profilePrefs.getString("image", "") ?: ""
    }

    // Permission Launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openCameraInActivity(cameraLauncher)
        else Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openGalleryInActivity(galleryLauncher)
        else Toast.makeText(context, "Gallery Permission Denied", Toast.LENGTH_SHORT).show()
    }

    if (showImageDialog) {
        ImageOptionsDialog(
            onDismiss = { showImageDialog = false },
            onViewPhoto = {
                if (profileImageUri.isNotEmpty()) {
                    val intent = Intent(context, FullScreenImageActivity::class.java)
                    intent.putExtra("imageUri", profileImageUri)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No profile image found", Toast.LENGTH_SHORT).show()
                }
            },
            onTakePhoto = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCameraInActivity(cameraLauncher)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onUploadPhoto = {
                val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    openGalleryInActivity(galleryLauncher)
                } else {
                    galleryPermissionLauncher.launch(permission)
                }
            },
            onRemovePhoto = {
                profileImageUri = ""
                saveImageInActivity(context, "")
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color(0xFFF7F9FC),
                    scrolledContainerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = { HomeBottomNavigation(selectedTab = "Profile") },
        containerColor = Color(0xFFF7F9FC)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileScreenContent(
                    name = name,
                    email = email,
                    profileImageUri = profileImageUri,
                    onImageClick = {
                        if (profileImageUri.isNotEmpty()) {
                            val intent = Intent(context, FullScreenImageActivity::class.java)
                            intent.putExtra("imageUri", profileImageUri)
                            context.startActivity(intent)
                        }
                    },
                    onCameraClick = { showImageDialog = true },
                    onMenuItemClick = { title ->
                        when (title) {
                            "Edit Profile" -> context.startActivity(Intent(context, EditProfileActivity::class.java))
                            "Change Password" -> context.startActivity(Intent(context, ChangePasswordActivity::class.java))
                            "Settings" -> context.startActivity(Intent(context, SettingsActivity::class.java))
                            "Security" -> context.startActivity(Intent(context, SecurityActivity::class.java))
                            "Language" -> context.startActivity(Intent(context, LanguageActivity::class.java))
                        }
                    },
                    onLogoutClick = {
                        showLogoutDialog = true
                    }
                )
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                contentColor = Color(0xFF2196F3)
            )

            LaunchedEffect(pullToRefreshState.isRefreshing) {
                if (pullToRefreshState.isRefreshing) {
                    isRefreshing = true
                }
            }

            LaunchedEffect(isRefreshing) {
                if (!isRefreshing) {
                    pullToRefreshState.endRefresh()
                }
            }
        }
    }
}

@Composable
fun ProfileScreenContent(
    name: String,
    email: String,
    profileImageUri: String,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit,
    onMenuItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image Section
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .background(Color.LightGray)
                    .clickable { onImageClick() }
            ) {
                if (profileImageUri.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = Color.Gray
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onCameraClick() },
                shape = CircleShape,
                color = Color(0xFF2196F3),
                shadowElevation = 4.dp
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = email, fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(30.dp))

        // Menu Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    subtitle = "Update your personal info"
                ) { onMenuItemClick("Edit Profile") }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    subtitle = "Update your password"
                ) { onMenuItemClick("Change Password") }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    subtitle = "App preferences"
                ) { onMenuItemClick("Settings") }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                ProfileMenuItem(
                    icon = Icons.Default.Security,
                    title = "Security",
                    subtitle = "Protect your account"
                ) { onMenuItemClick("Security") }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)

                ProfileMenuItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "Change app language"
                ) { onMenuItemClick("Language") }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFF0F0F0)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = Color.DarkGray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}

@Composable
fun ImageOptionsDialog(
    onDismiss: () -> Unit,
    onViewPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onUploadPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Profile Photo", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                DialogOption(Icons.Default.Visibility, "View Photo", onViewPhoto, onDismiss)
                DialogOption(Icons.Default.CameraAlt, "Take Photo", onTakePhoto, onDismiss)
                DialogOption(Icons.Default.Photo, "Upload from Gallery", onUploadPhoto, onDismiss)
                DialogOption(Icons.Default.Delete, "Remove Photo", onRemovePhoto, onDismiss, color = Color.Red)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun DialogOption(icon: ImageVector, text: String, onClick: () -> Unit, onDismiss: () -> Unit, color: Color = Color.Black) {
    TextButton(
        onClick = {
            onClick()
            onDismiss()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (color == Color.Red) color else Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = color)
        }
    }
}

private fun openCameraInActivity(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    launcher.launch(intent)
}

private fun openGalleryInActivity(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    launcher.launch(intent)
}

private fun startCropInActivity(activity: Activity, uri: Uri, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    val destinationUri = Uri.fromFile(File(activity.cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
    val options = UCrop.Options()
    options.setCircleDimmedLayer(true)
    options.setHideBottomControls(false)

    val uCropIntent = UCrop.of(uri, destinationUri)
        .withAspectRatio(1f, 1f)
        .withOptions(options)
        .getIntent(activity)
    
    launcher.launch(uCropIntent)
}

private fun saveImageInActivity(context: Context, uri: String) {
    val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
    prefs.edit().putString("image", uri).apply()
}

private fun uploadImageToFirebase(context: Context, fileUri: Uri) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${user.uid}.jpg")

    Toast.makeText(context, "Uploading photo...", Toast.LENGTH_SHORT).show()

    storageRef.putFile(fileUri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadUri)
                    .build()

                user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Profile photo updated in Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

private fun saveBitmapToStorageInActivity(context: Context, bitmap: Bitmap): Uri {
    val filename = "profile_${System.currentTimeMillis()}.jpg"
    var imageUri: Uri? = null
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProfileImages")
            }
            imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            imageUri?.let { context.contentResolver.openOutputStream(it)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            }}
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ProfileImages")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            imageUri = Uri.fromFile(file)
        }
    } catch (e: Exception) { e.printStackTrace() }
    return imageUri!!
}
