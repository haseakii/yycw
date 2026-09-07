package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ARScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.CustomizerScreen
import com.example.ui.screens.GrowthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PetViewModel

enum class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("萌宠小屋", Icons.Filled.Pets, Icons.Outlined.Pets, "nav_tab_home"),
    SHOP("装扮集市", Icons.Filled.Storefront, Icons.Outlined.Storefront, "nav_tab_shop"),
    CUSTOMIZE("定制工坊", Icons.Filled.Palette, Icons.Outlined.Palette, "nav_tab_customize"),
    GROWTH("成长足迹", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_tab_growth"),
    COMMUNITY("萌宠社区", Icons.Filled.Forum, Icons.Outlined.Forum, "nav_tab_community")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CloudPetApp()
            }
        }
    }
}

@Composable
fun CloudPetApp(viewModel: PetViewModel = viewModel()) {
    var selectedTab by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var isARModeOpen by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val activePet by viewModel.activePet.collectAsStateWithLifecycle()
    val allPets by viewModel.allPets.collectAsStateWithLifecycle()
    val growthRecords by viewModel.growthRecords.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val animationMode by viewModel.animationMode.collectAsStateWithLifecycle()
    val isSleeping by viewModel.isSleeping.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val dailyTasks by viewModel.dailyTasks.collectAsStateWithLifecycle()
    val isSoundMuted by viewModel.isSoundMuted.collectAsStateWithLifecycle()
    val isPlayingSound by viewModel.isPlayingSound.collectAsStateWithLifecycle()
    val currentSoundLabel by viewModel.currentSoundLabel.collectAsStateWithLifecycle()
    val arenaRating by viewModel.arenaRating.collectAsStateWithLifecycle()

    // Show snackbar when feedbackMessage updates
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = if (isSleeping && selectedTab == NavTab.HOME) Color(0xFF1E233E) else Color(0xFFF3EDF7),
                contentColor = CoralPrimary
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralPrimary,
                            selectedTextColor = CoralPrimary,
                            indicatorColor = Color(0xFFE8DEF8),
                            unselectedIconColor = if (isSleeping && selectedTab == NavTab.HOME) Color(0xFF90A4AE) else Color(0xFF49454F),
                            unselectedTextColor = if (isSleeping && selectedTab == NavTab.HOME) Color(0xFF90A4AE) else Color(0xFF49454F)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition",
            modifier = Modifier.padding(innerPadding)
        ) { targetTab ->
            when (targetTab) {
                NavTab.HOME -> {
                    HomeScreen(
                        pet = activePet,
                        allPets = allPets,
                        animationMode = animationMode,
                        isSleeping = isSleeping,
                        foodList = viewModel.foodList,
                        feedbackMessage = feedbackMessage,
                        dailyTasks = dailyTasks,
                        isSoundMuted = isSoundMuted,
                        isPlayingSound = isPlayingSound,
                        currentSoundLabel = currentSoundLabel,
                        onToggleSoundMute = { viewModel.toggleSoundMute() },
                        onCuddle = { viewModel.cuddlePet() },
                        onFeed = { food -> viewModel.feedPet(food) },
                        onBath = { viewModel.bathPet() },
                        onPlay = { viewModel.playPet() },
                        onToggleSleep = { viewModel.toggleSleep() },
                        onSwitchPet = { petId -> viewModel.switchPet(petId) },
                        onNavigateToCustomizer = { selectedTab = NavTab.CUSTOMIZE },
                        onNavigateToShop = { selectedTab = NavTab.SHOP },
                        onOpenAR = { isARModeOpen = true },
                        onClaimDailyTaskReward = { taskId -> viewModel.claimDailyTaskReward(taskId) },
                        onClaimMilestoneReward = { tier -> viewModel.claimMilestoneReward(tier) },
                        onCheckIn = { viewModel.checkInToday() },
                        onNavigateToGrowth = { selectedTab = NavTab.GROWTH },
                        onNavigateToCommunity = { selectedTab = NavTab.COMMUNITY }
                    )
                }
                NavTab.SHOP -> {
                    activePet?.let { pet ->
                        ShopScreen(
                            pet = pet,
                            onPurchaseOutfit = { outfit, autoEquip ->
                                viewModel.purchaseShopOutfit(outfit, autoEquip)
                            },
                            onEquipOutfit = { outfit ->
                                viewModel.equipOutfit(outfit)
                            },
                            onClaimShopBonus = {
                                viewModel.claimShopBonus()
                            },
                            onNavigateToTasks = {
                                selectedTab = NavTab.HOME
                            }
                        )
                    }
                }
                NavTab.GROWTH -> {
                    GrowthScreen(
                        pet = activePet,
                        records = growthRecords,
                        onAddDiary = { title, content, mood ->
                            viewModel.addGrowthDiary(title, content, mood)
                        }
                    )
                }
                NavTab.CUSTOMIZE -> {
                    CustomizerScreen(
                        currentPet = activePet,
                        onSavePet = { existingId, name, species, isCustomSpecies, baseBodyType, primaryColor, secondaryColor, earStyle, eyeStyle, tailStyle, accessory, personality, catchphrase ->
                            viewModel.saveCustomPet(
                                existingId = existingId,
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
                            selectedTab = NavTab.HOME
                        },
                        onPurchaseOutfit = { outfit ->
                            viewModel.purchaseOutfit(outfit)
                        },
                        onNavigateToShop = {
                            selectedTab = NavTab.SHOP
                        }
                    )
                }
                NavTab.COMMUNITY -> {
                    CommunityScreen(
                        currentPet = activePet,
                        posts = communityPosts,
                        arenaRating = arenaRating,
                        onToggleLike = { postId, liked ->
                            viewModel.togglePostLike(postId, liked)
                        },
                        onAddComment = { postId, content ->
                            viewModel.addComment(postId, content)
                        },
                        onPublishPost = { content, tag ->
                            viewModel.publishPost(content, tag)
                        },
                        onRecordBattleVictory = { coins, exp, summary ->
                            viewModel.recordBattleVictory(coins, exp, summary)
                        },
                        getCommentsFlow = { postId ->
                            viewModel.getCommentsForPost(postId)
                        }
                    )
                }
            }
        }

        // Fullscreen Real-World AR Interactive Mode
        if (isARModeOpen) {
            ARScreen(
                pet = activePet,
                foodList = viewModel.foodList,
                onFeedPet = { food -> viewModel.feedPet(food) },
                onPlayPet = { viewModel.playPet() },
                onCuddlePet = { viewModel.cuddlePet() },
                onCloseAR = { isARModeOpen = false }
            )
        }
    }
}
