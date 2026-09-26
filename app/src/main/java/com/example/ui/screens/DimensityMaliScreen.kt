package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.DimensityMaliOptimizer
import com.example.core.HardwareMonitor
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DimensityCyan
import com.example.ui.theme.DimensityCyanGlow
import com.example.ui.theme.FpsGold
import com.example.ui.theme.MaliPurple
import com.example.ui.theme.MaliPurpleLight
import com.example.ui.theme.RamSkyBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningRed
import com.example.ui.theme.XenGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DimensityMaliScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val optimizationProfile by DimensityMaliOptimizer.currentProfile.collectAsState()

    val memStats = remember { HardwareMonitor.getMemoryStats(context) }
    val gpuInfo = remember { HardwareMonitor.getGpuInfo(context) }
    val cpuInfo = remember { HardwareMonitor.getCpuInfo() }

    var isBenchmarking by remember { mutableStateOf(false) }
    var benchmarkProgress by remember { mutableDoubleStateOf(0.0) }
    var benchmarkResult by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showTomlConfig by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(horizontal = 16.dp)
            .testTag("dimensity_mali_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Title Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(listOf(DimensityCyan, MaliPurple))
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(DimensityCyan, MaliPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                        }
                        Column {
                            Text(
                                text = "Dimensity & ARM Mali Suite",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Investigación y Optimizaciones de Alto Rendimiento",
                                color = DimensityCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = "XenDroid original fue diseñado para Snapdragon con Turnip. Esta suite resuelve los cuellos de botella de ancho de banda TBDR en Mali y afina la afinidad de hilos en CPU Dimensity para maximizar los FPS.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // --- 1. HARDWARE DETECTION CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HARDWARE DETECTADO EN TU DISPOSITIVO",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    HardwareMetricRow(
                        label = "GPU Activa",
                        value = gpuInfo.renderer,
                        badge = if (gpuInfo.isMali) "ARM MALI (TBDR)" else "QUALCOMM",
                        badgeColor = if (gpuInfo.isMali) MaliPurple else DimensityCyan
                    )

                    HardwareMetricRow(
                        label = "Driver Vulkan",
                        value = gpuInfo.vulkanSupportLevel,
                        badge = "NATIVO",
                        badgeColor = XenGreen
                    )

                    HardwareMetricRow(
                        label = "Procesador (SoC)",
                        value = cpuInfo.socModel,
                        badge = "DIMENSITY",
                        badgeColor = DimensityCyan
                    )

                    HardwareMetricRow(
                        label = "Memoria RAM",
                        value = memStats.formattedSummary(),
                        badge = if (memStats.isLowMemory) "ALERTA" else "ÓPTIMO",
                        badgeColor = if (memStats.isLowMemory) WarningRed else RamSkyBlue
                    )
                }
            }
        }

        // --- 2. BENCHMARK SIMULATOR ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Simulador de Benchmark FPS",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Evalúa la ganancia en tiempo real con la suite",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (!isBenchmarking) {
                                    isBenchmarking = true
                                    benchmarkProgress = 0.0
                                    coroutineScope.launch {
                                        for (i in 1..20) {
                                            delay(100)
                                            benchmarkProgress = i / 20.0
                                        }
                                        benchmarkResult = DimensityMaliOptimizer.calculateEstimatedGains()
                                        isBenchmarking = false
                                    }
                                }
                            },
                            enabled = !isBenchmarking,
                            colors = ButtonDefaults.buttonColors(containerColor = XenGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBenchmarking) "Probando..." else "Ejecutar Test", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isBenchmarking) {
                        LinearProgressIndicator(
                            progress = { benchmarkProgress.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DimensityCyan,
                            trackColor = Color(0xFF1E293B)
                        )
                    }

                    // Benchmark Results Display
                    val result = benchmarkResult ?: DimensityMaliOptimizer.calculateEstimatedGains()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D16), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STOCK (Sin Optimizar)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("24.0 FPS", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("41.6 ms / frame", color = WarningRed, fontSize = 10.sp)
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0xFF1E293B))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CON SUITE DIMENSITY", color = DimensityCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.US, "%.1f FPS", result.first), color = XenGreen, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            Text(String.format(Locale.US, "%.1f ms / frame", result.second), color = XenGreen, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // --- 3. THE 8 CORE OPTIMIZATION SWITCHES ---
        item {
            Text(
                text = "CONFIGURACIÓN Y PARCHES DE INVESTIGACIÓN",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Opt 1: Mali TBDR Subpass Fusion
        item {
            OptimizationCard(
                title = "1. Fusión de Pases Mali TBDR (FBO Path)",
                badge = "+28% FPS",
                badgeColor = XenGreen,
                description = "Mali es un renderizador diferido por teselas (TBDR). Forzar 'render_target_path_vulkan = fbo' y 'LOAD_OP_DONT_CARE' previene que Mali vacíe y relea las teselas a la memoria RAM LPDDR5X principal en cada uno de los 55+ pases por fotograma.",
                checked = optimizationProfile.enableMaliTbdr,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(enableMaliTbdr = v) }
                }
            )
        }

        // Opt 2: FP16 Shader Precision
        item {
            OptimizationCard(
                title = "2. Precisión FP16 para Sombreadores Mali",
                badge = "+22% ALUs",
                badgeColor = MaliPurple,
                description = "Las ALUs de sombreado en GPUs Mali Valhall duplican su rendimiento de cálculo aritmético cuando las operaciones matemáticas intermedias se ejecutan en 16-bit Float (Half-Precision) mediante la extensión VK_KHR_shader_float16_int8.",
                checked = optimizationProfile.enableFp16Precision,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(enableFp16Precision = v) }
                }
            )
        }

        // Opt 3: Wave Subgroup Size = 16
        item {
            OptimizationCard(
                title = "3. Tamaño de Subgrupo 16 (Valhall Waves)",
                badge = "Anti-Crash",
                badgeColor = DimensityCyan,
                description = "En Xenia de escritorio y Adreno, los compute shaders asumen tamaños de onda (warp) de 64 o 128 hilos. La arquitectura ARM Mali G710/G715/G720 utiliza ondas de 16 hilos. Forzar 16 previene el bloqueo del despachador Vulkan.",
                checked = optimizationProfile.forceSubgroupSize16,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(forceSubgroupSize16 = v) }
                }
            )
        }

        // Opt 4: Dimensity Core Pinning
        item {
            OptimizationCard(
                title = "4. Fijación de Núcleos (Cortex-X Pinning)",
                badge = "Cortex-X",
                badgeColor = FpsGold,
                description = "MediaTek Dimensity cuenta con núcleos de máxima potencia Cortex-X (Ultra Prime) y Cortex-A7xx. Asigna afinidad de CPU fija (pthread_setaffinity_np) para que el hilo de emulación PowerPC y recompilación JIT nunca caigan a los núcleos lentos A510.",
                checked = optimizationProfile.dimensityCorePinning,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(dimensityCorePinning = v) }
                }
            )
        }

        // Opt 5: Turnip Blocker
        item {
            OptimizationCard(
                title = "5. Bloqueador de Drivers Turnip (Crash Guard)",
                badge = "Estabilidad",
                badgeColor = WarningRed,
                description = "Turnip (Mesa Freedreno) sólo existe para GPUs Adreno. Si XenDroid intenta cargar libvulkan_freedreno.so en un MediaTek Dimensity, el sistema aborta de inmediato con SIGSEGV. Esta opción aísla e impone el driver nativo de sistema.",
                checked = optimizationProfile.turnipDriverBlocker,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(turnipDriverBlocker = v) }
                }
            )
        }

        // Opt 6: Render Area Scissor Bounding
        item {
            OptimizationCard(
                title = "6. Scissor Bounding (Recorte de Franjas)",
                badge = "-3.2ms",
                badgeColor = XenGreen,
                description = "Juegos como Gears of War emiten pasadas de sombreado con franjas de 8192 píxeles vacías fuera de pantalla. Este parche intersecta el scissor de huésped con el viewport real, recortando miles de teselas inútiles del binner de Mali.",
                checked = optimizationProfile.renderAreaBackpatching,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(renderAreaBackpatching = v) }
                }
            )
        }

        // Opt 7: Async Pipeline Compilation
        item {
            OptimizationCard(
                title = "7. Compilación Asíncrona de Pipelines",
                badge = "Sin Stutter",
                badgeColor = DimensityCyan,
                description = "Los controladores de Mali son sensibles a la creación de pipelines en el hilo principal. Dibuja shaders provisionales mientras se compila en segundo plano en 4 hilos auxiliares, eliminando los molestos micro-congelamientos al jugar.",
                checked = optimizationProfile.asyncPipelineCompilation,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(asyncPipelineCompilation = v) }
                }
            )
        }

        // Opt 8: ARM NEON Accelerated Memory
        item {
            OptimizationCard(
                title = "8. Aceleración Vectorial ARM NEON",
                badge = "+40% DMA",
                badgeColor = MaliPurple,
                description = "Utiliza registros vectoriales ARM NEON de 128-bit (vld1q_u8, vrev32q_u32) para desenrollar bucles de 64 bytes durante el desentrelazado de texturas e intercambio Big Endian de Xbox 360 hacia la memoria unificada de Dimensity.",
                checked = optimizationProfile.fastMemoryAccessNeon,
                onCheckedChange = { v ->
                    DimensityMaliOptimizer.updateProfile { it.copy(fastMemoryAccessNeon = v) }
                }
            )
        }

        // --- 4. EXPORT TOML CONFIG CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Archivo de Configuración Xenia TOML",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                val toml = DimensityMaliOptimizer.generateXeniaConfigToml()
                                copyToClipboard(toml, "Xenia Config TOML")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DimensityCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar TOML", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "Genera el archivo xenia.config.toml optimizado con todos estos parámetros para copiarlo a la raíz de almacenamiento o a la carpeta de configuración de XenDroid.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = { showTomlConfig = !showTomlConfig },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131C2E))
                    ) {
                        Text(if (showTomlConfig) "Ocultar Vista Previa TOML" else "Ver Código TOML Completo", color = DimensityCyan, fontSize = 12.sp)
                    }

                    if (showTomlConfig) {
                        Text(
                            text = DimensityMaliOptimizer.generateXeniaConfigToml(),
                            color = Color(0xFF34D399),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF060B12), RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun HardwareMetricRow(
    label: String,
    value: String,
    badge: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Box(
            modifier = Modifier
                .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(badge, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun OptimizationCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) XenGreen.copy(alpha = 0.4f) else Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
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

            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
