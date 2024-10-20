package com.caodong0225.flappybird.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager


object ScreenUtils {

    // 获取屏幕宽度（dp）
    fun getScreenWidthDp(context: Context): Float {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        val density = metrics.density  // 获取屏幕密度
        return metrics.widthPixels / density  // 将像素转换为 dp
    }

    // 获取屏幕高度（dp）
    fun getScreenHeightDp(context: Context): Float {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        val density = metrics.density  // 获取屏幕密度
        return metrics.heightPixels / density  // 将像素转换为 dp
    }

}