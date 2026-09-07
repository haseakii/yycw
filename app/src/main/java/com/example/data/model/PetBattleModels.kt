package com.example.data.model

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Pet Battle Attributes calculated from pet level, vitals, and outfits.
 */
data class PetBattleStats(
    val maxHp: Int,
    val currentHp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val combatPower: Int
)

/**
 * Skill classification for arena tactical choices.
 */
enum class BattleSkillType {
    ATTACK, // Standard attack with status debuff
    BURST,  // Heavy damage burst with critical strike chance
    HEAL,   // Recovery and defensive ward
    ULTIMATE// Species-specific signature blitz
}

/**
 * Battle Skill definition
 */
data class BattleSkill(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val description: String,
    val skillType: BattleSkillType,
    val basePower: Int,
    val cooldownMax: Int,
    var currentCooldown: Int = 0
)

/**
 * Battle combat action turn log
 */
data class BattleActionLog(
    val round: Int,
    val attackerName: String,
    val skillName: String,
    val damage: Int = 0,
    val heal: Int = 0,
    val isCritical: Boolean = false,
    val message: String,
    val userHp: Int,
    val opponentHp: Int
)

/**
 * Arena Competitor Profile from Community
 */
data class CommunityOpponent(
    val id: String,
    val masterName: String,
    val petName: String,
    val species: String,
    val level: Int,
    val primaryColor: Long,
    val secondaryColor: Long,
    val baseBodyType: String = "ROUND",
    val earStyle: String = "POINTED",
    val eyeStyle: String = "ROUND",
    val tailStyle: String = "LONG",
    val accessory: String = "BELL_COLLAR",
    val stats: PetBattleStats,
    val arenaTier: String = "黄金萌宠 ★★★",
    val rating: Int = 1200
) {
    fun toPetEntity(): PetEntity {
        return PetEntity(
            id = 9000L + abs(id.hashCode() % 1000),
            name = petName,
            species = species,
            level = level,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            baseBodyType = baseBodyType,
            earStyle = earStyle,
            eyeStyle = eyeStyle,
            tailStyle = tailStyle,
            accessory = accessory,
            hunger = 90,
            cleanliness = 90,
            happiness = 90,
            energy = 90
        )
    }

    private fun abs(n: Int): Long = kotlin.math.abs(n).toLong()
}

/**
 * Helper to compute Battle Stats for any PetEntity
 */
fun calculatePetBattleStats(pet: PetEntity): PetBattleStats {
    val hp = 120 + pet.level * 22 + (pet.cleanliness * 0.4f).roundToInt()
    val atk = 28 + pet.level * 6 + (pet.energy * 0.2f).roundToInt()
    val def = 16 + pet.level * 4 + (pet.happiness * 0.15f).roundToInt()
    val spd = 20 + pet.level * 3
    val cp = hp + atk * 3 + def * 2 + spd * 2

    return PetBattleStats(
        maxHp = hp,
        currentHp = hp,
        attack = atk,
        defense = def,
        speed = spd,
        combatPower = cp
    )
}

/**
 * Available Battle Skills for a pet
 */
fun getPetBattleSkills(pet: PetEntity): List<BattleSkill> {
    val signatureSkillName = when {
        pet.species.contains("猫") -> "雷霆猫猫拳"
        pet.species.contains("犬") || pet.species.contains("狗") -> "天崩狗狗撞"
        pet.species.contains("兔") -> "月兔流星踢"
        pet.species.contains("龙") -> "神龙极光吐息"
        else -> "超级元气爆发"
    }

    val signatureSkillEmoji = when {
        pet.species.contains("猫") -> "⚡🐾"
        pet.species.contains("犬") || pet.species.contains("狗") -> "💥🐶"
        pet.species.contains("兔") -> "🌠🐰"
        pet.species.contains("龙") -> "🔥🐉"
        else -> "🌟✨"
    }

    return listOf(
        BattleSkill(
            id = "SKILL_CHARM",
            name = "萌力撒娇",
            iconEmoji = "🥺💖",
            description = "用楚楚可怜的眼神直击对手心灵，造成攻击并降低对手防御",
            skillType = BattleSkillType.ATTACK,
            basePower = 100,
            cooldownMax = 0
        ),
        BattleSkill(
            id = "SKILL_POUNCE",
            name = "元气飞扑",
            iconEmoji = "🐾💨",
            description = "蓄力后凌空飞扑，造成140%高额爆发伤害，有35%几率触发暴击",
            skillType = BattleSkillType.BURST,
            basePower = 140,
            cooldownMax = 2
        ),
        BattleSkill(
            id = "SKILL_PURR_HEAL",
            name = "治愈呼噜",
            iconEmoji = "🌿🫧",
            description = "释放治愈低频咕噜声，恢复自身28%最大生命并展开防护罩",
            skillType = BattleSkillType.HEAL,
            basePower = 70,
            cooldownMax = 3
        ),
        BattleSkill(
            id = "SKILL_ULTIMATE",
            name = signatureSkillName,
            iconEmoji = signatureSkillEmoji,
            description = "释放种族专属本命奥义，呼唤天地萌力造成220%毁灭性伤害！",
            skillType = BattleSkillType.ULTIMATE,
            basePower = 220,
            cooldownMax = 4
        )
    )
}

/**
 * Pre-populated Community Arena Opponents
 */
val DEFAULT_COMMUNITY_OPPONENTS = listOf(
    CommunityOpponent(
        id = "OPP_PIPI",
        masterName = "小鱼干采购员",
        petName = "皮皮",
        species = "软萌英短猫",
        level = 4,
        primaryColor = 0xFF90A4AE,
        secondaryColor = 0xFFFFFFFF,
        baseBodyType = "ROUND",
        earStyle = "POINTED",
        eyeStyle = "ROUND",
        tailStyle = "LONG",
        accessory = "BELL_COLLAR",
        stats = PetBattleStats(maxHp = 220, currentHp = 220, attack = 56, defense = 34, speed = 35, combatPower = 480),
        arenaTier = "白银萌宠 ★★",
        rating = 1150
    ),
    CommunityOpponent(
        id = "OPP_SNOWBALL",
        masterName = "月亮湾小兔",
        petName = "雪球",
        species = "奶香垂耳兔",
        level = 5,
        primaryColor = 0xFFFFFFFF,
        secondaryColor = 0xFFFFCDD2,
        baseBodyType = "ROUND",
        earStyle = "DROOPING",
        eyeStyle = "ROUND",
        tailStyle = "POMPOM",
        accessory = "BOW_TIE",
        stats = PetBattleStats(maxHp = 240, currentHp = 240, attack = 60, defense = 38, speed = 40, combatPower = 540),
        arenaTier = "白银萌宠 ★★★",
        rating = 1220
    ),
    CommunityOpponent(
        id = "OPP_DAHUANG",
        masterName = "隔壁王大爷",
        petName = "大黄",
        species = "忠勇柴犬",
        level = 6,
        primaryColor = 0xFFFFB74D,
        secondaryColor = 0xFFFFF8E1,
        baseBodyType = "CHUBBY",
        earStyle = "POINTED",
        eyeStyle = "ROUND",
        tailStyle = "BUSHY",
        accessory = "CAPE",
        stats = PetBattleStats(maxHp = 270, currentHp = 270, attack = 68, defense = 44, speed = 38, combatPower = 620),
        arenaTier = "黄金斗士 ★",
        rating = 1310
    ),
    CommunityOpponent(
        id = "OPP_DRAGON",
        masterName = "极光造梦师",
        petName = "极光龙宝",
        species = "云翼极光龙",
        level = 8,
        primaryColor = 0xFF80DEEA,
        secondaryColor = 0xFFE0F7FA,
        baseBodyType = "SLENDER",
        earStyle = "DRAGON",
        eyeStyle = "ROUND",
        tailStyle = "DRAGON",
        accessory = "BERET_HAT",
        stats = PetBattleStats(maxHp = 320, currentHp = 320, attack = 82, defense = 54, speed = 46, combatPower = 760),
        arenaTier = "钻石宠神 ★★★",
        rating = 1580
    )
)

/**
 * Convert a community post into a challengeable CommunityOpponent
 */
fun CommunityPostEntity.toCommunityOpponent(): CommunityOpponent {
    val dummyPet = PetEntity(
        id = id,
        name = petName,
        species = petSpecies,
        level = petLevel,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        baseBodyType = baseBodyType,
        earStyle = earStyle,
        eyeStyle = eyeStyle,
        tailStyle = tailStyle,
        accessory = accessory,
        hunger = 90,
        cleanliness = 90,
        happiness = 90,
        energy = 90
    )
    val battleStats = calculatePetBattleStats(dummyPet)
    val tier = when {
        petLevel <= 2 -> "青铜萌宠 ★"
        petLevel <= 4 -> "白银萌宠 ★★"
        petLevel <= 6 -> "黄金斗士 ★★★"
        petLevel <= 8 -> "钻石宠神 ★★★"
        else -> "传说大师 👑"
    }
    return CommunityOpponent(
        id = "POST_OPP_$id",
        masterName = authorName,
        petName = petName,
        species = petSpecies,
        level = petLevel,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        baseBodyType = baseBodyType,
        earStyle = earStyle,
        eyeStyle = eyeStyle,
        tailStyle = tailStyle,
        accessory = accessory,
        stats = battleStats,
        arenaTier = tier,
        rating = 1050 + petLevel * 45
    )
}

