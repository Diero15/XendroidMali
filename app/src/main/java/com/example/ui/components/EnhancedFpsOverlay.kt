package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.CpuHardwareInfo
import com.example.core.DimensityMaliOptimizer
import com.example.core.GpuHardwareInfo
import com.example.core.HardwareMonitor
import com.example.core.MemoryStats
import com.example.ui.theme.DimensityCyan
import com.example.ui.theme.FpsGold
import com.example.ui.theme.MaliPurple
import com.example.ui.theme.RamSkyBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import com.example.ui.theme.XenGreen
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EnhancedFpsOverlay(
    visible: Boolean = true,
    currentFps: Double = 60.0,
    currentFrameMs: Double = 16.6,
    modifier: Modifier = Modifier,
    pollHz: Int = 4,
    onModeChange: ((Boolean) -> Unit)? = null
) {
    if (!visible) return

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("xendroid_fps_overlay", Context.MODE_PRIVATE) }

    var offset by remember {
        mutableStateOf(Offset(prefs.getFloat("x", 20f), prefs.getFloat("y", 60f)))
    }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    // Expanded / Detailed mode toggle
    var isExpanded by remember { mutableStateOf(prefs.getBoolean("expanded", true)) }

    // Live Telemetry states
    var memStats by remember { mutableStateOf(HardwareMonitor.getMemoryStats(context)) }
    var gpuInfo by remember { mutableStateOf(HardwareMonitor.getGpuInfo(context)) }
    var cpuInfo by remember { mutableStateOf(HardwareMonitor.getCpuInfo()) }

    var displayFps by remember { mutableDoubleStateOf(currentFps) }
    var displayMs by remember { mutableDoubleStateOf(currentFrameMs) }

    // Rolling frametime samples for mini-chart
    val frameHistory = remember { mutableStateListOf<Float>() }

    LaunchedEffect(pollHz) {
        val periodMs = 1000L / pollHz.coerceIn(1, 30)
        while (true) {
            memStats = HardwareMonitor.getMemoryStats(context)
            displayFps = currentFps
            displayMs = currentFrameMs

            // Update mini chart
            if (frameHistory.size > 20) {
                frameHistory.removeAt(0)
            }
            frameHistory.add(displayMs.toFloat().coerceIn(0f, 50f))

            delay(periodMs)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE090D16),
                            Color(0xFA0F172A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            if (gpuInfo.isMali) MaliPurple.copy(alpha = 0.8f) else XenGreen.copy(alpha = 0.8f),
                            DimensityCyan.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .pointerInput(boxSize) {
                    detectDragGestures(
                        onDrag = { change, drag ->
                            change.consume()
                            val maxX = maxOf(0f, boxSize.width.toFloat() - size.width)
                            val maxY = maxOf(0f, boxSize.height.toFloat() - size.height)
                            offset = Offset(
                                (offset.x + drag.x).coerceIn(0f, maxX),
                                (offset.y + drag.y).coerceIn(0f, maxY)
                            )
                        },
                        onDragEnd = {
                            prefs.edit()
                                .putFloat("x", offset.x)
                                .putFloat("y", offset.y)
                                .apply()
                        }
                    )
                }
                .clickable {
                    isExpanded = !isExpanded
                    prefs.edit().putBoolean("expanded", isExpanded).apply()
                    onModeChange?.invoke(isExpanded)
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .animateContentSize()
                .testTag("enhanced_fps_overlay")
        ) {
            if (!isExpanded) {
                // Compact Mode: 1 clean line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // FPS Indicator
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (displayFps >= 50.0) XenGreen else if (displayFps >= 28.0) FpsGold else WarningRed)
                    )

                    Text(
                        text = String.format(Locale.US, "FPS %.0f · %.1fms", displayFps, displayMs),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "•",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    // RAM stat
                    Text(
                        text = String.format(Locale.US, "RAM %.1fG disp", memStats.availableGb),
                        color = RamSkyBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "•",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )

                    // GPU
                    Text(
                        text = if (gpuInfo.isMali) "Mali GPU" else "Adreno GPU",
                        color = if (gpuInfo.isMali) MaliPurple else DimensityCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand HUD",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                // Detailed HUD Mode: Full Hardware & Telemetry readout
                Column(
                    modifier = Modifier.width(280.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Header Bar with Drag & Expand affordance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(XenGreen)
                            )
                            Text(
                                text = "XENDROID HARDWARE HUD",
                                color = XenGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = "Collapse HUD",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // FPS & FrameTime Main Readout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", displayFps),
                                color = if (displayFps >= 50.0) XenGreen else if (displayFps >= 28.0) FpsGold else WarningRed,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = " FPS",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Text(
                            text = String.format(Locale.US, "%.1f ms / frame", displayMs),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Frametime stability sparkline
                    if (frameHistory.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 2.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            frameHistory.forEach { ms ->
                                val barHeightRatio = (ms / 33.3f).coerceIn(0.1f, 1.0f)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height((10 * barHeightRatio).dp)
                                        .background(
                                            if (ms <= 17f) XenGreen
                                            else if (ms <= 34f) FpsGold
                                            else WarningRed,
                                            RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }
                    }

                    // RAM Readout with Visual Progress Bar
                    Column(modifier = Modifier.padding(top = 2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "RAM DISPONIBLE:",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%.2f GB Libre / %.1f GB Total",
                                    memStats.availableGb,
                                    memStats.totalGb
                                ),
                                color = RamSkyBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { (memStats.usagePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (memStats.usagePercent > 85) WarningRed else RamSkyBlue,
                            trackColor = Color(0xFF1E293B)
                        )
                    }

                    // GPU Readout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF131C2E), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GPU ACTIVA:",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (gpuInfo.isMali) "MALI TBDR" else "QUALCOMM",
                                color = if (gpuInfo.isMali) MaliPurple else DimensityCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .background(
                                        if (gpuInfo.isMali) MaliPurple.copy(alpha = 0.2f) else DimensityCyan.copy(alpha = 0.2f),
                                        RoundedCornerShape(2.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = gpuInfo.renderer,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = gpuInfo.vulkanSupportLevel,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // CPU / Dimensity SoC Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C1929), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROCESADOR (CPU):",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "DIMENSITY BOOST ON",
                                color = DimensityCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = cpuInfo.socModel,
                            color = DimensityCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${cpuInfo.cores} Cores · Cortex-X Pinning Activo",
                            color = Color.LightGray.copy(alpha = 0.7f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Footer Tip
                    Text(
                        text = "Toca para minimizar · Arrastra para reubicar",
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
