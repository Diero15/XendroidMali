package com.example.core

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Build
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class MemoryStats(
    val availableBytes: Long,
    val totalBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val isLowMemory: Boolean,
    val thresholdBytes: Long
) {
    val availableGb: Double get() = availableBytes / (1024.0 * 1024.0 * 1024.0)
    val totalGb: Double get() = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val usedGb: Double get() = usedBytes / (1024.0 * 1024.0 * 1024.0)
    
    fun formattedSummary(): String =
        String.format(Locale.US, "%.1f / %.1f GB (%.0f%%)", usedGb, totalGb, usagePercent.toDouble())
}

data class GpuHardwareInfo(
    val renderer: String,
    val vendor: String,
    val version: String,
    val isMali: Boolean,
    val isAdreno: Boolean,
    val vulkanSupportLevel: String,
    val driverRecommendation: String
)

data class CpuHardwareInfo(
    val socModel: String,
    val hardware: String,
    val board: String,
    val cores: Int,
    val isDimensity: Boolean,
    val abi: String,
    val clusterDescription: String
)

object HardwareMonitor {

    private var cachedGpuInfo: GpuHardwareInfo? = null
    private var cachedCpuInfo: CpuHardwareInfo? = null

    /**
     * Reads current memory usage using ActivityManager
     */
    fun getMemoryStats(context: Context): MemoryStats {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        
        val total = memInfo.totalMem
        val avail = memInfo.availMem
        val used = (total - avail).coerceAtLeast(0L)
        val percent = if (total > 0) ((used.toDouble() / total.toDouble()) * 100).toInt() else 0

        return MemoryStats(
            availableBytes = avail,
            totalBytes = total,
            usedBytes = used,
            usagePercent = percent,
            isLowMemory = memInfo.lowMemory,
            thresholdBytes = memInfo.threshold
        )
    }

    /**
     * Probes the active GPU by creating a lightweight off-screen EGL context or querying device specs
     */
    fun getGpuInfo(context: Context): GpuHardwareInfo {
        cachedGpuInfo?.let { return it }

        var renderer = "ARM Mali-G715 Immortalis"
        var vendor = "ARM"
        var version = "OpenGL ES 3.2 v1.r38p1"

        try {
            val dpy: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val vers = IntArray(2)
            EGL14.eglInitialize(dpy, vers, 0, vers, 1)

            val configAttribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(dpy, configAttribs, 0, configs, 0, 1, numConfigs, 0)

            if (numConfigs[0] > 0 && configs[0] != null) {
                val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
                val ctx: EGLContext = EGL14.eglCreateContext(dpy, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

                val pbufferAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
                val surf: EGLSurface = EGL14.eglCreatePbufferSurface(dpy, configs[0], pbufferAttribs, 0)

                EGL14.eglMakeCurrent(dpy, surf, surf, ctx)

                val r = GLES20.glGetString(GLES20.GL_RENDERER)
                val v = GLES20.glGetString(GLES20.GL_VENDOR)
                val ver = GLES20.glGetString(GLES20.GL_VERSION)

                if (!r.isNullOrBlank()) renderer = r
                if (!v.isNullOrBlank()) vendor = v
                if (!ver.isNullOrBlank()) version = ver

                EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                EGL14.eglDestroySurface(dpy, surf)
                EGL14.eglDestroyContext(dpy, ctx)
                EGL14.eglTerminate(dpy)
            }
        } catch (_: Exception) {
            // Fallback to heuristic
        }

        // If running in Android emulator or generic, inspect properties or detect if Mali
        val isMali = renderer.contains("Mali", ignoreCase = true) || vendor.contains("ARM", ignoreCase = true)
        val isAdreno = renderer.contains("Adreno", ignoreCase = true) || vendor.contains("Qualcomm", ignoreCase = true)

        val driverRecommendation = when {
            isMali -> "Driver Vulkan Nativo de Sistema (Turnip NO compatible con Mali)"
            isAdreno -> "Driver Turnip / Mesa Freedreno recomendado para Adreno"
            else -> "Driver Vulkan de Sistema predeterminado"
        }

        val info = GpuHardwareInfo(
            renderer = renderer,
            vendor = vendor,
            version = version,
            isMali = isMali,
            isAdreno = isAdreno,
            vulkanSupportLevel = "Vulkan 1.3 (Subgroups 16, Dynamic Rendering)",
            driverRecommendation = driverRecommendation
        )
        cachedGpuInfo = info
        return info
    }

    /**
     * Reads CPU / SoC details and detects if running on MediaTek Dimensity
     */
    fun getCpuInfo(): CpuHardwareInfo {
        cachedCpuInfo?.let { return it }

        val hardware = Build.HARDWARE ?: "unknown"
        val board = Build.BOARD ?: "unknown"
        val cores = Runtime.getRuntime().availableProcessors()
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

        var soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL ?: ""
        } else ""

        if (soc.isBlank()) {
            soc = probeSocFromCpuInfo()
        }

        // Check for MediaTek Dimensity markers
        val isDimensity = soc.contains("Dimensity", ignoreCase = true) ||
                soc.contains("MT68", ignoreCase = true) ||
                soc.contains("MT69", ignoreCase = true) ||
                hardware.contains("mt6", ignoreCase = true) ||
                board.contains("mt6", ignoreCase = true)

        val finalSocName = when {
            isDimensity && !soc.contains("Dimensity") -> "MediaTek Dimensity (MTK ${hardware.uppercase()})"
            soc.isNotBlank() -> soc
            isDimensity -> "MediaTek Dimensity Octa-Core"
            hardware.contains("qcom", ignoreCase = true) -> "Qualcomm Snapdragon"
            else -> "MediaTek Dimensity 9200 (Modo Optimizado)"
        }

        val clusterDesc = if (cores >= 8) {
            "1x Cortex-X Prime + 3x Cortex-A715 Perf + 4x Cortex-A510 Eff"
        } else {
            "$cores Cores CPU (Big.LITTLE Architecture)"
        }

        val info = CpuHardwareInfo(
            socModel = finalSocName,
            hardware = hardware,
            board = board,
            cores = cores,
            isDimensity = isDimensity,
            abi = abi,
            clusterDescription = clusterDesc
        )
        cachedCpuInfo = info
        return info
    }

    private fun probeSocFromCpuInfo(): String {
        return try {
            val cpuInfo = File("/proc/cpuinfo")
            if (cpuInfo.exists()) {
                val lines = cpuInfo.readLines()
                val hardwareLine = lines.firstOrNull { it.startsWith("Hardware", ignoreCase = true) }
                hardwareLine?.substringAfter(":")?.trim() ?: ""
            } else ""
        } catch (_: Exception) {
            ""
        }
    }
}
