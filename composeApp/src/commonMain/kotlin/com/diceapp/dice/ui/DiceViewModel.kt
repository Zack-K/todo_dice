package com.diceapp.dice.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.diceapp.dice.model.DiceRoll
import com.diceapp.dice.model.StandardDice
import com.diceapp.dice.repository.DiceStatistics
import com.diceapp.dice.repository.LocalDiceRepository
import com.diceapp.dice.usecase.DiceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.diceapp.core.logging.Logger

private const val RECENT_ROLLS_LIMIT = 20
private const val DEFAULT_CUSTOM_SIDES = 6
private const val DEFAULT_DICE_COUNT = 1
private const val DEFAULT_MODIFIER = 0
private const val MIN_CUSTOM_SIDES = 2
private const val MAX_CUSTOM_SIDES = 100
private const val MIN_DICE_COUNT = 1
private const val MAX_DICE_COUNT = 10
private const val MIN_MODIFIER = -50
private const val MAX_MODIFIER = 50

/**
 * ダイス機能のViewModelクラス
 * 
 * 標準ダイス、カスタムダイス、ダイス記法によるロールを管理し、
 * 履歴や統計情報を提供します。
 */
class DiceViewModel {
    private val repository = LocalDiceRepository()
    private val useCase = DiceUseCase(repository)
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _recentRolls = MutableStateFlow<List<DiceRoll>>(emptyList())
    val recentRolls: StateFlow<List<DiceRoll>> = _recentRolls.asStateFlow()

    private val _currentRoll = MutableStateFlow<DiceRoll?>(null)
    val currentRoll: StateFlow<DiceRoll?> = _currentRoll.asStateFlow()

    private val _selectedStandardDice = MutableStateFlow(StandardDice.D6)
    val selectedStandardDice: StateFlow<StandardDice> = _selectedStandardDice.asStateFlow()
    
    private val _customSides = MutableStateFlow(DEFAULT_CUSTOM_SIDES)
    val customSides: StateFlow<Int> = _customSides.asStateFlow()
    
    private val _diceCount = MutableStateFlow(DEFAULT_DICE_COUNT)
    val diceCount: StateFlow<Int> = _diceCount.asStateFlow()
    
    private val _modifier = MutableStateFlow(DEFAULT_MODIFIER)
    val modifier: StateFlow<Int> = _modifier.asStateFlow()
    
    private val _diceNotation = MutableStateFlow("")
    val diceNotation: StateFlow<String> = _diceNotation.asStateFlow()
    
    private val _isUsingCustomDice = MutableStateFlow(false)
    val isUsingCustomDice: StateFlow<Boolean> = _isUsingCustomDice.asStateFlow()
    
    private val _isUsingNotation = MutableStateFlow(false)
    val isUsingNotation: StateFlow<Boolean> = _isUsingNotation.asStateFlow()

    var showStatistics by mutableStateOf(false)
        private set
    
    var statisticsForSides by mutableStateOf(6)
        private set

    init {
        loadRecentRolls()
    }

    private fun loadRecentRolls() {
        viewModelScope.launch {
            useCase.getRecentRolls(RECENT_ROLLS_LIMIT).collect { rolls ->
                _recentRolls.value = rolls
            }
        }
    }

    fun rollStandardDice() {
        if (_isUsingNotation.value && _diceNotation.value.isNotBlank()) {
            rollFromNotation()
            return
        }

        viewModelScope.launch {
            val result = if (_isUsingCustomDice.value) {
                useCase.rollDice(
                    sides = _customSides.value,
                    count = _diceCount.value,
                    modifier = _modifier.value
                )
            } else {
                useCase.rollStandardDice(
                    standardDice = _selectedStandardDice.value,
                    count = _diceCount.value,
                    modifier = _modifier.value
                )
            }

            result.onSuccess { roll ->
                _currentRoll.value = roll
            }.onFailure { error ->
                Logger.error("DiceViewModel", "ダイスロールエラー: ${error.message}", error)
            }
        }
    }

    private fun rollFromNotation() {
        viewModelScope.launch {
            val result = useCase.parseDiceNotation(_diceNotation.value)
            result.onSuccess { roll ->
                _currentRoll.value = roll
            }.onFailure { error ->
                Logger.error("DiceViewModel", "ダイス記法エラー: ${error.message}", error)
            }
        }
    }

    fun rollRandomDice() {
        viewModelScope.launch {
            val result = useCase.getRandomRoll()
            result.onSuccess { roll ->
                _currentRoll.value = roll
            }.onFailure { error ->
                Logger.error("DiceViewModel", "ランダムダイスエラー: ${error.message}", error)
            }
        }
    }

    fun showStatisticsFor(sides: Int) {
        statisticsForSides = sides
        showStatistics = true
    }

    fun hideStatistics() {
        showStatistics = false
    }

    fun clearHistory() {
        viewModelScope.launch {
            useCase.clearHistory().onSuccess {
                _recentRolls.value = emptyList()
                _currentRoll.value = null
            }
        }
    }

    suspend fun getStatistics(sides: Int): DiceStatistics {
        return useCase.getDiceStatistics(sides)
    }

    fun updateSelectedStandardDice(dice: StandardDice) {
        _selectedStandardDice.value = dice
    }

    fun updateCustomSides(sides: String) {
        sides.toIntOrNull()?.let { value ->
            if (value in MIN_CUSTOM_SIDES..MAX_CUSTOM_SIDES) {
                _customSides.value = value
            }
        }
    }

    fun updateDiceCount(count: String) {
        count.toIntOrNull()?.let { value ->
            if (value in MIN_DICE_COUNT..MAX_DICE_COUNT) {
                _diceCount.value = value
            }
        }
    }

    fun updateModifier(mod: String) {
        mod.toIntOrNull()?.let { value ->
            if (value in MIN_MODIFIER..MAX_MODIFIER) {
                _modifier.value = value
            }
        }
    }

    fun updateDiceNotation(notation: String) {
        _diceNotation.value = notation
    }

    fun toggleUseCustomDice() {
        _isUsingCustomDice.value = !_isUsingCustomDice.value
    }

    fun toggleUseNotation() {
        _isUsingNotation.value = !_isUsingNotation.value
    }
}