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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
import com.nedejje.vibe.R
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

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

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
        contentAlignment = Alignment.Center // Part B: Centered Layout
    ) {
        // Decorative blurred blobs
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 120.dp, y = (-180).dp)
                .background(
                    Brush.radialGradient(colors = listOf(ElectricPlum.copy(alpha = 0.35f), Color.Transparent)),
                    shape = RoundedCornerShape(50)
                )
                .align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
                .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_large)))
                .background(CardSurface.copy(alpha = 0.85f))
                .padding(horizontal = dimensionResource(R.dimen.padding_large), vertical = dimensionResource(R.dimen.spacer_large)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Part B: Centered Layout
        ) {
            Text(
                text  = "vibe.",
                style = MaterialTheme.typography.displayMedium, // Part A: Typography
                color = NeonLilac
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
            Text(
                text  = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.titleMedium,
                color = SoftCream.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.spacer_large)))

            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text(stringResource(R.string.email_label)) },
                modifier      = Modifier.fillMaxWidth(),
                leadingIcon   = { Icon(Icons.Default.Email, null, tint = MutedLavender) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftCream, unfocusedTextColor = SoftCream,
                    focusedLabelColor = NeonLilac, unfocusedLabelColor = MutedLavender,
                    focusedBorderColor = NeonLilac, unfocusedBorderColor = MutedLavender.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))

            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text(stringResource(R.string.password_label)) },
                modifier      = Modifier.fillMaxWidth(),
                leadingIcon   = { Icon(Icons.Default.Lock, null, tint = MutedLavender) },
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MutedLavender
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (email.isNotBlank() && password.isNotBlank()) viewModel.login(email.trim(), password)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftCream, unfocusedTextColor = SoftCream,
                    focusedLabelColor = NeonLilac, unfocusedLabelColor = MutedLavender,
                    focusedBorderColor = NeonLilac, unfocusedBorderColor = MutedLavender.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { viewModel.sendPasswordReset(email.trim()) }) {
                    Text(stringResource(R.string.forgot_password), color = MutedLavender, style = MaterialTheme.typography.labelSmall)
                }
            }

            AnimatedVisibility(visible = authState is AuthState.Error) {
                val msg = (authState as? AuthState.Error)?.message ?: ""
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.padding_small)),
                    color = ErrorRose.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small))
                ) {
                    Text(text = msg, color = ErrorRose, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)))
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))

            Button(
                onClick  = {
                    focusManager.clearFocus()
                    viewModel.login(email.trim(), password)
                },
                enabled  = email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
                shape    = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                colors   = ButtonDefaults.buttonColors(containerColor = ElectricPlum, disabledContainerColor = ElectricPlum.copy(alpha = 0.4f))
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)), color = SoftCream, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.login_button), style = MaterialTheme.typography.titleMedium, color = SoftCream)
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_large)))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SoftCream.copy(alpha = 0.12f))
                Text("  or  ", color = SoftCream.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall)
                HorizontalDivider(modifier = Modifier.weight(1f), color = SoftCream.copy(alpha = 0.12f))
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_large)))

            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.new_to_vibe), color = SoftCream.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { navController.navigate(Screen.Signup.route) }) {
                    Text(stringResource(R.string.signup_button), color = NeonLilac, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
