package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PetEntity
import com.example.ui.theme.HeartRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FloatingParticle(
    val id: Long = Random.nextLong(),
    val text: String = "❤️",
    val startX: Float,
    val startY: Float
)

enum class PetAnimationMode {
    IDLE,
    EATING,
    BATHING,
    SLEEPING,
    PLAYING
}

@Composable
fun PetModelView(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    petSize: Dp = 220.dp,
    animationMode: PetAnimationMode = PetAnimationMode.IDLE,
    onPetClicked: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val rotationAnim = remember { Animatable(0f) }
    val floatingHearts = remember { mutableStateListOf<FloatingParticle>() }

    // Idle breathing & floating
    val infiniteTransition = rememberInfiniteTransition(label = "pet_idle")
    val idleYOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_float"
    )
    val tailWiggle by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tail_wiggle"
    )

    // Trigger reaction when animationMode changes
    LaunchedEffect(animationMode) {
        when (animationMode) {
            PetAnimationMode.EATING -> {
                repeat(4) {
                    scaleAnim.animateTo(1.08f, tween(150))
                    scaleAnim.animateTo(0.96f, tween(150))
                }
                scaleAnim.animateTo(1f)
            }
            PetAnimationMode.PLAYING -> {
                rotationAnim.animateTo(8f, tween(150))
                rotationAnim.animateTo(-8f, tween(150))
                rotationAnim.animateTo(0f, tween(150))
                scaleAnim.animateTo(1.15f, spring(dampingRatio = 0.4f))
                scaleAnim.animateTo(1f)
            }
            PetAnimationMode.BATHING -> {
                repeat(3) {
                    rotationAnim.animateTo(5f, tween(120))
                    rotationAnim.animateTo(-5f, tween(120))
                }
                rotationAnim.animateTo(0f)
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier
            .size(petSize)
            .pointerInput(pet.id) {
                detectTapGestures { offset ->
                    onPetClicked()
                    coroutineScope.launch {
                        // Bouncy squash & stretch
                        scaleAnim.animateTo(0.88f, tween(80))
                        scaleAnim.animateTo(1.12f, spring(dampingRatio = 0.35f, stiffness = 400f))
                        scaleAnim.animateTo(1f, tween(120))
                    }
                    // Spawn heart particle
                    val particle = FloatingParticle(
                        text = if (animationMode == PetAnimationMode.SLEEPING) "💤" else "❤️",
                        startX = offset.x - 30f,
                        startY = offset.y - 40f
                    )
                    floatingHearts.add(particle)
                    coroutineScope.launch {
                        delay(900)
                        floatingHearts.remove(particle)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Shadow on the ground
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = 80f
                }
        ) {
            drawOval(
                color = Color(0x1A000000),
                topLeft = Offset(size.width * 0.22f, size.height * 0.82f),
                size = Size(size.width * 0.56f, size.height * 0.12f)
            )
        }

        // Main Animated Pet Body Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = if (animationMode == PetAnimationMode.SLEEPING) 10f else idleYOffset
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    rotationZ = rotationAnim.value
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f

            val primaryCol = Color(pet.primaryColor)
            val secondaryCol = Color(pet.secondaryColor)

            // 0. Draw Back Accessories / Wings / Cape (drawn behind pet body)
            drawPetBackAccessories(
                accessory = pet.accessory,
                centerX = centerX,
                centerY = centerY,
                canvasW = canvasW,
                canvasH = canvasH
            )

            // 1. Draw Tail
            drawPetTail(
                tailStyle = pet.tailStyle,
                centerX = centerX,
                centerY = centerY,
                canvasW = canvasW,
                canvasH = canvasH,
                primaryColor = primaryCol,
                secondaryColor = secondaryCol,
                wiggleAngle = tailWiggle
            )

            // 2. Draw Ears
            drawPetEars(
                bodyType = pet.baseBodyType,
                earStyle = pet.earStyle,
                centerX = centerX,
                centerY = centerY,
                canvasW = canvasW,
                canvasH = canvasH,
                primaryColor = primaryCol,
                secondaryColor = secondaryCol
            )

            // 3. Draw Body & Belly
            drawPetBody(
                bodyType = pet.baseBodyType,
                centerX = centerX,
                centerY = centerY,
                canvasW = canvasW,
                canvasH = canvasH,
                primaryColor = primaryCol,
                secondaryColor = secondaryCol
            )

            // 4. Draw Eyes & Expression
            drawPetFace(
                eyeStyle = if (animationMode == PetAnimationMode.SLEEPING) "HAPPY_CURVE" else pet.eyeStyle,
                bodyType = pet.baseBodyType,
                centerX = centerX,
                centerY = centerY,
                canvasW = canvasW,
                canvasH = canvasH,
                isEating = animationMode == PetAnimationMode.EATING,
                isSleeping = animationMode == PetAnimationMode.SLEEPING
            )

            // 5. Draw Front Accessories, Hats & Outfits
            drawPetAccessory(
                accessory = pet.accessory,
                centerX = centerX,
                centerY = centerY,
                canvasW = canvasW,
                canvasH = canvasH
            )
        }

        // Floating Hearts & Emojis on tap
        floatingHearts.forEach { particle ->
            FloatingParticleView(particle = particle)
        }

        // Sleeping Zzz or Bath Bubbles
        if (animationMode == PetAnimationMode.SLEEPING) {
            Text(
                text = "💤",
                fontSize = 28.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-24).dp, y = 16.dp)
            )
        } else if (animationMode == PetAnimationMode.BATHING) {
            Text(
                text = "🫧",
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 20.dp)
            )
            Text(
                text = "✨",
                fontSize = 22.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-20).dp, y = (-30).dp)
            )
        }
    }
}

@Composable
private fun FloatingParticleView(particle: FloatingParticle) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(particle.id) {
        launch {
            offsetY.animateTo(-90f, tween(800, easing = LinearEasing))
        }
        launch {
            delay(300)
            alpha.animateTo(0f, tween(500))
        }
    }

    Text(
        text = particle.text,
        fontSize = 22.sp,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (particle.startX - 60f).toInt(),
                    y = (particle.startY + offsetY.value).toInt()
                )
            }
            .graphicsLayer {
                this.alpha = alpha.value
            }
    )
}

private fun DrawScope.drawPetTail(
    tailStyle: String,
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float,
    primaryColor: Color,
    secondaryColor: Color,
    wiggleAngle: Float
) {
    when (tailStyle) {
        "PUFF_BALL" -> { // Rabbit / cute ball
            drawCircle(
                color = secondaryColor,
                radius = canvasW * 0.11f,
                center = Offset(centerX + canvasW * 0.28f, centerY + canvasH * 0.25f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = canvasW * 0.08f,
                center = Offset(centerX + canvasW * 0.28f, centerY + canvasH * 0.25f)
            )
        }
        "STAR_TAIL" -> { // Dragon / Star
            val tailPath = Path().apply {
                moveTo(centerX + canvasW * 0.22f, centerY + canvasH * 0.22f)
                quadraticTo(
                    centerX + canvasW * 0.44f, centerY + canvasH * 0.1f + wiggleAngle,
                    centerX + canvasW * 0.38f, centerY - canvasH * 0.02f
                )
                lineTo(centerX + canvasW * 0.32f, centerY + canvasH * 0.1f)
                close()
            }
            drawPath(tailPath, primaryColor)
            // Star on tip
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = canvasW * 0.06f,
                center = Offset(centerX + canvasW * 0.38f, centerY - canvasH * 0.02f)
            )
        }
        "CURLY" -> { // Shiba curly tail
            val path = Path().apply {
                moveTo(centerX + canvasW * 0.22f, centerY + canvasH * 0.2f)
                cubicTo(
                    centerX + canvasW * 0.42f, centerY + canvasH * 0.18f,
                    centerX + canvasW * 0.45f, centerY - canvasH * 0.05f + wiggleAngle,
                    centerX + canvasW * 0.28f, centerY - canvasH * 0.02f
                )
            }
            drawPath(path, primaryColor, style = Stroke(width = canvasW * 0.12f, cap = StrokeCap.Round))
        }
        else -> { // FLUFFY or SLENDER (Cat / Fox / Default)
            val path = Path().apply {
                moveTo(centerX + canvasW * 0.22f, centerY + canvasH * 0.22f)
                cubicTo(
                    centerX + canvasW * 0.46f, centerY + canvasH * 0.26f,
                    centerX + canvasW * 0.44f, centerY - canvasH * 0.08f + wiggleAngle,
                    centerX + canvasW * 0.3f, centerY - canvasH * 0.06f
                )
            }
            val strokeW = if (tailStyle == "FLUFFY") canvasW * 0.16f else canvasW * 0.09f
            drawPath(path, primaryColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            // Tail tip
            drawCircle(
                color = secondaryColor,
                radius = strokeW * 0.45f,
                center = Offset(centerX + canvasW * 0.3f, centerY - canvasH * 0.06f)
            )
        }
    }
}

private fun DrawScope.drawPetEars(
    bodyType: String,
    earStyle: String,
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val earY = centerY - canvasH * 0.22f
    val leftEarX = centerX - canvasW * 0.22f
    val rightEarX = centerX + canvasW * 0.22f

    when {
        earStyle == "HORNS" || bodyType == "DRAGON" -> {
            // Little cute dragon horns
            val leftHorn = Path().apply {
                moveTo(leftEarX, earY + canvasH * 0.05f)
                lineTo(leftEarX - canvasW * 0.12f, earY - canvasH * 0.18f)
                lineTo(leftEarX + canvasW * 0.06f, earY)
                close()
            }
            val rightHorn = Path().apply {
                moveTo(rightEarX, earY + canvasH * 0.05f)
                lineTo(rightEarX + canvasW * 0.12f, earY - canvasH * 0.18f)
                lineTo(rightEarX - canvasW * 0.06f, earY)
                close()
            }
            drawPath(leftHorn, Color(0xFFFFD54F))
            drawPath(rightHorn, Color(0xFFFFD54F))
        }
        earStyle == "DROOPY" || (bodyType == "RABBIT" && earStyle == "DROOPY") -> {
            // Floppy ears down the sides
            drawOval(
                color = primaryColor,
                topLeft = Offset(leftEarX - canvasW * 0.12f, earY - canvasH * 0.02f),
                size = Size(canvasW * 0.16f, canvasH * 0.36f)
            )
            drawOval(
                color = secondaryColor,
                topLeft = Offset(leftEarX - canvasW * 0.09f, earY + canvasH * 0.04f),
                size = Size(canvasW * 0.1f, canvasH * 0.24f)
            )
            drawOval(
                color = primaryColor,
                topLeft = Offset(rightEarX - canvasW * 0.04f, earY - canvasH * 0.02f),
                size = Size(canvasW * 0.16f, canvasH * 0.36f)
            )
            drawOval(
                color = secondaryColor,
                topLeft = Offset(rightEarX - canvasW * 0.01f, earY + canvasH * 0.04f),
                size = Size(canvasW * 0.1f, canvasH * 0.24f)
            )
        }
        earStyle == "LONG" || bodyType == "RABBIT" -> {
            // Tall upright bunny ears
            drawOval(
                color = primaryColor,
                topLeft = Offset(leftEarX - canvasW * 0.05f, earY - canvasH * 0.28f),
                size = Size(canvasW * 0.13f, canvasH * 0.32f)
            )
            drawOval(
                color = secondaryColor,
                topLeft = Offset(leftEarX - canvasW * 0.02f, earY - canvasH * 0.24f),
                size = Size(canvasW * 0.07f, canvasH * 0.24f)
            )
            drawOval(
                color = primaryColor,
                topLeft = Offset(rightEarX - canvasW * 0.08f, earY - canvasH * 0.28f),
                size = Size(canvasW * 0.13f, canvasH * 0.32f)
            )
            drawOval(
                color = secondaryColor,
                topLeft = Offset(rightEarX - canvasW * 0.05f, earY - canvasH * 0.24f),
                size = Size(canvasW * 0.07f, canvasH * 0.24f)
            )
        }
        earStyle == "ROUND" || bodyType == "PANDA" || bodyType == "CAPYBARA" -> {
            // Round bear/panda ears
            val earCol = if (bodyType == "PANDA") Color(0xFF37474F) else primaryColor
            drawCircle(
                color = earCol,
                radius = canvasW * 0.11f,
                center = Offset(leftEarX - canvasW * 0.04f, earY - canvasH * 0.04f)
            )
            drawCircle(
                color = secondaryColor,
                radius = canvasW * 0.06f,
                center = Offset(leftEarX - canvasW * 0.04f, earY - canvasH * 0.04f)
            )
            drawCircle(
                color = earCol,
                radius = canvasW * 0.11f,
                center = Offset(rightEarX + canvasW * 0.04f, earY - canvasH * 0.04f)
            )
            drawCircle(
                color = secondaryColor,
                radius = canvasW * 0.06f,
                center = Offset(rightEarX + canvasW * 0.04f, earY - canvasH * 0.04f)
            )
        }
        else -> { // POINTY (Cat, Dog, Fox)
            val leftEarPath = Path().apply {
                moveTo(leftEarX + canvasW * 0.08f, earY + canvasH * 0.05f)
                lineTo(leftEarX - canvasW * 0.08f, earY - canvasH * 0.18f)
                lineTo(leftEarX - canvasW * 0.14f, earY + canvasH * 0.02f)
                close()
            }
            val leftInner = Path().apply {
                moveTo(leftEarX + canvasW * 0.04f, earY + canvasH * 0.02f)
                lineTo(leftEarX - canvasW * 0.07f, earY - canvasH * 0.12f)
                lineTo(leftEarX - canvasW * 0.10f, earY)
                close()
            }
            val rightEarPath = Path().apply {
                moveTo(rightEarX - canvasW * 0.08f, earY + canvasH * 0.05f)
                lineTo(rightEarX + canvasW * 0.08f, earY - canvasH * 0.18f)
                lineTo(rightEarX + canvasW * 0.14f, earY + canvasH * 0.02f)
                close()
            }
            val rightInner = Path().apply {
                moveTo(rightEarX - canvasW * 0.04f, earY + canvasH * 0.02f)
                lineTo(rightEarX + canvasW * 0.07f, earY - canvasH * 0.12f)
                lineTo(rightEarX + canvasW * 0.10f, earY)
                close()
            }
            drawPath(leftEarPath, primaryColor)
            drawPath(leftInner, secondaryColor)
            drawPath(rightEarPath, primaryColor)
            drawPath(rightInner, secondaryColor)
        }
    }
}

private fun DrawScope.drawPetBody(
    bodyType: String,
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    // Round Chubby Body / Head Shape
    val headW = canvasW * 0.62f
    val headH = canvasH * 0.54f
    val headTop = centerY - headH * 0.46f

    // Soft gradient shading
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.92f)),
            center = Offset(centerX, centerY - canvasH * 0.06f),
            radius = canvasW * 0.4f
        ),
        topLeft = Offset(centerX - headW / 2f, headTop),
        size = Size(headW, headH)
    )

    // Cute chubby cheeks / belly patch
    val bellyW = headW * 0.66f
    val bellyH = headH * 0.42f
    drawOval(
        color = secondaryColor.copy(alpha = 0.88f),
        topLeft = Offset(centerX - bellyW / 2f, centerY + headH * 0.04f),
        size = Size(bellyW, bellyH)
    )

    // Soft blush on left & right cheeks
    val blushRadius = canvasW * 0.055f
    drawCircle(
        color = Color(0xFFFF8A80).copy(alpha = 0.45f),
        radius = blushRadius,
        center = Offset(centerX - canvasW * 0.18f, centerY + canvasH * 0.06f)
    )
    drawCircle(
        color = Color(0xFFFF8A80).copy(alpha = 0.45f),
        radius = blushRadius,
        center = Offset(centerX + canvasW * 0.18f, centerY + canvasH * 0.06f)
    )

    // Tiny Front Paws resting on belly
    val pawRadius = canvasW * 0.055f
    val pawY = centerY + headH * 0.36f
    drawCircle(
        color = secondaryColor,
        radius = pawRadius,
        center = Offset(centerX - canvasW * 0.12f, pawY)
    )
    drawCircle(
        color = secondaryColor,
        radius = pawRadius,
        center = Offset(centerX + canvasW * 0.12f, pawY)
    )
}

private fun DrawScope.drawPetFace(
    eyeStyle: String,
    bodyType: String,
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float,
    isEating: Boolean,
    isSleeping: Boolean
) {
    val eyeY = centerY - canvasH * 0.04f
    val eyeDist = canvasW * 0.11f
    val leftEyeX = centerX - eyeDist
    val rightEyeX = centerX + eyeDist

    if (isSleeping || eyeStyle == "HAPPY_CURVE") {
        // Happy sleeping crescent eyes ^_^
        val leftArc = Path().apply {
            moveTo(leftEyeX - canvasW * 0.04f, eyeY + canvasH * 0.01f)
            quadraticTo(leftEyeX, eyeY - canvasH * 0.035f, leftEyeX + canvasW * 0.04f, eyeY + canvasH * 0.01f)
        }
        val rightArc = Path().apply {
            moveTo(rightEyeX - canvasW * 0.04f, eyeY + canvasH * 0.01f)
            quadraticTo(rightEyeX, eyeY - canvasH * 0.035f, rightEyeX + canvasW * 0.04f, eyeY + canvasH * 0.01f)
        }
        drawPath(leftArc, Color(0xFF2C2420), style = Stroke(width = canvasW * 0.018f, cap = StrokeCap.Round))
        drawPath(rightArc, Color(0xFF2C2420), style = Stroke(width = canvasW * 0.018f, cap = StrokeCap.Round))
    } else if (eyeStyle == "WINK") {
        // Left eye winking ^, right eye big round sparkle
        val leftArc = Path().apply {
            moveTo(leftEyeX - canvasW * 0.04f, eyeY + canvasH * 0.01f)
            quadraticTo(leftEyeX, eyeY - canvasH * 0.035f, leftEyeX + canvasW * 0.04f, eyeY + canvasH * 0.01f)
        }
        drawPath(leftArc, Color(0xFF2C2420), style = Stroke(width = canvasW * 0.018f, cap = StrokeCap.Round))

        // Right eye big
        drawCircle(color = Color(0xFF2D2420), radius = canvasW * 0.045f, center = Offset(rightEyeX, eyeY))
        drawCircle(color = Color.White, radius = canvasW * 0.018f, center = Offset(rightEyeX - canvasW * 0.015f, eyeY - canvasH * 0.015f))
    } else { // SPARKLE or BIG_ROUND
        val eyeR = canvasW * 0.045f
        drawCircle(color = Color(0xFF2C2420), radius = eyeR, center = Offset(leftEyeX, eyeY))
        drawCircle(color = Color.White, radius = eyeR * 0.42f, center = Offset(leftEyeX - eyeR * 0.3f, eyeY - eyeR * 0.3f))
        drawCircle(color = Color.White, radius = eyeR * 0.22f, center = Offset(leftEyeX + eyeR * 0.3f, eyeY + eyeR * 0.3f))

        drawCircle(color = Color(0xFF2C2420), radius = eyeR, center = Offset(rightEyeX, eyeY))
        drawCircle(color = Color.White, radius = eyeR * 0.42f, center = Offset(rightEyeX - eyeR * 0.3f, eyeY - eyeR * 0.3f))
        drawCircle(color = Color.White, radius = eyeR * 0.22f, center = Offset(rightEyeX + eyeR * 0.3f, eyeY + eyeR * 0.3f))
    }

    // Tiny Cute Nose
    val noseY = centerY + canvasH * 0.02f
    val nosePath = Path().apply {
        moveTo(centerX - canvasW * 0.02f, noseY)
        lineTo(centerX + canvasW * 0.02f, noseY)
        lineTo(centerX, noseY + canvasH * 0.016f)
        close()
    }
    drawPath(nosePath, Color(0xFFFF7043))

    // Mouth
    val mouthY = noseY + canvasH * 0.018f
    if (isEating) {
        // Open chewing mouth :O
        drawCircle(
            color = Color(0xFFFF5252),
            radius = canvasW * 0.035f,
            center = Offset(centerX, mouthY + canvasH * 0.025f)
        )
    } else {
        // Cute :3 cat smile
        val leftSmile = Path().apply {
            moveTo(centerX, mouthY)
            quadraticTo(centerX - canvasW * 0.03f, mouthY + canvasH * 0.03f, centerX - canvasW * 0.05f, mouthY + canvasH * 0.015f)
        }
        val rightSmile = Path().apply {
            moveTo(centerX, mouthY)
            quadraticTo(centerX + canvasW * 0.03f, mouthY + canvasH * 0.03f, centerX + canvasW * 0.05f, mouthY + canvasH * 0.015f)
        }
        drawPath(leftSmile, Color(0xFF423832), style = Stroke(width = canvasW * 0.012f, cap = StrokeCap.Round))
        drawPath(rightSmile, Color(0xFF423832), style = Stroke(width = canvasW * 0.012f, cap = StrokeCap.Round))
    }

    // Whiskers for Cat / Fox
    if (bodyType == "CAT" || bodyType == "FOX") {
        val whiskerCol = Color(0x662D2420)
        val whiskerW = canvasW * 0.008f
        // Left whiskers
        drawLine(whiskerCol, Offset(centerX - canvasW * 0.16f, centerY + canvasH * 0.02f), Offset(centerX - canvasW * 0.28f, centerY + canvasH * 0.01f), strokeWidth = whiskerW)
        drawLine(whiskerCol, Offset(centerX - canvasW * 0.16f, centerY + canvasH * 0.05f), Offset(centerX - canvasW * 0.27f, centerY + canvasH * 0.06f), strokeWidth = whiskerW)
        // Right whiskers
        drawLine(whiskerCol, Offset(centerX + canvasW * 0.16f, centerY + canvasH * 0.02f), Offset(centerX + canvasW * 0.28f, centerY + canvasH * 0.01f), strokeWidth = whiskerW)
        drawLine(whiskerCol, Offset(centerX + canvasW * 0.16f, centerY + canvasH * 0.05f), Offset(centerX + canvasW * 0.27f, centerY + canvasH * 0.06f), strokeWidth = whiskerW)
    }
}

private fun DrawScope.drawPetBackAccessories(
    accessory: String,
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float
) {
    when (accessory) {
        "CAPE" -> {
            // Hero cape billowing behind
            val capeY = centerY + canvasH * 0.10f
            val capePath = Path().apply {
                moveTo(centerX - canvasW * 0.26f, capeY)
                quadraticTo(centerX - canvasW * 0.35f, capeY + canvasH * 0.28f, centerX - canvasW * 0.22f, capeY + canvasH * 0.32f)
                quadraticTo(centerX, capeY + canvasH * 0.26f, centerX + canvasW * 0.22f, capeY + canvasH * 0.32f)
                quadraticTo(centerX + canvasW * 0.35f, capeY + canvasH * 0.28f, centerX + canvasW * 0.26f, capeY)
                close()
            }
            drawPath(capePath, Color(0xFFE53935))
            drawPath(capePath, Color(0xFFC62828).copy(alpha = 0.4f), style = Stroke(width = canvasW * 0.015f))
        }
        "ANGEL_WINGS" -> {
            // Feathered wings fluttering on both sides
            val wingY = centerY + canvasH * 0.05f
            // Left wing
            val leftWing = Path().apply {
                moveTo(centerX - canvasW * 0.24f, wingY)
                cubicTo(
                    centerX - canvasW * 0.48f, wingY - canvasH * 0.15f,
                    centerX - canvasW * 0.52f, wingY + canvasH * 0.12f,
                    centerX - canvasW * 0.24f, wingY + canvasH * 0.18f
                )
                close()
            }
            // Right wing
            val rightWing = Path().apply {
                moveTo(centerX + canvasW * 0.24f, wingY)
                cubicTo(
                    centerX + canvasW * 0.48f, wingY - canvasH * 0.15f,
                    centerX + canvasW * 0.52f, wingY + canvasH * 0.12f,
                    centerX + canvasW * 0.24f, wingY + canvasH * 0.18f
                )
                close()
            }
            drawPath(leftWing, Color.White)
            drawPath(leftWing, Color(0xFFE1BEE7), style = Stroke(width = canvasW * 0.016f))
            drawPath(rightWing, Color.White)
            drawPath(rightWing, Color(0xFFE1BEE7), style = Stroke(width = canvasW * 0.016f))
        }
        "DEVIL_HORNS" -> {
            // Tiny devil wings behind
            val wingY = centerY + canvasH * 0.08f
            val wingL = Path().apply {
                moveTo(centerX - canvasW * 0.24f, wingY)
                lineTo(centerX - canvasW * 0.45f, wingY - canvasH * 0.08f)
                lineTo(centerX - canvasW * 0.38f, wingY + canvasH * 0.05f)
                lineTo(centerX - canvasW * 0.42f, wingY + canvasH * 0.12f)
                close()
            }
            val wingR = Path().apply {
                moveTo(centerX + canvasW * 0.24f, wingY)
                lineTo(centerX + canvasW * 0.45f, wingY - canvasH * 0.08f)
                lineTo(centerX + canvasW * 0.38f, wingY + canvasH * 0.05f)
                lineTo(centerX + canvasW * 0.42f, wingY + canvasH * 0.12f)
                close()
            }
            drawPath(wingL, Color(0xFF880E4F))
            drawPath(wingR, Color(0xFF880E4F))
        }
    }
}

private fun DrawScope.drawPetAccessory(
    accessory: String,
    centerX: Float,
    centerY: Float,
    canvasW: Float,
    canvasH: Float
) {
    when (accessory) {
        "BELL_COLLAR" -> {
            val collarY = centerY + canvasH * 0.17f
            // Red collar band
            drawOval(
                color = Color(0xFFFF5252),
                topLeft = Offset(centerX - canvasW * 0.22f, collarY - canvasH * 0.02f),
                size = Size(canvasW * 0.44f, canvasH * 0.05f)
            )
            // Golden bell
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = canvasW * 0.045f,
                center = Offset(centerX, collarY + canvasH * 0.02f)
            )
            drawCircle(
                color = Color(0xFF6D4C41),
                radius = canvasW * 0.012f,
                center = Offset(centerX, collarY + canvasH * 0.025f)
            )
        }
        "BOW_TIE" -> {
            val bowY = centerY + canvasH * 0.16f
            val bowCol = Color(0xFFE91E63)
            val leftWing = Path().apply {
                moveTo(centerX, bowY)
                lineTo(centerX - canvasW * 0.09f, bowY - canvasH * 0.04f)
                lineTo(centerX - canvasW * 0.09f, bowY + canvasH * 0.04f)
                close()
            }
            val rightWing = Path().apply {
                moveTo(centerX, bowY)
                lineTo(centerX + canvasW * 0.09f, bowY - canvasH * 0.04f)
                lineTo(centerX + canvasW * 0.09f, bowY + canvasH * 0.04f)
                close()
            }
            drawPath(leftWing, bowCol)
            drawPath(rightWing, bowCol)
            drawCircle(color = Color(0xFFFF80AB), radius = canvasW * 0.025f, center = Offset(centerX, bowY))
        }
        "BERET_HAT" -> {
            // Beret on top right ear
            val hatCenter = Offset(centerX + canvasW * 0.14f, centerY - canvasH * 0.22f)
            drawOval(
                color = Color(0xFF8D6E63),
                topLeft = Offset(hatCenter.x - canvasW * 0.14f, hatCenter.y - canvasH * 0.06f),
                size = Size(canvasW * 0.28f, canvasH * 0.12f)
            )
            // Tip ball
            drawCircle(color = Color(0xFF5D4037), radius = canvasW * 0.025f, center = Offset(hatCenter.x, hatCenter.y - canvasH * 0.05f))
        }
        "WIZARD_HAT" -> {
            // Mysterious wizard hat with star
            val hatBaseY = centerY - canvasH * 0.20f
            // Brim
            drawOval(
                color = Color(0xFF4A148C),
                topLeft = Offset(centerX - canvasW * 0.22f, hatBaseY - canvasH * 0.03f),
                size = Size(canvasW * 0.44f, canvasH * 0.08f)
            )
            // Cone
            val conePath = Path().apply {
                moveTo(centerX - canvasW * 0.14f, hatBaseY)
                quadraticTo(centerX - canvasW * 0.05f, hatBaseY - canvasH * 0.18f, centerX + canvasW * 0.06f, hatBaseY - canvasH * 0.26f)
                quadraticTo(centerX + canvasW * 0.08f, hatBaseY - canvasH * 0.15f, centerX + canvasW * 0.14f, hatBaseY)
                close()
            }
            drawPath(conePath, Color(0xFF6A1B9A))
            // Hat star
            drawCircle(Color(0xFFFFD54F), radius = canvasW * 0.03f, center = Offset(centerX + canvasW * 0.06f, hatBaseY - canvasH * 0.25f))
        }
        "CROWN" -> {
            // Royal golden crown
            val crownY = centerY - canvasH * 0.22f
            val crownPath = Path().apply {
                moveTo(centerX - canvasW * 0.14f, crownY)
                lineTo(centerX - canvasW * 0.16f, crownY - canvasH * 0.10f)
                lineTo(centerX - canvasW * 0.07f, crownY - canvasH * 0.04f)
                lineTo(centerX, crownY - canvasH * 0.12f)
                lineTo(centerX + canvasW * 0.07f, crownY - canvasH * 0.04f)
                lineTo(centerX + canvasW * 0.16f, crownY - canvasH * 0.10f)
                lineTo(centerX + canvasW * 0.14f, crownY)
                close()
            }
            drawPath(crownPath, Color(0xFFFFD700))
            drawPath(crownPath, Color(0xFFFFA000), style = Stroke(width = canvasW * 0.012f))
            // Jewels
            drawCircle(Color(0xFFE91E63), radius = canvasW * 0.016f, center = Offset(centerX, crownY - canvasH * 0.10f))
            drawCircle(Color(0xFF00E5FF), radius = canvasW * 0.012f, center = Offset(centerX - canvasW * 0.14f, crownY - canvasH * 0.08f))
            drawCircle(Color(0xFF00E5FF), radius = canvasW * 0.012f, center = Offset(centerX + canvasW * 0.14f, crownY - canvasH * 0.08f))
        }
        "STRAW_HAT" -> {
            val hatY = centerY - canvasH * 0.20f
            // Wide straw brim
            drawOval(
                color = Color(0xFFFFE082),
                topLeft = Offset(centerX - canvasW * 0.28f, hatY),
                size = Size(canvasW * 0.56f, canvasH * 0.09f)
            )
            // Straw crown
            drawOval(
                color = Color(0xFFFFD54F),
                topLeft = Offset(centerX - canvasW * 0.16f, hatY - canvasH * 0.08f),
                size = Size(canvasW * 0.32f, canvasH * 0.11f)
            )
            // Blue ribbon band
            drawOval(
                color = Color(0xFF0288D1),
                topLeft = Offset(centerX - canvasW * 0.16f, hatY - canvasH * 0.02f),
                size = Size(canvasW * 0.32f, canvasH * 0.035f)
            )
        }
        "PARTY_HAT" -> {
            val hatY = centerY - canvasH * 0.20f
            val partyCone = Path().apply {
                moveTo(centerX - canvasW * 0.10f, hatY)
                lineTo(centerX, hatY - canvasH * 0.20f)
                lineTo(centerX + canvasW * 0.10f, hatY)
                close()
            }
            drawPath(partyCone, Color(0xFFFF4081))
            // Pompom
            drawCircle(Color(0xFFFFEE58), radius = canvasW * 0.03f, center = Offset(centerX, hatY - canvasH * 0.20f))
        }
        "HEADPHONES" -> {
            val hpY = centerY - canvasH * 0.12f
            // Headband
            drawArc(
                color = Color(0xFF37474F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - canvasW * 0.30f, hpY - canvasH * 0.16f),
                size = Size(canvasW * 0.60f, canvasH * 0.24f),
                style = Stroke(width = canvasW * 0.025f)
            )
            // Left & right ear cups
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(centerX - canvasW * 0.34f, hpY - canvasH * 0.04f),
                size = Size(canvasW * 0.08f, canvasH * 0.12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(centerX + canvasW * 0.26f, hpY - canvasH * 0.04f),
                size = Size(canvasW * 0.08f, canvasH * 0.12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
        }
        "FLOWER_PIN" -> {
            // Sakura hairpin on left forehead
            val fCenter = Offset(centerX - canvasW * 0.18f, centerY - canvasH * 0.16f)
            val petalRadius = canvasW * 0.035f
            for (i in 0..4) {
                val angle = (i * 72f) * (Math.PI / 180.0).toFloat()
                val px = fCenter.x + kotlin.math.cos(angle) * petalRadius
                val py = fCenter.y + kotlin.math.sin(angle) * petalRadius
                drawCircle(Color(0xFFF48FB1), radius = petalRadius * 0.75f, center = Offset(px, py))
            }
            drawCircle(Color(0xFFFFEB3B), radius = petalRadius * 0.5f, center = fCenter)
        }
        "SUNGLASSES" -> {
            val glassesY = centerY - canvasH * 0.04f
            val lensW = canvasW * 0.11f
            val lensH = canvasH * 0.07f
            // Left lens
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(centerX - canvasW * 0.15f, glassesY - lensH / 2f),
                size = Size(lensW, lensH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            // Right lens
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(centerX + canvasW * 0.04f, glassesY - lensH / 2f),
                size = Size(lensW, lensH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            // Bridge
            drawLine(
                color = Color(0xFFD4AF37),
                start = Offset(centerX - canvasW * 0.04f, glassesY),
                end = Offset(centerX + canvasW * 0.04f, glassesY),
                strokeWidth = canvasW * 0.015f
            )
        }
        "SCARF" -> {
            val scarfY = centerY + canvasH * 0.14f
            // Scarf loop around neck
            drawOval(
                color = Color(0xFFD32F2F),
                topLeft = Offset(centerX - canvasW * 0.24f, scarfY),
                size = Size(canvasW * 0.48f, canvasH * 0.08f)
            )
            // Scarf dangling end
            drawRoundRect(
                color = Color(0xFFB71C1C),
                topLeft = Offset(centerX + canvasW * 0.06f, scarfY + canvasH * 0.03f),
                size = Size(canvasW * 0.09f, canvasH * 0.14f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
        "PEARL_NECKLACE" -> {
            val nY = centerY + canvasH * 0.18f
            for (i in -4..4) {
                val nx = centerX + i * (canvasW * 0.042f)
                val ny = nY + (i * i) * (canvasH * 0.003f)
                drawCircle(Color.White, radius = canvasW * 0.016f, center = Offset(nx, ny))
                drawCircle(Color(0xFFE0E0E0), radius = canvasW * 0.016f, center = Offset(nx, ny), style = Stroke(width = 1.5f))
            }
        }
        "SAILOR_SUIT" -> {
            // Blue sailor collar on upper chest
            val chestY = centerY + canvasH * 0.16f
            val sailorCollar = Path().apply {
                moveTo(centerX - canvasW * 0.20f, chestY)
                lineTo(centerX - canvasW * 0.24f, chestY + canvasH * 0.14f)
                lineTo(centerX - canvasW * 0.04f, chestY + canvasH * 0.08f)
                lineTo(centerX + canvasW * 0.04f, chestY + canvasH * 0.08f)
                lineTo(centerX + canvasW * 0.24f, chestY + canvasH * 0.14f)
                lineTo(centerX + canvasW * 0.20f, chestY)
                close()
            }
            drawPath(sailorCollar, Color(0xFF1565C0))
            // Red tie
            drawCircle(Color(0xFFE53935), radius = canvasW * 0.024f, center = Offset(centerX, chestY + canvasH * 0.09f))
        }
        "SWEATER" -> {
            // Knit cozy sweater belly covering
            val swY = centerY + canvasH * 0.14f
            drawOval(
                color = Color(0xFFFFB300),
                topLeft = Offset(centerX - canvasW * 0.24f, swY),
                size = Size(canvasW * 0.48f, canvasH * 0.20f)
            )
            // Cable pattern lines
            drawLine(Color(0xFFFFA000), Offset(centerX - canvasW * 0.08f, swY + canvasH * 0.03f), Offset(centerX - canvasW * 0.08f, swY + canvasH * 0.17f), strokeWidth = canvasW * 0.012f)
            drawLine(Color(0xFFFFA000), Offset(centerX + canvasW * 0.08f, swY + canvasH * 0.03f), Offset(centerX + canvasW * 0.08f, swY + canvasH * 0.17f), strokeWidth = canvasW * 0.012f)
        }
        "HOODIE" -> {
            // Streetwear purple hoodie
            val hY = centerY + canvasH * 0.15f
            drawOval(
                color = Color(0xFF7E57C2),
                topLeft = Offset(centerX - canvasW * 0.25f, hY),
                size = Size(canvasW * 0.50f, canvasH * 0.19f)
            )
            // Drawstrings
            drawLine(Color.White, Offset(centerX - canvasW * 0.04f, hY + canvasH * 0.02f), Offset(centerX - canvasW * 0.04f, hY + canvasH * 0.12f), strokeWidth = canvasW * 0.01f)
            drawLine(Color.White, Offset(centerX + canvasW * 0.04f, hY + canvasH * 0.02f), Offset(centerX + canvasW * 0.04f, hY + canvasH * 0.12f), strokeWidth = canvasW * 0.01f)
        }
        "HALO" -> {
            val haloY = centerY - canvasH * 0.32f
            drawOval(
                color = Color(0xFFFFD54F),
                topLeft = Offset(centerX - canvasW * 0.18f, haloY),
                size = Size(canvasW * 0.36f, canvasH * 0.08f),
                style = Stroke(width = canvasW * 0.022f)
            )
        }
    }
}
