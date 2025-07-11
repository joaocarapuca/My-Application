package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.GroupViewModel
import com.example.myapplication.database.AlertWithSender
import com.example.myapplication.database.GroupMessageWithSender
import com.example.myapplication.database.User
import com.example.myapplication.database.Group
import com.example.myapplication.repository.UserRepository
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.database.AppDatabase
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 🎨 Paleta tons verdes acadêmicos
private val COLOR_PRIMARY = Color(0xFF2E7D32)         // Verde escuro (top bar, botão)
private val COLOR_PRIMARY_LIGHT = Color(0xFF60AD5E)   // Verde claro para destaques
private val COLOR_BACKGROUND = Color(0xFFF1F8E9)      // Fundo suave, verde muito claro
private val COLOR_MSG_ME = Color(0xFFC8E6C9)          // Mensagens minhas (verde claro suave)
private val COLOR_MSG_OTHER = Color.White             // Mensagens outros (branco)
private val COLOR_TEXT_PRIMARY = Color(0xFF1B5E20)    // Texto escuro verde
private val COLOR_TEXT_SECONDARY = Color(0xFF4CAF50)  // Texto médio verde

data class ChatMessage(
    val content: String,
    val fromMe: Boolean,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

data class ChatUser(
    val name: String,
    val id: Int,
    val email: String,
    val isTeacher: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensagensScreen(
    username: String, 
    navController: NavController? = null,
    alertViewModel: AlertViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estados para usuários
    var teachers by remember { mutableStateOf<List<User>>(emptyList()) }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    
    // Repositório de mensagens
    var messageRepository by remember { mutableStateOf<MessageRepository?>(null) }
    
    val currentUser by authViewModel.currentUser.collectAsState()
    val isTeacher = currentUser?.isAdmin == true
    
    // Estados para chat
    var chatSelecionado by remember { mutableStateOf<ChatUser?>(null) }
    var grupoSelecionado by remember { mutableStateOf<Group?>(null) }
    var inputText by remember { mutableStateOf("") }
    var tabIndex by remember { mutableStateOf(0) }
    
    // Mensagens privadas
    var privateMessages by remember { mutableStateOf<List<com.example.myapplication.database.Message>>(emptyList()) }
    
    // Observar grupos e mensagens do ViewModel
    val userGroups by groupViewModel.userGroups.collectAsState()
    val groupMessages by groupViewModel.groupMessages.collectAsState()
    val alerts by alertViewModel.alerts.collectAsState()
    
    // Inicializar repositório de mensagens
    LaunchedEffect(Unit) {
        val database = AppDatabase.getDatabase(context)
        messageRepository = MessageRepository(database)
    }
    
    // Carregar usuários
    LaunchedEffect(Unit) {
        scope.launch {
            val database = AppDatabase.getDatabase(context)
            val userRepository = UserRepository(database)
            
            userRepository.getTeachers().collect { teacherList ->
                teachers = teacherList
            }
        }
    }
    
    // Carregar grupos do usuário
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            groupViewModel.loadUserGroups(user.id)
        }
    }
    
    // Carregar mensagens privadas quando um chat é selecionado
    LaunchedEffect(chatSelecionado, currentUser) {
        if (chatSelecionado != null && currentUser != null && messageRepository != null) {
            messageRepository!!.getMessagesBetweenUsers(currentUser.id, chatSelecionado!!.id).collect { messages ->
                privateMessages = messages
            }
        }
    }
    
    // Carregar mensagens do grupo quando um grupo é selecionado
    LaunchedEffect(grupoSelecionado) {
        grupoSelecionado?.let { group ->
            groupViewModel.loadGroupMessages(group.id)
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            val database = AppDatabase.getDatabase(context)
            val userRepository = UserRepository(database)
            
            userRepository.getStudents().collect { studentList ->
                students = studentList
            }
        }
    }

    val unreadCount by alertViewModel.unreadCount.collectAsState()

    // Determinar quais abas mostrar baseado no tipo de usuário
    val tabTitles = if (isTeacher) {
        listOf("Utilizadores", "Grupos", "Alertas")
    } else {
        listOf("Utilizadores", "Grupos", "Alertas")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chatSelecionado?.name ?: grupoSelecionado?.name ?: "BejaConnect",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    if (chatSelecionado != null || grupoSelecionado != null) {
                        IconButton(onClick = {
                            chatSelecionado = null
                            grupoSelecionado = null
                            groupViewModel.clearCurrentGroup()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White)
                    }
                },
                modifier = Modifier.background(COLOR_PRIMARY)
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            if (chatSelecionado == null && grupoSelecionado == null) {
                TabRow(selectedTabIndex = tabIndex, modifier = Modifier.background(Color.White)) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (tabIndex == index) COLOR_PRIMARY else COLOR_TEXT_SECONDARY,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        )
                    }
                }
            }

            when {
                chatSelecionado != null -> {
                    // Chat privado
                    PrivateChatConversation(
                        messages = privateMessages,
                        currentUserId = currentUser?.id ?: 0,
                        inputText = inputText,
                        onInputChange = { inputText = it }
                    ) {
                        if (inputText.isNotBlank()) {
                            scope.launch {
                                messageRepository?.sendMessage(
                                    currentUser?.id ?: 0,
                                    chatSelecionado!!.id,
                                    inputText.trim()
                                )
                            }
                            inputText = ""
                        }
                    }
                }

                grupoSelecionado != null -> {
                    // Chat do grupo
                    GroupChatConversation(
                        messages = groupMessages,
                        currentUserId = currentUser?.id ?: 0,
                        inputText = inputText,
                        onInputChange = { inputText = it }
                    ) {
                        if (inputText.isNotBlank()) {
                            groupViewModel.sendGroupMessage(
                                grupoSelecionado!!.id,
                                currentUser?.id ?: 0,
                                inputText.trim()
                            )
                            inputText = ""
                        }
                    }
                }

                else -> {
                    when (tabIndex) {
                        0 -> {
                            // Todos os utilizadores (exceto o próprio)
                            val allUsers = teachers + students
                            val usersToShow = allUsers.filter { it.id != currentUser?.id }
                            
                            LazyColumn(Modifier.padding(12.dp)) {
                                items(usersToShow) { user ->
                                    val chatUser = ChatUser(
                                        id = user.id,
                                        name = user.name,
                                        email = user.email,
                                        isTeacher = user.isAdmin
                                    )
                                    ChatUserItem(user = chatUser) { chatSelecionado = chatUser }
                                }
                            }
                        }

                        1 -> {
                            // Grupos do utilizador
                            LazyColumn(Modifier.padding(12.dp)) {
                                items(userGroups) { group ->
                                    GroupItem(group = group) { 
                                        grupoSelecionado = it
                                        groupViewModel.setCurrentGroup(it)
                                    }
                                }
                            }
                        }

                        2 -> {
                            AlertsTab(alerts, alertViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsTab(alerts: List<AlertWithSender>, alertViewModel: AlertViewModel) {
    if (alerts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔔",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhum alerta",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Os professores podem enviar alertas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alerts.size) { index ->
                val alert = alerts[index]
                AlertItem(
                    alert = alert,
                    onMarkAsRead = { alertViewModel.markAsRead(alert.id) }
                )
            }
        }
    }
}

@Composable
fun AlertItem(alert: AlertWithSender, onMarkAsRead: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (!alert.isRead) {
                    onMarkAsRead()
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isRead) Color.White else Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!alert.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = COLOR_TEXT_PRIMARY
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "De: ${alert.senderEmail.split("@")[0]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = COLOR_TEXT_SECONDARY
                    )
                    
                    alert.targetGroup?.let { group ->
                        Text(
                            text = "Grupo: $group",
                            style = MaterialTheme.typography.bodySmall,
                            color = COLOR_TEXT_SECONDARY
                        )
                    }
                }
                
                Text(
                    text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(alert.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = COLOR_TEXT_PRIMARY
            )
            
            if (alert.targetGroup != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            COLOR_PRIMARY_LIGHT.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📚 ${alert.targetGroup}",
                        style = MaterialTheme.typography.bodySmall,
                        color = COLOR_PRIMARY,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ChatUserItem(user: ChatUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone padrão para avatar
        Icon(
            imageVector = if (user.isTeacher) Icons.Default.School else Icons.Default.Person,
            contentDescription = "Avatar",
            tint = COLOR_PRIMARY,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(COLOR_PRIMARY_LIGHT.copy(alpha = 0.3f))
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = user.name, 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = COLOR_TEXT_PRIMARY
            )
            Text(
                text = if (user.isTeacher) "👨‍🏫 Professor" else "👨‍🎓 Estudante", 
                color = COLOR_TEXT_SECONDARY, 
                fontSize = 14.sp
            )
            Text(
                text = user.email, 
                color = Color.Gray, 
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun GroupItem(group: Group, onClick: (Group) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clickable { onClick(group) }
            .background(Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📚",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column {
                Text(
                    text = group.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = COLOR_TEXT_PRIMARY
                )
                if (group.description.isNotBlank()) {
                    Text(
                        text = group.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
