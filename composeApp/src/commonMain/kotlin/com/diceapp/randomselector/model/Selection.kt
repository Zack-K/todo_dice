package com.diceapp.randomselector.model

import com.diceapp.todo.model.Todo
import kotlinx.serialization.Serializable

@Serializable
data class Selection(
    val id: String = java.util.UUID.randomUUID().toString(),
    val items: List<SelectionItem>,
    val title: String = "ランダム選択",
    val timestamp: String = java.time.LocalDateTime.now().toString()
) {
    init {
        require(items.isNotEmpty()) { "選択肢は1つ以上必要です" }
        require(items.size <= 6) { "選択肢は最大6つまでです" }
    }
}

@Serializable
data class SelectionItem(
    val id: String,
    val text: String,
    val weight: Int = 1
) {
    init {
        require(text.isNotBlank()) { "選択肢のテキストは空白にできません" }
        require(weight > 0) { "重みは1以上である必要があります" }
    }
}

@Serializable
data class SelectionResult(
    val selection: Selection,
    val diceRoll: Int,
    val selectedItem: SelectionItem,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

// TODOからSelectionItemを作成するヘルパー関数
fun Todo.toSelectionItem(): SelectionItem {
    return SelectionItem(
        id = this.id,
        text = this.title
    )
}

fun List<Todo>.toSelection(title: String = "TODOからの選択"): Selection {
    require(this.isNotEmpty()) { "TODOリストは空であってはいけません" }
    require(this.size <= 6) { "TODOは最大6つまで選択できます" }
    
    return Selection(
        items = this.map { it.toSelectionItem() },
        title = title
    )
}