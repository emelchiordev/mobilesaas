package re.savio.mobile.ui.screen.client

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
import re.savio.mobile.data.remote.dto.CustomerSearchRowDto
import re.savio.mobile.ui.designsystem.BottomNavBar
import re.savio.mobile.ui.designsystem.BottomNavBarWithFab
import re.savio.mobile.ui.designsystem.BottomNavItem
import re.savio.mobile.ui.refonte.SavioEdgeToEdgeScaffoldInsets
import re.savio.mobile.ui.refonte.SavioHeaderStyle
import re.savio.mobile.ui.refonte.SavioNavyHeader
import re.savio.mobile.ui.refonte.SavioRefonteFab
import re.savio.mobile.ui.refonte.SavioSearchField
import re.savio.mobile.ui.theme.SavioRefonte
import re.savio.mobile.ui.theme.SavioPalette
import re.savio.mobile.ui.theme.SavioUi
import re.savio.mobile.ui.theme.savioTopAppBarColors
import re.savio.mobile.ui.theme.useSavioRefonteUi

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

    val refonte = useSavioRefonteUi()
    Scaffold(
        containerColor =
            if (refonte) MaterialTheme.colorScheme.background else SavioUi.PageBackground,
        contentWindowInsets = if (refonte) SavioEdgeToEdgeScaffoldInsets else androidx.compose.material3.ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (refonte) {
                SavioNavyHeader(title = "Clients", style = SavioHeaderStyle.Primary)
            } else {
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
            }
        },
        floatingActionButton = {
            if (refonte && !showBottomNav) {
                SavioRefonteFab(onClick = onCreateClient, contentDescription = "Nouveau client")
            } else if (!refonte) {
                FloatingActionButton(
                    onClick = onCreateClient,
                    containerColor = SavioPalette.Accent,
                    contentColor = SavioPalette.OnAccent,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nouveau client")
                }
            }
        },
        bottomBar = {
            if (showBottomNav && refonte) {
                BottomNavBarWithFab(
                    items = mainBottomNavItems(),
                    selectedIndex = bottomNavSelectedIndex,
                    onSelect = onBottomNavSelect,
                    onFabClick = onCreateClient,
                    fabContentDescription = "Nouveau client",
                )
            } else if (showBottomNav) {
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
            if (refonte) {
                SavioSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchChange,
                )
                if (uiState.searchQuery.isNotBlank()) {
                    Text(
                        text =
                            when (uiState.results.size) {
                                0 -> "Aucun résultat"
                                1 -> "1 résultat"
                                else -> "${uiState.results.size} résultats"
                            },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SavioRefonte.Muted,
                        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 2.dp),
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                CustomerSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchChange,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
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
