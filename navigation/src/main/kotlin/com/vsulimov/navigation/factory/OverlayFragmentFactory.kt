package com.vsulimov.navigation.factory

import androidx.fragment.app.DialogFragment

/**
 * Factory interface for creating overlay dialog fragments based on overlay states.
 * The host application must implement this to provide dialog fragments for each overlay type.
 *
 * @param Overlay The type representing overlay states, defined by the host application.
 */
interface OverlayFragmentFactory<Overlay> {
    /**
     * Creates a dialog fragment for the given overlay state.
     *
     * @param overlay The overlay state for which to create a dialog fragment.
     * @return A DialogFragment instance representing the overlay.
     */
    fun createOverlayFragment(overlay: Overlay): DialogFragment
}
