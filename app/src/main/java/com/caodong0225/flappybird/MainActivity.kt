package com.caodong0225.flappybird

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
    with(LocalDensity.current) {
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


        // 使用协程不断更新小鸟的位置
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                while (true) {
                    birdModel.updatePosition()  // 更新位置
                    // 遍历管道列表，更新每个管道的位置
                    pipes.forEach { pipe ->
                        pipe.movement()
                    }
                    cloudModel.movement() // 更新云朵位置
                    backgroundModel.movement(screenWidth.toInt()) // 更新背景图片的位置

                    val cloudY = Random.nextInt(100, 200)
                    // 确保管道的可见性
                    // 检查每个管道的可见性并处理不可见的管道
                    val iterator = pipes.iterator()
                    while (iterator.hasNext()) {
                        val pipe = iterator.next()
                        if (!pipe.isVisible()) {
                            iterator.remove() // 删除不可见的管道
                        }
                    }
                    // 如果列表中已经没有管道了，则添加新的管道
                    if (pipes.size in 1..2) {
                        if(pipes[0].x< screenWidth/2)
                        {
                            // 生成随机的Y坐标
                            val topY = Random.nextInt(20, 400)
                            val bottomY = topY + Random.nextInt(200, 260)

                            // 新建一对新的管道并加入列表
                            pipes.add(PipeModel(0, screenHeight, screenWidth).apply {
                                setPosition(screenWidth.toInt(), topY)
                            })
                            pipes.add(PipeModel(2, screenHeight, screenWidth).apply {
                                setPosition(screenWidth.toInt(), bottomY)
                            })
                        }
                    }
                    else if(pipes.size < 2)
                    {
                        // 生成随机的Y坐标
                        val topY = Random.nextInt(20, 400)
                        val bottomY = topY + Random.nextInt(200, 260)

                        // 新建一对新的管道并加入列表
                        pipes.add(PipeModel(0, screenHeight, screenWidth).apply {
                            setPosition(screenWidth.toInt(), topY)
                        })
                        pipes.add(PipeModel(2, screenHeight, screenWidth).apply {
                            setPosition(screenWidth.toInt(), bottomY)
                        })
                    }
                    if (!cloudModel.isVisible()) {
                        cloudModel.setPosition(screenWidth.toInt(), cloudY)
                    }
                    delay(36L)  // 每 16 毫秒更新一次位置，大约相当于 60 帧每秒
                }
            }
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
