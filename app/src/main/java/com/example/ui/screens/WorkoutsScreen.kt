package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BodyControlViewModel
import com.example.ui.theme.BodyControlGreen
import com.example.ui.theme.BodyControlDeepCharcoal
import com.example.ui.theme.BodyControlGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: BodyControlViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Home") }
    val loggedWorkouts by viewModel.workoutLogs.collectAsStateWithLifecycle()

    var showLogConfirmation by remember { mutableStateOf<String?>(null) }

    val homeRoutines = listOf(
        WorkoutRoutineStub(
            title = "Full Body Fat Loss",
            subtitle = "Core & Endurance focus",
            durationMin = 45,
            calories = 340,
            intensity = "Intermediate",
            progress = 0.62f,
            intensityColor = Color(0xFFFACC15)
        ),
        WorkoutRoutineStub(
            title = "Mobility Flow",
            subtitle = "Active recovery session",
            durationMin = 20,
            calories = 120,
            intensity = "Beginner",
            progress = 0.25f,
            intensityColor = BodyControlGreen
        ),
        WorkoutRoutineStub(
            title = "HIIT Sprint",
            subtitle = "Cardiovascular peak",
            durationMin = 30,
            calories = 480,
            intensity = "Elite",
            progress = 0.95f,
            intensityColor = Color(0xFFEF4444)
        )
    )

    val gymRoutines = listOf(
        WorkoutRoutineStub(
            title = "Strength Power II",
            subtitle = "Heavy weights & Low reps",
            durationMin = 60,
            calories = 450,
            intensity = "Advanced",
            progress = 0.8f,
            intensityColor = Color(0xFFFB923C)
        ),
        WorkoutRoutineStub(
            title = "Hyper-Burn 500",
            subtitle = "Elite athletic performance",
            durationMin = 45,
            calories = 520,
            intensity = "Elite",
            progress = 0.98f,
            intensityColor = Color(0xFFEF4444)
        )
    )

    val routinesToDisplay = if (activeTab == "Home") homeRoutines else gymRoutines

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BodyControlDeepCharcoal)
    ) {
        // --- 1. Workout Header ---
        Text(
            text = "Training Library",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Structured plans crafted by fitness coaches",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // --- 2. Featured Workout of the Day ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("featured_workout_hero"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BodyControlGray),
                    border = CardBorder()
                ) {
                    Column {
                        // Styled workout visual canvas banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(BodyControlDeepCharcoal, BodyControlGray)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Custom circular focus vector items
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = BodyControlGreen.copy(alpha = 0.05f),
                                    radius = 180f,
                                    center = Offset(this.size.width, 0f)
                                )
                                drawCircle(
                                    color = BodyControlGreen.copy(alpha = 0.03f),
                                    radius = 110f,
                                    center = Offset(this.size.width / 4, this.size.height)
                                )
                            }

                            // Glowing play circular logger
                            IconButton(
                                onClick = {
                                    viewModel.logCompletedWorkout("Hyper-Burn 500", 45, 520)
                                    showLogConfirmation = "Hyper-Burn 500"
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(BodyControlGreen)
                                    .testTag("featured_workout_play")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Workout",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Hero metadata tags
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(BodyControlGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("FEATURED", color = BodyControlGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Elite Level", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Bottom description details
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hyper-Burn 500",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = "Duration", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("45 min", color = Color.LightGray, fontSize = 13.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories", tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("520 kcal", color = Color.LightGray, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. Home/Gym Tab Category Selector ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val tabs = listOf("Home", "Gym")
                    tabs.forEach { tName ->
                        val isSelected = activeTab == tName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BodyControlGreen.copy(alpha = 0.15f) else BodyControlGray)
                                .border(
                                    1.dp,
                                    if (isSelected) BodyControlGreen else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { activeTab = tName }
                                .testTag("workout_tab_$tName"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (tName == "Gym") Icons.Default.FitnessCenter else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isSelected) BodyControlGreen else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tName,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. filtered Routines Library list elements ---
            items(routinesToDisplay) { routine ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("workout_item_${routine.title.replace(" ", "_")}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BodyControlGray),
                    border = CardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored visual focus circle
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(routine.intensityColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = routine.intensityColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Details Core
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = routine.subtitle,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                            )

                            // Quick specification tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${routine.durationMin} mins",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "•",
                                    color = Color.DarkGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${routine.calories} kcal",
                                    color = Color(0xFFF97316),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "•",
                                    color = Color.DarkGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = routine.intensity,
                                    color = routine.intensityColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Complete Action Circle Trigger
                        IconButton(
                            onClick = {
                                viewModel.logCompletedWorkout(routine.title, routine.durationMin, routine.calories)
                                showLogConfirmation = routine.title
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262A31))
                                .testTag("log_workout_button_${routine.title.replace(" ", "_")}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Log session completed",
                                tint = BodyControlGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }

    // Confirmation Toast Scaffold overlays
    if (showLogConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showLogConfirmation = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BodyControlGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Workout Logged!", color = Color.White)
                }
            },
            text = {
                Text(
                    text = "Congratulations! You completed '${showLogConfirmation}'. Calories burned and overall steps have been synced to your active daily fueling indicators.",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLogConfirmation = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BodyControlGreen)
                ) {
                    Text("Awesome", color = Color.Black)
                }
            },
            containerColor = BodyControlGray
        )
    }
}

data class WorkoutRoutineStub(
    val title: String,
    val subtitle: String,
    val durationMin: Int,
    val calories: Int,
    val intensity: String,
    val progress: Float,
    val intensityColor: Color
)
