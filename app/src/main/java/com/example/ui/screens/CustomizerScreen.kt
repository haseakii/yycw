package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.OUTFIT_CATEGORIES
import com.example.data.model.OUTFIT_ITEMS_LIST
import com.example.data.model.OutfitItem
import com.example.data.model.PetEntity
import com.example.ui.components.PetModelView
import com.example.ui.theme.CoralPrimary

data class PresetSpecies(
    val id: String,
    val name: String,
    val emoji: String,
    val baseBody: String,
    val defaultEar: String,
    val defaultTail: String,
    val primaryColor: Long,
    val defaultCatchphrase: String
)

val PRESET_SPECIES_LIST = listOf(
    PresetSpecies("CAT", "软萌英短猫", "🐱", "CAT", "POINTY", "FLUFFY", 0xFFFFAB91, "喵呜~ 蹭蹭主人手心！"),
    PresetSpecies("DOG", "活力柴犬", "🐶", "DOG", "POINTY", "CURLY", 0xFFFFA726, "汪汪！今天也想去草地狂奔！"),
    PresetSpecies("RABBIT", "垂耳白兔", "🐰", "RABBIT", "DROOPY", "PUFF_BALL", 0xFFFFFFFF, "嚼嚼~ 胡萝卜最美味啦！"),
    PresetSpecies("FOX", "灵动赤狐", "🦊", "FOX", "POINTY", "FLUFFY", 0xFFFF7043, "嗷呜~ 我有最蓬松的大尾巴！"),
    PresetSpecies("PANDA", "呆萌小熊猫", "🐼", "PANDA", "ROUND", "FLUFFY", 0xFFFF8A65, "抱抱爪爪，今天也悠哉游哉~"),
    PresetSpecies("CAPYBARA", "治愈水豚", "🐹", "CAPYBARA", "ROUND", "PUFF_BALL", 0xFF8D6E63, "咕噜噜... 情绪极其稳定~"),
    PresetSpecies("DRAGON", "星灵幻龙", "🐉", "DRAGON", "HORNS", "STAR_TAIL", 0xFF9FA8DA, "呼~ 吐出微光小星辰！")
)

val COLOR_PALETTE = listOf(
    0xFFFFAB91 to "暖桃杏",
    0xFFFFA726 to "元气橘",
    0xFFFFFFFF to "纯净白",
    0xFFB0BEC5 to "银雾灰",
    0xFF424242 to "曜石黑",
    0xFFFF80AB to "柔粉樱",
    0xFF80CBC4 to "薄荷绿",
    0xFFB39DDB to "薰衣草紫",
    0xFF8D6E63 to "可可棕",
    0xFFFFD54F to "向日黄",
    0xFF81D4FA to "晴空蓝",
    0xFFA5D6A7 to "抹茶青"
)

val SECONDARY_PALETTE = listOf(
    0xFFFFFFFF to "乳白腹",
    0xFFFFE0B2 to "奶油杏",
    0xFFFFCDD2 to "淡粉腹",
    0xFFFFF9C4 to "暖蛋黄"
)

val EAR_STYLES = listOf(
    "POINTY" to "尖俏折耳",
    "ROUND" to "软萌圆耳",
    "DROOPY" to "乖巧垂耳",
    "LONG" to "修长大耳",
    "HORNS" to "奇幻萌角"
)

val EYE_STYLES = listOf(
    "SPARKLE" to "闪耀星瞳",
    "HAPPY_CURVE" to "眯眼笑弧",
    "BIG_ROUND" to "呆萌圆眼",
    "WINK" to "俏皮单眨"
)

val TAIL_STYLES = listOf(
    "FLUFFY" to "蓬松大尾",
    "SLENDER" to "灵动细尾",
    "PUFF_BALL" to "棉花球尾",
    "CURLY" to "卷翘柴尾",
    "STAR_TAIL" to "星云龙尾"
)

val ACCESSORIES = listOf(
    "NONE" to "无佩饰",
    "BELL_COLLAR" to "小金铃",
    "BOW_TIE" to "红领结",
    "BERET_HAT" to "贝雷帽",
    "SUNGLASSES" to "黑墨镜",
    "HALO" to "光环",
    "CAPE" to "小披风"
)

val PERSONALITIES = listOf(
    "活泼粘人",
    "贪吃小馋猫",
    "傲娇高冷",
    "温柔治愈",
    "好奇探险家"
)

@Composable
fun CustomizerScreen(
    currentPet: PetEntity?,
    onSavePet: (
        existingId: Long,
        name: String,
        species: String,
        isCustomSpecies: Boolean,
        baseBodyType: String,
        primaryColor: Long,
        secondaryColor: Long,
        earStyle: String,
        eyeStyle: String,
        tailStyle: String,
        accessory: String,
        personality: String,
        catchphrase: String
    ) -> Unit,
    onPurchaseOutfit: (OutfitItem) -> Unit = {},
    onNavigateToShop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isNewPetMode by remember { mutableStateOf(false) }

    var petName by remember { mutableStateOf(currentPet?.name ?: "泡芙") }
    var isCustomSpecies by remember { mutableStateOf(currentPet?.isCustomSpecies ?: false) }
    var customSpeciesName by remember { mutableStateOf(if (currentPet?.isCustomSpecies == true) currentPet.species else "星际机甲萌宠") }
    var selectedPreset by remember { mutableStateOf(PRESET_SPECIES_LIST[0]) }

    var baseBodyType by remember { mutableStateOf(currentPet?.baseBodyType ?: "CAT") }
    var primaryColor by remember { mutableLongStateOf(currentPet?.primaryColor ?: 0xFFFFAB91) }
    var secondaryColor by remember { mutableLongStateOf(currentPet?.secondaryColor ?: 0xFFFFFFFF) }
    var earStyle by remember { mutableStateOf(currentPet?.earStyle ?: "POINTY") }
    var eyeStyle by remember { mutableStateOf(currentPet?.eyeStyle ?: "SPARKLE") }
    var tailStyle by remember { mutableStateOf(currentPet?.tailStyle ?: "FLUFFY") }
    var accessory by remember { mutableStateOf(currentPet?.accessory ?: "BELL_COLLAR") }
    var personality by remember { mutableStateOf(currentPet?.personality ?: "活泼粘人") }
    var catchphrase by remember { mutableStateOf(currentPet?.catchphrase ?: "喵呜~ 蹭蹭主人手心！") }

    var customizerTab by remember { mutableIntStateOf(0) } // 0: 造型特征, 1: 华丽衣橱装扮
    var selectedOutfitCategory by remember { mutableStateOf("ALL") }

    val unlockedList = remember(currentPet?.unlockedOutfits) {
        currentPet?.unlockedOutfits?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    val userCoins = currentPet?.coins ?: 0

    // Sync state when currentPet changes
    LaunchedEffect(currentPet) {
        if (!isNewPetMode && currentPet != null) {
            petName = currentPet.name
            isCustomSpecies = currentPet.isCustomSpecies
            if (currentPet.isCustomSpecies) customSpeciesName = currentPet.species
            baseBodyType = currentPet.baseBodyType
            primaryColor = currentPet.primaryColor
            secondaryColor = currentPet.secondaryColor
            earStyle = currentPet.earStyle
            eyeStyle = currentPet.eyeStyle
            tailStyle = currentPet.tailStyle
            accessory = currentPet.accessory
            personality = currentPet.personality
            catchphrase = currentPet.catchphrase
        }
    }

    val previewPet = remember(
        petName, isCustomSpecies, customSpeciesName, baseBodyType,
        primaryColor, secondaryColor, earStyle, eyeStyle, tailStyle, accessory
    ) {
        PetEntity(
            id = if (isNewPetMode) 0L else (currentPet?.id ?: 1L),
            name = petName,
            species = if (isCustomSpecies) customSpeciesName else selectedPreset.name,
            isCustomSpecies = isCustomSpecies,
            baseBodyType = baseBodyType,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            earStyle = earStyle,
            eyeStyle = eyeStyle,
            tailStyle = tailStyle,
            accessory = accessory,
            personality = personality,
            catchphrase = catchphrase
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎨 萌宠定制工坊",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isNewPetMode) "自由创造全新物种与外观！" else "调整「$petName」的模型与特质",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$userCoins 币",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        isNewPetMode = !isNewPetMode
                        if (isNewPetMode) {
                            petName = "新萌宝"
                            isCustomSpecies = true
                        } else if (currentPet != null) {
                            petName = currentPet.name
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6750A4)),
                    modifier = Modifier.testTag("toggle_new_pet_button")
                ) {
                    Text(
                        text = if (isNewPetMode) "已有宠物" else "+ 领养新宠",
                        color = Color(0xFF6750A4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Live Model Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "实时外观模型渲染",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D1B20)
                    )
                    Surface(
                        color = Color(0xFFEADDFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isCustomSpecies) "✨ 自定义物种" else "🐾 原生物种",
                            fontSize = 11.sp,
                            color = Color(0xFF21005D),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PetModelView(
                    pet = previewPet,
                    petSize = 200.dp,
                    modifier = Modifier.testTag("customizer_preview_model")
                )

                Text(
                    text = "「${previewPet.name}」 · ${previewPet.species}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
            }
        }

        // Customizer / Dress-up Mode Tabs
        TabRow(
            selectedTabIndex = customizerTab,
            containerColor = Color(0xFFF3EDF7),
            contentColor = Color(0xFF6750A4),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[customizerTab]),
                    color = Color(0xFF6750A4),
                    height = 3.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            Tab(
                selected = customizerTab == 0,
                onClick = { customizerTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("特征模型定制", fontWeight = if (customizerTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = customizerTab == 1,
                onClick = { customizerTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Checkroom, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("萌宠时尚衣橱", fontWeight = if (customizerTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }

        if (customizerTab == 1) {
            // DRESS-UP & OUTFIT WARDROBE TAB
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("👗 潮流装扮衣橱", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                            Text("为萌宠换上个性服装、头饰与奇幻羽翼", fontSize = 12.sp, color = Color(0xFF49454F))
                        }
                        Surface(
                            color = Color(0xFFEADDFF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "共 ${OUTFIT_ITEMS_LIST.size} 件道具",
                                fontSize = 11.sp,
                                color = Color(0xFF21005D),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Coin Balance & Shop Shortcut Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF8E1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToShop() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "装扮币: $userCoins",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Text(
                                text = "🛍️ 进入虚拟商店试衣间 ↗",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4)
                            )
                        }
                    }

                    // Category Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OUTFIT_CATEGORIES) { (catKey, catName) ->
                            val isSelected = selectedOutfitCategory == catKey
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                                modifier = Modifier.clickable { selectedOutfitCategory = catKey }
                            ) {
                                Text(
                                    text = catName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF49454F),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Filtered Outfit Items Grid / Horizontal List
                    val filteredOutfits = remember(selectedOutfitCategory) {
                        if (selectedOutfitCategory == "ALL") OUTFIT_ITEMS_LIST
                        else OUTFIT_ITEMS_LIST.filter { it.category == selectedOutfitCategory }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredOutfits.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { outfit ->
                                    val isUnlocked = outfit.costCoins == 0 || unlockedList.contains(outfit.id) || unlockedList.contains(outfit.styleKey)
                                    val isEquipped = accessory == outfit.styleKey
                                    val canAfford = userCoins >= outfit.costCoins

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                // Preview instantly on pet
                                                accessory = outfit.styleKey
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isEquipped) Color(0xFFF3EDF7) else Color.White
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isEquipped) 2.dp else 1.dp,
                                            color = if (isEquipped) Color(0xFF6750A4) else Color(0xFFE8DEF8)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = outfit.emoji, fontSize = 28.sp)
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when (outfit.rarity) {
                                                        "史诗" -> Color(0xFFFFF3E0)
                                                        "稀有" -> Color(0xFFEDE7F6)
                                                        else -> Color(0xFFE8F5E9)
                                                    }
                                                ) {
                                                    Text(
                                                        text = outfit.rarity,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (outfit.rarity) {
                                                            "史诗" -> Color(0xFFE65100)
                                                            "稀有" -> Color(0xFF512DA8)
                                                            else -> Color(0xFF2E7D32)
                                                        },
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = outfit.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isEquipped) Color(0xFF6750A4) else Color(0xFF1D1B20)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = outfit.description,
                                                fontSize = 10.sp,
                                                color = Color(0xFF79747E),
                                                maxLines = 1,
                                                lineHeight = 13.sp
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Action Buttons: Equipped, Wear, or Purchase
                                            when {
                                                isEquipped -> {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color(0xFF6750A4),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = "已穿戴 ✓",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                                isUnlocked -> {
                                                    Button(
                                                        onClick = { accessory = outfit.styleKey },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFFEADDFF),
                                                            contentColor = Color(0xFF21005D)
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(28.dp)
                                                    ) {
                                                        Text("穿戴", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                                else -> {
                                                    Button(
                                                        onClick = {
                                                            if (canAfford) {
                                                                onPurchaseOutfit(outfit)
                                                                accessory = outfit.styleKey
                                                            }
                                                        },
                                                        enabled = canAfford,
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = CoralPrimary,
                                                            contentColor = Color.White,
                                                            disabledContainerColor = Color(0xFFE0E0E0),
                                                            disabledContentColor = Color(0xFF9E9E9E)
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(28.dp)
                                                    ) {
                                                        if (canAfford) {
                                                            Text("🪙${outfit.costCoins} 解锁", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        } else {
                                                            Text("🪙${outfit.costCoins} (缺${outfit.costCoins - userCoins})", fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // FEATURE & SPECIES CUSTOMIZATION TAB
            // Section 1: Species & Identity Customization
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "1. 身份与种类设定",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )

                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("宠物昵称") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pet_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("自定义全新物种", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1D1B20))
                        Text("开启后可自由定义物种名称和特异形态", fontSize = 12.sp, color = Color(0xFF49454F))
                    }
                    Switch(
                        checked = isCustomSpecies,
                        onCheckedChange = { isCustomSpecies = it },
                        modifier = Modifier.testTag("custom_species_switch")
                    )
                }

                if (isCustomSpecies) {
                    OutlinedTextField(
                        value = customSpeciesName,
                        onValueChange = { customSpeciesName = it },
                        label = { Text("物种名称 (如: 极光机甲狐 / 星云灵兽)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_species_name_input")
                    )
                } else {
                    Text("选择原生动物基底:", fontSize = 13.sp, color = Color(0xFF49454F))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PRESET_SPECIES_LIST) { preset ->
                            val isSelected = selectedPreset.id == preset.id && !isCustomSpecies
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                                modifier = Modifier.clickable {
                                    selectedPreset = preset
                                    baseBodyType = preset.baseBody
                                    earStyle = preset.defaultEar
                                    tailStyle = preset.defaultTail
                                    primaryColor = preset.primaryColor
                                    catchphrase = preset.defaultCatchphrase
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = preset.emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = preset.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF1D1B20)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Visual Model Parameter Customization
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "2. 外观与模型深度定制",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )

                // Fur primary color
                Text("毛色主色调:", fontSize = 13.sp, color = Color(0xFF49454F))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(COLOR_PALETTE) { (colorVal, colorName) ->
                        val isSelected = primaryColor == colorVal
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { primaryColor = colorVal }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (colorVal == 0xFFFFFFFF || colorVal == 0xFFFF80AB) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = colorName, fontSize = 10.sp, color = Color(0xFF79747E))
                        }
                    }
                }

                // Belly / Accent color
                Text("肚皮/衬色副色:", fontSize = 13.sp, color = Color(0xFF49454F))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(SECONDARY_PALETTE) { (colorVal, colorName) ->
                        val isSelected = secondaryColor == colorVal
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFEADDFF) else Color(0xFFF3EDF7),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6750A4)) else null,
                            modifier = Modifier.clickable { secondaryColor = colorVal }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorVal))
                                        .border(1.dp, Color(0xFFCAC4D0), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = colorName,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color(0xFF21005D) else Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }

                // Ear style
                Text("耳朵外形模型:", fontSize = 13.sp, color = Color(0xFF49454F))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EAR_STYLES) { (styleKey, styleName) ->
                        val isSelected = earStyle == styleKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                            modifier = Modifier.clickable { earStyle = styleKey }
                        ) {
                            Text(
                                text = styleName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Eye style
                Text("眼部表情模型:", fontSize = 13.sp, color = Color(0xFF49454F))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EYE_STYLES) { (styleKey, styleName) ->
                        val isSelected = eyeStyle == styleKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                            modifier = Modifier.clickable { eyeStyle = styleKey }
                        ) {
                            Text(
                                text = styleName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Tail style
                Text("尾巴形态模型:", fontSize = 13.sp, color = Color(0xFF49454F))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TAIL_STYLES) { (styleKey, styleName) ->
                        val isSelected = tailStyle == styleKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                            modifier = Modifier.clickable { tailStyle = styleKey }
                        ) {
                            Text(
                                text = styleName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Accessories
                Text("专属潮流服饰/佩饰:", fontSize = 13.sp, color = Color(0xFF49454F))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ACCESSORIES) { (styleKey, styleName) ->
                        val isSelected = accessory == styleKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                            modifier = Modifier.clickable { accessory = styleKey }
                        ) {
                            Text(
                                text = styleName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Personality & Voice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8DEF8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "3. 性格与萌语设定",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1B20)
                )

                Text("性格特质:", fontSize = 13.sp, color = Color(0xFF49454F))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PERSONALITIES) { trait ->
                        val isSelected = personality == trait
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                            modifier = Modifier.clickable { personality = trait }
                        ) {
                            Text(
                                text = trait,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF49454F),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = catchphrase,
                    onValueChange = { catchphrase = it },
                    label = { Text("专属萌宠口头禅 / 撒娇语") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        }

        // Action Buttons
        Button(
            onClick = {
                val finalSpecies = if (isCustomSpecies) {
                    customSpeciesName.ifBlank { "自定义星灵兽" }
                } else {
                    selectedPreset.name
                }
                val finalName = petName.ifBlank { "小萌宝" }
                val targetId = if (isNewPetMode) 0L else (currentPet?.id ?: 0L)

                onSavePet(
                    targetId,
                    finalName,
                    finalSpecies,
                    isCustomSpecies,
                    baseBodyType,
                    primaryColor,
                    secondaryColor,
                    earStyle,
                    eyeStyle,
                    tailStyle,
                    accessory,
                    personality,
                    catchphrase
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_custom_pet_button")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isNewPetMode) "✨ 孵化并领养此新物种" else "💾 保存宠物定制造型",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
