package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.viewmodel.AuthViewModel
import com.nedejje.vibe.viewmodel.AuthState

// ─── Color Palette ────────────────────────────────────────────────────────────
private val DeepViolet   = Color(0xFF0D0720)
private val MidnightPurple = Color(0xFF1A0F3C)
private val ElectricPlum = Color(0xFF7C3AED)
private val NeonLilac    = Color(0xFFBB86FC)
private val SoftCream    = Color(0xFFF5F0FF)
private val MutedLavender = Color(0xFF9575CD)
private val ErrorRose    = Color(0xFFFF6B8A)
private val CardSurface  = Color(0xFF1E1040)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app.container.userRepository)
    )

    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Navigate to Home on successful login
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(MidnightPurple, DeepViolet, Color(0xFF050210)),
                    center = Offset(300f * gradientShift + 100f, 600f),
                    radius  = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Decorative blurred blob top-right
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 120.dp, y = (-180).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ElectricPlum.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
                .align(Alignment.TopEnd)
        )

        // Decorative blurred blob bottom-left
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-80).dp, y = 160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonLilac.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
                .align(Alignment.BottomStart)
        )

        // ── Card ──────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(CardSurface.copy(alpha = 0.85f))
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Brand / Header ────────────────────────────────────────────────
            Text(
                text  = "vibe.",
                fontSize   = 42.sp,
                fontWeight = FontWeight.Black,
                color = NeonLilac,
                letterSpacing = (-1).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Welcome back",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Light,
                color = SoftCream.copy(alpha = 0.75f),
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(32.dp))

            // ── Email Field ───────────────────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text("Email") },
                modifier      = Modifier.fillMaxWidth(),
                leadingIcon   = {
                    Icon(Icons.Default.Email, contentDescription = null,
                        tint = MutedLavender)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftCream,
                    unfocusedTextColor = SoftCream,
                    focusedLabelColor = NeonLilac,
                    unfocusedLabelColor = MutedLavender,
                    focusedBorderColor = NeonLilac,
                    unfocusedBorderColor = MutedLavender.copy(alpha = 0.4f)
                )
            )

            Spacer(Modifier.height(14.dp))

            // ── Password Field ────────────────────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Password") },
                modifier      = Modifier.fillMaxWidth(),
                leadingIcon   = {
                    Icon(Icons.Default.Lock, contentDescription = null,
                        tint = MutedLavender)
                },
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide" else "Show",
                            tint = MutedLavender
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank())
                            viewModel.login(email.trim(), password)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftCream,
                    unfocusedTextColor = SoftCream,
                    focusedLabelColor = NeonLilac,
                    unfocusedLabelColor = MutedLavender,
                    focusedBorderColor = NeonLilac,
                    unfocusedBorderColor = MutedLavender.copy(alpha = 0.4f)
                )
            )

            // ── Forgot Password ───────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { viewModel.sendPasswordReset(email.trim()) }) {
                    Text(
                        "Forgot password?",
                        color  = MutedLavender,
                        fontSize = 12.sp
                    )
                }
            }

            // ── Error Banner ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = authState is AuthState.Error,
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                val msg = (authState as? AuthState.Error)?.message ?: ""
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color        = ErrorRose.copy(alpha = 0.12f),
                    shape        = RoundedCornerShape(12.dp),
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text      = msg,
                        color     = ErrorRose,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Login Button ──────────────────────────────────────────────────
            Button(
                onClick  = {
                    focusManager.clearFocus()
                    viewModel.login(email.trim(), password)
                },
                enabled  = email.isNotBlank() && password.isNotBlank()
                        && authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = ElectricPlum,
                    disabledContainerColor = ElectricPlum.copy(alpha = 0.4f)
                )
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(22.dp),
                        color     = SoftCream,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        "Log in",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = SoftCream
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Divider ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = SoftCream.copy(alpha = 0.12f))
                Text(
                    "  or  ",
                    color    = SoftCream.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f),
                    color = SoftCream.copy(alpha = 0.12f))
            }

            Spacer(Modifier.height(20.dp))

            // ── Sign Up Link ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "New to Vibe?",
                    color = SoftCream.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                TextButton(onClick = { navController.navigate(Screen.Signup.route) }) {
                    Text(
                        "Sign up",
                        color = NeonLilac,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
