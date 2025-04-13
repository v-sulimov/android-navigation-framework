package com.vsulimov.navigation.factory

import androidx.fragment.app.Fragment
import com.vsulimov.navigation.state.NavigationComponent

/**
 * Factory interface for creating screen fragments based on screen states.
 * The host application must implement this to provide fragments for each screen type.
 *
 * @param Screen The type representing screen states, must implement [NavigationComponent].
 */
interface ScreenFragmentFactory<Screen : NavigationComponent> {
    /**
     * Creates a fragment for the given screen state.
     * The implementation should store the screen's typeId in the fragment's arguments.
     *
     * @param screen The screen state for which to create a fragment.
     * @return A Fragment instance representing the screen.
     */
    fun createScreenFragment(screen: Screen): Fragment

    /**
     * Retrieves the type identifier of the screen state associated with the given fragment.
     * Typically, this is retrieved from the fragment's arguments.
     *
     * @param fragment The fragment whose corresponding screen state typeId is to be retrieved.
     * @return The typeId of the screen state associated with the fragment.
     */
    fun getStateTypeIdForScreen(fragment: Fragment): String
}
