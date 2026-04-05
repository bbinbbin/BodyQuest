package com.bodyquest.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bodyquest.app.ui.home.HomeScreen
import com.bodyquest.app.ui.home.HomeViewModel
import com.bodyquest.app.ui.intro.IntroScreen
import com.bodyquest.app.ui.login.LoginScreen
import com.bodyquest.app.ui.login.LoginViewModel
import com.bodyquest.app.ui.onboarding.OnboardingScreen
import com.bodyquest.app.ui.onboarding.OnboardingViewModel
import com.bodyquest.app.ui.splash.SplashScreen
import com.bodyquest.app.ui.splash.SplashViewModel
import com.bodyquest.app.ui.quest.QuestDetailScreen
import com.bodyquest.app.ui.quest.QuestDetailViewModel
import com.bodyquest.app.ui.quest.QuestScreen
import com.bodyquest.app.ui.quest.QuestTreeScreen
import com.bodyquest.app.ui.quest.QuestViewModel
import com.bodyquest.app.ui.avatar.AvatarScreen
import com.bodyquest.app.ui.gacha.GachaScreen
import com.bodyquest.app.ui.gacha.GachaViewModel
import com.bodyquest.app.ui.inventory.InventoryScreen
import com.bodyquest.app.ui.inventory.InventoryViewModel
import com.bodyquest.app.ui.profile.ProfileScreen
import com.bodyquest.app.ui.boss.BossScreen
import com.bodyquest.app.ui.boss.BossViewModel
import com.bodyquest.app.ui.test.TestScreen
import com.bodyquest.app.ui.workout.WorkoutCompleteScreen
import com.bodyquest.app.ui.workout.WorkoutScreen
import com.bodyquest.app.ui.workout.WorkoutViewModel


// Routes where bottom nav is visible
private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Quest.route,
    Screen.Boss.route,
    Screen.Avatar.route,
    Screen.Profile.route,
    Screen.ModelTest.route
)

@Composable
fun BodyQuestNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                val splashViewModel: SplashViewModel = hiltViewModel()
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToIntro = {
                        navController.navigate(Screen.Intro.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Intro.route) {
                IntroScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Intro.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                val loginViewModel: LoginViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = { isNewUser ->
                        if (isNewUser) {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToQuest = {
                        navController.navigate(Screen.Quest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onQuestClick = { questId ->
                        navController.navigate(Screen.QuestDetail.createRoute(questId))
                    }
                )
            }
            composable(Screen.Quest.route) {
                QuestScreen(
                    onCategorySelect = { category ->
                        navController.navigate(Screen.QuestTree.createRoute(category))
                    }
                )
            }
            composable(Screen.QuestTree.route) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: return@composable
                val questViewModel: QuestViewModel = hiltViewModel()
                QuestTreeScreen(
                    category = category,
                    viewModel = questViewModel,
                    onQuestSelect = { questId ->
                        navController.navigate(Screen.QuestDetail.createRoute(questId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.QuestDetail.route) { backStackEntry ->
                val questId = backStackEntry.arguments?.getString("questId") ?: return@composable
                val questDetailViewModel: QuestDetailViewModel = hiltViewModel()
                QuestDetailScreen(
                    questId = questId,
                    viewModel = questDetailViewModel,
                    onStartWorkout = { id ->
                        navController.navigate(Screen.Workout.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Workout.route) { backStackEntry ->
                val questId = backStackEntry.arguments?.getString("questId") ?: return@composable
                val workoutViewModel: WorkoutViewModel = hiltViewModel()
                WorkoutScreen(
                    questId = questId,
                    viewModel = workoutViewModel,
                    onComplete = { workoutId ->
                        navController.navigate(Screen.WorkoutComplete.createRoute(workoutId)) {
                            popUpTo(Screen.Quest.route)
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Screen.WorkoutComplete.route) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("workoutId")?.toLongOrNull() ?: return@composable
                val workoutViewModel: WorkoutViewModel = hiltViewModel()
                WorkoutCompleteScreen(
                    workoutId = workoutId,
                    viewModel = workoutViewModel,
                    onGoHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Screen.Boss.route) {
                val bossViewModel: BossViewModel = hiltViewModel()
                BossScreen(viewModel = bossViewModel)
            }
            composable(Screen.Avatar.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                AvatarScreen(
                    viewModel = homeViewModel,
                    onNavigateToGacha = { navController.navigate(Screen.Gacha.route) },
                    onNavigateToInventory = { navController.navigate(Screen.Inventory.route) }
                )
            }
            composable(Screen.Gacha.route) {
                val gachaViewModel: GachaViewModel = hiltViewModel()
                GachaScreen(
                    viewModel = gachaViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Inventory.route) {
                val inventoryViewModel: InventoryViewModel = hiltViewModel()
                InventoryScreen(
                    viewModel = inventoryViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.ModelTest.route) {
                TestScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
