package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PetSoundEngine
import com.example.data.db.AppDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.CommunityPostEntity
import com.example.data.model.DEFAULT_FOOD_LIST
import com.example.data.model.DailyTaskEntity
import com.example.data.model.FoodItem
import com.example.data.model.GrowthRecordEntity
import com.example.data.model.OutfitItem
import com.example.data.model.PetEntity
import com.example.data.repository.PetRepository
import com.example.ui.components.PetAnimationMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = PetRepository(database)
    private val soundEngine = PetSoundEngine.getInstance()

    val isSoundMuted: StateFlow<Boolean> = soundEngine.isMuted
    val isPlayingSound: StateFlow<Boolean> = soundEngine.isPlayingAudio
    val currentSoundLabel: StateFlow<String?> = soundEngine.currentSoundLabel

    fun toggleSoundMute() = soundEngine.toggleMute()
    fun setSoundVolume(vol: Float) = soundEngine.setVolume(vol)

    val activePet: StateFlow<PetEntity?> = repository.activePet
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allPets: StateFlow<List<PetEntity>> = repository.allPets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val growthRecords: StateFlow<List<GrowthRecordEntity>> = activePet
        .flatMapLatest { pet ->
            if (pet != null) repository.getGrowthRecords(pet.id)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.communityPosts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailyTasks: StateFlow<List<DailyTaskEntity>> = repository.getDailyTasksFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _arenaRating = MutableStateFlow(1250)
    val arenaRating: StateFlow<Int> = _arenaRating.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDailyTasks()
        }
    }

    private val _animationMode = MutableStateFlow(PetAnimationMode.IDLE)
    val animationMode: StateFlow<PetAnimationMode> = _animationMode.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _isSleeping = MutableStateFlow(false)
    val isSleeping: StateFlow<Boolean> = _isSleeping.asStateFlow()

    val foodList: List<FoodItem> = DEFAULT_FOOD_LIST

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    fun feedPet(food: FoodItem) {
        val pet = activePet.value ?: return
        if (pet.hunger >= 100) {
            _feedbackMessage.value = "${pet.name} 已经吃得圆滚滚的啦，稍微消化一下吧~"
            return
        }
        viewModelScope.launch {
            _animationMode.value = PetAnimationMode.EATING
            soundEngine.playFeeding(food, pet.hunger, pet.species)
            val (updatedPet, message) = repository.feedPet(pet, food)
            _feedbackMessage.value = message
            if (updatedPet.level > pet.level) {
                soundEngine.playLevelUp()
            }
            delay(1500)
            if (_animationMode.value == PetAnimationMode.EATING) {
                _animationMode.value = PetAnimationMode.IDLE
            }
        }
    }

    fun bathPet() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            _animationMode.value = PetAnimationMode.BATHING
            soundEngine.playBathing()
            val (updatedPet, message) = repository.bathPet(pet)
            _feedbackMessage.value = message
            if (updatedPet.level > pet.level) {
                soundEngine.playLevelUp()
            }
            delay(1600)
            if (_animationMode.value == PetAnimationMode.BATHING) {
                _animationMode.value = PetAnimationMode.IDLE
            }
        }
    }

    fun playPet() {
        val pet = activePet.value ?: return
        if (pet.energy <= 15) {
            _feedbackMessage.value = "${pet.name} 有点累啦，先让它睡个好觉充充电吧！💤"
            return
        }
        viewModelScope.launch {
            _animationMode.value = PetAnimationMode.PLAYING
            soundEngine.playPlaying(pet.species)
            val (updatedPet, message) = repository.playPet(pet)
            _feedbackMessage.value = message
            if (updatedPet.level > pet.level) {
                soundEngine.playLevelUp()
            }
            delay(1500)
            if (_animationMode.value == PetAnimationMode.PLAYING) {
                _animationMode.value = PetAnimationMode.IDLE
            }
        }
    }

    fun toggleSleep() {
        val pet = activePet.value ?: return
        val currentlySleeping = _isSleeping.value
        if (!currentlySleeping) {
            _isSleeping.value = true
            _animationMode.value = PetAnimationMode.SLEEPING
            soundEngine.playSleep(isFallingAsleep = true)
            viewModelScope.launch {
                val (_, message) = repository.sleepPet(pet)
                _feedbackMessage.value = message
            }
        } else {
            _isSleeping.value = false
            _animationMode.value = PetAnimationMode.IDLE
            soundEngine.playSleep(isFallingAsleep = false)
            _feedbackMessage.value = "${pet.name} 伸了个大大的懒腰，醒来精神抖擞！✨"
        }
    }

    fun cuddlePet() {
        val pet = activePet.value ?: return
        if (_isSleeping.value) {
            _feedbackMessage.value = "${pet.name} 正在香甜美梦中，发出轻轻的呼噜声~ 💤"
            soundEngine.playPetting(pet.species, happiness = pet.happiness)
            return
        }
        viewModelScope.launch {
            soundEngine.playPetting(pet.species, happiness = pet.happiness)
            val (updatedPet, message) = repository.petCuddle(pet)
            _feedbackMessage.value = message
            if (updatedPet.level > pet.level) {
                soundEngine.playLevelUp()
            }
        }
    }

    fun addGrowthDiary(title: String, content: String, moodTag: String) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            repository.addGrowthDiary(pet.id, title, content, moodTag)
            _feedbackMessage.value = "成长日记已记录，珍藏美好时光！📖"
        }
    }

    fun saveCustomPet(
        existingId: Long = 0,
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
    ) {
        viewModelScope.launch {
            val current = activePet.value
            val petToSave = if (existingId != 0L && current != null && current.id == existingId) {
                current.copy(
                    name = name,
                    species = species,
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
            } else {
                PetEntity(
                    id = existingId,
                    name = name,
                    species = species,
                    isCustomSpecies = isCustomSpecies,
                    baseBodyType = baseBodyType,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    earStyle = earStyle,
                    eyeStyle = eyeStyle,
                    tailStyle = tailStyle,
                    accessory = accessory,
                    personality = personality,
                    catchphrase = catchphrase,
                    isActive = true
                )
            }
            repository.savePet(petToSave)
            _feedbackMessage.value = "宠物造型与属性保存成功！✨"
        }
    }

    fun switchPet(petId: Long) {
        viewModelScope.launch {
            repository.switchActivePet(petId)
            _feedbackMessage.value = "已切换活跃萌宠！🐾"
        }
    }

    fun togglePostLike(postId: Long, currentIsLiked: Boolean) {
        viewModelScope.launch {
            repository.togglePostLike(postId, currentIsLiked)
        }
    }

    fun addComment(postId: Long, content: String) {
        if (content.isBlank()) return
        val currentPet = activePet.value
        val author = currentPet?.name?.let { "$it 的守护者" } ?: "暖心萌宠师"
        viewModelScope.launch {
            repository.addComment(postId, author, content.trim())
            _feedbackMessage.value = "评论发送成功！💬"
        }
    }

    fun publishPost(content: String, tag: String) {
        if (content.isBlank()) return
        val currentPet = activePet.value ?: return
        val author = "${currentPet.name} 的守护者"
        viewModelScope.launch {
            repository.publishPost(
                authorName = author,
                pet = currentPet,
                content = content.trim(),
                tag = tag
            )
            _feedbackMessage.value = "萌宠动态已发布到社区广场！🎉"
        }
    }

    fun getCommentsForPost(postId: Long) = repository.getComments(postId)

    fun recordBattleVictory(coinsWon: Int, expGained: Int, summary: String) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val newCoins = pet.coins + coinsWon
            var newExp = pet.exp + expGained
            var newLevel = pet.level
            var expNeeded = pet.level * 100
            var leveledUp = false

            while (newExp >= expNeeded) {
                newExp -= expNeeded
                newLevel++
                leveledUp = true
                expNeeded = newLevel * 100
            }

            val updatedPet = pet.copy(
                coins = newCoins,
                exp = newExp,
                level = newLevel
            )
            repository.savePet(updatedPet)
            _arenaRating.value = _arenaRating.value + 25
            _feedbackMessage.value = "🏆 擂台大胜！获得 +$coinsWon 装扮币，+$expGained 成长经验！"
            if (leveledUp) {
                soundEngine.playLevelUp()
            }
        }
    }

    fun claimDailyTaskReward(taskId: String) {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val result = repository.claimDailyTaskReward(taskId, pet)
            if (result != null) {
                val (_, coins, exp) = result
                soundEngine.playCoinReward(coins)
                _feedbackMessage.value = "🎉 任务奖励领取成功：+$coins 装扮币，+$exp EXP！"
            }
        }
    }

    fun claimMilestoneReward(tier: Int) {
        val pet = activePet.value ?: return
        val (bonusCoins, name) = when (tier) {
            1 -> Pair(40, "陪伴进阶小礼包")
            else -> Pair(80, "今日全勤至尊宝箱")
        }
        viewModelScope.launch {
            repository.claimMilestoneBonus(pet, bonusCoins, name)
            soundEngine.playCoinReward(bonusCoins)
            _feedbackMessage.value = "🎁 成功开启【$name】：+$bonusCoins 装扮币！"
        }
    }

    fun checkInToday() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val result = repository.performDailyCheckIn(pet)
            if (result != null) {
                val (updatedPet, reward) = result
                soundEngine.playCoinReward(reward.coinReward)
                _feedbackMessage.value = "🌟 签到成功！已连续打卡 ${updatedPet.dailyCheckInStreak} 天，获得 +${reward.coinReward} 装扮币！"
            } else {
                _feedbackMessage.value = "今日已经签过到啦，明天继续保持哦！✨"
            }
        }
    }

    fun purchaseOutfit(outfit: OutfitItem) {
        purchaseShopOutfit(outfit, autoEquip = true)
    }

    fun purchaseShopOutfit(outfit: OutfitItem, autoEquip: Boolean = true) {
        val pet = activePet.value ?: return
        val unlockedList = pet.unlockedOutfits.split(",")
        if (unlockedList.contains(outfit.id)) {
            if (autoEquip && pet.accessory != outfit.id) {
                viewModelScope.launch {
                    val updated = repository.equipAccessory(pet, outfit.id)
                    _activePet.value = updated
                    soundEngine.playClick()
                    _feedbackMessage.value = "已为「${pet.name}」换上「${outfit.name}」！✨"
                }
            } else {
                _feedbackMessage.value = "「${outfit.name}」已在衣橱中！"
            }
            return
        }

        if (pet.coins < outfit.costCoins) {
            _feedbackMessage.value = "装扮币不足（需 ${outfit.costCoins} 币，当前拥有 ${pet.coins} 币，还差 ${outfit.costCoins - pet.coins} 币），做日常任务或社区对战可赚取哦！"
            return
        }

        viewModelScope.launch {
            val (updatedPet, success) = repository.unlockOutfitItem(pet, outfit.id, outfit.costCoins, autoWear = autoEquip)
            if (success) {
                _activePet.value = updatedPet
                soundEngine.playLevelUp()
                _feedbackMessage.value = if (autoEquip) {
                    "🎉 成功购买并为「${pet.name}」换上【${outfit.name}】！"
                } else {
                    "🎉 成功购买【${outfit.name}】，已收入衣橱！"
                }
            }
        }
    }

    fun equipOutfit(outfit: OutfitItem) {
        val pet = activePet.value ?: return
        val unlockedList = pet.unlockedOutfits.split(",")
        if (!unlockedList.contains(outfit.id)) {
            _feedbackMessage.value = "尚未拥有该装扮，请先在集市购买！"
            return
        }
        viewModelScope.launch {
            val updated = repository.equipAccessory(pet, outfit.id)
            _activePet.value = updated
            soundEngine.playClick()
            _feedbackMessage.value = "✨ 已换上「${outfit.name}」！"
        }
    }

    fun claimShopBonus() {
        val pet = activePet.value ?: return
        viewModelScope.launch {
            val bonusAmount = 60
            val updated = repository.addCoins(pet, bonusAmount, "萌宠集市打卡礼包")
            _activePet.value = updated
            soundEngine.playCoinReward()
            _feedbackMessage.value = "🎁 成功领取集市装扮币补给：+$bonusAmount 币！当前余额 ${updated.coins} 币。"
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}
