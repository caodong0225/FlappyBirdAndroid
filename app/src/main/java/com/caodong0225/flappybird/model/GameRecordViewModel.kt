package com.caodong0225.flappybird.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.caodong0225.flappybird.Repository.GameRepository
import com.caodong0225.flappybird.record.GameRecord

class GameRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository = GameRepository(application)

    fun getAllGameRecords(): List<GameRecord> {
        return repository.getAllGameRecords()
    }
}