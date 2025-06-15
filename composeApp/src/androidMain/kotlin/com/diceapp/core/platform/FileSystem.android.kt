package com.diceapp.core.platform

import android.content.Context
import java.io.File

// Note: This implementation requires a Context, which would typically be injected
// For simplicity, we're using a basic implementation here
actual object FileSystem {
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context
    }
    
    actual fun getDataDirectory(): String {
        return context?.filesDir?.absolutePath ?: "/data/data/com.diceapp/files"
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