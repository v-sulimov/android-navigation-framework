package com.vsulimov.navigation.state

import com.vsulimov.stack.CopyOnWriteStack

/**
 * Represents the navigation state of the application, including the current screen,
 * back stack of previous screens, and an optional overlay.
 *
 * @param Screen The type representing screen states, defined by the host application.
 * @param Overlay The type representing overlay states, defined by the host application.
 *
 * @property screen The current screen state to be displayed.
 * @property backStack An immutable stack of previous screen states for back navigation.
 * @property overlay An optional overlay state (e.g., dialog or bottom sheet), or null if none.
 * @property transitionType The transition type for animation on screen replacement.
 */
data class NavigationState<Screen, Overlay>(
    val screen: Screen,
    val backStack: CopyOnWriteStack<Screen>,
    val overlay: Overlay?,
    val transitionType: TransitionType = TransitionType.NONE
)
