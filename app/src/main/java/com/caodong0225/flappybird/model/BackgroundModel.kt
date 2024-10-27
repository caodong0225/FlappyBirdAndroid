package com.caodong0225.flappybird.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.caodong0225.flappybird.R

class BackgroundModel {

    companion object {
        private val backgroundImages = listOf(
            R.drawable.background, // 背景图片资源 1
            R.drawable.background  // 背景图片资源 2，第二张与第一张相同
            // 可以添加更多背景图片
        )
    }

    // 背景的属性
    var x1: Int = 0 // 第一张背景的位置
    var x2: Int = 360 // 第二张背景的位置，跟第一张图片拼接在一起
    var speed: Int = 4 // 背景移动速度

    // 当前背景图片资源 ID
    var backgroundImageResId1 by mutableStateOf(backgroundImages[0])
    var backgroundImageResId2 by mutableStateOf(backgroundImages[1])

    // 初始化设置
    init {
        // 设置第二张图片的初始位置紧接在第一张图片后
        x2 = 360 // 假设每张图片的宽度为300
    }

    // 背景的运动逻辑
    fun movement(screenWidth: Int) {
        // 背景向左移动
        x1 -= speed
        x2 -= speed

        // 如果第一张背景图片完全移出屏幕，则将其移动到第二张图片的右侧
        if (x1 <= -screenWidth) {
            x1 = x2 + screenWidth
        }

        // 如果第二张背景图片完全移出屏幕，则将其移动到第一张图片的右侧
        if (x2 <= -screenWidth) {
            x2 = x1 + screenWidth
        }
    }
}