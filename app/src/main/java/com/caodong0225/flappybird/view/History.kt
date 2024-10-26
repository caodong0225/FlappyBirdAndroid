package com.caodong0225.flappybird.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.caodong0225.flappybird.model.GameRecordViewModel
import com.caodong0225.flappybird.record.GameRecord


class History : ComponentActivity() {
    private lateinit var viewModel: GameRecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[GameRecordViewModel::class.java]
        setContent {
            HistoryScreen(viewModel)
        }
    }
}

@Composable
fun HistoryScreen(viewModel: GameRecordViewModel) {
    // 获取游戏记录数据
    val gameRecords = viewModel.getAllGameRecords()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "游戏历史",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 使用 LazyColumn 显示游戏记录
        if (gameRecords.isEmpty()) {
            Text(text = "暂无记录")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(gameRecords) { record ->
                    GameRecordItem(record)
                }
            }
        }
    }
}

@Composable
fun GameRecordItem(record: GameRecord) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "分数: ${record.score}")
        Text(text = "游玩时间: ${record.timestamp}")
        Text(text = "位置: ${record.location}")
        Text(text = "纬度: ${record.latitude}")
        Text(text = "精度: ${record.longitude}")
        Text(text = "游玩时长: ${record.duration}")
        Text(text = "说明: ${record.remark}")
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}