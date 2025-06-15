package com.diceapp.core.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.Foundation.*
import platform.posix.remove

@OptIn(ExperimentalForeignApi::class)
actual object FileSystem {
    actual fun getDataDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        return "${paths.first() as String}/DiceApp"
    }
    
    actual fun readTextFromFile(filePath: String): String? {
        return try {
            val nsString = NSString.stringWithContentsOfFile(
                filePath,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            nsString?.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun writeTextToFile(filePath: String, content: String): Boolean {
        return try {
            val nsString = NSString.create(string = content)
            val directory = (filePath as NSString).stringByDeletingLastPathComponent
            
            // Create directory if it doesn't exist
            NSFileManager.defaultManager.createDirectoryAtPath(
                directory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
            
            nsString.writeToFile(
                filePath,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun fileExists(filePath: String): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(filePath)
    }
}