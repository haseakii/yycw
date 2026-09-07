package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.FoodItem
import com.example.data.model.PetEntity
import com.example.ui.components.PetAnimationMode
import com.example.ui.components.PetModelView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class ARInteractionState {
    IDLE,
    PETTING,
    FEEDING,
    PLAYING
}

@Composable
fun ARScreen(
    pet: PetEntity?,
    foodList: List<FoodItem>,
    onFeedPet: (FoodItem) -> Unit,
    onPlayPet: () -> Unit,
    onCuddlePet: () -> Unit,
    onCloseAR: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (pet == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到当前萌宠", color = Color.White)
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // Live Real-World Camera Feed Viewport
            CameraPreviewView(modifier = Modifier.fillMaxSize())
        } else {
            // Fallback AR Room / Permission prompt
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "开启现实世界AR空间",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "将手机对准桌面或地面，即可把「${pet.name}」带入真实环境，随时抚摸、投喂与玩耍！",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("授权开启相机 AR 体验")
                    }
                }
            }
        }

        // Overlay: Interactive AR World Layer
        ARInteractiveOverlay(
            pet = pet,
            foodList = foodList,
            onFeedPet = onFeedPet,
            onPlayPet = onPlayPet,
            onCuddlePet = onCuddlePet,
            onCloseAR = onCloseAR,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CameraPreviewView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (e: Exception) {
                    // Safe camera fallback
                }
            }, ContextCompat.getMainExecutor(context))
        },
        modifier = modifier
    )
}

@Composable
private fun ARInteractiveOverlay(
    pet: PetEntity,
    foodList: List<FoodItem>,
    onFeedPet: (FoodItem) -> Unit,
    onPlayPet: () -> Unit,
    onCuddlePet: () -> Unit,
    onCloseAR: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Pet transform in AR space
    var petOffsetX by remember { mutableFloatStateOf(0f) }
    var petOffsetY by remember { mutableFloatStateOf(0f) }
    var petScale by remember { mutableFloatStateOf(1.0f) }
    var petRotation by remember { mutableFloatStateOf(0f) }

    // Floating interaction & speech reaction
    var arSpeech by remember { mutableStateOf("在真实世界看到主人啦！✨") }
    var arMode by remember { mutableStateOf(ARInteractionState.IDLE) }
    var animationMode by remember { mutableStateOf(PetAnimationMode.IDLE) }

    // Heart particles animation triggered on petting
    var showHearts by remember { mutableStateOf(false) }

    // Food picker tray visibility
    var showFoodTray by remember { mutableStateOf(false) }

    // Floating bounce effect for realism
    val bounceAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        bounceAnim.animateTo(
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Shadow pulse
    val shadowAlpha = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        shadowAlpha.animateTo(
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val screenW = maxWidth
        val screenH = maxHeight

        // Center pet initially if not positioned
        val baseSize = 220.dp

        // Top Navigation & HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AR Status Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AR 现实空间已锚定",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Close AR Button
            IconButton(
                onClick = onCloseAR,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .testTag("close_ar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "退出AR",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Floating Speech Bubble over Pet
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset {
                    IntOffset(
                        petOffsetX.roundToInt(),
                        (petOffsetY - 160f + bounceAnim.value).roundToInt()
                    )
                }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFEADDFF))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🐾 ",
                        fontSize = 14.sp
                    )
                    Text(
                        text = arSpeech,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                }
            }
        }

        // Interactive Pet Model anchored in AR
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset {
                    IntOffset(
                        petOffsetX.roundToInt(),
                        (petOffsetY + bounceAnim.value).roundToInt()
                    )
                }
                .graphicsLayer {
                    scaleX = petScale
                    scaleY = petScale
                    rotationZ = petRotation
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        petOffsetX += dragAmount.x
                        petOffsetY += dragAmount.y
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Pet cuddle / interact
                            onCuddlePet()
                            arMode = ARInteractionState.PETTING
                            animationMode = PetAnimationMode.PLAYING
                            arSpeech = "${pet.name} 在现实世界蹭蹭你的指尖~ 💖"
                            showHearts = true
                            coroutineScope.launch {
                                delay(2000)
                                showHearts = false
                                arMode = ARInteractionState.IDLE
                                animationMode = PetAnimationMode.IDLE
                            }
                        }
                    )
                }
                .testTag("ar_pet_anchor")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Heart particles overlay
                AnimatedVisibility(
                    visible = showHearts,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text("💖", fontSize = 24.sp)
                        Text("✨", fontSize = 20.sp)
                        Text("🌸", fontSize = 24.sp)
                    }
                }

                // Pet Model View
                PetModelView(
                    pet = pet,
                    petSize = baseSize,
                    animationMode = animationMode,
                    modifier = Modifier.testTag("ar_pet_model_view")
                )

                // Ground Contact Shadow Projection (makes model grounded in reality)
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 24.dp)
                        .graphicsLayer {
                            scaleX = 1f + bounceAnim.value / 25f
                            alpha = shadowAlpha.value
                        }
                        .clip(CircleShape)
                        .background(Color.Black)
                )
            }
        }

        // Bottom Controls: AR Interactions Dock (Feed, Pet, Play, Scale)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Food Selection Drawer if open
            AnimatedVisibility(
                visible = showFoodTray,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍖 现实AR空间投喂美味", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { showFoodTray = false }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(foodList) { food ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF3EDF7),
                                    border = BorderStroke(1.dp, Color(0xFFE8DEF8)),
                                    modifier = Modifier.clickable {
                                        onFeedPet(food)
                                        showFoodTray = false
                                        arMode = ARInteractionState.FEEDING
                                        animationMode = PetAnimationMode.EATING
                                        arSpeech = "大口吃掉了 ${food.name}！好满足~ 😋"
                                        coroutineScope.launch {
                                            delay(2500)
                                            arMode = ARInteractionState.IDLE
                                            animationMode = PetAnimationMode.IDLE
                                        }
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = food.iconEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(text = food.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text(text = "+${food.hungerRestore}饱腹", fontSize = 10.sp, color = Color(0xFF6750A4))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AR Main Interaction Dock Bar
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pet / Cuddle button
                    ARActionButton(
                        emoji = "👋",
                        label = "抚摸亲近",
                        onClick = {
                            onCuddlePet()
                            arMode = ARInteractionState.PETTING
                            animationMode = PetAnimationMode.PLAYING
                            showHearts = true
                            arSpeech = "${pet.name} 咕噜咕噜~ 享受主人的轻抚！"
                            coroutineScope.launch {
                                delay(2000)
                                showHearts = false
                                arMode = ARInteractionState.IDLE
                                animationMode = PetAnimationMode.IDLE
                            }
                        }
                    )

                    // Feed button
                    ARActionButton(
                        emoji = "🍖",
                        label = "现实投喂",
                        onClick = { showFoodTray = !showFoodTray }
                    )

                    // Play ball button
                    ARActionButton(
                        emoji = "🎾",
                        label = "抛球玩耍",
                        onClick = {
                            onPlayPet()
                            arMode = ARInteractionState.PLAYING
                            animationMode = PetAnimationMode.PLAYING
                            arSpeech = "接住主人抛出的球啦！太好玩了！🏃💨"
                            // Fun quick animation hop
                            coroutineScope.launch {
                                petOffsetY -= 40f
                                delay(250)
                                petOffsetY += 40f
                                delay(2000)
                                arMode = ARInteractionState.IDLE
                                animationMode = PetAnimationMode.IDLE
                            }
                        }
                    )

                    // Reset position button
                    ARActionButton(
                        emoji = "🎯",
                        label = "重置居中",
                        onClick = {
                            petOffsetX = 0f
                            petOffsetY = 0f
                            petScale = 1.0f
                            petRotation = 0f
                            arSpeech = "重置到画面中央啦！"
                        }
                    )

                    // Scale toggle (+ / -)
                    ARActionButton(
                        emoji = if (petScale >= 1.2f) "🔍" else "🔎",
                        label = if (petScale >= 1.2f) "缩小" else "放大",
                        onClick = {
                            petScale = if (petScale >= 1.2f) 0.85f else (petScale + 0.25f)
                        }
                    )
                }
            }

            // Interactive hint
            Text(
                text = "💡 单指拖动可调整宠物在现实中的位置 · 点击宠物进行抚摸互动",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ARActionButton(
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
