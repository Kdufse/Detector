package com.kdufse.detector.utils

import java.io.File

object FileUtils {
    
    fun fileContainsKeywords(file: File, keywords: List<String>): Boolean {
        return try {
            if (file.exists() && file.canRead()) {
                val content = file.readText()
                keywords.any { keyword ->
                    content.contains(keyword, ignoreCase = true)
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun searchFilesInDirectory(directory: File, keywords: List<String>): List<File> {
        val foundFiles = mutableListOf<File>()
        
        if (directory.exists() && directory.isDirectory) {
            directory.walk().forEach { file ->
                if (file.isFile) {
                    if (keywords.any { keyword ->
                            file.name.contains(keyword, ignoreCase = true) ||
                            file.path.contains(keyword, ignoreCase = true)
                        }) {
                        foundFiles.add(file)
                    }
                }
            }
        }
        
        return foundFiles
    }
    
    fun checkFilePermissions(file: File): String {
        return buildString {
            append("Exists: ${file.exists()}\n")
            append("Can Read: ${file.canRead()}\n")
            append("Can Write: ${file.canWrite()}\n")
            append("Can Execute: ${file.canExecute()}\n")
            append("Is Directory: ${file.isDirectory}\n")
        }
    }
}