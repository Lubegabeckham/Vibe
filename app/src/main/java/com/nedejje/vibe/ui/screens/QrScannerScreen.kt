package com.nedejje.vibe.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.TicketEntity
import com.nedejje.vibe.viewmodel.TicketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val scope = rememberCoroutineScope()

    val viewModel: TicketViewModel = viewModel(
        factory = TicketViewModel.Factory(
            app.container.ticketRepository,
            app.container.eventRepository,
            app.container.guestRepository
        )
    )

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var scannedTicket by remember { mutableStateOf<TicketEntity?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Ticket") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (hasCameraPermission) {
                CameraPreview(
                    onBarcodeScanned = { barcode ->
                        if (!isVerifying && scannedTicket == null && errorMessage == null) {
                            scope.launch {
                                isVerifying = true
                                // QR format: "ticketId|eventId|tier" — extract just the ticketId
                                val ticketId = barcode.split("|").firstOrNull()?.trim() ?: barcode
                                val ticket = app.container.ticketRepository.getById(ticketId)
                                if (ticket != null) {
                                    if (ticket.isUsed) {
                                        errorMessage = "This ticket has already been used!"
                                    } else {
                                        scannedTicket = ticket
                                    }
                                } else {
                                    errorMessage = "Invalid Ticket QR Code"
                                }
                                isVerifying = false
                            }
                        }
                    }
                )

                // Scanner Overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(2.dp)
                    )
                }

                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("Camera access is required to scan tickets", textAlign = TextAlign.Center)
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Enable Camera")
                    }
                }
            }

            // Verification Dialog
            AnimatedVisibility(
                visible = scannedTicket != null || errorMessage != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (errorMessage != null) {
                            Icon(Icons.Default.Error, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                            Text("Invalid Ticket", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = { errorMessage = null },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Try Again") }
                        } else if (scannedTicket != null) {
                            val isPending = scannedTicket!!.status == "PENDING"

                            Icon(
                                imageVector = if (isPending) Icons.Default.Warning else Icons.Default.CheckCircle,
                                null, Modifier.size(64.dp),
                                tint = if (isPending) Color(0xFFFFA000) else Color(0xFF4CAF50)
                            )

                            Text(
                                text = if (isPending) "Payment Pending" else "Ticket Verified",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("TIER: ${scannedTicket!!.tier.uppercase()}", fontWeight = FontWeight.ExtraBold)
                                    Text("QTY: ${scannedTicket!!.quantity}")
                                    Text("STATUS: ${scannedTicket!!.status}", color = if (isPending) Color.Red else Color.Unspecified, fontWeight = if (isPending) FontWeight.Bold else FontWeight.Normal)
                                    Text("ID: #${scannedTicket!!.id.take(8).uppercase()}")
                                }
                            }

                            if (isPending) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            app.container.ticketRepository.updateStatus(scannedTicket!!.id, "PAID")
                                            app.container.ticketRepository.markAsUsed(scannedTicket!!.id)
                                            Toast.makeText(context, "Payment Confirmed & Checked In", Toast.LENGTH_SHORT).show()
                                            scannedTicket = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Confirm Payment & Check In", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            app.container.ticketRepository.markAsUsed(scannedTicket!!.id)
                                            Toast.makeText(context, "Checked In Successfully", Toast.LENGTH_SHORT).show()
                                            scannedTicket = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Mark as Attended", fontWeight = FontWeight.Bold)
                                }
                            }

                            TextButton(onClick = { scannedTicket = null }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onBarcodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val executor = Executors.newSingleThreadExecutor()
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }

                val scanner = BarcodeScanning.getClient()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { onBarcodeScanned(it) }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}