package com.vsulimov.navigation

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vsulimov.navigation.action.NavigationAction
import com.vsulimov.navigation.animation.AnimationSet
import com.vsulimov.navigation.factory.OverlayFragmentFactory
import com.vsulimov.navigation.factory.ScreenFragmentFactory
import com.vsulimov.navigation.state.NavigationState
import com.vsulimov.navigation.state.TransitionType
import com.vsulimov.redux.Action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Controls navigation by observing the navigation state and updating the UI with fragments.
 * This class should be instantiated in a FragmentActivity and initialized to start navigation.
 *
 * @param Screen The type representing screen states, defined by the host application. Must be non-null.
 * @param Overlay The type representing overlay states, defined by the host application.
 *
 * @param dispatchFunction Function to dispatch [Action] to the host application store.
 * @param navigationStateFlow [Flow] with [NavigationState] updates from the host application store.
 * @param activity The FragmentActivity hosting the navigation framework.
 * @param screenFragmentFactory Factory for creating screen fragments.
 * @param overlayFragmentFactory Factory for creating overlay dialog fragments.
 * @param containerId The ID of the layout view (e.g., FrameLayout) where screen fragments are placed.
 * @param defaultAnimations Optional animations for screen transitions.
 */
class NavigationController<Screen : Any, Overlay>(
    private val dispatchFunction: (Action) -> Unit,
    private val navigationStateFlow: Flow<NavigationState<Screen, Overlay>>,
    private val activity: FragmentActivity,
    private val screenFragmentFactory: ScreenFragmentFactory<Screen>,
    private val overlayFragmentFactory: OverlayFragmentFactory<Overlay>,
    private val containerId: Int,
    private val defaultAnimations: AnimationSet = AnimationSet()
) {
    private val fragmentManager: FragmentManager = activity.supportFragmentManager
    private var currentFragment: Fragment? = null
    private var currentOverlay: DialogFragment? = null
    private var currentOverlayState: Overlay? = null
    private var currentScreenClass: KClass<*>? = null
    private var isInitialSetup = true

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        /**
         * Handles the system back press by dispatching a GoBack action to the store.
         */
        override fun handleOnBackPressed() {
            dispatchFunction(NavigationAction.GoBack)
        }
    }

    /**
     * Initializes the navigation controller by starting state observation and registering
     * the back press callback. Must be called after construction (e.g., in Activity.onCreate).
     */
    fun init() {
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigationStateFlow
                    .distinctUntilChanged()   // Skip identical states
                    .collect { state ->
                        updateScreen(state.screen, state.transitionType)
                        updateOverlay(state.overlay)
                    }
            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, backPressedCallback)
    }

    /**
     * Updates the displayed screen fragment based on the new screen state.
     * Only replaces the fragment if the screen type (KClass) changes.
     *
     * @param screen The new screen state to display. Assumed non-null due to Screen : Any constraint.
     * @param transitionType The transition type for animation on screen replacement.
     */
    private fun updateScreen(screen: Screen, transitionType: TransitionType) {
        val screenClass = screen::class
        if (currentScreenClass != screenClass) {
            val newFragment = screenFragmentFactory.createScreenFragment(screen)
            val transaction = fragmentManager.beginTransaction()
            if (!defaultAnimations.disableAnimations && !isInitialSetup) {
                val (enter, exit) = when (transitionType) {
                    TransitionType.FORWARD -> {
                        defaultAnimations.forwardEnter to defaultAnimations.forwardExit
                    }

                    TransitionType.BACKWARD -> {
                        defaultAnimations.backwardEnter to defaultAnimations.backwardExit
                    }

                    TransitionType.NONE -> {
                        0 to 0
                    }
                }
                if (enter != 0 && exit != 0) {
                    transaction.setCustomAnimations(enter, exit)
                }
            }
            transaction.replace(containerId, newFragment)
            transaction.commitAllowingStateLoss() // Safe commit to avoid IllegalStateException
            currentFragment = newFragment
            currentScreenClass = screenClass
        }
        isInitialSetup = false
    }

    /**
     * Updates the overlay by showing a new dialog fragment or dismissing the current one.
     * Reuses an existing overlay fragment if it matches the new overlay state.
     *
     * @param overlay The new overlay state to display, or null to dismiss the current overlay.
     */
    private fun updateOverlay(overlay: Overlay?) {
        if (overlay == null) {
            currentOverlay?.dismissAllowingStateLoss()
            currentOverlay = null
            currentOverlayState = null
        } else {
            val tag = "overlay_${overlay.hashCode()}"
            val existingFragment = fragmentManager.findFragmentByTag(tag) as? DialogFragment
            if (existingFragment != null) {
                if (existingFragment != currentOverlay) {
                    currentOverlay?.dismissAllowingStateLoss()
                    currentOverlay = existingFragment
                    currentOverlayState = overlay
                }
            } else {
                currentOverlay?.dismissAllowingStateLoss()
                val dialogFragment = overlayFragmentFactory.createOverlayFragment(overlay)
                dialogFragment.show(fragmentManager, tag)
                currentOverlay = dialogFragment
                currentOverlayState = overlay
            }
        }
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * Should be called in Activity.onDestroy.
     */
    fun cleanup() {
        currentOverlay?.let { overlay ->
            if (overlay.isAdded) {
                overlay.dismissAllowingStateLoss()
            }
        }
        currentOverlay = null
        currentFragment = null
    }
}
