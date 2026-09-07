package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommentEntity
import com.example.data.model.CommunityOpponent
import com.example.data.model.CommunityPostEntity
import com.example.data.model.DEFAULT_COMMUNITY_OPPONENTS
import com.example.data.model.PetEntity
import com.example.data.model.calculatePetBattleStats
import com.example.data.model.toCommunityOpponent
import com.example.ui.components.Pet3DModelView
import com.example.ui.components.PetBattleArenaDialog
import com.example.ui.components.PetModelView
import com.example.ui.theme.BubbleBlue
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.StarGold
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityScreen(
    currentPet: PetEntity?,
    posts: List<CommunityPostEntity>,
    arenaRating: Int = 1250,
    onToggleLike: (postId: Long, currentlyLiked: Boolean) -> Unit,
    onAddComment: (postId: Long, content: String) -> Unit,
    onPublishPost: (content: String, tag: String) -> Unit,
    onRecordBattleVictory: (coinsWon: Int, expGained: Int, summary: String) -> Unit = { _, _, _ -> },
    getCommentsFlow: (postId: Long) -> Flow<List<CommentEntity>>,
    modifier: Modifier = Modifier
) {
    var showPublishDialog by remember { mutableStateOf(false) }
    var visitingPetPost by remember { mutableStateOf<CommunityPostEntity?>(null) }
    var viewingCommentsPost by remember { mutableStateOf<CommunityPostEntity?>(null) }
    var activeBattleOpponent by remember { mutableStateOf<CommunityOpponent?>(null) }
    var selectedCommunityTab by remember { mutableStateOf(0) } // 0: 广场动态, 1: 萌宠擂台 ⚔️
    var selectedTagFilter by remember { mutableStateOf("ALL") }

    val tags = listOf(
        "ALL" to "全部广场",
        "萌宠日常" to "🐱 萌宠日常",
        "物种创作者" to "✨ 物种创作者",
        "互动求串门" to "🏡 互动求串门",
        "成长见证" to "🏆 成长见证"
    )

    val filteredPosts = remember(posts, selectedTagFilter) {
        if (selectedTagFilter == "ALL") posts
        else posts.filter { it.tag == selectedTagFilter }
    }

    // Combine default opponents + community post pets
    val allOpponents = remember(posts) {
        val postOpponents = posts.take(6).map { it.toCommunityOpponent() }
        DEFAULT_COMMUNITY_OPPONENTS + postOpponents
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFEF7FF),
        floatingActionButton = {
            if (selectedCommunityTab == 0) {
                FloatingActionButton(
                    onClick = { showPublishDialog = true },
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("publish_post_fab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "晒晒萌宠")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("晒晒萌宠", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        if (allOpponents.isNotEmpty()) {
                            activeBattleOpponent = allOpponents.random()
                        }
                    },
                    containerColor = Color(0xFFE65100),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("quick_match_fab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text("⚡ 快速匹配决斗", fontWeight = FontWeight.Bold, color = Color.White)
                    }
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedCommunityTab == 0) "🐾 萌宠社交社区" else "⚔️ 萌宠竞技擂台",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = if (selectedCommunityTab == 0) "展示自创萌宠、交流养宠心得、云串门与切磋！"
                            else "全服萌宠3D回合制战力切磋，争夺萌界霸主！",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }

            // Dual Tab Switcher: 广场动态 vs 萌宠擂台
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEDE7F6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedCommunityTab == 0) Color(0xFF6750A4) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCommunityTab = 0 }
                        ) {
                            Text(
                                text = "💬 广场动态",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCommunityTab == 0) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedCommunityTab == 1) Color(0xFFE65100) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCommunityTab = 1 }
                        ) {
                            Text(
                                text = "⚔️ 萌宠竞技擂台",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCommunityTab == 1) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (selectedCommunityTab == 0) {
                // TAG FILTER CHIPS
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tags) { (tagKey, tagLabel) ->
                            val isSelected = selectedTagFilter == tagKey
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                                modifier = Modifier.clickable { selectedTagFilter = tagKey }
                            ) {
                                Text(
                                    text = tagLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF49454F),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // POSTS FEED
                items(filteredPosts) { post ->
                    CommunityPostCard(
                        post = post,
                        onLikeClick = { onToggleLike(post.id, post.isLiked) },
                        onCommentClick = { viewingCommentsPost = post },
                        onVisitClick = { visitingPetPost = post },
                        onChallengeClick = {
                            activeBattleOpponent = post.toCommunityOpponent()
                        }
                    )
                }
            } else {
                // ==================== BATTLE ARENA CONTENT ====================
                // 1. ARENA RANK & RATING BANNER
                item {
                    val tierTitle = when {
                        arenaRating < 1100 -> "萌新学徒"
                        arenaRating < 1200 -> "白银萌宠 ★★"
                        arenaRating < 1400 -> "黄金斗士 ★★★"
                        arenaRating < 1600 -> "钻石宠神 ★★★"
                        else -> "传说萌主 👑"
                    }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF311B92)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏆", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = tierTitle,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFFD54F)
                                        )
                                        Text(
                                            text = "第 1 赛季 • 萌界无双天梯",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFF8F00)
                                ) {
                                    Text(
                                        text = "天梯积分: $arenaRating",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. MY PET COMBAT READINESS CARD
                item {
                    if (currentPet != null) {
                        val myStats = remember(currentPet) { calculatePetBattleStats(currentPet) }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE0B2)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mini 3D Preview
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFFFF3E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Pet3DModelView(
                                            pet = currentPet,
                                            modelSize = 85.dp,
                                            enableOrbitDrag = false
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentPet.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D1B20)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFFFF3E0),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Lv.${currentPet.level}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "综合战力: ${myStats.combatPower} CP",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFE65100)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "生命: ${myStats.maxHp}  •  攻击: ${myStats.attack}  •  防御: ${myStats.defense}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF49454F)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF3EDF7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🐾 暂无出战萌宠，请先领养或定制一只萌宠吧！",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // 3. CHALLENGEABLE OPPONENTS SECTION
                item {
                    Text(
                        text = "⚔️ 擂台守擂者与社区劲敌",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(allOpponents) { opponent ->
                    val oppPet = remember(opponent) { opponent.toPetEntity() }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Opponent 3D Mini View
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3EDF7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Pet3DModelView(
                                    pet = oppPet,
                                    modelSize = 72.dp,
                                    enableOrbitDrag = false
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = opponent.petName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D1B20)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Lv.${opponent.level}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6750A4)
                                    )
                                }
                                Text(
                                    text = "主人: ${opponent.masterName} • ${opponent.arenaTier}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF79747E)
                                )
                                Text(
                                    text = "战力: ${opponent.stats.combatPower} CP (HP:${opponent.stats.maxHp} ATK:${opponent.stats.attack})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { activeBattleOpponent = opponent },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE65100),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("挑战", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 4. ARENA LEADERBOARD TOP 5
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "👑 萌界天梯战神榜 (Top 5)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                }

                item {
                    val leaderboard = remember(allOpponents) {
                        allOpponents.sortedByDescending { it.stats.combatPower }.take(5)
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            leaderboard.forEachIndexed { index, opp ->
                                val rankBadge = when (index) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else -> "#${index + 1}"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rankBadge,
                                        fontSize = if (index < 3) 18.sp else 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${opp.petName} (${opp.masterName})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1D1B20)
                                        )
                                        Text(
                                            text = "${opp.species} • Lv.${opp.level}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF79747E)
                                        )
                                    }
                                    Text(
                                        text = "${opp.stats.combatPower} CP",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                                if (index < leaderboard.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF3EDF7), thickness = 0.8.dp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // ==================== DIALOGS ====================

    // Publish Post Dialog
    if (showPublishDialog) {
        PublishPostDialog(
            currentPet = currentPet,
            onDismiss = { showPublishDialog = false },
            onPublish = { content, tag ->
                onPublishPost(content, tag)
                showPublishDialog = false
            }
        )
    }

    // Comments Modal Dialog
    viewingCommentsPost?.let { post ->
        CommentsDialog(
            post = post,
            commentsFlow = getCommentsFlow(post.id),
            onDismiss = { viewingCommentsPost = null },
            onSendComment = { text -> onAddComment(post.id, text) }
        )
    }

    // Visiting Neighbor's Pet Cozy Room Dialog
    visitingPetPost?.let { post ->
        VisitNeighborPetDialog(
            post = post,
            onDismiss = { visitingPetPost = null }
        )
    }

    // ⚔️ Community 3D Turn-Based Battle Arena Dialog
    activeBattleOpponent?.let { opponent ->
        if (currentPet != null) {
            PetBattleArenaDialog(
                userPet = currentPet,
                opponent = opponent,
                onDismiss = { activeBattleOpponent = null },
                onVictory = { coinsWon, expGained, summary ->
                    onRecordBattleVictory(coinsWon, expGained, summary)
                },
                onShareBattleReport = { report ->
                    onPublishPost(report, "成长见证")
                    activeBattleOpponent = null
                }
            )
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPostEntity,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onVisitClick: () -> Unit,
    onChallengeClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val timeStr = remember(post.timestamp) { dateFormat.format(Date(post.timestamp)) }

    // Mock dummy pet instance for rendering the post's pet model
    val postPetModel = remember(post) {
        PetEntity(
            id = post.id,
            name = post.petName,
            species = post.petSpecies,
            baseBodyType = post.baseBodyType,
            primaryColor = post.primaryColor,
            secondaryColor = post.secondaryColor,
            earStyle = post.earStyle,
            eyeStyle = post.eyeStyle,
            tailStyle = post.tailStyle,
            accessory = post.accessory,
            level = post.petLevel
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_${post.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top author bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEADDFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐾", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D1B20)
                    )
                    Text(text = timeStr, fontSize = 11.sp, color = Color(0xFF79747E))
                }
                Surface(
                    color = Color(0xFFEADDFF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "#${post.tag}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF21005D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Text
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Color(0xFF1D1B20),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pet Showcase Card inside Post
            Surface(
                color = Color(0xFFF3EDF7),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mini Pet Render
                    PetModelView(
                        pet = postPetModel,
                        petSize = 90.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.petName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D1B20)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFEADDFF),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Lv.${post.petLevel}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = post.petSpecies,
                            fontSize = 12.sp,
                            color = Color(0xFF6750A4),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onVisitClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6750A4),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("visit_pet_button_${post.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("串门", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = onChallengeClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE65100),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("challenge_pet_button_${post.id}")
                            ) {
                                Text("⚔️ 切磋", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Like, Comment, Visit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onLikeClick)
                        .testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (post.isLiked) HeartRed else Color(0xFF79747E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.likesCount}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (post.isLiked) HeartRed else Color(0xFF49454F)
                    )
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onCommentClick)
                        .testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "评论",
                        tint = Color(0xFF79747E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "互动评论",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF49454F)
                    )
                }

                // Visit Neighbor button
                Text(
                    text = "🏡 敲门串门 >",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6750A4),
                    modifier = Modifier.clickable(onClick = onVisitClick)
                )
            }
        }
    }
}

@Composable
private fun PublishPostDialog(
    currentPet: PetEntity?,
    onDismiss: () -> Unit,
    onPublish: (content: String, tag: String) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("萌宠日常") }
    val tags = listOf("萌宠日常", "成长见证", "物种创作者", "互动求串门")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("📢 分享「${currentPet?.name ?: "我的萌宠"}」的精彩日常", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (currentPet != null) {
                    Surface(
                        color = Color(0xFFFFF8F1),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            PetModelView(pet = currentPet, petSize = 60.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentPet.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currentPet.species} · Lv.${currentPet.level}",
                                    fontSize = 12.sp,
                                    color = CoralPrimary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text("动态分享内容") },
                    placeholder = { Text("记录今天宠物的趣事、新造型，邀请大家来串门互动吧...") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_post_content_input")
                )

                Text("选择动态标签:", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        val isSelected = selectedTag == tag
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CoralPrimary else Color(0xFFF5F5F5),
                            modifier = Modifier.clickable { selectedTag = tag }
                        ) {
                            Text(
                                text = tag,
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
                    if (contentText.isNotBlank()) {
                        onPublish(contentText, selectedTag)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_publish_post_button")
            ) {
                Text("立即发布")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = Color(0xFF6750A4)) }
        }
    )
}

@Composable
private fun CommentsDialog(
    post: CommunityPostEntity,
    commentsFlow: Flow<List<CommentEntity>>,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    val comments by commentsFlow.collectAsState(initial = emptyList())
    var newCommentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("💬 萌宠动态评论 (${comments.size})", fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("还没有评论呢，快来抢沙发打个招呼吧~", fontSize = 12.sp, color = Color(0xFF79747E))
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            Surface(
                                color = Color(0xFFF3EDF7),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = comment.authorName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6750A4)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = comment.content, fontSize = 13.sp, color = Color(0xFF1D1B20))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("写下友善评论...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onSendComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.testTag("send_comment_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "发送", tint = Color(0xFF6750A4))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭", color = Color(0xFF6750A4)) }
        }
    )
}

@Composable
private fun VisitNeighborPetDialog(
    post: CommunityPostEntity,
    onDismiss: () -> Unit
) {
    var visitorFeedback by remember { mutableStateOf<String?>(null) }
    var visitorHearts by remember { mutableStateOf(false) }

    val neighborPet = remember(post) {
        PetEntity(
            id = post.id,
            name = post.petName,
            species = post.petSpecies,
            baseBodyType = post.baseBodyType,
            primaryColor = post.primaryColor,
            secondaryColor = post.secondaryColor,
            earStyle = post.earStyle,
            eyeStyle = post.eyeStyle,
            tailStyle = post.tailStyle,
            accessory = post.accessory,
            level = post.petLevel
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏡 拜访「${post.authorName}」的萌宠小屋", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1D1B20))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Room Background Stage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color(0xFFEADDFF), Color(0xFFF3EDF7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    PetModelView(pet = neighborPet, petSize = 160.dp)

                    if (visitorHearts) {
                        Text(
                            text = "💖 💖 💖",
                            fontSize = 24.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "正在和「${neighborPet.name}」打招呼",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "${neighborPet.species} · 等级 Lv.${neighborPet.level}",
                    fontSize = 12.sp,
                    color = Color(0xFF6750A4)
                )

                if (visitorFeedback != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFEADDFF),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = visitorFeedback ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF21005D),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Neighbor Visit Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            visitorFeedback = "你给「${neighborPet.name}」赠送了一份美味肉干，它开心极了！"
                            visitorHearts = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("投喂小点心 🍖", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            visitorFeedback = "你温柔地轻抚了「${neighborPet.name}」的脑袋，它发出惬意的咕噜声~"
                            visitorHearts = true
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6750A4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("摸摸小肚肚 🐾", fontSize = 12.sp, color = Color(0xFF6750A4))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("完成拜访")
            }
        }
    )
}
