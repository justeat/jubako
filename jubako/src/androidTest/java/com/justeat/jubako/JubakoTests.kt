package com.justeat.jubako

import com.justeat.jubako.steps.JubakoSteps.amLookingAtTestJubakoActivity
import com.justeat.jubako.steps.JubakoSteps.shouldSeeRayTextChangesToPeekaBoo
import com.justeat.jubako.steps.JubakoSteps.tapTheRowWithRayText
import com.justeat.jubako.steps.JubakoSteps.tapTheShowPeekaBooButton
import org.junit.Test

internal class JubakoTests {

    @Test
    fun rowUpdatesWhenClicked() {
        // Given I
        amLookingAtTestJubakoActivity()

        // When I
        tapTheRowWithRayText()

        // Then I
        shouldSeeRayTextChangesToPeekaBoo()
    }

    @Test
    fun rowUpdatesWhenOutsideButtonIsClicked() {
        // Given I
        amLookingAtTestJubakoActivity()

        // When I
        tapTheShowPeekaBooButton()

        // Then I
        shouldSeeRayTextChangesToPeekaBoo()
    }
}