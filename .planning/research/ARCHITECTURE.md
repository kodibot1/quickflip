# Architecture Research: QuickFlip Android MVVM

**Research Date:** 2026-01-31
**Dimension:** Architecture
**Project:** QuickFlip - Android marketplace listing generator

---

## Pattern Overview

**Overall:** Android MVVM (Model-View-ViewModel) with clean architecture principles, using Jetpack Compose for UI, CameraX for photo capture, Gemini Vision API for AI processing, Room for local persistence, and WorkManager for offline queue handling.

**Key Characteristics:**
- Unidirectional data flow (ViewModel → UI via StateFlow/Flow)
- Clear separation of concerns across layers
- Repository pattern mediating between data sources (local/remote)
- Dependency injection for loose coupling (Hilt recommended)
- Offline-first approach with background sync
- Reactive data streams throughout the stack

**Architecture Style:** Layered + Clean Architecture variant optimized for Android

---

## Layers

### 1. Presentation Layer (UI)

**Purpose:** Render UI using Jetpack Compose, collect user interactions, observe ViewModel state

**Location:**
- `ui/theme/` - Material3 theme, colors, typography
- `ui/components/` - Reusable composables (buttons, cards, dialogs)
- `ui/home/` - Home screen composables
- `ui/camera/` - Camera preview and capture UI
- `ui/listing/` - Listing display and edit screens
- `ui/settings/` - Settings screen
- `ui/navigation/` - NavGraph, NavHost setup

**Contains:**
- Composable functions (stateless where possible)
- Screen-level composables that hoist state from ViewModels
- Navigation graph defining app flow
- UI events (sealed classes for user actions)

**Depends on:**
- ViewModels (observes StateFlow/Flow)
- Navigation component
- Material3 design system

**Used by:** Android framework (Activity/Fragment hosts)

**Key Patterns:**
- State hoisting: ViewModels hold state, Composables are stateless
- Event handling: UI emits events → ViewModel processes → updates state
- Navigation: Single NavHost with type-safe navigation arguments

---

### 2. ViewModel Layer

**Purpose:** Hold UI state, handle business logic orchestration, survive configuration changes

**Location:**
- `ui/home/HomeViewModel.kt`
- `ui/camera/CameraViewModel.kt`
- `ui/listing/ListingViewModel.kt`
- `ui/settings/SettingsViewModel.kt`

**Contains:**
- UI state as `StateFlow<T>` or `State<T>`
- Event handlers that call repository methods
- ViewModelScope for coroutine lifecycle management
- UI event sealed classes

**Pattern Example:**
```kotlin
data class ListingUiState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedListing: Listing? = null
)

class ListingViewModel(
    private val repository: ListingRepository,
    private val preferences: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListingUiState())
    val uiState: StateFlow<ListingUiState> = _uiState.asStateFlow()

    init {
        observeListings()
    }

    private fun observeListings() {
        viewModelScope.launch {
            repository.getAllListings()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collectLatest { listings ->
                    _uiState.update { it.copy(listings = listings, isLoading = false) }
                }
        }
    }

    fun generateListing(photoUris: List<Uri>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.generateListingFromPhotos(photoUris)
                .onSuccess { listing ->
                    _uiState.update { it.copy(selectedListing = listing, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
```

**Depends on:**
- Repository interfaces
- Preferences/Settings managers
- Kotlin Coroutines/Flow

**Used by:** Composable screens

---

### 3. Repository Layer

**Purpose:** Abstract data sources, coordinate between local and remote, implement business logic

**Location:**
- `data/repository/ListingRepository.kt`
- `data/repository/PhotoRepository.kt` (optional)

**Contains:**
- Single source of truth logic
- Network-to-database caching
- Offline queue management
- Data mapping (DTO → Domain model)

**Pattern Example:**
```kotlin
class ListingRepository(
    private val listingDao: ListingDao,
    private val geminiService: GeminiService,
    private val workManager: WorkManager,
    private val connectivityManager: ConnectivityManager
) {

    fun getAllListings(): Flow<List<Listing>> {
        return listingDao.getAllListingsFlow()
    }

    suspend fun generateListingFromPhotos(photoUris: List<Uri>): Result<Listing> {
        // Step 1: Check photo quality via Gemini Vision API
        val qualityFeedback = if (isNetworkAvailable()) {
            geminiService.analyzePhotoQuality(photoUris)
                .getOrElse { return Result.failure(it) }
        } else {
            // Queue for later processing
            queueQualityCheck(photoUris)
            return Result.failure(NetworkUnavailableException())
        }

        // Step 2: If quality is acceptable, generate listing
        val generatedListing = geminiService.generateMarketplaceListing(
            photos = photoUris,
            qualityFeedback = qualityFeedback
        ).getOrElse { return Result.failure(it) }

        // Step 3: Save to local database
        val listing = Listing(
            title = generatedListing.title,
            description = generatedListing.description,
            price = generatedListing.suggestedPrice,
            photoUris = photoUris.map { it.toString() },
            status = ListingStatus.DRAFT,
            createdAt = System.currentTimeMillis()
        )

        val listingId = listingDao.insert(listing)
        return Result.success(listing.copy(id = listingId))
    }

    private fun queueQualityCheck(photoUris: List<Uri>) {
        val workRequest = OneTimeWorkRequestBuilder<PhotoQualityWorker>()
            .setInputData(workDataOf("photoUris" to photoUris.map { it.toString() }.toTypedArray()))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(workRequest)
    }

    suspend fun copyListingToClipboard(listing: Listing): Result<Unit> {
        // Format listing for clipboard
        val formattedText = buildString {
            appendLine(listing.title)
            appendLine()
            appendLine(listing.description)
            appendLine()
            appendLine("Price: $${listing.price}")
        }

        return clipboardManager.copyText(formattedText)
    }

    private fun isNetworkAvailable(): Boolean {
        // Check network connectivity
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
```

**Depends on:**
- DAO interfaces (Room)
- Network service interfaces (Retrofit/Ktor)
- WorkManager for background tasks
- System services (ConnectivityManager, ClipboardManager)

**Used by:** ViewModels

---

### 4. Data Layer - Local (Room Database)

**Purpose:** Persist listings locally, provide reactive data streams, support offline-first

**Location:**
- `data/local/AppDatabase.kt`
- `data/local/ListingDao.kt`
- `data/local/Converters.kt`

**Contains:**
- Room database singleton
- DAO interfaces with Flow-based queries
- Type converters for complex types (List<String>, enums)
- Migration strategies

**Pattern Example:**
```kotlin
@Database(
    entities = [Listing::class, QueuedRequest::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
    abstract fun queuedRequestDao(): QueuedRequestDao
}

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListingsFlow(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getListingById(id: Long): Listing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: Listing): Long

    @Update
    suspend fun update(listing: Listing)

    @Delete
    suspend fun delete(listing: Listing)

    @Query("DELETE FROM listings")
    suspend fun deleteAll()
}

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(",") ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun fromListingStatus(status: ListingStatus): String = status.name

    @TypeConverter
    fun toListingStatus(value: String): ListingStatus = ListingStatus.valueOf(value)
}
```

**Depends on:**
- Room library
- Kotlin Coroutines Flow

**Used by:** Repository layer

---

### 5. Data Layer - Remote (Network)

**Purpose:** Communicate with Gemini Vision API for photo analysis and listing generation

**Location:**
- `network/GeminiService.kt`
- `network/GeminiModels.kt`
- `network/NetworkModule.kt` (DI setup)

**Contains:**
- API interface definitions
- Request/Response DTOs
- Retrofit/Ktor HTTP client configuration
- Error handling and retry logic

**Pattern Example:**
```kotlin
interface GeminiService {
    suspend fun analyzePhotoQuality(photos: List<Uri>): Result<PhotoQualityResponse>
    suspend fun generateMarketplaceListing(
        photos: List<Uri>,
        qualityFeedback: PhotoQualityResponse
    ): Result<GeneratedListing>
}

class GeminiServiceImpl(
    private val apiKey: String,
    private val httpClient: HttpClient
) : GeminiService {

    override suspend fun analyzePhotoQuality(photos: List<Uri>): Result<PhotoQualityResponse> {
        return try {
            val encodedPhotos = photos.map { encodeImageToBase64(it) }

            val request = GeminiRequest(
                model = "gemini-pro-vision",
                messages = listOf(
                    Message(
                        role = "user",
                        content = buildPrompt(
                            "Analyze these product photos for marketplace listing quality. " +
                            "Rate lighting, clarity, background, and suggest improvements.",
                            encodedPhotos
                        )
                    )
                )
            )

            val response: GeminiResponse = httpClient.post("https://generativelanguage.googleapis.com/v1/models/gemini-pro-vision:generateContent") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            Result.success(parseQualityResponse(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateMarketplaceListing(
        photos: List<Uri>,
        qualityFeedback: PhotoQualityResponse
    ): Result<GeneratedListing> {
        return try {
            val encodedPhotos = photos.map { encodeImageToBase64(it) }

            val prompt = """
                Generate a compelling marketplace listing for this product.

                Photo quality assessment: ${qualityFeedback.summary}

                Provide:
                - Catchy title (max 80 chars)
                - Detailed description highlighting features and condition
                - Suggested price range based on visible condition
                - Relevant keywords/tags
            """.trimIndent()

            val request = GeminiRequest(
                model = "gemini-pro-vision",
                messages = listOf(
                    Message(role = "user", content = buildPrompt(prompt, encodedPhotos))
                )
            )

            val response: GeminiResponse = httpClient.post("https://generativelanguage.googleapis.com/v1/models/gemini-pro-vision:generateContent") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            Result.success(parseListingResponse(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class GeneratedListing(
    val title: String,
    val description: String,
    val suggestedPrice: Double,
    val keywords: List<String>
)
```

**Depends on:**
- HTTP client (Ktor/Retrofit)
- JSON serialization (kotlinx.serialization/Gson)
- Coroutines for async operations

**Used by:** Repository layer

---

### 6. Background Processing Layer (WorkManager)

**Purpose:** Handle offline queue, retry failed network requests, sync when network returns

**Location:**
- `workers/PhotoQualityWorker.kt`
- `workers/ListingGenerationWorker.kt`
- `data/local/QueuedRequestDao.kt`

**Contains:**
- Worker classes for background tasks
- Queue management in Room database
- Retry policies and constraints

**Pattern Example:**
```kotlin
class PhotoQualityWorker(
    context: Context,
    params: WorkerParameters,
    private val geminiService: GeminiService,
    private val queuedRequestDao: QueuedRequestDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val photoUrisString = inputData.getStringArray("photoUris") ?: return Result.failure()
        val photoUris = photoUrisString.map { Uri.parse(it) }

        return when (val response = geminiService.analyzePhotoQuality(photoUris)) {
            is kotlin.Result.Success -> {
                // Update database with quality feedback
                queuedRequestDao.deleteByPhotoUris(photoUrisString.toList())
                Result.success()
            }
            is kotlin.Result.Failure -> {
                // Retry if network error, fail if API error
                if (response.exception is NetworkException) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }
}

@Entity(tableName = "queued_requests")
data class QueuedRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestType: RequestType,
    val photoUris: List<String>,
    val createdAt: Long,
    val retryCount: Int = 0
)

enum class RequestType {
    PHOTO_QUALITY_CHECK,
    LISTING_GENERATION
}
```

**Depends on:**
- WorkManager library
- Repository/Service layer
- Room database

**Used by:** Repository layer (enqueues work)

---

### 7. Camera Integration Layer (CameraX)

**Purpose:** Provide camera preview, capture photos, manage camera lifecycle

**Location:**
- `ui/camera/CameraPreview.kt`
- `ui/camera/CameraViewModel.kt`
- `util/CameraManager.kt`

**Contains:**
- CameraX use cases (Preview, ImageCapture)
- Camera permission handling
- Photo capture with URI result
- Flash/focus/zoom controls

**Pattern Example:**
```kotlin
@Composable
fun CameraPreview(
    onPhotoCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                onError(e)
            }

            previewView
        },
        modifier = modifier
    )

    // Expose capture function
    LaunchedEffect(imageCapture) {
        // Camera is ready
    }
}

fun capturePhoto(
    imageCapture: ImageCapture,
    context: Context,
    onPhotoCaptured: (Uri) -> Unit
) {
    val photoFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onPhotoCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                // Handle error
            }
        }
    )
}
```

**Depends on:**
- CameraX library
- Jetpack Compose
- Android Context/Lifecycle

**Used by:** Camera screen composables

---

### 8. Preferences/Settings Layer

**Purpose:** Store user preferences, API keys, app settings using DataStore

**Location:**
- `data/preferences/UserPreferencesManager.kt`

**Contains:**
- Preferences repository using DataStore
- Type-safe preference keys
- Flow-based reactive preferences

**Pattern Example:**
```kotlin
class UserPreferencesManager(context: Context) {

    private val dataStore = context.createDataStore(name = "user_preferences")

    private object PreferencesKeys {
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val AUTO_COPY_TO_CLIPBOARD = booleanPreferencesKey("auto_copy_to_clipboard")
        val PHOTO_QUALITY_THRESHOLD = floatPreferencesKey("photo_quality_threshold")
    }

    val geminiApiKey: Flow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.GEMINI_API_KEY] }

    suspend fun setGeminiApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_API_KEY] = apiKey
        }
    }

    val autoCopyToClipboard: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.AUTO_COPY_TO_CLIPBOARD] ?: true }

    suspend fun setAutoCopyToClipboard(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_COPY_TO_CLIPBOARD] = enabled
        }
    }
}
```

**Depends on:**
- DataStore (Preferences)
- Kotlin Flow

**Used by:** ViewModels, Repository

---

## Data Flow

### Flow 1: Camera Capture → AI Analysis → Listing Generation

**Sequence:**

1. **User opens Camera screen**
   - CameraViewModel initializes CameraX use cases
   - CameraPreview composable binds camera lifecycle
   - Preview stream displays on screen

2. **User captures photo**
   - User taps capture button
   - CameraPreview calls `imageCapture.takePicture()`
   - Photo saved to app-specific directory
   - Photo URI passed to CameraViewModel

3. **ViewModel handles captured photo**
   - CameraViewModel accumulates photo URIs in state
   - UI shows thumbnail gallery of captured photos
   - User can capture multiple photos or proceed

4. **User triggers listing generation**
   - User taps "Generate Listing" button
   - CameraViewModel calls `repository.generateListingFromPhotos(photoUris)`

5. **Repository orchestrates AI workflow**
   - Check network connectivity
   - If online:
     - Call `geminiService.analyzePhotoQuality(photoUris)`
     - Gemini Vision API analyzes lighting, clarity, background
     - Receive quality feedback
     - If quality acceptable → call `geminiService.generateMarketplaceListing(photoUris, feedback)`
     - Gemini generates title, description, price suggestion
   - If offline:
     - Enqueue `PhotoQualityWorker` with WorkManager
     - Show error to user: "No network, queued for later"
     - Return early

6. **Repository saves listing to database**
   - Map API response to `Listing` entity
   - Insert into Room database via `listingDao.insert(listing)`
   - Return success result to ViewModel

7. **ViewModel updates UI state**
   - Update state with generated listing
   - Navigate to Listing Detail screen
   - User can edit title/description before copying

**Error Handling:**
- Network timeout → Retry with exponential backoff
- API error (400/500) → Show error message, allow manual retry
- Photo quality too low → Show suggestions, allow retake

---

### Flow 2: Offline Queue Processing

**Sequence:**

1. **Network becomes unavailable during operation**
   - Repository detects `isNetworkAvailable() == false`
   - Creates `QueuedRequest` entity with photo URIs and request type
   - Saves to `queued_requests` table
   - Enqueues `PhotoQualityWorker` with network constraint

2. **WorkManager waits for network**
   - Worker remains pending until network connected
   - Constraints: `NetworkType.CONNECTED`
   - No execution until constraint met

3. **Network returns**
   - WorkManager detects network connection
   - Triggers `PhotoQualityWorker.doWork()`

4. **Worker processes queued request**
   - Fetch photo URIs from inputData
   - Call `geminiService.analyzePhotoQuality(photoUris)`
   - On success:
     - Delete `QueuedRequest` from database
     - Optionally show notification "Listing ready"
     - Return `Result.success()`
   - On failure:
     - If network error → `Result.retry()` (WorkManager retries)
     - If API error → `Result.failure()` (permanent failure)

5. **User views synced listing**
   - Navigate to Home screen
   - See new listing in list (via Flow from Room)

---

### Flow 3: Copy Listing to Clipboard

**Sequence:**

1. **User taps "Copy to Clipboard" on Listing Detail**
   - ListingDetailScreen composable calls `viewModel.copyToClipboard(listing)`

2. **ViewModel calls repository**
   - ListingViewModel invokes `repository.copyListingToClipboard(listing)`

3. **Repository formats and copies text**
   - Build formatted string: title + description + price
   - Use `ClipboardManager.setPrimaryClip()`
   - Return success result

4. **ViewModel updates UI**
   - Show Snackbar: "Copied to clipboard"
   - Optionally auto-navigate to sharing screen

---

### Flow 4: User Views All Listings (Reactive)

**Sequence:**

1. **User navigates to Home screen**
   - HomeViewModel initializes
   - `init {}` block observes `repository.getAllListings()`

2. **Repository returns Flow from DAO**
   - `listingDao.getAllListingsFlow()` emits Flow<List<Listing>>
   - Room automatically emits on database changes

3. **ViewModel collects Flow**
   - `viewModelScope.launch { repository.getAllListings().collectLatest { ... } }`
   - Updates `_uiState` with new list
   - UI automatically recomposes

4. **User creates new listing (Flow 1)**
   - New listing inserted into database
   - Room triggers Flow emission
   - HomeViewModel receives updated list
   - UI shows new listing without manual refresh

**Key Benefit:** No manual refresh needed, reactive data stream

---

## Component Boundaries

### Clear Interface Contracts

**ViewModel ↔ Repository:**
- ViewModels call suspend functions on Repository
- Repository returns `Result<T>` or `Flow<T>`
- No direct DAO/Service access from ViewModel

**Repository ↔ Data Sources:**
- Repository depends on DAO and Service interfaces (not implementations)
- Dependency injection provides concrete implementations
- Repository never exposes Room entities directly (maps to domain models if needed)

**UI ↔ ViewModel:**
- UI observes StateFlow/Flow from ViewModel
- UI calls functions on ViewModel (no direct repository access)
- UI never directly accesses database or network

**WorkManager ↔ Repository:**
- Workers receive dependencies via DI (Hilt WorkerFactory)
- Workers call repository methods like any other caller
- Workers update database to signal completion

---

## Suggested Build Order

### Phase 1: Foundation (Core Data Models + Local Persistence)

**Why First:** Establishes data contracts, enables offline-first development

**Components:**
1. Define `Listing` entity with Room annotations
2. Create `ListingDao` with CRUD operations
3. Build `AppDatabase` with initial schema
4. Add `Converters` for complex types
5. Write unit tests for DAO operations

**Dependencies:** Room, Kotlin Coroutines
**Milestone:** Can insert/query listings locally

---

### Phase 2: Repository + Preferences

**Why Second:** Centralizes data access before building UI

**Components:**
1. Create `ListingRepository` interface
2. Implement basic CRUD methods using DAO
3. Build `UserPreferencesManager` with DataStore
4. Add clipboard integration
5. Mock network calls (return hardcoded responses)

**Dependencies:** Phase 1, DataStore
**Milestone:** Repository layer functional with local data

---

### Phase 3: Basic UI + Navigation

**Why Third:** Provides visual feedback, enables testing data flow

**Components:**
1. Set up Navigation graph (Home, Camera, Listing Detail, Settings)
2. Build Home screen with listing list
3. Create Listing Detail screen (read-only)
4. Add basic theme/components
5. Wire ViewModels to screens

**Dependencies:** Phase 2, Jetpack Compose, Navigation
**Milestone:** Can navigate between screens, view local listings

---

### Phase 4: Camera Integration

**Why Fourth:** Captures photos for later AI processing

**Components:**
1. Add CameraX dependencies
2. Build `CameraPreview` composable
3. Implement photo capture with file storage
4. Create `CameraViewModel` to manage captured photos
5. Add permission handling (runtime permissions)

**Dependencies:** Phase 3, CameraX
**Milestone:** Can capture photos and preview them

---

### Phase 5: Network Layer (Gemini API)

**Why Fifth:** Enables AI-powered listing generation

**Components:**
1. Define `GeminiService` interface
2. Implement HTTP client (Ktor/Retrofit)
3. Build request/response DTOs
4. Add photo quality analysis endpoint
5. Add listing generation endpoint
6. Integrate into Repository (remove mocks)

**Dependencies:** Phase 2, HTTP client library
**Milestone:** Can call Gemini API when online

---

### Phase 6: Offline Queue (WorkManager)

**Why Sixth:** Handles network failures gracefully

**Components:**
1. Create `QueuedRequest` entity and DAO
2. Build `PhotoQualityWorker`
3. Implement queue logic in Repository
4. Add network connectivity detection
5. Set up WorkManager constraints and retry policy

**Dependencies:** Phase 5, WorkManager
**Milestone:** Failed requests retry when network returns

---

### Phase 7: End-to-End Integration

**Why Last:** Ties all components together

**Components:**
1. Wire Camera → Repository → Gemini flow
2. Add loading states and error handling
3. Implement clipboard copy on Listing Detail
4. Add user feedback (Snackbars, Dialogs)
5. Polish UI/UX

**Dependencies:** All previous phases
**Milestone:** Complete user flow works

---

### Phase 8: Testing + Polish

**Why Final:** Validates correctness, improves quality

**Components:**
1. Unit tests for Repository, ViewModel
2. Integration tests for database + API
3. UI tests with Compose Testing
4. Add analytics/crash reporting
5. Performance optimization

**Dependencies:** Phase 7
**Milestone:** Production-ready app

---

## Key Abstractions

### 1. Listing Entity

**Purpose:** Represents a marketplace listing (domain model)

**Location:** `data/model/Listing.kt` or within `data/local/` as Room entity

**Pattern:**
```kotlin
@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val price: Double,
    val photoUris: List<String>, // Stored as comma-separated via Converter
    val status: ListingStatus,
    val createdAt: Long,
    val updatedAt: Long
)

enum class ListingStatus {
    DRAFT,
    READY,
    POSTED,
    SOLD
}
```

**Lifecycle:** Created by Repository after AI generation → Saved to Room → Updated by user → Deleted when sold

---

### 2. Repository Pattern

**Purpose:** Mediate between data sources, provide single source of truth

**Examples:**
- `ListingRepository`: Coordinates Room + Gemini API
- Decides when to fetch from network vs. local cache
- Handles offline queueing

**Pattern:**
```kotlin
interface ListingRepository {
    fun getAllListings(): Flow<List<Listing>>
    suspend fun generateListingFromPhotos(photoUris: List<Uri>): Result<Listing>
    suspend fun updateListing(listing: Listing): Result<Unit>
    suspend fun deleteListing(listingId: Long): Result<Unit>
    suspend fun copyListingToClipboard(listing: Listing): Result<Unit>
}
```

**Benefits:**
- ViewModels don't know about Room or Retrofit
- Easy to swap data sources (e.g., Firebase instead of Room)
- Testable with fake implementations

---

### 3. StateFlow/Flow for Reactive UI

**Purpose:** Expose observable data streams from ViewModel to UI

**Pattern:**
- ViewModel holds `MutableStateFlow<T>` privately
- Exposes `StateFlow<T>` publicly (read-only)
- UI collects as State in Compose: `val state by viewModel.uiState.collectAsState()`

**Benefits:**
- UI automatically updates when state changes
- Survives configuration changes (ViewModel lifecycle)
- Type-safe, null-safe

---

### 4. WorkManager for Background Tasks

**Purpose:** Execute deferrable, guaranteed background work

**When to Use:**
- Offline queue processing
- Periodic data sync
- Uploading photos to cloud storage

**Pattern:**
- Define Worker class extending `CoroutineWorker`
- Enqueue work with constraints (network, battery)
- WorkManager guarantees execution even after app kill

---

### 5. Dependency Injection (Hilt)

**Purpose:** Provide dependencies without manual construction

**Pattern:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "quickflip_database"
        ).build()
    }

    @Provides
    fun provideListingDao(database: AppDatabase): ListingDao {
        return database.listingDao()
    }

    @Provides
    @Singleton
    fun provideGeminiService(
        preferences: UserPreferencesManager
    ): GeminiService {
        // Return GeminiServiceImpl with API key from preferences
    }

    @Provides
    @Singleton
    fun provideListingRepository(
        listingDao: ListingDao,
        geminiService: GeminiService,
        workManager: WorkManager
    ): ListingRepository {
        return ListingRepositoryImpl(listingDao, geminiService, workManager)
    }
}

@HiltViewModel
class ListingViewModel @Inject constructor(
    private val repository: ListingRepository,
    private val preferences: UserPreferencesManager
) : ViewModel() { ... }
```

**Benefits:**
- No manual object construction
- Easy to replace implementations for testing
- Lifecycle-aware (ViewModel scoped correctly)

---

## Entry Points

### 1. Application Entry

**Location:** `QuickFlipApplication.kt`

**Triggers:** Android app launch

**Responsibilities:**
- Initialize Hilt (`@HiltAndroidApp`)
- Set up WorkManager
- Initialize logging/analytics
- Create notification channels

**Pattern:**
```kotlin
@HiltAndroidApp
class QuickFlipApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // WorkManager auto-initializes
        // Hilt handles dependency graph
    }
}
```

---

### 2. MainActivity Entry

**Location:** `MainActivity.kt`

**Triggers:** App launch, deep link

**Responsibilities:**
- Set up Compose UI
- Define NavHost
- Handle runtime permissions
- Observe app-level state (e.g., network connectivity)

**Pattern:**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickFlipTheme {
                NavGraph()
            }
        }
    }
}
```

---

### 3. ViewModel Entry

**Location:** Each screen's ViewModel (e.g., `HomeViewModel`)

**Triggers:** First composition of associated screen

**Responsibilities:**
- Initialize state
- Start observing repository Flows
- Handle lifecycle (cleared when screen destroyed)

---

## Error Handling

### Strategy: Result-based with UI feedback

**Patterns:**

1. **Network Errors:**
   - Repository returns `Result.failure(NetworkException())`
   - ViewModel catches, updates state with error message
   - UI shows Snackbar or Dialog
   - Option to retry or queue for later

2. **API Errors (4xx/5xx):**
   - Parse error response from Gemini API
   - Return `Result.failure(ApiException(message))`
   - Show user-friendly message
   - Log full error for debugging

3. **Database Errors:**
   - Rare due to Room's built-in handling
   - Catch SQLiteException in Repository
   - Return `Result.failure()`
   - Log error, show generic message

4. **Permission Errors:**
   - Check permissions before camera access
   - If denied, show rationale dialog
   - Navigate to app settings if permanently denied

**Example:**
```kotlin
// In Repository
suspend fun generateListingFromPhotos(photoUris: List<Uri>): Result<Listing> {
    return try {
        val response = geminiService.analyzePhotoQuality(photoUris)
        when {
            response.isSuccess -> {
                // Continue with listing generation
                val listing = geminiService.generateMarketplaceListing(...)
                Result.success(listing)
            }
            response.isNetworkError -> {
                queueForLater(photoUris)
                Result.failure(NetworkUnavailableException("Queued for later"))
            }
            else -> {
                Result.failure(ApiException(response.errorMessage))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// In ViewModel
fun generateListing(photoUris: List<Uri>) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }

        repository.generateListingFromPhotos(photoUris)
            .onSuccess { listing ->
                _uiState.update { it.copy(selectedListing = listing, isLoading = false) }
                navigateToListingDetail(listing.id)
            }
            .onFailure { exception ->
                val errorMessage = when (exception) {
                    is NetworkUnavailableException -> "No internet. We'll process this when you're back online."
                    is ApiException -> "AI service error: ${exception.message}"
                    else -> "Something went wrong. Please try again."
                }
                _uiState.update { it.copy(error = errorMessage, isLoading = false) }
            }
    }
}
```

---

## Cross-Cutting Concerns

### 1. Logging

**Strategy:** Timber for structured logging

**Pattern:**
- Log errors in Repository/ViewModel
- Log network requests/responses (debug only)
- Log WorkManager execution
- No sensitive data (API keys, user info)

**Example:**
```kotlin
class ListingRepository(...) {
    suspend fun generateListingFromPhotos(photoUris: List<Uri>): Result<Listing> {
        Timber.d("Generating listing from ${photoUris.size} photos")

        return try {
            val result = geminiService.analyzePhotoQuality(photoUris)
            Timber.d("Quality analysis complete: ${result.summary}")
            // ...
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate listing")
            Result.failure(e)
        }
    }
}
```

---

### 2. Validation

**Where:**
- UI: Input validation (non-empty fields, price format)
- ViewModel: Business rules (price > 0, title length < 80 chars)
- Repository: Data integrity (photo URIs exist, API key configured)

**Pattern:**
```kotlin
// In ViewModel
fun updateListingTitle(newTitle: String) {
    val trimmedTitle = newTitle.trim()

    if (trimmedTitle.length > 80) {
        _uiState.update { it.copy(error = "Title must be 80 characters or less") }
        return
    }

    viewModelScope.launch {
        repository.updateListing(
            _uiState.value.selectedListing!!.copy(title = trimmedTitle)
        )
    }
}
```

---

### 3. Authentication/Authorization

**For Gemini API:**
- API key stored in DataStore (encrypted at rest by OS)
- User enters API key in Settings screen
- Repository retrieves API key before each request
- No OAuth flow, just API key bearer token

**Pattern:**
```kotlin
class GeminiServiceImpl(
    private val preferences: UserPreferencesManager,
    private val httpClient: HttpClient
) : GeminiService {

    override suspend fun analyzePhotoQuality(photos: List<Uri>): Result<PhotoQualityResponse> {
        val apiKey = preferences.geminiApiKey.first()
            ?: return Result.failure(MissingApiKeyException())

        // Use apiKey in Authorization header
        return httpClient.post(...) {
            header("Authorization", "Bearer $apiKey")
            // ...
        }
    }
}
```

---

### 4. Persistence

**Strategy:** Room for structured data, File storage for photos

**Patterns:**
- All listings stored in Room database
- Photos saved to app-specific directory (auto-deleted on uninstall)
- No external storage access (scoped storage)
- Database migrations when schema changes

**Example Migration:**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE listings ADD COLUMN sold_date INTEGER")
    }
}

@Provides
@Singleton
fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "quickflip_database"
    )
    .addMigrations(MIGRATION_1_2)
    .build()
}
```

---

### 5. Network Connectivity

**Strategy:** ConnectivityManager for real-time status

**Pattern:**
```kotlin
class NetworkConnectivityManager(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
```

---

## Build Order Implications

### Dependencies Between Phases

**Phase 1 → Phase 2:**
- Repository depends on DAO interfaces
- Cannot build Repository without Room schema

**Phase 3 → Phase 2:**
- ViewModels depend on Repository
- Cannot build UI without Repository

**Phase 4 → Phase 3:**
- Camera screen uses Navigation
- Cannot integrate CameraX without NavGraph

**Phase 5 → Phase 2:**
- Repository implementation needs GeminiService
- Can mock service in Phase 2, replace in Phase 5

**Phase 6 → Phase 5:**
- Workers call GeminiService
- Cannot build offline queue without network layer

**Phase 7 → All Previous:**
- Integration depends on all components
- Must complete Phases 1-6 first

**Recommendation:** Follow phases sequentially, test each phase before proceeding.

---

## Additional Architecture Notes

### Performance Optimizations

1. **Image Compression:**
   - Compress photos before uploading to Gemini API
   - Use Coil library for efficient image loading in UI
   - Max image size: 1024x1024 for API calls

2. **Database Indexing:**
   - Add index on `createdAt` for fast sorting
   - Add index on `status` for filtering

3. **Lazy Loading:**
   - Use LazyColumn in Compose for listing list
   - Only load visible items

4. **Caching:**
   - Cache Gemini API responses for 24 hours
   - Cache strategy: Network-first, fallback to cache

### Security Considerations

1. **API Key Storage:**
   - Store in DataStore (encrypted by OS)
   - Never log API key
   - Validate API key format before use

2. **Photo Privacy:**
   - Photos stored in app-specific directory (not accessible to other apps)
   - Deleted when app uninstalled
   - Option to manually delete photos

3. **Network Security:**
   - Use HTTPS only
   - Certificate pinning (optional, for production)
   - No sensitive data in URL params

### Testing Strategy

1. **Unit Tests:**
   - Repository logic (with fake DAO + Service)
   - ViewModel state transitions
   - Converters and utilities

2. **Integration Tests:**
   - Database migrations
   - Repository + Room interaction
   - Repository + API interaction (with mock server)

3. **UI Tests:**
   - Navigation flow
   - Camera capture
   - Listing CRUD operations
   - Error states

---

**Architecture Research Complete**
*This document should inform roadmap phase structure and component dependencies.*
