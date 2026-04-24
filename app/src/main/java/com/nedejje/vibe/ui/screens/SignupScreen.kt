package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.viewmodel.AuthState
import com.nedejje.vibe.viewmodel.AuthViewModel

private val DeepViolet     = Color(0xFF0D0720)
private val MidnightPurple = Color(0xFF1A0F3C)
private val ElectricPlum   = Color(0xFF7C3AED)
private val NeonLilac      = Color(0xFFBB86FC)
private val SoftCream      = Color(0xFFF5F0FF)
private val MutedLavender  = Color(0xFF9575CD)
private val ErrorRose      = Color(0xFFFF6B8A)
private val CardSurface    = Color(0xFF1E1040)
private val GoldAccent     = Color(0xFFE8B84B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.container.userRepository)
    )
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole    by remember { mutableStateOf("Guest") } // "Guest" | "Host"
    var roleExpanded    by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val user = (authState as AuthState.Success).user
            val dest = if (user.isAdmin) Screen.AdminHome.route else Screen.Home.route
            navController.navigate(dest) { popUpTo(Screen.Signup.route) { inclusive = true } }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(MidnightPurple, DeepViolet, Color(0xFF050210)),
                    center = Offset(200f + 200f * gradientShift, 400f),
                    radius = 1100f
                )
            )
    ) {
        // Decorative blobs
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(listOf(GoldAccent.copy(alpha = 0.18f), Color.Transparent)),
                    RoundedCornerShape(50)
                )
                .align(Alignment.TopStart)
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 80.dp, y = 100.dp)
                .background(
                    Brush.radialGradient(listOf(ElectricPlum.copy(alpha = 0.28f), Color.Transparent)),
                    RoundedCornerShape(50)
                )
                .align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonLilac)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Header
            Text("vibe.", fontSize = 42.sp, fontWeight = FontWeight.Black, color = NeonLilac, letterSpacing = (-1).sp)
            Spacer(Modifier.height(4.dp))
            Text("Create your account", fontSize = 17.sp, fontWeight = FontWeight.Light,
                color = SoftCream.copy(alpha = 0.7f))
            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardSurface.copy(alpha = 0.88f))
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = MutedLavender) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    colors = signupFieldColors()
                )

                // Email
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = MutedLavender) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    colors = signupFieldColors()
                )

                // Phone
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = MutedLavender) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    colors = signupFieldColors()
                )

                // Role selector
                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(
                        value = if (selectedRole == "Host") "Event Organiser / Host" else "Guest / Attendee",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("I am a...") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = {
                            Icon(
                                if (selectedRole == "Host") Icons.Default.AdminPanelSettings else Icons.Default.People,
                                null, tint = if (selectedRole == "Host") GoldAccent else MutedLavender
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        colors = signupFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                        modifier = Modifier.background(CardSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Event Organiser / Host", color = SoftCream) },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = GoldAccent) },
                            onClick = { selectedRole = "Host"; roleExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Guest / Attendee", color = SoftCream) },
                            leadingIcon = { Icon(Icons.Default.People, null, tint = MutedLavender) },
                            onClick = { selectedRole = "Guest"; roleExpanded = false }
                        )
                    }
                }

                // Role badge
                AnimatedVisibility(visible = selectedRole == "Host") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldAccent.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Text(
                                "You'll have access to the Event Manager Dashboard",
                                fontSize = 12.sp, color = GoldAccent
                            )
                        }
                    }
                }

                // Password
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MutedLavender) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = MutedLavender
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    colors = signupFieldColors()
                )

                // Error
                AnimatedVisibility(visible = authState is AuthState.Error, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                    val msg = (authState as? AuthState.Error)?.message ?: ""
                    Surface(color = ErrorRose.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(msg, color = ErrorRose, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(10.dp))
                    }
                }

                // CTA
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.signup(
                            name     = name,
                            email    = email,
                            phone    = phone,
                            password = password,
                            isAdmin  = selectedRole == "Host"
                        )
                    },
                    enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                            && authState !is AuthState.Loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricPlum,
                        disabledContainerColor = ElectricPlum.copy(alpha = 0.4f)
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = SoftCream, strokeWidth = 2.5.dp)
                    } else {
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = SoftCream)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("Already have an account?", color = SoftCream.copy(alpha = 0.7f), fontSize = 14.sp)
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Log in", color = NeonLilac, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun signupFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor     = Color(0xFFF5F0FF),
    unfocusedTextColor   = Color(0xFFF5F0FF),
    focusedLabelColor    = Color(0xFFBB86FC),
    unfocusedLabelColor  = Color(0xFF9575CD),
    focusedBorderColor   = Color(0xFFBB86FC),
    unfocusedBorderColor = Color(0xFF9575CD).copy(alpha = 0.4f),
    cursorColor          = Color(0xFFBB86FC)
)
