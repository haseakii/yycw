package com.example.data.model

data class OutfitItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: String, // HAT, ACCESSORY, CLOTHING, WINGS
    val description: String,
    val styleKey: String,
    val costCoins: Int = 0,
    val rarity: String = "经典"
)

val OUTFIT_CATEGORIES = listOf(
    "ALL" to "全部装扮",
    "HAT" to "个性帽子",
    "ACCESSORY" to "精美饰品",
    "CLOTHING" to "潮流服装",
    "WINGS" to "奇幻翅膀"
)

val OUTFIT_ITEMS_LIST = listOf(
    OutfitItem("NONE", "初始无佩饰", "🐾", "ACCESSORY", "保持宠物原生态萌态，素颜也很可爱", "NONE", costCoins = 0, rarity = "经典"),
    // Hats
    OutfitItem("BERET_HAT", "复古贝雷帽", "🎨", "HAT", "文艺青年画家风，棕咖色毛呢质感", "BERET_HAT", costCoins = 0, rarity = "经典"),
    OutfitItem("WIZARD_HAT", "星月魔法帽", "🧙", "HAT", "闪耀神秘星光的巫师尖顶帽，魔力十足", "WIZARD_HAT", costCoins = 80, rarity = "稀有"),
    OutfitItem("CROWN", "璀璨皇冠", "👑", "HAT", "黄金镶钻萌宠贵族王冠，尊贵优雅", "CROWN", costCoins = 120, rarity = "史诗"),
    OutfitItem("STRAW_HAT", "清凉草帽", "👒", "HAT", "夏日海滩微风遮阳编织草帽", "STRAW_HAT", costCoins = 50, rarity = "精选"),
    OutfitItem("PARTY_HAT", "缤纷庆典帽", "🎉", "HAT", "彩带旋转的派对尖角帽，节日快乐", "PARTY_HAT", costCoins = 50, rarity = "精选"),
    OutfitItem("HEADPHONES", "炫彩电竞耳机", "🎧", "HAT", "霓虹光芒头戴式降噪耳机，动感潮流", "HEADPHONES", costCoins = 80, rarity = "稀有"),
    OutfitItem("FLOWER_PIN", "淡粉樱花发饰", "🌸", "HAT", "春意盎然的精致粉嫩樱花点缀", "FLOWER_PIN", costCoins = 40, rarity = "精选"),

    // Accessories
    OutfitItem("BELL_COLLAR", "金铃项圈", "🔔", "ACCESSORY", "经典喜气红颈圈，走起路来叮当响", "BELL_COLLAR", costCoins = 0, rarity = "经典"),
    OutfitItem("BOW_TIE", "绅士蝴蝶结", "🎀", "ACCESSORY", "优雅俏皮的酒红丝绒领结", "BOW_TIE", costCoins = 0, rarity = "经典"),
    OutfitItem("SUNGLASSES", "酷黑墨镜", "🕶️", "ACCESSORY", "气场全开的酷炫黑超金框墨镜", "SUNGLASSES", costCoins = 0, rarity = "经典"),
    OutfitItem("SCARF", "暖绒围巾", "🧣", "ACCESSORY", "红色英伦格纹针织围巾，冬日温暖", "SCARF", costCoins = 50, rarity = "精选"),
    OutfitItem("PEARL_NECKLACE", "珍珠项链", "📿", "ACCESSORY", "温润光泽的优雅白珍珠串饰", "PEARL_NECKLACE", costCoins = 90, rarity = "稀有"),

    // Clothing
    OutfitItem("CAPE", "英雄红披风", "🦸", "CLOTHING", "随风扬起的小斗篷，守护正义与和平", "CAPE", costCoins = 0, rarity = "经典"),
    OutfitItem("SAILOR_SUIT", "水手海风服", "⚓", "CLOTHING", "蓝白条纹水手领海军装，元气十足", "SAILOR_SUIT", costCoins = 80, rarity = "稀有"),
    OutfitItem("SWEATER", "软糯小毛衣", "🧶", "CLOTHING", "暖黄色麻花编织毛衣，居家舒适", "SWEATER", costCoins = 60, rarity = "精选"),
    OutfitItem("HOODIE", "萌耳卫衣", "🧥", "CLOTHING", "街头潮牌浅紫连帽衫，时尚前线", "HOODIE", costCoins = 85, rarity = "稀有"),

    // Wings & Special
    OutfitItem("HALO", "天使光环", "😇", "WINGS", "圣洁金黄的悬浮小光环，宛若坠入凡间的小天使", "HALO", costCoins = 100, rarity = "稀有"),
    OutfitItem("ANGEL_WINGS", "纯白羽翼", "🪽", "WINGS", "扑闪扑闪的纯白天使小翅膀", "ANGEL_WINGS", costCoins = 150, rarity = "史诗"),
    OutfitItem("DEVIL_HORNS", "小恶魔角", "😈", "WINGS", "俏皮捣蛋的暗红小恶魔角与尾针", "DEVIL_HORNS", costCoins = 110, rarity = "史诗")
)

val FUR_TEXTURE_OPTIONS = listOf(
    "SMOOTH" to "丝滑纯色",
    "STRIPES" to "可爱斑纹",
    "SPOTS" to "斑点花纹",
    "GRADIENT" to "晨曦渐变",
    "STARRY" to "星河流光"
)
