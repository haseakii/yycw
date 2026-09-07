package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.CheckInDayReward
import com.example.data.model.CommentEntity
import com.example.data.model.CommunityPostEntity
import com.example.data.model.DEFAULT_DAILY_TASKS
import com.example.data.model.DailyTaskEntity
import com.example.data.model.FoodItem
import com.example.data.model.GrowthRecordEntity
import com.example.data.model.PetEntity
import com.example.data.model.WEEKLY_CHECK_IN_REWARDS
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class PetRepository(private val database: AppDatabase) {
    private val petDao = database.petDao()
    private val growthRecordDao = database.growthRecordDao()
    private val communityDao = database.communityDao()
    private val dailyTaskDao = database.dailyTaskDao()

    val activePet: Flow<PetEntity?> = petDao.getActivePetFlow()
    val allPets: Flow<List<PetEntity>> = petDao.getAllPetsFlow()
    val communityPosts: Flow<List<CommunityPostEntity>> = communityDao.getAllPostsFlow()

    fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getDailyTasksFlow(dateKey: String = getTodayDateKey()): Flow<List<DailyTaskEntity>> {
        return dailyTaskDao.getDailyTasksFlow(dateKey)
    }

    suspend fun ensureDailyTasks(dateKey: String = getTodayDateKey()): List<DailyTaskEntity> {
        val existing = dailyTaskDao.getDailyTasks(dateKey)
        if (existing.isEmpty()) {
            val initialTasks = DEFAULT_DAILY_TASKS.map { template ->
                DailyTaskEntity(
                    id = "${template.id}_$dateKey",
                    dateKey = dateKey,
                    title = template.title,
                    description = template.description,
                    iconEmoji = template.iconEmoji,
                    category = template.category,
                    rewardCoins = template.rewardCoins,
                    rewardExp = template.rewardExp,
                    currentProgress = 0,
                    targetProgress = template.targetProgress,
                    isClaimed = false
                )
            }
            dailyTaskDao.insertTasks(initialTasks)
            return initialTasks
        }
        return existing
    }

    suspend fun incrementDailyTask(taskCategory: String, delta: Int = 1) {
        val dateKey = getTodayDateKey()
        ensureDailyTasks(dateKey)
        val template = DEFAULT_DAILY_TASKS.find { it.category == taskCategory } ?: return
        val fullId = "${template.id}_$dateKey"
        dailyTaskDao.incrementProgress(fullId, dateKey, delta)
    }

    suspend fun claimDailyTaskReward(taskId: String, pet: PetEntity): Triple<PetEntity, Int, Int>? {
        val task = dailyTaskDao.getTaskById(taskId) ?: return null
        if (task.isClaimed || task.currentProgress < task.targetProgress) return null

        dailyTaskDao.markTaskClaimed(taskId)

        val newCoins = pet.coins + task.rewardCoins
        val totalExp = pet.exp + task.rewardExp
        val expNeeded = pet.level * 100
        val isLevelUp = totalExp >= expNeeded
        val newLevel = if (isLevelUp) pet.level + 1 else pet.level
        val remainingExp = if (isLevelUp) totalExp - expNeeded else totalExp

        val updatedPet = pet.copy(
            coins = newCoins,
            level = newLevel,
            exp = remainingExp
        )
        petDao.updatePet(updatedPet)

        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = pet.id,
                eventType = "TASK_REWARD",
                title = "日常任务达标：${task.title}",
                description = "勤劳陪伴获丰厚奖赏！赢得 ${task.rewardCoins} 装扮币与 ${task.rewardExp} EXP。",
                moodTag = "日常成就",
                extraMetric = "+${task.rewardCoins} 币"
            )
        )

        return Triple(updatedPet, task.rewardCoins, task.rewardExp)
    }

    suspend fun claimMilestoneBonus(pet: PetEntity, bonusCoins: Int, milestoneName: String): PetEntity {
        val updatedPet = pet.copy(coins = pet.coins + bonusCoins)
        petDao.updatePet(updatedPet)
        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = pet.id,
                eventType = "MILESTONE_REWARD",
                title = "解锁日常宝箱：$milestoneName",
                description = "达成今日陪伴日常目标，开启惊喜大宝箱获得 $bonusCoins 装扮币！",
                moodTag = "惊喜宝箱",
                extraMetric = "+$bonusCoins 币"
            )
        )
        return updatedPet
    }

    suspend fun performDailyCheckIn(pet: PetEntity): Pair<PetEntity, CheckInDayReward>? {
        val today = getTodayDateKey()
        if (pet.lastCheckInDate == today) {
            return null // Already checked in today
        }

        // Calculate streak
        val newStreak = if (pet.dailyCheckInStreak in 1..6) pet.dailyCheckInStreak + 1 else 1
        val rewardIndex = (newStreak - 1).coerceIn(0, WEEKLY_CHECK_IN_REWARDS.lastIndex)
        val reward = WEEKLY_CHECK_IN_REWARDS[rewardIndex]

        val updatedPet = pet.copy(
            coins = pet.coins + reward.coinReward,
            exp = pet.exp + reward.expReward,
            dailyCheckInStreak = newStreak,
            lastCheckInDate = today
        )
        petDao.updatePet(updatedPet)

        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = pet.id,
                eventType = "CHECK_IN",
                title = "连续打卡第 $newStreak 天",
                description = "今日温馨签到成功，获得 ${reward.coinReward} 装扮币！${reward.specialGiftName?.let { "附赠特别礼：$it" } ?: ""}",
                moodTag = "连续陪伴",
                extraMetric = "Day $newStreak"
            )
        )

        return Pair(updatedPet, reward)
    }

    suspend fun unlockOutfitItem(pet: PetEntity, outfitId: String, cost: Int, autoWear: Boolean = true): Pair<PetEntity, Boolean> {
        val unlockedList = pet.unlockedOutfits.split(",").toMutableList()
        if (unlockedList.contains(outfitId)) {
            if (autoWear && pet.accessory != outfitId) {
                val equippedPet = pet.copy(accessory = outfitId)
                petDao.updatePet(equippedPet)
                return Pair(equippedPet, true)
            }
            return Pair(pet, true) // Already unlocked
        }
        if (pet.coins < cost) {
            return Pair(pet, false) // Not enough coins
        }

        unlockedList.add(outfitId)
        val updatedPet = pet.copy(
            coins = pet.coins - cost,
            unlockedOutfits = unlockedList.joinToString(","),
            accessory = if (autoWear) outfitId else pet.accessory
        )
        petDao.updatePet(updatedPet)

        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = pet.id,
                eventType = "OUTFIT_UNLOCK",
                title = "集市解锁全新装扮",
                description = "消耗 $cost 装扮币，成功将【$outfitId】收入衣橱${if (autoWear) "并实时换上新装" else ""}！",
                moodTag = "新衣服",
                extraMetric = "-$cost 币"
            )
        )

        return Pair(updatedPet, true)
    }

    suspend fun equipAccessory(pet: PetEntity, accessoryId: String): PetEntity {
        val updatedPet = pet.copy(accessory = accessoryId)
        petDao.updatePet(updatedPet)
        return updatedPet
    }

    suspend fun addCoins(pet: PetEntity, amount: Int, reason: String): PetEntity {
        val updatedPet = pet.copy(coins = pet.coins + amount)
        petDao.updatePet(updatedPet)
        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = pet.id,
                eventType = "COIN_BONUS",
                title = reason,
                description = "在萌宠集市获得 $amount 装扮币，当前余额 ${updatedPet.coins} 币！",
                moodTag = "财源滚滚",
                extraMetric = "+$amount 币"
            )
        )
        return updatedPet
    }

    fun getGrowthRecords(petId: Long): Flow<List<GrowthRecordEntity>> {
        return growthRecordDao.getGrowthRecordsFlow(petId)
    }

    fun getComments(postId: Long): Flow<List<CommentEntity>> {
        return communityDao.getCommentsForPostFlow(postId)
    }

    suspend fun feedPet(pet: PetEntity, food: FoodItem): Pair<PetEntity, String> {
        incrementDailyTask("FEED", 1)
        val newHunger = min(100, pet.hunger + food.hungerRestore)
        val newHappiness = min(100, pet.happiness + food.happinessRestore)
        val newEnergy = min(100, pet.energy + food.energyRestore)
        val totalExp = pet.exp + food.expGain
        val expNeeded = pet.level * 100
        val isLevelUp = totalExp >= expNeeded
        val newLevel = if (isLevelUp) pet.level + 1 else pet.level
        val remainingExp = if (isLevelUp) totalExp - expNeeded else totalExp
        val newWeight = pet.weight + 0.05f

        val updatedPet = pet.copy(
            hunger = newHunger,
            happiness = newHappiness,
            energy = newEnergy,
            level = newLevel,
            exp = remainingExp,
            weight = (newWeight * 10).toInt() / 10f,
            totalFeedCount = pet.totalFeedCount + 1
        )
        petDao.updatePet(updatedPet)

        // Growth record
        val message = if (isLevelUp) {
            growthRecordDao.insertRecord(
                GrowthRecordEntity(
                    petId = pet.id,
                    eventType = "LEVEL_UP",
                    title = "升级啦！迈入 Lv.$newLevel",
                    description = "吃饱饱后精神焕发，解锁了更多活力与可爱姿态！",
                    moodTag = "升级啦",
                    extraMetric = "Lv.$newLevel"
                )
            )
            "升级啦！${pet.name} 晋级为 Lv.$newLevel！"
        } else {
            growthRecordDao.insertRecord(
                GrowthRecordEntity(
                    petId = pet.id,
                    eventType = "FEED",
                    title = "享受美味「${food.name}」",
                    description = "${pet.name} 开心地吃掉了 ${food.name}，饱腹+${food.hungerRestore}，心情+${food.happinessRestore}",
                    moodTag = "饱饱哒",
                    extraMetric = "+${food.hungerRestore} 饱腹"
                )
            )
            "${pet.name} 吃得好开心！(+${food.hungerRestore}饱腹, +${food.expGain}经验)"
        }

        return Pair(updatedPet, message)
    }

    suspend fun bathPet(pet: PetEntity): Pair<PetEntity, String> {
        incrementDailyTask("CLEAN", 1)
        val newCleanliness = 100
        val newHappiness = min(100, pet.happiness + 15)
        val totalExp = pet.exp + 20
        val expNeeded = pet.level * 100
        val isLevelUp = totalExp >= expNeeded
        val newLevel = if (isLevelUp) pet.level + 1 else pet.level
        val remainingExp = if (isLevelUp) totalExp - expNeeded else totalExp

        val updatedPet = pet.copy(
            cleanliness = newCleanliness,
            happiness = newHappiness,
            level = newLevel,
            exp = remainingExp,
            totalBathCount = pet.totalBathCount + 1
        )
        petDao.updatePet(updatedPet)

        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = pet.id,
                eventType = "BATH",
                title = "香喷喷泡泡浴",
                description = "全身洗得干干净净，毛发清爽柔顺，散发着草莓奶香！",
                moodTag = "香喷喷",
                extraMetric = "清洁度满格"
            )
        )

        return Pair(updatedPet, "${pet.name} 浑身香喷喷的，毛茸茸手感极佳！")
    }

    suspend fun playPet(pet: PetEntity): Pair<PetEntity, String> {
        incrementDailyTask("PLAY", 1)
        val newHappiness = min(100, pet.happiness + 25)
        val newEnergy = kotlin.math.max(10, pet.energy - 15)
        val newCleanliness = kotlin.math.max(20, pet.cleanliness - 10)
        val totalExp = pet.exp + 30
        val expNeeded = pet.level * 100
        val isLevelUp = totalExp >= expNeeded
        val newLevel = if (isLevelUp) pet.level + 1 else pet.level
        val remainingExp = if (isLevelUp) totalExp - expNeeded else totalExp

        val updatedPet = pet.copy(
            happiness = newHappiness,
            energy = newEnergy,
            cleanliness = newCleanliness,
            level = newLevel,
            exp = remainingExp,
            totalPlayCount = pet.totalPlayCount + 1
        )
        petDao.updatePet(updatedPet)

        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = pet.id,
                eventType = "PLAY",
                title = "追逐毛线球大冒险",
                description = "${pet.name} 欢快地扑腾翻滚，玩得满头大汗超尽兴！",
                moodTag = "超快乐",
                extraMetric = "+25 心情"
            )
        )

        return Pair(updatedPet, "${pet.name} 开心极了，蹦蹦跳跳地蹭着你！")
    }

    suspend fun sleepPet(pet: PetEntity): Pair<PetEntity, String> {
        val newEnergy = 100
        val newHappiness = min(100, pet.happiness + 10)
        val updatedPet = pet.copy(
            energy = newEnergy,
            happiness = newHappiness
        )
        petDao.updatePet(updatedPet)
        return Pair(updatedPet, "${pet.name} 舒舒服服地睡了个好觉，活力满满！")
    }

    suspend fun petCuddle(pet: PetEntity): Pair<PetEntity, String> {
        incrementDailyTask("LOVE", 1)
        val newHappiness = min(100, pet.happiness + 8)
        val totalExp = pet.exp + 10
        val expNeeded = pet.level * 100
        val isLevelUp = totalExp >= expNeeded
        val newLevel = if (isLevelUp) pet.level + 1 else pet.level
        val remainingExp = if (isLevelUp) totalExp - expNeeded else totalExp

        val updatedPet = pet.copy(
            happiness = newHappiness,
            level = newLevel,
            exp = remainingExp
        )
        petDao.updatePet(updatedPet)

        val dialogues = listOf(
            "${pet.name} 发出惬意的呼噜声，小脑袋在你掌心蹭蹭~",
            "${pet.name} 眯起眼睛，尾巴愉快地轻轻摇晃！",
            "${pet.name} 仰起肚皮求抚摸，对你超级信任！",
            "${pet.name} 轻哼了一声：${pet.catchphrase}"
        )
        return Pair(updatedPet, dialogues.random())
    }

    suspend fun savePet(pet: PetEntity): Long {
        return if (pet.id == 0L) {
            petDao.deactivateAllPets()
            petDao.insertPet(pet.copy(isActive = true))
        } else {
            petDao.updatePet(pet)
            pet.id
        }
    }

    suspend fun switchActivePet(petId: Long) {
        petDao.deactivateAllPets()
        petDao.setActivePet(petId)
    }

    suspend fun addGrowthDiary(petId: Long, title: String, content: String, moodTag: String) {
        incrementDailyTask("DIARY", 1)
        growthRecordDao.insertRecord(
            GrowthRecordEntity(
                petId = petId,
                eventType = "DIARY",
                title = title,
                description = content,
                moodTag = moodTag,
                extraMetric = "日记手账"
            )
        )
    }

    suspend fun togglePostLike(postId: Long, currentIsLiked: Boolean) {
        val delta = if (currentIsLiked) -1 else 1
        communityDao.updatePostLike(postId, delta, !currentIsLiked)
    }

    suspend fun addComment(postId: Long, authorName: String, content: String) {
        incrementDailyTask("SHARE", 1)
        communityDao.insertComment(
            CommentEntity(
                postId = postId,
                authorName = authorName,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun publishPost(
        authorName: String,
        pet: PetEntity,
        content: String,
        tag: String
    ): Long {
        incrementDailyTask("SHARE", 1)
        val post = CommunityPostEntity(
            authorName = authorName,
            petName = pet.name,
            petSpecies = pet.species,
            petLevel = pet.level,
            baseBodyType = pet.baseBodyType,
            primaryColor = pet.primaryColor,
            secondaryColor = pet.secondaryColor,
            earStyle = pet.earStyle,
            eyeStyle = pet.eyeStyle,
            tailStyle = pet.tailStyle,
            accessory = pet.accessory,
            content = content,
            likesCount = 1,
            isLiked = true,
            timestamp = System.currentTimeMillis(),
            tag = tag
        )
        return communityDao.insertPost(post)
    }
}
