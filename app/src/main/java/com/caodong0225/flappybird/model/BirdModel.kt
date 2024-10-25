package com.caodong0225.flappybird.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import com.caodong0225.flappybird.R


class BirdModel(private val screenHeight: Float) {

    // 控制小鸟的当前位置
    var positionY by mutableStateOf(0f)

    // 小鸟的图片状态（上升 或 不同阶段的下降）
    var birdImageResId by mutableStateOf(R.drawable.down_0)

    // 旋转角度
    var rotationAngle by mutableStateOf(0f)

    // 速度，用于控制小鸟上升或下降的幅度
    private var speed = 0f

    // 重力影响的加速度
    private val gravity = 4.8f

    private val birdSizePx = 60

    // 小鸟的状态（是否处于上升状态）
    var isFlying by mutableStateOf(false)
        private set

    // 更新小鸟的状态：上升
    fun startFlying() {
        isFlying = true
        speed = -12f  // 初始上升速度
    }

    // 更新小鸟的状态：停止上升（下落）
    fun stopFlying() {
        isFlying = false
    }

    fun reset()
    {
        this.positionY = 0F
    }

    fun getBoundingBox(): Rect {
        val birdX = 0f // The X position of the bird; this should match your UI layout
        return Rect(birdX, positionY, birdX + birdSizePx, positionY + birdSizePx)
    }

    // 更新小鸟的位置，考虑速度和重力
    fun updatePosition() {
        // 更新速度，下降时加速
        if (!isFlying) {
            speed += gravity * 0.5f  // 重力加速下落
        } else {
            // 上升时减缓速度，最终会逐渐停止
            speed += gravity * 0.1f
        }

        // 更新小鸟的位置
        positionY += speed

        // 防止小鸟掉到地面以下
        if (positionY > screenHeight) {
            positionY = screenHeight
        }

        // 防止小鸟飞出屏幕
        if (positionY < 0f) {
            positionY = 0f
        }

        // 调整下落的图片，根据速度分阶段选择不同的图片
        updateRotationAngle()
    }


    // 根据当前速度调整旋转角度
    private fun updateRotationAngle() {
        rotationAngle = when {
            speed < 0 -> {
                // 上升时，向上倾斜
                -15f  // 自定义的上升角度
            }
            speed in 0f..5f -> {
                // 平稳下落
                10f
            }
            speed in 5f..10f -> {
                // 轻微下落
                30f
            }
            speed in 10f..15f -> {
                // 中度下落
                50f
            }
            else -> {
                // 大幅下落
                70f
            }
        }
    }
}