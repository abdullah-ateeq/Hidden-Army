package com.example.hiddenarmy.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.hiddenarmy.ui.components.BattleOverlay
import com.example.hiddenarmy.ui.components.BoardView
import com.example.hiddenarmy.ui.components.GameStatusPanel
import com.example.hiddenarmy.ui.components.RestartDialog
import com.example.hiddenarmy.ui.components.SelectedUnitPanel
import com.example.hiddenarmy.ui.components.StatusHeader
import com.example.hiddenarmy.ui.viewmodel.HiddenArmyViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    viewModel: HiddenArmyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val confirmRestart by viewModel.preferences.confirmRestart.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestartDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Listen for banner events
    LaunchedEffect(Unit) {
        viewModel.bannerEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // When game over, wait briefly to let animations conclude, then navigate to Result
    LaunchedEffect(state.status) {
        if (state.status.isGameOver) {
            kotlinx.coroutines.delay(850)
            onNavigateToResult()
        }
    }

    BackHandler {
        showRestartDialog = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            StatusHeader(
                turn = state.turn,
                status = state.status,
                turnCount = state.turnCount,
                onBackClicked = { showRestartDialog = true },
                onRestartClicked = {
                    if (confirmRestart) {
                        showRestartDialog = true
                    } else {
                        viewModel.startNewGame()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLandscape) {
                // Landscape / Tablet layout: Board on left, status on right
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        BoardView(
                            state = state,
                            onCellClicked = { viewModel.onCellClicked(it) }
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center
                    ) {
                        GameStatusPanel(state = state)
                        Spacer(modifier = Modifier.height(16.dp))
                        SelectedUnitPanel(state = state)
                    }
                }
            } else {
                // Phone portrait layout: Status panel on top, board centered, selected unit below
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameStatusPanel(state = state)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        BoardView(
                            state = state,
                            onCellClicked = { viewModel.onCellClicked(it) }
                        )
                    }

                    SelectedUnitPanel(state = state)
                }
            }

            // Battle animation overlay
            BattleOverlay(
                activeBattle = state.activeBattle,
                onSkip = { viewModel.skipBattleAnimation() }
            )
        }
    }

    if (showRestartDialog) {
        RestartDialog(
            onDismiss = { showRestartDialog = false },
            onRestart = {
                viewModel.startNewGame()
            },
            onExit = {
                onNavigateBack()
            }
        )
    }
}
