package re.melchior.saviomobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.ui.screen.auth.AuthViewModel
import re.melchior.saviomobile.ui.screen.auth.LoginScreen
import re.melchior.saviomobile.ui.screen.auth.SelectSocieteScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SelectSociete : Screen("select_societe")
    object Tournee : Screen("tournee")
}

@Composable
fun AppNavigation(tokenDataStore: TokenDataStore) {
    val navController = rememberNavController()
    val isLoggedIn by tokenDataStore.isLoggedIn.collectAsStateWithLifecycle(false)
    val startDestination = if (isLoggedIn) Screen.Tournee.route else Screen.Login.route

    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Tournee.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onChooseSociete = {
                    navController.navigate(Screen.SelectSociete.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.SelectSociete.route) {
            SelectSocieteScreen(
                societes = authUiState.societesToChoose,
                onSocieteSelected = {
                    navController.navigate(Screen.Tournee.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Tournee.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tournée — à venir",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}