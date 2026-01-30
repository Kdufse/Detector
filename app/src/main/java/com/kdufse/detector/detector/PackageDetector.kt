package com.kdufse.detector.detector

import android.content.Context
import android.content.pm.PackageManager
import com.kdufse.detector.model.DetectionItem
import com.kdufse.detector.model.DetectionStatus

class PackageDetector(private val context: Context) {
    
    private val suspiciousPackages = listOf(
        // Magisk相关
        "topjohnwu",
        "com.topjohnwu.magisk",
        "io.github.vvb2060.magisk",
        "io.github.huskydg.magisk",
        "io.github.huskydg.magiskalpha",
        
        // KernelSU相关
        "me.weishu.kernelsu",
        "me.weishu.kernelsu.manager",
        
        // APatch相关
        "org.bmax.apatch",
        "org.bmax.apatch.manager",
        "io.github.bmax.apatch",
        
        // 其他Root工具
        "yuki.folk",
        "acpatch",
        "apatch.next",
        "ksunext",
        "sukisu",
        "com.modosa.rootinstaller",
        "com.thirdparty.superuser",
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.koushikdutta.superuser",
        "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine",
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "de.robv.android.xposed.installer",
        "com.saurik.substrate",
        "com.android.vending.billing.InAppBillingService.LUCK"
    )
    
    fun checkPackages(): DetectionItem {
        return try {
            val packageManager = context.packageManager
            val installedPackages = packageManager.getInstalledPackages(0)
            
            val foundPackages = mutableListOf<String>()
            
            suspiciousPackages.forEach { packageName ->
                try {
                    packageManager.getPackageInfo(packageName, 0)
                    foundPackages.add(packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    // 包不存在，继续检查
                }
            }
            
            // 检查包名中包含可疑关键词的应用
            installedPackages.forEach { packageInfo ->
                suspiciousPackages.forEach { keyword ->
                    if (packageInfo.packageName.contains(keyword, ignoreCase = true)) {
                        if (!foundPackages.contains(packageInfo.packageName)) {
                            foundPackages.add(packageInfo.packageName)
                        }
                    }
                }
            }
            
            val hasSuspiciousPackages = foundPackages.isNotEmpty()
            
            DetectionItem(
                id = "packages",
                title = "应用包名检测",
                description = "检测是否安装了Root相关应用",
                status = if (hasSuspiciousPackages) DetectionStatus.DANGER else DetectionStatus.SAFE,
                details = if (hasSuspiciousPackages) {
                    "发现可疑应用: ${foundPackages.joinToString(", ")}"
                } else {
                    "未发现可疑应用"
                },
                iconRes = android.R.drawable.ic_menu_apps
            )
        } catch (e: Exception) {
            DetectionItem(
                id = "packages",
                title = "应用包名检测",
                description = "检测是否安装了Root相关应用",
                status = DetectionStatus.UNKNOWN,
                details = "检测失败: ${e.message}",
                iconRes = android.R.drawable.ic_menu_help
            )
        }
    }
}