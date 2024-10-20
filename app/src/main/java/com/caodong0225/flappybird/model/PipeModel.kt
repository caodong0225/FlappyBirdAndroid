package com.caodong0225.flappybird.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.Dp
import com.caodong0225.flappybird.R
import kotlin.random.Random

class PipeModel(private val type: Int,
                private val screenHeight: Float,
                private val screenWidth: Float
    ) {

    companion object {
        private val pipeImages = SnapshotStateList<Int>().apply {
            add(R.drawable.pipe) // 使用 pipe 图片资源
            add(R.drawable.pipe_top) // 顶部管道图片资源
            add(R.drawable.pipe_bottom) // 底部管道图片资源
        }

    }

    // 管道的属性
    var x: Int = 0
    var y: Int = 0
    var speed: Int = 5
    var visible: Boolean = true

    // 当前管道图片资源 ID
    var pipeImageResId by mutableStateOf(pipeImages[type])

    // 设置管道的初始位置
    fun setPosition(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    // 绘制管道（返回图片资源ID）
    fun draw(): Int {
        movement() // 更新位置
        return pipeImageResId // 返回当前的图片资源 ID
    }

    // 随机设置管道的初始位置
    fun setRandomPosition(down: Int, up: Int) {
        x = screenWidth.toInt() // 初始位置从右侧屏幕外开始
        // 随机生成 Y 坐标（根据需要设置随机范围）
        y = Random.nextInt(down, up)
    }

    // 管道的运动逻辑
    fun movement() {
        x -= speed
        if (x < -120) { // 水管完全离开窗口
            visible = false
        }else
        {
            visible = true
        }
    }

    // 判断管道是否可见
    fun isVisible(): Boolean {
        return visible
    }
}