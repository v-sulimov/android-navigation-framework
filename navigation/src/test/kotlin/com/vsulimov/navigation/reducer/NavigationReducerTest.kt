package com.vsulimov.navigation.reducer

import com.vsulimov.navigation.action.NavigationAction
import com.vsulimov.navigation.state.NavigationComponent
import com.vsulimov.navigation.state.NavigationState
import com.vsulimov.navigation.state.TransitionType
import com.vsulimov.stack.CopyOnWriteStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavigationReducerTest {
    sealed class ScreenState(
        override val typeId: String,
    ) : NavigationComponent {
        data class Home(
            override val typeId: String = "Home",
        ) : ScreenState(typeId)

        data class Details(
            override val typeId: String = "Details",
        ) : ScreenState(typeId)

        data class Current(
            override val typeId: String = "Current",
        ) : ScreenState(typeId)

        data class Previous(
            override val typeId: String = "Previous",
        ) : ScreenState(typeId)

        data class A(
            override val typeId: String = "A",
        ) : ScreenState(typeId)

        data class B(
            override val typeId: String = "B",
        ) : ScreenState(typeId)

        data class C(
            override val typeId: String = "C",
        ) : ScreenState(typeId)
    }

    sealed class OverlayState(
        override val typeId: String,
    ) : NavigationComponent {
        data class Default(
            override val typeId: String = "Default",
        ) : OverlayState(typeId)
    }

    private val navigationReducer = NavigationReducer<ScreenState, OverlayState>()

    @Test
    fun `NavigateTo pushes current screen to back stack and sets new screen`() {
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Home(),
                backStack = CopyOnWriteStack(),
                overlay = null,
            )
        val newState =
            navigationReducer.reduceTyped(
                action = NavigationAction.NavigateTo(ScreenState.Details()),
                state = initialState,
            )
        assertEquals(expected = "Details", actual = newState.screen.typeId)
        assertEquals(expected = "Home", actual = newState.backStack.peek()?.typeId)
        assertEquals(TransitionType.FORWARD, newState.transitionType)
    }

    @Test
    fun `NavigateTo sets new screen without adding to back stack when addCurrentScreenToBackStack is false`() {
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Home(),
                backStack = CopyOnWriteStack(),
                overlay = null,
            )
        val newState =
            navigationReducer.reduceTyped(
                action =
                    NavigationAction.NavigateTo(
                        newScreen = ScreenState.Details(),
                        addCurrentScreenToBackStack = false,
                    ),
                state = initialState,
            )
        assertEquals(expected = "Details", actual = newState.screen.typeId)
        assertNull(newState.backStack.peek())
        assertEquals(TransitionType.FORWARD, newState.transitionType)
    }

    @Test
    fun `NavigateTo sets new screen and clears back stack when flag is true`() {
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Home(),
                backStack = CopyOnWriteStack(),
                overlay = null,
            )
        val newState =
            navigationReducer.reduceTyped(
                action = NavigationAction.NavigateTo(newScreen = ScreenState.Details(), clearBackStack = true),
                state = initialState,
            )
        assertEquals(expected = "Details", actual = newState.screen.typeId)
        assertNull(newState.backStack.peek())
        assertEquals(TransitionType.FORWARD, newState.transitionType)
    }

    @Test
    fun `GoBack dismisses overlay when overlay is present`() {
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Home(),
                backStack = CopyOnWriteStack(),
                overlay = OverlayState.Default(),
            )
        val newState =
            navigationReducer.reduceTyped(
                action = NavigationAction.GoBack,
                state = initialState,
            )
        assertNull(newState.overlay)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `GoBack pops back stack and sets previous screen when no overlay is present`() {
        val backStack = CopyOnWriteStack<ScreenState>().push(ScreenState.Previous())
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Current(),
                backStack = backStack,
                overlay = null,
            )
        val newState =
            navigationReducer.reduceTyped(
                action = NavigationAction.GoBack,
                state = initialState,
            )
        assertEquals(expected = "Previous", actual = newState.screen.typeId)
        assertEquals(expected = 0, actual = newState.backStack.size())
        assertEquals(TransitionType.BACKWARD, newState.transitionType)
    }

    @Test
    fun `GoBack leaves state unchanged when no overlay and back stack is empty`() {
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Home(),
                backStack = CopyOnWriteStack(),
                overlay = null,
                transitionType = TransitionType.NONE,
            )
        val newState =
            navigationReducer.reduceTyped(
                action = NavigationAction.GoBack,
                state = initialState,
            )
        assertEquals(initialState, newState)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `ShowOverlay sets overlay and transitionType to NONE`() {
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Home(),
                backStack = CopyOnWriteStack(),
                overlay = null,
            )
        val newState =
            navigationReducer.reduceTyped(
                action = NavigationAction.ShowOverlay(OverlayState.Default()),
                state = initialState,
            )
        assertEquals(expected = "Default", actual = newState.overlay?.typeId)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `DismissOverlay clears overlay and sets transitionType to NONE`() {
        val initialState =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.Home(),
                backStack = CopyOnWriteStack(),
                overlay = OverlayState.Default(),
            )
        val newState =
            navigationReducer.reduceTyped(
                action = NavigationAction.DismissOverlay,
                state = initialState,
            )
        assertNull(newState.overlay)
        assertEquals(TransitionType.NONE, newState.transitionType)
    }

    @Test
    fun `Multiple NavigateTo and GoBack actions handle back stack correctly`() {
        var state =
            NavigationState<ScreenState, OverlayState>(
                screen = ScreenState.A(),
                backStack = CopyOnWriteStack(),
                overlay = null,
            )

        // Navigate to B
        state =
            navigationReducer.reduceTyped(
                action = NavigationAction.NavigateTo(ScreenState.B()),
                state = state,
            )
        assertEquals("B", state.screen.typeId)
        assertEquals("A", state.backStack.peek()?.typeId)
        assertEquals(TransitionType.FORWARD, state.transitionType)

        // Navigate to C
        state =
            navigationReducer.reduceTyped(
                action = NavigationAction.NavigateTo(ScreenState.C()),
                state = state,
            )
        assertEquals("C", state.screen.typeId)
        assertEquals("B", state.backStack.peek()?.typeId)
        assertEquals(TransitionType.FORWARD, state.transitionType)

        // GoBack to B
        state =
            navigationReducer.reduceTyped(
                action = NavigationAction.GoBack,
                state = state,
            )
        assertEquals("B", state.screen.typeId)
        assertEquals("A", state.backStack.peek()?.typeId)
        assertEquals(TransitionType.BACKWARD, state.transitionType)

        // GoBack to A
        state =
            navigationReducer.reduceTyped(
                action = NavigationAction.GoBack,
                state = state,
            )
        assertEquals("A", state.screen.typeId)
        assertNull(state.backStack.peek())
        assertEquals(TransitionType.BACKWARD, state.transitionType)
    }
}
