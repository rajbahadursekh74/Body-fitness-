package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import kotlin.math.roundToInt
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.Achievement
import com.example.ui.BodyControlViewModel
import com.example.ui.theme.BodyControlGreen
import com.example.ui.theme.BodyControlDeepCharcoal
import com.example.ui.theme.BodyControlGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: BodyControlViewModel,
    modifier: Modifier = Modifier
) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val metrics by viewModel.currentMetrics.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BodyControlDeepCharcoal),
        contentPadding = PaddingValues(16.dp)
    ) {
        // --- 1. Header ---
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Insights",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Biometric analysis and progression logs",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // --- 2. Body Weight progression line graph card ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("insights_weight_progress_card"),
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
                                text = "BODY WEIGHT",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${metrics.loggedWeightKg} kg",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "~ -1.2%",
                                color = BodyControlGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "vs last month",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Beautiful neon Bezier canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val points = listOf(78.2f, 77.8f, 77.1f, 76.9f, 76.4f, metrics.loggedWeightKg)
                            val max = 79.0f
                            val min = 75.0f
                            val stepX = size.width / (points.size - 1)
                            val stepY = size.height / (max - min)

                            val path = Path()
                            val coords = points.mapIndexed { idx, v ->
                                Offset(idx * stepX, size.height - (v - min) * stepY)
                            }

                            path.moveTo(coords[0].x, coords[0].y)
                            for (i in 0 until coords.size - 1) {
                                val from = coords[i]
                                val to = coords[i + 1]
                                path.cubicTo(
                                    (from.x + to.x) / 2f, from.y,
                                    (from.x + to.x) / 2f, to.y,
                                    to.x, to.y
                                )
                            }

                            // Draw reference axis line
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, size.height * 0.7f),
                                end = Offset(size.width, size.height * 0.7f),
                                strokeWidth = 2f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )

                            drawPath(
                                path = path,
                                brush = Brush.linearGradient(listOf(BodyControlGreen, BodyControlGreen.copy(alpha = 0.5f))),
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 SEP", color = Color.Gray, fontSize = 9.sp)
                        Text("10 SEP", color = Color.Gray, fontSize = 9.sp)
                        Text("20 SEP", color = Color.Gray, fontSize = 9.sp)
                        Text("30 SEP", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }

        // --- 3. Body Composition grid elements ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weight Lost card widget
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .testTag("insights_lost_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = BodyControlGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Weight Lost", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("-4.2kg", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Text("Since inception", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }

                // Fat % Card widget
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .testTag("insights_fat_percent_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Percent, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fat Ratio", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("18.5%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Text("Ideal Zone: 14-20%", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Muscle Mass Single Full block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("insights_muscle_mass_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BodyControlGray),
                border = CardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BodyControlGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = BodyControlGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Muscle Mass Volume", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Highly reactive skeletal tissue weight", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Text("34.0 kg", color = BodyControlGreen, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // --- 4. Monthly Calorie intake list tracker graph ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("insights_summary_graph_card"),
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
                            Text("Monthly Intake Comparison Map", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Actual fueling vs specified strategy", color = Color.Gray, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BodyControlGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Avg: 2,140 kcal", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simplified analytical calorie card nodes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CalorieWeekColumnMini(day = "WK1", kcal = 2100, isSelected = false)
                        CalorieWeekColumnMini(day = "WK2", kcal = 2250, isSelected = false)
                        CalorieWeekColumnMini(day = "WK3", kcal = 2080, isSelected = false)
                        CalorieWeekColumnMini(day = "WK4", kcal = 2140, isSelected = true)
                    }
                }
            }
        }

        // --- 5. Unlocked Gamified Badges rack ---
        item {
            Text(
                text = "Recent Achievements",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Horizontal scrolling trophies strip
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Avoid edge clipping
            ) {
                items(achievements) { ach ->
                    AchievementTrohpyNode(ach)
                }
            }
        }
    }
}

@Composable
fun CalorieWeekColumnMini(day: String, kcal: Int, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(if (isSelected) BodyControlGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val multiplier = (kcal / 2500f).coerceIn(0.2f, 1f)
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF262A31)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(multiplier)
                    .clip(CircleShape)
                    .background(if (isSelected) BodyControlGreen else BodyControlGreen.copy(alpha = 0.6f))
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(day, color = if (isSelected) BodyControlGreen else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("${kcal}k", color = Color.LightGray, fontSize = 9.sp)
    }
}

@Composable
fun AchievementTrohpyNode(achievement: Achievement) {
    val ringColor = if (achievement.isUnlocked) BodyControlGreen else Color(0xFF262A31)
    val circleOpacity = if (achievement.isUnlocked) 1f else 0.4f

    Card(
        modifier = Modifier
            .width(135.dp)
            .height(135.dp)
            .testTag("achievement_${achievement.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BodyControlGray),
        border = CardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animate completion dial
                val animatedVal by animateFloatAsState(
                    targetValue = achievement.progress,
                    animationSpec = tween(1200)
                )

                Canvas(modifier = Modifier.size(46.dp)) {
                    drawCircle(
                        color = Color(0xFF262A31),
                        style = Stroke(width = 8f)
                    )
                    drawArc(
                        color = if (achievement.isUnlocked) BodyControlGreen else BodyControlGreen.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = animatedVal * 360f,
                        useCenter = false,
                        style = Stroke(width = 8f)
                    )
                }

                // Relevant visual center icon
                val badgeIcon = when (achievement.iconType) {
                    "water" -> Icons.Default.LocalDrink
                    "steps" -> Icons.Default.DirectionsRun
                    else -> Icons.Default.WorkspacePremium
                }
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = ringColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = if (achievement.isUnlocked) "UNLOCKED" else "${(achievement.progress * 100).roundToInt()}% DONE",
                color = if (achievement.isUnlocked) BodyControlGreen else Color.Gray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
