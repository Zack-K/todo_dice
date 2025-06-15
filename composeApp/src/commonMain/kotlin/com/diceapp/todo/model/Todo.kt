package com.diceapp.todo.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import com.benasher44.uuid.uuid4

@Serializable
data class Todo(
    val id: String = uuid4().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "デフォルト",
    val createdAt: String = Clock.System.now().toString(),
    val updatedAt: String = Clock.System.now().toString(),
    val dueDate: String? = null
)

enum class Priority {
    LOW, MEDIUM, HIGH
}