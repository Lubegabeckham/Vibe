package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.BudgetItemEntity
import com.nedejje.vibe.db.TierBreakdown
import com.nedejje.vibe.viewmodel.WrapReportViewModel
import java.util.Locale
import kotlin.math.min

// ── Chart palette ─────────────────────────────────────────────────────────────
private val chartColors = listOf(
    Color(0xFF7C3AED),
    Color(0xFFE8B84B),
    Color(0xFF00D4AA),
    Color(0xFF9D5CF5),
    Color(0xFFEF4444),
    Color(0xFF3B82F6),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrapReportScreen(navController: NavController, eventId: String?) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: WrapReportViewModel = viewModel(
        factory = WrapReportViewModel.Factory(
            app.container.eventRepository,
            app.container.guestRepository,
            app.container.budgetRepository,
            app.container.ticketRepository
        )
    )

    LaunchedEffect(eventId) { eventId?.let { viewModel.setEventId(it) } }

    val event          by viewModel.event.collectAsStateWithLifecycle()
    val guestCount     by viewModel.guestCount.collectAsStateWithLifecycle()
    val checkedInCount by viewModel.checkedInCount.collectAsStateWithLifecycle()
    val totalSpend     by viewModel.totalSpend.collectAsStateWithLifecycle()
    val ticketRevenue  by viewModel.ticketRevenue.collectAsStateWithLifecycle()
    val budgetItems    by viewModel.budgetItems.collectAsStateWithLifecycle()
    val tierBreakdown  by viewModel.tierBreakdown.collectAsStateWithLifecycle()

    val profitOrLoss = ticketRevenue - totalSpend

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wrap_report_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacer_medium))
        ) {
            event?.let {
                Text(it.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }

            // ── Financial summary ────────────────────────────────────────────
            Text(stringResource(R.string.financial_summary), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                colors = CardDefaults.cardColors(
                    containerColor = if (profitOrLoss >= 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
                    Text(if (profitOrLoss >= 0) stringResource(R.string.net_profit) else stringResource(R.string.net_loss), style = MaterialTheme.typography.labelMedium)
                    Text("${stringResource(R.string.ugx_currency)} ${formatAmount(profitOrLoss.toDouble())}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                StatCard(stringResource(R.string.revenue_label), "${stringResource(R.string.ugx_currency)} ${formatAmount(ticketRevenue.toDouble())}", Modifier.weight(1f))
                StatCard(stringResource(R.string.expenses_label), "${stringResource(R.string.ugx_currency)} ${formatAmount(totalSpend)}", Modifier.weight(1f))
            }

            // ── Ticket tier bar chart ────────────────────────────────────────
            if (tierBreakdown.isNotEmpty()) {
                Text("Ticket Sales by Tier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TierBarChart(tierBreakdown)
                        Spacer(Modifier.height(12.dp))
                        tierBreakdown.forEachIndexed { i, t ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                                Canvas(Modifier.size(10.dp)) { drawCircle(chartColors[i % chartColors.size]) }
                                Text(
                                    "${t.tier}  ·  ${t.count} ticket${if (t.count != 1) "s" else ""}  ·  ${stringResource(R.string.ugx_currency)} ${formatAmount(t.revenue.toDouble())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Attendance ───────────────────────────────────────────────────
            Text(stringResource(R.string.attendance_summary), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ReportStat(stringResource(R.string.total_guests), "$guestCount")
            ReportStat(stringResource(R.string.checked_in), "$checkedInCount")
            ReportStat(stringResource(R.string.show_up_rate), "${if (guestCount > 0) (checkedInCount * 100 / guestCount) else 0}%")

            // ── Budget pie chart + breakdown ─────────────────────────────────
            Text(stringResource(R.string.expense_breakdown), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (budgetItems.isEmpty()) {
                Text(stringResource(R.string.no_expenses_recorded), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BudgetPieChart(budgetItems)
                        Spacer(Modifier.height(12.dp))
                        budgetItems.forEachIndexed { i, item ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                                Canvas(Modifier.size(10.dp)) { drawCircle(chartColors[i % chartColors.size]) }
                                Text(item.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${stringResource(R.string.ugx_currency)} ${formatAmount(item.amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                budgetItems.forEach { item ->
                    VendorItem(item.name, "${stringResource(R.string.ugx_currency)} ${formatAmount(item.amount)}", stringResource(R.string.paid_status))
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.spacer_large)))
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
            ) {
                Text(stringResource(R.string.close_report), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ── Bar chart: ticket count per tier ─────────────────────────────────────────
@Composable
private fun TierBarChart(tiers: List<TierBreakdown>) {
    val density = LocalDensity.current
    val labelPx = with(density) { 11.sp.toPx() }
    val maxCount = tiers.maxOf { it.count }.coerceAtLeast(1)

    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val barAreaH  = size.height - 32.dp.toPx()
        val slotW     = size.width / tiers.size
        val barW      = slotW * 0.55f

        tiers.forEachIndexed { i, tier ->
            val barH   = (tier.count.toFloat() / maxCount) * barAreaH
            val left   = slotW * i + (slotW - barW) / 2f
            val top    = barAreaH - barH
            val color  = chartColors[i % chartColors.size]

            drawRoundRect(
                color       = color,
                topLeft     = Offset(left, top),
                size        = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textAlign      = android.graphics.Paint.Align.CENTER
                    textSize       = labelPx
                    isFakeBoldText = true
                    this.color     = android.graphics.Color.WHITE
                }
                drawText("${tier.count}", left + barW / 2f, (top - 4.dp.toPx()).coerceAtLeast(labelPx), paint)
                paint.isFakeBoldText = false
                paint.color = android.graphics.Color.LTGRAY
                drawText(tier.tier.take(7), left + barW / 2f, size.height - 4.dp.toPx(), paint)
            }
        }
    }
}

// ── Donut pie chart: budget proportions ──────────────────────────────────────
@Composable
private fun BudgetPieChart(items: List<BudgetItemEntity>) {
    val total = items.sumOf { it.amount }.coerceAtLeast(0.01)

    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val diameter = min(size.width, size.height) * 0.85f
        val topLeft  = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize  = Size(diameter, diameter)
        var startAngle = -90f

        items.forEachIndexed { i, item ->
            val sweep = ((item.amount / total) * 360f).toFloat()
            drawArc(color = chartColors[i % chartColors.size], startAngle = startAngle, sweepAngle = sweep - 1.5f, useCenter = true, topLeft = topLeft, size = arcSize)
            startAngle += sweep
        }

        // Donut hole
        val holeR = diameter * 0.22f
        drawCircle(color = Color(0xFF12102A), radius = holeR, center = Offset(size.width / 2f, size.height / 2f))
    }
}

// ── Shared composables (kept identical to original) ───────────────────────────
@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ReportStat(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_extra_small)), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VendorItem(name: String, amount: String, status: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_small)), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
        Text(text = amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

private fun formatAmount(amount: Double): String = String.format(Locale.getDefault(), "%,.0f", amount)