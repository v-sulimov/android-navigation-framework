package com.vsulimov.navigation.fragment.screen

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.vsulimov.navigation.R

/**
 * An abstract base Fragment class for screen fragments that use a specified layout.
 * This class provides a foundation for initializing views, setting click listeners, applying window insets,
 * and managing screen-specific middlewares. Subclasses must implement [findViewsById] and [applyWindowInsets],
 * and may override [setOnClickListeners] to define custom click behavior.
 *
 * @param contentLayoutId The resource ID of the layout to be inflated for this fragment.
 * @constructor Creates an instance of [AbstractScreenFragment] with the specified layout resource ID.
 */
abstract class AbstractScreenFragment(
    @LayoutRes val contentLayoutId: Int,
) : Fragment(contentLayoutId) {
    /**
     * Called after the fragment's view is created. Initializes the fragment by:
     * - Locating views using [findViewsById].
     * - Setting click listeners using [setOnClickListeners].
     * - Applying window insets using [applyWindowInsets].
     * - Adding screen-specific middlewares.
     *
     * @param view The root view of the fragment's layout.
     * @param savedInstanceState If non-null, the fragment is being re-constructed from a previous saved state.
     */
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            addScreenMiddlewares()
        }
        findViewsById(view)
        applyWindowInsets()
        setOnClickListeners()
    }

    /**
     * Called when the fragment's view is being destroyed. Cleans up by:
     * - Removing screen-specific middlewares added in [onViewCreated].
     */
    override fun onDestroyView() {
        super.onDestroyView()
        if (isRemoving || activity?.isFinishing == true) {
            removeScreenMiddlewares()
        }
    }

    /**
     * Abstract method to locate and initialize views from the fragment's layout.
     * Subclasses must implement this to find views (e.g., using [View.findViewById]).
     *
     * @param view The root view of the fragment's layout.
     */
    abstract fun findViewsById(view: View)

    /**
     * Abstract method to apply window insets to the fragment's views.
     * Subclasses must implement this to handle system window insets (e.g., status bar, navigation bar).
     */
    abstract fun applyWindowInsets()

    /**
     * Applies default window insets to the specified root layout by adjusting its padding.
     * Configures a listener to handle system bars and display cutout insets, updating the [rootLayout]'s
     * padding to accommodate these insets while adding an optional uniform padding value.
     *
     * @param rootLayout The root [ViewGroup] whose padding will be adjusted based on the insets.
     * @param additionalPaddingResId The resource ID for additional padding to apply to all sides.
     *        Defaults to [R.dimen.default_padding].
     * @param typeMask The types of insets to apply (e.g., system bars or display cutouts).
     *        Defaults to [WindowInsetsCompat.Type.systemBars] or [WindowInsetsCompat.Type.displayCutout].
     * @return [WindowInsetsCompat.CONSUMED] to indicate that the insets have been fully processed.
     */
    public fun applyDefaultInsets(
        rootLayout: ViewGroup,
        @DimenRes additionalPaddingResId: Int = R.dimen.default_padding,
        typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(typeMask)
            val additionalPadding = resources.getDimensionPixelSize(additionalPaddingResId)
            view.updatePadding(
                top = insets.top + additionalPadding,
                bottom = insets.bottom + additionalPadding,
                left = insets.left + additionalPadding,
                right = insets.right + additionalPadding,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Displays the soft keyboard for the specified view.
     * Requests the system to show the soft input keyboard, typically for a focused input field.
     *
     * @param view The view (e.g., an [EditText]) that should receive focus and trigger the keyboard.
     */
    public fun showKeyboard(view: View) {
        val inputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, SHOW_IMPLICIT)
    }

    /**
     * Hides the soft keyboard if it is currently visible.
     * Requests the system to hide the soft input keyboard associated with the specified view.
     *
     * @param view The view whose window token is used to identify the keyboard to hide.
     */
    public fun hideKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, HIDE_IMPLICIT_ONLY)
    }

    /**
     * Open method to set click listeners for views in the fragment.
     * Subclasses can override this to define click listener behavior. The default implementation is empty.
     */
    open fun setOnClickListeners() {}

    /**
     * Adds screen-specific middlewares for the fragment.
     * Subclasses can override this to provide custom middleware initialization logic.
     * The default implementation is empty.
     */
    open fun addScreenMiddlewares() {}

    /**
     * Removes screen-specific middlewares for the fragment.
     * Subclasses can override this to clean up middlewares added in [addScreenMiddlewares],
     * typically to prevent memory leaks when the fragment is being removed.
     * The default implementation is empty.
     */
    open fun removeScreenMiddlewares() {}
}
