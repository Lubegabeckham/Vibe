package com.nedejje.vibe.ui.screens

<<<<<<< HEAD
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
=======
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.ui.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(key1 = true) {
        delay(2000L)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }
<<<<<<< HEAD

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
        contentAlignment = Alignment.Center
    ) {
        // Glow behind logo
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(listOf(ElectricPlum.copy(alpha = glowAlpha * 0.4f), Color.Transparent))
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = FastOutSlowInEasing), initialScale = 0.7f)
            ) {
                Text(
                    text = "vibe.",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonLilac,
                    letterSpacing = (-2).sp
                )
            }

            AnimatedVisibility(
                visible = subVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "experience the moment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = GoldAccent.copy(alpha = 0.9f),
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        // Version tag at bottom
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)) {
            AnimatedVisibility(visible = subVisible, enter = fadeIn(tween(800))) {
                Text("Uganda's premier event platform", fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.3f), letterSpacing = 0.5.sp)
            }
        }
=======
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(id = R.string.app_name).uppercase(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
    }
}
