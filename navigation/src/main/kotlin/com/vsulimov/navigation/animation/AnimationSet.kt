package com.vsulimov.navigation.animation

import com.vsulimov.navigation.R

/**
 * Defines a set of animation resource IDs for screen transitions.
 * These animations are applied during fragment replacements.
 *
 * @property forwardEnter Animation resource ID for the entering fragment during forward navigation.
 * @property forwardExit Animation resource ID for the exiting fragment during forward navigation.
 * @property backwardEnter Animation resource ID for the entering fragment during backward navigation.
 * @property backwardExit Animation resource ID for the exiting fragment during backward navigation.
 * @property disableAnimations If true, disables all animations for better performance.
 */
data class AnimationSet(
    val forwardEnter: Int = R.anim.slide_in_right,
    val forwardExit: Int = R.anim.slide_out_left,
    val backwardEnter: Int = R.anim.slide_in_left,
    val backwardExit: Int = R.anim.slide_out_right,
    val disableAnimations: Boolean = false,
)
