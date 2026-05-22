package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BodyControlViewModel
import com.example.ui.theme.BodyControlGreen
import com.example.ui.theme.BodyControlDeepCharcoal
import com.example.ui.theme.BodyControlGray
import com.example.ui.theme.BodyControlBorderColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BodyControlViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: (String) -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val metrics by viewModel.currentMetrics.collectAsStateWithLifecycle()
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    // Caloric Calculations
    // Dynamic calorie engine based on body height and weight biometrics
    val baseCalorieTarget = ((10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age) + 5).toInt().coerceAtLeast(1500)
    val dailyCalorieTarget = baseCalorieTarget + 400 // Active target
    
    // Sum current calories consumed today from meals database
    val caloriesConsumed = meals.sumOf { it.kCal }
    val totalProtein = meals.sumOf { it.proteinG }
    val totalCarbs = meals.sumOf { it.carbsG }
    val totalFats = meals.sumOf { it.fatsG }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BodyControlDeepCharcoal)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Top Greeting Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262A31))
                    .border(1.5.dp, BodyControlGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User avatar",
                    tint = BodyControlGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(BodyControlGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            color = BodyControlGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Personal Strategy Dashboard",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            IconButton(
                onClick = { /* Simulated alerts */ },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF161818))
                    .testTag("notification_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alerts",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. Active Fueling Circular Tracker ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_fueling_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BodyControlGray),
            border = CardBorder()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DAILY PROGRESS",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Active Fueling",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // Circular Progress Dial
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressRatio = if (dailyCalorieTarget > 0) caloriesConsumed.toFloat() / dailyCalorieTarget else 0f
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressRatio,
                        animationSpec = tween(1000)
                    )

                    Canvas(modifier = Modifier.size(180.dp)) {
                        // Background track
                        drawArc(
                            color = Color(0xFF262A31),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 16f, cap = StrokeCap.Round)
                        )
                        // Glowing foreground tract
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color(0xFF9ECA00), BodyControlGreen, Color(0xFFE0FF66))
                            ),
                            startAngle = -220f,
                            sweepAngle = animatedProgress * 260f,
                            useCenter = false,
                            style = Stroke(width = 16f, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", caloriesConsumed),
                            color = BodyControlGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "/ ${String.format("%,d", dailyCalorieTarget)} kcal",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Macros Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroCountWidget(label = "Protein", value = "${totalProtein}g", color = Color(0xFFEEF2F6))
                    MacroCountWidget(label = "Carbs", value = "${totalCarbs}g", color = Color(0xFF38BDF8))
                    MacroCountWidget(label = "Fats", value = "${totalFats}g", color = Color(0xFFFACC15))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Habit Tracking Grid Row ---
        // Water Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("water_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BodyControlGray),
            border = CardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = "Water",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Water Intake",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Increment Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { viewModel.addWater(250) },
                            colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp).testTag("water_add_250")
                        ) {
                            Text("+250ml", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { viewModel.addWater(500) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .testTag("water_add_500")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add 500ml", tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                val waterLiters = metrics.waterIntakeMl / 1000f
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.1f", waterLiters),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = " / 3.5 L",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom linear progress bar for Blue water container
                val animatedWaterProgress by animateFloatAsState(
                    targetValue = (metrics.waterIntakeMl / 3500f).coerceIn(0f, 1f),
                    animationSpec = tween(800)
                )
                LinearProgressIndicator(
                    progress = { animatedWaterProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFF262A31),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Steps Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .testTag("steps_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BodyControlGray),
                border = CardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = "Steps",
                                tint = BodyControlGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Steps", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        // Manual Step Log Assist
                        IconButton(
                            onClick = { viewModel.addSteps(1000) },
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262A31))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Log step", tint = BodyControlGreen, modifier = Modifier.size(12.dp))
                        }
                    }

                    Column {
                        Text(
                            text = String.format("%,d", metrics.steps),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Goal: 10,000",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    val stepProgress = (metrics.steps / 10000f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { stepProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = BodyControlGreen,
                        trackColor = Color(0xFF262A31)
                    )
                }
            }

            // BMI Slider Card
            // Dynamic BMI status card
            val bmiValue = if (profile.heightCm > 0) (profile.weightKg / ((profile.heightCm / 100f) * (profile.heightCm / 100f))) else 0f
            val bmiCategory = when {
                bmiValue < 18.5f -> "Underweight"
                bmiValue < 25.0f -> "Healthy"
                bmiValue < 30.0f -> "Overweight"
                else -> "Obese"
            }
            val bmiCategoryColor = when (bmiCategory) {
                "Healthy" -> BodyControlGreen
                "Underweight" -> Color(0xFFFACC15)
                "Overweight" -> Color(0xFFFB923C)
                else -> Color(0xFFEF4444)
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .testTag("bmi_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BodyControlGray),
                border = CardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current BMI", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .background(bmiCategoryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = bmiCategory,
                                color = bmiCategoryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Column {
                        Text(
                            text = String.format("%.1f", bmiValue),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Optimal range (18.5 - 24.9)",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    // A subtle visual 3-zone health bar representing BMI levels
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    ) {
                        val strokeRadius = 3.dp.toPx()
                        // 3 segmented bands: Underweight, Healthy, Overweight
                        val totalW = size.width
                        val padding = 4f
                        val segmentW = (totalW - padding * 2) / 3f

                        // Underweight segment (Yellow)
                        drawRoundRect(
                            color = Color(0xFFFACC15),
                            topLeft = Offset(0f, 0f),
                            size = Size(segmentW, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeRadius)
                        )
                        // Healthy segment (Green)
                        drawRoundRect(
                            color = BodyControlGreen,
                            topLeft = Offset(segmentW + padding, 0f),
                            size = Size(segmentW, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeRadius)
                        )
                        // Overweight segment (Red Orange)
                        drawRoundRect(
                            color = Color(0xFFF87171),
                            topLeft = Offset(segmentW * 2 + padding * 2, 0f),
                            size = Size(segmentW, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeRadius)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. Next Scheduled Meal Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToTab("Meals") }
                .testTag("next_meal_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BodyControlGray),
            border = CardBorder()
        ) {
            Column {
                // High-fidelity graphic container (simulates meal layout using Canvas)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Abstract representations of plates and fresh ingredients
                        drawCircle(
                            color = Color(0xFF334155),
                            radius = 120f,
                            center = Offset(size.width / 2f, size.height * 0.9f)
                        )
                        // Strawberry / Berries details (colors orange red blue)
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 28f,
                            center = Offset(size.width / 2f - 40f, size.height * 0.7f)
                        )
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 22f,
                            center = Offset(size.width / 2f + 40f, size.height * 0.75f)
                        )
                        drawCircle(
                            color = Color(0xFFFACC15),
                            radius = 18f,
                            center = Offset(size.width / 2f, size.height * 0.65f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go to Meals",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Next Meal • 11:00 AM",
                        color = BodyControlGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Oats with Fresh Fruits",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Calories", tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("380 kcal", color = Color.LightGray, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2F6))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("12g Protein", color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. Weight Progress Chart Board ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weight_progress_card"),
            shape = RoundedCornerShape(24.dp),
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
                            text = "Weight Progress",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Weekly weight logging timeline",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "-0.8 kg this week",
                            color = BodyControlGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Simple Tap weight logger
                        TextButton(
                            onClick = { viewModel.logWeight(profile.weightKg - 0.2f) },
                            colors = ButtonDefaults.textButtonColors(contentColor = BodyControlGreen),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.MonitorWeight, contentDescription = "Log", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Quick Log (-0.2kg)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Smooth Cubic Bezier Chart of Weight history
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val points = listOf(77.2f, 77.0f, 76.8f, 76.6f, metrics.loggedWeightKg)
                        val maxVal = 78.0f
                        val minVal = 75.0f
                        val diff = maxVal - minVal

                        val pointsCount = points.size
                        val stepX = size.width / (pointsCount - 1)

                        val path = Path()
                        val heightMultiplier = size.height / diff

                        // Generate Bezier path coordinates
                        val coordinates = points.mapIndexed { idx, value ->
                            val cX = idx * stepX
                            val cY = size.height - ((value - minVal) * heightMultiplier)
                            Offset(cX, cY)
                        }

                        path.moveTo(coordinates[0].x, coordinates[0].y)
                        for (i in 0 until coordinates.size - 1) {
                            val from = coordinates[i]
                            val to = coordinates[i + 1]
                            path.cubicTo(
                                x1 = (from.x + to.x) / 2f, y1 = from.y,
                                x2 = (from.x + to.x) / 2f, y2 = to.y,
                                x3 = to.x, y3 = to.y
                            )
                        }

                        // Drawing neon-green tracking line
                        drawPath(
                            path = path,
                            color = BodyControlGreen,
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )

                        // Draw reference indicators
                        coordinates.forEachIndexed { i, offset ->
                            drawCircle(
                                color = if (i == coordinates.size - 1) Color.White else BodyControlGreen,
                                radius = if (i == coordinates.size - 1) 10f else 6f,
                                center = offset
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chart labels matching the design grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelDays = listOf("M", "T", "W", "T", "F", "S", "S")
                    labelDays.forEachIndexed { idx, day ->
                        val isToday = day == "F" // Today is Friday 22nd in model dates
                        Text(
                            text = day,
                            color = if (isToday) BodyControlGreen else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Avoid bottom nav clip
    }
}

@Composable
fun MacroCountWidget(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun CardBorder() = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
