package com.kdufse.detector.detector

import com.kdufse.detector.model.DetectionItem
import com.kdufse.detector.model.DetectionStatus
import java.io.File

class BootloaderDetector {
    
    fun checkBootloader(): DetectionItem {
        return try {
            // 方法1: 检查/proc/cmdline中的bootloader状态
            val cmdlineFile = File("/proc/cmdline")
            val isUnlocked = if (cmdlineFile.exists()) {
                val cmdline = cmdlineFile.readText()
                // 一些设备在解锁bootloader后会在cmdline中添加特定标记
                cmdline.contains("androidboot.unlocked_kernel=true") ||
                cmdline.contains("androidboot.verifiedbootstate=orange") ||
                cmdline.contains("unlocked")
            } else false
            
            // 方法2: 检查系统属性
            val systemProps = try {
                val process = Runtime.getRuntime().exec("getprop")
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                val props = mutableMapOf<String, String>()
                reader.lineSequence().forEach { line ->
                    val parts = line.split("]: [")
                    if (parts.size == 2) {
                        val key = parts[0].removePrefix("[")
                        val value = parts[1].removeSuffix("]")
                        props[key] = value
                    }
                }
                reader.close()
                props
            } catch (e: Exception) {
                emptyMap()
            }
            
            val unlockedByProps = systemProps["ro.boot.verifiedbootstate"] == "orange" ||
                                 systemProps["ro.boot.flash.locked"] == "0" ||
                                 systemProps["ro.boot.vbmeta.device_state"] == "unlocked"
            
            val isBootloaderUnlocked = isUnlocked || unlockedByProps
            
            DetectionItem(
                id = "bootloader",
                title = "Bootloader状态检测",
                description = "检测设备Bootloader是否已解锁",
                status = if (isBootloaderUnlocked) DetectionStatus.DANGER else DetectionStatus.SAFE,
                details = if (isBootloaderUnlocked) "Bootloader已解锁" else "Bootloader已锁定",
                iconRes = android.R.drawable.ic_lock_lock
            )
        } catch (e: Exception) {
            DetectionItem(
                id = "bootloader",
                title = "Bootloader状态检测",
                description = "检测设备Bootloader是否已解锁",
                status = DetectionStatus.UNKNOWN,
                details = "检测失败: ${e.message}",
                iconRes = android.R.drawable.ic_menu_help
            )
        }
    }
}