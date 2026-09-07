package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OUTFIT_CATEGORIES
import com.example.data.model.OUTFIT_ITEMS_LIST
import com.example.data.model.OutfitItem
import com.example.data.model.PetEntity
import com.example.ui.components.Pet3DActionState
import com.example.ui.components.Pet3DModelView
import com.example.ui.components.PetAnimationMode
import com.example.ui.components.PetModelView

/**
 * Dedicated Virtual Shop Screen:
 * - Displays user's coin balance with real-time updates and coin rewards.
 * - Interactive Fitting Room: Live real-time preview (both 3D 360° orbit and 2D mode) on the user's pet.
 * - Categorized accessory catalog with prices, rarity badges, and purchase/equip state.
 * - Purchase confirmation modal with instant equip option.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    pet: PetEntity,
    onPurchaseOutfit: (OutfitItem, Boolean) -> Unit,
    onEquipOutfit: (OutfitItem) -> Unit,
    onClaimShopBonus: () -> Unit,
    onNavigateToTasks: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Current unlocked outfits parsed from database string
    val unlockedList = remember(pet.unlockedOutfits) {
        pet.unlockedOutfits.split(",").filter { it.isNotBlank() }
    }

    // Currently equipped outfit item
    val equippedOutfit = remember(pet.accessory) {
        OUTFIT_ITEMS_LIST.find { it.id == pet.accessory } ?: OUTFIT_ITEMS_LIST.first()
    }

    // Fitting Room Preview Item (default to currently equipped)
    var previewOutfit by remember(pet.accessory) {
        mutableStateOf(equippedOutfit)
    }

    // 3D vs 2D Fitting Room mode switcher
    var is3DPreviewMode by remember { mutableStateOf(true) }

    // Category filter: ALL, HAT, ACCESSORY, CLOTHING, WINGS
    var selectedCategory by remember { mutableStateOf("ALL") }

    // Ownership filter: 0: 全部, 1: 未拥有, 2: 已拥有
    var ownershipFilter by remember { mutableStateOf(0) }

    // Dialog state for confirming purchase
    var itemToPurchase by remember { mutableStateOf<OutfitItem?>(null) }
    var autoEquipOnBuy by remember { mutableStateOf(true) }

    // Earn coins modal bottom sheet
    var showEarnCoinsSheet by remember { mutableStateOf(false) }

    // Virtual preview pet derived from real pet with the previewed accessory
    val previewPet = remember(pet, previewOutfit) {
        pet.copy(accessory = previewOutfit.id)
    }

    // Filtered outfits based on category & ownership
    val filteredOutfits = remember(selectedCategory, ownershipFilter, unlockedList) {
        OUTFIT_ITEMS_LIST.filter { item ->
            val matchesCategory = (selectedCategory == "ALL" || item.category == selectedCategory)
            val isUnlocked = unlockedList.contains(item.id)
            val matchesOwnership = when (ownershipFilter) {
                1 -> !isUnlocked // 未拥有
                2 -> isUnlocked // 已拥有
                else -> true
            }
            matchesCategory && matchesOwnership
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FE))
            .testTag("shop_screen")
    ) {
        // -------------------------------------------------------------
        // 1. Top Header: Market Title & Coin Economy Pill
        // -------------------------------------------------------------
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🛍️ 萌宠装扮集市",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                        }
                        Text(
                            text = "3D全景试衣间 · 实时试穿预览",
                            fontSize = 12.sp,
                            color = Color(0xFF79747E)
                        )
                    }

                    // Coin Balance Pill & Earn Bonus Action
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                            modifier = Modifier
                                .testTag("shop_coin_balance_badge")
                                .clickable { showEarnCoinsSheet = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = "装扮币",
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${pet.coins}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "币",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFA000)
                                )
                            }
                        }

                        // Free Daily Supply Pill
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF6750A4),
                            modifier = Modifier
                                .testTag("shop_bonus_claim_button")
                                .clickable { onClaimShopBonus() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🎁 +60币",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // Main Content: Fitting Room Stage + Catalog Grid
        // -------------------------------------------------------------
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // SPAN 2: Interactive Real-Time Fitting Room Stage
            item(span = { GridItemSpan(2) }) {
                FittingRoomPreviewDeck(
                    pet = previewPet,
                    realPet = pet,
                    previewOutfit = previewOutfit,
                    equippedOutfit = equippedOutfit,
                    is3DMode = is3DPreviewMode,
                    onToggle3D = { is3DPreviewMode = !is3DPreviewMode },
                    onRevertPreview = { previewOutfit = equippedOutfit },
                    onConfirmBuy = {
                        itemToPurchase = previewOutfit
                    },
                    onEquipItem = {
                        onEquipOutfit(previewOutfit)
                    },
                    onOpenEarnCoins = { showEarnCoinsSheet = true }
                )
            }

            // SPAN 2: Category Filter Bar & Ownership Tabs
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Category Tab Row
                    ScrollableTabRow(
                        selectedTabIndex = OUTFIT_CATEGORIES.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OUTFIT_CATEGORIES.forEach { (catId, catName) ->
                            val isSelected = selectedCategory == catId
                            Tab(
                                selected = isSelected,
                                onClick = { selectedCategory = catId },
                                text = {
                                    Text(
                                        text = catName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.testTag("shop_category_tab_$catId")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Secondary filter chips: 全部 / 未拥有 / 已拥有
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0 to "全部装扮", 1 to "仅看未拥有", 2 to "仅看已拥有").forEach { (filterVal, label) ->
                            FilterChip(
                                selected = ownershipFilter == filterVal,
                                onClick = { ownershipFilter = filterVal },
                                label = { Text(text = label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEADDFF),
                                    selectedLabelColor = Color(0xFF21005D)
                                ),
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }
            }

            // Empty state if filter yields nothing
            if (filteredOutfits.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔍", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "该分类下暂无符合条件的装扮",
                                fontSize = 14.sp,
                                color = Color(0xFF49454F),
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(onClick = {
                                selectedCategory = "ALL"
                                ownershipFilter = 0
                            }) {
                                Text("重置所有筛选")
                            }
                        }
                    }
                }
            }

            // Products Grid Items
            items(filteredOutfits, key = { it.id }) { item ->
                val isUnlocked = unlockedList.contains(item.id)
                val isEquipped = pet.accessory == item.id
                val isCurrentPreview = previewOutfit.id == item.id
                val canAfford = pet.coins >= item.costCoins

                ShopOutfitItemCard(
                    item = item,
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    isCurrentPreview = isCurrentPreview,
                    canAfford = canAfford,
                    onPreviewClick = {
                        previewOutfit = item
                    },
                    onBuyClick = {
                        previewOutfit = item
                        itemToPurchase = item
                    },
                    onEquipClick = {
                        previewOutfit = item
                        onEquipOutfit(item)
                    }
                )
            }
        }
    }

    // -------------------------------------------------------------
    // Purchase Confirmation Dialog
    // -------------------------------------------------------------
    itemToPurchase?.let { outfit ->
        AlertDialog(
            onDismissRequest = { itemToPurchase = null },
            icon = {
                Text(text = outfit.emoji, fontSize = 40.sp)
            },
            title = {
                Text(
                    text = "购买装扮「${outfit.name}」",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = outfit.description,
                        fontSize = 13.sp,
                        color = Color(0xFF49454F)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3EDF7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "商品售价", fontSize = 12.sp, color = Color(0xFF49454F))
                                Text(
                                    text = if (outfit.costCoins == 0) "免费" else "🪙 ${outfit.costCoins} 装扮币",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6750A4)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "当前装扮币", fontSize = 12.sp, color = Color(0xFF49454F))
                                Text(text = "🪙 ${pet.coins}", fontSize = 12.sp, color = Color(0xFF49454F))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "购买后结余", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                val remaining = pet.coins - outfit.costCoins
                                Text(
                                    text = "🪙 $remaining 币",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining >= 0) Color(0xFF2E7D32) else Color(0xFFB71C1C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { autoEquipOnBuy = !autoEquipOnBuy }
                    ) {
                        Checkbox(
                            checked = autoEquipOnBuy,
                            onCheckedChange = { autoEquipOnBuy = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6750A4))
                        )
                        Text(
                            text = "购买后立即为「${pet.name}」穿戴此装扮",
                            fontSize = 12.sp,
                            color = Color(0xFF1D1B20)
                        )
                    }
                }
            },
            confirmButton = {
                val canAfford = pet.coins >= outfit.costCoins
                Button(
                    onClick = {
                        val toBuy = outfit
                        itemToPurchase = null
                        onPurchaseOutfit(toBuy, autoEquipOnBuy)
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    modifier = Modifier.testTag("confirm_purchase_dialog_button")
                ) {
                    Text(text = if (canAfford) "确认支付并拥有" else "装扮币不足")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToPurchase = null }) {
                    Text(text = "再想想")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // How to Earn Coins Bottom Sheet
    // -------------------------------------------------------------
    if (showEarnCoinsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEarnCoinsSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 如何赚取装扮币？",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                    IconButton(onClick = { showEarnCoinsSheet = false }) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "完成")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val earningWays = listOf(
                    Triple("🍖 每日日常关爱", "投喂、清洁、玩耍、哄睡宠物，每项日常任务奖励 25~45 装扮币", "去做任务"),
                    Triple("🎁 集市每日补给红包", "每天可在虚拟集市直接领取 60 装扮币福利礼包", "一键领取"),
                    Triple("⚔️ 社区萌宠擂台对决", "与其他萌宠伙伴对战切磋，获得胜利可赢取 50 装扮币与荣耀积分", "去对战"),
                    Triple("🌟 宠物成长与升级", "提升宠物亲密度与等级，每次升级均赠送 100 装扮币", "查看成长")
                )

                earningWays.forEach { (title, desc, actionLabel) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                                Text(text = desc, fontSize = 11.sp, color = Color(0xFF49454F))
                            }
                            Button(
                                onClick = {
                                    showEarnCoinsSheet = false
                                    when (actionLabel) {
                                        "一键领取" -> onClaimShopBonus()
                                        else -> onNavigateToTasks()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(text = actionLabel, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Real-Time Fitting Room Deck:
 * Displays the 3D or 2D live pet model wearing the trial accessory,
 * along with comparison, reset, and direct purchase / equip actions.
 */
@Composable
private fun FittingRoomPreviewDeck(
    pet: PetEntity,
    realPet: PetEntity,
    previewOutfit: OutfitItem,
    equippedOutfit: OutfitItem,
    is3DMode: Boolean,
    onToggle3D: () -> Unit,
    onRevertPreview: () -> Unit,
    onConfirmBuy: () -> Unit,
    onEquipItem: () -> Unit,
    onOpenEarnCoins: () -> Unit
) {
    val isPreviewingDifferent = previewOutfit.id != equippedOutfit.id
    val isUnlocked = realPet.unlockedOutfits.split(",").contains(previewOutfit.id)
    val isEquipped = realPet.accessory == previewOutfit.id
    val canAfford = realPet.coins >= previewOutfit.costCoins

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_fitting_room_stage")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stage Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF6750A4).copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "✨ 实时试衣间",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (isPreviewingDifferent) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "试穿效果预览中",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // 3D / 2D Switcher Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF3EDF7),
                    modifier = Modifier
                        .testTag("shop_preview_3d_toggle")
                        .clickable { onToggle3D() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ViewInAr,
                            contentDescription = "切换3D",
                            tint = if (is3DMode) Color(0xFF6750A4) else Color(0xFF79747E),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (is3DMode) "3D全景(拖动旋转)" else "2D经典",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (is3DMode) Color(0xFF6750A4) else Color(0xFF49454F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stage Backdrop with Pet Model
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFEDE7F6),
                                Color(0xFFF3E5F5)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (is3DMode) {
                    Pet3DModelView(
                        pet = pet,
                        modelSize = 190.dp,
                        actionState = Pet3DActionState.IDLE,
                        enableOrbitDrag = true,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PetModelView(
                        pet = pet,
                        petSize = 180.dp,
                        animationMode = PetAnimationMode.IDLE,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Revert to original outfit button (shown when previewing trial item)
                if (isPreviewingDifferent) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clickable { onRevertPreview() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "还原原貌",
                                tint = Color(0xFF49454F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "还原原装扮", fontSize = 10.sp, color = Color(0xFF49454F))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preview Item Details Card & Action Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF7F2FA),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = previewOutfit.emoji,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = previewOutfit.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RarityBadge(rarity = previewOutfit.rarity)
                        }
                        Text(
                            text = previewOutfit.description,
                            fontSize = 11.sp,
                            color = Color(0xFF49454F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Primary Action Button based on outfit state
                    when {
                        isEquipped -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "已穿戴",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "已穿戴",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                        isUnlocked -> {
                            Button(
                                onClick = onEquipItem,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("fitting_room_equip_button")
                            ) {
                                Text(text = "立即穿戴", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        canAfford -> {
                            Button(
                                onClick = onConfirmBuy,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("fitting_room_buy_button")
                            ) {
                                Text(
                                    text = if (previewOutfit.costCoins == 0) "免费拥有" else "🪙 ${previewOutfit.costCoins} 购买",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> {
                            OutlinedButton(
                                onClick = onOpenEarnCoins,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "差 ${previewOutfit.costCoins - realPet.coins} 币 · 去赚币",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB71C1C),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Card in Shop Catalog Grid:
 * Displays price, rarity badge, trial status, and instant preview tap.
 */
@Composable
private fun ShopOutfitItemCard(
    item: OutfitItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    isCurrentPreview: Boolean,
    canAfford: Boolean,
    onPreviewClick: () -> Unit,
    onBuyClick: () -> Unit,
    onEquipClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isCurrentPreview) Color(0xFF6750A4) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "border_color"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPreview) Color(0xFFF3EDF7) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPreview) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentPreview) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onPreviewClick() }
            .testTag("shop_item_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Card Top Row: Rarity Badge & Price Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RarityBadge(rarity = item.rarity)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isUnlocked -> Color(0xFFE8F5E9)
                        canAfford -> Color(0xFFFFF8E1)
                        else -> Color(0xFFFFEBEE)
                    }
                ) {
                    Text(
                        text = when {
                            isEquipped -> "佩戴中"
                            isUnlocked -> "已拥有"
                            item.costCoins == 0 -> "免费"
                            else -> "🪙 ${item.costCoins}"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isEquipped -> Color(0xFF2E7D32)
                            isUnlocked -> Color(0xFF388E3C)
                            canAfford -> Color(0xFFE65100)
                            else -> Color(0xFFC62828)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Centered Emoji Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(
                        when (item.rarity) {
                            "史诗" -> Brush.radialGradient(listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80)))
                            "稀有" -> Brush.radialGradient(listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8)))
                            "精选" -> Brush.radialGradient(listOf(Color(0xFFB2EBF2), Color(0xFF80DEEA)))
                            else -> Brush.radialGradient(listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Outfit Name & Category
            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.description,
                fontSize = 10.sp,
                color = Color(0xFF79747E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button on Card
            when {
                isEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ 当前穿戴中",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
                isUnlocked -> {
                    OutlinedButton(
                        onClick = onEquipClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text(text = "穿戴此装", fontSize = 11.sp)
                    }
                }
                canAfford -> {
                    Button(
                        onClick = onBuyClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text(text = "购买 🪙${item.costCoins}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onPreviewClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text(text = "试穿预览", fontSize = 11.sp, color = Color(0xFF6750A4))
                    }
                }
            }
        }
    }
}

/**
 * Visual Rarity Badge pill
 */
@Composable
private fun RarityBadge(rarity: String) {
    val (bgColor, textColor) = when (rarity) {
        "史诗" -> Color(0xFFFF9800).copy(alpha = 0.15f) to Color(0xFFE65100)
        "稀有" -> Color(0xFF9C27B0).copy(alpha = 0.15f) to Color(0xFF7B1FA2)
        "精选" -> Color(0xFF009688).copy(alpha = 0.15f) to Color(0xFF00695C)
        else -> Color(0xFF3F51B5).copy(alpha = 0.12f) to Color(0xFF283593)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = rarity,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
