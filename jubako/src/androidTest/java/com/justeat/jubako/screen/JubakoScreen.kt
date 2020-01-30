package com.justeat.jubako.screen

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.justeat.jubako.recyclerviews.JubakoViewHolder
import com.justeat.jubako.test.R
import com.justeat.jubako.util.RecyclerViewExt.withRecyclerView
import org.hamcrest.Matchers

object JubakoScreen {
    fun clickOnRay() {
        onView(withId(R.id.recyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
    }

    fun checkRowContainsPeekaBoo() {
        onView(withRecyclerView(R.id.recyclerView).atPositionOnView(1, R.id.itemText))
            .check(matches(Matchers.allOf(withText("Peek-a-Boo!"))))
    }

    fun clickOnPeekaBooButton() {
        onView(withId(R.id.showPeekaBooButton))
            .perform(click())
    }

    fun scrollToTop() {
        onView(withId(R.id.recyclerView))
            .perform(RecyclerViewActions.scrollToPosition<com.justeat.jubako.recyclerviews.JubakoViewHolder<Any>>(0))
    }

    fun clickDoButton() {
        onView(withId(R.id.showDo))
            .perform(click())
    }

    fun clickReButton() {
        onView(withId(R.id.showRe))
            .perform(click())
    }

    fun clickMiButton() {
        onView(withId(R.id.showMi))
            .perform(click())
    }

    fun clickShowAllButton() {
        onView(withId(R.id.showAll))
            .perform(click())
    }
}
