package com.example.myapplication.Pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.database.User
import com.example.myapplication.database.Schedule
import com.example.myapplication.viewmodel.AdminViewModel
import com.example.myapplication.viewmodel.PostViewModel
import com.example.myapplication.viewmodel.ScheduleViewModel
import com.example.myapplication.viewmodel.AlertViewModel
import com.example.myapplication.viewmodel.GroupViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun AdminScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    adminViewModel: AdminViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    alertViewModel: AlertViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Utilizadores", "Gerir Utilizadores", "Criar Post", "Gerir Horários", "Gerir Grupos", "Enviar Alerta")
    
    // Observar estados do ViewModel
    val users by adminViewModel.users.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val message by adminViewModel.message.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Painel do Admin",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF4CAF50)
            )
            Button(
                onClick = {
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sair", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF4CAF50),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mostrar mensagem se existir
        message?.let { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.contains("sucesso")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = if (msg.contains("sucesso")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }

        when (selectedTab) {
            0 -> CreateUserTab(adminViewModel, isLoading)
            1 -> ManageUsersTab(users, adminViewModel, isLoading)
            2 -> CreatePostTab(postViewModel)
            3 -> ManageScheduleTab(scheduleViewModel)
            4 -> ManageGroupsTab(groupViewModel, adminViewModel)
            5 -> SendAlertTab(alertViewModel)
        }
    }
}

@Composable
fun CreateUserTab(adminViewModel: AdminViewModel, isLoading: Boolean) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        // Checkbox para definir se é administrador
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isAdmin,
                onCheckedChange = { isAdmin = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("É Professor/Administrador")
        }

        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                    adminViewModel.createUser(name, email, password, isAdmin)
                    name = ""
                    email = ""
                    password = ""
                    isAdmin = false
                } else {
                    // Mensagem será gerida pelo ViewModel
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Criar Utilizador", color = Color.White)
            }
        }
    }
}

@Composable
fun ManageUsersTab(users: List<User>, adminViewModel: AdminViewModel, isLoading: Boolean) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = if (user.isAdmin) "Professor/Administrador" else "Estudante",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (user.isAdmin) Color(0xFF2E7D32) else Color(0xFF666666)
                        )
                    }
                    
                    Row {
                        // Botão para alterar senha
                        IconButton(
                            onClick = {
                                selectedUser = user
                                showPasswordDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Alterar Senha",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        
                        // Botão para eliminar (não permitir eliminar admin)
                        if (!user.isAdmin) {
                            IconButton(
                                onClick = {
                                    adminViewModel.deleteUser(user)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    
    // Dialog para alterar senha
    if (showPasswordDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { 
                showPasswordDialog = false
                newPassword = ""
            },
            title = { Text("Alterar Senha") },
            text = {
                Column {
                    Text("Alterar senha para: ${selectedUser!!.name}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nova Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPassword.isNotBlank()) {
                            adminViewModel.changeUserPassword(selectedUser!!.id, newPassword)
                            showPasswordDialog = false
                            newPassword = ""
                        }
                    }
                ) {
                    Text("Alterar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPasswordDialog = false
                        newPassword = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CreatePostTab(postViewModel: PostViewModel) {
    var imageUrl by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    
    val isLoading by postViewModel.isLoading.collectAsState()
    val message by postViewModel.message.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Criar Post Estilo Instagram",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50)
        )
        
        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("URL da Imagem") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("https://exemplo.com/imagem.jpg") }
        )

        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("Legenda do Post") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            placeholder = { Text("Escreva uma legenda interessante...") }
        )

        Button(
            onClick = {
                if (imageUrl.isNotBlank() && caption.isNotBlank()) {
                    postViewModel.createPost(1, imageUrl, caption) // ID 1 = admin
                    imageUrl = ""
                    caption = ""
                } else {
                    // Mensagem será gerida pelo ViewModel
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Publicar Post", color = Color.White)
            }
        }

        message?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.contains("sucesso")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = if (msg.contains("sucesso")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun ManageScheduleTab(scheduleViewModel: ScheduleViewModel) {
    var timeSlot by remember { mutableStateOf("") }
    var monday by remember { mutableStateOf("") }
    var tuesday by remember { mutableStateOf("") }
    var wednesday by remember { mutableStateOf("") }
    var thursday by remember { mutableStateOf("") }
    var friday by remember { mutableStateOf("") }
    
    val schedules by scheduleViewModel.schedules.collectAsState()
    val isLoading by scheduleViewModel.isLoading.collectAsState()
    val message by scheduleViewModel.message.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gerir Horários",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50)
        )
        
        // Formulário para criar horário
        OutlinedTextField(
            value = timeSlot,
            onValueChange = { timeSlot = it },
            label = { Text("Horário (ex: 08:00-10:00)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = monday,
                onValueChange = { monday = it },
                label = { Text("Segunda") },
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = tuesday,
                onValueChange = { tuesday = it },
                label = { Text("Terça") },
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                singleLine = true
            )
        }
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = wednesday,
                onValueChange = { wednesday = it },
                label = { Text("Quarta") },
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = thursday,
                onValueChange = { thursday = it },
                label = { Text("Quinta") },
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                singleLine = true
            )
        }
        
        OutlinedTextField(
            value = friday,
            onValueChange = { friday = it },
            label = { Text("Sexta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                if (timeSlot.isNotBlank()) {
                    scheduleViewModel.createSchedule(timeSlot, monday, tuesday, wednesday, thursday, friday)
                    timeSlot = ""
                    monday = ""
                    tuesday = ""
                    wednesday = ""
                    thursday = ""
                    friday = ""
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Adicionar Horário", color = Color.White)
            }
        }

        message?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.contains("sucesso")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = if (msg.contains("sucesso")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }
        
        // Lista de horários existentes
        Text(
            text = "Horários Existentes:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(top = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(schedules.size) { index ->
                val schedule = schedules[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = schedule.timeSlot,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Seg: ${schedule.monday} | Ter: ${schedule.tuesday}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Qua: ${schedule.wednesday} | Qui: ${schedule.thursday} | Sex: ${schedule.friday}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                scheduleViewModel.deleteSchedule(schedule)
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageGroupsTab(groupViewModel: GroupViewModel, adminViewModel: AdminViewModel) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val subTabTitles = listOf("Criar Grupo", "Gerir Membros")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gerir Grupos",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50)
        )
        
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color(0xFFE8F5E9),
            contentColor = Color(0xFF4CAF50)
        ) {
            subTabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedSubTab) {
            0 -> CreateGroupSubTab(groupViewModel)
            1 -> ManageGroupMembersSubTab(groupViewModel, adminViewModel)
        }
    }
}

@Composable
fun CreateGroupSubTab(groupViewModel: GroupViewModel) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    
    val groups by groupViewModel.groups.collectAsState()
    val isLoading by groupViewModel.isLoading.collectAsState()
    val message by groupViewModel.message.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Nome do Grupo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Ex: SI, TW, Design") }
        )
        
        OutlinedTextField(
            value = groupDescription,
            onValueChange = { groupDescription = it },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            placeholder = { Text("Descrição do grupo...") }
        )
        
        Button(
            onClick = {
                if (groupName.isNotBlank()) {
                    groupViewModel.createGroup(groupName, groupDescription, 1) // ID 1 = admin
                    groupName = ""
                    groupDescription = ""
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Criar Grupo", color = Color.White)
            }
        }
        
        message?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.contains("sucesso")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = if (msg.contains("sucesso")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }
        
        // Lista de grupos existentes
        Text(
            text = "Grupos Existentes:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(top = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups.size) { index ->
                val group = groups[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (group.description.isNotBlank()) {
                                Text(
                                    text = group.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = {
                                groupViewModel.deleteGroup(group)
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageGroupMembersSubTab(groupViewModel: GroupViewModel, adminViewModel: AdminViewModel) {
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    
    val groups by groupViewModel.groups.collectAsState()
    val groupMembers by groupViewModel.groupMembers.collectAsState()
    val availableUsers by groupViewModel.availableUsers.collectAsState()
    val message by groupViewModel.message.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Seletor de grupo
        Text(
            text = "Selecionar Grupo:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4CAF50)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(groups.size) { index ->
                val group = groups[index]
                Card(
                    modifier = Modifier.clickable {
                        selectedGroup = group
                        groupViewModel.setCurrentGroup(group)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedGroup?.id == group.id) Color(0xFF4CAF50) else Color(0xFFE8F5E9)
                    )
                ) {
                    Text(
                        text = group.name,
                        modifier = Modifier.padding(12.dp),
                        color = if (selectedGroup?.id == group.id) Color.White else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        selectedGroup?.let { group ->
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Membros do Grupo: ${group.name}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )
            
            // Lista de membros atuais
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(groupMembers.size) { index ->
                    val member = groupMembers[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = member.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    groupViewModel.removeMemberFromGroup(group.id, member.id)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remover",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Adicionar Utilizadores:",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )
            
            // Lista de utilizadores disponíveis para adicionar
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(availableUsers.filter { user -> 
                    groupMembers.none { member -> member.id == user.id }
                }.size) { index ->
                    val user = availableUsers.filter { user -> 
                        groupMembers.none { member -> member.id == user.id }
                    }[index]
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if (user.isAdmin) "Professor" else "Estudante",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (user.isAdmin) Color(0xFF2E7D32) else Color(0xFF666666)
                                )
                            }
                            
                            Button(
                                onClick = {
                                    groupViewModel.addMemberToGroup(group.id, user.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Adicionar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        
        message?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.contains("sucesso")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = if (msg.contains("sucesso")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun SendAlertTab(alertViewModel: AlertViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("Todos") }
    
    val isLoading by alertViewModel.isLoading.collectAsState()
    val alertMessage by alertViewModel.message.collectAsState()
    
    val groups = listOf("Todos", "SI", "TW", "Design", "Matemática")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Enviar Alerta para Estudantes",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50)
        )
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título do Alerta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Ex: Aula Cancelada") }
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Mensagem") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            placeholder = { Text("Escreva a mensagem do alerta...") }
        )
        
        // Seletor de grupo
        Text(
            text = "Grupo Alvo:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4CAF50)
        )
        
        LazyColumn(
            modifier = Modifier.height(120.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(groups.size) { index ->
                val group = groups[index]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGroup = group }
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(group)
                }
            }
        }

        Button(
            onClick = {
                if (title.isNotBlank() && message.isNotBlank()) {
                    val targetGroup = if (selectedGroup == "Todos") null else selectedGroup
                    alertViewModel.createAlert(1, title, message, targetGroup) // ID 1 = admin
                    title = ""
                    message = ""
                    selectedGroup = "Todos"
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Enviar Alerta", color = Color.White)
            }
        }

        alertMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.contains("sucesso")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    color = if (msg.contains("sucesso")) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }
    }
}