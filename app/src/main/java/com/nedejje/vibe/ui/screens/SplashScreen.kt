package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.ui.navigation.Screen
import kotlinx.coroutines.delay

private val DeepViolet  = Color(0xFF0D0720)
private val ElectricPlum = Color(0xFF7C3AED)
private val NeonLilac   = Color(0xFFBB86FC)
private val GoldAccent  = Color(0xFFE8B84B)

@Composable
fun SplashScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    var subVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        visible = true
        delay(500)
        subVisible = true
        delay(1800)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A0F3C), DeepViolet),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center // Part B: Centered Layout
    ) {
        // Glow behind logo
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(listOf(ElectricPlum.copy(alpha = glowAlpha * 0.4f), Color.Transparent))
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Part B: Centered Layout
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = FastOutSlowInEasing), initialScale = 0.7f)
            ) {
                Text(
                    text = stringResource(R.string.vibe_logo_text),
                    style = MaterialTheme.typography.displayLarge, // Part A: Typography
                    color = NeonLilac
                )
            }

            AnimatedVisibility(
                visible = subVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                    Text(
                        text = stringResource(R.string.experience_moment),
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent.copy(alpha = 0.9f),
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        // Version tag at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = dimensionResource(R.dimen.spacer_large))
        ) {
            AnimatedVisibility(visible = subVisible, enter = fadeIn(tween(800))) {
                Text(
                    text = stringResource(R.string.premier_platform),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
