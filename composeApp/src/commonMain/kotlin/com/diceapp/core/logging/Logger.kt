package com.diceapp.core.logging

/**
 * ログレベルの定義
 */
enum class LogLevel(val priority: Int) {
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4)
}

/**
 * アプリケーション用の統一ロガー
 * 
 * 開発環境では詳細なログを出力し、リリース環境では重要なエラーのみを記録します。
 */
object Logger {
    private var currentLevel = LogLevel.DEBUG
    private var isEnabled = true
    
    /**
     * ログレベルを設定
     */
    fun setLevel(level: LogLevel) {
        currentLevel = level
    }
    
    /**
     * ログ出力の有効/無効を設定
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    /**
     * デバッグレベルのログ出力
     */
    fun debug(tag: String, message: String) {
        log(LogLevel.DEBUG, tag, message)
    }
    
    /**
     * 情報レベルのログ出力
     */
    fun info(tag: String, message: String) {
        log(LogLevel.INFO, tag, message)
    }
    
    /**
     * 警告レベルのログ出力
     */
    fun warning(tag: String, message: String) {
        log(LogLevel.WARNING, tag, message)
    }
    
    /**
     * エラーレベルのログ出力
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message)
        throwable?.let {
            if (isEnabled && LogLevel.ERROR.priority >= currentLevel.priority) {
                it.printStackTrace()
            }
        }
    }
    
    private fun log(level: LogLevel, tag: String, message: String) {
        if (!isEnabled || level.priority < currentLevel.priority) return
        
        val timestamp = System.currentTimeMillis()
        val levelString = level.name.padEnd(7)
        println("$timestamp $levelString [$tag] $message")
    }
}