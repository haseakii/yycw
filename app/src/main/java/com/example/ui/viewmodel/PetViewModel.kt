package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.OutfitCategory
import com.example.data.model.OutfitItem
import com.example.data.model.ShopUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PetViewModel : ViewModel() {

    // 预置商城饰品列表
    val shopCatalog = listOf(
        OutfitItem("h_01", "魔法巫师帽", OutfitCategory.HEAD, 120, "🧙‍♂️", description = "带有星光微粒的神秘帽子"),
        OutfitItem("h_02", "小熊贝雷帽", OutfitCategory.HEAD, 80, "🐻", description = "软萌复古的棕色贝雷帽"),
        OutfitItem("h_03", "天使光环", OutfitCategory.HEAD, 200, "😇", description = "散发圣洁微光的悬浮光环"),
        OutfitItem("f_01", "酷炫墨镜", OutfitCategory.FACE, 60, "🕶️", description = "戴上瞬间变身街头潮宠"),
        OutfitItem("f_02", "复古圆框眼镜", OutfitCategory.FACE, 50, "👓", description = "学者风范，乖巧斯文"),
        OutfitItem("c_01", "温暖红毛衣", OutfitCategory.CLOTHES, 150, "🧶", description = "手工针织，冬日必备"),
        OutfitItem("c_02", "绅士小燕尾服", OutfitCategory.CLOTHES, 220, "🤵", description = "参加晚宴与对战的专属正装"),
        OutfitItem("k_01", "红绸铃铛项圈", OutfitCategory.COLLAR, 70, "🔔", description = "走动时会发出清脆叮当声"),
        OutfitItem("e_01", "樱花飘落", OutfitCategory.EFFECT, 180, "🌸", description = "周身环绕飘散的浪漫花瓣")
    )

    private val _shopState = MutableStateFlow(
        ShopUiState(
            outfitCoins = 360, // 初始装扮币（可通过日常任务赚取）
            ownedItemIds = setOf("h_02"),
            equippedItemIds = setOf("h_02")
        )
    )
    val shopState: StateFlow<ShopUiState> = _shopState.asStateFlow()

    /**
     * 切换饰品分类标签
     */
    fun selectCategory(category: OutfitCategory) {
        _shopState.update { it.copy(selectedCategory = category) }
    }

    /**
     * 点击饰品：进入实时试穿预览模式
     * 若再次点击相同试穿饰品，则退出预览恢复原样
     */
    fun togglePreview(item: OutfitItem) {
        _shopState.update { current ->
            if (current.previewItem?.id == item.id) {
                current.copy(previewItem = null) // 取消预览，恢复原样
            } else {
                current.copy(previewItem = item) // 开启当前饰品试穿
            }
        }
    }

    /**
     * 重置所有试穿状态，恢复宠物原始装备
     */
    fun resetPreview() {
        _shopState.update { it.copy(previewItem = null) }
    }

    /**
     * 购买选中的饰品
     */
    fun buyOutfitItem(item: OutfitItem) {
        val currentCoins = _shopState.value.outfitCoins
        if (currentCoins < item.price) {
            _shopState.update { it.copy(userMessage = "装扮币不足，去完成每日任务赚取吧！") }
            return
        }

        viewModelScope.launch {
            _shopState.update { current ->
                current.copy(
                    outfitCoins = currentCoins - item.price,
                    ownedItemIds = current.ownedItemIds + item.id,
                    userMessage = "成功购买【${item.name}】！"
                )
            }
        }
    }

    /**
     * 穿戴或卸下已拥有饰品
     */
    fun toggleEquip(itemId: String) {
        _shopState.update { current ->
            val newEquipped = if (current.equippedItemIds.contains(itemId)) {
                current.equippedItemIds - itemId
            } else {
                current.equippedItemIds + itemId
            }
            current.copy(equippedItemIds = newEquipped, previewItem = null)
        }
    }

    fun clearMessage() {
        _shopState.update { it.copy(userMessage = null) }
    }
}

