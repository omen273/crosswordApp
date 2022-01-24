package com.omen273.crossLingo

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Test

class CheckSettingsTest: SolveCrossword() {

    @Test
    fun checkSettingsTest() {
        waitForCondition("Sound option checking", { readSoundFromConfig() })
        Espresso.onView(ViewMatchers.isRoot())
            .perform(waitForView(ViewMatchers.withId(R.id.tableLayout)))
        Espresso.onView(ViewMatchers.withId(R.id.settingsImage)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.sound_enable)).perform(ViewActions.click())
        Espresso.pressBack()
        waitForCondition("Sound option checking", { !readSoundFromConfig() })


        waitForCondition("Filling cells mode checking", { !readCellFillingFromConfig() })
        Espresso.onView(ViewMatchers.isRoot())
            .perform(waitForView(ViewMatchers.withId(R.id.tableLayout)))
        Espresso.onView(ViewMatchers.withId(R.id.settingsImage)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.move_selection_to_solved_squares)).perform(ViewActions.click())
        Espresso.pressBack()
        waitForCondition("Filling cells mode checking", { readCellFillingFromConfig() })
    }
}