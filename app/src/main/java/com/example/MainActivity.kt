package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BodyControlViewModel
import com.example.ui.BodyControlViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize modern Viewmodel with static Application constraints
                val factory = BodyControlViewModelFactory(application)
                val viewModel: BodyControlViewModel = viewModel(factory = factory)

                var activeTab by remember { mutableStateOf("Home") }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0C0D0D)),
                    bottomBar = {
                        Column {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bottom_nav_bar")
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = Color(0xFF0C0D0D),
                                tonalElevation = 8.dp
                            ) {
                                val navTabs = listOf(
                                    NavTabItem("Home", Icons.Default.Home, Icons.Outlined.Home),
                                    NavTabItem("Meals", Icons.Default.RestaurantMenu, Icons.Outlined.RestaurantMenu),
                                    NavTabItem("Workout", Icons.Default.FitnessCenter, Icons.Outlined.FitnessCenter),
                                    NavTabItem("AI", Icons.Default.SmartToy, Icons.Outlined.SmartToy),
                                    NavTabItem("Insights", Icons.Default.ShowChart, Icons.Outlined.ShowChart),
                                    NavTabItem("Profile", Icons.Default.Person, Icons.Outlined.Person)
                                )

                                navTabs.forEach { tab ->
                                    val isSelected = activeTab == tab.name
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { activeTab = tab.name },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                                contentDescription = tab.name,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.name,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.Black,
                                            selectedTextColor = Color(0xFF22C55E),
                                            indicatorColor = Color(0xFF22C55E),
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                                    )
                                }
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            "Home" -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { target -> activeTab = target }
                            )
                            "Meals" -> MealsScreen(
                                viewModel = viewModel
                            )
                            "Workout" -> WorkoutsScreen(
                                viewModel = viewModel
                            )
                            "AI" -> CoachScreen(
                                viewModel = viewModel
                            )
                            "Insights" -> InsightsScreen(
                                viewModel = viewModel
                            )
                            "Profile" -> ProfileScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

data class NavTabItem(
    val name: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
)
