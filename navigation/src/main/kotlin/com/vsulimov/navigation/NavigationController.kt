package com.vsulimov.navigation

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vsulimov.navigation.action.NavigationAction.GoBack
import com.vsulimov.navigation.animation.AnimationSet
import com.vsulimov.navigation.factory.OverlayFragmentFactory
import com.vsulimov.navigation.factory.ScreenFragmentFactory
import com.vsulimov.navigation.state.NavigationComponent
import com.vsulimov.navigation.state.NavigationState
import com.vsulimov.navigation.state.TransitionType
import com.vsulimov.navigation.util.Logger.logOverlayFragmentCreated
import com.vsulimov.navigation.util.Logger.logOverlayStateExistsButDialogFragmentMismatch
import com.vsulimov.navigation.util.Logger.logOverlayStateNullButDialogFragmentExists
import com.vsulimov.navigation.util.Logger.logScreenFragmentCreated
import com.vsulimov.redux.Action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

/**
 * Manages navigation between screens and overlays in an Android app by observing a navigation state flow
 * and updating the UI with fragments. This class should be instantiated in a [FragmentActivity] and
 * initialized via [init] to begin navigation.
 *
 * @param Screen The type representing screen states, must implement [NavigationComponent].
 * @param Overlay The type representing overlay states, must implement [NavigationComponent].
 *
 * @param dispatchFunction Function to dispatch [Action] to the application's Redux store.
 * @param navigationStateFlow [Flow] providing [NavigationState] updates from the application's store.
 * @param activity The [FragmentActivity] hosting the navigation framework.
 * @param screenFragmentFactory Factory for creating screen fragments based on screen states.
 * @param overlayFragmentFactory Factory for creating overlay dialog fragments based on overlay states.
 * @param containerId The ID of the layout view (e.g., FrameLayout) where screen fragments are placed.
 * @param defaultAnimations Optional [AnimationSet] for screen transition animations (default is no animations).
 *
 * **Note**: The application must ensure that each distinct screen and overlay type has a unique `typeId`.
 * The `typeId` is a string that uniquely identifies the screen or overlay state type. Consider using a registry
 * or map in the factories to enforce uniqueness during development.
 */
class NavigationController<Screen : NavigationComponent, Overlay : NavigationComponent>(
    private val dispatchFunction: (Action) -> Unit,
    private val navigationStateFlow: Flow<NavigationState<Screen, Overlay>>,
    private val activity: FragmentActivity,
    private val screenFragmentFactory: ScreenFragmentFactory<Screen>,
    private val overlayFragmentFactory: OverlayFragmentFactory<Overlay>,
    private val containerId: Int,
    private val defaultAnimations: AnimationSet = AnimationSet(),
) {
    private val fragmentManager: FragmentManager = activity.supportFragmentManager
    private var isInitialSetup = true

    private val backPressedCallback =
        object : OnBackPressedCallback(true) {
            /**
             * Dispatches a [GoBack] action when the system back button is pressed.
             */
            override fun handleOnBackPressed() {
                dispatchFunction(GoBack)
            }
        }

    /**
     * Initializes the navigation controller by subscribing to state changes and registering
     * the back press callback. Call this method in [FragmentActivity.onCreate] after instantiation.
     */
    fun init() {
        subscribeToScreenStateChanges()
        subscribeToOverlayStateChanges()
        activity.onBackPressedDispatcher.addCallback(activity, backPressedCallback)
    }

    /**
     * Subscribes to screen state changes, updating the UI only when the screen type changes.
     * Uses lifecycle-aware coroutines to ensure updates occur when the activity is started.
     */
    private fun subscribeToScreenStateChanges() {
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigationStateFlow
                    .distinctUntilChangedBy { it.screen::class }
                    .collect { state -> updateScreen(state.screen, state.transitionType) }
            }
        }
    }

    /**
     * Subscribes to overlay state changes, updating the UI only when the overlay type changes.
     * Uses lifecycle-aware coroutines to ensure updates occur when the activity is started.
     */
    private fun subscribeToOverlayStateChanges() {
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigationStateFlow
                    .distinctUntilChangedBy { it.overlay?.let { overlay -> overlay::class } }
                    .collect { state -> updateOverlay(state.overlay) }
            }
        }
    }

    /**
     * Updates the current screen fragment if the new screen type differs from the existing one.
     *
     * @param screenState The new screen state to display.
     * @param transitionType The [TransitionType] defining the animation for the screen transition.
     */
    private fun updateScreen(
        screenState: Screen,
        transitionType: TransitionType,
    ) {
        val currentScreenTypeId =
            fragmentManager.findFragmentByTag(TAG_SCREEN)?.let {
                screenFragmentFactory.getStateTypeIdForScreen(it)
            }
        val newScreenTypeId = screenState.typeId
        if (currentScreenTypeId != newScreenTypeId) {
            replaceScreenFragment(screenState, newScreenTypeId, transitionType)
        }
        isInitialSetup = false
    }

    /**
     * Replaces the current screen fragment with a new one based on the provided screen state.
     *
     * @param screenState The screen state to create a fragment for.
     * @param newScreenTypeId The unique type ID of the new screen state.
     * @param transitionType The [TransitionType] for the transition animation.
     */
    private fun replaceScreenFragment(
        screenState: Screen,
        newScreenTypeId: String,
        transitionType: TransitionType,
    ) {
        val newScreenFragment = screenFragmentFactory.createScreenFragment(screenState)
        logScreenFragmentCreated(newScreenFragment, newScreenTypeId)
        val transaction = fragmentManager.beginTransaction()
        setupScreenTransitionAnimations(transaction, transitionType)
        transaction.replace(containerId, newScreenFragment, TAG_SCREEN)
        transaction.commitNow() // Synchronous commit for immediate UI consistency
    }

    /**
     * Configures transition animations for screen fragment replacement if animations are enabled
     * and this is not the initial setup.
     *
     * @param transaction The [FragmentTransaction] to apply animations to.
     * @param transitionType The [TransitionType] determining the animation pair.
     */
    private fun setupScreenTransitionAnimations(
        transaction: FragmentTransaction,
        transitionType: TransitionType,
    ) {
        if (!defaultAnimations.disableAnimations && !isInitialSetup) {
            val (enter, exit) = getAnimationPair(transitionType)
            if (enter != 0 && exit != 0) {
                transaction.setCustomAnimations(enter, exit)
            }
        }
    }

    /**
     * Returns the enter and exit animation resource IDs for a given transition type.
     *
     * @param transitionType The [TransitionType] (FORWARD, BACKWARD, NONE).
     * @return A [Pair] of enter and exit animation resource IDs (0 indicates no animation).
     */
    private fun getAnimationPair(transitionType: TransitionType): Pair<Int, Int> =
        when (transitionType) {
            TransitionType.FORWARD -> defaultAnimations.forwardEnter to defaultAnimations.forwardExit
            TransitionType.BACKWARD -> defaultAnimations.backwardEnter to defaultAnimations.backwardExit
            TransitionType.NONE -> 0 to 0
        }

    /**
     * Updates the overlay UI by showing, replacing, or dismissing a dialog fragment based on the overlay state.
     *
     * @param overlayState The new overlay state to display, or null to dismiss the current overlay.
     */
    private fun updateOverlay(overlayState: Overlay?) {
        val currentOverlay = fragmentManager.findFragmentByTag(TAG_OVERLAY) as? DialogFragment
        when {
            overlayState == null && currentOverlay != null -> dismissOverlay(currentOverlay)
            overlayState != null && currentOverlay == null -> showOverlay(overlayState)
            overlayState != null && currentOverlay != null -> replaceOverlayIfNecessary(currentOverlay, overlayState)
        }
    }

    /**
     * Dismisses the current overlay dialog fragment.
     *
     * @param currentOverlay The [DialogFragment] to dismiss.
     */
    private fun dismissOverlay(currentOverlay: DialogFragment) {
        logOverlayStateNullButDialogFragmentExists()
        currentOverlay.dismissAllowingStateLoss()
    }

    /**
     * Shows a new overlay dialog fragment based on the provided overlay state.
     *
     * @param overlayState The overlay state to create and display a fragment for.
     */
    private fun showOverlay(overlayState: Overlay) {
        val overlayFragment = overlayFragmentFactory.createOverlayFragment(overlayState)
        logOverlayFragmentCreated(overlayFragment, overlayState.typeId)
        overlayFragment.show(fragmentManager, TAG_OVERLAY)
    }

    /**
     * Handles replacing an existing overlay fragment if its type differs from the new overlay state.
     *
     * @param currentOverlay The current [DialogFragment] displayed as an overlay.
     * @param overlayState The new overlay state to potentially replace the current overlay.
     */
    private fun replaceOverlayIfNecessary(
        currentOverlay: DialogFragment,
        overlayState: Overlay,
    ) {
        val currentOverlayTypeId = overlayFragmentFactory.getStateTypeIdForOverlay(currentOverlay)
        val newOverlayTypeId = overlayState.typeId
        if (currentOverlayTypeId != newOverlayTypeId) {
            logOverlayStateExistsButDialogFragmentMismatch(newOverlayTypeId, currentOverlayTypeId)
            currentOverlay.dismissAllowingStateLoss()
            showOverlay(overlayState)
        }
    }

    companion object {
        private const val TAG_SCREEN = "Screen" // Tag for screen fragments
        private const val TAG_OVERLAY = "Overlay" // Tag for overlay fragments
    }
}
