package com.vsulimov.navigation.state

import com.vsulimov.stack.CopyOnWriteStack

/**
 * Represents the current navigation state, including the current screen,
 * an optional overlay, and the transition type for screen changes.
 *
 * @param Screen The type representing screen states, must implement [NavigationComponent].
 * @param Overlay The type representing overlay states, must implement [NavigationComponent].
 *
 * @property screen The current screen state to be displayed.
 * @property backStack An immutable stack of previous screen states for back navigation.
 * @property overlay An optional overlay state (e.g., dialog or bottom sheet), or null if none.
 * @property transitionType The transition type for animation on screen replacement.
 */
data class NavigationState<Screen : NavigationComponent, Overlay : NavigationComponent>(
    val screen: Screen,
    val backStack: CopyOnWriteStack<Screen>,
    val overlay: Overlay?,
    val transitionType: TransitionType = TransitionType.NONE,
)
