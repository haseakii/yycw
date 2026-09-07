package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey
    val id: String, // e.g. "TASK_BATH", "TASK_FEED", "TASK_PLAY", "TASK_CUDDLE", "TASK_DIARY", "TASK_SHARE"
    val dateKey: String, // e.g. "2026-09-07"
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String, // CLEAN, FEED, PLAY, LOVE, DIARY, SHARE
    val rewardCoins: Int,
    val rewardExp: Int,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val isClaimed: Boolean = false
)

data class DailyTaskTemplate(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String,
    val rewardCoins: Int,
    val rewardExp: Int,
    val targetProgress: Int
)

val DEFAULT_DAILY_TASKS = listOf(
    DailyTaskTemplate(
        id = "TASK_BATH",
        title = "香氛舒爽沐浴",
        description = "给萌宠做一次草莓泡泡浴，清洁毛发保持洁净芳香",
        iconEmoji = "🛁",
        category = "CLEAN",
        rewardCoins = 30,
        rewardExp = 20,
        targetProgress = 1
    ),
    DailyTaskTemplate(
        id = "TASK_FEED",
        title = "暖心饱腹投喂",
        description = "投喂2次美味食物（肉块/三文鱼/布丁等），补充体力与饱腹度",
        iconEmoji = "🍲",
        category = "FEED",
        rewardCoins = 25,
        rewardExp = 15,
        targetProgress = 2
    ),
    DailyTaskTemplate(
        id = "TASK_PLAY",
        title = "快乐逗趣玩耍",
        description = "陪伴萌宠抛球或逗趣嬉戏，点燃好心情与运动活力",
        iconEmoji = "🧸",
        category = "PLAY",
        rewardCoins = 30,
        rewardExp = 20,
        targetProgress = 1
    ),
    DailyTaskTemplate(
        id = "TASK_CUDDLE",
        title = "温存抚摸蹭蹭",
        description = "在主页或AR中轻触抚摸萌宠2次，感受羁绊依恋",
        iconEmoji = "👋",
        category = "LOVE",
        rewardCoins = 20,
        rewardExp = 15,
        targetProgress = 2
    ),
    DailyTaskTemplate(
        id = "TASK_DIARY",
        title = "珍藏成长瞬间",
        description = "在成长档案中写下1篇专属日记手账，记录温馨陪伴",
        iconEmoji = "📖",
        category = "DIARY",
        rewardCoins = 40,
        rewardExp = 25,
        targetProgress = 1
    ),
    DailyTaskTemplate(
        id = "TASK_SHARE",
        title = "社区广场分享",
        description = "在萌宠社区发布1条生活动态或给邻居暖心评论",
        iconEmoji = "💬",
        category = "SHARE",
        rewardCoins = 35,
        rewardExp = 20,
        targetProgress = 1
    )
)

data class CheckInDayReward(
    val dayNumber: Int,
    val coinReward: Int,
    val expReward: Int,
    val isSpecial: Boolean = false,
    val specialGiftName: String? = null
)

val WEEKLY_CHECK_IN_REWARDS = listOf(
    CheckInDayReward(1, 20, 10),
    CheckInDayReward(2, 25, 15),
    CheckInDayReward(3, 35, 20, true, "元气甜心罐"),
    CheckInDayReward(4, 30, 15),
    CheckInDayReward(5, 40, 25),
    CheckInDayReward(6, 45, 25),
    CheckInDayReward(7, 80, 50, true, "限定星辉装扮券")
)
