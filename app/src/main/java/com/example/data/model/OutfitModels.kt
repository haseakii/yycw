package com.example.data.model

import androidx.annotation.DrawableRes

/**
 * 饰品与装扮分类
 */
enum class OutfitCategory(val displayName: String, val icon: String) {
    ALL("全部", "✨"),
    HEAD("头部", "👑"),
    FACE("面饰", "👓"),
    CLOTHES("服饰", "👕"),
    COLLAR("项圈", "🎀"),
    EFFECT("特效", "🌟")
}

/**
 * 饰品道具模型
 */
data class OutfitItem(
    val id: String,
    val name: String,
    val category: OutfitCategory,
    val price: Int,               // 所需装扮币
    val iconEmoji: String,        // 预览/占位图标
    @DrawableRes val drawableRes: Int? = null,
    val description: String = "",
    val happinessBonus: Int = 5   // 穿戴增益效果
)

/**
 * 商店页面 UI 状态
 */
data class ShopUiState(
    val outfitCoins: Int = 0,                         // 用户当前持有的装扮币
    val selectedCategory: OutfitCategory = OutfitCategory.ALL,
    val ownedItemIds: Set<String> = emptySet(),       // 已购买的饰品 ID
    val equippedItemIds: Set<String> = emptySet(),    // 当前正式穿戴的饰品 ID
    val previewItem: OutfitItem? = null,              // 当前正在实时试穿预览的饰品（为 null 时展示原装扮）
    val isPurchasing: Boolean = false,
    val userMessage: String? = null                   // 操作提示（如“购买成功”、“装扮币不足”）
)

