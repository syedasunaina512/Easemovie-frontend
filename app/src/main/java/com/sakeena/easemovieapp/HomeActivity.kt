package com.sakeena.easemovieapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.content.Context
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HomeScreen()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var unreadNotificationCount by remember { mutableIntStateOf(0) }

    val pullToRefreshState = rememberPullToRefreshState()

    // Simulation of real-time data
    val animations = remember { 
        mutableStateListOf(
            AnimationItem("Cosmic Journey EP 1", "Completed", R.drawable.img_onboarding, progress = 1f),
            AnimationItem("Comedy Comedy", "In Progress", R.drawable.onboarding, isNew = true, progress = 0.6f),
            AnimationItem("Fantasy Adventure", "In Progress", R.drawable.bg_splash, progress = 0.2f),
            AnimationItem("Nature Doc", "Completed", R.drawable.img_onboarding, progress = 1f)
        )
    }

    val templates = remember {
        mutableStateListOf(
            TemplateItem("Sci-fi Character", "Simple", R.drawable.img_onboarding, "4.8"),
            TemplateItem("Talking Animal", "Advanced", R.drawable.onboarding, "5.0"),
            TemplateItem("Stylized Landscape", "Simple", R.drawable.bg_splash, "4.5"),
            TemplateItem("Cyberpunk City", "Intermediate", R.drawable.onboarding, "4.9")
        )
    }

    // Refresh simulation
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(1500) // Simulating network delay
            isRefreshing = false
        }
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            isRefreshing = true
            delay(1500)
            pullToRefreshState.endRefresh()
        }
    }

    // Update profile image when screen is resumed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
                profileImageUri = prefs.getString("image", "") ?: ""
                
                val notifPrefs = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
                unreadNotificationCount = notifPrefs.getInt("unread_count", 4) // Initial dummy count
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredAnimations = remember(searchQuery, animations.size) {
        if (searchQuery.isEmpty()) animations
        else animations.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val filteredTemplates = remember(searchQuery, templates.size) {
        if (searchQuery.isEmpty()) templates
        else templates.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        bottomBar = { HomeBottomNavigation() },
        containerColor = Color(0xFFF9FAFF)
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
                HomeHeader(
                    isSearching = isSearching,
                    searchQuery = searchQuery,
                    profileImageUri = profileImageUri,
                    unreadCount = unreadNotificationCount,
                    onSearchToggle = { isSearching = !isSearching },
                    onQueryChange = { searchQuery = it }
                )
                
                if (!isSearching || searchQuery.isEmpty()) {
                    StartStorySection()
                }

                MyAnimationsSection(filteredAnimations)
                TrendingTemplatesSection(filteredTemplates)
                
                if (!isSearching) {
                    TutorialsSection()
                }
                
                Spacer(modifier = Modifier.height(30.dp))
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                contentColor = Color(0xFF00BFA5)
            )
        }
    }
}

@Composable
fun HomeHeader(
    isSearching: Boolean,
    searchQuery: String,
    profileImageUri: String,
    unreadCount: Int,
    onSearchToggle: () -> Unit,
    onQueryChange: (String) -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Profile Options", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showProfileDialog = false
                            if (profileImageUri.isNotEmpty()) {
                                val intent = Intent(context, FullScreenImageActivity::class.java)
                                intent.putExtra("imageUri", profileImageUri)
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "No profile photo set", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("View Profile Photo", color = Color(0xFF0D2137))
                        }
                    }
                    TextButton(
                        onClick = {
                            showProfileDialog = false
                            context.startActivity(Intent(context, ProfileActivity::class.java))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Open Profile Settings", color = Color(0xFF0D2137))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearching) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    onSearchToggle()
                    onQueryChange("")
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                }
                TextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search animations, templates...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.brain_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EaseMovie",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D2137),
                    letterSpacing = 0.5.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSearchToggle, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                Box(modifier = Modifier.size(36.dp)) {
                    IconButton(onClick = { 
                        context.startActivity(Intent(context, AppNotificationsActivity::class.java))
                    }, modifier = Modifier.fillMaxSize()) {
                        Icon(painterResource(id = R.drawable.ic_notifications_24), contentDescription = "Notifications", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF2196F3), CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                
                // Functional Profile Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                        .clickable { showProfileDialog = true }
                ) {
                    if (profileImageUri.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_person_24),
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StartStorySection() {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Start Your Next Story",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D2137)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gradient Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF80DEEA), Color(0xFFB39DDB))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable {
                    context.startActivity(Intent(context, GeneratorActivity::class.java))
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NEW PROJECT:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Story to Animation",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { 
                    context.startActivity(Intent(context, LibraryActivity::class.java))
                },
                modifier = Modifier.weight(1f).height(50.dp).shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_edit_24), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Input Script", color = Color(0xFF0D2137), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = { 
                    context.startActivity(Intent(context, LibraryActivity::class.java))
                },
                modifier = Modifier.weight(1f).height(50.dp).shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Upload Idea", color = Color(0xFF0D2137), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MyAnimationsSection(list: List<AnimationItem>) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "My Animations",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D2137),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (list.isEmpty()) {
            Text(
                text = "No animations found",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(list) { anim ->
                    AnimationCard(anim)
                }
            }
        }
    }
}

@Composable
fun AnimationCard(anim: AnimationItem) {
    val context = LocalContext.current
    Card(
        onClick = {
            Toast.makeText(context, "Opening ${anim.title}", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.width(165.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(id = anim.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    contentScale = ContentScale.Crop
                )
                if (anim.isNew) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color(0xFF00BFA5), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(text = "New", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = anim.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color(0xFF0D2137))
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = anim.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (anim.status == "Completed") Color(0xFF4CAF50) else Color(0xFF2196F3)
                    )
                    if (anim.status != "Completed") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${(anim.progress * 100).toInt()}%", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                if (anim.status != "Completed") {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { anim.progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = Color(0xFF2196F3),
                        trackColor = Color(0xFFE3F2FD)
                    )
                } else {
                    Text(text = "2 mins ago", fontSize = 10.sp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun TrendingTemplatesSection(list: List<TemplateItem>) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "Trending Templates",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D2137),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (list.isEmpty()) {
            Text(
                text = "No templates found",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(list) { template ->
                    TemplateCard(template)
                }
            }
        }
    }
}

@Composable
fun TemplateCard(template: TemplateItem) {
    val context = LocalContext.current
    Card(
        onClick = {
            Toast.makeText(context, "Template: ${template.title}", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box {
                Image(
                    painter = painterResource(id = template.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(6.dp).align(Alignment.BottomEnd),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = template.rating, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = template.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color(0xFF0D2137))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Complexity: ${template.complexity}", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { 
                    val intent = Intent(context, GeneratorActivity::class.java).apply {
                        putExtra("template_title", template.title)
                        putExtra("template_prompt", "A cinematic scene of ${template.title} in a high-detail environment...")
                        putExtra("template_style", "Anime")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1))
            ) {
                Text(text = "Use Template", color = Color(0xFF00BFA5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TutorialsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        Text(
            text = "Tutorials & Community",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D2137)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TutorialItem("Masterclass:\nAI Keyframing", Modifier.weight(1f))
            TutorialItem("Community Choice:\nBest Short Film", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TutorialItem("Getting Started\nGuide", Modifier.weight(1f))
            TutorialItem("AI Voice Lab", Modifier.weight(1f))
        }
    }
}

@Composable
fun TutorialItem(title: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        onClick = {
            Toast.makeText(context, "Tutorial: ${title.replace("\n", " ")}", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.CenterStart) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D2137), lineHeight = 18.sp)
        }
    }
}

@Composable
fun HomeBottomNavigation(selectedTab: String = "Home") {
    val context = LocalContext.current
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(85.dp)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            selected = selectedTab == "Home",
            onClick = { 
                if (selectedTab != "Home") {
                    context.startActivity(Intent(context, HomeActivity::class.java))
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2196F3),
                selectedTextColor = Color(0xFF2196F3),
                indicatorColor = Color(0xFFE3F2FD),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.ic_folder_24), contentDescription = "Library") },
            label = { Text("Library", fontSize = 11.sp) },
            selected = selectedTab == "Library",
            onClick = { 
                if (selectedTab != "Library") {
                    context.startActivity(Intent(context, LibraryActivity::class.java))
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2196F3),
                selectedTextColor = Color(0xFF2196F3),
                indicatorColor = Color(0xFFE3F2FD),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        
        // Custom Create Button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (selectedTab == "Create") Color(0xFF2196F3) else Color(0xFF00BFA5),
                    shadowElevation = 4.dp
                ) {
                    IconButton(onClick = { 
                        if (selectedTab != "Create") {
                            context.startActivity(Intent(context, GeneratorActivity::class.java))
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Create", fontSize = 11.sp, color = if (selectedTab == "Create") Color(0xFF2196F3) else Color.Gray)
            }
        }

        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, contentDescription = "AI Tools") },
            label = { Text("AI Tools", fontSize = 11.sp) },
            selected = selectedTab == "AI Tools",
            onClick = { 
                Toast.makeText(context, "AI Tools coming soon", Toast.LENGTH_SHORT).show()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2196F3),
                selectedTextColor = Color(0xFF2196F3),
                indicatorColor = Color(0xFFE3F2FD),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp) },
            selected = selectedTab == "Profile",
            onClick = { 
                context.startActivity(Intent(context, ProfileActivity::class.java))
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2196F3),
                selectedTextColor = Color(0xFF2196F3),
                indicatorColor = Color(0xFFE3F2FD),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

data class AnimationItem(val title: String, val status: String, val imageRes: Int, val isNew: Boolean = false, val progress: Float = 1f)
data class TemplateItem(val title: String, val complexity: String, val imageRes: Int, val rating: String = "4.0")
