package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.CommunityPostEntity
import com.example.data.model.DailyTaskEntity
import com.example.data.model.GrowthRecordEntity
import com.example.data.model.PetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PetEntity::class,
        GrowthRecordEntity::class,
        CommunityPostEntity::class,
        CommentEntity::class,
        DailyTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun communityDao(): CommunityDao
    abstract fun dailyTaskDao(): DailyTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cloud_pet_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val petDao = database.petDao()
            val growthDao = database.growthRecordDao()
            val communityDao = database.communityDao()

            val defaultPet = PetEntity(
                name = "泡芙",
                species = "软萌英短猫",
                isCustomSpecies = false,
                baseBodyType = "CAT",
                gender = "FEMALE",
                birthday = System.currentTimeMillis() - 86400000L * 12, // 12 days ago
                level = 2,
                exp = 65,
                hunger = 70,
                happiness = 85,
                cleanliness = 80,
                energy = 90,
                weight = 2.6f,
                primaryColor = 0xFFFFAB91,
                secondaryColor = 0xFFFFF8E1,
                earStyle = "POINTY",
                eyeStyle = "SPARKLE",
                tailStyle = "FLUFFY",
                accessory = "BELL_COLLAR",
                personality = "活泼粘人",
                catchphrase = "喵呜~ 蹭蹭主人手心！",
                totalFeedCount = 16,
                totalBathCount = 4,
                totalPlayCount = 22,
                isActive = true
            )
            val petId = petDao.insertPet(defaultPet)

            // Initial growth records
            growthDao.insertRecord(
                GrowthRecordEntity(
                    petId = petId,
                    eventType = "ADOPTION",
                    title = "入住云端温馨小窝",
                    description = "小泡芙正式来到云养世界，好奇地嗅着周围，软软地叫了一声。",
                    timestamp = System.currentTimeMillis() - 86400000L * 12,
                    moodTag = "初次相遇",
                    extraMetric = "体重 2.0kg"
                )
            )
            growthDao.insertRecord(
                GrowthRecordEntity(
                    petId = petId,
                    eventType = "LEVEL_UP",
                    title = "晋升为 2阶 · 灵动幼宠",
                    description = "在主人的悉心照顾下，小泡芙解锁了新装扮和欢快翻滚动作！",
                    timestamp = System.currentTimeMillis() - 86400000L * 5,
                    moodTag = "升级啦",
                    extraMetric = "Lv.2"
                )
            )
            growthDao.insertRecord(
                GrowthRecordEntity(
                    petId = petId,
                    eventType = "FEED",
                    title = "第一次大嚼鲜美三文鱼",
                    description = "吃得满嘴都是，开心地发出呼噜呼噜的满足声~",
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    moodTag = "饱饱哒",
                    extraMetric = "+35 饱腹"
                )
            )
            growthDao.insertRecord(
                GrowthRecordEntity(
                    petId = petId,
                    eventType = "BATH",
                    title = "草莓香香泡泡浴",
                    description = "洗完擦干毛茸茸的大尾巴，全身散发着清甜香气，在垫子上伸了个大懒腰。",
                    timestamp = System.currentTimeMillis() - 86400000L * 1,
                    moodTag = "香喷喷",
                    extraMetric = "清洁度满格"
                )
            )

            // Initial community posts
            val post1Id = communityDao.insertPost(
                CommunityPostEntity(
                    authorName = "喵星守护官",
                    petName = "糯米团",
                    petSpecies = "垂耳白兔",
                    petLevel = 3,
                    baseBodyType = "RABBIT",
                    primaryColor = 0xFFFFFFFF,
                    secondaryColor = 0xFFFFCDD2,
                    earStyle = "DROOPY",
                    eyeStyle = "HAPPY_CURVE",
                    tailStyle = "PUFF_BALL",
                    accessory = "BOW_TIE",
                    content = "我家糯米团今天学会主动跑过来叼胡萝卜啦！两只大垂耳一晃一晃的，治愈了我一整天的疲惫~ 🥕✨",
                    likesCount = 18,
                    isLiked = false,
                    timestamp = System.currentTimeMillis() - 3600000L * 3,
                    tag = "萌宠日常"
                )
            )
            communityDao.insertComment(
                CommentEntity(
                    postId = post1Id,
                    authorName = "芝士脆波波",
                    content = "太软萌了吧！求问领结是在工坊哪个部件配的呀？",
                    timestamp = System.currentTimeMillis() - 3600000L * 2
                )
            )
            communityDao.insertComment(
                CommentEntity(
                    postId = post1Id,
                    authorName = "喵星守护官",
                    content = "在工坊配件里选‘绅士蝴蝶结’就可以啦，搭配浅粉耳朵超好看！",
                    timestamp = System.currentTimeMillis() - 3600000L * 1
                )
            )

            val post2Id = communityDao.insertPost(
                CommunityPostEntity(
                    authorName = "阳光柴犬社",
                    petName = "旺财",
                    petSpecies = "活力柴犬",
                    petLevel = 5,
                    baseBodyType = "DOG",
                    primaryColor = 0xFFFFA726,
                    secondaryColor = 0xFFFFF8E1,
                    earStyle = "POINTY",
                    eyeStyle = "SPARKLE",
                    tailStyle = "CURLY",
                    accessory = "BERET_HAT",
                    content = "给大家展示戴着小贝雷帽的帅气旺财！今天在庭院狂奔追球，吐着小舌头傻笑，欢迎邻居们来串门喂小肉干！🐾🐕",
                    likesCount = 32,
                    isLiked = true,
                    timestamp = System.currentTimeMillis() - 3600000L * 8,
                    tag = "互动求串门"
                )
            )
            communityDao.insertComment(
                CommentEntity(
                    postId = post2Id,
                    authorName = "阿柴狂热粉",
                    content = "已经去串门投喂了一块特级三文鱼，旺财开心地转圈圈了！",
                    timestamp = System.currentTimeMillis() - 3600000L * 6
                )
            )

            communityDao.insertPost(
                CommunityPostEntity(
                    authorName = "星际幻想家",
                    petName = "星穹",
                    petSpecies = "自定义物种 · 星灵羽龙",
                    petLevel = 4,
                    baseBodyType = "DRAGON",
                    primaryColor = 0xFF9FA8DA,
                    secondaryColor = 0xFFE1BEE7,
                    earStyle = "HORNS",
                    eyeStyle = "SPARKLE",
                    tailStyle = "STAR_TAIL",
                    accessory = "HALO",
                    content = "在工坊自定义合成的新物种【星灵羽龙】！蓝紫色星云鳞片，头顶光环，睡觉时还会吐出微光小星星呢～喜欢的小伙伴也可以去工坊创建哦！⭐",
                    likesCount = 56,
                    isLiked = false,
                    timestamp = System.currentTimeMillis() - 3600000L * 16,
                    tag = "物种创作者"
                )
            )

            communityDao.insertPost(
                CommunityPostEntity(
                    authorName = "竹林客",
                    petName = "团团",
                    petSpecies = "呆萌小熊猫",
                    petLevel = 2,
                    baseBodyType = "PANDA",
                    primaryColor = 0xFFFF7043,
                    secondaryColor = 0xFF4E342E,
                    earStyle = "ROUND",
                    eyeStyle = "BIG_ROUND",
                    tailStyle = "FLUFFY",
                    accessory = "BELL_COLLAR",
                    content = "团团今天第一次泡泡浴，全程呆呆地顶着一团白泡沫，简直像个小玩偶！记录一下成长里程碑~ ❤️",
                    likesCount = 24,
                    isLiked = false,
                    timestamp = System.currentTimeMillis() - 3600000L * 24,
                    tag = "成长见证"
                )
            )
        }
    }
}
