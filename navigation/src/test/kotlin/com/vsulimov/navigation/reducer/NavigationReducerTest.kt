package com.vsulimov.navigation.reducer

import com.vsulimov.navigation.action.NavigationAction
import com.vsulimov.navigation.state.NavigationState
import com.vsulimov.navigation.state.TransitionType
import com.vsulimov.stack.CopyOnWriteStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavigationReducerTest {

    private val navigationReducer = NavigationReducer<String, String?>()

    @Test
    fun `NavigateTo pushes current screen to back stack and sets new screen`() {
        val initialState = NavigationState<String, String?>(
            screen = "Home",
            backStack = CopyOnWriteStack(),
            overlay = null
        )
        val newState = navigationReducer.reduceTyped(
            action = NavigationAction.NavigateTo("Details"),
            state = initialState
        )
        assertEquals(expected = "Details", actual = newState.screen)
        assertEquals(expected = "Home", actual = newState.backStack.peek())
        assertEquals(TransitionType.FORWARD, newState.transitionType)
    }

    @Test
    fun `NavigateTo sets new screen and clears back stack when flag is true`() {
        val initialState = NavigationState<String, String?>(
            screen = "Home",
            backStack = CopyOnWriteStack(),
            overlay = null
        )
        val newState = navigationReducer.reduceTyped(
            action = NavigationAction.NavigateTo(newScreen = "Details", clearBackStack = true),
            state = initialState
        )
        assertEquals(expected = "Details", actual = newState.screen)
        assertNull(newState.backStack.peek())
        assertEquals(TransitionType.FORWARD, newState.transitionType)
    }

    @Test
    fun `GoBack dismisses overlay when overlay is present`() {
        val initialState = NavigationState<String, String?>(
            screen = "Home",
            backStack = CopyOnWriteStack(),
            overlay = "Overlay"
        )
        val newState = navigationReducer.reduceTyped(
            action = NavigationAction.GoBack,
            state = initialState
        )
        assertNull(newState.overlay)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `GoBack pops back stack and sets previous screen when no overlay is present`() {
        val backStack = CopyOnWriteStack<String>().push("Previous")
        val initialState = NavigationState<String, String?>(
            screen = "Current",
            backStack = backStack,
            overlay = null
        )
        val newState = navigationReducer.reduceTyped(
            action = NavigationAction.GoBack,
            state = initialState
        )
        assertEquals(expected = "Previous", actual = newState.screen)
        assertEquals(expected = 0, actual = newState.backStack.size())
        assertEquals(TransitionType.BACKWARD, newState.transitionType)
    }

    @Test
    fun `GoBack leaves state unchanged when no overlay and back stack is empty`() {
        val initialState = NavigationState<String, String?>(
            screen = "Home",
            backStack = CopyOnWriteStack(),
            overlay = null,
            transitionType = TransitionType.NONE
        )
        val newState = navigationReducer.reduceTyped(
            action = NavigationAction.GoBack,
            state = initialState
        )
        assertEquals(initialState, newState)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `ShowOverlay sets overlay and transitionType to NONE`() {
        val initialState = NavigationState<String, String?>(
            screen = "Home",
            backStack = CopyOnWriteStack(),
            overlay = null
        )
        val newState = navigationReducer.reduceTyped(
            action = NavigationAction.ShowOverlay("Overlay"),
            state = initialState
        )
        assertEquals(expected = "Overlay", actual = newState.overlay)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `DismissOverlay clears overlay and sets transitionType to NONE`() {
        val initialState = NavigationState<String, String?>(
            screen = "Home",
            backStack = CopyOnWriteStack(),
            overlay = "Overlay"
        )
        val newState = navigationReducer.reduceTyped(
            action = NavigationAction.DismissOverlay,
            state = initialState
        )
        assertNull(newState.overlay)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `Multiple NavigateTo and GoBack actions handle back stack correctly`() {
        var state = NavigationState<String, String?>(
            screen = "A",
            backStack = CopyOnWriteStack(),
            overlay = null
        )

        // Navigate to B
        state = navigationReducer.reduceTyped(
            action = NavigationAction.NavigateTo("B"),
            state = state
        )
        assertEquals("B", state.screen)
        assertEquals("A", state.backStack.peek())
        assertEquals(TransitionType.FORWARD, state.transitionType)

        // Navigate to C
        state = navigationReducer.reduceTyped(
            action = NavigationAction.NavigateTo("C"),
            state = state
        )
        assertEquals("C", state.screen)
        assertEquals("B", state.backStack.peek())
        assertEquals(TransitionType.FORWARD, state.transitionType)

        // GoBack to B
        state = navigationReducer.reduceTyped(
            action = NavigationAction.GoBack,
            state = state
        )
        assertEquals("B", state.screen)
        assertEquals("A", state.backStack.peek())
        assertEquals(TransitionType.BACKWARD, state.transitionType)

        // GoBack to A
        state = navigationReducer.reduceTyped(
            action = NavigationAction.GoBack,
            state = state
        )
        assertEquals("A", state.screen)
        assertNull(state.backStack.peek())
        assertEquals(TransitionType.BACKWARD, state.transitionType)
    }
}
