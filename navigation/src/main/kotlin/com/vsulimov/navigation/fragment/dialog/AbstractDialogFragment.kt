package com.vsulimov.navigation.fragment.dialog

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import com.vsulimov.navigation.action.NavigationAction.DismissOverlay
import com.vsulimov.redux.Store

/**
 * Abstract base class for DialogFragments providing shared functionality.
 * Extends [DialogFragment].
 */
abstract class AbstractDialogFragment : DialogFragment() {
    /**
     * Provides the Redux store instance.
     *
     * @return The [Store] instance with a generic state type.
     */
    abstract fun provideStore(): Store<*>

    /**
     * Flag indicating whether to dispatch the [DismissOverlay] action on dialog dismissal.
     * Defaults to `true`.
     */
    protected var shouldDispatchOnDismiss: Boolean = true

    /**
     * Handles dialog dismissal, executing default dismissal behavior and conditionally
     * dispatching a [DismissOverlay] action if the host activity is in the RESUMED state
     * and [shouldDispatchOnDismiss] is `true`.
     *
     * @param dialog The [DialogInterface] that was dismissed.
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = activity
        if (activity != null && activity.lifecycle.currentState == Lifecycle.State.RESUMED && shouldDispatchOnDismiss) {
            provideStore().dispatch(DismissOverlay)
        }
    }
}
