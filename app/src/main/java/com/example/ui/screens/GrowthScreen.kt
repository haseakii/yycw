package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrowthRecordEntity
import com.example.data.model.PetEntity
import com.example.ui.components.GrowthChartSection
import com.example.ui.theme.BubbleBlue
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.StarGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GrowthScreen(
    pet: PetEntity?,
    records: List<GrowthRecordEntity>,
    onAddDiary: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    if (pet == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无活跃宠物档案", fontSize = 16.sp, color = Color.Gray)
        }
        return
    }

    val daysTogether = ((System.currentTimeMillis() - pet.birthday) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)

    val growthStageTitle = when {
        pet.level <= 2 -> "萌芽幼宠期"
        pet.level <= 5 -> "活力成长期"
        pet.level <= 8 -> "优雅成熟期"
        else -> "幻耀巅峰期"
    }

    val filteredRecords = remember(records, selectedFilter) {
        when (selectedFilter) {
            "LEVEL" -> records.filter { it.eventType == "LEVEL_UP" }
            "FEED" -> records.filter { it.eventType == "FEED" }
            "DIARY" -> records.filter { it.eventType == "DIARY" }
            else -> records
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFEF7FF),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_diary_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = "写手账")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("写手账", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📖「${pet.name}」成长档案",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "记录相遇以来的每一个温馨足迹",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                    Surface(
                        color = Color(0xFFEADDFF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = growthStageTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Recharts / D3 Inspired Interactive Data Visualization
            item {
                GrowthChartSection(
                    pet = pet,
                    records = records
                )
            }

            // Stats 4-Pack Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "成长里程碑数据",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1D1B20)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            MilestoneStatBadge(
                                icon = Icons.Default.CalendarToday,
                                count = "$daysTogether 天",
                                label = "相伴时光",
                                iconColor = Color(0xFF6750A4)
                            )
                            MilestoneStatBadge(
                                icon = Icons.Default.Restaurant,
                                count = "${pet.totalFeedCount} 次",
                                label = "爱心喂养",
                                iconColor = HeartRed
                            )
                            MilestoneStatBadge(
                                icon = Icons.Default.Bathtub,
                                count = "${pet.totalBathCount} 次",
                                label = "舒爽泡泡",
                                iconColor = BubbleBlue
                            )
                            MilestoneStatBadge(
                                icon = Icons.Default.SportsBaseball,
                                count = "${pet.totalPlayCount} 次",
                                label = "欢笑玩耍",
                                iconColor = EnergyGreen
                            )
                        }
                    }
                }
            }

            // Badges showcase
            item {
                Text(
                    text = "🏆 萌宠羁绊勋章",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        BadgeCard(
                            emoji = "🏡",
                            title = "云家相伴",
                            desc = "开启云养生活",
                            unlocked = true
                        )
                    }
                    item {
                        BadgeCard(
                            emoji = "🍖",
                            title = "干饭达人",
                            desc = "投喂达5次",
                            unlocked = pet.totalFeedCount >= 5
                        )
                    }
                    item {
                        BadgeCard(
                            emoji = "🧼",
                            title = "洗白白标兵",
                            desc = "香香沐浴3次",
                            unlocked = pet.totalBathCount >= 3
                        )
                    }
                    item {
                        BadgeCard(
                            emoji = "⭐",
                            title = "闪耀升阶",
                            desc = "晋级Lv.2+",
                            unlocked = pet.level >= 2
                        )
                    }
                    item {
                        BadgeCard(
                            emoji = "🐾",
                            title = "百日挚友",
                            desc = "相伴30天",
                            unlocked = daysTogether >= 30
                        )
                    }
                }
            }

            // Filter row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏳ 成长时光轴 (${filteredRecords.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D1B20)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        label = "全部足迹",
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" }
                    )
                    FilterChip(
                        label = "🌟 升级突破",
                        selected = selectedFilter == "LEVEL",
                        onClick = { selectedFilter = "LEVEL" }
                    )
                    FilterChip(
                        label = "🍲 投喂美食",
                        selected = selectedFilter == "FEED",
                        onClick = { selectedFilter = "FEED" }
                    )
                    FilterChip(
                        label = "📖 专属手账",
                        selected = selectedFilter == "DIARY",
                        onClick = { selectedFilter = "DIARY" }
                    )
                }
            }

            // Timeline Items
            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无对应类型的足迹，快去互动或记录手账吧！",
                            fontSize = 13.sp,
                            color = Color(0xFF79747E)
                        )
                    }
                }
            } else {
                items(filteredRecords) { record ->
                    GrowthTimelineItem(record = record)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Diary Dialog
    if (showAddDialog) {
        var diaryTitle by remember { mutableStateOf("") }
        var diaryContent by remember { mutableStateOf("") }
        var diaryMood by remember { mutableStateOf("超开心") }
        val moodOptions = listOf("超开心", "饱饱哒", "黏糊糊", "傲娇萌", "打哈欠")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("📝 记录与「${pet.name}」的时光手账", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = diaryTitle,
                        onValueChange = { diaryTitle = it },
                        label = { Text("手账标题") },
                        placeholder = { Text("例如：今天学会了伸小懒腰") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diary_title_input")
                    )

                    OutlinedTextField(
                        value = diaryContent,
                        onValueChange = { diaryContent = it },
                        label = { Text("心情与小故事") },
                        placeholder = { Text("记下小宠物今天发生的趣事或可爱瞬间...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diary_content_input")
                    )

                    Text(text = "今日宠物心情标签:", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        moodOptions.forEach { mood ->
                            val isSelected = diaryMood == mood
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CoralPrimary else Color(0xFFF5F5F5),
                                modifier = Modifier.clickable { diaryMood = mood }
                            ) {
                                Text(
                                    text = mood,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF616161),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (diaryTitle.isNotBlank()) {
                            onAddDiary(diaryTitle, diaryContent, diaryMood)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.testTag("save_diary_button")
                ) {
                    Text("保存时光手账")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun MilestoneStatBadge(
    icon: ImageVector,
    count: String,
    label: String,
    iconColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF49454F))
    }
}

@Composable
private fun BadgeCard(
    emoji: String,
    title: String,
    desc: String,
    unlocked: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (unlocked) Color.White else Color(0xFFF3EDF7),
        shadowElevation = if (unlocked) 2.dp else 0.dp,
        border = if (unlocked) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)) else null,
        modifier = Modifier.width(108.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) Color(0xFF1D1B20) else Color(0xFF79747E)
            )
            Text(
                text = desc,
                fontSize = 10.sp,
                color = if (unlocked) Color(0xFF6750A4) else Color(0xFFCAC4D0)
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF49454F),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun GrowthTimelineItem(record: GrowthRecordEntity) {
    val dateFormat = remember { SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()) }
    val timeStr = remember(record.timestamp) { dateFormat.format(Date(record.timestamp)) }

    val (iconEmoji, tagColor) = when (record.eventType) {
        "LEVEL_UP" -> Pair("🌟", StarGold)
        "FEED" -> Pair("🍲", Color(0xFF6750A4))
        "BATH" -> Pair("🫧", BubbleBlue)
        "PLAY" -> Pair("🎾", EnergyGreen)
        "ADOPTION" -> Pair("🏡", Color(0xFFAB47BC))
        else -> Pair("📝", Color(0xFF5C6BC0))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("growth_record_item_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = iconEmoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    if (record.moodTag.isNotBlank()) {
                        Surface(
                            color = tagColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = record.moodTag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tagColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.description,
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = Color(0xFF79747E)
                    )
                    if (record.extraMetric.isNotBlank()) {
                        Text(
                            text = record.extraMetric,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                    }
                }
            }
        }
    }
}
