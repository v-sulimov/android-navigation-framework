package com.vsulimov.navigation.state

import com.vsulimov.navigation.state.TransitionType.BACKWARD
import com.vsulimov.navigation.state.TransitionType.FORWARD
import com.vsulimov.navigation.state.TransitionType.NONE

/**
 * Represents the type of transition to be used when navigating between screens.
 * This enum is part of the navigation state and is used by the [NavigationController]
 * to select the appropriate animation or transition effect during screen changes.
 *
 * @property FORWARD Indicates a forward navigation, typically used when pushing a new screen
 *                   onto the stack or clearing the back stack to a new screen.
 * @property BACKWARD Indicates a backward navigation, used when popping a screen from the stack
 *                    to return to a previous screen.
 * @property NONE Indicates no transition animation should be applied, often used for initial
 *                screen loads or when the screen remains the same.
 */
enum class TransitionType {
    FORWARD,
    BACKWARD,
    NONE,
}
