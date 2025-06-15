package com.diceapp.core.platform

/**
 * Platform-agnostic file system operations
 */
expect object FileSystem {
    /**
     * Get the application data directory
     */
    fun getDataDirectory(): String
    
    /**
     * Read text from file
     */
    fun readTextFromFile(filePath: String): String?
    
    /**
     * Write text to file
     */
    fun writeTextToFile(filePath: String, content: String): Boolean
    
    /**
     * Check if file exists
     */
    fun fileExists(filePath: String): Boolean
}