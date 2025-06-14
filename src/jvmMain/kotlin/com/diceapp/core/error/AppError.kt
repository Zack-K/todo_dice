package com.diceapp.core.error

/**
 * アプリケーション固有のエラータイプ
 */
sealed class AppError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * データ保存/読み込みエラー
     */
    data class DataError(
        val operation: String,
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError("データ$operation エラー: $message", cause)
    
    /**
     * バリデーションエラー
     */
    data class ValidationError(
        val field: String,
        val value: String,
        val rule: String
    ) : AppError("バリデーションエラー: $field ($value) は $rule を満たしていません")
    
    /**
     * ビジネスロジックエラー
     */
    data class BusinessLogicError(
        val operation: String,
        override val message: String
    ) : AppError("ビジネスロジックエラー: $operation - $message")
    
    /**
     * 設定エラー
     */
    data class ConfigurationError(
        val setting: String,
        override val message: String
    ) : AppError("設定エラー: $setting - $message")
    
    /**
     * 予期しないエラー
     */
    data class UnexpectedError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError("予期しないエラー: $message", cause)
}

/**
 * Result型の拡張関数で、エラーハンドリングを簡素化
 */
inline fun <T> Result<T>.onAppError(action: (AppError) -> Unit): Result<T> {
    if (isFailure) {
        val exception = exceptionOrNull()
        if (exception is AppError) {
            action(exception)
        }
    }
    return this
}

/**
 * エラーを安全に実行し、AppErrorでラップ
 */
inline fun <T> safeExecute(
    operation: String,
    action: () -> T
): Result<T> {
    return try {
        Result.success(action())
    } catch (e: AppError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AppError.UnexpectedError("$operation 中にエラーが発生しました", e))
    }
}