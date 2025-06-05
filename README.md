# Android Navigation Framework

A lightweight and flexible navigation framework for Android, designed to manage screen navigation using a stack-based approach with Fragments.

## Key Features

- **Screen Navigation**: Navigate between screens with a managed back stack and customizable animations.
- **Overlay Support**: Display and dismiss overlays without affecting the underlying screen.
- **Redux Architecture**: Leverages a state management approach with a store, actions, and a reducer.
- **Lightweight**: Minimal dependencies, optimized for performance, and synchronous operations for predictable behavior.

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

Define your screen and overlay states, ensuring each has a unique `typeId`. These are typically represented as sealed classes or data classes implementing `NavigationComponent`.

```kotlin
sealed class Screen : NavigationComponent {
    object Home : Screen() {
        override val typeId: String = "home"
    }
    data class Details(val itemId: Int) : Screen() {
        override val typeId: String = "details_$itemId"
    }
}

sealed class Overlay : NavigationComponent {
    data class MessageDialog(val message: String) : Overlay() {
        override val typeId: String = "message_dialog"
    }
    data class ItemsBottomSheet(val items: List<String>) : Overlay() {
        override val typeId: String = "items_bottom_sheet"
    }
}
```

**Note**: Ensure `typeId` is unique for each screen and overlay type to prevent navigation errors.

### 2. Implement Fragment Factories

Provide implementations for `ScreenFragmentFactory` and `OverlayFragmentFactory` to create fragments based on your states.

```kotlin
class MyScreenFragmentFactory : ScreenFragmentFactory<Screen> {
    override fun createScreenFragment(screen: Screen): Fragment {
        return when (screen) {
            is Screen.Home -> HomeFragment()
            is Screen.Details -> DetailsFragment.newInstance(screen.itemId)
        }
    }

    override fun getStateTypeIdForScreen(fragment: Fragment): String {
        return when (fragment) {
            is HomeFragment -> "home"
            is DetailsFragment -> "details_${fragment.itemId}"
            else -> throw IllegalArgumentException("Unknown fragment type")
        }
    }
}

class MyOverlayFragmentFactory : OverlayFragmentFactory<Overlay> {
    override fun createOverlayFragment(overlay: Overlay): DialogFragment {
        return when (overlay) {
            is Overlay.MessageDialog -> MessageDialogFragment.newInstance(overlay.message)
            is Overlay.ItemsBottomSheet -> ItemsBottomSheetFragment.newInstance(overlay.items)
        }
    }

    override fun getStateTypeIdForOverlay(fragment: DialogFragment): String {
        return when (fragment) {
            is MessageDialogFragment -> "message_dialog"
            is ItemsBottomSheetFragment -> "items_bottom_sheet"
            else -> throw IllegalArgumentException("Unknown overlay type")
        }
    }
}
```

### 3. Attach NavigationReducer to Your Store

In your root reducer, delegate all `NavigationAction` to the `NavigationReducer`.

```kotlin
class RootReducer : Reducer<ApplicationState> {
    private val navigationReducer = NavigationReducer<Screen, Overlay>()

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

        val screenFactory = MyScreenFragmentFactory()
        val overlayFactory = MyOverlayFragmentFactory()
        navigationController = NavigationController(
            dispatchFunction = { dispatch(it) },
            navigationStateFlow = getStateFlow().map { it.navigationState },
            activity = this,
            screenFragmentFactory = screenFactory,
            overlayFragmentFactory = overlayFactory,
            containerId = R.id.fragment_container,
            defaultAnimations = AnimationSet(
                forwardEnter = R.anim.enter_from_right,
                forwardExit = R.anim.exit_to_left,
                backwardEnter = R.anim.enter_from_left,
                backwardExit = R.anim.exit_to_right
            )
        )
        
        navigationController.init()
    }
}
```

**Note**: Ensure your layout contains a container for fragments, e.g., `<FrameLayout android:id="@+id/fragment_container" ... />`.

### 5. Implement Fragments

Screen fragments should observe the state and update their UI. Overlay fragments should dispatch `DismissOverlay` when dismissed.

```kotlin
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

- **Unique Type IDs**: Maintain a registry or map in your factories to ensure `typeId` uniqueness for screens and overlays.
- **State Observation**: Use `distinctUntilChanged` in fragments to avoid redundant UI updates.
- **Testing**: Write unit tests for navigation flows and use Android Studio's Profiler to verify performance.
- **Synchronous Operations**: Leverage the framework's synchronous fragment transactions for predictable UI updates.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
