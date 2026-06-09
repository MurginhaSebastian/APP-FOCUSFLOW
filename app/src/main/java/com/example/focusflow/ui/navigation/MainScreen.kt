package com.example.focusflow.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.animation.togetherWith
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.ui.focus.FocusScreen
import com.example.focusflow.ui.home.HomeScreen
import com.example.focusflow.ui.tasks.TareaListScreen
import com.example.focusflow.ui.enlace.EnlaceScreen
import com.example.focusflow.viewmodel.AuthViewModel
import com.example.focusflow.viewmodel.FocusPhase
import com.example.focusflow.viewmodel.FocusViewModel

private data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem("Inicio", Icons.Default.Home),
    BottomNavItem("Focus", Icons.Default.Timer),
    BottomNavItem("Tareas", Icons.Default.Checklist),
    BottomNavItem("Smart", Icons.Default.SmartToy),
    BottomNavItem("Enlace", Icons.Default.Link),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onPickLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    focusViewModel: FocusViewModel = hiltViewModel(),
) {
    val uiState by authViewModel.uiState.collectAsState()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showFocusBlockedDialog by remember { mutableStateOf(false) }

    val focusState by focusViewModel.uiState.collectAsState()
    val isFocusActive = focusState.phase == FocusPhase.FOCUSING ||
            focusState.phase == FocusPhase.BREAK ||
            focusState.phase == FocusPhase.LONG_BREAK

    LaunchedEffect(uiState.isLoggedIn) {
        if (!uiState.isLoggedIn) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FocusFlow") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                showMenu = false
                                authViewModel.logout()
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            if (index != 1 && isFocusActive) {
                                showFocusBlockedDialog = true
                            } else {
                                selectedIndex = index
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        if (showFocusBlockedDialog) {
            AlertDialog(
                onDismissRequest = { showFocusBlockedDialog = false },
                title = { Text("Enfoque activo") },
                text = { Text("Termina tu enfoque primero") },
                confirmButton = {
                    TextButton(onClick = {
                        showFocusBlockedDialog = false
                        selectedIndex = 1
                    }) {
                        Text("Ir al enfoque")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFocusBlockedDialog = false }) {
                        Text("Cerrar")
                    }
                },
            )
        }

        androidx.compose.animation.AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = {
                androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) togetherWith
                        androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(200)
                        )
            },
            label = "screen_transition",
        ) { index ->
            when (index) {
                0 -> HomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
                1 -> FocusScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
                2 -> TareaListScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onPickLocation = onPickLocation,
                )
                3 -> SmartScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
                4 -> EnlaceScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onNavigateToQR = { selectedIndex = 3 },
                )
            }
        }
    }
}
