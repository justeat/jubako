package com.justeat.jubako

import com.justeat.jubako.screen.JubakoScreen.clickDoButton
import com.justeat.jubako.screen.JubakoScreen.clickMiButton
import com.justeat.jubako.screen.JubakoScreen.clickReButton
import com.justeat.jubako.screen.JubakoScreen.clickShowAllButton
import com.justeat.jubako.steps.JubakoSteps.amLookingAtRobustnessTestJubakoActivity
import org.junit.Ignore
import org.junit.Test
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Run to test robustness, uncomment [Ignore] to use
 */
//@Ignore
internal class JubakoRobustnessTests {
    @Test
    fun thrashContentAssemblerReloading() {
        // Given I
        amLookingAtRobustnessTestJubakoActivity()

        // And I
        val buttons = listOf(
                { clickDoButton() },
                { clickReButton() },
                { clickMiButton() },
                { clickShowAllButton() }
        )

        val random = Random(System.currentTimeMillis())

        for (x in 0..1000) {
            buttons[random.nextInt(0 until buttons.size)]()
        }
    }
}
