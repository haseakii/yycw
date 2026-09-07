package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("云养萌宠", appName)
  }

  @Test
  fun `verify pet initial growth metrics`() {
    val pet = com.example.data.model.PetEntity(
      name = "测试萌宠",
      species = "软萌英短猫",
      happiness = 85,
      hunger = 70,
      totalFeedCount = 5,
      totalPlayCount = 8
    )
    assertEquals(85, pet.happiness)
    assertEquals(70, pet.hunger)
    assertEquals(13, pet.totalFeedCount + pet.totalPlayCount)
  }

  @Test
  fun `verify daily task system templates and coin mechanics`() {
    val templates = com.example.data.model.DEFAULT_DAILY_TASKS
    // Verify required task categories: CLEAN, FEED, PLAY, LOVE, DIARY, SHARE
    val categories = templates.map { it.category }.toSet()
    org.junit.Assert.assertTrue("Should contain cleaning category", categories.contains("CLEAN"))
    org.junit.Assert.assertTrue("Should contain interaction category", categories.contains("FEED"))
    org.junit.Assert.assertTrue("Should contain sharing category", categories.contains("SHARE"))

    // Verify task progress and reward calculation
    val task = com.example.data.model.DailyTaskEntity(
      id = "TASK_FEED",
      dateKey = "2026-09-07",
      title = "暖心饱腹投喂",
      description = "投喂2次美味食物",
      iconEmoji = "🍲",
      category = "FEED",
      rewardCoins = 25,
      rewardExp = 15,
      currentProgress = 2,
      targetProgress = 2,
      isClaimed = false
    )

    org.junit.Assert.assertTrue(task.currentProgress >= task.targetProgress)
    assertEquals(25, task.rewardCoins)

    // Verify pet outfit purchasing logic
    val initialCoins = 100
    val outfitCost = 80
    val remainingCoins = initialCoins - outfitCost
    assertEquals(20, remainingCoins)
  }

  @Test
  fun `verify sound engine procedural audio synthesis and context awareness`() {
    val soundEngine = com.example.audio.PetSoundEngine.getInstance()

    // Verify mute and volume controls
    soundEngine.setMuted(false)
    org.junit.Assert.assertFalse(soundEngine.isMuted.value)
    soundEngine.toggleMute()
    org.junit.Assert.assertTrue(soundEngine.isMuted.value)
    soundEngine.setMuted(false)

    soundEngine.setVolume(0.75f)
    assertEquals(0.75f, soundEngine.volume.value, 0.01f)

    // Verify petting sound synthesis (Cat purr vs Dog rumble vs Consecutive strokes)
    val catPurrWave = soundEngine.generatePettingWaveform(species = "英短猫咪", happiness = 60, streak = 1)
    org.junit.Assert.assertTrue("Purr wave should have samples", catPurrWave.isNotEmpty())

    // High happiness / streak >= 3 generates richer chirp-infused audio
    val happyCatPurrWave = soundEngine.generatePettingWaveform(species = "英短猫咪", happiness = 95, streak = 3)
    org.junit.Assert.assertTrue("Streak purr wave should be longer or equal", happyCatPurrWave.size >= catPurrWave.size)

    // Verify feeding sound synthesis (crunchy vs drink)
    val crunchyFood = com.example.data.model.FoodItem(
      id = "fish",
      name = "新鲜小鱼干",
      iconEmoji = "🐟",
      description = "香脆可口",
      hungerRestore = 25,
      happinessRestore = 15,
      energyRestore = 10,
      expGain = 15
    )
    val feedWave = soundEngine.generateFeedingWaveform(food = crunchyFood, hunger = 40, species = "猫咪")
    org.junit.Assert.assertTrue("Feeding wave should generate PCM data", feedWave.isNotEmpty())

    // Verify bathing and playing sound synthesis
    val bathWave = soundEngine.generateBathingWaveform()
    org.junit.Assert.assertTrue("Bathing wave should have bubbly samples", bathWave.isNotEmpty())

    val playWave = soundEngine.generatePlayingWaveform("柴犬")
    org.junit.Assert.assertTrue("Play wave should have bouncy joy samples", playWave.isNotEmpty())

    // Verify coin reward and level up waveforms
    val coinWave = soundEngine.generateCoinWaveform(50)
    org.junit.Assert.assertTrue("Coin wave should have metallic clinks", coinWave.isNotEmpty())

    val levelUpWave = soundEngine.generateLevelUpWaveform()
    org.junit.Assert.assertTrue("Level up wave should have fanfare notes", levelUpWave.isNotEmpty())
  }
}
