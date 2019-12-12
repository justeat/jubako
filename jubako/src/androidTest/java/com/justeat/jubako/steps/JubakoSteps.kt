package com.justeat.jubako.steps

import android.content.Intent
import androidx.test.InstrumentationRegistry
import com.justeat.jubako.platform.TestJubakoActivity
import com.justeat.jubako.platform.RobustnessTestJubakoActivity
import com.justeat.jubako.screen.JubakoScreen.checkRowContainsPeekaBoo
import com.justeat.jubako.screen.JubakoScreen.clickOnPeekaBooButton
import com.justeat.jubako.screen.JubakoScreen.clickOnRay
import com.justeat.jubako.screen.JubakoScreen.scrollToTop
import com.justeat.jubako.util.IntentLauncher

object JubakoSteps {
    fun amLookingAtTestJubakoActivity() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = Intent(
            instrumentation.targetContext,
            TestJubakoActivity::class.java
        )

        IntentLauncher.launchActivityFromIntent(intent)
    }

    fun amLookingAtRobustnessTestJubakoActivity() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = Intent(
            instrumentation.targetContext,
            RobustnessTestJubakoActivity::class.java
        )

        IntentLauncher.launchActivityFromIntent(intent)
    }

    fun tapTheRowWithRayText() {
        clickOnRay()
    }

    fun shouldSeeRayTextChangesToPeekaBoo() {
        checkRowContainsPeekaBoo()
    }

    fun tapTheShowPeekaBooButton() {
        scrollToTop()
        clickOnPeekaBooButton()
    }
}