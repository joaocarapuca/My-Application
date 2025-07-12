package com.example.myapplication.routes

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.HomePage
import com.example.myapplication.Pages.AdminScreen
import com.example.myapplication.Pages.LoginPage
import com.example.myapplication.navigation.Routes
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun AppNavHosts(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginPage(
                onLoginSuccess = { isAdmin ->
                    if (isAdmin) {
                        navController.navigate("admin") {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.HOME_SCREEN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Routes.HOME_SCREEN) {
            HomePage(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("admin") {
            AdminScreen(
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo("admin") { inclusive = true }
                    }
                }
            )
        }
        
        // Rota para professores - mesma interface que estudantes mas com acesso completo
        composable("teacher") {
            HomePage(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}