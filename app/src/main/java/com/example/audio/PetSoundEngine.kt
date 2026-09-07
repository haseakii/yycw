package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.example.data.model.FoodItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Context-aware Sound Engine for virtual pets.
 * Procedurally generates realistic, organic sound effects including:
 * - Purring with respiratory amplitude modulation and happy micro-chirps
 * - Rhythmic munching and crunching during feeding with swallowing gulps
 * - Water splashes and soap bubble pops during bathing
 * - Bouncy squeaky toy sounds during playtime
 * - Soft lullabies for sleep and joyful morning chirps for wakeup
 * - Metallic coin jingling and level-up celebratory fanfares
 *
 * Runs 100% offline, zero external asset downloads needed, low latency.
 */
class PetSoundEngine private constructor() {

    companion object {
        private const val TAG = "PetSoundEngine"
        private const val SAMPLE_RATE = 22050 // High fidelity yet lightweight for procedural synth

        @Volatile
        private var INSTANCE: PetSoundEngine? = null

        fun getInstance(): PetSoundEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PetSoundEngine().also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val audioMutex = Mutex()

    // Sound settings & reactive state
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _currentSoundLabel = MutableStateFlow<String?>(null)
    val currentSoundLabel: StateFlow<String?> = _currentSoundLabel.asStateFlow()

    // Consecutive petting tracker to trigger escalating happy purrs & chirps
    private var consecutivePetCount = 0
    private var lastPetTime = 0L

    // Active AudioTrack pool to prevent resource leaks
    private val activeTracks = ConcurrentLinkedQueue<AudioTrack>()

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
    }

    fun setVolume(vol: Float) {
        _volume.value = vol.coerceIn(0f, 1f)
    }

    /**
     * Play petting sound:
     * - Rhythmic respiratory purr (low-frequency resonance with breathing cycle).
     * - If stroked repeatedly or high happiness, adds a sweet melodious trill/chirp ("mrrp~").
     * - Adapts according to pet species (cat, dog, bunny, dragon, etc.).
     */
    fun playPetting(species: String, happiness: Int) {
        if (_isMuted.value) return

        val now = System.currentTimeMillis()
        if (now - lastPetTime < 2500) {
            consecutivePetCount++
        } else {
            consecutivePetCount = 1
        }
        lastPetTime = now

        val streak = consecutivePetCount
        scope.launch {
            val label = when {
                species.contains("狗") || species.contains("犬") -> "🐶 欢喜摇尾轻呼~"
                species.contains("兔") -> "🐰 软糯贴贴轻颤~"
                species.contains("龙") -> "🐲 暖暖龙焰呼噜~"
                streak >= 3 -> "❤️ 呼噜呼噜~ 娇萌叫声!"
                else -> "🐱 享受呼噜呼噜中~"
            }
            emitSoundStatus(label, 1400)

            val pcmData = generatePettingWaveform(species, happiness, streak)
            playPcm(pcmData)
        }
    }

    /**
     * Play feeding sound:
     * - Multi-bite crunch and munch (nom nom nom).
     * - Swallowing gulp.
     * - Delighted happy chime or satisfied sigh.
     * - Customized based on food item (crunchy snacks, fish, drinks, premium can).
     */
    fun playFeeding(food: FoodItem, hunger: Int, species: String) {
        if (_isMuted.value) return

        scope.launch {
            val label = "🍖 嗷呜大口咀嚼中..."
            emitSoundStatus(label, 1500)

            val pcmData = generateFeedingWaveform(food, hunger, species)
            playPcm(pcmData)
        }
    }

    /**
     * Play bathing sound:
     * - Water sloshing and multiple bubble pops ("bloop bloop splash").
     * - Fresh squeaky clean chime.
     */
    fun playBathing() {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("🛁 欢快水花与气泡~", 1600)
            val pcmData = generateBathingWaveform()
            playPcm(pcmData)
        }
    }

    /**
     * Play playing/entertainment sound:
     * - Squeaky toy boing and bouncy upbeat arpeggios.
     */
    fun playPlaying(species: String) {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("🧸 欢声雀跃跳跳~", 1300)
            val pcmData = generatePlayingWaveform(species)
            playPcm(pcmData)
        }
    }

    /**
     * Play sleeping/waking sound:
     * - Entering sleep: gentle soothing music box lullaby tones.
     * - Waking up: bright ascending morning sunshine chime.
     */
    fun playSleep(isFallingAsleep: Boolean) {
        if (_isMuted.value) return

        scope.launch {
            if (isFallingAsleep) {
                emitSoundStatus("💤 摇篮曲与轻柔安睡~", 1800)
                val pcmData = generateLullabyWaveform()
                playPcm(pcmData)
            } else {
                emitSoundStatus("✨ 精神抖擞伸懒腰~", 1200)
                val pcmData = generateWakeUpWaveform()
                playPcm(pcmData)
            }
        }
    }

    /**
     * Play coin reward sound:
     * - Clinking gold coins falling into pouch.
     */
    fun playCoinReward(coins: Int) {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("🪙 金币入账 +$coins", 1100)
            val pcmData = generateCoinWaveform(coins)
            playPcm(pcmData)
        }
    }

    /**
     * Play celebratory level up fanfare.
     */
    fun playLevelUp() {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("🎉 等级提升大喜悦!", 1800)
            val pcmData = generateLevelUpWaveform()
            playPcm(pcmData)
        }
    }

    /**
     * Play battle attack whoosh / claw slash.
     */
    fun playBattleAttack() {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("⚔️ 萌宠出招！", 600)
            val pcmData = generateBattleAttackWaveform()
            playPcm(pcmData)
        }
    }

    /**
     * Play battle physical impact thud.
     */
    fun playBattleHit() {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("💥 击中命中！", 500)
            val pcmData = generateBattleHitWaveform()
            playPcm(pcmData)
        }
    }

    /**
     * Play battle critical hit sparkle impact.
     */
    fun playBattleCritical() {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("🔥 萌力暴击！！", 900)
            val pcmData = generateBattleCriticalWaveform()
            playPcm(pcmData)
        }
    }

    /**
     * Play protective shield bubble invocation.
     */
    fun playBattleShield() {
        if (_isMuted.value) return

        scope.launch {
            emitSoundStatus("🛡️ 治愈守护盾！", 800)
            val pcmData = generateBattleShieldWaveform()
            playPcm(pcmData)
        }
    }

    /**
     * Play subtle UI feedback tap.
     */
    fun playClick() {
        if (_isMuted.value) return

        scope.launch {
            val pcmData = generateClickWaveform()
            playPcm(pcmData)
        }
    }

    private suspend fun emitSoundStatus(label: String, durationMs: Long) {
        _isPlayingAudio.value = true
        _currentSoundLabel.value = label
        scope.launch {
            delay(durationMs)
            if (_currentSoundLabel.value == label) {
                _isPlayingAudio.value = false
                _currentSoundLabel.value = null
            }
        }
    }

    // =========================================================================
    // Waveform Synthesizers (16-bit PCM Mono @ 22050Hz)
    // =========================================================================

    /**
     * Synthesizes authentic purring audio:
     * 1. Purr motor: 26-38 Hz fundamental with harmonics (56Hz, 84Hz, 112Hz).
     * 2. Rhythmic respiration envelope: 1.8 Hz cycle (inhalation/exhalation).
     * 3. Subtle organic pink-noise friction.
     * 4. Melodious sweet chirp overlay if consecutive petting >= 2 or high happiness.
     */
    fun generatePettingWaveform(species: String, happiness: Int, streak: Int): ShortArray {
        val durationSec = if (streak >= 3) 1.5f else 1.2f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val baseFreq = when {
            species.contains("狗") || species.contains("犬") -> 45.0 // Deeper rumble for dogs
            species.contains("龙") -> 32.0 // Mythical low hum
            species.contains("兔") -> 55.0 // Higher soft purr
            else -> 28.0 // Classic feline purr frequency
        }

        // Respiration cycle frequency (~1.8 breaths per second)
        val breathFreq = 1.8

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            // Breathing envelope: creates rhythmic rise & fall
            val breathEnvelope = (0.5 + 0.5 * sin(2.0 * PI * breathFreq * t)).pow(1.5)

            // Purr harmonics: Fundamental + 2nd, 3rd, 4th harmonics
            val wave = 0.45 * sin(2.0 * PI * baseFreq * t) +
                    0.25 * sin(2.0 * PI * (baseFreq * 2.0) * t + 0.3) +
                    0.15 * sin(2.0 * PI * (baseFreq * 3.0) * t + 0.7) +
                    0.08 * sin(2.0 * PI * (baseFreq * 4.0) * t + 1.1)

            // Subtle furry texture (filtered low-amplitude random flutter)
            val flutter = (Random.nextDouble(-1.0, 1.0) * 0.08)

            // Overall fade in/fade out to prevent audio clicks
            val attack = (t / 0.1).coerceIn(0.0, 1.0)
            val release = ((durationSec - t) / 0.2).coerceIn(0.0, 1.0)
            val masterEnvelope = attack * release

            var sampleVal = (wave + flutter) * breathEnvelope * masterEnvelope * 0.75

            // If stroked repeatedly or happiness is high, append an affectionate high trill chirp ("mrrp~")
            if ((streak >= 2 || happiness >= 70) && t > 0.45 && t < 0.95) {
                val chirpT = t - 0.45
                val chirpDuration = 0.5
                val chirpEnv = sin(PI * (chirpT / chirpDuration)).coerceAtLeast(0.0).pow(1.8)

                // Frequency glides upwards from 520Hz to 780Hz with vibrato
                val chirpFreq = 520.0 + (chirpT / chirpDuration) * 260.0 + 15.0 * sin(2.0 * PI * 18.0 * chirpT)
                val chirpWave = sin(2.0 * PI * chirpFreq * chirpT) * 0.35 +
                        sin(2.0 * PI * (chirpFreq * 2.0) * chirpT) * 0.1

                sampleVal += chirpWave * chirpEnv
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes munching and feeding audio:
     * - 3-4 distinct crunchy chewing bursts.
     * - Gulp / swallow effect at the end.
     * - Sweet little chime indicating satisfaction.
     */
    fun generateFeedingWaveform(food: FoodItem, hunger: Int, species: String): ShortArray {
        val durationSec = 1.4f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        // 3 chewing bites at 0.05s, 0.35s, 0.65s
        val biteTimes = doubleArrayOf(0.05, 0.35, 0.65)
        val isCrispy = food.name.contains("饼") || food.name.contains("干") || food.name.contains("鱼")
        val isDrink = food.name.contains("奶") || food.name.contains("水")

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            // Chewing bites
            for (biteTime in biteTimes) {
                val dt = t - biteTime
                if (dt in 0.0..0.22) {
                    val biteEnv = exp(-dt * 18.0) // fast decaying hit

                    if (isDrink) {
                        // Slurp / liquid bubble frequency modulation
                        val bubbleFreq = 420.0 - dt * 600.0
                        sampleVal += sin(2.0 * PI * bubbleFreq * dt) * biteEnv * 0.45
                    } else {
                        // Crunchy munch: burst of shaped noise + resonant wooden/food chew tone
                        val crunchNoise = Random.nextDouble(-1.0, 1.0) * (if (isCrispy) 0.55 else 0.35)
                        val bodyFreq = 240.0 + sin(dt * 80.0) * 40.0
                        val bodyTone = sin(2.0 * PI * bodyFreq * dt) * 0.4
                        sampleVal += (crunchNoise + bodyTone) * biteEnv
                    }
                }
            }

            // Gulp sound at 0.95s (pitch drops swiftly like swallowing)
            val gulpDt = t - 0.92
            if (gulpDt in 0.0..0.18) {
                val gulpEnv = sin(PI * (gulpDt / 0.18)).coerceAtLeast(0.0)
                val gulpFreq = 380.0 - (gulpDt / 0.18) * 180.0
                sampleVal += sin(2.0 * PI * gulpFreq * gulpDt) * gulpEnv * 0.5
            }

            // Satisfied happy bell sparkle at 1.1s
            val chimeDt = t - 1.10
            if (chimeDt in 0.0..0.30) {
                val chimeEnv = exp(-chimeDt * 10.0)
                // Cheerful bell tones: 880Hz (A5) & 1320Hz (E6)
                val chimeTone = (sin(2.0 * PI * 880.0 * chimeDt) * 0.25 +
                        sin(2.0 * PI * 1320.0 * chimeDt) * 0.15) * chimeEnv
                sampleVal += chimeTone
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes bathing sound:
     * - Bubble bloops (sinusoidal downward frequency sweep).
     * - Gentle water sloshing.
     * - Clean sparkle ping.
     */
    fun generateBathingWaveform(): ShortArray {
        val durationSec = 1.4f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val bubbleTimes = doubleArrayOf(0.08, 0.28, 0.50, 0.72, 0.90)
        val bubblePitches = doubleArrayOf(720.0, 840.0, 650.0, 910.0, 780.0)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            // Water slosh background (soft lowpass noise)
            val sloshEnvelope = sin(PI * (t / durationSec)).coerceAtLeast(0.0)
            val sloshNoise = (Random.nextDouble(-1.0, 1.0) * 0.12) * sloshEnvelope
            sampleVal += sloshNoise

            // Bubble pops
            for (idx in bubbleTimes.indices) {
                val bTime = bubbleTimes[idx]
                val dt = t - bTime
                if (dt in 0.0..0.12) {
                    val bEnv = sin(PI * (dt / 0.12)).pow(2.0)
                    // Downward frequency sweep gives realistic "bloop" bubble sound
                    val baseP = bubblePitches[idx]
                    val currentFreq = baseP - (dt / 0.12) * (baseP * 0.45)
                    sampleVal += sin(2.0 * PI * currentFreq * dt) * bEnv * 0.48
                }
            }

            // Fresh squeak chime at 1.1s
            val squeakDt = t - 1.10
            if (squeakDt in 0.0..0.25) {
                val sqEnv = exp(-squeakDt * 14.0)
                val sqFreq = 1200.0 + (squeakDt / 0.25) * 300.0
                sampleVal += sin(2.0 * PI * sqFreq * squeakDt) * sqEnv * 0.25
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes playtime bouncy joy sound:
     * - Squeak boing with frequency vibration.
     * - Cheerful ascending melody.
     */
    fun generatePlayingWaveform(species: String): ShortArray {
        val durationSec = 1.1f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            // Squeak toy boing at 0.05s
            val boingDt = t - 0.05
            if (boingDt in 0.0..0.35) {
                val boingEnv = exp(-boingDt * 8.0)
                val boingFreq = 380.0 + (boingDt / 0.35) * 450.0 + 30.0 * sin(2.0 * PI * 22.0 * boingDt)
                sampleVal += sin(2.0 * PI * boingFreq * boingDt) * boingEnv * 0.5
            }

            // Cheerful melody notes at 0.42s, 0.62s, 0.82s (C5, E5, G5)
            val notes = doubleArrayOf(523.25, 659.25, 783.99)
            val noteTimes = doubleArrayOf(0.42, 0.62, 0.82)
            for (idx in notes.indices) {
                val nDt = t - noteTimes[idx]
                if (nDt in 0.0..0.22) {
                    val nEnv = exp(-nDt * 12.0)
                    val freq = notes[idx]
                    sampleVal += (sin(2.0 * PI * freq * nDt) * 0.32 +
                            sin(2.0 * PI * freq * 2.0 * nDt) * 0.12) * nEnv
                }
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes lullaby music box tones for sleep.
     */
    fun generateLullabyWaveform(): ShortArray {
        val durationSec = 1.6f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        // Music box notes: E5, C5, G4, C5 gentle descending cadence
        val notes = doubleArrayOf(659.25, 523.25, 392.00, 523.25)
        val noteTimes = doubleArrayOf(0.1, 0.45, 0.85, 1.2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            for (idx in notes.indices) {
                val dt = t - noteTimes[idx]
                if (dt in 0.0..0.38) {
                    val env = exp(-dt * 6.5)
                    val freq = notes[idx]
                    // Clean music box timbre (fundamental + soft overtone)
                    sampleVal += (sin(2.0 * PI * freq * dt) * 0.35 +
                            sin(2.0 * PI * (freq * 2.0) * dt) * 0.12) * env
                }
            }

            // Soft warm breath rumble underneath
            val breath = sin(2.0 * PI * 40.0 * t) * (0.5 + 0.5 * sin(2.0 * PI * 0.7 * t)) * 0.15
            sampleVal += breath

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes waking up cheerful chirp.
     */
    fun generateWakeUpWaveform(): ShortArray {
        val durationSec = 1.0f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        // Ascending sunrise notes: C5 -> E5 -> G5 -> C6
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteTimes = doubleArrayOf(0.05, 0.22, 0.39, 0.56)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            for (idx in notes.indices) {
                val dt = t - noteTimes[idx]
                if (dt in 0.0..0.35) {
                    val env = exp(-dt * 9.0)
                    val freq = notes[idx]
                    sampleVal += (sin(2.0 * PI * freq * dt) * 0.38 +
                            sin(2.0 * PI * (freq * 2.0) * dt) * 0.14) * env
                }
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes sparkling metallic coin drop and clink.
     */
    fun generateCoinWaveform(coins: Int): ShortArray {
        val durationSec = 0.9f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        // Rapid coin clinks
        val clinkTimes = doubleArrayOf(0.04, 0.18, 0.34, 0.50)
        val coinFreqs = doubleArrayOf(2450.0, 3120.0, 2780.0, 3450.0)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            for (idx in clinkTimes.indices) {
                val dt = t - clinkTimes[idx]
                if (dt in 0.0..0.25) {
                    val env = exp(-dt * 22.0)
                    val baseF = coinFreqs[idx]
                    // Inharmonic high frequencies sound authentically like brass/silver coins
                    val coinTone = sin(2.0 * PI * baseF * dt) * 0.35 +
                            sin(2.0 * PI * (baseF * 1.58) * dt) * 0.20 +
                            sin(2.0 * PI * (baseF * 2.32) * dt) * 0.12
                    sampleVal += coinTone * env
                }
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes celebratory level up fanfare.
     */
    fun generateLevelUpWaveform(): ShortArray {
        val durationSec = 1.5f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
        val noteTimes = doubleArrayOf(0.05, 0.20, 0.35, 0.52, 0.72)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sampleVal = 0.0

            for (idx in notes.indices) {
                val dt = t - noteTimes[idx]
                if (dt in 0.0..0.60) {
                    val env = exp(-dt * 5.0)
                    val freq = notes[idx]
                    sampleVal += (sin(2.0 * PI * freq * dt) * 0.35 +
                            sin(2.0 * PI * freq * 2.0 * dt) * 0.15 +
                            sin(2.0 * PI * freq * 3.0 * dt) * 0.08) * env
                }
            }

            samples[i] = (sampleVal.coerceIn(-1.0, 1.0) * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes a subtle tactile click sound.
     */
    fun generateClickWaveform(): ShortArray {
        val numSamples = (SAMPLE_RATE * 0.04f).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 80.0)
            val wave = sin(2.0 * PI * 850.0 * t) * env * 0.4
            samples[i] = (wave * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes swift attack whoosh and claw slash.
     */
    fun generateBattleAttackWaveform(): ShortArray {
        val duration = 0.22 // seconds
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / duration
            // Pitch sweeps rapidly from 900Hz down to 240Hz
            val freq = 900.0 - 660.0 * progress
            val env = sin(PI * progress)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.25 * env
            val wave = (sin(2.0 * PI * freq * t) * 0.7 + noise) * env
            samples[i] = (wave * 32767.0 * _volume.value * 0.85).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes punchy physical hit impact thud.
     */
    fun generateBattleHitWaveform(): ShortArray {
        val duration = 0.18
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 35.0)
            val freq = 220.0 * exp(-t * 20.0)
            val wave = (sin(2.0 * PI * freq * t) * 0.8 + (Random.nextDouble() * 2.0 - 1.0) * 0.3) * env
            samples[i] = (wave * 32767.0 * _volume.value * 0.9).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes critical hit impact with explosive bell chime.
     */
    fun generateBattleCriticalWaveform(): ShortArray {
        val duration = 0.38
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 12.0)
            val impact = sin(2.0 * PI * 180.0 * t) * exp(-t * 30.0) * 0.6
            val chime = (sin(2.0 * PI * 1250.0 * t) + 0.5 * sin(2.0 * PI * 2500.0 * t)) * env * 0.5
            val wave = impact + chime
            samples[i] = (wave * 32767.0 * _volume.value).toInt().toShort()
        }

        return samples
    }

    /**
     * Synthesizes shimmering protective energy barrier aura.
     */
    fun generateBattleShieldWaveform(): ShortArray {
        val duration = 0.32
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = sin(PI * (t / duration))
            // Shimmering crystalline triad: 660Hz, 880Hz, 1320Hz
            val wave = (sin(2.0 * PI * 660.0 * t) * 0.4 +
                    sin(2.0 * PI * 880.0 * t) * 0.35 +
                    sin(2.0 * PI * 1320.0 * t) * 0.25) * env
            samples[i] = (wave * 32767.0 * _volume.value * 0.8).toInt().toShort()
        }

        return samples
    }

    /**
     * Plays raw PCM data safely via Android AudioTrack.
     */
    private suspend fun playPcm(pcmData: ShortArray) = audioMutex.withLock {
        try {
            val bufferSize = pcmData.size * 2

            val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STATIC
                )
            }

            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                activeTracks.add(audioTrack)
                audioTrack.write(pcmData, 0, pcmData.size)
                audioTrack.play()

                // Clean up track when sound ends
                val playDurationMs = ((pcmData.size.toDouble() / SAMPLE_RATE) * 1000.0).toLong() + 100L
                scope.launch {
                    delay(playDurationMs)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error stopping audio track", e)
                    } finally {
                        activeTracks.remove(audioTrack)
                    }
                }
            } else {
                audioTrack.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio waveform", e)
        }
    }

    /**
     * Release all active tracks when app is stopped or destroyed.
     */
    fun release() {
        while (activeTracks.isNotEmpty()) {
            val track = activeTracks.poll()
            try {
                track?.stop()
                track?.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing audio track", e)
            }
        }
    }
}
