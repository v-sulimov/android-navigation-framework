package com.vsulimov.navigation.middleware

import com.vsulimov.navigation.action.NavigationAction
import com.vsulimov.redux.Action
import com.vsulimov.stack.CopyOnWriteStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [NavigationMiddleware] to verify back navigation logic.
 */
class NavigationMiddlewareTest {
    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private var finishActivityCalled = false
    private val finishActivityFunction: () -> Unit = { finishActivityCalled = true }
    private var nextAction: Action? = null
    private val next: (Action) -> Unit = { action -> nextAction = action }
    private val dispatch: (Action) -> Unit = { /* No-op for tests */ }

    /**
     * Tests that [finishActivityFunction] is called when the overlay is null and the back stack is empty.
     */
    @Test
    fun `given no overlay and empty back stack, when GoBack action is processed, then finishActivityFunction is called`() {
        val state = TestState(overlay = null, backStack = CopyOnWriteStack())
        val middleware =
            NavigationMiddleware<TestState, String, String>(
                toOverlayState = { it.overlay },
                toNavigationBackStack = { it.backStack },
                finishActivityFunction = finishActivityFunction,
            )
        val action = NavigationAction.GoBack

        middleware.invokeTyped(action, state, next, dispatch, scope)

        assertTrue(finishActivityCalled, "finishActivityFunction should be called")
        assertEquals(action, nextAction, "Action should be passed to next middleware")
    }

    /**
     * Tests that [finishActivityFunction] is not called when an overlay is present.
     */
    @Test
    fun `given overlay present, when GoBack action is processed, then finishActivityFunction is not called`() {
        val state = TestState(overlay = "SomeOverlay", backStack = CopyOnWriteStack())
        val middleware =
            NavigationMiddleware<TestState, String, String>(
                toOverlayState = { it.overlay },
                toNavigationBackStack = { it.backStack },
                finishActivityFunction = finishActivityFunction,
            )
        val action = NavigationAction.GoBack

        middleware.invokeTyped(action, state, next, dispatch, scope)

        assertTrue(!finishActivityCalled, "finishActivityFunction should not be called")
        assertEquals(action, nextAction, "Action should be passed to next middleware")
    }

    /**
     * Tests that [finishActivityFunction] is not called when the back stack is not empty.
     */
    @Test
    fun `given non-empty back stack, when GoBack action is processed, then finishActivityFunction is not called`() {
        val backStack = CopyOnWriteStack<String>().push("Screen1")
        val state = TestState(overlay = null, backStack = backStack)
        val middleware =
            NavigationMiddleware<TestState, String, String>(
                toOverlayState = { it.overlay },
                toNavigationBackStack = { it.backStack },
                finishActivityFunction = finishActivityFunction,
            )
        val action = NavigationAction.GoBack

        middleware.invokeTyped(action, state, next, dispatch, scope)

        assertTrue(!finishActivityCalled, "finishActivityFunction should not be called")
        assertEquals(action, nextAction, "Action should be passed to next middleware")
    }

    /**
     * Data class representing a test application state.
     *
     * @param overlay The overlay state, or null if no overlay is present.
     * @param backStack The navigation back stack containing screens.
     */
    private data class TestState(
        val overlay: String?,
        val backStack: CopyOnWriteStack<String>,
    )
}
