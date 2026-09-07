package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val species: String,
    val isCustomSpecies: Boolean = false,
    val baseBodyType: String = "CAT", // CAT, DOG, RABBIT, FOX, PANDA, CAPYBARA, DRAGON
    val gender: String = "FEMALE", // MALE, FEMALE
    val birthday: Long = System.currentTimeMillis() - 86400000L * 7, // 7 days ago
    val level: Int = 1,
    val exp: Int = 30,
    val hunger: Int = 75,
    val happiness: Int = 85,
    val cleanliness: Int = 90,
    val energy: Int = 80,
    val weight: Float = 2.4f,
    val primaryColor: Long = 0xFFFFAB91, // Soft peach/coral
    val secondaryColor: Long = 0xFFFFFFFF, // Cream white
    val earStyle: String = "POINTY", // POINTY, ROUND, DROOPY, LONG, HORNS
    val eyeStyle: String = "SPARKLE", // SPARKLE, HAPPY_CURVE, BIG_ROUND, WINK
    val tailStyle: String = "FLUFFY", // FLUFFY, SLENDER, PUFF_BALL, CURLY, STAR_TAIL
    val accessory: String = "BELL_COLLAR", // NONE, BOW_TIE, BELL_COLLAR, BERET_HAT, SUNGLASSES, HALO, CAPE
    val personality: String = "活泼粘人",
    val catchphrase: String = "喵呜~ 想吃小鱼干！",
    val totalFeedCount: Int = 5,
    val totalBathCount: Int = 2,
    val totalPlayCount: Int = 8,
    val coins: Int = 180, // 装扮币
    val unlockedOutfits: String = "NONE,BELL_COLLAR,BOW_TIE,BERET_HAT,SUNGLASSES,CAPE",
    val dailyCheckInStreak: Int = 1,
    val lastCheckInDate: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "growth_records")
data class GrowthRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val eventType: String, // ADOPTION, FEED, BATH, PLAY, LEVEL_UP, WEIGHT, DIARY, VISIT
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val moodTag: String = "开心",
    val extraMetric: String = ""
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorName: String,
    val petName: String,
    val petSpecies: String,
    val petLevel: Int,
    val baseBodyType: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val earStyle: String,
    val eyeStyle: String,
    val tailStyle: String,
    val accessory: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String = "萌宠日常"
)

@Entity(tableName = "post_comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FoodItem(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String,
    val hungerRestore: Int,
    val happinessRestore: Int,
    val energyRestore: Int,
    val expGain: Int
)

val DEFAULT_FOOD_LIST = listOf(
    FoodItem(
        id = "meat",
        name = "鲜嫩肉块",
        iconEmoji = "🥩",
        description = "高蛋白营养肉块，快速补充体力",
        hungerRestore = 25,
        happinessRestore = 10,
        energyRestore = 10,
        expGain = 15
    ),
    FoodItem(
        id = "salmon",
        name = "鲜美三文鱼",
        iconEmoji = "🐟",
        description = "深海优质鱼肉，亮泽毛发超爱吃",
        hungerRestore = 35,
        happinessRestore = 25,
        energyRestore = 15,
        expGain = 25
    ),
    FoodItem(
        id = "pudding",
        name = "蜜糖布丁",
        iconEmoji = "🍮",
        description = "香甜软糯的甜点，心情瞬间爆棚",
        hungerRestore = 15,
        happinessRestore = 35,
        energyRestore = 20,
        expGain = 20
    ),
    FoodItem(
        id = "berry",
        name = "魔法元气果",
        iconEmoji = "🍓",
        description = "蕴含自然魔法的神奇浆果，恢复充沛精力",
        hungerRestore = 20,
        happinessRestore = 20,
        energyRestore = 35,
        expGain = 30
    ),
    FoodItem(
        id = "carrot",
        name = "脆甜胡萝卜",
        iconEmoji = "🥕",
        description = "清脆爽口助消化，营养均衡",
        hungerRestore = 20,
        happinessRestore = 15,
        energyRestore = 10,
        expGain = 15
    ),
    FoodItem(
        id = "cake",
        name = "梦幻庆典蛋糕",
        iconEmoji = "🍰",
        description = "高级烘焙盛宴，满满的爱与幸福！",
        hungerRestore = 50,
        happinessRestore = 50,
        energyRestore = 30,
        expGain = 60
    )
)
