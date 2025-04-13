package com.vsulimov.navigation.reducer

import com.vsulimov.navigation.action.NavigationAction
import com.vsulimov.navigation.state.NavigationComponent
import com.vsulimov.navigation.state.NavigationState
import com.vsulimov.navigation.state.TransitionType
import com.vsulimov.redux.TypedReducer
import com.vsulimov.stack.CopyOnWriteStack

/**
 * Reduces the current navigation state based on a navigation action, producing a new state.
 * This function is intended to be integrated into the host application's Redux reducer.
 *
 * @param Screen The type representing screen states, defined by the host application, must implement [NavigationComponent]
 * @param Overlay The type representing overlay states, defined by the host application, must implement [NavigationComponent]
 *
 * @return A new NavigationState reflecting the changes from the action.
 *
 * @throws ClassCastException If the newScreen or overlay in the action cannot be cast to
 *         the expected Screen or Overlay type, indicating a mismatch in dispatched actions.
 */
@Suppress("UNCHECKED_CAST")
class NavigationReducer<Screen : NavigationComponent, Overlay : NavigationComponent> :
    TypedReducer<NavigationAction, NavigationState<Screen, Overlay>>(NavigationAction::class.java) {
    override fun reduceTyped(
        action: NavigationAction,
        state: NavigationState<Screen, Overlay>,
    ): NavigationState<Screen, Overlay> =
        when (action) {
            is NavigationAction.NavigateTo -> {
                try {
                    val backStack =
                        when {
                            action.clearBackStack -> CopyOnWriteStack<Screen>()
                            !action.addCurrentScreenToBackStack -> state.backStack
                            else -> state.backStack.push(state.screen)
                        }
                    val newScreen = action.newScreen as Screen
                    state.copy(
                        backStack = backStack,
                        screen = newScreen,
                        transitionType = TransitionType.FORWARD, // Forward for both cases
                    )
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException("Invalid screen type in NavigateTo action", e)
                }
            }

            is NavigationAction.GoBack -> {
                if (state.overlay != null) {
                    state.copy(overlay = null, transitionType = TransitionType.NONE)
                } else if (!state.backStack.isEmpty()) {
                    val (previousScreen, newBackStack) = state.backStack.pop()
                    state.copy(
                        screen = previousScreen ?: state.screen,
                        backStack = newBackStack,
                        transitionType = TransitionType.BACKWARD,
                    )
                } else {
                    state.copy(transitionType = TransitionType.NONE)
                }
            }

            is NavigationAction.ShowOverlay -> {
                try {
                    val overlay = action.overlay as Overlay
                    state.copy(overlay = overlay, transitionType = TransitionType.NONE)
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException("Invalid overlay type in ShowOverlay action", e)
                }
            }

            is NavigationAction.DismissOverlay -> {
                state.copy(overlay = null, transitionType = TransitionType.NONE)
            }
        }
}
