package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OutfitCategory
import com.example.data.model.OutfitItem
import com.example.ui.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: PetViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.shopState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val filteredItems = remember(state.selectedCategory) {
        if (state.selectedCategory == OutfitCategory.ALL) {
            viewModel.shopCatalog
        } else {
            viewModel.shopCatalog.filter { it.category == state.selectedCategory }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("装扮小铺", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 装扮币余额徽章
                    CoinBalanceChip(coins = state.outfitCoins)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // 底部操作栏：试穿结算或快捷穿戴
            ShopBottomActionBar(
                previewItem = state.previewItem,
                ownedItemIds = state.ownedItemIds,
                equippedItemIds = state.equippedItemIds,
                coins = state.outfitCoins,
                onBuyClicked = { item -> viewModel.buyOutfitItem(item) },
                onEquipClicked = { itemId -> viewModel.toggleEquip(itemId) },
                onResetPreview = { viewModel.resetPreview() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. 实时预览舞台 (Live Preview Stage)
            PetLivePreviewStage(
                previewItem = state.previewItem,
                equippedItemIds = state.equippedItemIds,
                catalog = viewModel.shopCatalog,
                onResetPreview = { viewModel.resetPreview() }
            )

            // 2. 分类切换标签
            ScrollableTabRow(
                selectedTabIndex = state.selectedCategory.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutfitCategory.values().forEach { category ->
                    Tab(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        text = {
                            Text("${category.icon} ${category.displayName}")
                        }
                    )
                }
            }

            // 3. 饰品网格列表
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val isOwned = state.ownedItemIds.contains(item.id)
                    val isEquipped = state.equippedItemIds.contains(item.id)
                    val isPreviewing = state.previewItem?.id == item.id

                    ShopItemCard(
                        item = item,
                        isOwned = isOwned,
                        isEquipped = isEquipped,
                        isPreviewing = isPreviewing,
                        onClick = {
                            viewModel.togglePreview(item)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 顶部装扮币胶囊组件
 */
@Composable
fun CoinBalanceChip(coins: Int) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF3E0),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text("🪙", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = coins.toString(),
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE65100),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 实时试穿预览舞台组件
 */
@Composable
fun PetLivePreviewStage(
    previewItem: OutfitItem?,
    equippedItemIds: Set<String>,
    catalog: List<OutfitItem>,
    onResetPreview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color(0xFFEDE7F6)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 宠物虚拟展示
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐱", fontSize = 64.sp)

                    // 叠加装扮效果（优先试穿项，其次正式穿戴项）
                    val displayHead = previewItem?.takeIf { it.category == OutfitCategory.HEAD }
                        ?: catalog.firstOrNull { it.id in equippedItemIds && it.category == OutfitCategory.HEAD }
                    displayHead?.let {
                        Text(
                            text = it.iconEmoji,
                            fontSize = 28.sp,
                            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-14).dp)
                        )
                    }

                    val displayFace = previewItem?.takeIf { it.category == OutfitCategory.FACE }
                        ?: catalog.firstOrNull { it.id in equippedItemIds && it.category == OutfitCategory.FACE }
                    displayFace?.let {
                        Text(
                            text = it.iconEmoji,
                            fontSize = 24.sp,
                            modifier = Modifier.align(Alignment.Center).offset(y = (-4).dp)
                        )
                    }

                    val displayCollar = previewItem?.takeIf { it.category == OutfitCategory.COLLAR }
                        ?: catalog.firstOrNull { it.id in equippedItemIds && it.category == OutfitCategory.COLLAR }
                    displayCollar?.let {
                        Text(
                            text = it.iconEmoji,
                            fontSize = 22.sp,
                            modifier = Modifier.align(Alignment.BottomCenter).offset(y = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "喵喵 (开心度 100%)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 试穿状态指示与一键重置按钮
            AnimatedVisibility(
                visible = previewItem != null,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("试穿效果中: ${previewItem?.name}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "恢复原样",
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onResetPreview() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 饰品网格卡片
 */
@Composable
fun ShopItemCard(
    item: OutfitItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    isPreviewing: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isPreviewing -> MaterialTheme.colorScheme.primary
        isEquipped -> Color(0xFF4CAF50)
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPreviewing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.iconEmoji, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = item.description, fontSize = 11.sp, color = Color.Gray, maxLines = 1)

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOwned) {
                    Surface(
                        color = if (isEquipped) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isEquipped) "已穿戴" else "已拥有",
                            color = if (isEquipped) Color(0xFF2E7D32) else Color.DarkGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text("🪙", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.price}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        fontSize = 13.sp
                    )
                }

                if (isPreviewing) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "试穿中",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 底部购买/穿戴与结算栏
 */
@Composable
fun ShopBottomActionBar(
    previewItem: OutfitItem?,
    ownedItemIds: Set<String>,
    equippedItemIds: Set<String>,
    coins: Int,
    onBuyClicked: (OutfitItem) -> Unit,
    onEquipClicked: (String) -> Unit,
    onResetPreview: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (previewItem == null) {
                Text(
                    text = "点击饰品可在上方实时试穿 🐾",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                OutlinedButton(
                    onClick = onResetPreview,
                    enabled = equippedItemIds.isNotEmpty()
                ) {
                    Text("恢复原装")
                }
            } else {
                val isOwned = ownedItemIds.contains(previewItem.id)
                val isEquipped = equippedItemIds.contains(previewItem.id)
                val canAfford = coins >= previewItem.price

                Column {
                    Text(
                        text = previewItem.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isOwned) "已解锁该饰品" else "单价: 🪙 ${previewItem.price} 装扮币",
                        fontSize = 12.sp,
                        color = if (!isOwned && !canAfford) Color.Red else Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onResetPreview,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("取消试穿")
                    }

                    if (isOwned) {
                        Button(
                            onClick = { onEquipClicked(previewItem.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEquipped) Color(0xFF757575) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isEquipped) "卸下" else "立即穿戴")
                        }
                    } else {
                        Button(
                            onClick = { onBuyClicked(previewItem) },
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800)
                            )
                        ) {
                            Text(if (canAfford) "购买此饰品" else "装扮币不足")
                        }
                    }
                }
            }
        }
    }
}

