package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyTaskEntity
import com.example.data.model.FoodItem
import com.example.data.model.PetEntity
import com.example.ui.components.AudioEqualizerWave
import com.example.ui.components.DailyTasksModalSheet
import com.example.ui.components.Pet3DActionState
import com.example.ui.components.Pet3DModelView
import com.example.ui.components.PetAnimationMode
import com.example.ui.components.PetModelView
import com.example.ui.components.PetSoundControlBadge
import com.example.ui.theme.BubbleBlue
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    pet: PetEntity?,
    allPets: List<PetEntity>,
    animationMode: PetAnimationMode,
    isSleeping: Boolean,
    foodList: List<FoodItem>,
    feedbackMessage: String?,
    dailyTasks: List<DailyTaskEntity> = emptyList(),
    isSoundMuted: Boolean = false,
    isPlayingSound: Boolean = false,
    currentSoundLabel: String? = null,
    onToggleSoundMute: () -> Unit = {},
    onCuddle: () -> Unit,
    onFeed: (FoodItem) -> Unit,
    onBath: () -> Unit,
    onPlay: () -> Unit,
    onToggleSleep: () -> Unit,
    onSwitchPet: (Long) -> Unit,
    onNavigateToCustomizer: () -> Unit,
    onNavigateToShop: () -> Unit = {},
    onOpenAR: () -> Unit,
    onClaimDailyTaskReward: (String) -> Unit = {},
    onClaimMilestoneReward: (Int) -> Unit = {},
    onCheckIn: () -> Unit = {},
    onNavigateToGrowth: () -> Unit = {},
    onNavigateToCommunity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFoodSheet by remember { mutableStateOf(false) }
    var showDailyTasksSheet by remember { mutableStateOf(false) }
    var is3DMode by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState()

    if (pet == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("还没有萌宠呢，快去工坊领养一只吧！", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToCustomizer,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.testTag("adopt_pet_button")
                ) {
                    Text("前往定制工坊领养")
                }
            }
        }
        return
    }

    // Room background: Immersive UI lavender background when awake, deep dark violet when sleeping
    val roomBg = if (isSleeping) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF141218), Color(0xFF1D1B20))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFEF7FF), Color(0xFFF3EDF7))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(roomBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Top Pet Header & Multiple Pet Switcher
            PetHeaderSection(
                pet = pet,
                allPets = allPets,
                isSleeping = isSleeping,
                isSoundMuted = isSoundMuted,
                isPlayingSound = isPlayingSound,
                currentSoundLabel = currentSoundLabel,
                onToggleSoundMute = onToggleSoundMute,
                onSwitchPet = onSwitchPet,
                onNavigateToCustomizer = onNavigateToCustomizer,
                onNavigateToShop = onNavigateToShop,
                onOpenAR = onOpenAR,
                onOpenDailyTasks = { showDailyTasksSheet = true }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Immersive Pet Hero Stage with Floating Badges & Action Buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0xFF6750A4).copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        BorderStroke(
                            4.dp,
                            if (isSleeping) Color(0xFF2B2831) else Color.White
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .background(
                        if (isSleeping) {
                            Brush.verticalGradient(listOf(Color(0xFF241E38), Color(0xFF382F54)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFFD0BCFF), Color(0xFFEADDFF)))
                        }
                    )
                    .padding(top = 16.dp, bottom = 20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar inside Hero Viewport: Floating Health Pill, Sound Equalizer Wave & Rotated Gold Star Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Floating Glass Status Badge
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSleeping) Color(0xFF90A4AE) else Color(0xFF4CAF50))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val statusDesc = when {
                                    isSleeping -> "安睡休息中"
                                    pet.hunger >= 50 && pet.happiness >= 50 && pet.cleanliness >= 50 -> "健康状态良好"
                                    else -> "需要主人照料"
                                }
                                Text(
                                    text = statusDesc,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D)
                                )
                            }
                        }

                        // Real-time Sound Wave Equalizer Chip when sound is playing
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isPlayingSound && !isSoundMuted,
                            enter = fadeIn() + slideInVertically { -it / 2 },
                            exit = fadeOut() + slideOutVertically { -it / 2 }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CoralPrimary.copy(alpha = 0.95f),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    AudioEqualizerWave(isPlaying = true, waveColor = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentSoundLabel ?: "音效播放中~",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Gold Star Badge (Rotated 10 degrees)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFBC02D),
                            shadowElevation = 4.dp,
                            modifier = Modifier.rotate(10f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Lv.${pet.level}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Floating Speech Bubble if active
                    androidx.compose.animation.AnimatedVisibility(
                        visible = feedbackMessage != null,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Surface(
                            color = if (isSleeping) Color(0xFF2B2831) else Color.White,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 6.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = feedbackMessage ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSleeping) Color.White else Color(0xFF21005D),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }

                    // Pet Canvas Stage with Soft Ambient Glow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient Radial Glow
                        Box(
                            modifier = Modifier
                                .size(195.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                        )

                        // 3D / 2D Model Rendering Switcher
                        if (is3DMode) {
                            val pet3DAction = remember(animationMode, isSleeping) {
                                when {
                                    isSleeping -> Pet3DActionState.SLEEPING
                                    animationMode == PetAnimationMode.PLAYING -> Pet3DActionState.VICTORY_SPIN
                                    animationMode == PetAnimationMode.EATING -> Pet3DActionState.CUDDLE_PURR
                                    animationMode == PetAnimationMode.BATHING -> Pet3DActionState.DEFEND_SHIELD
                                    else -> Pet3DActionState.IDLE
                                }
                            }

                            Pet3DModelView(
                                pet = pet,
                                modelSize = 220.dp,
                                actionState = pet3DAction,
                                enableOrbitDrag = true,
                                onPetClicked = onCuddle,
                                modifier = Modifier.testTag("interactive_pet_3d_avatar")
                            )
                        } else {
                            PetModelView(
                                pet = pet,
                                petSize = 210.dp,
                                animationMode = animationMode,
                                onPetClicked = onCuddle,
                                modifier = Modifier.testTag("interactive_pet_avatar")
                            )
                        }

                        // Top-Right 3D/2D Mode Toggle Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.88f),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp, top = 6.dp)
                                .clickable { is3DMode = !is3DMode }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (is3DMode) "🎮 3D全景" else "🎨 2D经典",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (is3DMode) Color(0xFF6750A4) else Color(0xFF49454F)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isSleeping) "💤 正在酣睡中... 轻触唤醒或查看状态"
                        else if (is3DMode) "✨ 按住滑动可360°旋转3D萌宠，轻触互动抚摸！"
                        else "✨ 点击轻抚「${pet.name}」，与它亲密互动吧！",
                        fontSize = 12.sp,
                        color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Floating Action Buttons in Immersive Viewport Dock
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImmersiveActionButton(
                            emoji = "🍖",
                            label = "投喂",
                            onClick = { showFoodSheet = true },
                            testTag = "feed_action_button"
                        )
                        ImmersiveActionButton(
                            emoji = "🧸",
                            label = "玩耍",
                            onClick = onPlay,
                            testTag = "play_action_button"
                        )
                        ImmersiveActionButton(
                            emoji = "🚿",
                            label = "清洁",
                            onClick = onBath,
                            testTag = "bath_action_button"
                        )
                        ImmersiveActionButton(
                            emoji = if (isSleeping) "☀️" else "💤",
                            label = if (isSleeping) "叫醒" else "哄睡",
                            onClick = onToggleSleep,
                            testTag = "sleep_action_button"
                        )
                        ImmersiveActionButton(
                            emoji = "👓",
                            label = "AR互动",
                            onClick = onOpenAR,
                            testTag = "ar_action_button"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two-column Quick Summary Cards matching Immersive UI Design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Growth Record
                val expNeeded = pet.level * 100
                val progressPercent = ((pet.exp.toFloat() / expNeeded).coerceIn(0f, 1f) * 100).toInt()
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSleeping) Color(0xFF1D1B20) else Color(0xFFF3EDF7)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "成长记录",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F)
                            )
                            Text(
                                text = "EXP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4)
                            )
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$progressPercent%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSleeping) Color.White else Color(0xFF1D1B20)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "距下一等级",
                                fontSize = 10.sp,
                                color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F),
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF6750A4),
                            trackColor = if (isSleeping) Color(0xFF2B2831) else Color(0xFFE6E0E9)
                        )
                    }
                }

                // Card 2: Status overview
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSleeping) Color(0xFF1D1B20) else Color(0xFFF3EDF7)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "体征数据",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F)
                            )
                            Text(
                                text = "⚖️ 健硕",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4)
                            )
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${pet.weight}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSleeping) Color.White else Color(0xFF1D1B20)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "kg 体重",
                                fontSize = 10.sp,
                                color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F),
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        Text(
                            text = if (isSleeping) "睡眠充能中 💤" else "“${pet.catchphrase}”",
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Tasks Summary Card (留存日常任务入口)
            DailyTasksSummaryCard(
                pet = pet,
                tasks = dailyTasks,
                isSleeping = isSleeping,
                onOpenDailyTasks = { showDailyTasksSheet = true },
                onClaimReward = onClaimDailyTaskReward,
                onActionNavigate = { category ->
                    when (category) {
                        "CLEAN" -> onBath()
                        "FEED" -> showFoodSheet = true
                        "PLAY" -> onPlay()
                        "LOVE" -> onCuddle()
                        "DIARY" -> onNavigateToGrowth()
                        "SHARE" -> onNavigateToCommunity()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vital Stats Overview Card
            PetVitalStatsCard(pet = pet, isSleeping = isSleeping)
        }
    }

    // Modal Bottom Sheet for Daily Tasks
    if (showDailyTasksSheet) {
        DailyTasksModalSheet(
            pet = pet,
            tasks = dailyTasks,
            onDismiss = { showDailyTasksSheet = false },
            onClaimReward = onClaimDailyTaskReward,
            onClaimMilestone = onClaimMilestoneReward,
            onCheckIn = onCheckIn,
            onActionNavigate = { category ->
                showDailyTasksSheet = false
                when (category) {
                    "CLEAN" -> onBath()
                    "FEED" -> showFoodSheet = true
                    "PLAY" -> onPlay()
                    "LOVE" -> onCuddle()
                    "DIARY" -> onNavigateToGrowth()
                    "SHARE" -> onNavigateToCommunity()
                }
            }
        )
    }

    // Modal Bottom Sheet for Food Selection
    if (showFoodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFoodSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🍲 萌宠美食食盘",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralPrimary
                    )
                    Text(
                        text = "当前饱腹度: ${pet.hunger}%",
                        fontSize = 14.sp,
                        color = Color(0xFF756A63)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                foodList.forEach { food ->
                    FoodItemRow(
                        food = food,
                        onSelect = {
                            onFeed(food)
                            showFoodSheet = false
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PetHeaderSection(
    pet: PetEntity,
    allPets: List<PetEntity>,
    isSleeping: Boolean,
    isSoundMuted: Boolean = false,
    isPlayingSound: Boolean = false,
    currentSoundLabel: String? = null,
    onToggleSoundMute: () -> Unit = {},
    onSwitchPet: (Long) -> Unit,
    onNavigateToCustomizer: () -> Unit,
    onNavigateToShop: () -> Unit = {},
    onOpenAR: () -> Unit,
    onOpenDailyTasks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSleeping) Color(0xFF382F54) else Color(0xFFEADDFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = if (isSleeping) Color(0xFFEADDFF) else Color(0xFF21005D),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = pet.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSleeping) Color.White else Color(0xFF1D1B20)
                    )
                    Text(
                        text = "${pet.species} · Lv.${pet.level}",
                        fontSize = 12.sp,
                        color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F)
                    )
                }
            }

            // Quick action buttons: Sound Control, Coins badge, Shop Market, AR World & Customization
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PetSoundControlBadge(
                    isMuted = isSoundMuted,
                    isPlaying = isPlayingSound,
                    soundLabel = currentSoundLabel,
                    onToggleMute = onToggleSoundMute
                )

                Surface(
                    onClick = onNavigateToShop,
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSleeping) Color(0xFF2B2831) else Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    modifier = Modifier.testTag("header_coins_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("🪙", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${pet.coins}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSleeping) Color(0xFFFFCC80) else Color(0xFFE65100)
                        )
                    }
                }

                Surface(
                    onClick = onNavigateToShop,
                    shape = CircleShape,
                    color = if (isSleeping) Color(0xFF2B2831) else Color(0xFFFFECB3),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("header_shop_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "虚拟装扮商城",
                            tint = if (isSleeping) Color(0xFFFFD54F) else Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    onClick = onOpenAR,
                    shape = CircleShape,
                    color = if (isSleeping) Color(0xFF2B2831) else Color(0xFFEADDFF),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("open_ar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = "开启AR互动",
                            tint = if (isSleeping) Color.White else Color(0xFF21005D),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    onClick = onNavigateToCustomizer,
                    shape = CircleShape,
                    color = if (isSleeping) Color(0xFF2B2831) else Color(0xFFE8DEF8),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "个性定制",
                            tint = if (isSleeping) Color.White else Color(0xFF6750A4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Multiple Pets Switcher Row
        if (allPets.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allPets) { itemPet ->
                    val isSelected = itemPet.id == pet.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF6750A4) else (if (isSleeping) Color(0xFF2B2831) else Color(0xFFEADDFF).copy(alpha = 0.5f)),
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier.clickable { onSwitchPet(itemPet.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isSelected) "🐾 " else "🐱 ",
                                fontSize = 12.sp
                            )
                            Text(
                                text = itemPet.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else (if (isSleeping) Color.White else Color(0xFF21005D))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImmersiveActionButton(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 5.dp,
        modifier = Modifier
            .size(width = 64.dp, height = 64.dp)
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D1B20)
            )
        }
    }
}

@Composable
private fun PetVitalStatsCard(pet: PetEntity, isSleeping: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSleeping) Color(0xFF1D1B20) else Color(0xFFF3EDF7)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "萌宠健康指标",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSleeping) Color.White else Color(0xFF1D1B20)
                )
                Text(
                    text = "健康评估: ${if (pet.hunger > 50 && pet.happiness > 50) "良好 ⭐" else "偏低 ⚠️"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6750A4)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Core Status Bars in 2x2 Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Hunger
                StatBarItem(
                    title = "饱腹度",
                    value = pet.hunger,
                    emoji = "🍖",
                    color = Color(0xFF6750A4),
                    isDark = isSleeping,
                    modifier = Modifier.weight(1f)
                )
                // Happiness
                StatBarItem(
                    title = "心情快乐",
                    value = pet.happiness,
                    emoji = "💖",
                    color = HeartRed,
                    isDark = isSleeping,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Cleanliness
                StatBarItem(
                    title = "身体洁净",
                    value = pet.cleanliness,
                    emoji = "🫧",
                    color = BubbleBlue,
                    isDark = isSleeping,
                    modifier = Modifier.weight(1f)
                )
                // Energy
                StatBarItem(
                    title = "体力充沛",
                    value = pet.energy,
                    emoji = "⚡",
                    color = EnergyGreen,
                    isDark = isSleeping,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatBarItem(
    title: String,
    value: Int,
    emoji: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$emoji $title",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFCAC4D0) else Color(0xFF49454F)
            )
            Text(
                text = "$value%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = if (isDark) Color(0xFF2B2831) else Color(0xFFE6E0E9)
        )
    }
}

@Composable
private fun CareActionsDock(
    isSleeping: Boolean,
    onFeedClick: () -> Unit,
    onBathClick: () -> Unit,
    onPlayClick: () -> Unit,
    onSleepClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSleeping) Color(0xFF1E233E) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "陪伴抚育指令",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSleeping) Color.White else Color(0xFF3E2723)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CareDockButton(
                    icon = Icons.Default.Restaurant,
                    label = "投喂",
                    containerColor = Color(0xFFFFCCBC),
                    contentColor = Color(0xFFD84315),
                    onClick = onFeedClick,
                    testTag = "feed_action_button"
                )
                CareDockButton(
                    icon = Icons.Default.Bathtub,
                    label = "洗香香",
                    containerColor = Color(0xFFB2EBF2),
                    contentColor = Color(0xFF00838F),
                    onClick = onBathClick,
                    testTag = "bath_action_button"
                )
                CareDockButton(
                    icon = Icons.Default.SportsBaseball,
                    label = "玩耍逗趣",
                    containerColor = Color(0xFFC8E6C9),
                    contentColor = Color(0xFF2E7D32),
                    onClick = onPlayClick,
                    testTag = "play_action_button"
                )
                CareDockButton(
                    icon = if (isSleeping) Icons.Default.WbSunny else Icons.Default.Bedtime,
                    label = if (isSleeping) "叫醒" else "哄睡",
                    containerColor = if (isSleeping) Color(0xFFFFECB3) else Color(0xFFD1C4E9),
                    contentColor = if (isSleeping) Color(0xFFF57F17) else Color(0xFF512DA8),
                    onClick = onSleepClick,
                    testTag = "sleep_action_button"
                )
            }
        }
    }
}

@Composable
private fun CareDockButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5D4037)
        )
    }
}

@Composable
private fun FoodItemRow(food: FoodItem, onSelect: () -> Unit) {
    OutlinedCard(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_item_${food.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xFFF3EDF7)
        ),
        border = BorderStroke(1.dp, Color(0xFFE8DEF8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = food.iconEmoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = food.description,
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "+${food.hungerRestore} 饱腹",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                    Text(
                        text = "+${food.happinessRestore} 心情",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HeartRed
                    )
                    Text(
                        text = "+${food.expGain} EXP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("喂食", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun DailyTasksSummaryCard(
    pet: PetEntity,
    tasks: List<DailyTaskEntity>,
    isSleeping: Boolean,
    onOpenDailyTasks: () -> Unit,
    onClaimReward: (String) -> Unit,
    onActionNavigate: (String) -> Unit
) {
    val completedCount = tasks.count { it.currentProgress >= it.targetProgress }
    val totalCount = tasks.size.coerceAtLeast(1)
    val claimableCount = tasks.count { it.currentProgress >= it.targetProgress && !it.isClaimed }
    val isCheckedInToday = pet.lastCheckInDate == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSleeping) Color(0xFF1E1B24) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (claimableCount > 0) Color(0xFFFFB74D) else if (isSleeping) Color(0xFF382F54) else Color(0xFFE8DEF8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("daily_tasks_summary_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title, Streak, Coins
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📅 每日陪伴任务",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSleeping) Color.White else Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSleeping) Color(0xFF3E2723) else Color(0xFFFFEBE5)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "🔥", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "连续${pet.dailyCheckInStreak}天",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF7043)
                            )
                        }
                    }
                }

                // Coin Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSleeping) Color(0xFF2B2831) else Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    modifier = Modifier.clickable(onClick = onOpenDailyTasks)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${pet.coins} 币",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSleeping) Color(0xFFFFCC80) else Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar and Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日进度: $completedCount / $totalCount 任务达成",
                    fontSize = 12.sp,
                    color = if (isSleeping) Color(0xFFCAC4D0) else Color(0xFF49454F)
                )
                if (claimableCount > 0) {
                    Text(
                        text = "🎁 有 $claimableCount 个奖励待领！",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralPrimary
                    )
                } else if (!isCheckedInToday) {
                    Text(
                        text = "🌟 今日尚未签到打卡",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (completedCount.toFloat() / totalCount).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CoralPrimary,
                trackColor = if (isSleeping) Color(0xFF2B2831) else Color(0xFFF3EDF7)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2 Quick Preview Task Items
            val previewTasks = tasks.filter { !it.isClaimed }.take(2).ifEmpty { tasks.take(2) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                previewTasks.forEach { task ->
                    val isDone = task.currentProgress >= task.targetProgress
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSleeping) Color(0xFF2B2831) else Color(0xFFF8F6FA),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = task.iconEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSleeping) Color.White else Color(0xFF1D1B20)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${task.rewardCoins}🪙",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                                Text(
                                    text = "进度: ${task.currentProgress}/${task.targetProgress}",
                                    fontSize = 11.sp,
                                    color = if (isSleeping) Color(0xFF90A4AE) else Color(0xFF757575)
                                )
                            }

                            when {
                                task.isClaimed -> {
                                    Text(
                                        text = "已领取✓",
                                        fontSize = 11.sp,
                                        color = Color(0xFF9E9E9E),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                isDone -> {
                                    Button(
                                        onClick = { onClaimReward(task.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("领取", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    FilledTonalButton(
                                        onClick = { onActionNavigate(task.category) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("去完成", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenDailyTasks)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "查看全部日常任务与签到奖励 (${tasks.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CoralPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
