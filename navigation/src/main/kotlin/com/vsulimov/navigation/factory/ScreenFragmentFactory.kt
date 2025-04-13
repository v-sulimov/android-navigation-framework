package com.vsulimov.navigation.factory

import androidx.fragment.app.Fragment

/**
 * Factory interface for creating screen fragments based on screen states.
 * The host application must implement this to provide fragments for each screen type.
 *
 * @param Screen The type representing screen states, defined by the host application.
 */
interface ScreenFragmentFactory<Screen> {
    /**
     * Creates a fragment for the given screen state.
     *
     * @param screen The screen state for which to create a fragment.
     * @return A Fragment instance representing the screen.
     */
    fun createScreenFragment(screen: Screen): Fragment
}
