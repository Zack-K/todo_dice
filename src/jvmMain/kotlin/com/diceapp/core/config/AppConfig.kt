package com.diceapp.core.config

import kotlinx.serialization.Serializable

/**
 * アプリケーション設定
 * 
 * アニメーション時間やUI設定などのアプリケーション全体の設定を管理します。
 */
@Serializable
data class AppConfig(
    /**
     * ダイスアニメーション時間（ミリ秒）
     * ランダム選択時のダイスロールアニメーションの継続時間
     */
    val diceAnimationDurationMs: Long = 6000L,
    
    /**
     * ダイスアニメーション更新間隔（ミリ秒）
     * アニメーション中のダイス値更新頻度
     */
    val diceAnimationUpdateIntervalMs: Long = 100L,
    
    /**
     * ダイスタブ表示設定
     * falseの場合、ダイスタブを非表示にします
     */
    val showDiceTab: Boolean = false
) {
    companion object {
        /**
         * デフォルト設定インスタンス
         */
        val DEFAULT = AppConfig()
    }
}