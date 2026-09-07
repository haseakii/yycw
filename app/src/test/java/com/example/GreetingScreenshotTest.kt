package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.PetEntity
import com.example.ui.components.PetModelView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val samplePet = PetEntity(
      id = 1L,
      name = "泡芙",
      species = "软萌英短猫",
      baseBodyType = "CAT",
      primaryColor = 0xFFFFAB91,
      secondaryColor = 0xFFFFFFFF,
      earStyle = "POINTY",
      eyeStyle = "SPARKLE",
      tailStyle = "FLUFFY",
      accessory = "BELL_COLLAR",
      level = 3
    )
    composeTestRule.setContent {
      MyApplicationTheme {
        PetModelView(pet = samplePet)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
