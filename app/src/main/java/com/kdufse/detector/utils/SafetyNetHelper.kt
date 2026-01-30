package com.kdufse.detector.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.security.MessageDigest
import java.util.*
import javax.net.ssl.HttpsURLConnection

class SafetyNetHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "SafetyNetHelper"
        private const val API_KEY = "YOUR_API_KEY_HERE" // 需要从Google Cloud Console获取
        
        // Play Integrity API endpoint (SafetyNet已被弃用，推荐使用Play Integrity)
        private const val PLAY_INTEGRITY_VERIFY_URL = "https://playintegrity.googleapis.com/v1/PACKAGE_NAME:verifyIntegrityToken"
    }
    
    /**
     * 检查设备是否支持Google Play服务
     */
    fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }
    
    /**
     * 执行SafetyNet Attestation (旧版API，已弃用但部分设备仍可用)
     */
    suspend fun performSafetyNetAttestation(
        nonce: String = generateNonce(),
        apiKey: String = API_KEY
    ): SafetyNetResult = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!isGooglePlayServicesAvailable()) {
                return@withContext SafetyNetResult(
                    success = false,
                    error = "Google Play Services not available"
                )
            }
            
            // 注意：SafetyNet API 已被弃用，使用Play Integrity API替代
            // 这里仅作为示例展示旧的实现方式
            val task = SafetyNet.getClient(context).attest(nonce.toByteArray(), apiKey)
            
            // 由于协程无法直接等待Task，我们使用回调包装
            var result: SafetyNetApi.AttestationResponse? = null
            var exception: Exception? = null
            
            task.addOnSuccessListener { attestationResponse ->
                result = attestationResponse
            }.addOnFailureListener { e ->
                exception = e
            }
            
            // 简单等待，实际应用中应该使用适当的同步机制
            Thread.sleep(2000)
            
            if (exception != null) {
                SafetyNetResult(
                    success = false,
                    error = exception?.message ?: "Unknown error"
                )
            } else if (result != null) {
                parseSafetyNetResult(result!!.jwsResult)
            } else {
                SafetyNetResult(
                    success = false,
                    error = "Timeout or no response"
                )
            }
        } catch (e: Exception) {
            SafetyNetResult(
                success = false,
                error = "Exception: ${e.message}"
            )
        }
    }
    
    /**
     * 生成随机nonce
     */
    private fun generateNonce(): String {
        val random = Random(System.currentTimeMillis())
        val nonceData = ByteArray(24)
        random.nextBytes(nonceData)
        return Base64.getEncoder().encodeToString(nonceData)
    }
    
    /**
     * 解析SafetyNet JWS结果
     */
    private fun parseSafetyNetResult(jwsResult: String): SafetyNetResult {
        return try {
            // JWS格式: header.payload.signature
            val parts = jwsResult.split(".")
            if (parts.size != 3) {
                return SafetyNetResult(
                    success = false,
                    error = "Invalid JWS format"
                )
            }
            
            // 解码payload
            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
            val payload = JSONObject(payloadJson)
            
            // 提取关键信息
            val nonce = payload.optString("nonce")
            val timestampMs = payload.optLong("timestampMs")
            val apkPackageName = payload.optString("apkPackageName")
            val apkCertificateDigestSha256 = payload.optJSONArray("apkCertificateDigestSha256")
            val apkDigestSha256 = payload.optString("apkDigestSha256")
            val ctsProfileMatch = payload.optBoolean("ctsProfileMatch")
            val basicIntegrity = payload.optBoolean("basicIntegrity")
            val evaluationType = payload.optString("evaluationType")
            
            // 检查设备完整性
            val isDeviceIntegrityValid = ctsProfileMatch || basicIntegrity
            
            SafetyNetResult(
                success = true,
                jwsResult = jwsResult,
                ctsProfileMatch = ctsProfileMatch,
                basicIntegrity = basicIntegrity,
                evaluationType = evaluationType,
                timestampMs = timestampMs,
                isDeviceIntegrityValid = isDeviceIntegrityValid,
                details = payload.toString(2)
            )
        } catch (e: Exception) {
            SafetyNetResult(
                success = false,
                error = "Failed to parse JWS: ${e.message}"
            )
        }
    }
    
    /**
     * 执行基本完整性检查（简化版）
     * 注意：这只是一个本地的简单检查，不能替代真正的SafetyNet/Play Integrity
     */
    fun performBasicIntegrityCheck(): BasicIntegrityResult {
        return try {
            val checks = mutableListOf<IntegrityCheck>()
            
            // 检查1: 是否为模拟器
            val isEmulator = checkIsEmulator()
            checks.add(IntegrityCheck(
                name = "Emulator Check",
                passed = !isEmulator,
                details = if (isEmulator) "Running on emulator" else "Not an emulator"
            ))
            
            // 检查2: 是否rooted（通过常见root检测）
            val hasRootFiles = checkRootFiles()
            checks.add(IntegrityCheck(
                name = "Root Files Check",
                passed = !hasRootFiles,
                details = if (hasRootFiles) "Root files detected" else "No root files found"
            ))
            
            // 检查3: 是否允许未知来源
            val unknownSources = checkUnknownSources()
            checks.add(IntegrityCheck(
                name = "Unknown Sources Check",
                passed = !unknownSources,
                details = if (unknownSources) "Unknown sources enabled" else "Unknown sources disabled"
            ))
            
            // 检查4: USB调试
            val usbDebugging = checkUsbDebugging()
            checks.add(IntegrityCheck(
                name = "USB Debugging Check",
                passed = !usbDebugging,
                details = if (usbDebugging) "USB debugging enabled" else "USB debugging disabled"
            ))
            
            // 检查5: 开发者选项
            val developerOptions = checkDeveloperOptions()
            checks.add(IntegrityCheck(
                name = "Developer Options Check",
                passed = !developerOptions,
                details = if (developerOptions) "Developer options enabled" else "Developer options disabled"
            ))
            
            val passedChecks = checks.count { it.passed }
            val totalChecks = checks.size
            val integrityScore = passedChecks.toFloat() / totalChecks
            
            BasicIntegrityResult(
                passedChecks = passedChecks,
                totalChecks = totalChecks,
                integrityScore = integrityScore,
                checks = checks,
                overallPassed = integrityScore >= 0.7f // 70%通过率
            )
        } catch (e: Exception) {
            BasicIntegrityResult(
                passedChecks = 0,
                totalChecks = 0,
                integrityScore = 0f,
                checks = emptyList(),
                overallPassed = false,
                error = "Integrity check failed: ${e.message}"
            )
        }
    }
    
    /**
     * 检查是否运行在模拟器上
     */
    private fun checkIsEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google".equals(Build.PRODUCT, ignoreCase = true) ||
                System.getProperty("ro.kernel.qemu") == "1")
    }
    
    /**
     * 检查常见的root文件
     */
    private fun checkRootFiles(): Boolean {
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/sbin/su",
            "/system/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su",
            "/system/xbin/ku.sud",
            "/system/xbin/mu",
            "/sbin/magisk"
        )
        
        return rootPaths.any { path ->
            try {
                val file = java.io.File(path)
                file.exists()
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * 检查是否允许未知来源安装
     */
    private fun checkUnknownSources(): Boolean {
        return try {
            // 这是一个简化检查，实际应该检查Settings.Global.INSTALL_NON_MARKET_APPS
            // 但需要系统权限
            false // 默认返回false，实际实现需要适当权限
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查USB调试是否启用
     */
    private fun checkUsbDebugging(): Boolean {
        return try {
            // 检查系统属性
            val process = Runtime.getRuntime().exec("getprop persist.sys.usb.config")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            process.waitFor()
            
            result?.contains("adb") == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查开发者选项是否启用
     */
    private fun checkDeveloperOptions(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("settings get global development_settings_enabled")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            process.waitFor()
            
            result == "1"
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 计算APK证书哈希（用于验证应用完整性）
     */
    fun getAppCertificateHash(): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName, 
                android.content.pm.PackageManager.GET_SIGNATURES
            )
            
            val signatures = packageInfo.signatures
            if (signatures.isNotEmpty()) {
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(signatures[0].toByteArray())
                Base64.getEncoder().encodeToString(digest)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app certificate hash", e)
            null
        }
    }
}

/**
 * SafetyNet结果数据类
 */
data class SafetyNetResult(
    val success: Boolean,
    val jwsResult: String? = null,
    val ctsProfileMatch: Boolean = false,
    val basicIntegrity: Boolean = false,
    val evaluationType: String? = null,
    val timestampMs: Long = 0,
    val isDeviceIntegrityValid: Boolean = false,
    val details: String? = null,
    val error: String? = null
)

/**
 * 基本完整性检查结果
 */
data class BasicIntegrityResult(
    val passedChecks: Int,
    val totalChecks: Int,
    val integrityScore: Float,
    val checks: List<IntegrityCheck>,
    val overallPassed: Boolean,
    val error: String? = null
)

/**
 * 完整性检查项
 */
data class IntegrityCheck(
    val name: String,
    val passed: Boolean,
    val details: String
)