package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.navigation.Routes
import com.example.presentation.ui.components.PuterBridgeWebView
import com.example.presentation.ui.screens.AuthScreen
import com.example.presentation.ui.screens.DialogueScreen
import com.example.presentation.ui.screens.GameScreen
import com.example.presentation.ui.screens.HiddenAdminScreen
import com.example.presentation.ui.screens.LifeSummaryScreen
import com.example.presentation.ui.screens.MainMenuScreen
import com.example.presentation.ui.screens.NewLifeSetupScreen
import com.example.presentation.ui.screens.RedeemCouponScreen
import com.example.presentation.ui.screens.SettingsScreen
import com.example.presentation.ui.screens.SplashScreen
import com.example.presentation.ui.screens.StoreScreen
import com.example.presentation.viewmodel.MainViewModel
import com.example.presentation.viewmodel.MainViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AiLifeSimApplication
        val container = app.container

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    lifeRepository = container.lifeRepository,
                    billingRepository = container.billingRepository,
                    aiRepository = container.aiRepository,
                    userPreferencesRepository = container.userPreferencesRepository,
                    puterBridge = container.puterBridge
                )
            )

            val state by viewModel.uiState.collectAsState()
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(state.toastMessage) {
                state.toastMessage?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            LaunchedEffect(state.legacyAssessment) {
                if (state.legacyAssessment != null) {
                    navController.navigate(Routes.LIFE_SUMMARY)
                }
            }

            MyApplicationTheme(darkTheme = state.userPreferences?.isDarkMode ?: true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Puter.js headless background bridge
                    PuterBridgeWebView(puterBridge = container.puterBridge)

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Routes.SPLASH,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Routes.SPLASH) {
                                SplashScreen(
                                    onLoaded = {
                                        navController.navigate(Routes.MAIN_MENU) {
                                            popUpTo(Routes.SPLASH) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Routes.AUTH) {
                                AuthScreen(
                                    puterUser = state.puterUser,
                                    isAuthenticating = state.isPuterAuthenticating,
                                    onPuterSignIn = {
                                        viewModel.authenticateWithPuter {
                                            navController.navigate(Routes.MAIN_MENU) {
                                                popUpTo(Routes.AUTH) { inclusive = true }
                                            }
                                        }
                                    },
                                    onAuthenticated = {
                                        navController.navigate(Routes.MAIN_MENU) {
                                            popUpTo(Routes.AUTH) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Routes.MAIN_MENU) {
                                MainMenuScreen(
                                    state = state,
                                    onStartNewLife = { navController.navigate(Routes.NEW_LIFE) },
                                    onContinueGame = { navController.navigate(Routes.GAME) },
                                    onOpenStore = { navController.navigate(Routes.STORE) },
                                    onOpenRedeem = { navController.navigate(Routes.REDEEM_COUPON) },
                                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                    onOpenAdmin = { navController.navigate(Routes.HIDDEN_ADMIN) }
                                )
                            }

                            composable(Routes.NEW_LIFE) {
                                NewLifeSetupScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onStartLife = { name, gender, country, traits, difficulty, sandbox, challenge ->
                                        viewModel.startNewLife(name, gender, country, traits, difficulty, sandbox, challenge)
                                        navController.navigate(Routes.GAME) {
                                            popUpTo(Routes.MAIN_MENU)
                                        }
                                    },
                                    onOpenStore = { navController.navigate(Routes.STORE) }
                                )
                            }

                            composable(Routes.GAME) {
                                GameScreen(
                                    state = state,
                                    onBackToMenu = { navController.navigate(Routes.MAIN_MENU) },
                                    onAdvanceDay = { viewModel.advanceDay() },
                                    onPerformAction = { action -> viewModel.performAction(action) },
                                    onPlayCasino = { bet -> viewModel.playCasinoBlackjack(bet) },
                                    onTalkToNpc = { npc ->
                                        viewModel.startDialogueWithNpc(npc)
                                        navController.navigate(Routes.DIALOGUE)
                                    },
                                    onExportPdf = { ctx -> viewModel.exportLifeToPdf(ctx) }
                                )
                            }

                            composable(Routes.DIALOGUE) {
                                DialogueScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onSendMessage = { msg -> viewModel.sendNpcDialogueMessage(msg) }
                                )
                            }

                            composable(Routes.STORE) {
                                StoreScreen(
                                    state = state,
                                    passes = container.billingRepository.availablePasses,
                                    onBack = { navController.popBackStack() },
                                    onPurchasePass = { passId -> viewModel.purchasePass(passId) },
                                    onOpenRedeemCoupon = { navController.navigate(Routes.REDEEM_COUPON) }
                                )
                            }

                            composable(Routes.REDEEM_COUPON) {
                                RedeemCouponScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onRedeem = { code -> viewModel.redeemCoupon(code) }
                                )
                            }

                            composable(Routes.SETTINGS) {
                                SettingsScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onSelectAiModel = { model -> viewModel.updateSelectedAiModel(model) },
                                    onToggleTts = { tts -> viewModel.toggleTts(tts) },
                                    onToggleDarkMode = { dark -> viewModel.toggleDarkMode(dark) },
                                    onSetSpeed = { speed -> viewModel.setSimulationSpeed(speed) },
                                    onExportPdf = { ctx -> viewModel.exportLifeToPdf(ctx) },
                                    onAuthenticatePuter = { viewModel.authenticateWithPuter() },
                                    onRefreshModels = { viewModel.refreshAvailableAiModels() },
                                    onOpenAdmin = { navController.navigate(Routes.HIDDEN_ADMIN) }
                                )
                            }

                            composable(Routes.LIFE_SUMMARY) {
                                LifeSummaryScreen(
                                    state = state,
                                    onStartReincarnation = {
                                        navController.navigate(Routes.NEW_LIFE) {
                                            popUpTo(Routes.MAIN_MENU)
                                        }
                                    },
                                    onExportPdf = { ctx -> viewModel.exportLifeToPdf(ctx) }
                                )
                            }

                            composable(Routes.HIDDEN_ADMIN) {
                                HiddenAdminScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onCreateCoupon = { coupon -> viewModel.createAdminCoupon(coupon) },
                                    onApproveSubscription = { reqId -> viewModel.approveSubscription(reqId) },
                                    onRejectSubscription = { reqId -> viewModel.rejectSubscription(reqId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
