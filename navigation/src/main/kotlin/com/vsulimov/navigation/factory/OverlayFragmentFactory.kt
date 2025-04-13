package com.vsulimov.navigation.factory

import androidx.fragment.app.DialogFragment
import com.vsulimov.navigation.state.NavigationComponent

/**
 * Factory interface for creating overlay dialog fragments based on overlay states.
 * The host application must implement this to provide dialog fragments for each overlay type.
 *
 * @param Overlay The type representing overlay states, must implement [NavigationComponent].
 */
interface OverlayFragmentFactory<Overlay : NavigationComponent> {
    /**
     * Creates a dialog fragment for the given overlay state.
     * The implementation should store the overlay's typeId in the fragment's arguments.
     *
     * @param overlay The overlay state for which to create a dialog fragment.
     * @return A DialogFragment instance representing the overlay.
     */
    fun createOverlayFragment(overlay: Overlay): DialogFragment

    /**
     * Retrieves the type identifier of the overlay state associated with the given dialog fragment.
     * Typically, this is retrieved from the dialog fragment's arguments.
     *
     * @param dialogFragment The dialog fragment whose corresponding overlay state typeId is to be retrieved.
     * @return The typeId of the overlay state associated with the dialog fragment.
     */
    fun getStateTypeIdForOverlay(dialogFragment: DialogFragment): String
}
