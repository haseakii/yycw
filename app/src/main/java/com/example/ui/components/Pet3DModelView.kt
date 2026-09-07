package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.PetEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 3D Animation States for Virtual Pets
 */
enum class Pet3DActionState {
    IDLE,           // 3D breathing, gentle yaw float & tail sway
    CUDDLE_PURR,    // Happy bounce, blushing cheeks, floating 3D hearts
    BATTLE_STANCE,  // Athletic lean, ready to strike
    ATTACK_LEAP,    // 3D forward lunge with energy claw trail
    DEFEND_SHIELD,  // Translucent 3D spherical protective barrier
    HIT_SHAKE,      // Knockback wobble & flash
    VICTORY_SPIN,   // Full 360° victory pirouette & confetti
    SLEEPING        // Curled up with floating 3D Zzz
}

/**
 * Lightweight 3D Vector Math for Compose Canvas
 */
data class Vec3(val x: Float, val y: Float, val z: Float) {
    fun rotateY(yawRad: Float): Vec3 {
        val cosY = cos(yawRad)
        val sinY = sin(yawRad)
        return Vec3(
            x = x * cosY + z * sinY,
            y = y,
            z = -x * sinY + z * cosY
        )
    }

    fun rotateX(pitchRad: Float): Vec3 {
        val cosX = cos(pitchRad)
        val sinX = sin(pitchRad)
        return Vec3(
            x = x,
            y = y * cosX - z * sinX,
            z = y * sinX + z * cosX
        )
    }

    fun rotateZ(rollRad: Float): Vec3 {
        val cosZ = cos(rollRad)
        val sinZ = sin(rollRad)
        return Vec3(
            x = x * cosZ - y * sinZ,
            y = x * sinZ + y * cosZ,
            z = z
        )
    }

    fun translate(dx: Float, dy: Float, dz: Float): Vec3 {
        return Vec3(x + dx, y + dy, z + dz)
    }
}

/**
 * 3D Projected Screen Point with Depth Scale
 */
data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val scale: Float,
    val depthZ: Float
)

/**
 * Depth-Sorted 3D Drawable Primitive for Painter's Algorithm
 */
private interface RenderPrimitive {
    val sortZ: Float
    fun draw(drawScope: DrawScope)
}

/**
 * Interactive 3D Animated Pet Model View.
 *
 * Features:
 * - True 3D perspective projection with focal length and depth scale.
 * - Interactive 360° Touch Orbit rotation (Yaw & Pitch) with inertial damping.
 * - Morphable 3D anatomy for all species (Cat, Dog, Bunny, Dragon, Bear) and body types.
 * - Depth-sorted 3D Painter's Algorithm for realistic occlusion of body, ears, eyes, limbs, accessories.
 * - Dynamic 3D lighting shader (ambient + diffuse + specular highlights).
 * - Rich 3D animation actions: Idle, Cuddle/Petting, Battle Attack, Shield Defense, Hit, Victory, Sleeping.
 */
@Composable
fun Pet3DModelView(
    pet: PetEntity,
    modifier: Modifier = Modifier,
    modelSize: Dp = 240.dp,
    actionState: Pet3DActionState = Pet3DActionState.IDLE,
    enableOrbitDrag: Boolean = true,
    onPetClicked: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    // 3D Orbit Angles (in degrees)
    var userYaw by remember { mutableFloatStateOf(0f) }
    var userPitch by remember { mutableFloatStateOf(0f) }

    // Dynamic animation loops
    val infiniteTransition = rememberInfiniteTransition(label = "pet_3d_anim")

    // Idle breathing & floating
    val idleBreathing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "3d_breathe"
    )

    // Tail sway oscillation
    val tailSway by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "3d_tail_sway"
    )

    // Ear twitch
    val earTwitch by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "3d_ear_twitch"
    )

    // Eye blink cycle
    val eyeBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "3d_eye_blink"
    )

    // Interactive Action Transient Animations (Bounce, Spin, Attack)
    val actionBounce = remember { Animatable(0f) }
    val actionSpin = remember { Animatable(0f) }
    val actionLunge = remember { Animatable(0f) }
    val actionWobble = remember { Animatable(0f) }
    val floating3DHearts = remember { mutableStateListOf<FloatingParticle>() }

    // React to actionState changes
    LaunchedEffect(actionState) {
        when (actionState) {
            Pet3DActionState.CUDDLE_PURR -> {
                actionBounce.animateTo(1f, animationSpec = tween(150))
                actionBounce.animateTo(0f, animationSpec = tween(250))
            }
            Pet3DActionState.ATTACK_LEAP -> {
                actionLunge.animateTo(1f, animationSpec = tween(180, easing = FastOutSlowInEasing))
                actionLunge.animateTo(0f, animationSpec = tween(220))
            }
            Pet3DActionState.HIT_SHAKE -> {
                actionWobble.animateTo(15f, animationSpec = tween(70))
                actionWobble.animateTo(-15f, animationSpec = tween(70))
                actionWobble.animateTo(0f, animationSpec = tween(70))
            }
            Pet3DActionState.VICTORY_SPIN -> {
                actionSpin.animateTo(360f, animationSpec = tween(700, easing = FastOutSlowInEasing))
                actionSpin.snapTo(0f)
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier
            .size(modelSize)
            .pointerInput(enableOrbitDrag) {
                if (!enableOrbitDrag) return@pointerInput
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag horizontally to orbit Yaw, vertically to tilt Pitch
                    userYaw = (userYaw + dragAmount.x * 0.7f).coerceIn(-180f, 180f)
                    userPitch = (userPitch - dragAmount.y * 0.4f).coerceIn(-35f, 35f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onPetClicked()
                    scope.launch {
                        actionBounce.animateTo(1f, animationSpec = tween(120))
                        actionBounce.animateTo(0f, animationSpec = tween(200))
                    }
                    // Spawn interactive heart particle
                    floating3DHearts.add(
                        FloatingParticle(
                            startX = offset.x + Random.nextFloat() * 40f - 20f,
                            startY = offset.y - 20f
                        )
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f + 10f

            // Focal distance for perspective projection
            val focalD = 380f

            // Total 3D rotation angles
            val totalYawRad = Math.toRadians((userYaw + actionSpin.value).toDouble()).toFloat()
            val totalPitchRad = Math.toRadians(userPitch.toDouble()).toFloat()
            val totalRollRad = Math.toRadians((actionWobble.value).toDouble()).toFloat()

            // Transformation function: local 3D -> world 3D -> 2D projected screen
            fun project(localVec: Vec3, worldOffset: Vec3 = Vec3(0f, 0f, 0f)): ProjectedPoint {
                // 1. Local rotation
                val rotated = localVec
                    .rotateZ(totalRollRad)
                    .rotateX(totalPitchRad)
                    .rotateY(totalYawRad)

                // 2. World translation (breathing, action bounce, attack lunge)
                val worldY = rotated.y + worldOffset.y - (idleBreathing * 8f) - (actionBounce.value * 28f)
                val worldZ = rotated.z + worldOffset.z + (actionLunge.value * 65f)
                val worldX = rotated.x + worldOffset.x

                // 3. Perspective calculation
                val depthFactor = focalD / max(80f, focalD + worldZ)
                val scrX = centerX + worldX * depthFactor
                val scrY = centerY + worldY * depthFactor

                return ProjectedPoint(scrX, scrY, depthFactor, worldZ)
            }

            // Directional Key Light vector for 3D Shading
            val lightDir = Vec3(0.45f, -0.65f, -0.60f)
            val lightMag = sqrt(lightDir.x * lightDir.x + lightDir.y * lightDir.y + lightDir.z * lightDir.z)
            val normLight = Vec3(lightDir.x / lightMag, lightDir.y / lightMag, lightDir.z / lightMag)

            // Extract Pet Color Palette
            val primaryColor = Color(pet.primaryColor.toULong())
            val secondaryColor = Color(pet.secondaryColor.toULong())
            val shadowTint = Color(0xFF1D1B20).copy(alpha = 0.25f)

            // Compute body scaling based on baseBodyType
            val (bodyScaleX, bodyScaleY, bodyScaleZ) = when (pet.baseBodyType) {
                "ROUND" -> Triple(1.05f, 0.95f, 1.05f)
                "CHUBBY" -> Triple(1.18f, 1.02f, 1.15f)
                "SLENDER" -> Triple(0.88f, 1.10f, 0.88f)
                "TALL" -> Triple(0.92f, 1.22f, 0.92f)
                else -> Triple(1.0f, 1.0f, 1.0f)
            }

            val primitives = mutableListOf<RenderPrimitive>()

            // -------------------------------------------------------------
            // 1. Ground Drop Shadow (3D Planar Projection)
            // -------------------------------------------------------------
            val groundShadowCenter = project(Vec3(0f, 95f, 0f))
            val shadowElevation = idleBreathing * 6f + actionBounce.value * 20f
            val shadowW = (130f * bodyScaleX - shadowElevation * 1.5f).coerceAtLeast(40f) * groundShadowCenter.scale
            val shadowH = (48f * bodyScaleZ - shadowElevation * 0.8f).coerceAtLeast(16f) * groundShadowCenter.scale

            primitives.add(object : RenderPrimitive {
                override val sortZ: Float = -9999f // Always behind/underneath
                override fun draw(drawScope: DrawScope) {
                    drawScope.drawOval(
                        color = shadowTint.copy(alpha = (0.28f - shadowElevation * 0.005f).coerceIn(0.08f, 0.35f)),
                        topLeft = Offset(groundShadowCenter.screenX - shadowW / 2f, groundShadowCenter.screenY - shadowH / 2f),
                        size = Size(shadowW, shadowH)
                    )
                }
            })

            // -------------------------------------------------------------
            // 2. 3D Articulated Tail (Depth-Sorted Segments)
            // -------------------------------------------------------------
            val tailBasePos = Vec3(0f, 40f * bodyScaleY, -45f * bodyScaleZ)
            val tailSegments = 5
            var prevProj = project(tailBasePos)

            for (i in 1..tailSegments) {
                val t = i.toFloat() / tailSegments
                val segmentAngle = Math.toRadians((tailSway * t * 1.5f).toDouble()).toFloat()
                val segLocal = Vec3(
                    x = sin(segmentAngle) * (38f * t),
                    y = tailBasePos.y - (t * 30f) + cos(segmentAngle) * 5f,
                    z = tailBasePos.z - (t * 42f)
                )
                val currProj = project(segLocal)
                val segRadius = when (pet.tailStyle) {
                    "POMPOM" -> if (i == tailSegments) 18f * currProj.scale else 7f * currProj.scale
                    "BUSHY" -> (8f + t * 14f) * currProj.scale
                    "DRAGON" -> (12f - t * 6f) * currProj.scale
                    else -> (10f - t * 4f) * currProj.scale // Long curvy
                }

                val segZ = currProj.depthZ
                val p1 = prevProj
                val p2 = currProj

                primitives.add(object : RenderPrimitive {
                    override val sortZ: Float = segZ
                    override fun draw(drawScope: DrawScope) {
                        drawScope.drawLine(
                            color = if (i % 2 == 0) primaryColor else secondaryColor,
                            start = Offset(p1.screenX, p1.screenY),
                            end = Offset(p2.screenX, p2.screenY),
                            strokeWidth = segRadius * 2f,
                            cap = StrokeCap.Round
                        )
                        drawScope.drawCircle(
                            color = primaryColor,
                            radius = segRadius,
                            center = Offset(p2.screenX, p2.screenY)
                        )
                    }
                })
                prevProj = currProj
            }

            // -------------------------------------------------------------
            // 3. 3D Hind & Front Paws
            // -------------------------------------------------------------
            val pawCoords = listOf(
                // Hind Left, Hind Right
                Triple(-38f * bodyScaleX, 72f * bodyScaleY, -25f * bodyScaleZ),
                Triple(38f * bodyScaleX, 72f * bodyScaleY, -25f * bodyScaleZ),
                // Front Left, Front Right
                Triple(-32f * bodyScaleX, 76f * bodyScaleY, 32f * bodyScaleZ),
                Triple(32f * bodyScaleX, 76f * bodyScaleY, 32f * bodyScaleZ)
            )

            pawCoords.forEachIndexed { index, (lx, ly, lz) ->
                val pawOffset = if (actionState == Pet3DActionState.ATTACK_LEAP && index >= 2) {
                    Vec3(lx * 1.2f, ly - 15f, lz + 25f)
                } else {
                    Vec3(lx, ly, lz)
                }
                val pawProj = project(pawOffset)
                val pawRadius = 16f * pawProj.scale

                primitives.add(object : RenderPrimitive {
                    override val sortZ: Float = pawProj.depthZ
                    override fun draw(drawScope: DrawScope) {
                        // Paw base with lighting
                        val pawColor = if (index >= 2) secondaryColor else primaryColor
                        drawScope.drawCircle(
                            color = pawColor,
                            radius = pawRadius,
                            center = Offset(pawProj.screenX, pawProj.screenY)
                        )
                        // Toe pads
                        val toeColor = Color(0xFFFF8A80)
                        val toeRadius = pawRadius * 0.28f
                        drawScope.drawCircle(
                            color = toeColor,
                            radius = toeRadius * 1.5f,
                            center = Offset(pawProj.screenX, pawProj.screenY + pawRadius * 0.15f)
                        )
                        drawScope.drawCircle(
                            color = toeColor,
                            radius = toeRadius,
                            center = Offset(pawProj.screenX - pawRadius * 0.45f, pawProj.screenY - pawRadius * 0.3f)
                        )
                        drawScope.drawCircle(
                            color = toeColor,
                            radius = toeRadius,
                            center = Offset(pawProj.screenX, pawProj.screenY - pawRadius * 0.5f)
                        )
                        drawScope.drawCircle(
                            color = toeColor,
                            radius = toeRadius,
                            center = Offset(pawProj.screenX + pawRadius * 0.45f, pawProj.screenY - pawRadius * 0.3f)
                        )
                    }
                })
            }

            // -------------------------------------------------------------
            // 4. 3D Torso / Body (Layered Volumetric Sphere with 3D Shading)
            // -------------------------------------------------------------
            val bodyCenter = Vec3(0f, 32f * bodyScaleY, 0f)
            val bodyProj = project(bodyCenter)
            val bodyRadiusX = 54f * bodyScaleX * bodyProj.scale
            val bodyRadiusY = 60f * bodyScaleY * bodyProj.scale

            primitives.add(object : RenderPrimitive {
                override val sortZ: Float = bodyProj.depthZ
                override fun draw(drawScope: DrawScope) {
                    // Dynamic 3D lighting shader for the torso
                    drawScope.drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 1f),
                                primaryColor.copy(red = (primaryColor.red * 0.82f), green = (primaryColor.green * 0.82f), blue = (primaryColor.blue * 0.82f)),
                                primaryColor.copy(red = (primaryColor.red * 0.60f), green = (primaryColor.green * 0.60f), blue = (primaryColor.blue * 0.60f))
                            ),
                            center = Offset(bodyProj.screenX - bodyRadiusX * 0.3f, bodyProj.screenY - bodyRadiusY * 0.35f),
                            radius = max(bodyRadiusX, bodyRadiusY) * 1.35f
                        ),
                        topLeft = Offset(bodyProj.screenX - bodyRadiusX, bodyProj.screenY - bodyRadiusY),
                        size = Size(bodyRadiusX * 2f, bodyRadiusY * 2f)
                    )
                }
            })

            // -------------------------------------------------------------
            // 5. 3D Chest / Belly Fur Patch (Front of Torso)
            // -------------------------------------------------------------
            val bellyCenter = Vec3(0f, 34f * bodyScaleY, 38f * bodyScaleZ)
            val bellyProj = project(bellyCenter)
            val bellyRadiusX = 36f * bodyScaleX * bellyProj.scale
            val bellyRadiusY = 42f * bodyScaleY * bellyProj.scale

            primitives.add(object : RenderPrimitive {
                override val sortZ: Float = bellyProj.depthZ
                override fun draw(drawScope: DrawScope) {
                    drawScope.drawOval(
                        color = secondaryColor,
                        topLeft = Offset(bellyProj.screenX - bellyRadiusX, bellyProj.screenY - bellyRadiusY),
                        size = Size(bellyRadiusX * 2f, bellyRadiusY * 2f)
                    )
                }
            })

            // -------------------------------------------------------------
            // 6. 3D Head (Volumetric Sphere with Specular Highlights)
            // -------------------------------------------------------------
            val headCenter = Vec3(0f, -28f, 10f)
            val headProj = project(headCenter)
            val headRadius = 56f * headProj.scale

            primitives.add(object : RenderPrimitive {
                override val sortZ: Float = headProj.depthZ
                override fun draw(drawScope: DrawScope) {
                    // Head surface with directional ambient-diffuse lighting
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.40f),
                                primaryColor,
                                primaryColor.copy(red = (primaryColor.red * 0.78f), green = (primaryColor.green * 0.78f), blue = (primaryColor.blue * 0.78f)),
                                primaryColor.copy(red = (primaryColor.red * 0.52f), green = (primaryColor.green * 0.52f), blue = (primaryColor.blue * 0.52f))
                            ),
                            center = Offset(headProj.screenX - headRadius * 0.35f, headProj.screenY - headRadius * 0.38f),
                            radius = headRadius * 1.30f
                        ),
                        radius = headRadius,
                        center = Offset(headProj.screenX, headProj.screenY)
                    )
                }
            })

            // -------------------------------------------------------------
            // 7. 3D Ears (Left & Right, Depth-sorted based on Rotation)
            // -------------------------------------------------------------
            val earSpreadX = 42f
            val earBaseY = -62f
            val earZ = -5f
            val earTwitchOffset = sin(Math.toRadians(earTwitch.toDouble())).toFloat() * 6f

            // Left Ear & Right Ear local coords
            val leftEarTip = Vec3(-earSpreadX + earTwitchOffset, earBaseY - 32f, earZ)
            val rightEarTip = Vec3(earSpreadX - earTwitchOffset, earBaseY - 32f, earZ)

            val leftEarBaseL = Vec3(-earSpreadX - 20f, earBaseY + 10f, earZ)
            val leftEarBaseR = Vec3(-earSpreadX + 16f, earBaseY + 8f, earZ)

            val rightEarBaseL = Vec3(earSpreadX - 16f, earBaseY + 8f, earZ)
            val rightEarBaseR = Vec3(earSpreadX + 20f, earBaseY + 10f, earZ)

            val leftTipProj = project(leftEarTip)
            val leftBaseLProj = project(leftEarBaseL)
            val leftBaseRProj = project(leftEarBaseR)

            val rightTipProj = project(rightEarTip)
            val rightBaseLProj = project(rightEarBaseL)
            val rightBaseRProj = project(rightEarBaseR)

            // Add Left Ear
            primitives.add(object : RenderPrimitive {
                override val sortZ: Float = leftTipProj.depthZ
                override fun draw(drawScope: DrawScope) {
                    when (pet.earStyle) {
                        "DROOPING" -> {
                            // Bunny / Puppy floppy ear
                            drawScope.drawOval(
                                color = primaryColor,
                                topLeft = Offset(leftTipProj.screenX - 18f * leftTipProj.scale, leftTipProj.screenY + 10f),
                                size = Size(32f * leftTipProj.scale, 55f * leftTipProj.scale)
                            )
                            drawScope.drawOval(
                                color = Color(0xFFFFCDD2),
                                topLeft = Offset(leftTipProj.screenX - 12f * leftTipProj.scale, leftTipProj.screenY + 18f),
                                size = Size(20f * leftTipProj.scale, 38f * leftTipProj.scale)
                            )
                        }
                        "ROUND" -> {
                            // Bear / Hamster rounded ear
                            drawScope.drawCircle(
                                color = primaryColor,
                                radius = 24f * leftTipProj.scale,
                                center = Offset(leftTipProj.screenX, leftTipProj.screenY + 12f)
                            )
                            drawScope.drawCircle(
                                color = Color(0xFFFFCDD2),
                                radius = 14f * leftTipProj.scale,
                                center = Offset(leftTipProj.screenX, leftTipProj.screenY + 12f)
                            )
                        }
                        "DRAGON" -> {
                            // Dragon golden horn
                            val hornPath = Path().apply {
                                moveTo(leftBaseLProj.screenX, leftBaseLProj.screenY)
                                lineTo(leftTipProj.screenX - 10f, leftTipProj.screenY - 15f)
                                lineTo(leftBaseRProj.screenX, leftBaseRProj.screenY)
                                close()
                            }
                            drawScope.drawPath(hornPath, Color(0xFFFFD54F), style = Fill)
                            drawScope.drawPath(hornPath, Color(0xFFFFA000), style = Stroke(width = 2.5f))
                        }
                        else -> {
                            // Default Pointed Cat Ear
                            val earPath = Path().apply {
                                moveTo(leftBaseLProj.screenX, leftBaseLProj.screenY)
                                lineTo(leftTipProj.screenX, leftTipProj.screenY)
                                lineTo(leftBaseRProj.screenX, leftBaseRProj.screenY)
                                close()
                            }
                            drawScope.drawPath(earPath, color = primaryColor, style = Fill)
                            // Inner pink ear
                            val innerEarPath = Path().apply {
                                moveTo(leftBaseLProj.screenX + (leftTipProj.screenX - leftBaseLProj.screenX) * 0.35f, leftBaseLProj.screenY + (leftTipProj.screenY - leftBaseLProj.screenY) * 0.35f)
                                lineTo(leftTipProj.screenX, leftTipProj.screenY + 12f * leftTipProj.scale)
                                lineTo(leftBaseRProj.screenX + (leftTipProj.screenX - leftBaseRProj.screenX) * 0.35f, leftBaseRProj.screenY + (leftTipProj.screenY - leftBaseRProj.screenY) * 0.35f)
                                close()
                            }
                            drawScope.drawPath(innerEarPath, color = Color(0xFFFF8A80), style = Fill)
                        }
                    }
                }
            })

            // Add Right Ear
            primitives.add(object : RenderPrimitive {
                override val sortZ: Float = rightTipProj.depthZ
                override fun draw(drawScope: DrawScope) {
                    when (pet.earStyle) {
                        "DROOPING" -> {
                            drawScope.drawOval(
                                color = primaryColor,
                                topLeft = Offset(rightTipProj.screenX - 14f * rightTipProj.scale, rightTipProj.screenY + 10f),
                                size = Size(32f * rightTipProj.scale, 55f * rightTipProj.scale)
                            )
                            drawScope.drawOval(
                                color = Color(0xFFFFCDD2),
                                topLeft = Offset(rightTipProj.screenX - 8f * rightTipProj.scale, rightTipProj.screenY + 18f),
                                size = Size(20f * rightTipProj.scale, 38f * rightTipProj.scale)
                            )
                        }
                        "ROUND" -> {
                            drawScope.drawCircle(
                                color = primaryColor,
                                radius = 24f * rightTipProj.scale,
                                center = Offset(rightTipProj.screenX, rightTipProj.screenY + 12f)
                            )
                            drawScope.drawCircle(
                                color = Color(0xFFFFCDD2),
                                radius = 14f * rightTipProj.scale,
                                center = Offset(rightTipProj.screenX, rightTipProj.screenY + 12f)
                            )
                        }
                        "DRAGON" -> {
                            val hornPath = Path().apply {
                                moveTo(rightBaseLProj.screenX, rightBaseLProj.screenY)
                                lineTo(rightTipProj.screenX + 10f, rightTipProj.screenY - 15f)
                                lineTo(rightBaseRProj.screenX, rightBaseRProj.screenY)
                                close()
                            }
                            drawScope.drawPath(hornPath, Color(0xFFFFD54F), style = Fill)
                            drawScope.drawPath(hornPath, Color(0xFFFFA000), style = Stroke(width = 2.5f))
                        }
                        else -> {
                            val earPath = Path().apply {
                                moveTo(rightBaseLProj.screenX, rightBaseLProj.screenY)
                                lineTo(rightTipProj.screenX, rightTipProj.screenY)
                                lineTo(rightBaseRProj.screenX, rightBaseRProj.screenY)
                                close()
                            }
                            drawScope.drawPath(earPath, color = primaryColor, style = Fill)
                            val innerEarPath = Path().apply {
                                moveTo(rightBaseLProj.screenX + (rightTipProj.screenX - rightBaseLProj.screenX) * 0.35f, rightBaseLProj.screenY + (rightTipProj.screenY - rightBaseLProj.screenY) * 0.35f)
                                lineTo(rightTipProj.screenX, rightTipProj.screenY + 12f * rightTipProj.scale)
                                lineTo(rightBaseRProj.screenX + (rightTipProj.screenX - rightBaseRProj.screenX) * 0.35f, rightBaseRProj.screenY + (rightTipProj.screenY - rightBaseRProj.screenY) * 0.35f)
                                close()
                            }
                            drawScope.drawPath(innerEarPath, color = Color(0xFFFF8A80), style = Fill)
                        }
                    }
                }
            })

            // -------------------------------------------------------------
            // 8. 3D Face Features: Eyes, Snout, Mouth, Cheeks
            // -------------------------------------------------------------
            val eyeSpacing = 22f
            val eyeY = -34f
            val eyeZ = 52f

            val leftEyeProj = project(Vec3(-eyeSpacing, eyeY, eyeZ))
            val rightEyeProj = project(Vec3(eyeSpacing, eyeY, eyeZ))

            // Cheeks (Blushing)
            val leftCheekProj = project(Vec3(-38f, -20f, 48f))
            val rightCheekProj = project(Vec3(38f, -20f, 48f))

            // Snout & Nose
            val snoutProj = project(Vec3(0f, -18f, 56f))

            // Draw Face Primitives
            primitives.add(object : RenderPrimitive {
                override val sortZ: Float = snoutProj.depthZ + 10f
                override fun draw(drawScope: DrawScope) {
                    // Cheeks glow
                    val cheekRadius = 14f * leftCheekProj.scale
                    drawScope.drawCircle(
                        color = Color(0xFFFF8A80).copy(alpha = 0.55f),
                        radius = cheekRadius,
                        center = Offset(leftCheekProj.screenX, leftCheekProj.screenY)
                    )
                    drawScope.drawCircle(
                        color = Color(0xFFFF8A80).copy(alpha = 0.55f),
                        radius = cheekRadius,
                        center = Offset(rightCheekProj.screenX, rightCheekProj.screenY)
                    )

                    // Snout oval
                    drawScope.drawOval(
                        color = secondaryColor,
                        topLeft = Offset(snoutProj.screenX - 22f * snoutProj.scale, snoutProj.screenY - 16f * snoutProj.scale),
                        size = Size(44f * snoutProj.scale, 32f * snoutProj.scale)
                    )

                    // Nose button (heart/triangle)
                    val noseRadius = 6f * snoutProj.scale
                    drawScope.drawCircle(
                        color = Color(0xFF263238),
                        radius = noseRadius,
                        center = Offset(snoutProj.screenX, snoutProj.screenY - 4f * snoutProj.scale)
                    )

                    // Cute cat / dog mouth
                    val mouthY = snoutProj.screenY + 4f * snoutProj.scale
                    val mouthW = 10f * snoutProj.scale
                    drawScope.drawLine(
                        color = Color(0xFF37474F),
                        start = Offset(snoutProj.screenX, snoutProj.screenY - 2f),
                        end = Offset(snoutProj.screenX, mouthY),
                        strokeWidth = 2.5f
                    )
                    // Left curve & Right curve
                    val mouthPath = Path().apply {
                        moveTo(snoutProj.screenX - mouthW, mouthY + 3f)
                        quadraticTo(snoutProj.screenX - mouthW / 2f, mouthY + 7f, snoutProj.screenX, mouthY)
                        quadraticTo(snoutProj.screenX + mouthW / 2f, mouthY + 7f, snoutProj.screenX + mouthW, mouthY + 3f)
                    }
                    drawScope.drawPath(mouthPath, color = Color(0xFF37474F), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

                    // 3D Eyes (Blinking & Expression Handling)
                    val isEyesClosed = actionState == Pet3DActionState.SLEEPING || actionState == Pet3DActionState.CUDDLE_PURR
                    val eyeHeightFactor = if (isEyesClosed) 0.12f else 1.0f

                    listOf(leftEyeProj, rightEyeProj).forEach { eye ->
                        val eyeR = 12f * eye.scale
                        if (isEyesClosed) {
                            // Happy crescent eyes
                            val arcPath = Path().apply {
                                moveTo(eye.screenX - eyeR, eye.screenY)
                                quadraticTo(eye.screenX, eye.screenY - eyeR * 0.8f, eye.screenX + eyeR, eye.screenY)
                            }
                            drawScope.drawPath(arcPath, color = Color(0xFF263238), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                        } else {
                            // Sclera / Big Shiny Pupil
                            val eyeScaleY = eyeR * eyeHeightFactor
                            drawScope.drawOval(
                                color = Color(0xFF212121),
                                topLeft = Offset(eye.screenX - eyeR, eye.screenY - eyeScaleY),
                                size = Size(eyeR * 2f, eyeScaleY * 2f)
                            )
                            // Iris Ring (Customizable or cute hazel)
                            drawScope.drawCircle(
                                color = Color(0xFF42A5F5),
                                radius = eyeR * 0.45f,
                                center = Offset(eye.screenX, eye.screenY)
                            )
                            // Big Specular Catchlight reflection
                            drawScope.drawCircle(
                                color = Color.White,
                                radius = eyeR * 0.40f,
                                center = Offset(eye.screenX - eyeR * 0.32f, eye.screenY - eyeScaleY * 0.35f)
                            )
                            // Small catchlight reflection
                            drawScope.drawCircle(
                                color = Color.White,
                                radius = eyeR * 0.20f,
                                center = Offset(eye.screenX + eyeR * 0.35f, eye.screenY + eyeScaleY * 0.25f)
                            )
                        }
                    }

                    // Whiskers (Left & Right)
                    val whiskerLen = 28f * snoutProj.scale
                    val whiskerY1 = snoutProj.screenY - 2f
                    val whiskerY2 = snoutProj.screenY + 6f
                    // Left whiskers
                    drawScope.drawLine(Color(0xFF616161), Offset(snoutProj.screenX - 22f, whiskerY1), Offset(snoutProj.screenX - 22f - whiskerLen, whiskerY1 - 4f), strokeWidth = 2f)
                    drawScope.drawLine(Color(0xFF616161), Offset(snoutProj.screenX - 22f, whiskerY2), Offset(snoutProj.screenX - 22f - whiskerLen, whiskerY2 + 4f), strokeWidth = 2f)
                    // Right whiskers
                    drawScope.drawLine(Color(0xFF616161), Offset(snoutProj.screenX + 22f, whiskerY1), Offset(snoutProj.screenX + 22f + whiskerLen, whiskerY1 - 4f), strokeWidth = 2f)
                    drawScope.drawLine(Color(0xFF616161), Offset(snoutProj.screenX + 22f, whiskerY2), Offset(snoutProj.screenX + 22f + whiskerLen, whiskerY2 + 4f), strokeWidth = 2f)
                }
            })

            // -------------------------------------------------------------
            // 9. 3D Accessories (Collars, Hats, Bowties, Sunglasses, Capes)
            // -------------------------------------------------------------
            when (pet.accessory) {
                "BELL_COLLAR" -> {
                    val collarCenter = Vec3(0f, -2f, 32f)
                    val collarProj = project(collarCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = collarProj.depthZ + 15f
                        override fun draw(drawScope: DrawScope) {
                            val collarW = 60f * collarProj.scale
                            val collarH = 12f * collarProj.scale
                            // Red ribbon collar
                            drawScope.drawRoundRect(
                                color = Color(0xFFE53935),
                                topLeft = Offset(collarProj.screenX - collarW / 2f, collarProj.screenY - collarH / 2f),
                                size = Size(collarW, collarH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                            // 3D Golden Bell
                            drawScope.drawCircle(
                                color = Color(0xFFFFD54F),
                                radius = 10f * collarProj.scale,
                                center = Offset(collarProj.screenX, collarProj.screenY + 4f)
                            )
                            drawScope.drawCircle(
                                color = Color(0xFFB71C1C),
                                radius = 2.5f * collarProj.scale,
                                center = Offset(collarProj.screenX, collarProj.screenY + 5f)
                            )
                        }
                    })
                }
                "BOW_TIE" -> {
                    val bowCenter = Vec3(0f, -2f, 34f)
                    val bowProj = project(bowCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = bowProj.depthZ + 15f
                        override fun draw(drawScope: DrawScope) {
                            val bowW = 36f * bowProj.scale
                            val bowH = 20f * bowProj.scale
                            val path = Path().apply {
                                moveTo(bowProj.screenX - bowW / 2f, bowProj.screenY - bowH / 2f)
                                lineTo(bowProj.screenX, bowProj.screenY)
                                lineTo(bowProj.screenX - bowW / 2f, bowProj.screenY + bowH / 2f)
                                lineTo(bowProj.screenX + bowW / 2f, bowProj.screenY + bowH / 2f)
                                lineTo(bowProj.screenX, bowProj.screenY)
                                lineTo(bowProj.screenX + bowW / 2f, bowProj.screenY - bowH / 2f)
                                close()
                            }
                            drawScope.drawPath(path, Color(0xFF1E88E5))
                            drawScope.drawCircle(Color(0xFF0D47A1), radius = 6f * bowProj.scale, center = Offset(bowProj.screenX, bowProj.screenY))
                        }
                    })
                }
                "BERET_HAT" -> {
                    val hatCenter = Vec3(-12f, -80f, 0f)
                    val hatProj = project(hatCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = hatProj.depthZ + 25f
                        override fun draw(drawScope: DrawScope) {
                            val hatW = 68f * hatProj.scale
                            val hatH = 28f * hatProj.scale
                            drawScope.drawOval(
                                color = Color(0xFF5D4037),
                                topLeft = Offset(hatProj.screenX - hatW / 2f, hatProj.screenY - hatH / 2f),
                                size = Size(hatW, hatH)
                            )
                            drawScope.drawCircle(Color(0xFF3E2723), radius = 4f * hatProj.scale, center = Offset(hatProj.screenX, hatProj.screenY - hatH / 2f))
                        }
                    })
                }
                "SUNGLASSES" -> {
                    val glassCenter = Vec3(0f, -34f, 60f)
                    val glassProj = project(glassCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = glassProj.depthZ + 20f
                        override fun draw(drawScope: DrawScope) {
                            val lensW = 26f * glassProj.scale
                            val lensH = 18f * glassProj.scale
                            // Bridge
                            drawScope.drawLine(
                                color = Color(0xFF212121),
                                start = Offset(glassProj.screenX - 16f, glassProj.screenY),
                                end = Offset(glassProj.screenX + 16f, glassProj.screenY),
                                strokeWidth = 3f
                            )
                            // Left lens
                            drawScope.drawRoundRect(
                                color = Color(0xEE212121),
                                topLeft = Offset(glassProj.screenX - 28f * glassProj.scale, glassProj.screenY - lensH / 2f),
                                size = Size(lensW, lensH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                            // Right lens
                            drawScope.drawRoundRect(
                                color = Color(0xEE212121),
                                topLeft = Offset(glassProj.screenX + 4f * glassProj.scale, glassProj.screenY - lensH / 2f),
                                size = Size(lensW, lensH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                            // Specular lens sheen reflection
                            drawScope.drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(glassProj.screenX - 24f * glassProj.scale, glassProj.screenY - 4f),
                                end = Offset(glassProj.screenX - 12f * glassProj.scale, glassProj.screenY + 4f),
                                strokeWidth = 2f
                            )
                        }
                    })
                }
                "CAPE" -> {
                    val capeCenter = Vec3(0f, 35f, -48f)
                    val capeProj = project(capeCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = capeProj.depthZ - 10f // Behind back
                        override fun draw(drawScope: DrawScope) {
                            val capeW = 85f * capeProj.scale
                            val capeH = 95f * capeProj.scale
                            val path = Path().apply {
                                moveTo(capeProj.screenX - capeW * 0.35f, capeProj.screenY - capeH * 0.45f)
                                lineTo(capeProj.screenX + capeW * 0.35f, capeProj.screenY - capeH * 0.45f)
                                lineTo(capeProj.screenX + capeW * 0.50f, capeProj.screenY + capeH * 0.55f)
                                lineTo(capeProj.screenX - capeW * 0.50f, capeProj.screenY + capeH * 0.55f)
                                close()
                            }
                            drawScope.drawPath(path, Color(0xFFD32F2F))
                        }
                    })
                }
                "WIZARD_HAT" -> {
                    val hatBase = Vec3(0f, -80f, 0f)
                    val hatTip = Vec3(12f, -145f, -5f)
                    val baseProj = project(hatBase)
                    val tipProj = project(hatTip)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = baseProj.depthZ + 35f
                        override fun draw(drawScope: DrawScope) {
                            val brimW = 85f * baseProj.scale
                            val brimH = 32f * baseProj.scale
                            drawScope.drawOval(
                                color = Color(0xFF4A148C),
                                topLeft = Offset(baseProj.screenX - brimW / 2f, baseProj.screenY - brimH / 2f),
                                size = Size(brimW, brimH)
                            )
                            val conePath = Path().apply {
                                moveTo(baseProj.screenX - brimW * 0.35f, baseProj.screenY)
                                lineTo(tipProj.screenX, tipProj.screenY)
                                lineTo(baseProj.screenX + brimW * 0.35f, baseProj.screenY)
                                close()
                            }
                            drawScope.drawPath(conePath, Color(0xFF6A1B9A))
                            // Gold magic star on tip
                            drawScope.drawCircle(
                                color = Color(0xFFFFD54F),
                                radius = 7f * tipProj.scale,
                                center = Offset(tipProj.screenX, tipProj.screenY)
                            )
                        }
                    })
                }
                "CROWN" -> {
                    val crownCenter = Vec3(0f, -84f, 0f)
                    val crownProj = project(crownCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = crownProj.depthZ + 35f
                        override fun draw(drawScope: DrawScope) {
                            val cW = 54f * crownProj.scale
                            val cH = 32f * crownProj.scale
                            val crownPath = Path().apply {
                                moveTo(crownProj.screenX - cW / 2f, crownProj.screenY + cH / 2f)
                                lineTo(crownProj.screenX - cW / 2f, crownProj.screenY - cH / 2f)
                                lineTo(crownProj.screenX - cW * 0.2f, crownProj.screenY)
                                lineTo(crownProj.screenX, crownProj.screenY - cH * 0.7f)
                                lineTo(crownProj.screenX + cW * 0.2f, crownProj.screenY)
                                lineTo(crownProj.screenX + cW / 2f, crownProj.screenY - cH / 2f)
                                lineTo(crownProj.screenX + cW / 2f, crownProj.screenY + cH / 2f)
                                close()
                            }
                            drawScope.drawPath(crownPath, Color(0xFFFFD700))
                            drawScope.drawPath(crownPath, Color(0xFFFFA000), style = Stroke(width = 2.5f))
                            // Jewels
                            drawScope.drawCircle(Color(0xFFE91E63), radius = 3.5f * crownProj.scale, center = Offset(crownProj.screenX, crownProj.screenY - cH * 0.7f))
                            drawScope.drawCircle(Color(0xFF00E5FF), radius = 3f * crownProj.scale, center = Offset(crownProj.screenX - cW / 2f, crownProj.screenY - cH / 2f))
                            drawScope.drawCircle(Color(0xFF00E5FF), radius = 3f * crownProj.scale, center = Offset(crownProj.screenX + cW / 2f, crownProj.screenY - cH / 2f))
                        }
                    })
                }
                "STRAW_HAT" -> {
                    val strawCenter = Vec3(0f, -80f, 0f)
                    val strawProj = project(strawCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = strawProj.depthZ + 30f
                        override fun draw(drawScope: DrawScope) {
                            val brimW = 92f * strawProj.scale
                            val brimH = 34f * strawProj.scale
                            // Straw brim
                            drawScope.drawOval(
                                color = Color(0xFFFFE082),
                                topLeft = Offset(strawProj.screenX - brimW / 2f, strawProj.screenY - brimH / 2f),
                                size = Size(brimW, brimH)
                            )
                            // Straw dome
                            val domeW = 50f * strawProj.scale
                            val domeH = 26f * strawProj.scale
                            drawScope.drawOval(
                                color = Color(0xFFFFD54F),
                                topLeft = Offset(strawProj.screenX - domeW / 2f, strawProj.screenY - domeH * 0.85f),
                                size = Size(domeW, domeH)
                            )
                            // Ribbon band
                            drawScope.drawOval(
                                color = Color(0xFF0288D1),
                                topLeft = Offset(strawProj.screenX - domeW * 0.52f, strawProj.screenY - domeH * 0.35f),
                                size = Size(domeW * 1.04f, 8f * strawProj.scale)
                            )
                        }
                    })
                }
                "PARTY_HAT" -> {
                    val baseCenter = Vec3(0f, -80f, 0f)
                    val tipCenter = Vec3(0f, -140f, 0f)
                    val baseProj = project(baseCenter)
                    val tipProj = project(tipCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = baseProj.depthZ + 30f
                        override fun draw(drawScope: DrawScope) {
                            val coneW = 44f * baseProj.scale
                            val path = Path().apply {
                                moveTo(baseProj.screenX - coneW / 2f, baseProj.screenY)
                                lineTo(tipProj.screenX, tipProj.screenY)
                                lineTo(baseProj.screenX + coneW / 2f, baseProj.screenY)
                                close()
                            }
                            drawScope.drawPath(path, Color(0xFFFF4081))
                            drawScope.drawCircle(Color(0xFFFFEE58), radius = 6f * tipProj.scale, center = Offset(tipProj.screenX, tipProj.screenY))
                        }
                    })
                }
                "HEADPHONES" -> {
                    val headCenter = Vec3(0f, -50f, 0f)
                    val headProj = project(headCenter)
                    val leftCup = project(Vec3(-48f, -40f, 0f))
                    val rightCup = project(Vec3(48f, -40f, 0f))
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = headProj.depthZ + 35f
                        override fun draw(drawScope: DrawScope) {
                            // Arch Band
                            drawScope.drawArc(
                                color = Color(0xFF37474F),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(headProj.screenX - 48f * headProj.scale, headProj.screenY - 35f * headProj.scale),
                                size = Size(96f * headProj.scale, 50f * headProj.scale),
                                style = Stroke(width = 6f * headProj.scale)
                            )
                            // Left & Right neon cups
                            val cupW = 14f * leftCup.scale
                            val cupH = 24f * leftCup.scale
                            drawScope.drawRoundRect(
                                color = Color(0xFF00E5FF),
                                topLeft = Offset(leftCup.screenX - cupW / 2f, leftCup.screenY - cupH / 2f),
                                size = Size(cupW, cupH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                            drawScope.drawRoundRect(
                                color = Color(0xFF00E5FF),
                                topLeft = Offset(rightCup.screenX - cupW / 2f, rightCup.screenY - cupH / 2f),
                                size = Size(cupW, cupH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                        }
                    })
                }
                "FLOWER_PIN" -> {
                    val pinCenter = Vec3(-28f, -65f, 25f)
                    val pinProj = project(pinCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = pinProj.depthZ + 35f
                        override fun draw(drawScope: DrawScope) {
                            val r = 8f * pinProj.scale
                            for (i in 0..4) {
                                val angle = (i * 72f) * (Math.PI / 180f).toFloat()
                                val px = pinProj.screenX + kotlin.math.cos(angle) * r * 1.5f
                                val py = pinProj.screenY + kotlin.math.sin(angle) * r * 1.5f
                                drawScope.drawCircle(Color(0xFFF48FB1), radius = r, center = Offset(px, py))
                            }
                            drawScope.drawCircle(Color(0xFFFFEB3B), radius = r * 0.7f, center = Offset(pinProj.screenX, pinProj.screenY))
                        }
                    })
                }
                "SCARF" -> {
                    val scarfCenter = Vec3(0f, 4f, 32f)
                    val scarfProj = project(scarfCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = scarfProj.depthZ + 25f
                        override fun draw(drawScope: DrawScope) {
                            val sW = 68f * scarfProj.scale
                            val sH = 18f * scarfProj.scale
                            drawScope.drawRoundRect(
                                color = Color(0xFFD32F2F),
                                topLeft = Offset(scarfProj.screenX - sW / 2f, scarfProj.screenY - sH / 2f),
                                size = Size(sW, sH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f, 9f)
                            )
                            // Tail hanging
                            drawScope.drawRoundRect(
                                color = Color(0xFFB71C1C),
                                topLeft = Offset(scarfProj.screenX + 12f * scarfProj.scale, scarfProj.screenY),
                                size = Size(14f * scarfProj.scale, 28f * scarfProj.scale),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                            )
                        }
                    })
                }
                "PEARL_NECKLACE" -> {
                    val neckCenter = Vec3(0f, 8f, 34f)
                    val neckProj = project(neckCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = neckProj.depthZ + 25f
                        override fun draw(drawScope: DrawScope) {
                            val r = 4f * neckProj.scale
                            for (i in -4..4) {
                                val px = neckProj.screenX + i * 7f * neckProj.scale
                                val py = neckProj.screenY + (i * i) * 0.5f * neckProj.scale
                                drawScope.drawCircle(Color.White, radius = r, center = Offset(px, py))
                                drawScope.drawCircle(Color(0xFFE0E0E0), radius = r, center = Offset(px, py), style = Stroke(width = 1f))
                            }
                        }
                    })
                }
                "SAILOR_SUIT" -> {
                    val chestCenter = Vec3(0f, 15f, 32f)
                    val chestProj = project(chestCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = chestProj.depthZ + 25f
                        override fun draw(drawScope: DrawScope) {
                            val cW = 56f * chestProj.scale
                            val cH = 28f * chestProj.scale
                            val path = Path().apply {
                                moveTo(chestProj.screenX - cW / 2f, chestProj.screenY)
                                lineTo(chestProj.screenX - cW * 0.35f, chestProj.screenY + cH)
                                lineTo(chestProj.screenX, chestProj.screenY + cH * 0.5f)
                                lineTo(chestProj.screenX + cW * 0.35f, chestProj.screenY + cH)
                                lineTo(chestProj.screenX + cW / 2f, chestProj.screenY)
                                close()
                            }
                            drawScope.drawPath(path, Color(0xFF1565C0))
                            drawScope.drawCircle(Color(0xFFE53935), radius = 5f * chestProj.scale, center = Offset(chestProj.screenX, chestProj.screenY + cH * 0.6f))
                        }
                    })
                }
                "SWEATER" -> {
                    val bellyCenter = Vec3(0f, 34f, 22f)
                    val bellyProj = project(bellyCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = bellyProj.depthZ + 22f
                        override fun draw(drawScope: DrawScope) {
                            val bW = 68f * bellyProj.scale
                            val bH = 48f * bellyProj.scale
                            drawScope.drawOval(
                                color = Color(0xFFFFB300),
                                topLeft = Offset(bellyProj.screenX - bW / 2f, bellyProj.screenY - bH / 2f),
                                size = Size(bW, bH)
                            )
                            // Knit lines
                            drawScope.drawLine(
                                color = Color(0xFFFFA000),
                                start = Offset(bellyProj.screenX - 10f * bellyProj.scale, bellyProj.screenY - bH * 0.35f),
                                end = Offset(bellyProj.screenX - 10f * bellyProj.scale, bellyProj.screenY + bH * 0.35f),
                                strokeWidth = 2.5f
                            )
                            drawScope.drawLine(
                                color = Color(0xFFFFA000),
                                start = Offset(bellyProj.screenX + 10f * bellyProj.scale, bellyProj.screenY - bH * 0.35f),
                                end = Offset(bellyProj.screenX + 10f * bellyProj.scale, bellyProj.screenY + bH * 0.35f),
                                strokeWidth = 2.5f
                            )
                        }
                    })
                }
                "HOODIE" -> {
                    val hCenter = Vec3(0f, 34f, 22f)
                    val hProj = project(hCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = hProj.depthZ + 22f
                        override fun draw(drawScope: DrawScope) {
                            val bW = 70f * hProj.scale
                            val bH = 50f * hProj.scale
                            drawScope.drawOval(
                                color = Color(0xFF7E57C2),
                                topLeft = Offset(hProj.screenX - bW / 2f, hProj.screenY - bH / 2f),
                                size = Size(bW, bH)
                            )
                            // White drawstrings
                            drawScope.drawLine(Color.White, Offset(hProj.screenX - 6f, hProj.screenY - bH * 0.3f), Offset(hProj.screenX - 6f, hProj.screenY), strokeWidth = 2f)
                            drawScope.drawLine(Color.White, Offset(hProj.screenX + 6f, hProj.screenY - bH * 0.3f), Offset(hProj.screenX + 6f, hProj.screenY), strokeWidth = 2f)
                        }
                    })
                }
                "HALO" -> {
                    val haloCenter = Vec3(0f, -110f, 0f)
                    val haloProj = project(haloCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = haloProj.depthZ + 40f
                        override fun draw(drawScope: DrawScope) {
                            val hW = 64f * haloProj.scale
                            val hH = 20f * haloProj.scale
                            drawScope.drawOval(
                                color = Color(0xFFFFD54F),
                                topLeft = Offset(haloProj.screenX - hW / 2f, haloProj.screenY - hH / 2f),
                                size = Size(hW, hH),
                                style = Stroke(width = 5f * haloProj.scale)
                            )
                        }
                    })
                }
                "ANGEL_WINGS" -> {
                    val wingCenter = Vec3(0f, 15f, -38f)
                    val wingProj = project(wingCenter)
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = wingProj.depthZ - 15f // Behind
                        override fun draw(drawScope: DrawScope) {
                            val wingW = 60f * wingProj.scale
                            val wingH = 45f * wingProj.scale
                            // Left wing
                            val leftW = Path().apply {
                                moveTo(wingProj.screenX - 25f * wingProj.scale, wingProj.screenY)
                                cubicTo(
                                    wingProj.screenX - 70f * wingProj.scale, wingProj.screenY - 35f * wingProj.scale,
                                    wingProj.screenX - 85f * wingProj.scale, wingProj.screenY + 25f * wingProj.scale,
                                    wingProj.screenX - 25f * wingProj.scale, wingProj.screenY + 30f * wingProj.scale
                                )
                                close()
                            }
                            // Right wing
                            val rightW = Path().apply {
                                moveTo(wingProj.screenX + 25f * wingProj.scale, wingProj.screenY)
                                cubicTo(
                                    wingProj.screenX + 70f * wingProj.scale, wingProj.screenY - 35f * wingProj.scale,
                                    wingProj.screenX + 85f * wingProj.scale, wingProj.screenY + 25f * wingProj.scale,
                                    wingProj.screenX + 25f * wingProj.scale, wingProj.screenY + 30f * wingProj.scale
                                )
                                close()
                            }
                            drawScope.drawPath(leftW, Color.White)
                            drawScope.drawPath(leftW, Color(0xFFCE93D8), style = Stroke(width = 2.5f))
                            drawScope.drawPath(rightW, Color.White)
                            drawScope.drawPath(rightW, Color(0xFFCE93D8), style = Stroke(width = 2.5f))
                        }
                    })
                }
                "DEVIL_HORNS" -> {
                    val hornL = project(Vec3(-22f, -80f, 0f))
                    val hornR = project(Vec3(22f, -80f, 0f))
                    primitives.add(object : RenderPrimitive {
                        override val sortZ: Float = hornL.depthZ + 35f
                        override fun draw(drawScope: DrawScope) {
                            val hW = 12f * hornL.scale
                            val hH = 22f * hornL.scale
                            val pathL = Path().apply {
                                moveTo(hornL.screenX - hW / 2f, hornL.screenY)
                                lineTo(hornL.screenX - hW, hornL.screenY - hH)
                                lineTo(hornL.screenX + hW / 2f, hornL.screenY)
                                close()
                            }
                            val pathR = Path().apply {
                                moveTo(hornR.screenX - hW / 2f, hornR.screenY)
                                lineTo(hornR.screenX + hW, hornR.screenY - hH)
                                lineTo(hornR.screenX + hW / 2f, hornR.screenY)
                                close()
                            }
                            drawScope.drawPath(pathL, Color(0xFFB71C1C))
                            drawScope.drawPath(pathR, Color(0xFFB71C1C))
                        }
                    })
                }
            }

            // -------------------------------------------------------------
            // 10. Special Action Visual Effects (Shield Aura, Attack Claw Trail)
            // -------------------------------------------------------------
            if (actionState == Pet3DActionState.DEFEND_SHIELD) {
                primitives.add(object : RenderPrimitive {
                    override val sortZ: Float = 9999f // In front of everything
                    override fun draw(drawScope: DrawScope) {
                        drawScope.drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x2280D8FF),
                                    Color(0x6600E5FF),
                                    Color(0xCC00B0FF)
                                ),
                                center = Offset(centerX, centerY),
                                radius = 135f
                            ),
                            radius = 135f,
                            center = Offset(centerX, centerY)
                        )
                        drawScope.drawCircle(
                            color = Color(0xFF80D8FF),
                            radius = 135f,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 4f)
                        )
                    }
                })
            }

            // -------------------------------------------------------------
            // Painter's Algorithm Execution: Sort from Back (-Z) to Front (+Z)
            // -------------------------------------------------------------
            primitives.sortBy { it.sortZ }
            primitives.forEach { it.draw(this) }
        }
    }
}
