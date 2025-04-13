package com.vsulimov.navigation.util

import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

/**
 * A utility object for logging navigation-related events in the application.
 * This logger uses Android's [Log] class to output debug messages with a consistent tag.
 * All methods are designed to log specific events related to screen and overlay fragments,
 * ensuring clear and verbose messages for debugging navigation flows.
 */
object Logger {
    /** The tag used for all log messages to identify navigation-related logs. */
    private const val TAG = "Navigation"

    /**
     * Logs the creation of a screen fragment associated with a specific screen type.
     *
     * @param fragment The [Fragment] instance that was created.
     * @param typeId The unique [String] identifier representing the screen type associated with the fragment.
     */
    fun logScreenFragmentCreated(
        fragment: Fragment,
        typeId: String,
    ) {
        Log.d(TAG, "Screen fragment ${fragment::class.simpleName} successfully created for screen type $typeId.")
    }

    /**
     * Logs the creation of an overlay fragment (e.g., a dialog) associated with a specific overlay type.
     *
     * @param dialogFragment The [DialogFragment] instance that was created.
     * @param typeId The unique [String] identifier representing the overlay type associated with the dialog fragment.
     */
    fun logOverlayFragmentCreated(
        dialogFragment: DialogFragment,
        typeId: String,
    ) {
        Log.d(
            TAG,
            "Overlay dialog fragment ${dialogFragment::class.simpleName} successfully created for overlay type $typeId.",
        )
    }

    /**
     * Logs a situation where the overlay state is null, but a [DialogFragment] exists.
     * This typically indicates that the dialog should be dismissed to maintain consistency.
     */
    fun logOverlayStateNullButDialogFragmentExists() {
        Log.d(TAG, "Overlay state is null, but a DialogFragment exists. Initiating dismissal of the DialogFragment.")
    }

    /**
     * Logs a situation where an overlay state exists, but the associated [DialogFragment] is of a different type.
     * This typically indicates that the existing dialog fragment should be replaced with the correct one.
     *
     * @param newTypeId The unique [String] identifier representing the overlay type in the state.
     * @param currentTypeId The unique [String] identifier of the current dialog fragment's overlay type, or null if unavailable.
     */
    fun logOverlayStateExistsButDialogFragmentMismatch(
        newTypeId: String,
        currentTypeId: String?,
    ) {
        Log.d(
            TAG,
            "Overlay state for $newTypeId exists, but the current DialogFragment is for $currentTypeId. Replacing with the correct DialogFragment.",
        )
    }
}
