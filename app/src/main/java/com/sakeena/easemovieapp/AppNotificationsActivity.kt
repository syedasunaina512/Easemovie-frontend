package com.sakeena.easemovieapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class AppNotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NotificationsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: "guest_user"
    
    // State for notifications
    val notificationList = remember { mutableStateListOf<NotificationData>() }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch notifications from Firestore
    LaunchedEffect(userId) {
        firestore.collection("users").document(userId).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("NotificationsScreen", "Listen failed.", e)
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    notificationList.clear()
                    for (doc in snapshot.documents) {
                        val notification = doc.toObject(NotificationData::class.java)?.copy(id = doc.id)
                        if (notification != null) {
                            notificationList.add(notification)
                        }
                    }
                }
                isLoading = false
            }
    }

    // Update unread count when list or read status changes
    val currentUnreadCount = notificationList.count { it.isNew }
    LaunchedEffect(currentUnreadCount) {
        context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
            .edit()
            .putInt("unread_count", currentUnreadCount)
            .apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notificationList.isNotEmpty()) {
                        TextButton(onClick = {
                            // Clear all in Firestore
                            val batch = firestore.batch()
                            notificationList.forEach {
                                val docRef = firestore.collection("users").document(userId)
                                    .collection("notifications").document(it.id)
                                batch.delete(docRef)
                            }
                            batch.commit()
                        }) {
                            Text("Clear All", color = Color(0xFF3A7BD5))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFF)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (notificationList.isEmpty()) {
            EmptyNotificationsView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notificationList,
                    key = { it.id }
                ) { notification ->
                    SwipeToDeleteContainer(
                        item = notification,
                        onDelete = {
                            firestore.collection("users").document(userId)
                                .collection("notifications").document(notification.id)
                                .delete()
                        }
                    ) {
                        NotificationItem(
                            data = notification,
                            onClick = {
                                Toast.makeText(context, "Opening: ${notification.title}", Toast.LENGTH_SHORT).show()
                                // Mark as read in Firestore
                                if (notification.isNew) {
                                    firestore.collection("users").document(userId)
                                        .collection("notifications").document(notification.id)
                                        .update("isNew", false)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    item: NotificationData,
    onDelete: (NotificationData) -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Color.Red.copy(alpha = 0.8f)
            } else Color.Transparent
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(color, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        content = { content() }
    )
}

@Composable
fun NotificationItem(data: NotificationData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (data.isNew) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (data.isNew) Color(0xFF3A7BD5) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D2137)
                    )
                    Text(
                        text = data.time,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
            
            if (data.isNew) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF3A7BD5), CircleShape)
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Notifications Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We'll notify you when something\ninteresting happens.",
            fontSize = 14.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

data class NotificationData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val time: String = "",
    val isNew: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
