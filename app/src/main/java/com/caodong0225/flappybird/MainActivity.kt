package com.caodong0225.flappybird

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.caodong0225.flappybird.model.BackgroundModel
import com.caodong0225.flappybird.model.BirdModel
import com.caodong0225.flappybird.model.CloudModel
import com.caodong0225.flappybird.model.PipeModel
import com.caodong0225.flappybird.ui.theme.FlappyBirdTheme
import com.caodong0225.flappybird.util.ScreenUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlappyBirdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BirdGame()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BirdGame(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val screenHeight = ScreenUtils.getScreenHeightDp(context)
    val screenWidth = ScreenUtils.getScreenWidthDp(context)
    val birdSize = 80.dp // 鸟的大小
    val birdSizePx: Float
    val pipeWidth = 80.dp
    val pipeHeight = 100.dp
    val density = LocalDensity.current
    with(density) {
        birdSizePx = birdSize.toPx()  // 将 dp 转换为像素 float 值
    }

    // 创建 BirdModel 实例来控制小鸟的状态
    val birdModel = remember { BirdModel(screenHeight - birdSizePx / 2) }

    val backgroundModel = remember { BackgroundModel() }

    // 管道列表用于填充
    val pipes = remember { mutableListOf<PipeModel>() }


    val cloudModel = remember {
        CloudModel()
    }
    // 协程处理器，用于定时更新小鸟的位置
    val coroutineScope = rememberCoroutineScope()

    var isGameOver = remember { mutableStateOf(false) }


    fun startGameLoop() {
        coroutineScope.launch {
            while (!isGameOver.value) {
                birdModel.updatePosition()  // 更新小鸟位置

                // 检查碰撞
                val birdBox = birdModel.getBoundingBox()
                val hasCollision = pipes.any { pipe -> pipe.checkCollision(birdBox, density) }

                if (hasCollision) {
                    // 碰撞发生时，停止游戏逻辑
                    isGameOver.value = true
                    break
                }

                // 遍历管道列表，更新每个管道的位置
                pipes.forEach { pipe -> pipe.movement() }
                cloudModel.movement() // 更新云朵位置
                backgroundModel.movement(screenWidth.toInt()) // 更新背景位置

                // 处理云朵和管道的可见性
                val iterator = pipes.iterator()
                while (iterator.hasNext()) {
                    val pipe = iterator.next()
                    if (!pipe.isVisible()) {
                        iterator.remove()  // 删除不可见的管道
                    }
                }

                // 如果管道数量不足，则添加新管道
                if (pipes.size < 2 || (pipes.size in 2..3 && pipes[0].x < screenWidth / 3)) {
                    val topY = Random.nextInt(20, 400)
                    val bottomY = topY + Random.nextInt(260, 300)

                    // 添加新的管道
                    pipes.add(PipeModel(0, screenHeight, screenWidth).apply {
                        setPosition(screenWidth.toInt(), topY)
                    })
                    pipes.add(PipeModel(2, screenHeight, screenWidth).apply {
                        setPosition(screenWidth.toInt(), bottomY)
                    })
                }

                if (!cloudModel.isVisible()) {
                    val cloudY = Random.nextInt(100, 200)
                    cloudModel.setPosition(screenWidth.toInt(), cloudY)
                }

                delay(36L)  // 每 36 毫秒更新一次位置
            }
        }
    }
    // 触摸事件控制小鸟的上升与下降
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInteropFilter {
                when (it.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        birdModel.startFlying()  // 开始飞
                        true
                    }

                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        birdModel.stopFlying()  // 停止飞，进入下落状态
                        true
                    }

                    else -> false
                }
            }
    ) {
        // 绘制背景图片
        // 绘制第一张背景图片
        Image(
            painter = painterResource(id = backgroundModel.backgroundImageResId1),
            contentDescription = "Background 1",
            modifier = Modifier
                .offset(x = backgroundModel.x1.dp)
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 绘制第二张背景图片
        Image(
            painter = painterResource(id = backgroundModel.backgroundImageResId2),
            contentDescription = "Background 2",
            modifier = Modifier
                .offset(x = backgroundModel.x2.dp)
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 绘制云朵
        Image(
            painter = painterResource(id = cloudModel.cloudImageResId), // 获取云朵的图片资源
            contentDescription = "Cloud",
            modifier = Modifier
                .offset(x = cloudModel.x.dp, y = cloudModel.y.dp) // 根据云朵的坐标更新位置
                .size(100.dp, 50.dp) // 根据需要设置云朵的大小
        )


        // 图片展示小鸟，并使用 BirdModel 的 positionY 来设置垂直位置
        Image(
            painter = painterResource(id = birdModel.birdImageResId),
            contentDescription = "Bird",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(birdSize)  // 小鸟的大小
                .offset(y = birdModel.positionY.dp)  // 根据 BirdModel 的位置更新
                .rotate(birdModel.rotationAngle)  // 旋转小鸟
        )

        // 绘制管道
        for (pipe in pipes) {
            Image(
                painter = painterResource(id = pipe.pipeImageResId), // 获取管道的图片资源
                contentDescription = "Pipe",
                modifier = Modifier
                    .offset(x = pipe.x.dp, y = pipe.y.dp)
                    .size(pipeWidth, pipeHeight)
            )
        }

        for (pipe in pipes) {
            // 绘制中间管道
            val filledPipes = pipe.filledPipes
            for (filledPipe in filledPipes) {
                Image(
                    painter = painterResource(id = R.drawable.pipe), // 中间管道使用普通的管道图片
                    contentDescription = "Middle Pipe",
                    modifier = Modifier
                        .offset(x = filledPipe.x.dp, y = filledPipe.y.dp)
                        .size(pipeWidth, pipeHeight) // 根据需要设置中间管道的高度
                )
            }
        }
    }

    // 游戏启动时调用
    LaunchedEffect(Unit) {
        startGameLoop()  // 启动游戏循环
    }

    // 当游戏结束时显示 "Game Over" 并允许点击重新启动游戏
    if (isGameOver.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    // 点击后重新开始游戏
                    isGameOver.value = false
                    birdModel.reset()  // 重置小鸟状态
                    pipes.clear()  // 清空管道

                    // 重新启动游戏循环
                    startGameLoop()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.over),  // 假设有一个 "Game Over" 图标
                contentDescription = "Game Over",
                modifier = Modifier.size(200.dp)  // 设置图标大小
            )
        }
    }

}


@Preview(showBackground = true)
@Composable
fun BirdGamePreview() {
    FlappyBirdTheme {
        BirdGame()
    }
}
