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
import com.example.myapplication.database.AlertWithSender
import com.example.myapplication.database.User
import com.example.myapplication.repository.UserRepository
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
    val email: String,
    val isTeacher: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensagensScreen(
    username: String, 
    navController: NavController? = null,
    alertViewModel: AlertViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estados para usuários
    var teachers by remember { mutableStateOf<List<User>>(emptyList()) }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    
    val currentUser by authViewModel.currentUser.collectAsState()
    val isTeacher = currentUser?.isAdmin == true
    
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
    
    LaunchedEffect(Unit) {
        scope.launch {
            val database = AppDatabase.getDatabase(context)
            val userRepository = UserRepository(database)
            
            userRepository.getStudents().collect { studentList ->
                students = studentList
            }
        }
    }

    val grupos = listOf("SI", "TW", "Design")
    
    // Observar alertas do ViewModel
    val alerts by alertViewModel.alerts.collectAsState()
    val unreadCount by alertViewModel.unreadCount.collectAsState()

    var tabIndex by remember { mutableStateOf(0) }
    var chatSelecionado by remember { mutableStateOf<ChatUser?>(null) }
    var grupoSelecionado by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }

    val mensagensUsuarios = remember {
        mutableStateMapOf<String, MutableList<ChatMessage>>()
    }

    val mensagensGrupos = mapOf(
        "SI" to listOf(ChatMessage("Revisão amanhã", false), ChatMessage("Confirmado", true)),
        "TW" to listOf(ChatMessage("Materia nova adicionada", false), ChatMessage("ok!", true)),
        "Design" to listOf(ChatMessage("Recuperação de design dia 10/06 as 15:30", false))
    )

    // Determinar quais abas mostrar baseado no tipo de usuário
    val tabTitles = if (isTeacher) {
        listOf("Estudantes", "Grupos", "Alertas")
    } else {
        listOf("Professores", "Grupos", "Alertas")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chatSelecionado?.name ?: grupoSelecionado ?: "BejaConnect",
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
                    val msgs = mensagensUsuarios.getOrPut(chatSelecionado!!.name) { 
                        mutableStateListOf(
                            ChatMessage("Olá! Como posso ajudar?", !isTeacher)
                        )
                    }
                    ChatConversation(messages = msgs, inputText = inputText, onInputChange = { inputText = it }) {
                        if (inputText.isNotBlank()) {
                            msgs.add(ChatMessage(inputText.trim(), true))
                            inputText = ""
                        }
                    }
                }

                grupoSelecionado != null -> {
                    val msgs = mensagensGrupos[grupoSelecionado!!] ?: emptyList()
                    ChatConversation(messages = msgs)
                }

                else -> {
                    when (tabIndex) {
                        0 -> {
                            // Professores para estudantes, Estudantes para professores
                            val usersToShow = if (isTeacher) students else teachers
                            LazyColumn(Modifier.padding(12.dp)) {
                                items(usersToShow) { user ->
                                    val chatUser = ChatUser(
                                        name = user.name,
                                        email = user.email,
                                        isTeacher = user.isAdmin
                                    )
                                    ChatUserItem(user = chatUser) { chatSelecionado = chatUser }
                                }
                            }
                        }

                        1 -> {
                            LazyColumn(Modifier.padding(12.dp)) {
                                items(grupos) { grupo ->
                                    GroupItem(grupo = grupo) { grupoSelecionado = it }
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
fun GroupItem(grupo: String, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clickable { onClick(grupo) }
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
            Text(
                text = grupo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = COLOR_TEXT_PRIMARY
            )
        }
    }
}

@Composable
fun ChatConversation(
    messages: List<ChatMessage>,
    inputText: String = "",
    onInputChange: (String) -> Unit = {},
    onSend: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                Row(
                    horizontalArrangement = if (msg.fromMe) Arrangement.End else Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (msg.fromMe) COLOR_MSG_ME else COLOR_MSG_OTHER,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(text = msg.content, fontSize = 16.sp, color = COLOR_TEXT_PRIMARY)
                    }
                }
            }
        }

        // Campo para digitar mensagem
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite sua mensagem") },
                maxLines = 2
            )
            IconButton(onClick = onSend) {
                Icon(Icons.Default.Send, contentDescription = null, tint = COLOR_PRIMARY)
            }
        }
    }
}