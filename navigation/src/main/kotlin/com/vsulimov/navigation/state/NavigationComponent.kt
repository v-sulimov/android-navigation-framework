package com.vsulimov.navigation.state

/**
 * Interface for navigation states, providing a unique type identifier.
 * Both Screen and Overlay states must implement this interface.
 */
interface NavigationComponent {
    /**
     * A unique identifier for the type of this navigation state.
     * Must be unique across all screen or overlay types.
     */
    val typeId: String
}
