package com.caodong0225.flappybird.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.caodong0225.flappybird.Repository.GameRepository
import com.caodong0225.flappybird.record.GameRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository = GameRepository(application)
    // 使用 StateFlow 来存储游戏记录
    private val _gameRecords = MutableStateFlow<List<GameRecord>>(emptyList())
    val gameRecords = _gameRecords.asStateFlow()

    init {
        getAllGameRecords()
    }
    private fun getAllGameRecords() {
        viewModelScope.launch {
            _gameRecords.value = repository.getAllGameRecords()
        }
    }

    fun deleteGameRecord(timestamp: Long) {
        repository.deleteGameRecord(timestamp)
        getAllGameRecords() // 刷新记录
    }
}