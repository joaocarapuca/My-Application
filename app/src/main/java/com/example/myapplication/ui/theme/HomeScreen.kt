package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.database.PostWithAuthor
import com.example.myapplication.viewmodel.PostViewModel
import com.example.myapplication.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

// Data class for posts
data class Post(
    val author: String,
    val content: String
)

// Academic post item component
@Composable
fun AcademicPostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.first().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = post.author,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1B5E20)
            )
        }
    }
}

// Barra superior estilo Instagram
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "BejaConnect",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF1B5E20)
            )
        },
        navigationIcon = {
            IconButton(onClick = { /* ação da câmera */ }) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Abrir câmera",
                    tint = Color(0xFF1B5E20)
                )
            }
        },
        actions = {
            IconButton(onClick = { /* ação de mensagens */ }) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Mensagens",
                    tint = Color(0xFF1B5E20)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFFE8F5E9)
        )
    )
}

// Item do post estilo Instagram
@Composable
fun InstagramPostItem(
    post: PostWithAuthor,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    currentUserId: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
        ) {
            // Cabeçalho do post (avatar + nome)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (círculo com inicial)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorEmail.first().uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.authorEmail.split("@")[0],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(post.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Imagem do post (tamanho fixo)
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            // Botões de ação (like, comentar, partilhar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de like
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Contador de likes
                Text(
                    text = "${post.likesCount} likes",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
            }

            // Legenda do post
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1B5E20)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Tela principal
@Composable
fun HomeScreen(
    postViewModel: PostViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val posts by postViewModel.posts.collectAsState()
    val userLikes by postViewModel.userLikes.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            postViewModel.loadUserLikes(user.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        InstagramTopBar()

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📱", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum post ainda",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Os administradores podem criar posts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts.size) { index ->
                    val post = posts[index]
                    val isLiked = userLikes.contains(post.id)

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        InstagramPostItem(
                            post = post,
                            isLiked = isLiked,
                            onLikeClick = {
                                currentUser?.let { user ->
                                    postViewModel.toggleLike(user.id, post.id)
                                }
                            },
                            currentUserId = currentUser?.id ?: 0
                        )
                    }
                }
            }
        }
    }
}
