package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MealItem
import com.example.ui.BodyControlViewModel
import com.example.ui.theme.BodyControlGreen
import com.example.ui.theme.BodyControlDeepCharcoal
import com.example.ui.theme.BodyControlGray
import com.example.ui.theme.BodyControlBorderColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(
    viewModel: BodyControlViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()
    val isRegenerating by viewModel.isAiRegeneratingMeals.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showRegenDialog by remember { mutableStateOf(false) }

    // Calculate dynamic base diet parameters
    val baseCalorieTarget = ((10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) + 5).toInt().coerceAtLeast(1500)
    val dailyCalorieTarget = baseCalorieTarget + 400

    val totalCalories = meals.sumOf { it.kCal }

    // Dialog state
    var selectedGoalOption by remember { mutableStateOf("Low Carb Shred") }
    val goalOptions = listOf(
        "Low Carb Shred",
        "High Protein Muscle Gain",
        "Keto Fueling Plan",
        "Vegetarian Clean Strategy"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BodyControlDeepCharcoal)
    ) {
        // --- 1. Calendar Tab Header ---
        Text(
            text = "Nutritional Planner",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Plan nutrition with your AI coaching advisor",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        // Linear Horizontal Calendar Cards Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.daysOfWeek) { day ->
                val isSelected = selectedDate == day.dateString
                Box(
                    modifier = Modifier
                        .width(62.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BodyControlGreen.copy(alpha = 0.15f) else BodyControlGray)
                        .border(
                            1.dp,
                            if (isSelected) BodyControlGreen else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.selectDate(day.dateString) }
                        .padding(vertical = 12.dp)
                        .testTag("calendar_day_${day.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.name,
                            color = if (isSelected) BodyControlGreen else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dateNum,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            // --- 2. Fuel Goals Card ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("meals_target_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BodyControlGray),
                    border = CardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily Nutrition",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Smart targets calculated automatically",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            // Sparkling AI Adjusted Indicator Badge
                            Box(
                                modifier = Modifier
                                    .background(BodyControlGreen.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .border(0.5.dp, BodyControlGreen, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Adjusted",
                                        tint = BodyControlGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI ADJUSTED",
                                        color = BodyControlGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%,d", totalCalories),
                                color = BodyControlGreen,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " / ${String.format("%,d", dailyCalorieTarget)} kcal",
                                color = Color.Gray,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val fuelProgress = if (dailyCalorieTarget > 0) totalCalories.toFloat() / dailyCalorieTarget else 0f
                        LinearProgressIndicator(
                            progress = { fuelProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = BodyControlGreen,
                            trackColor = Color(0xFF262A31)
                        )
                    }
                }
            }

            // --- 3. Empty State or Meals List ---
            if (meals.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = "No food logged",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Meals Scheduled",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate a personalized health eating program for this day using our AI model below.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                    }
                }
            } else {
                items(meals) { meal ->
                    MealTimelineRow(meal)
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Bottom Spacing to buffer behind FAB container
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    // --- 4. Floating AI Regeneration Console ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = { showRegenDialog = true },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(54.dp)
                .testTag("ai_regenerate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BodyControlGreen),
            shape = RoundedCornerShape(27.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRegenerating) "AI Synchronizing Nutrition..." else "AI Regenerate Plan",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    // AI Customize Dialog
    if (showRegenDialog) {
        AlertDialog(
            onDismissRequest = { showRegenDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BodyControlGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Nutrition Strategy", color = Color.White)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Customize the dietary algorithm so the Gemini model adjusts proteins and fats for today's physical split.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    goalOptions.forEach { opt ->
                        val isSelected = selectedGoalOption == opt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF262A31) else Color.Transparent)
                                .clickable { selectedGoalOption = opt }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedGoalOption = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = BodyControlGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = opt,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRegenDialog = false
                        viewModel.regenerateMealsPlan(selectedGoalOption)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BodyControlGreen)
                ) {
                    Text("Regenerate Plan", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = BodyControlGray
        )
    }
}

@Composable
fun MealTimelineRow(meal: MealItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meal_item_${meal.category}"),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Dot and Bar Connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp)
        ) {
            Text(
                text = meal.timeLabel,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(BodyControlGreen)
                    .border(2.dp, BodyControlDeepCharcoal, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(90.dp)
                    .background(Color(0xFF262A31))
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Food Card details
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BodyControlGray),
            border = CardBorder()
        ) {
            Column {
                // High-fidelity graphic representation panel (simulating unique foods)
                val categoryColor = when (meal.category) {
                    "Breakfast" -> BodyControlGreen
                    "Snack" -> Color(0xFF60A5FA)
                    "Lunch" -> Color(0xFFFB7185)
                    else -> Color(0xFFEEF2F6)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(categoryColor.copy(alpha = 0.25f), Color(0xFF262A31))
                            )
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = meal.category.uppercase(),
                                    color = categoryColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = meal.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${meal.kCal} kcal",
                        color = BodyControlGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                    )
                }

                // Macro breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BodyControlDeepCharcoal)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MacroShortLabel("PROTEIN", "${meal.proteinG}g", Color(0xFFEEF2F6))
                    MacroShortLabel("CARBS", "${meal.carbsG}g", Color(0xFF38BDF8))
                    MacroShortLabel("FATS", "${meal.fatsG}g", Color(0xFFFACC15))
                }
            }
        }
    }
}

@Composable
fun MacroShortLabel(label: String, valStr: String, dotCol: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotCol)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = valStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
