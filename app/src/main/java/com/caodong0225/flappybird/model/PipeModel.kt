package com.caodong0225.flappybird.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import com.caodong0225.flappybird.R
import kotlin.random.Random

class PipeModel(private val type: Int, // type = 1 for bottom pipe, type = 0 for top pipe
                private val screenHeight: Float,
                private val screenWidth: Float) {

    companion object {
        private val pipeImages = SnapshotStateList<Int>().apply {
            add(R.drawable.pipe)       // 普通管道图片资源
            add(R.drawable.pipe_top)   // 顶部管道图片资源
            add(R.drawable.pipe_bottom) // 底部管道图片资源
        }
    }

    // 管道的属性
    var x: Int = 0
    var y: Int = 0
    var speed: Int = 5
    var visible: Boolean = true

    // 当前管道图片资源 ID
    var pipeImageResId by mutableStateOf(if (type == 1) pipeImages[2] else pipeImages[0]) // 0 for top pipe, 2 for bottom pipe

    // 管道列表用于填充
    val filledPipes = mutableListOf<PipeModel>()

    // 设置管道的初始位置
    fun setPosition(x: Int, y: Int) {
        this.x = x
        this.y = y
        if (type == 0) { // 如果是顶部管道
            fillWithPipes(y, -1)
        }
        else if(type == 2)
        {
            fillWithPipes(y, 1)
        }
    }

    // 绘制管道（返回图片资源ID）
    fun draw(): Int {
        movement() // 更新位置
        return pipeImageResId // 返回当前的图片资源 ID
    }


    // 填充普通管道
    private fun fillWithPipes(topY: Int, direction: Int) {
        val gap = 30 // 设定管道之间的空隙
        for (i in 0 until 20) { // 填充三个普通管道
            val pipe = PipeModel(1, screenHeight, screenWidth) // 创建普通管道实例
            pipe.setPosition(screenWidth.toInt(), topY + direction * gap * (i + 1)) // 设置位置
            filledPipes.add(pipe) // 添加到填充管道列表
        }
    }

    // 管道的运动逻辑
    fun movement() {
        x -= speed
        if (x < -120) { // 水管完全离开窗口
            visible = false
        } else {
            visible = true
        }

        // 更新填充管道的位置
        filledPipes.forEach { it.movement() }
    }

    // 判断管道是否可见
    fun isVisible(): Boolean {
        return visible
    }
}