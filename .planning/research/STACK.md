# Stack Research - QuickFlip Android App

**Research Date:** 2026-01-31
**Project:** QuickFlip - Rapid Marketplace Listing Generator
**Stack Dimension:** Android Development Stack
**Researcher:** Claude (gsd-project-researcher)

## Executive Summary

Standard 2025 Android Kotlin/Jetpack Compose stack for camera-based AI app with offline capabilities:
- **UI:** Jetpack Compose with Material 3
- **Architecture:** MVVM + Clean Architecture principles
- **Camera:** CameraX 1.3.x
- **Database:** Room 2.6.x + DataStore 1.0.x
- **AI Integration:** Google Generative AI SDK 0.2.x+
- **Offline Queue:** WorkManager 2.9.x
- **DI:** Hilt 2.48+
- **Async:** Kotlin Coroutines + Flow
- **Image Loading:** Coil 2.5.x

**Confidence Level:** High (90%) - Standard, well-established stack for production Android apps

---

## Core Stack Components

### 1. UI Layer

#### Jetpack Compose
**Recommended Version:**
```gradle
androidx.compose.ui:ui:1.6.0
androidx.compose.material3:material3:1.2.0
androidx.activity:activity-compose:1.8.2
```

**Rationale:**
- Material 3 provides modern design system aligned with Android 14+ guidelines
- Declarative UI simplifies camera preview integration and state management
- Better performance than XML layouts for dynamic content (listing previews)
- Version 1.6.0 includes stability improvements and better tooling support

**Confidence:** High (95%)

**Alternatives NOT Recommended:**
- ❌ XML Layouts - Legacy approach, harder to maintain dynamic UI states
- ❌ Jetpack Compose 1.5.x or lower - Missing key stability fixes

---

#### Navigation
**Recommended Version:**
```gradle
androidx.navigation:navigation-compose:2.7.7
```

**Rationale:**
- Type-safe navigation for Compose screens
- Deep linking support for future features
- Version 2.7.7 includes better state restoration after process death
- Essential for camera → preview → editing → listing flow

**Confidence:** High (95%)

**Alternatives:**
- ⚠️ Compose Destinations - Third-party library, adds build complexity
- ⚠️ Manual navigation - Error-prone, hard to test

---

### 2. Architecture Layer

#### Dependency Injection - Hilt
**Recommended Version:**
```gradle
com.google.dagger:hilt-android:2.48
androidx.hilt:hilt-navigation-compose:1.1.0
androidx.hilt:hilt-work:1.1.0
```

**Rationale:**
- Standard DI solution for Android, built on Dagger
- Excellent integration with WorkManager (offline queue requirement)
- Scoped ViewModels work seamlessly with Compose
- Simplifies testing by injecting mock repositories
- Version 2.48+ includes Kotlin 1.9+ support

**Confidence:** High (90%)

**Alternatives NOT Recommended:**
- ❌ Koin - Runtime DI, misses compile-time validation
- ❌ Manual DI - Unsustainable for camera + AI + database complexity
- ❌ Dagger 2 (without Hilt) - Excessive boilerplate

---

#### ViewModel & Lifecycle
**Recommended Version:**
```gradle
androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0
androidx.lifecycle:lifecycle-runtime-compose:2.7.0
```

**Rationale:**
- Survives configuration changes (critical for camera state)
- Lifecycle-aware coroutine scopes prevent memory leaks
- Version 2.7.0 adds `collectAsStateWithLifecycle()` for safer Flow collection
- MVVM pattern keeps business logic testable

**Confidence:** High (95%)

---

### 3. Camera Layer

#### CameraX
**Recommended Version:**
```gradle
androidx.camera:camera-camera2:1.3.1
androidx.camera:camera-lifecycle:1.3.1
androidx.camera:camera-view:1.3.1
```

**Rationale:**
- Abstracts Camera2 API complexity while maintaining control
- Built-in lifecycle awareness prevents camera lock issues
- ImageCapture use case perfect for marketplace photos
- Version 1.3.1 stable as of late 2024, good vendor support
- Handles device fragmentation better than Camera2 directly

**Confidence:** High (90%)

**What NOT to Use:**
- ❌ Camera2 API directly - Too low-level, fragmentation nightmare
- ❌ Deprecated Camera API - Removed in newer Android versions
- ❌ CameraX 1.4.0-alpha - Unstable, potential breaking changes

**Implementation Notes:**
- Use `PreviewView` for camera preview in Compose via `AndroidView`
- ImageCapture.takePicture() saves to app-specific directory (privacy compliant)
- Consider ImageAnalysis use case if adding real-time quality hints later

---

### 4. Data Persistence Layer

#### Room Database
**Recommended Version:**
```gradle
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
kapt("androidx.room:room-compiler:2.6.1")
```

**Rationale:**
- SQLite abstraction with compile-time query validation
- Kotlin Coroutines + Flow support for reactive data
- Perfect for storing listing drafts, photo metadata, queue status
- Version 2.6.1 includes multimap query support and stability fixes
- Local-only storage aligns with privacy requirements

**Confidence:** High (95%)

**Schema Design Recommendations:**
- `listings` table: id, title, description_formal, description_casual, created_at
- `photos` table: id, listing_id, file_path, gemini_feedback, upload_status
- `queue_items` table: id, type (photo_analysis/listing_gen), status, retry_count

**What NOT to Use:**
- ❌ Raw SQLite - No type safety, error-prone
- ❌ Realm - Moving away from Android, uncertain future
- ❌ SQLDelight - Less ecosystem support than Room

---

#### DataStore (Preferences)
**Recommended Version:**
```gradle
androidx.datastore:datastore-preferences:1.0.0
```

**Rationale:**
- Replaces SharedPreferences with type-safe, async API
- Perfect for app settings (API keys, camera preferences, default listing format)
- Coroutines-based, no UI blocking
- Version 1.0.0 is stable and production-ready

**Confidence:** High (95%)

**Use Cases:**
- Gemini API key storage (encrypted with EncryptedSharedPreferences wrapper)
- User preferences: default marketplace, photo quality threshold
- Onboarding state, feature flags

**What NOT to Use:**
- ❌ SharedPreferences - Synchronous, prone to data loss
- ❌ Proto DataStore - Overkill for simple key-value preferences

---

### 5. AI Integration Layer

#### Google Generative AI SDK
**Recommended Version:**
```gradle
com.google.ai.client.generativeai:generativeai:0.2.0
```

**Rationale:**
- Official Kotlin SDK for Gemini API
- Multimodal support (text + images) for photo quality analysis
- Structured output support for generating listing descriptions
- Version 0.2.0 includes streaming support and better error handling

**Confidence:** Medium (70%) - SDK is relatively new, may see breaking changes

**Implementation Pattern:**
```kotlin
val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash", // Fast for photo feedback
    apiKey = BuildConfig.GEMINI_API_KEY
)
```

**What to Watch:**
- ⚠️ SDK evolving rapidly - pin version, monitor release notes
- ⚠️ Consider abstraction layer (repository pattern) for easier migration
- ⚠️ Rate limiting and quota management essential

**Alternatives:**
- ⚠️ Vertex AI SDK - Requires Google Cloud setup, overkill for MVP
- ❌ Direct REST API calls - Reinventing the wheel, no type safety

---

### 6. Offline Queue Layer

#### WorkManager
**Recommended Version:**
```gradle
androidx.work:work-runtime-ktx:2.9.0
```

**Rationale:**
- **THE** standard solution for deferrable, guaranteed background work
- Handles offline → online transitions automatically
- Respects battery optimization and Doze mode
- Constraint-based execution (network required, battery not low)
- Integrates with Hilt for dependency injection
- Version 2.9.0 includes improved coroutine support

**Confidence:** High (95%)

**Use Cases for QuickFlip:**
- Queue Gemini Vision API calls when offline
- Retry failed API requests with exponential backoff
- Batch processing of multiple photos

**Implementation Pattern:**
```kotlin
OneTimeWorkRequestBuilder<PhotoAnalysisWorker>()
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .build()
```

**What NOT to Use:**
- ❌ JobScheduler - Lower-level API, WorkManager wraps it
- ❌ AlarmManager - Not for network-dependent tasks
- ❌ Foreground Services - Overkill, drains battery, poor UX
- ❌ Manual queue with BroadcastReceiver - Fragile, hard to maintain

---

### 7. Image Loading Layer

#### Coil
**Recommended Version:**
```gradle
io.coil-kt:coil-compose:2.5.0
```

**Rationale:**
- Kotlin-first library with excellent Compose integration
- Smaller APK size than Glide/Picasso
- Built-in support for memory/disk caching (important for photo gallery)
- Coroutines-based, non-blocking
- Version 2.5.0 includes improved GIF support and better error handling

**Confidence:** High (90%)

**Use Cases:**
- Display captured photos in listing editor
- Thumbnail generation for photo gallery
- Efficient loading of large camera images

**Alternatives:**
- ⚠️ Glide - Mature but Java-centric, larger APK
- ⚠️ Picasso - Maintenance mode, not recommended for new projects

---

### 8. Concurrency Layer

#### Kotlin Coroutines + Flow
**Recommended Version:**
```gradle
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
```

**Rationale:**
- Standard for asynchronous programming in Kotlin
- Structured concurrency prevents leaks
- Flow provides reactive streams for Room queries and UI state
- Version 1.7.3 stable, works with Compose lifecycle

**Confidence:** High (95%)

**Patterns for QuickFlip:**
- `viewModelScope.launch` for one-time operations (save listing)
- `StateFlow/SharedFlow` for UI state (camera preview state, queue status)
- `Flow` from Room for reactive database queries
- `withContext(Dispatchers.IO)` for file operations

**What NOT to Use:**
- ❌ RxJava - Legacy approach, Kotlin Coroutines is the standard
- ❌ Callbacks - Callback hell, hard to test

---

## Additional Libraries

### Testing
**Recommended:**
```gradle
// Unit testing
junit:junit:4.13.2
org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3
app.cash.turbine:turbine:1.0.0 // Flow testing

// Android testing
androidx.test.ext:junit:1.1.5
androidx.test.espresso:espresso-core:3.5.1
androidx.compose.ui:ui-test-junit4:1.6.0
```

**Rationale:**
- Turbine simplifies Flow testing in ViewModels
- Compose UI testing for camera preview and listing editor
- Coroutines test dispatcher for time manipulation

**Confidence:** High (90%)

---

### Serialization (for API responses)
**Recommended:**
```gradle
org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0
```

**Rationale:**
- Kotlin-native serialization for Gemini API JSON responses
- Type-safe, integrates with data classes
- Lighter than Gson/Moshi

**Confidence:** Medium (75%)

**Alternative:**
- ⚠️ Moshi - Good choice if already using Retrofit extensively

---

### Build Configuration
**Recommended:**
```gradle
// build.gradle.kts (project)
kotlin("android") version "1.9.20"
kotlin("plugin.serialization") version "1.9.20"
com.google.dagger.hilt.android version "2.48"

// build.gradle.kts (app)
compileSdk = 34
targetSdk = 34
minSdk = 26

kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
}
```

**Rationale:**
- Kotlin 1.9.20+ required for Compose 1.6.0
- JVM target 17 is Android Studio default for new projects
- Min SDK 26 (Android 8.0) covers 95%+ devices while supporting modern APIs

**Confidence:** High (90%)

---

## Architecture Recommendations

### Layer Structure
```
QuickFlip/
├── data/
│   ├── local/
│   │   ├── room/ (entities, DAOs, database)
│   │   └── datastore/ (preferences)
│   ├── remote/
│   │   └── gemini/ (API client)
│   ├── repository/ (single source of truth)
│   └── worker/ (WorkManager workers)
├── domain/
│   ├── model/ (business entities)
│   └── usecase/ (business logic)
└── ui/
    ├── camera/
    ├── editor/
    ├── gallery/
    └── theme/ (Material 3 theme)
```

**Rationale:**
- Clear separation of concerns
- Easy to test (mock repositories in ViewModel tests)
- Scales well as features grow

**Confidence:** High (90%)

---

### State Management Pattern
**Recommended:**
```kotlin
data class ListingEditorState(
    val photos: List<Photo> = emptyList(),
    val formalDescription: String = "",
    val casualDescription: String = "",
    val isGenerating: Boolean = false,
    val error: String? = null
)

class ListingEditorViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val listingRepository: ListingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ListingEditorState())
    val state = _state.asStateFlow()

    // Single direction data flow
}
```

**Rationale:**
- Unidirectional data flow (UDF) is Compose best practice
- Single state object simplifies Compose recomposition
- Easy to save/restore state during process death

**Confidence:** High (95%)

---

## Security Considerations

### API Key Management
**Recommended Approach:**
```gradle
// local.properties (gitignored)
GEMINI_API_KEY=your_key_here

// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\"")
    }
}
```

**Additional Layer (for production):**
```gradle
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

**Rationale:**
- BuildConfig prevents hardcoding keys
- EncryptedSharedPreferences protects keys at rest
- Consider backend proxy for production to avoid client-side keys

**Confidence:** Medium (75%) - Client-side API keys have inherent risks

---

## What NOT to Use (Anti-Patterns)

### Avoid These Libraries/Patterns
1. ❌ **LiveData for new code** - Use StateFlow instead (better Kotlin support)
2. ❌ **RxJava** - Coroutines are the standard, smaller learning curve
3. ❌ **Retrofit** - Not needed; Gemini SDK handles HTTP, Room handles local data
4. ❌ **Firebase Realtime Database** - Project is local-only
5. ❌ **Jetpack DataBinding** - Deprecated in favor of Compose
6. ❌ **Fragments** - Use Compose Navigation instead
7. ❌ **Custom AsyncTask/Thread management** - Use Coroutines
8. ❌ **EventBus/Otto** - Use StateFlow/SharedFlow for event handling

---

## Version Upgrade Strategy

### When to Upgrade
- **Patch versions** (x.y.Z): Upgrade immediately (bug fixes)
- **Minor versions** (x.Y.0): Upgrade within 2-4 weeks (features + fixes)
- **Major versions** (X.0.0): Evaluate breaking changes, upgrade within 1-3 months

### Monitoring
- Follow [Android Developers Blog](https://android-developers.googleblog.com/)
- Check [Jetpack release notes](https://developer.android.com/jetpack/androidx/versions)
- Monitor Gemini SDK updates closely (0.x.x = unstable API)

---

## Risk Assessment

### High Confidence (90%+)
- Room, DataStore, WorkManager, CameraX, Compose, Hilt, Coroutines
- These are stable, production-proven, won't change significantly

### Medium Confidence (70-89%)
- Gemini SDK version 0.2.0 - Still evolving, expect API changes
- Specific library versions - May have newer releases by implementation time

### Low Confidence (<70%)
- None for core stack - all recommendations are industry standard

---

## Next Steps (for Roadmap Creation)

1. **Validate versions** - Check Maven Central / Google Maven for latest stable releases
2. **Prototype camera + Gemini integration** - Validate SDK compatibility
3. **Set up Hilt DI** - Foundation for testability
4. **Design Room schema** - Model entities for listings, photos, queue
5. **Implement WorkManager offline queue** - Critical for offline-first approach

---

## References

- [Android Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [CameraX Developer Guide](https://developer.android.com/training/camerax)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Google Generative AI SDK](https://ai.google.dev/tutorials/android_quickstart)
- [Now in Android Sample App](https://github.com/android/nowinandroid) - Reference architecture

---

**Last Updated:** 2026-01-31
**Review Date:** Before roadmap creation (validate versions with web search)
**Confidence:** Overall High (85%) - Standard stack with one evolving component (Gemini SDK)
