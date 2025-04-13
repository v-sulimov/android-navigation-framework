package com.vsulimov.navigation.action

import com.vsulimov.redux.Action

/**
 * Sealed class defining actions that can modify the navigation state.
 * Actions are dispatched to the store to trigger navigation changes.
 */
sealed class NavigationAction : Action {
    /**
     * Action to navigate to a new screen, adding the current screen to the back stack.
     *
     * @property newScreen The new screen state to navigate to. Must match the Screen type
     *                     expected by the NavigationState when processed by the reducer.
     * @property clearBackStack A flag that indicates that the navigation back stack should be cleared.
     */
    data class NavigateTo(
        val newScreen: Any,
        val clearBackStack: Boolean = false
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
    data class ShowOverlay(val overlay: Any) : NavigationAction()

    /**
     * Action to dismiss the current overlay, if any.
     */
    object DismissOverlay : NavigationAction()
}
