package com.sakeena.easemovieapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import androidx.core.content.FileProvider
import java.io.File
import coil.compose.AsyncImage
import com.sakeena.easemovieapp.api.ApiClient
import com.sakeena.easemovieapp.api.SegmentRequest
import com.sakeena.easemovieapp.api.VideoRequest
import com.sakeena.easemovieapp.api.NarrationRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GeneratorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GeneratorScreen()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GeneratorScreenPreview() {
    MaterialTheme {
        GeneratorScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Header States (Functional like Home)
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf("") }
    var unreadCount by remember { mutableIntStateOf(0) }

    // Flow States
    var storyText by remember { 
        mutableStateOf(
            (context as? Activity)?.intent?.getStringExtra("template_prompt") ?: ""
        ) 
    }
    var currentStep by remember { mutableIntStateOf(0) } 

    val templateStyle = (context as? Activity)?.intent?.getStringExtra("template_style")
    var selectedStyle by remember { 
        mutableStateOf(
            generatorStyles.find { it.name == templateStyle } ?: generatorStyles.firstOrNull()
        ) 
    }

    val scenes = remember { mutableStateListOf<SceneModel>() }
    
    var narrationUrl by remember { mutableStateOf("") }
    var isGeneratingNarration by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }

    // Generation States
    var showGenDialog by remember { mutableStateOf(false) }
    var genType by remember { mutableStateOf("") }
    var genProgress by remember { mutableFloatStateOf(0f) }
    var showPreviewScreen by remember { mutableStateOf(false) }
    var generatedVideoUrl by remember { mutableStateOf("") }

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var sceneToEdit by remember { mutableStateOf<SceneModel?>(null) }

    if (showPreviewScreen) {
        VideoPreviewScreen(
            videoUrl = generatedVideoUrl,
            onBack = { showPreviewScreen = false },
            onEdit = { 
                showPreviewScreen = false
                Toast.makeText(context, "Returning to editor...", Toast.LENGTH_SHORT).show() 
            },
            onDownload = { url -> downloadVideo(context, url) },
            onShare = { url -> shareVideo(context, url) }
        )
    } else {
        if (showEditDialog && sceneToEdit != null) {
            var editTitle by remember { mutableStateOf(sceneToEdit!!.text) }
            var editDesc by remember { mutableStateOf(sceneToEdit!!.description ?: "") }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Scene Details", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Scene Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDesc,
                            onValueChange = { editDesc = it },
                            label = { Text("Scene Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val index = scenes.indexOfFirst { it.index == sceneToEdit!!.index }
                        if (index != -1) {
                            scenes[index] = scenes[index].copy(text = editTitle, description = editDesc)
                        }
                        showEditDialog = false
                    }) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showGenDialog) {
            AlertDialog(
                onDismissRequest = { },
                confirmButton = { },
                title = { Text("AI Generation in Progress", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Generating $genType using AI...", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearProgressIndicator(
                            progress = { genProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = if (genType == "Images") Color(0xFF4DB6AC) else Color(0xFFBA68C8)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("${(genProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }

        Scaffold(
            bottomBar = { HomeBottomNavigation(selectedTab = "Create") },
            containerColor = Color(0xFFF7F9FC)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                HomeHeader(
                    isSearching = isSearching,
                    searchQuery = searchQuery,
                    profileImageUri = profileImageUri,
                    unreadCount = unreadCount,
                    onSearchToggle = { isSearching = !isSearching },
                    onQueryChange = { searchQuery = it }
                )

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    
                    // --- SECTION 1: CREATE YOUR STORY ---
                    Text(
                        text = "Create Your Story",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Column {
                                Text("Enter Your Story Concept", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                                TextField(
                                    value = storyText,
                                    onValueChange = { storyText = it },
                                    placeholder = {
                                        Text(
                                            "An astronaut discovers a lost alien planet to find contextations and prames uses in an ounverrator the expects of its frost bisory and the land.",
                                            fontSize = 13.sp,
                                            color = Color.Gray.copy(alpha = 0.6f)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.Gray)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (storyText.isNotEmpty()) {
                                    isProcessing = true
                                    scope.launch {
                                        try {
                                            val response = ApiClient.instance.splitScenes(SegmentRequest(storyText))
                                            scenes.clear()
                                            response.scenes.forEach { dto ->
                                                scenes.add(
                                                    SceneModel(
                                                        index = dto.index,
                                                        text = "Scene ${dto.index}",
                                                        description = dto.text,
                                                        mood = dto.mood,
                                                        camera = dto.camera
                                                    )
                                                )
                                            }
                                            currentStep = 1
                                            Toast.makeText(context, "Story Segmented into ${scenes.size} Scenes!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            // Fallback local segmentation if API fails
                                            val sentences = storyText.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
                                            scenes.clear()
                                            sentences.forEachIndexed { idx, s ->
                                                scenes.add(SceneModel(idx + 1, "Scene ${idx + 1}", description = s.trim()))
                                            }
                                            currentStep = 1
                                        } finally {
                                            isProcessing = false
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter or speak a story first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isProcessing && currentStep == 0) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF3F51B5))
                            } else {
                                Image(painter = painterResource(id = R.drawable.brain_logo), contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Summarize\n& Segment", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 12.sp)
                            }
                        }

                        Button(
                            onClick = { 
                                if (storyText.isNotEmpty()) {
                                    isGeneratingNarration = true
                                    scope.launch {
                                        try {
                                            val response = ApiClient.instance.generateNarration(NarrationRequest(storyText))
                                            narrationUrl = response.audio_url
                                            Toast.makeText(context, "Voice Narration Added!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Narration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isGeneratingNarration = false
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter story text first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E5F5)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isGeneratingNarration) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF9C27B0))
                            } else {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Narration", color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // --- SECTION 2: SCENE SEGMENTATION ---
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Scene Segmentation", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Segmented Scenes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3F51B5))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    scenes.forEachIndexed { index, scene ->
                        ProfessionalSceneCard(
                            scene = scene,
                            onEdit = {
                                sceneToEdit = scene
                                showEditDialog = true
                            },
                            onRegenerate = {
                                isProcessing = true
                                scope.launch {
                                    delay(1500)
                                    val updatedScene = scene.copy(description = "Regenerated: ${scene.description} (Enhanced Details)")
                                    scenes[index] = updatedScene
                                    isProcessing = false
                                    Toast.makeText(context, "Scene ${scene.index} Regenerated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onReorder = {
                                if (index < scenes.size - 1) {
                                    // Swap with next
                                    val nextScene = scenes[index + 1]
                                    scenes[index] = nextScene
                                    scenes[index + 1] = scene
                                    Toast.makeText(context, "Scene moved down", Toast.LENGTH_SHORT).show()
                                } else if (index > 0) {
                                    // Swap with previous if it's the last one
                                    val prevScene = scenes[index - 1]
                                    scenes[index] = prevScene
                                    scenes[index - 1] = scene
                                    Toast.makeText(context, "Scene moved up", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // --- SECTION 3: STYLE SELECTION ---
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Style & Assets Selection", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Global Animation Style", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(generatorStyles) { style ->
                            ProfessionalStyleCard(style, isSelected = selectedStyle?.name == style.name) {
                                selectedStyle = style
                                if (currentStep < 2) currentStep = 2
                            }
                        }
                    }

                    // --- SECTION 4: EXECUTION & PREVIEW ---
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Execution & Preview", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExecutionCard(
                            title = "Scene-by-Scene Images",
                            icon = Icons.Default.PhotoLibrary,
                            gradient = listOf(Color(0xFF80CBC4), Color(0xFF4DB6AC)),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (scenes.isEmpty()) {
                                Toast.makeText(context, "Please segment scenes first", Toast.LENGTH_SHORT).show()
                            } else if (selectedStyle == null) {
                                Toast.makeText(context, "Please select a style first", Toast.LENGTH_SHORT).show()
                            } else {
                                genType = "Images"
                                showGenDialog = true
                                scope.launch {
                                    try {
                                        val total = scenes.size
                                        scenes.forEachIndexed { idx, scene ->
                                            genProgress = (idx + 1) / total.toFloat()
                                            val response = ApiClient.instance.generateImage(
                                                text = scene.description ?: scene.text,
                                                style = selectedStyle?.name ?: "Anime",
                                                emotion = scene.mood
                                            )
                                            scenes[idx] = scene.copy(imagePath = response.image_path)
                                        }
                                        showGenDialog = false
                                        currentStep = 3
                                        Toast.makeText(context, "AI Images Generated Successfully!", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        showGenDialog = false
                                        Toast.makeText(context, "Generation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        ExecutionCard(
                            title = "Full Video Generation",
                            icon = Icons.Default.MovieFilter,
                            gradient = listOf(Color(0xFFCE93D8), Color(0xFFBA68C8)),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (currentStep < 3) {
                                Toast.makeText(context, "Please generate scene images first", Toast.LENGTH_SHORT).show()
                            } else {
                                genType = "Video"
                                showGenDialog = true
                                scope.launch {
                                    try {
                                        genProgress = 0.2f
                                        val sceneTexts = scenes.map { it.description ?: it.text }
                                        val response = ApiClient.instance.generateVideo(
                                            VideoRequest(
                                                scenes = sceneTexts,
                                                style = selectedStyle?.name ?: "Anime",
                                                narration_url = narrationUrl.ifEmpty { null }
                                            )
                                        )
                                        genProgress = 1f
                                        delay(500)
                                        showGenDialog = false
                                        generatedVideoUrl = response.video_url
                                        currentStep = 4
                                        showPreviewScreen = true
                                        
                                        // Save to Firestore
                                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                                        val projectRef = FirebaseFirestore.getInstance().collection("projects").document()
                                        val project = Project(
                                            id = projectRef.id,
                                            userId = userId,
                                            title = if (scenes.isNotEmpty()) scenes[0].text else "My Animation",
                                            videoUrl = generatedVideoUrl,
                                            thumbnailUrl = if (scenes.isNotEmpty()) scenes[0].imagePath ?: "" else "",
                                            style = selectedStyle?.name ?: "Anime",
                                            scenes = scenes.toList()
                                        )
                                        projectRef.set(project)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Project saved to Library!", Toast.LENGTH_SHORT).show()
                                            }

                                        Toast.makeText(context, "Full Video Rendered Successfully!", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        showGenDialog = false
                                        Toast.makeText(context, "Video rendering failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                    
                    // Small Player Bar
                    Spacer(modifier = Modifier.height(16.dp))
                    PlayerBar()

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun VideoPreviewScreen(
    videoUrl: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDownload: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(if (videoUrl.isNotEmpty()) videoUrl else "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            setMediaItem(mediaItem)
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Video Preview",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }

        // Real Video Player using ExoPlayer
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    StyledPlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Action Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PreviewActionButton(Icons.Default.Edit, "Edit", onEdit)
                PreviewActionButton(Icons.Default.FileDownload, "Download") { onDownload(videoUrl) }
                PreviewActionButton(Icons.Default.Share, "Share") { onShare(videoUrl) }
            }
        }
    }
}

@Composable
fun PreviewActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProfessionalSceneCard(
    scene: SceneModel,
    onEdit: () -> Unit,
    onRegenerate: () -> Unit,
    onReorder: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))) {
                AsyncImage(
                    model = if (scene.imagePath?.startsWith("http") == true) scene.imagePath 
                            else when(scene.imagePath) {
                                "onboarding" -> R.drawable.onboarding
                                "bg_splash" -> R.drawable.bg_splash
                                "img_onboarding" -> R.drawable.img_onboarding
                                else -> R.drawable.img_onboarding
                            },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.img_onboarding)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Scene ${scene.index}: ${scene.text}", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Scene") },
                                onClick = { 
                                    showMenu = false
                                    onEdit() 
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Regenerate") },
                                onClick = { 
                                    showMenu = false
                                    onRegenerate() 
                                },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Delete Scene", color = Color.Red) },
                                onClick = { 
                                    showMenu = false
                                    // Add delete logic if needed
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }
                Text(text = scene.description ?: "Astronaut spaceflight to Planet", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionLink(Icons.Default.Edit, "Edit Text", onEdit)
                    ActionLink(Icons.Default.Refresh, "Regenerate", onRegenerate)
                    ActionLink(Icons.Default.SwapVert, "Re-order", onReorder)
                }
            }
        }
    }
}

@Composable
fun ActionLink(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 9.sp, color = Color.Gray)
    }
}

@Composable
fun ProfessionalStyleCard(item: GeneratorSelectionItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(85.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFE0F2F1) else Color.Transparent)
            .clickable { onClick() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(70.dp),
            shape = RoundedCornerShape(10.dp),
            border = if (isSelected) BorderStroke(2.dp, Color(0xFF00BFA5)) else null
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.name,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            color = if (isSelected) Color(0xFF00695C) else Color(0xFF424242)
        )
    }
}

@Composable
fun ExecutionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, gradient: List<Color>, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun PlayerBar() {
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(18.dp)).background(Color.White).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "0:00", fontSize = 9.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).height(3.dp).background(Color.LightGray.copy(alpha = 0.5f), CircleShape)) {
            Box(modifier = Modifier.fillMaxWidth(0.3f).fillMaxHeight().background(Color(0xFF4DB6AC), CircleShape))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "0:00", fontSize = 9.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
    }
}

// Reuse or adapt existing data models
data class GeneratorSelectionItem(val name: String, val imageRes: Int, val isSelected: Boolean = false)

val generatorStyles = listOf(
    GeneratorSelectionItem("Sci-fi\nAnime", R.drawable.style_anime),
    GeneratorSelectionItem("3D\nCartoon", R.drawable.style_cartoon),
    GeneratorSelectionItem("Stylized\nHand-drawn", R.drawable.sketch),
    GeneratorSelectionItem("Realistic\nCinematic", R.drawable.style_realistic)
)
