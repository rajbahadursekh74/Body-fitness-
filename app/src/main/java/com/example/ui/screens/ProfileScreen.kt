package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BodyControlViewModel
import com.example.ui.theme.BodyControlGreen
import com.example.ui.theme.BodyControlDeepCharcoal
import com.example.ui.theme.BodyControlGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: BodyControlViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Dialog state for biometrics editor
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editAge by remember { mutableStateOf("") }
    var editHeight by remember { mutableStateOf("") }
    var editWeight by remember { mutableStateOf("") }

    // Synchronize initial input values when dialog triggers
    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            editName = profile.name
            editAge = profile.age.toString()
            editHeight = profile.heightCm.toString()
            editWeight = profile.weightKg.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BodyControlDeepCharcoal)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Settings icon corner header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { /* Simulated Settings */ },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF262A31))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "System Settings",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. Main Premium Member Card Avatar slot ---
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Elegant pulsing halo indicators
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(2.dp, BodyControlGreen, CircleShape)
                    .background(Color(0xFF262A31)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = BodyControlGreen,
                    modifier = Modifier.size(48.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(BodyControlGreen, RoundedCornerShape(8.dp))
                    .border(1.5.dp, BodyControlDeepCharcoal, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PRO",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = profile.name,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )

        // Premium Badge label
        Row(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .background(BodyControlGreen.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .border(0.5.dp, BodyControlGreen, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BodyControlGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Premium Member Since 2024",
                color = BodyControlGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // --- 3. Interactive Biometrics Row Dashboard ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEditDialog = true }
                .testTag("biometrics_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BodyControlGray),
            border = CardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Biometrics",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TAP TO UPDATE",
                        color = BodyControlGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BiometricCardItem(label = "Age", value = "${profile.age}")
                    VerticalMetricDivider()
                    BiometricCardItem(label = "Height", value = "${profile.heightCm} cm")
                    VerticalMetricDivider()
                    BiometricCardItem(label = "Weight", value = "${profile.weightKg} kg")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 4. Sub Configuration list elements ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("configs_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BodyControlGray),
            border = CardBorder()
        ) {
            Column {
                ProfileSlotRow(icon = Icons.Default.ContactPage, title = "Personal Information")
                ProfileSlotDivider()
                ProfileSlotRow(icon = Icons.Default.EmojiEvents, title = "Health Goals")
                ProfileSlotDivider()
                ProfileSlotRow(icon = Icons.Default.NotificationsActive, title = "Notification Settings")
                ProfileSlotDivider()
                ProfileSlotRow(icon = Icons.Default.CreditCard, title = "Subscription Management")
                ProfileSlotDivider()
                ProfileSlotRow(icon = Icons.Default.Watch, title = "Connected Devices", rightSubtitle = "2 Active")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 5. Promo Sharing Center invite code ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("invite_promo_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BodyControlGray),
            border = CardBorder()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Invite Friends",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Give 1 month of Premium free and get a limited edition Body Control badge.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // High contrast copy code widget
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BodyControlDeepCharcoal)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.promoCode,
                        color = BodyControlGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(profile.promoCode))
                            Toast.makeText(context, "Promo code copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BodyControlGreen)
                            .size(34.dp)
                            .testTag("copy_promo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 6. Outlined Logout bar button ---
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "Logout clicked (Prototype simulation)", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(27.dp),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT", fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }

        Spacer(modifier = Modifier.height(90.dp)) // edge nav buffer
    }

    // Biometrics Editing Dialog Modal
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(text = "Modify Biometrics Profile", color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = editAge,
                        onValueChange = { editAge = it },
                        label = { Text("Age (years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = editHeight,
                        onValueChange = { editHeight = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { editWeight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ageInt = editAge.toIntOrNull() ?: profile.age
                        val heightInt = editHeight.toIntOrNull() ?: profile.heightCm
                        val weightF = editWeight.toFloatOrNull() ?: profile.weightKg
                        viewModel.updateProfile(editName, ageInt, heightInt, weightF)
                        showEditDialog = false
                        Toast.makeText(context, "Biometrics updated reactively", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BodyControlGreen)
                ) {
                    Text("Save Changes", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Discard", color = Color.Gray)
                }
            },
            containerColor = BodyControlGray
        )
    }
}

@Composable
fun BiometricCardItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = BodyControlGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun VerticalMetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(Color.White.copy(alpha = 0.05f))
    )
}

@Composable
fun ProfileSlotRow(
    icon: ImageVector,
    title: String,
    rightSubtitle: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Slot select simulation */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (rightSubtitle.isNotEmpty()) {
                Text(
                    text = rightSubtitle,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProfileSlotDivider() {
    Divider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
}
