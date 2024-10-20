package com.caodong0225.flappybird.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.caodong0225.flappybird.R

class CloudModel{

    companion object {
        private val cloudImages = SnapshotStateList<Int>().apply {
            add(R.drawable.cloud_0) // 云朵图片资源
            add(R.drawable.cloud_1) // 另一种云朵图片资源
            // 可以添加更多云朵图片
        }
    }

    // 云朵的属性
    var x: Int = 0
    var y: Int = 0
    var speed: Int = 2 // 云朵移动速度
    var visible: Boolean = true
    // 计数器用于控制云朵图片的变化
    private var imageChangeCounter = 0
    // 当前云朵图片资源 ID
    var cloudImageResId by mutableStateOf(cloudImages[0])

    // 设置云朵的初始位置
    fun setPosition(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    // 绘制云朵（返回图片资源ID）
    fun draw(): Int {
        movement() // 更新位置
        return cloudImageResId // 返回当前的图片资源 ID
    }

    // 云朵的运动逻辑
    fun movement() {
        x -= speed // 向左移动
        imageChangeCounter++ // 增加计数器

        // 每隔一定次数（例如每 5 次移动）交替改变云朵的图片
        if (imageChangeCounter >= 5) {
            // 切换到下一个云朵图片
            cloudImageResId = cloudImages[(cloudImages.indexOf(cloudImageResId) + 1) % cloudImages.size]
            imageChangeCounter = 0 // 重置计数器
        }
        if (x < -120) { // 云朵完全离开窗口
            visible = false
        } else {
            visible = true
        }
    }

    // 判断云朵是否可见
    fun isVisible(): Boolean {
        return visible
    }
}
