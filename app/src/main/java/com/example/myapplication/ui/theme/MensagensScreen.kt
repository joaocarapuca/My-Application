package com.example.myapplication.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    
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
            
            // Carregar todos os usuários
            userRepository.getAllUsers().collect { userList ->
                allUsers = userList
                teachers = userList.filter { it.isAdmin }
                students = userList.filter { !it.isAdmin }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = COLOR_PRIMARY,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
                            // Mostrar utilizadores baseado no tipo de usuário logado
                            val usersToShow = if (isTeacher) {
                                // Professores veem todos (estudantes e outros professores)
                                allUsers.filter { it.id != currentUser?.id }
                            } else {
                                // Estudantes veem todos (professores e outros estudantes)
                                allUsers.filter { it.id != currentUser?.id }
                            }
                            
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
                                
                                if (usersToShow.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Nenhum utilizador disponível",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                        }
                                    }
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
                                
                                if (userGroups.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "📚",
                                                    fontSize = 48.sp
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "Nenhum grupo",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = "Aguarde ser adicionado a um grupo",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
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
fun PrivateChatConversation(
    messages: List<com.example.myapplication.database.Message>,
    currentUserId: Int,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.size) { index ->
                val message = messages[index]
                MessageBubble(
                    content = message.content,
                    isFromMe = message.senderId == currentUserId,
                    timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escreva uma mensagem...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSendMessage,
                colors = ButtonDefaults.buttonColors(containerColor = COLOR_PRIMARY)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

@Composable
fun GroupChatConversation(
    messages: List<GroupMessageWithSender>,
    currentUserId: Int,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.size) { index ->
                val message = messages[index]
                GroupMessageBubble(
                    content = message.content,
                    senderName = message.senderName,
                    isFromMe = message.senderId == currentUserId,
                    timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escreva uma mensagem...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSendMessage,
                colors = ButtonDefaults.buttonColors(containerColor = COLOR_PRIMARY)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

@Composable
fun MessageBubble(
    content: String,
    isFromMe: Boolean,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromMe) COLOR_MSG_ME else COLOR_MSG_OTHER
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = content,
                    color = COLOR_TEXT_PRIMARY
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun GroupMessageBubble(
    content: String,
    senderName: String,
    isFromMe: Boolean,
    timestamp: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromMe) COLOR_MSG_ME else COLOR_MSG_OTHER
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isFromMe) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = COLOR_PRIMARY
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = content,
                    color = COLOR_TEXT_PRIMARY
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
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