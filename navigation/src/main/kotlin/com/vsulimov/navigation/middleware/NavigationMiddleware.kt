package com.vsulimov.navigation.middleware

import android.util.Log
import com.vsulimov.navigation.action.NavigationAction
import com.vsulimov.redux.Action
import com.vsulimov.redux.TypedMiddleware
import com.vsulimov.stack.CopyOnWriteStack
import kotlinx.coroutines.CoroutineScope

/**
 * Middleware that processes [NavigationAction.GoBack] actions to handle back navigation logic.
 *
 * This class intercepts [NavigationAction.GoBack] actions and determines whether to finish the activity
 * or allow the action to proceed based on the current navigation state. It is part of the application's
 * middleware chain, ensuring proper handling of back navigation for screens and overlays.
 *
 * @param Application The type representing the application's state.
 * @param Screen The type representing the screen in the navigation stack.
 * @param Overlay The type representing the overlay state.
 * @param toOverlayState A function that extracts the overlay state from the application state.
 * @param toNavigationBackStack A function that extracts the navigation back stack from the application state.
 * @param finishActivityFunction A lambda function to finish the activity when back navigation requires it.
 * @constructor Creates an instance of [NavigationMiddleware] with the provided state extraction functions and activity finish function.
 * @see NavigationAction.GoBack
 * @see TypedMiddleware
 */
class NavigationMiddleware<Application, Screen, Overlay>(
    private val toOverlayState: (Application) -> Overlay?,
    private val toNavigationBackStack: (Application) -> CopyOnWriteStack<Screen>,
    private val finishActivityFunction: () -> Unit,
) : TypedMiddleware<NavigationAction.GoBack, Application>(NavigationAction.GoBack::class.java) {
    /**
     * Processes a [NavigationAction.GoBack] action and determines the appropriate navigation behavior.
     *
     * This method evaluates the current application state. If no overlay is present and the navigation back stack is empty,
     * it invokes the [finishActivityFunction] to close the activity. Otherwise, it allows the action to proceed to the next
     * middleware in the chain without taking any action. Logs are generated to track the resolution of the action.
     *
     * @param action The [NavigationAction.GoBack] action to process.
     * @param state The current application state containing navigation information.
     * @param next A function to pass the action to the next middleware in the chain.
     * @param dispatch A function to dispatch new actions to the state management system.
     * @param scope The [CoroutineScope] for handling asynchronous operations.
     */
    override fun invokeTyped(
        action: NavigationAction.GoBack,
        state: Application,
        next: (Action) -> Unit,
        dispatch: (Action) -> Unit,
        scope: CoroutineScope,
    ) {
        if (toOverlayState(state) == null && toNavigationBackStack(state).isEmpty()) {
            Log.d(TAG, "Received GoBack action. Overlay is null and backStack is empty. Resolution: Finish activity.")
            finishActivityFunction.invoke()
        } else {
            Log.d(TAG, "Received GoBack action. Overlay is present or backStack is not empty. Resolution: Do nothing.")
        }
        next(action)
    }

    companion object {
        /**
         * Tag used for logging navigation-related events.
         */
        private const val TAG = "NavigationMiddleware"
    }
}
