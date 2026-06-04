package re.melchior.saviomobile.ui.screen.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import re.melchior.saviomobile.data.remote.dto.CustomerSearchRowDto
import re.melchior.saviomobile.ui.designsystem.BottomNavBar
import re.melchior.saviomobile.ui.designsystem.BottomNavItem
import re.melchior.saviomobile.ui.theme.SavioPalette
import re.melchior.saviomobile.ui.theme.SavioUi
import re.melchior.saviomobile.ui.theme.savioTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    onCreateClient: () -> Unit,
    onClientClick: (CustomerSearchRowDto) -> Unit,
    showBottomNav: Boolean = false,
    bottomNavSelectedIndex: Int = 1,
    onBottomNavSelect: (Int) -> Unit = {},
    viewModel: ClientsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = SavioUi.PageBackground,
        topBar = {
            TopAppBar(
                colors = savioTopAppBarColors(),
                title = {
                    Text(
                        "Clients",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClient,
                containerColor = SavioPalette.Accent,
                contentColor = SavioPalette.OnAccent,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nouveau client")
            }
        },
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    items = mainBottomNavItems(),
                    selectedIndex = bottomNavSelectedIndex,
                    onSelect = onBottomNavSelect,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            CustomerSearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchChange,
            )
            Spacer(modifier = Modifier.height(8.dp))
            CustomerSearchResultsSection(
                query = uiState.searchQuery,
                results = uiState.results,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onSelect = onClientClick,
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

fun mainBottomNavItems(): List<BottomNavItem> =
    listOf(
        BottomNavItem(Icons.Filled.CalendarToday, "Planning"),
        BottomNavItem(Icons.Filled.Person, "Clients"),
    )
