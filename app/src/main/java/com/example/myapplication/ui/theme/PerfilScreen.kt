package com.example.myapplication.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.Typography
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun MyApp() {
    var isDarkTheme by remember { mutableStateOf(false) }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        PerfilScreen(
            onLogout = { /* Lógica de logout */ },
            onThemeChange = { isDarkTheme = it },
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
fun PerfilScreen(
    nome: String = "", // Parâmetro mantido para compatibilidade
    email: String = "", // Parâmetro mantido para compatibilidade
    onLogout: () -> Unit,
    onThemeChange: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    authViewModel: AuthViewModel = viewModel()
) {
    var showPasswordFields by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val currentUser by authViewModel.currentUser.collectAsState()

    // Usar sempre dados do usuário logado
    val displayName = currentUser?.name ?: "Utilizador"
    val displayEmail = currentUser?.email ?: "email@ipbeja.pt"
    val userType = if (currentUser?.isAdmin == true) "Professor/Administrador" else "Estudante"
    val curso = "Tecnologia Web e Dispositivos Móveis"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            Text(
                text = displayEmail,
                fontSize = 16.sp,
                color = colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userType,
                fontSize = 16.sp,
                color = if (currentUser?.isAdmin == true) colorScheme.primary else colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = if (currentUser?.isAdmin == true) FontWeight.Medium else FontWeight.Normal
            )
            Text(
                text = curso,
                fontSize = 14.sp,
                color = colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Card com informações do utilizador
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📋 Informações do Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row {
                        Text("Nome: ", fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                        Text(displayName, color = colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row {
                        Text("Email: ", fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                        Text(displayEmail, color = colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row {
                        Text("Tipo: ", fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                        Text(
                            text = userType,
                            color = if (currentUser?.isAdmin == true) colorScheme.primary else colorScheme.onBackground,
                            fontWeight = if (currentUser?.isAdmin == true) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    
                    if (currentUser?.isAdmin == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🔑 Acesso administrativo ativo",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Modo Escuro", color = colorScheme.onBackground, modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Notificações", color = colorScheme.onBackground, modifier = Modifier.weight(1f))
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showPasswordFields = !showPasswordFields },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.primary)
            ) {
                Text(if (showPasswordFields) "Cancelar" else "Alterar Senha")
            }

            if (showPasswordFields) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Senha Atual") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nova Senha") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        // Lógica para alterar senha
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Guardar", color = colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAboutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text("Sobre o Aplicativo", color = colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
            ) {
                Text("Logout", color = colorScheme.onError)
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "Sobre o Aplicativo",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            },
            text = {
                Column {
                    Text(
                        text = "📱 BejaConnect",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Versão: 1.0.0",
                        fontSize = 14.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Este aplicativo foi desenvolvido como projeto acadêmico para a disciplina de Tecnologia Web e Dispositivos Móveis.",
                        fontSize = 14.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🎓 Desenvolvido por:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onBackground
                    )
                    Text(
                        text = "João Carapuça e Laura Remechido",
                        fontSize = 14.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "26603@stu.ipbeja.pt | 26603@stu.ipbeja.pt",
                        fontSize = 12.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🏫 Instituto Politécnico de Beja",
                        fontSize = 14.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "© 2024 - Todos os direitos reservados",
                        fontSize = 12.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Fechar")
                }
            },
            containerColor = colorScheme.background,
            titleContentColor = colorScheme.onBackground,
            textContentColor = colorScheme.onBackground
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val greenLightColors = lightColorScheme(
        primary = Color(0xFF2E7D32),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFA5D6A7),
        onPrimaryContainer = Color(0xFF1B5E20),
        secondary = Color(0xFF4CAF50),
        onSecondary = Color.White,
        background = Color(0xFFF1F8E9),
        onBackground = Color(0xFF1B5E20),
        surface = Color.White,
        onSurface = Color(0xFF2E7D32),
        error = Color(0xFFD32F2F),
        onError = Color.White
    )

    val greenDarkColors = darkColorScheme(
        primary = Color(0xFFA5D6A7),
        onPrimary = Color(0xFF1B5E20),
        primaryContainer = Color(0xFF2E7D32),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFF81C784),
        onSecondary = Color(0xFF1B5E20),
        background = Color(0xFF1B5E20),
        onBackground = Color(0xFFC8E6C9),
        surface = Color(0xFF2E7D32),
        onSurface = Color(0xFFC8E6C9),
        error = Color(0xFFEF9A9A),
        onError = Color(0xFFB71C1C)
    )

    MaterialTheme(
        colorScheme = if (darkTheme) greenDarkColors else greenLightColors,
        typography = Typography,
        content = content
    )
}