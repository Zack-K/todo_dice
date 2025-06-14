package com.diceapp.randomselector.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.diceapp.randomselector.model.Selection
import com.diceapp.randomselector.model.SelectionResult
import com.diceapp.randomselector.repository.LocalRandomSelectorRepository
import com.diceapp.randomselector.repository.SelectionStatistics
import com.diceapp.randomselector.usecase.RandomSelectorUseCase
import com.diceapp.todo.model.Todo
import com.diceapp.todo.repository.LocalTodoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.diceapp.core.logging.Logger
import com.diceapp.core.config.AppConfig
import kotlin.random.Random

private const val MAX_CUSTOM_SELECTIONS = 6
private const val RECENT_RESULTS_LIMIT = 20

/**
 * ランダム選択機能のViewModelクラス
 * 
 * TODOカテゴリ、特定のTODO、カスタム選択肢からのランダム選択を管理し、
 * 選択結果の履歴や統計情報を提供します。
 */
class RandomSelectorViewModel {
    private val randomSelectorRepository = LocalRandomSelectorRepository()
    private val todoRepository = LocalTodoRepository()
    private val useCase = RandomSelectorUseCase(randomSelectorRepository, todoRepository)
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _recentResults = MutableStateFlow<List<SelectionResult>>(emptyList())
    val recentResults: StateFlow<List<SelectionResult>> = _recentResults.asStateFlow()

    private val _currentResult = MutableStateFlow<SelectionResult?>(null)
    val currentResult: StateFlow<SelectionResult?> = _currentResult.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _incompleteTodos = MutableStateFlow<List<Todo>>(emptyList())
    val incompleteTodos: StateFlow<List<Todo>> = _incompleteTodos.asStateFlow()

    private val _selectedCategory = MutableStateFlow("すべて")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    private val _customTexts = MutableStateFlow(List(MAX_CUSTOM_SELECTIONS) { "" })
    val customTexts: StateFlow<List<String>> = _customTexts.asStateFlow()
    
    private val _currentMode = MutableStateFlow(SelectionMode.FROM_CATEGORY)
    val currentMode: StateFlow<SelectionMode> = _currentMode.asStateFlow()

    // アニメーション関連の状態
    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()
    
    private val _animatingDiceValue = MutableStateFlow<Int?>(null)
    val animatingDiceValue: StateFlow<Int?> = _animatingDiceValue.asStateFlow()

    var showCustomInput by mutableStateOf(false)
        private set
    
    var showStatistics by mutableStateOf(false)
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 利用可能なカテゴリを読み込み
            val categories = listOf("すべて") + useCase.getAvailableCategories()
            _availableCategories.value = categories

            // 未完了TODOを読み込み
            useCase.getAvailableCategories() // Trigger loading
            todoRepository.getAllTodos().collect { todos ->
                _incompleteTodos.value = todos.filter { !it.isCompleted }
            }
        }

        viewModelScope.launch {
            // 最近の結果を読み込み
            useCase.getRecentSelectionResults(RECENT_RESULTS_LIMIT).collect { results ->
                _recentResults.value = results
            }
        }
    }

    fun performQuickSelection() {
        viewModelScope.launch {
            startDiceAnimation()
            
            val result = useCase.quickSelectFromAllTodos()
            result.onSuccess { selectionResult ->
                _currentResult.value = selectionResult
            }.onFailure { error ->
                Logger.error("RandomSelectorViewModel", "クイック選択エラー: ${error.message}", error)
            }
        }
    }

    fun performCategorySelection() {
        if (_selectedCategory.value == "すべて") {
            performQuickSelection()
            return
        }

        viewModelScope.launch {
            val result = useCase.createSelectionFromCategory(_selectedCategory.value)
            result.onSuccess { selection ->
                performSelectionFromSelection(selection.id)
            }.onFailure { error ->
                Logger.error("RandomSelectorViewModel", "カテゴリ選択エラー: ${error.message}", error)
            }
        }
    }

    fun performCustomSelection() {
        val validTexts = _customTexts.value.filter { it.isNotBlank() }
        if (validTexts.isEmpty()) {
            Logger.warning("RandomSelectorViewModel", "有効な選択肢がありません")
            return
        }

        viewModelScope.launch {
            val result = useCase.createSelectionFromTexts(validTexts, "カスタム選択")
            result.onSuccess { selection ->
                performSelectionFromSelection(selection.id)
            }.onFailure { error ->
                Logger.error("RandomSelectorViewModel", "カスタム選択エラー: ${error.message}", error)
            }
        }
    }

    fun performSelectedTodosSelection(todoIds: List<String>) {
        if (todoIds.isEmpty()) {
            Logger.warning("RandomSelectorViewModel", "TODOが選択されていません")
            return
        }

        viewModelScope.launch {
            val result = useCase.createSelectionFromTodos(todoIds, "選択されたTODO")
            result.onSuccess { selection ->
                performSelectionFromSelection(selection.id)
            }.onFailure { error ->
                Logger.error("RandomSelectorViewModel", "TODO選択エラー: ${error.message}", error)
            }
        }
    }

    private suspend fun performSelectionFromSelection(selectionId: String) {
        startDiceAnimation()
        
        val result = useCase.performSelection(selectionId)
        result.onSuccess { selectionResult ->
            _currentResult.value = selectionResult
        }.onFailure { error ->
            Logger.error("RandomSelectorViewModel", "選択実行エラー: ${error.message}", error)
        }
    }
    
    /**
     * ダイスアニメーションを開始
     */
    private suspend fun startDiceAnimation() {
        val config = AppConfig.DEFAULT
        _isAnimating.value = true
        _currentResult.value = null
        
        Logger.info("RandomSelectorViewModel", "ダイスアニメーション開始: ${config.diceAnimationDurationMs}ms")
        
        val startTime = System.currentTimeMillis()
        val endTime = startTime + config.diceAnimationDurationMs
        
        while (System.currentTimeMillis() < endTime) {
            // ランダムなダイス値を表示（1-6の範囲）
            _animatingDiceValue.value = Random.nextInt(1, 7)
            delay(config.diceAnimationUpdateIntervalMs)
        }
        
        _isAnimating.value = false
        _animatingDiceValue.value = null
        Logger.info("RandomSelectorViewModel", "ダイスアニメーション完了")
    }

    fun updateCustomText(index: Int, text: String) {
        val currentTexts = _customTexts.value
        if (index in currentTexts.indices) {
            _customTexts.value = currentTexts.toMutableList().apply {
                set(index, text)
            }
        }
    }

    fun updateSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateCurrentMode(mode: SelectionMode) {
        _currentMode.value = mode
    }

    fun showCustomInputDialog() {
        showCustomInput = true
    }

    fun hideCustomInputDialog() {
        showCustomInput = false
    }

    fun showStatisticsDialog() {
        showStatistics = true
    }

    fun hideStatisticsDialog() {
        showStatistics = false
    }

    fun clearHistory() {
        viewModelScope.launch {
            useCase.clearSelectionHistory().onSuccess {
                _recentResults.value = emptyList()
                _currentResult.value = null
            }
        }
    }

    suspend fun getStatistics(): SelectionStatistics {
        return useCase.getSelectionStatistics()
    }

    enum class SelectionMode {
        FROM_CATEGORY,
        FROM_SELECTED_TODOS,
        CUSTOM
    }
}