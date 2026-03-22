package com.bodyquest.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository
import com.bodyquest.app.ui.home.HomeScreen
import com.bodyquest.app.ui.home.HomeViewModel
import com.bodyquest.app.ui.onboarding.OnboardingScreen
import com.bodyquest.app.ui.onboarding.OnboardingViewModel
import com.bodyquest.app.ui.quest.QuestDetailScreen
import com.bodyquest.app.ui.quest.QuestScreen
import com.bodyquest.app.ui.quest.QuestTreeScreen
import com.bodyquest.app.ui.quest.QuestViewModel
import com.bodyquest.app.ui.avatar.AvatarScreen
import com.bodyquest.app.ui.profile.ProfileScreen
import com.bodyquest.app.ui.pvp.PvpScreen
import com.bodyquest.app.ui.workout.WorkoutCompleteScreen
import com.bodyquest.app.ui.workout.WorkoutScreen
import com.bodyquest.app.ui.workout.WorkoutViewModel

// Routes where bottom nav is visible
private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Quest.route,
    Screen.Pvp.route,
    Screen.Avatar.route,
    Screen.Profile.route
)

@Composable
fun BodyQuestNavGraph(
    userRepository: UserRepository,
    questRepository: QuestRepository,
    workoutRepository: WorkoutRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if user exists to determine start destination
    val user by userRepository.getUser().collectAsState(initial = null)
    val hasUser = user != null

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
            startDestination = Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                // If user already exists, skip to home
                if (hasUser) {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                } else {
                    val onboardingViewModel: OnboardingViewModel = viewModel(
                        factory = OnboardingViewModel.Factory(userRepository)
                    )
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onComplete = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(userRepository, questRepository, workoutRepository)
                )
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
                val questViewModel: QuestViewModel = viewModel(
                    factory = QuestViewModel.Factory(questRepository)
                )
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
                QuestDetailScreen(
                    questId = questId,
                    questRepository = questRepository,
                    onStartWorkout = { id ->
                        navController.navigate(Screen.Workout.createRoute(id))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Workout.route) { backStackEntry ->
                val questId = backStackEntry.arguments?.getString("questId") ?: return@composable
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModel.Factory(questRepository, workoutRepository, userRepository)
                )
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
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModel.Factory(questRepository, workoutRepository, userRepository)
                )
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
            composable(Screen.Pvp.route) {
                PvpScreen()
            }
            composable(Screen.Avatar.route) {
                AvatarScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
