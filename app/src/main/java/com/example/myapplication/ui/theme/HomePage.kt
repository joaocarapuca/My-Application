package com.example.myapplication.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule // Ícone para "horário"
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.routes.Routes
import com.example.myapplication.ui.screens.HorarioScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.AuthViewModel


data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun HomePage(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val bottomNavController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isTeacher = currentUser?.isAdmin == true

    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Routes.HOME_SCREEN),
        BottomNavItem("Mensagens", Icons.Default.Email, Routes.MENSAGENS),
        BottomNavItem("Horário", Icons.Default.Schedule, Routes.HORARIO),
        BottomNavItem("Perfil", Icons.Default.Person, Routes.PERFIL)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = bottomNavController,
                startDestination = Routes.HOME_SCREEN
            ) {
                composable(Routes.HOME_SCREEN) {
                    HomeScreen()
                }
                composable(Routes.MENSAGENS) {
                    com.example.myapplication.ui.theme.MensagensScreen(
                        username = currentUser?.name ?: "Utilizador",
                        navController = bottomNavController
                    )
                }
                composable(Routes.HORARIO) {
                    HorarioScreen()
                }
                composable(Routes.PERFIL) {
                    PerfilScreen(
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME_SCREEN) { inclusive = true }
                            }
                        },
                        onThemeChange = {},
                        isDarkTheme = false
                    )
                }
            }
        }
    }
}