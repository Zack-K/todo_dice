package com.diceapp.core.platform

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Platform-agnostic clock operations
 */
object AppClock {
    /**
     * Get current time in milliseconds
     */
    fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
    
    /**
     * Get current instant
     */
    fun now(): Instant = Clock.System.now()
}