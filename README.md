# Android Navigation Framework

A lightweight and flexible navigation framework for Android, designed to manage screen navigation using a stack-based approach with Fragments.

## Key Features

- **Screen Navigation**: Navigate between screens with a managed back stack and customizable animations.
- **Overlay Support**: Display and dismiss overlays without affecting the underlying screen.
- **Redux Architecture**: Leverages a state management approach with a store, actions, and a reducer.
- **Lightweight**: Minimal dependencies and optimized for performance.

## Installation

To add Navigation Framework to your project, include the following repository in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Other repositories here.
        maven {
            name = "vsulimovRepositoryReleases"
            url = uri("https://maven.vsulimov.com/releases")
        }
    }
}
```

Then, include the following dependencies in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.vsulimov:navigation:1.0.0")
}
```

## Getting Started

### 1. Define Screen and Overlay States

Your application needs to define its own screen and overlay states. These are typically represented as sealed classes or
data classes.

```kotlin
// Example Screen states
sealed class Screen {
    object Home : Screen()
    data class Details(val itemId: Int) : Screen()
}

// Example Overlay states
sealed class Overlay {
    data class MessageDialog(val message: String) : Overlay()
    data class ItemsBottomSheet(val items: List<String>) : Overlay()
}
```

### 2. Implement Fragment Factories

You must provide implementations for `ScreenFragmentFactory` and `OverlayFragmentFactory` to create fragments based on
your screen and overlay states.

```kotlin
// Factory for screen fragments
class MyScreenFragmentFactory : ScreenFragmentFactory<Screen> {
    override fun createScreenFragment(screen: Screen): Fragment {
        return when (screen) {
            is Screen.Home -> HomeFragment()
            is Screen.Details -> DetailsFragment.newInstance(screen.itemId)
        }
    }
}

// Factory for overlay fragments
class MyOverlayFragmentFactory : OverlayFragmentFactory<Overlay> {
    override fun createOverlayFragment(overlay: Overlay): DialogFragment {
        return when (overlay) {
            is Overlay.MessageDialog -> MessageDialogFragment.newInstance(overlay.message)
            is Overlay.ItemsBottomSheet -> ItemsBottomSheetFragment.newInstance(overlay.items)
        }
    }
}
```

### 3. Attach NavigationReducer to your store

In your root reducer, delegate all `NavigationAction` to the `NavigationReducer`.

```kotlin
// Example root reducer implementation
class RootReducer : Reducer<ApplicationState> {

    private val navigationReducer = NavigationReducer<ScreenState, OverlayState>()

    override fun reduce(
        action: Action,
        state: ApplicationState
    ): ApplicationState {
        return when (action) {
            is NavigationAction -> {
                val navigationState = state.navigationState
                val reducedNavigationState = navigationReducer.reduce(action, navigationState)
                state.copy(navigationState = reducedNavigationState)
            }

            else -> state
        }
    }
}
```

### 4. Initialize the Navigation Controller

In your main activity, set up the `NavigationController` and call `init()` to start observing the state.

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var navigationController: NavigationController<Screen, Overlay>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val screenFactory = ScreenFragmentFactory()
        val overlayFactory = OverlayFragmentFactory()
        navigationController = NavigationController(
            dispatchFunction = { dispatch(it) },
            navigationStateFlow = getStateFlow().map { it.navigationState },
            activity = this,
            screenFragmentFactory = screenFactory,
            overlayFragmentFactory = overlayFactory,
            containerId = R.id.container
        )
        
        navigationController.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigationController.cleanup()
    }
}
```

**Note**: Ensure your layout contains a container for fragments, e.g.,
`<FrameLayout android:id="@+id/fragment_container" ... />`.

### 5. Implement Fragments

Your screen fragments should observe the state and update their UI accordingly. Overlay fragments should dispatch
`DismissOverlay` when dismissed.

```kotlin
// Example overlay fragment
class MessageDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(message: String) = MessageDialogFragment().apply {
            arguments = Bundle().apply { putString("message", message) }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        store.dispatch(NavigationAction.DismissOverlay)
    }
}
```

### 6. Dispatch Navigation Actions

Use the store to dispatch actions for navigation and overlay management.

```kotlin
// Navigate to a new screen
store.dispatch(NavigationAction.NavigateTo(Screen.Details(itemId = 1)))

// Show an overlay
store.dispatch(NavigationAction.ShowOverlay(Overlay.MessageDialog("Hello")))

// Go back (dismisses overlay or pops back stack)
store.dispatch(NavigationAction.GoBack)
```

## Best Practices

- **State Observation**: Use `distinctUntilChanged` in fragments to avoid redundant UI updates.
- **Memory Management**: Always call `navigationController.cleanup()` in `onDestroy` to release resources.
- **Testing**: Write unit tests for your navigation flows and use Android Studio's Profiler to optimize performance.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
