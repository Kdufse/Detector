package com.kdufse.detector.detector

import com.kdufse.detector.model.DetectionItem
import com.kdufse.detector.model.DetectionStatus
import java.io.File

class ProcessDetector {
    
    fun checkProcesses(): DetectionItem {
        return try {
            val suspiciousProcesses = mutableListOf<String>()
            val suspiciousLibraries = mutableListOf<String>()
            
            // 检查/proc目录下的进程
            val procDir = File("/proc")
            if (procDir.exists() && procDir.isDirectory) {
                procDir.listFiles()?.forEach { processDir ->
                    if (processDir.isDirectory && processDir.name.matches(Regex("\\d+"))) {
                        try {
                            // 检查进程命令行
                            val cmdlineFile = File(processDir, "cmdline")
                            if (cmdlineFile.exists()) {
                                val cmdline = cmdlineFile.readText()
                                checkProcessCmdline(cmdline)?.let {
                                    suspiciousProcesses.add(it)
                                }
                            }
                            
                            // 检查进程maps文件中的动态库
                            val mapsFile = File(processDir, "maps")
                            if (mapsFile.exists()) {
                                val mapsContent = mapsFile.readText()
                                checkProcessMaps(mapsContent)?.let {
                                    suspiciousLibraries.add(it)
                                }
                            }
                        } catch (e: Exception) {
                            // 忽略无法读取的进程
                        }
                    }
                }
            }
            
            // 检查特定的系统属性
            val systemProps = try {
                val process = Runtime.getRuntime().exec("getprop")
                val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                val props = mutableListOf<String>()
                reader.lineSequence().forEach { line ->
                    props.add(line)
                }
                reader.close()
                props.joinToString("\n")
            } catch (e: Exception) {
                ""
            }
            
            // 检查属性中的关键词
            val propKeywords = listOf("magisk", "apatch", "kernelsu")
            propKeywords.forEach { keyword ->
                if (systemProps.contains(keyword, ignoreCase = true)) {
                    suspiciousProcesses.add("system_prop:$keyword")
                }
            }
            
            val hasSuspicious = suspiciousProcesses.isNotEmpty() || suspiciousLibraries.isNotEmpty()
            
            DetectionItem(
                id = "processes",
                title = "进程与动态库检测",
                description = "检测运行进程和动态库中的Root痕迹",
                status = if (hasSuspicious) DetectionStatus.DANGER else DetectionStatus.SAFE,
                details = if (hasSuspicious) {
                    val details = mutableListOf<String>()
                    if (suspiciousProcesses.isNotEmpty()) {
                        details.add("可疑进程: ${suspiciousProcesses.distinct().joinToString(", ")}")
                    }
                    if (suspiciousLibraries.isNotEmpty()) {
                        details.add("可疑动态库: ${suspiciousLibraries.distinct().joinToString(", ")}")
                    }
                    details.joinToString("\n")
                } else {
                    "未发现可疑进程或动态库"
                },
                iconRes = android.R.drawable.ic_dialog_info
            )
        } catch (e: Exception) {
            DetectionItem(
                id = "processes",
                title = "进程与动态库检测",
                description = "检测运行进程和动态库中的Root痕迹",
                status = DetectionStatus.UNKNOWN,
                details = "检测失败: ${e.message}",
                iconRes = android.R.drawable.ic_menu_help
            )
        }
    }
    
    private fun checkProcessCmdline(cmdline: String): String? {
        val keywords = listOf(
            "magisk",
            "apatch",
            "kernelsu",
            "ksud",
            "apd",
            "zygisk",
            "su",
            "daemon"
        )
        
        keywords.forEach { keyword ->
            if (cmdline.contains(keyword, ignoreCase = true)) {
                return keyword
            }
        }
        return null
    }
    
    private fun checkProcessMaps(mapsContent: String): String? {
        val libraryKeywords = listOf(
            "magisk",
            "apatch",
            "kernelsu",
            "libmagisk",
            "libapatch",
            "libksu",
            "magisk.db",
            "apd",
            "ksud"
        )
        
        libraryKeywords.forEach { keyword ->
            if (mapsContent.contains(keyword, ignoreCase = true)) {
                return keyword
            }
        }
        return null
    }
}