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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.R
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
            ),
        contentAlignment = Alignment.Center // Part B: Centered Layout
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(R.dimen.padding_large), vertical = dimensionResource(R.dimen.spacer_large)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Part B: Centered Layout
        ) {
            // Back button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = stringResource(R.string.back), 
                        tint = NeonLilac
                    )
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))

            // Header
            Text(
                text = stringResource(R.string.vibe_logo_text), 
                style = MaterialTheme.typography.displayMedium, // Part A: Typography
                color = NeonLilac
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
            Text(
                text = stringResource(R.string.signup_title), 
                style = MaterialTheme.typography.titleMedium,
                color = SoftCream.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.spacer_large)))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.padding_large)))
                    .background(CardSurface.copy(alpha = 0.88f))
                    .padding(horizontal = dimensionResource(R.dimen.padding_large), vertical = dimensionResource(R.dimen.spacer_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                // Name
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.full_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = MutedLavender) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    colors = signupFieldColors(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                )

                // Email
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = MutedLavender) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    colors = signupFieldColors(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
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
                        colors = signupFieldColors(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
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

                // Password
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password_label)) },
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
                    colors = signupFieldColors(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                )

                // Error
                AnimatedVisibility(visible = authState is AuthState.Error) {
                    val msg = (authState as? AuthState.Error)?.message ?: ""
                    Surface(color = ErrorRose.copy(alpha = 0.12f), shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)), modifier = Modifier.fillMaxWidth()) {
                        Text(msg, color = ErrorRose, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)))
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
                    enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading,
                    modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPlum, disabledContainerColor = ElectricPlum.copy(alpha = 0.4f))
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)), color = SoftCream, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.create_account_button), style = MaterialTheme.typography.titleMedium, color = SoftCream)
                    }
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_large)))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.already_have_account_text), color = SoftCream.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(stringResource(R.string.login_button), color = NeonLilac, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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
