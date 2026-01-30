package com.example.rootdetector.detector

import com.example.rootdetector.model.DetectionItem
import com.example.rootdetector.model.DetectionStatus
import java.io.File

class MountDetector {
    
    fun checkMounts(): DetectionItem {
        return try {
            // 读取/proc/mounts文件
            val mountsFile = File("/proc/mounts")
            val suspiciousMounts = mutableListOf<String>()
            
            if (mountsFile.exists()) {
                val mountsContent = mountsFile.readText()
                
                // 检测关键词
                val mountKeywords = listOf(
                    "Magisk",
                    "APatch",
                    "KernelSU",
                    "/data/adb/modules",
                    "magisk",
                    "apatch",
                    "kernelsu"
                )
                
                mountKeywords.forEach { keyword ->
                    if (mountsContent.contains(keyword, ignoreCase = true)) {
                        suspiciousMounts.add(keyword)
                    }
                }
            }
            
            // 检查/data/adb目录是否存在
            val adbDir = File("/data/adb")
            if (adbDir.exists()) {
                suspiciousMounts.add("/data/adb")
                
                // 检查modules子目录
                val modulesDir = File("/data/adb/modules")
                if (modulesDir.exists()) {
                    suspiciousMounts.add("/data/adb/modules")
                }
            }
            
            // 检查Magisk路径
            val magiskPaths = listOf(
                "/sbin/.magisk",
                "/dev/.magisk",
                "/data/adb/magisk"
            )
            
            magiskPaths.forEach { path ->
                if (File(path).exists()) {
                    suspiciousMounts.add(path)
                }
            }
            
            val hasSuspiciousMounts = suspiciousMounts.isNotEmpty()
            
            DetectionItem(
                id = "mounts",
                title = "挂载点检测",
                description = "检测系统中是否存在Root相关挂载点",
                status = if (hasSuspiciousMounts) DetectionStatus.DANGER else DetectionStatus.SAFE,
                details = if (hasSuspiciousMounts) {
                    "发现可疑挂载点: ${suspiciousMounts.joinToString(", ")}"
                } else {
                    "未发现可疑挂载点"
                },
                iconRes = android.R.drawable.ic_menu_upload
            )
        } catch (e: Exception) {
            DetectionItem(
                id = "mounts",
                title = "挂载点检测",
                description = "检测系统中是否存在Root相关挂载点",
                status = DetectionStatus.UNKNOWN,
                details = "检测失败: ${e.message}",
                iconRes = android.R.drawable.ic_menu_help
            )
        }
    }
}