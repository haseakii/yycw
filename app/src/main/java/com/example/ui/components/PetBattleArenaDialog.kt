package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.PetSoundEngine
import com.example.data.model.BattleActionLog
import com.example.data.model.BattleSkill
import com.example.data.model.BattleSkillType
import com.example.data.model.CommunityOpponent
import com.example.data.model.PetEntity
import com.example.data.model.calculatePetBattleStats
import com.example.data.model.getPetBattleSkills
import com.example.ui.theme.BubbleBlue
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.HeartRed
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.StarGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Floating Damage or Healing popup text
 */
data class DamagePopup(
    val id: Long = Random.nextLong(),
    val text: String,
    val color: Color,
    val isOpponent: Boolean
)

/**
 * Full-screen Community Pet Battle Arena Modal Dialog
 */
@Composable
fun PetBattleArenaDialog(
    userPet: PetEntity,
    opponent: CommunityOpponent,
    onDismiss: () -> Unit,
    onVictory: (coinsWon: Int, expGained: Int, battleSummary: String) -> Unit,
    onShareBattleReport: (battleSummary: String) -> Unit
) {
    val soundEngine = remember { PetSoundEngine.getInstance() }
    val scope = rememberCoroutineScope()

    // Calculate initial battle profiles
    val userBaseStats = remember(userPet) { calculatePetBattleStats(userPet) }
    var userCurrentHp by remember { mutableIntStateOf(userBaseStats.maxHp) }
    var opponentCurrentHp by remember { mutableIntStateOf(opponent.stats.maxHp) }

    val opponentPetEntity = remember(opponent) { opponent.toPetEntity() }

    // Skills
    val userSkills = remember(userPet) { getPetBattleSkills(userPet) }
    val opponentSkills = remember(opponentPetEntity) { getPetBattleSkills(opponentPetEntity) }

    // Animation States for 3D Models
    var userActionState by remember { mutableStateOf(Pet3DActionState.IDLE) }
    var opponentActionState by remember { mutableStateOf(Pet3DActionState.IDLE) }

    // Battle Flow States
    var currentRound by remember { mutableIntStateOf(1) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var isProcessingTurn by remember { mutableStateOf(false) }
    var isAutoBattle by remember { mutableStateOf(false) }
    var battleEnded by remember { mutableStateOf(false) }
    var userWon by remember { mutableStateOf(false) }

    // Defense shields
    var userHasShield by remember { mutableStateOf(false) }
    var opponentHasShield by remember { mutableStateOf(false) }

    // Combat logs and floating damage popups
    val combatLogs = remember { mutableStateListOf<BattleActionLog>() }
    val damagePopups = remember { mutableStateListOf<DamagePopup>() }
    val logListState = rememberLazyListState()

    // Background arena animated lighting
    val infiniteTransition = rememberInfiniteTransition(label = "arena_pulse")
    val arenaPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arena_pulse"
    )

    // Execute an action
    fun executeSkillTurn(skill: BattleSkill, isUser: Boolean) {
        if (battleEnded || isProcessingTurn) return
        isProcessingTurn = true

        scope.launch {
            val attackerName = if (isUser) userPet.name else opponent.petName
            val defenderName = if (isUser) opponent.petName else userPet.name

            // 1. Attacker 3D Animation & Sound
            if (isUser) {
                userActionState = when (skill.skillType) {
                    BattleSkillType.HEAL -> Pet3DActionState.CUDDLE_PURR
                    BattleSkillType.BURST, BattleSkillType.ULTIMATE -> Pet3DActionState.ATTACK_LEAP
                    else -> Pet3DActionState.ATTACK_LEAP
                }
            } else {
                opponentActionState = when (skill.skillType) {
                    BattleSkillType.HEAL -> Pet3DActionState.CUDDLE_PURR
                    BattleSkillType.BURST, BattleSkillType.ULTIMATE -> Pet3DActionState.ATTACK_LEAP
                    else -> Pet3DActionState.ATTACK_LEAP
                }
            }

            // Sound effect trigger
            when (skill.skillType) {
                BattleSkillType.HEAL -> soundEngine.playBattleShield()
                BattleSkillType.BURST -> soundEngine.playBattleAttack()
                BattleSkillType.ULTIMATE -> soundEngine.playBattleCritical()
                else -> soundEngine.playBattleAttack()
            }

            delay(280)

            // 2. Resolve damage / heal numbers
            val atk = if (isUser) userBaseStats.attack else opponent.stats.attack
            val def = if (isUser) {
                opponent.stats.defense * (if (opponentHasShield) 1.4f else 1.0f)
            } else {
                userBaseStats.defense * (if (userHasShield) 1.4f else 1.0f)
            }

            if (skill.skillType == BattleSkillType.HEAL) {
                val healAmount = (if (isUser) userBaseStats.maxHp else opponent.stats.maxHp) * 0.28f + Random.nextInt(10, 20)
                val intHeal = healAmount.toInt()

                if (isUser) {
                    userCurrentHp = min(userBaseStats.maxHp, userCurrentHp + intHeal)
                    userHasShield = true
                    damagePopups.add(DamagePopup(text = "+$intHeal 💚 护盾生效", color = Color(0xFF00E676), isOpponent = false))
                } else {
                    opponentCurrentHp = min(opponent.stats.maxHp, opponentCurrentHp + intHeal)
                    opponentHasShield = true
                    damagePopups.add(DamagePopup(text = "+$intHeal 💚 护盾生效", color = Color(0xFF00E676), isOpponent = true))
                }

                combatLogs.add(
                    0,
                    BattleActionLog(
                        round = currentRound,
                        attackerName = attackerName,
                        skillName = skill.name,
                        heal = intHeal,
                        message = "$attackerName 施展【${skill.name}】，恢复了 $intHeal 点生命并凝结防御护盾！🫧",
                        userHp = userCurrentHp,
                        opponentHp = opponentCurrentHp
                    )
                )
            } else {
                // Attack logic
                val isCrit = (skill.skillType == BattleSkillType.BURST && Random.nextFloat() < 0.45f) ||
                        (skill.skillType == BattleSkillType.ULTIMATE && Random.nextFloat() < 0.60f)

                val powerMultiplier = skill.basePower / 100f
                val critMultiplier = if (isCrit) 1.75f else 1.0f
                val rawDmg = max(18f, (atk * powerMultiplier * 1.35f - def * 0.45f) * critMultiplier) + Random.nextInt(-4, 6)
                val finalDmg = max(12, rawDmg.toInt())

                // Target Reaction & Sound
                if (isCrit) {
                    soundEngine.playBattleCritical()
                } else {
                    soundEngine.playBattleHit()
                }

                if (isUser) {
                    opponentCurrentHp = max(0, opponentCurrentHp - finalDmg)
                    opponentActionState = Pet3DActionState.HIT_SHAKE
                    opponentHasShield = false
                    val popupText = if (isCrit) "🔥 暴击 -$finalDmg!" else "-$finalDmg"
                    damagePopups.add(DamagePopup(text = popupText, color = if (isCrit) Color(0xFFFF3D00) else Color(0xFFFF5252), isOpponent = true))
                } else {
                    userCurrentHp = max(0, userCurrentHp - finalDmg)
                    userActionState = Pet3DActionState.HIT_SHAKE
                    userHasShield = false
                    val popupText = if (isCrit) "🔥 暴击 -$finalDmg!" else "-$finalDmg"
                    damagePopups.add(DamagePopup(text = popupText, color = if (isCrit) Color(0xFFFF3D00) else Color(0xFFFF5252), isOpponent = false))
                }

                val critNote = if (isCrit) "引发萌力暴击！💥" else ""
                combatLogs.add(
                    0,
                    BattleActionLog(
                        round = currentRound,
                        attackerName = attackerName,
                        skillName = skill.name,
                        damage = finalDmg,
                        isCritical = isCrit,
                        message = "$attackerName 挥出【${skill.name}】，造成 $finalDmg 点伤害！$critNote",
                        userHp = userCurrentHp,
                        opponentHp = opponentCurrentHp
                    )
                )
            }

            delay(350)
            userActionState = Pet3DActionState.IDLE
            opponentActionState = Pet3DActionState.IDLE

            // 3. Check for Win/Loss
            if (opponentCurrentHp <= 0) {
                battleEnded = true
                userWon = true
                userActionState = Pet3DActionState.VICTORY_SPIN
                soundEngine.playLevelUp()
                soundEngine.playCoinReward(45)
                onVictory(45, 35, "在社区萌宠擂台中击败了 ${opponent.masterName} 的萌宠【${opponent.petName}】！荣登擂台排行榜！🏆")
                isProcessingTurn = false
                return@launch
            } else if (userCurrentHp <= 0) {
                battleEnded = true
                userWon = false
                opponentActionState = Pet3DActionState.VICTORY_SPIN
                isProcessingTurn = false
                return@launch
            }

            // 4. Advance Turn
            if (isUser) {
                isPlayerTurn = false
                delay(450)
                // Opponent AI turn
                val readySkills = opponentSkills.filter { it.currentCooldown == 0 }
                val chosenSkill = if (opponentCurrentHp < opponent.stats.maxHp * 0.45f && readySkills.any { it.skillType == BattleSkillType.HEAL }) {
                    readySkills.first { it.skillType == BattleSkillType.HEAL }
                } else {
                    readySkills.randomOrNull() ?: opponentSkills.first()
                }
                executeSkillTurn(chosenSkill, isUser = false)
            } else {
                currentRound++
                isPlayerTurn = true
                // Decrement cooldowns
                userSkills.forEach { if (it.currentCooldown > 0) it.currentCooldown-- }
                opponentSkills.forEach { if (it.currentCooldown > 0) it.currentCooldown-- }
                isProcessingTurn = false
            }
        }
    }

    // Auto-battle continuous loop
    LaunchedEffect(isAutoBattle, isPlayerTurn, battleEnded) {
        if (isAutoBattle && isPlayerTurn && !battleEnded && !isProcessingTurn) {
            delay(500)
            val usableSkills = userSkills.filter { it.currentCooldown == 0 }
            val bestSkill = if (userCurrentHp < userBaseStats.maxHp * 0.4f && usableSkills.any { it.skillType == BattleSkillType.HEAL }) {
                usableSkills.first { it.skillType == BattleSkillType.HEAL }
            } else {
                usableSkills.maxByOrNull { it.basePower } ?: userSkills.first()
            }
            executeSkillTurn(bestSkill, isUser = true)
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessingTurn) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CoralPrimary.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⚔️", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "社区萌宠竞技擂台",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "回合 $currentRound • ${if (isPlayerTurn) "己方出招回合" else "对手行动中..."}",
                            color = if (isPlayerTurn) Color(0xFF69F0AE) else Color(0xFFFFB74D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Auto Battle Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "自动",
                            fontSize = 12.sp,
                            color = if (isAutoBattle) CoralPrimary else Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isAutoBattle,
                            onCheckedChange = { isAutoBattle = it },
                            modifier = Modifier.size(width = 36.dp, height = 24.dp),
                            thumbContent = null,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CoralPrimary,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF334155)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "退出", tint = Color.White)
                    }
                }
            }

            // Main Arena Viewport
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                // 3D Arena Stage Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.3f)
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                    ) {
                        // Arena Field Floor Circle
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .align(Alignment.Center)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0x336366F1),
                                            Color(0x1138BDF8),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                                .border(1.5.dp, Color(0x44818CF8), CircleShape)
                        )

                        // Opponent Section (Top-Right)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp, end = 16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                // Opponent Info Card
                                Surface(
                                    color = Color(0xCC0F172A),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                opponent.petName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFF334155),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    "Lv.${opponent.level}",
                                                    fontSize = 10.sp,
                                                    color = StarGold,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "${opponent.masterName} • ${opponent.arenaTier}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Health Bar
                                        val oppHpRatio = (opponentCurrentHp.toFloat() / opponent.stats.maxHp).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { oppHpRatio },
                                            modifier = Modifier
                                                .width(130.dp)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = if (oppHpRatio > 0.5f) Color(0xFF00E676) else if (oppHpRatio > 0.25f) Color(0xFFFFB74D) else Color(0xFFFF5252),
                                            trackColor = Color(0xFF334155)
                                        )
                                        Text(
                                            "$opponentCurrentHp / ${opponent.stats.maxHp}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }

                                // Opponent 3D Pet Model
                                Pet3DModelView(
                                    pet = opponentPetEntity,
                                    modelSize = 130.dp,
                                    actionState = opponentActionState,
                                    enableOrbitDrag = false
                                )
                            }
                        }

                        // Player Section (Bottom-Left)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 16.dp, start = 16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                // User 3D Pet Model
                                Pet3DModelView(
                                    pet = userPet,
                                    modelSize = 150.dp,
                                    actionState = userActionState,
                                    enableOrbitDrag = false
                                )

                                // User Info Card
                                Surface(
                                    color = Color(0xCC0F172A),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.5f)),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                userPet.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = CoralPrimary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    "Lv.${userPet.level}",
                                                    fontSize = 10.sp,
                                                    color = CoralPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "战力: ${userBaseStats.combatPower} • 速度: ${userBaseStats.speed}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Health Bar
                                        val userHpRatio = (userCurrentHp.toFloat() / userBaseStats.maxHp).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { userHpRatio },
                                            modifier = Modifier
                                                .width(140.dp)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = if (userHpRatio > 0.5f) Color(0xFF00E676) else if (userHpRatio > 0.25f) Color(0xFFFFB74D) else Color(0xFFFF5252),
                                            trackColor = Color(0xFF334155)
                                        )
                                        Text(
                                            "$userCurrentHp / ${userBaseStats.maxHp}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }

                        // Floating Damage / Heal Popups
                        damagePopups.takeLast(3).forEach { popup ->
                            Box(
                                modifier = Modifier
                                    .align(if (popup.isOpponent) Alignment.TopCenter else Alignment.CenterStart)
                                    .padding(if (popup.isOpponent) 30.dp else 45.dp)
                            ) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, popup.color)
                                ) {
                                    Text(
                                        text = popup.text,
                                        color = popup.color,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Combat Log Ticker
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Box(modifier = Modifier.padding(10.dp)) {
                        if (combatLogs.isEmpty()) {
                            Text(
                                "⚔️ 战斗开始！请点击下方技能出招，或开启右上角自动战斗！",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(state = logListState) {
                                items(combatLogs) { log ->
                                    Text(
                                        text = "• ${log.message}",
                                        color = if (log.isCritical) Color(0xFFFFD54F) else Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = if (log.isCritical) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Skills Selection Pad
                Text(
                    text = "出招技能",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userSkills.forEach { skill ->
                        val isAvailable = isPlayerTurn && !isProcessingTurn && skill.currentCooldown == 0 && !battleEnded
                        val isUltimate = skill.skillType == BattleSkillType.ULTIMATE

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = isAvailable) {
                                    skill.currentCooldown = skill.cooldownMax
                                    executeSkillTurn(skill, isUser = true)
                                }
                                .border(
                                    width = if (isUltimate) 1.5.dp else 1.dp,
                                    color = if (isAvailable) (if (isUltimate) StarGold else CoralPrimary) else Color(0xFF334155),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            color = if (isAvailable) (if (isUltimate) Color(0xFF312E81) else Color(0xFF1E293B)) else Color(0xFF111827)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(skill.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    skill.name,
                                    color = if (isAvailable) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    if (skill.currentCooldown > 0) "冷却 ${skill.currentCooldown} 回合" else "威力 ${skill.basePower}",
                                    color = if (skill.currentCooldown > 0) Color(0xFFFF7043) else Color(0xFF818CF8),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            // Victory / Defeat Overlay Modal
            if (battleEnded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(2.dp, if (userWon) StarGold else Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (userWon) "🏆 萌宠擂台大胜利！" else "🐾 惜败！萌力满满虽败犹荣",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userWon) StarGold else Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (userWon)
                                    "恭喜！${userPet.name} 凭借出色的萌力和敏捷战胜了对手！"
                                else
                                    "对手经验丰富，带 ${userPet.name} 多吃点好吃的、洗个香香澡再来挑战吧！",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Rewards Display
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🪙 装扮币", fontSize = 11.sp, color = Color.Gray)
                                        Text(if (userWon) "+45" else "+15", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarGold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("✨ 成长EXP", fontSize = 11.sp, color = Color.Gray)
                                        Text(if (userWon) "+35" else "+15", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🏆 天梯积分", fontSize = 11.sp, color = Color.Gray)
                                        Text(if (userWon) "+25" else "+5", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BubbleBlue)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Buttons
                            if (userWon) {
                                Button(
                                    onClick = {
                                        val summary = "🎉 在社区萌宠擂台赛中，我家 ${userPet.name} (Lv.${userPet.level}) 经过 $currentRound 回合鏖战，击败了 ${opponent.masterName} 的萌宠【${opponent.petName}】！萌力爆表！🐾✨"
                                        onShareBattleReport(summary)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("一键分享战报至社区广场", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("返回社区广场", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
