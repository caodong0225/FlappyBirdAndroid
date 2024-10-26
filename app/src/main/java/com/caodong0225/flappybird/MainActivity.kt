package com.caodong0225.flappybird

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.caodong0225.flappybird.Repository.GameRepository
import com.caodong0225.flappybird.model.BackgroundModel
import com.caodong0225.flappybird.model.BirdModel
import com.caodong0225.flappybird.model.CloudModel
import com.caodong0225.flappybird.model.PipeModel
import com.caodong0225.flappybird.record.GameRecord
import com.caodong0225.flappybird.ui.theme.FlappyBirdTheme
import com.caodong0225.flappybird.util.ScreenUtils
import com.caodong0225.flappybird.view.History
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    private lateinit var locationClient: AMapLocationClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AMapLocationClient.updatePrivacyShow(this, true, true)
        AMapLocationClient.updatePrivacyAgree(this, true)
        locationClient = AMapLocationClient(this)
        locationClient.setLocationOption(getDefaultOption())
        locationClient.setLocationListener {}
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        locationClient.startLocation()
        setContent {
            FlappyBirdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 设置导航
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "bird_game") {
                        composable("bird_game") {
                            BirdGame(locationClient, androidId)
                        }
                        composable("menu") {
                            History() // 新视图
                        }
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        locationClient.stopLocation()
        locationClient.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        locationClient.disableBackgroundLocation(true)
    }
    private fun getDefaultOption(): AMapLocationClientOption {
        val mOption = AMapLocationClientOption()
        mOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.isGpsFirst = true //可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.httpTimeOut = 30000 //可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.interval = 2000 //可选，设置定位间隔。默认为2秒
        mOption.isNeedAddress = true //可选，设置是否返回逆地理地址信息。默认是true
        mOption.isOnceLocation = false //可选，设置是否单次定位。默认是false
        mOption.isOnceLocationLatest = false //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP) //可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.isSensorEnable = false //可选，设置是否使用传感器。默认是false
        mOption.isWifiScan = true //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.isLocationCacheEnable = true //可选，设置是否使用缓存定位，默认为true
        return mOption
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BirdGame(locationClient : AMapLocationClient,
             androidId : String,
    modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val screenHeight = ScreenUtils.getScreenHeightDp(context)
    val screenWidth = ScreenUtils.getScreenWidthDp(context)
    val birdSize = 80.dp // 鸟的大小
    val birdSizePx: Float
    val pipeWidth = 80.dp
    val pipeHeight = 100.dp
    val density = LocalDensity.current
    val birdXPos = 40.dp
    var isAdded = true // 判断小鸟的本次管道分数是否已经添加
    var startTime = System.currentTimeMillis()
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

    var gameScore = remember { mutableStateOf(0) }


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
                    isAdded = false
                }

                if(pipes[0].x < 40 && !isAdded )
                {
                    gameScore.value += 1
                    isAdded = true
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
                .offset(x = birdXPos,y = birdModel.positionY.dp)  // 根据 BirdModel 的位置更新
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

        // 显示游戏分数的文本框
        Text(
            text = "Score: ${gameScore.value}",  // 使用当前分数值
            fontSize = 32.sp,  // 设置字体大小
            color = Color.White,  // 设置文本颜色
            modifier = Modifier
                .align(Alignment.TopCenter)  // 水平居中，垂直靠顶部
                .padding(top = 50.dp)  // 设置偏上距离
        )

    }
    // 放置独立的IconButton
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        IconButton(
            onClick = {
                val intent = Intent(context, History::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .size(60.dp)
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu")
        }
    }

    // 游戏启动时调用
    LaunchedEffect(Unit) {
        startGameLoop()  // 启动游戏循环
    }


    // 当游戏结束时显示 "Game Over" 并允许点击重新启动游戏
    if (isGameOver.value) {
        val currentLocation = locationClient.lastKnownLocation;
        // println("currentLocation: ${currentLocation.toStr()}")
        val longitude = currentLocation.longitude
        val latitude = currentLocation.latitude
        val location = currentLocation.address
        val score = gameScore.value
        val appId = androidId

        val dbHelper = GameRepository(context)
        val gameRecord = GameRecord(appId, score, System.currentTimeMillis(),
            location, latitude.toString(), longitude.toString(), System.currentTimeMillis() - startTime, "")
        dbHelper.insertGameRecord(gameRecord)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    // 点击后重新开始游戏
                    isGameOver.value = false
                    birdModel.reset()  // 重置小鸟状态
                    pipes.clear()  // 清空管道
                    gameScore.value = 0  // 重置分数
                    startTime = System.currentTimeMillis() // 重置开始时间
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
