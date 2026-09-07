package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralPrimary

/**
 * Visual Equalizer Bars that dance when pet sound effect is active.
 */
@Composable
fun AudioEqualizerWave(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    waveColor: Color = CoralPrimary
) {
    val transition = rememberInfiniteTransition(label = "sound_wave")

    val bar1Height by transition.animateFloat(
        initialValue = 4f,
        targetValue = if (isPlaying) 16f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2Height by transition.animateFloat(
        initialValue = 8f,
        targetValue = if (isPlaying) 20f else 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, delayMillis = 60, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3Height by transition.animateFloat(
        initialValue = 5f,
        targetValue = if (isPlaying) 14f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(340, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar1Height.dp)
                .clip(CircleShape)
                .background(waveColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar2Height.dp)
                .clip(CircleShape)
                .background(waveColor)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(bar3Height.dp)
                .clip(CircleShape)
                .background(waveColor)
        )
    }
}

/**
 * Compact Sound Control Capsule with Soundwave Feedback & Mute Toggle.
 */
@Composable
fun PetSoundControlBadge(
    isMuted: Boolean,
    isPlaying: Boolean,
    soundLabel: String?,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isMuted) Color(0xFFF5F5F5) else Color(0xFFF3EDF7),
        border = BorderStroke(1.dp, if (isPlaying) CoralPrimary.copy(alpha = 0.6f) else Color(0xFFE6E0E9)),
        shadowElevation = if (isPlaying) 3.dp else 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onToggleMute() }
            .testTag("sound_engine_toggle_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "静音模式" else "音效已开启",
                tint = if (isMuted) Color(0xFF79747E) else CoralPrimary,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (!isMuted && isPlaying) {
                AudioEqualizerWave(isPlaying = true, waveColor = CoralPrimary)
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = when {
                    isMuted -> "音效: 关"
                    isPlaying && !soundLabel.isNullOrBlank() -> soundLabel
                    else -> "音效: 开"
                },
                fontSize = 11.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                color = if (isMuted) Color(0xFF79747E) else Color(0xFF21005D),
                maxLines = 1
            )
        }
    }
}
