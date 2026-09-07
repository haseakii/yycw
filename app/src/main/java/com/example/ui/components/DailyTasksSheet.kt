package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyTaskEntity
import com.example.data.model.PetEntity
import com.example.data.model.WEEKLY_CHECK_IN_REWARDS
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTasksModalSheet(
    pet: PetEntity?,
    tasks: List<DailyTaskEntity>,
    onDismiss: () -> Unit,
    onClaimReward: (String) -> Unit,
    onClaimMilestone: (Int) -> Unit,
    onCheckIn: () -> Unit,
    onActionNavigate: (String) -> Unit, // category e.g. "CLEAN", "FEED", "PLAY", "LOVE", "DIARY", "SHARE"
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFAF7FD),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD0BCFF))
            )
        }
    ) {
        DailyTasksSheetContent(
            pet = pet,
            tasks = tasks,
            onClose = onDismiss,
            onClaimReward = onClaimReward,
            onClaimMilestone = onClaimMilestone,
            onCheckIn = onCheckIn,
            onActionNavigate = onActionNavigate
        )
    }
}

@Composable
fun DailyTasksSheetContent(
    pet: PetEntity?,
    tasks: List<DailyTaskEntity>,
    onClose: () -> Unit,
    onClaimReward: (String) -> Unit,
    onClaimMilestone: (Int) -> Unit,
    onCheckIn: () -> Unit,
    onActionNavigate: (String) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val completedCount = tasks.count { it.currentProgress >= it.targetProgress }
    val totalCount = tasks.size.coerceAtLeast(1)
    val totalClaimableCount = tasks.count { it.currentProgress >= it.targetProgress && !it.isClaimed }
    val progressFloat by animateFloatAsState(
        targetValue = (completedCount.toFloat() / totalCount).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "task_progress_anim"
    )

    val isCheckedInToday = pet?.lastCheckInDate == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val streakDays = pet?.dailyCheckInStreak ?: 1
    val coinsBalance = pet?.coins ?: 0

    val filteredTasks = remember(tasks, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") tasks
        else tasks.filter { it.category == selectedCategoryFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        // Sheet Header: Title, Coin Balance, Close
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅 每日陪伴日常",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D)
                        )
                        if (totalClaimableCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CoralPrimary,
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = "$totalClaimableCount 可领取",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "完成每日任务赚取装扮币，为萌宠解锁百变新装！",
                        fontSize = 12.sp,
                        color = Color(0xFF6750A4)
                    )
                }

                // Coin Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF3E0),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coinsBalance",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }

        // Section 1: Weekly Check-In Card (每日留存打卡)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFFE8DEF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("daily_checkin_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF7043),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "连续相伴签到",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFEBE5)
                            ) {
                                Text(
                                    text = "已连续 $streakDays 天",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onCheckIn,
                            enabled = !isCheckedInToday,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralPrimary,
                                disabledContainerColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("check_in_button")
                        ) {
                            Text(
                                text = if (isCheckedInToday) "今日已签到 ✓" else "立即打卡",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCheckedInToday) Color(0xFF757575) else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7 Days Check-in Track
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(WEEKLY_CHECK_IN_REWARDS) { index, reward ->
                            val dayNum = reward.dayNumber
                            val isPast = dayNum < streakDays || (dayNum == streakDays && isCheckedInToday)
                            val isCurrentToday = dayNum == streakDays && !isCheckedInToday

                            CheckInDayItem(
                                dayNumber = dayNum,
                                coins = reward.coinReward,
                                isSpecial = reward.isSpecial,
                                specialGift = reward.specialGiftName,
                                isChecked = isPast,
                                isToday = isCurrentToday
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Daily Progress Bar & Milestone Chests
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFFE8DEF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth().testTag("daily_milestone_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "今日日常进度",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "$completedCount / $totalCount 任务已达成",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CoralPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = CoralPrimary,
                            trackColor = Color(0xFFF3EDF7)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Milestone Chest Rewards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Chest 1: 3 tasks
                        MilestoneChestCard(
                            tier = 1,
                            title = "进阶陪伴礼包",
                            targetTasks = 3,
                            completedTasks = completedCount,
                            rewardCoins = 40,
                            modifier = Modifier.weight(1f),
                            onClaim = { onClaimMilestone(1) }
                        )

                        // Chest 2: All tasks (全勤)
                        MilestoneChestCard(
                            tier = 2,
                            title = "全勤至尊宝箱",
                            targetTasks = totalCount,
                            completedTasks = completedCount,
                            rewardCoins = 80,
                            isGrandPrize = true,
                            modifier = Modifier.weight(1f),
                            onClaim = { onClaimMilestone(2) }
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "全部日常",
                    "CLEAN" to "清洁沐浴",
                    "FEED" to "营养投喂",
                    "PLAY" to "互动玩耍",
                    "DIARY" to "手账记录",
                    "SHARE" to "社区广场"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filters) { (key, label) ->
                        val selected = selectedCategoryFilter == key
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategoryFilter = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEADDFF),
                                selectedLabelColor = Color(0xFF21005D)
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Task Items List
        items(filteredTasks, key = { it.id }) { task ->
            DailyTaskItemCard(
                task = task,
                onClaim = { onClaimReward(task.id) },
                onNavigate = { onActionNavigate(task.category) }
            )
        }
    }
}

@Composable
private fun CheckInDayItem(
    dayNumber: Int,
    coins: Int,
    isSpecial: Boolean,
    specialGift: String?,
    isChecked: Boolean,
    isToday: Boolean
) {
    val bgColor = when {
        isChecked -> Color(0xFFE8F5E9)
        isToday -> Color(0xFFFFF3E0)
        isSpecial -> Color(0xFFF3E5F5)
        else -> Color(0xFFF5F5F5)
    }

    val borderColor = when {
        isChecked -> Color(0xFF81C784)
        isToday -> Color(0xFFFF9800)
        isSpecial -> Color(0xFFBA68C8)
        else -> Color(0xFFE0E0E0)
    }

    Box(
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "第${dayNumber}天",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isChecked) Color(0xFF2E7D32) else Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已签到",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
            } else if (isSpecial) {
                Text(text = "🎁", fontSize = 18.sp)
            } else {
                Text(text = "🪙", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "+$coins",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isChecked -> Color(0xFF2E7D32)
                    isToday -> Color(0xFFE65100)
                    else -> Color(0xFF424242)
                }
            )
        }
    }
}

@Composable
private fun MilestoneChestCard(
    tier: Int,
    title: String,
    targetTasks: Int,
    completedTasks: Int,
    rewardCoins: Int,
    isGrandPrize: Boolean = false,
    modifier: Modifier = Modifier,
    onClaim: () -> Unit
) {
    val isReady = completedTasks >= targetTasks
    var isOpened by remember { mutableStateOf(false) }

    val bgGradient = if (isReady) {
        if (isGrandPrize) {
            Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFB300)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8)))
        }
    } else {
        Brush.linearGradient(listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE)))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isReady) Color(0xFFFFB300) else Color(0xFFE0E0E0)),
        modifier = modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .background(bgGradient)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = if (isGrandPrize) "🏆" else "🎁", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isReady) Color(0xFF3E2723) else Color(0xFF616161)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "达成 $targetTasks 个任务",
                fontSize = 10.sp,
                color = if (isReady) Color(0xFF4E342E) else Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isOpened) {
                Text(
                    text = "已领取奖励 ✓",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            } else if (isReady) {
                Button(
                    onClick = {
                        isOpened = true
                        onClaim()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("claim_chest_button_$tier")
                ) {
                    Text("开宝箱 +$rewardCoins", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "+$rewardCoins 币",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTaskItemCard(
    task: DailyTaskEntity,
    onClaim: () -> Unit,
    onNavigate: () -> Unit
) {
    val isComplete = task.currentProgress >= task.targetProgress
    val isClaimed = task.isClaimed

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isClaimed) Color(0xFFF8F6FA) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isComplete && !isClaimed) CoralLight else Color(0xFFE8DEF8)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isComplete && !isClaimed) 4.dp else 1.dp
        ),
        modifier = Modifier.fillMaxWidth().testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Icon Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isClaimed -> Color(0xFFE0E0E0)
                            isComplete -> Color(0xFFFFEBE5)
                            else -> Color(0xFFEDE7F6)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = task.iconEmoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Title, Desc, Progress & Rewards
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClaimed) Color(0xFF757575) else Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Category Badge
                    val categoryName = when (task.category) {
                        "CLEAN" -> "日常清洁"
                        "FEED" -> "营养投喂"
                        "PLAY" -> "陪伴互动"
                        "LOVE" -> "亲密抚摸"
                        "DIARY" -> "手账日记"
                        "SHARE" -> "社区分享"
                        else -> "陪伴日常"
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF3EDF7)
                    ) {
                        Text(
                            text = categoryName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6750A4),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = task.description,
                    fontSize = 11.sp,
                    color = if (isClaimed) Color(0xFF9E9E9E) else Color(0xFF49454F),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reward Coins Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF8E1),
                        border = BorderStroke(0.5.dp, Color(0xFFFFD54F))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(text = "🪙", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "+${task.rewardCoins}币",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }

                    // Reward EXP Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDE7F6)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${task.rewardExp} EXP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4)
                            )
                        }
                    }

                    // Progress Number
                    Text(
                        text = "${task.currentProgress}/${task.targetProgress}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isComplete) Color(0xFF2E7D32) else Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            when {
                isClaimed -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEEEEE),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "已领取 ✓",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
                isComplete -> {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("claim_task_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "领取",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onNavigate,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralPrimary),
                        border = BorderStroke(1.dp, CoralPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("go_task_button_${task.id}")
                    ) {
                        Text(
                            text = "去完成",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
