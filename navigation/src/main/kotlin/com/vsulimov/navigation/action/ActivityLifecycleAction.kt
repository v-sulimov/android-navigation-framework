package com.vsulimov.navigation.action

import android.os.Bundle
import com.vsulimov.redux.Action

/**
 * A sealed class representing actions related to the lifecycle of an Android Activity.
 * This class serves as a base for specific lifecycle actions and extends the [Action] interface.
 */
sealed class ActivityLifecycleAction : Action {
    /**
     * Represents the `onCreate` lifecycle event of an Android Activity.
     *
     * @property savedInstanceState The saved instance state bundle, if available, used to restore the Activity's state.
     */
    data class OnCreate(
        val savedInstanceState: Bundle?,
    ) : ActivityLifecycleAction()

    /**
     * Represents the `onStart` lifecycle event of an Android Activity.
     */
    data object OnStart : ActivityLifecycleAction()

    /**
     * Represents the `onResume` lifecycle event of an Android Activity.
     */
    data object OnResume : ActivityLifecycleAction()

    /**
     * Represents the `onPause` lifecycle event of an Android Activity.
     */
    data object OnPause : ActivityLifecycleAction()

    /**
     * Represents the `onStop` lifecycle event of an Android Activity.
     */
    data object OnStop : ActivityLifecycleAction()

    /**
     * Represents the `onDestroy` lifecycle event of an Android Activity.
     *
     * @property isFinishing Indicates whether the Activity is finishing when destroyed.
     */
    data class OnDestroy(
        val isFinishing: Boolean,
    ) : ActivityLifecycleAction()
}
