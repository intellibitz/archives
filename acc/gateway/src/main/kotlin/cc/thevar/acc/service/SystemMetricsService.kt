package cc.thevar.acc.service

import cc.thevar.acc.protocol.*
import oshi.SystemInfo
import java.io.File

class SystemMetricsService(private val projectRoot: File) {
    private val si = SystemInfo()
    private val hardware = si.hardware
    private val cpu = hardware.processor
    private val memory = hardware.memory
    
    private var prevTicks = cpu.systemCpuLoadTicks
    private var cachedDiskUsage = "0B"
    private var lastDiskCheck = 0L
    private val diskCheckInterval = 60_000L // 1 minute

    fun getSystemStats(): SystemStats {
        val cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0
        prevTicks = cpu.systemCpuLoadTicks
        
        val now = System.currentTimeMillis()
        if (now - lastDiskCheck > diskCheckInterval) {
            cachedDiskUsage = getFleetDiskUsage()
            lastDiskCheck = now
        }
        
        return SystemStats(
            cpuUtilization = cpuLoad.toFloat().coerceIn(0f, 100f),
            ramUsed = ((memory.total - memory.available) / (1024.0 * 1024.0 * 1024.0)).toFloat(),
            ramTotal = (memory.total / (1024.0 * 1024.0 * 1024.0)).toFloat(),
            gpu = getGpuStats(),
            diskUsage = cachedDiskUsage
        )
    }

    private fun getGpuStats(): GpuStats {
        val gpus = hardware.graphicsCards
        if (gpus.isEmpty()) return GpuStats(0f, 0f, 0f, 0f, 0f, false)
        
        val primary = gpus.first()
        return GpuStats(
            utilization = 0f,
            memoryUsed = 0f,
            memoryTotal = (primary.vRam / (1024f * 1024f * 1024f)),
            temperature = 0f,
            power = 0f,
            active = true
        )
    }

    private fun getFleetDiskUsage(): String {
        val ollamaDir = if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
            File(System.getProperty("user.home"), "Library/Application Support/Ollama/models")
        } else {
            val sandboxDir = File(projectRoot, "data/ollama/models")
            if (sandboxDir.exists()) sandboxDir else File(System.getProperty("user.home"), ".ollama/models")
        }

        if (!ollamaDir.exists()) return "0B"
        
        val bytes = getDirectorySize(ollamaDir)
        return formatSize(bytes)
    }

    private fun getDirectorySize(dir: File): Long {
        if (!dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun formatSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return String.format("%.1f%s", size, units[unitIndex])
    }
}
