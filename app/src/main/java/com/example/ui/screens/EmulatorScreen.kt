package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.DimensityMaliOptimizer
import com.example.model.SampleGameLibrary
import com.example.model.XboxGame
import com.example.ui.components.EnhancedFpsOverlay
import com.example.ui.theme.DimensityCyan
import com.example.ui.theme.FpsGold
import com.example.ui.theme.MaliPurple
import com.example.ui.theme.RamSkyBlue
import com.example.ui.theme.WarningRed
import com.example.ui.theme.XenGreen
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorScreen(
    selectedGame: XboxGame? = null,
    onNavigateToDimensitySettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val activeGame = selectedGame ?: SampleGameLibrary.games.first()
    val optimizationProfile by DimensityMaliOptimizer.currentProfile.collectAsState()

    var isRunning by remember { mutableStateOf(true) }
    var showQuickMenu by remember { mutableStateOf(false) }

    // Simulation metrics influenced by optimization settings
    var liveFps by remember { mutableDoubleStateOf(58.8) }
    var liveFrameMs by remember { mutableDoubleStateOf(17.0) }
    var renderAngle by remember { mutableFloatStateOf(0f) }

    // Dynamic FPS simulation responding to Dimensity & Mali optimizer settings
    LaunchedEffect(isRunning, optimizationProfile) {
        while (isRunning) {
            val (targetFps, targetMs) = DimensityMaliOptimizer.calculateEstimatedGains(
                baseFps = if (activeGame.has60FpsPatch) 32.0 else 24.0
            )

            // Add slight jitter for realistic simulation
            val jitter = (Math.random() - 0.5) * 1.8
            liveFps = (targetFps + jitter).coerceIn(12.0, 60.0)
            liveFrameMs = (1000.0 / liveFps).coerceIn(16.0, 75.0)

            renderAngle = (renderAngle + 3f) % 360f
            delay(16) // ~60 Hz loop
        }
    }

    fun vibrateShort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B12))
            .testTag("emulator_screen")
    ) {
        // --- 1. GAME VIEWPORT CANVAS ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            // Emulated Xbox 360 3D Rendering Frame
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Draw background game scene
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(activeGame.coverAccentHex).copy(alpha = 0.45f),
                            Color(0xFF0F172A),
                            Color(0xFF050811)
                        ),
                        center = Offset(cx, cy),
                        radius = size.width.coerceAtLeast(size.height)
                    )
                )

                // 3D Wireframe ring simulation (representing game geometry being rendered)
                val rad = 140f
                val radAngle = Math.toRadians(renderAngle.toDouble())
                val px = cx + (rad * cos(radAngle)).toFloat()
                val py = cy + (rad * sin(radAngle)).toFloat()

                // Render grid lines
                for (i in -4..4) {
                    val yLine = cy + i * 40f
                    drawLine(
                        color = Color(0x2210B981),
                        start = Offset(0f, yLine),
                        end = Offset(size.width, yLine),
                        strokeWidth = 1f
                    )
                }

                // Rotating 3D Emblem / Xbox Ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(XenGreen, DimensityCyan, MaliPurple, XenGreen),
                        center = Offset(cx, cy)
                    ),
                    radius = 90f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 6f)
                )

                // Shimmering particle
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(px, py)
                )
            }

            // Game Title & Status Banner
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeGame.titleName.uppercase(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "TitleID: ${activeGame.titleId} • Vulkan Subpass TBDR Activo",
                    color = DimensityCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick Floating Toolbar (Top Right)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        vibrateShort()
                        showQuickMenu = true
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xCC1E293B), CircleShape)
                        .testTag("emulator_quick_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Quick Settings",
                        tint = DimensityCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        vibrateShort()
                        isRunning = !isRunning
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xCC1E293B), CircleShape)
                        .testTag("emulator_pause_resume_btn")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pausar" else "Reanudar",
                        tint = XenGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- 2. THE REQUESTED ENHANCED FPS & HARDWARE OVERLAY ---
        EnhancedFpsOverlay(
            visible = true,
            currentFps = liveFps,
            currentFrameMs = liveFrameMs,
            modifier = Modifier.fillMaxSize()
        )

        // --- 3. VIRTUAL XBOX 360 CONTROLLER ON-SCREEN OVERLAY ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Side: D-Pad & Left Thumbstick
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // D-Pad Cross
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Up
                        VirtualButton(
                            text = "▲",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                        // Down
                        VirtualButton(
                            text = "▼",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                        // Left
                        VirtualButton(
                            text = "◀",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                        // Right
                        VirtualButton(
                            text = "▶",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                    }

                    // Left Stick (LS)
                    VirtualThumbstick(label = "LS", onMove = { vibrateShort() })
                }

                // Center: Guide & Menu buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VirtualButton(text = "BACK", modifier = Modifier.size(width = 46.dp, height = 28.dp)) {
                        vibrateShort()
                    }
                    // Xbox Guide Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF22C55E), Color(0xFF15803D))))
                            .clickable { vibrateShort() }
                            .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    VirtualButton(text = "START", modifier = Modifier.size(width = 46.dp, height = 28.dp)) {
                        vibrateShort()
                    }
                }

                // Right Side: Action Buttons (A, B, X, Y) & Right Thumbstick
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ABXY Diamond
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Y (Yellow)
                        VirtualButton(
                            text = "Y",
                            color = Color(0xFFFBBF24),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                        // A (Green)
                        VirtualButton(
                            text = "A",
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                        // X (Blue)
                        VirtualButton(
                            text = "X",
                            color = Color(0xFF38BDF8),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                        // B (Red)
                        VirtualButton(
                            text = "B",
                            color = Color(0xFFEF4444),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(34.dp),
                            onClick = { vibrateShort() }
                        )
                    }

                    // Right Stick (RS)
                    VirtualThumbstick(label = "RS", onMove = { vibrateShort() })
                }
            }
        }

        // --- 4. QUICK SETTINGS MODAL BOTTOM SHEET ---
        if (showQuickMenu) {
            ModalBottomSheet(
                onDismissRequest = { showQuickMenu = false },
                containerColor = Color(0xFF0F172A),
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ajustes Rápidos Dimensity & Mali",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f FPS", liveFps),
                            color = XenGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Toggle Mali TBDR
                    QuickSettingSwitch(
                        title = "Optimización Mali TBDR (FBO)",
                        subtitle = "Evita pasadas redundantes a la memoria RAM",
                        checked = optimizationProfile.enableMaliTbdr,
                        onCheckedChange = { v ->
                            DimensityMaliOptimizer.updateProfile { it.copy(enableMaliTbdr = v) }
                        }
                    )

                    // Toggle FP16 Precision
                    QuickSettingSwitch(
                        title = "Precisión FP16 (Sombreadores Mali)",
                        subtitle = "Acelera las ALUs en Mali G77/G78/G710/G715",
                        checked = optimizationProfile.enableFp16Precision,
                        onCheckedChange = { v ->
                            DimensityMaliOptimizer.updateProfile { it.copy(enableFp16Precision = v) }
                        }
                    )

                    // Toggle Dimensity Core Pinning
                    QuickSettingSwitch(
                        title = "CPU Core Pinning (Dimensity Cortex-X)",
                        subtitle = "Prioriza núcleos de alto rendimiento",
                        checked = optimizationProfile.dimensityCorePinning,
                        onCheckedChange = { v ->
                            DimensityMaliOptimizer.updateProfile { it.copy(dimensityCorePinning = v) }
                        }
                    )

                    // Resolution scale selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Escala de Resolución:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.5f, 0.75f, 1.0f).forEach { scale ->
                                FilterChip(
                                    selected = optimizationProfile.resolutionScale == scale,
                                    onClick = {
                                        DimensityMaliOptimizer.updateProfile { it.copy(resolutionScale = scale) }
                                    },
                                    label = { Text("${(scale * 100).toInt()}%") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DimensityCyan,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            showQuickMenu = false
                            onNavigateToDimensitySettings()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = DimensityCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Suite Completa de Investigación Dimensity/Mali", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0x771E293B))
            .border(1.5.dp, color.copy(alpha = 0.8f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun VirtualThumbstick(
    label: String,
    onMove: () -> Unit
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color(0x551E293B))
            .border(1.5.dp, Color(0x6694A3B8), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))))
                .border(1.dp, Color(0xFF94A3B8), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, drag ->
                            change.consume()
                            val newX = (knobOffset.x + drag.x).coerceIn(-20f, 20f)
                            val newY = (knobOffset.y + drag.y).coerceIn(-20f, 20f)
                            knobOffset = Offset(newX, newY)
                            onMove()
                        },
                        onDragEnd = {
                            knobOffset = Offset.Zero
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickSettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = XenGreen,
                checkedTrackColor = Color(0xFF064E3B)
            )
        )
    }
}
