package com.diceapp.core.platform

import java.io.File

actual object FileSystem {
    actual fun getDataDirectory(): String {
        return System.getProperty("user.home") + "/.diceapp"
    }
    
    actual fun readTextFromFile(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun writeTextToFile(filePath: String, content: String): Boolean {
        return try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}