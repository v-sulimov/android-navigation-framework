package com.vsulimov.navigation.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Interface for handling Android activity lifecycle events.
 * This interface extends [Application.ActivityLifecycleCallbacks] to provide callbacks
 * for various stages of an activity's lifecycle, allowing monitoring and handling of
 * activity state changes.
 */
interface ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    /**
     * Called when the activity is first created.
     * This is where initial setup, such as inflating layouts or initializing views, typically occurs.
     *
     * @param activity The activity being created.
     * @param savedInstanceState The bundle containing the activity's previously saved state, or null if none exists.
     */
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
    }

    /**
     * Called when the activity becomes visible to the user.
     * This is typically used to start animations or other operations that should occur when the activity is starting.
     *
     * @param activity The activity that is starting.
     */
    override fun onActivityStarted(activity: Activity) {}

    /**
     * Called when the activity is fully interactive and in the foreground.
     * This is a good place to start processes like music playback or updating UI elements.
     *
     * @param activity The activity that is resumed.
     */
    override fun onActivityResumed(activity: Activity) {}

    /**
     * Called when the activity is no longer in the foreground but may still be visible.
     * This is suitable for pausing ongoing actions like animations or media playback.
     *
     * @param activity The activity that is paused.
     */
    override fun onActivityPaused(activity: Activity) {}

    /**
     * Called when the activity is no longer visible to the user.
     * This is a good place to stop background processes or release resources.
     *
     * @param activity The activity that is stopped.
     */
    override fun onActivityStopped(activity: Activity) {}

    /**
     * Called when the activity is about to be destroyed or rotated, to save its state.
     * This allows the activity to save data to be restored later via [savedInstanceState].
     *
     * @param activity The activity whose state is being saved.
     * @param outState The bundle in which to store the activity's state.
     */
    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) {
    }

    /**
     * Called when the activity is being fully destroyed.
     * This is a good place to perform final cleanup of resources or data.
     *
     * @param activity The activity being destroyed.
     */
    override fun onActivityDestroyed(activity: Activity) {}
}
