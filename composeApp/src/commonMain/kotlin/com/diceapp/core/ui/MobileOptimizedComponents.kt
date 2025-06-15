package com.diceapp.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * モバイル最適化されたボタンコンポーネント
 * - 最適なタッチターゲットサイズ (48dp以上)
 * - ハプティックフィードバック
 * - アニメーション効果
 */
@Composable
fun MobileOptimizedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 24.dp,
        vertical = 16.dp
    ),
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * モバイル最適化されたFloatingActionButton
 * - ハプティックフィードバック
 * - アニメーション効果
 */
@Composable
fun MobileOptimizedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "fab_scale"
    )

    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        containerColor = containerColor,
        content = content
    )
}

/**
 * プルトゥリフレッシュ風のローディングインジケーター
 */
@Composable
fun MobileLoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String = "読み込み中..."
) {
    AnimatedVisibility(
        visible = isLoading,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * レスポンシブな間隔調整
 */
@Composable
fun ResponsiveSpacer(
    compactHeight: Int = 8,
    expandedHeight: Int = 16
) {
    // 今回はcompactサイズで固定
    Spacer(modifier = Modifier.height(compactHeight.dp))
}

/**
 * タッチフレンドリーなカードコンポーネント
 */
@Composable
fun MobileFriendlyCard(
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick?.invoke()
                                }
                            )
                        }
                } else Modifier
            ),
        colors = colors,
        elevation = elevation,
        content = content
    )
}

/**
 * アニメーション付きの結果表示
 */
@Composable
fun AnimatedResultDisplay(
    result: String?,
    isAnimating: Boolean,
    animatingValue: String = "?",
    modifier: Modifier = Modifier
) {
    val displayText = when {
        isAnimating -> animatingValue
        result != null -> result
        else -> "結果を表示"
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isAnimating -> MaterialTheme.colorScheme.tertiary
            result != null -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "result_background"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isAnimating -> MaterialTheme.colorScheme.onTertiary
            result != null -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
        label = "result_text"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}