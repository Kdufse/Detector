package com.kdufse.detector.detector

import android.content.Context
import com.kdufse.detector.model.DetectionItem
import com.kdufse.detector.model.DetectionResult
import com.kdufse.detector.model.DetectionStatus
import com.kdufse.detector.model.RootStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RootDetector(private val context: Context) {
    
    private val bootloaderDetector = BootloaderDetector()
    private val mountDetector = MountDetector()
    private val processDetector = ProcessDetector()
    private val packageDetector = PackageDetector(context)
    
    suspend fun performDetection(): DetectionResult = withContext(Dispatchers.IO) {
        val detectionItems = mutableListOf<DetectionItem>()
        
        // 1. Bootloader检测
        val bootloaderResult = bootloaderDetector.checkBootloader()
        detectionItems.add(bootloaderResult)
        
        // 2. 挂载点检测
        val mountResult = mountDetector.checkMounts()
        detectionItems.add(mountResult)
        
        // 3. 进程检测
        val processResult = processDetector.checkProcesses()
        detectionItems.add(processResult)
        
        // 4. 包名检测
        val packageResult = packageDetector.checkPackages()
        detectionItems.add(packageResult)
        
        // 5. 额外检测项
        detectionItems.addAll(performAdditionalChecks())
        
        // 计算总体状态
        val overallStatus = calculateOverallStatus(detectionItems)
        
        return@withContext DetectionResult(
            items = detectionItems,
            overallStatus = overallStatus
        )
    }
    
    private fun performAdditionalChecks(): List<DetectionItem> {
        val additionalItems = mutableListOf<DetectionItem>()
        
        // 检测su二进制文件
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        val suExists = suPaths.any { path ->
            java.io.File(path).exists()
        }
        
        additionalItems.add(
            DetectionItem(
                id = "su_binary",
                title = "SU二进制文件检测",
                description = "检测系统中是否存在su二进制文件",
                status = if (suExists) DetectionStatus.DANGER else DetectionStatus.SAFE,
                details = if (suExists) "发现su二进制文件" else "未发现su二进制文件",
                iconRes = android.R.drawable.ic_dialog_alert
            )
        )
        
        // 检测ro.debuggable属性
        val isDebuggable = try {
            val process = Runtime.getRuntime().exec("getprop ro.debuggable")
            val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            val value = reader.readLine()
            reader.close()
            value == "1"
        } catch (e: Exception) {
            false
        }
        
        additionalItems.add(
            DetectionItem(
                id = "debuggable",
                title = "系统调试模式检测",
                description = "检测系统是否处于调试模式",
                status = if (isDebuggable) DetectionStatus.WARNING else DetectionStatus.SAFE,
                details = if (isDebuggable) "系统处于调试模式" else "系统未处于调试模式",
                iconRes = android.R.drawable.ic_menu_manage
            )
        )
        
        return additionalItems
    }
    
    private fun calculateOverallStatus(items: List<DetectionItem>): RootStatus {
        val dangerCount = items.count { it.status == DetectionStatus.DANGER }
        val warningCount = items.count { it.status == DetectionStatus.WARNING }
        
        return when {
            dangerCount > 0 -> RootStatus.ROOTED
            warningCount > 0 -> RootStatus.SUSPICIOUS
            items.all { it.status == DetectionStatus.SAFE } -> RootStatus.NOT_ROOTED
            else -> RootStatus.UNKNOWN
        }
    }
}