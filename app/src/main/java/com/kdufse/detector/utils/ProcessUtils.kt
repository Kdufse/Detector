package com.kdufse.detector.utils

object ProcessUtils {
    
    fun executeCommand(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = process.inputStream.bufferedReader()
            val result = reader.readText()
            reader.close()
            process.waitFor()
            result
        } catch (e: Exception) {
            null
        }
    }
    
    fun isProcessRunning(processName: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ps")
            val reader = process.inputStream.bufferedReader()
            val lines = reader.readLines()
            reader.close()
            process.waitFor()
            
            lines.any { line -> line.contains(processName) }
        } catch (e: Exception) {
            false
        }
    }
}