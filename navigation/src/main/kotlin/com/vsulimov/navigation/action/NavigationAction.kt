package com.vsulimov.navigation.action

import com.vsulimov.redux.Action

/**
 * Sealed class defining actions that can modify the navigation state.
 * Actions are dispatched to the store to trigger navigation changes.
 */
sealed class NavigationAction : Action {
    /**
     * Action to navigate to a new screen, optionally adding the current screen to the back stack.
     *
     * @property newScreen The target screen state to navigate to. Must be compatible with the
     *                     [Screen] type expected by the [NavigationState] in the reducer.
     * @property addCurrentScreenToBackStack Indicates whether the current screen should be added
     *                                       to the back stack. Defaults to `true`.
     * @property clearBackStack If `true`, clears the entire back stack before navigating. Defaults
     *                          to `false`.
     */
    data class NavigateTo(
        val newScreen: Any,
        val addCurrentScreenToBackStack: Boolean = true,
        val clearBackStack: Boolean = false,
    ) : NavigationAction()

    /**
     * Action to navigate back to the previous screen or dismiss the current overlay.
     * If an overlay is present, it dismisses the overlay; otherwise, it pops the back stack.
     */
    object GoBack : NavigationAction()

    /**
     * Action to display an overlay (e.g., dialog or bottom sheet).
     *
     * @property overlay The overlay state to display. Must match the Overlay type
     *                   expected by the NavigationState when processed by the reducer.
     */
    data class ShowOverlay(
        val overlay: Any,
    ) : NavigationAction()

    /**
     * Action to dismiss the current overlay, if any.
     */
    object DismissOverlay : NavigationAction()
}
