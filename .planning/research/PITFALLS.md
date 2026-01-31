# PITFALLS.md

**Project:** QuickFlip - Android CameraX + Gemini Vision + Room + Compose
**Research Focus:** Common pitfalls in photo capture + AI API + offline queue integration
**Date:** 2026-01-31

---

## Overview

This document identifies critical pitfalls when building an Android app that combines CameraX photo capture, Gemini Vision API integration, Room database with JSON type converters, offline queueing, and Jetpack Compose UI. Each pitfall includes warning signs, prevention strategies, and phase recommendations.

---

## CameraX + Jetpack Compose Integration

### 1. Lifecycle Mismanagement in Compose

**Problem:** CameraX requires a `LifecycleOwner` to bind use cases. In Compose, using the wrong lifecycle (composition lifecycle vs activity/fragment lifecycle) causes camera crashes, black screens, or resource leaks.

**Warning Signs:**
- Camera preview shows black screen after screen rotation
- `IllegalStateException: Camera is not initialized` errors
- Memory leaks detected in LeakCanary after navigating away
- Camera doesn't release when app backgrounds

**Prevention Strategy:**
- Always use `LocalLifecycleOwner.current` in Composables, NOT composition lifecycle
- Bind camera in a `DisposableEffect` with proper cleanup
- Test rotation, backgrounding, and navigation thoroughly
- Use `ProcessCameraProvider.unbindAll()` in cleanup

```kotlin
@Composable
fun CameraPreview() {
    val lifecycleOwner = LocalLifecycleOwner.current // Correct

    DisposableEffect(lifecycleOwner) {
        // Bind camera here
        onDispose {
            cameraProvider.unbindAll() // Critical cleanup
        }
    }
}
```

**Phase:** Foundation (Phase 1) - Camera setup must be solid from start

---

### 2. AndroidView Recomposition Overhead

**Problem:** Wrapping `PreviewView` in `AndroidView` without proper keys causes unnecessary recreations on every recomposition, leading to flickering and performance issues.

**Warning Signs:**
- Camera preview flickers during UI updates
- Frame drops in Compose performance profiler
- Preview recreates when unrelated state changes
- High CPU usage during camera operation

**Prevention Strategy:**
- Use stable keys for `AndroidView` (e.g., camera ID)
- Move camera binding logic outside of Compose recomposition
- Use `remember` for `PreviewView` instances
- Avoid passing frequently changing state to camera composables

```kotlin
AndroidView(
    factory = { context -> PreviewView(context) },
    modifier = Modifier.fillMaxSize(),
    update = { /* Update only when necessary */ }
)
```

**Phase:** Foundation (Phase 1) - Performance baseline

---

### 3. Photo File URI Persistence

**Problem:** Captured photos saved to cache directory or external storage can be deleted by system cleanup, leaving orphaned database references. ContentResolver URIs may become invalid after reboot.

**Warning Signs:**
- `FileNotFoundException` when loading images from Room
- Photos disappear after device restart
- URIs work immediately after capture but fail later
- Inconsistent image loading across app restarts

**Prevention Strategy:**
- Use app-specific internal storage (`context.filesDir`) for photos
- Never rely on cache directory for permanent storage
- Convert `content://` URIs to file copies immediately after capture
- Implement database migration to handle URI changes
- Add file existence validation before displaying images

```kotlin
// Wrong: Cache directory
val photoFile = File(context.cacheDir, "photo.jpg")

// Correct: Permanent internal storage
val photoFile = File(context.filesDir, "photos/${UUID.randomUUID()}.jpg")
```

**Phase:** Foundation (Phase 1) - Data persistence architecture

---

## Gemini Vision API Integration

### 4. Missing Rate Limit Handling

**Problem:** Gemini API has rate limits (requests per minute/day). Apps that don't handle 429 responses properly will fail silently or crash, especially during batch operations.

**Warning Signs:**
- API calls succeed in testing but fail with multiple photos
- 429 HTTP errors in logs
- Users report "processing failed" for bulk uploads
- Retry logic causes exponential failure cascade

**Prevention Strategy:**
- Implement exponential backoff with jitter for retries
- Track API quota usage locally (requests per minute counter)
- Queue photos for sequential processing, not parallel batch
- Show user-friendly rate limit messages ("Processing 3 of 10...")
- Consider batch API if available instead of individual calls

```kotlin
suspend fun callGeminiWithRetry(imageData: ByteArray, maxRetries: Int = 3) {
    repeat(maxRetries) { attempt ->
        try {
            return geminiApi.analyzeImage(imageData)
        } catch (e: HttpException) {
            if (e.code() == 429) {
                val delay = (2.0.pow(attempt) * 1000).toLong() + Random.nextLong(1000)
                delay(delay) // Exponential backoff with jitter
            } else throw e
        }
    }
}
```

**Phase:** Core Features (Phase 2) - When implementing AI processing queue

---

### 5. Uncompressed Image Upload

**Problem:** Sending full-resolution photos (4-12MB) to Gemini API wastes bandwidth, increases latency, exceeds API size limits, and drains battery. Vision models don't need full resolution for marketplace descriptions.

**Warning Signs:**
- API calls timeout frequently
- High data usage (100MB+ for 10 photos)
- "Payload too large" API errors
- Slow upload times on cellular connection
- Battery drain during photo processing

**Prevention Strategy:**
- Compress images to 1024x1024 or API's recommended size BEFORE upload
- Use JPEG quality 85 for good balance
- Validate file size before API call (< 4MB recommended)
- Show upload progress for transparency
- Consider WebP format for better compression

```kotlin
fun compressForGemini(originalFile: File): ByteArray {
    val bitmap = BitmapFactory.decodeFile(originalFile.path)
    val scaled = Bitmap.createScaledBitmap(bitmap, 1024, 1024, true)

    val outputStream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    return outputStream.toByteArray()
}
```

**Phase:** Core Features (Phase 2) - Before API integration testing

---

### 6. Poor API Error Parsing

**Problem:** Gemini API returns structured error responses (invalid API key, content policy violations, malformed requests). Generic error handling loses this context, making debugging impossible.

**Warning Signs:**
- All API failures show same generic error to user
- Can't distinguish between network errors and API rejections
- Content policy violations crash the app
- Invalid API key discovered only in production
- No actionable error messages for users

**Prevention Strategy:**
- Parse API response errors into sealed class hierarchy
- Map specific errors to user-friendly messages
- Log full error details for debugging (excluding sensitive data)
- Handle content policy violations gracefully
- Validate API key on app first launch with test request

```kotlin
sealed class GeminiError {
    object RateLimitExceeded : GeminiError()
    object InvalidApiKey : GeminiError()
    data class ContentPolicyViolation(val reason: String) : GeminiError()
    data class NetworkError(val cause: Throwable) : GeminiError()
    data class UnknownError(val message: String) : GeminiError()
}

fun parseGeminiError(response: HttpResponse): GeminiError {
    return when (response.status) {
        429 -> GeminiError.RateLimitExceeded
        401, 403 -> GeminiError.InvalidApiKey
        // ... other cases
    }
}
```

**Phase:** Core Features (Phase 2) - During initial API integration

---

### 7. Blocking Main Thread with API Calls

**Problem:** Making network calls on the main thread causes ANR (Application Not Responding) errors. Even with coroutines, incorrect dispatcher usage can freeze UI.

**Warning Signs:**
- "Application Not Responding" dialogs
- UI freezes during photo processing
- StrictMode violations in logs
- NetworkOnMainThreadException crashes
- Janky scrolling during API operations

**Prevention Strategy:**
- Always use `Dispatchers.IO` for network calls
- Never use `runBlocking` in UI code
- Wrap ViewModel API calls in `viewModelScope.launch`
- Show loading indicators during network operations
- Test on slow network (enable network throttling in dev options)

**Phase:** Foundation (Phase 1) - Architecture setup with coroutines

---

## Room Database + JSON Storage

### 8. Inefficient JSON Type Converters

**Problem:** Storing photo URI lists as JSON strings in Room requires serialization/deserialization on every read/write. Naive implementation using reflection-based JSON libraries kills performance.

**Warning Signs:**
- UI lag when scrolling through items with multiple photos
- High CPU usage in profiler during database queries
- Slow room query times (>100ms for simple queries)
- `@TypeConverter` methods appear in performance traces
- Battery drain during database-heavy operations

**Prevention Strategy:**
- Use Kotlin Serialization (faster than Gson/Moshi for small objects)
- Cache deserialized lists in memory when appropriate
- Consider normalized tables (separate Photos table) for >3 photos per item
- Profile query performance with Room's query logging
- Implement pagination for list views

```kotlin
// Slower: Gson with reflection
@TypeConverter
fun fromStringList(value: String): List<String> {
    return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
}

// Faster: Kotlin Serialization with compile-time code generation
@TypeConverter
fun fromStringList(value: String): List<String> {
    return Json.decodeFromString(value)
}

// Best: Normalized table for >3 photos
@Entity
data class Photo(
    @PrimaryKey val id: String,
    val listingId: String,
    val uri: String
)
```

**Phase:** Optimization (Phase 4) - Unless >5 photos per item, then Phase 1

---

### 9. Missing Database Migration Strategy

**Problem:** Room requires explicit migrations when schema changes. Without migration paths, app updates crash with "Migration path not found" or wipe user data.

**Warning Signs:**
- App crashes on update install
- "IllegalStateException: A migration from X to Y is necessary"
- All user data lost after app update
- Different users on different schema versions
- Test builds work but production crashes

**Prevention Strategy:**
- Plan migration strategy from day 1
- Use `fallbackToDestructiveMigration()` ONLY during development
- Write migration tests for every schema change
- Export Room schema JSON for version tracking
- Document migration paths in code comments

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE listings ADD COLUMN status TEXT NOT NULL DEFAULT 'draft'")
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, "quickflip.db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

**Phase:** Foundation (Phase 1) - Architecture decision

---

### 10. Storing Absolute File Paths

**Problem:** Storing absolute file paths (e.g., `/data/user/0/com.app/files/photo.jpg`) in database breaks when Android changes internal storage paths or during app reinstall/backup restore.

**Warning Signs:**
- Photos don't load after app reinstall
- Broken images after Android OS update
- Issues on specific device manufacturers (Samsung, Xiaomi)
- Backup/restore functionality fails
- Can't migrate data between devices

**Prevention Strategy:**
- Store relative paths from a known base directory
- Use constants for base directories, reconstruct full path at runtime
- Implement path resolution helper functions
- Test backup/restore with Android's Auto Backup
- Add path migration logic for legacy data

```kotlin
// Wrong: Absolute path
photoUri = "/data/user/0/com.quickflip/files/photos/photo.jpg"

// Correct: Relative path
photoUri = "photos/photo.jpg" // Relative to context.filesDir

// Runtime resolution
fun resolvePhotoPath(relativePath: String): File {
    return File(context.filesDir, relativePath)
}
```

**Phase:** Foundation (Phase 1) - Data model design

---

## Offline Queue + WorkManager

### 11. Work Constraints Too Restrictive

**Problem:** Setting WorkManager constraints (unmetered network, charging, battery not low) that are too strict means photos never get processed for users without WiFi or who never charge their phone.

**Warning Signs:**
- User complaints: "photos stuck in queue for days"
- Analytics show low WorkManager completion rates
- Queue backlog keeps growing
- Workers never execute on cellular-only devices
- Processing only happens overnight when charging

**Prevention Strategy:**
- Start with relaxed constraints (just network connectivity)
- Make constraints user-configurable in settings
- Implement foreground processing option for immediate needs
- Monitor WorkManager metrics (enqueued vs completed)
- Add "Process Now" manual override button

```kotlin
// Too restrictive
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
    .setRequiresBatteryNotLow(true)
    .setRequiresCharging(true)
    .build()

// Better: User-friendly defaults
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED) // Any network
    .build()
```

**Phase:** Core Features (Phase 2) - When implementing offline queue

---

### 12. Missing Idempotency for Retries

**Problem:** WorkManager retries failed work, but if your API processing isn't idempotent, you might create duplicate listings, double-charge API quota, or corrupt data.

**Warning Signs:**
- Duplicate listings appear in database
- Same photo processed multiple times
- API quota consumed faster than expected
- Database constraint violations on retry
- Inconsistent state after failures

**Prevention Strategy:**
- Use unique request IDs for API calls
- Check if item already processed before API call
- Implement database upsert instead of insert
- Make Workers check completion status first
- Log work attempts to detect duplicates

```kotlin
class ProcessPhotoWorker : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val listingId = inputData.getString("listing_id") ?: return Result.failure()

        // Idempotency check
        val listing = database.listingDao().getById(listingId)
        if (listing.status == Status.PROCESSED) {
            return Result.success() // Already done, skip
        }

        // Process with unique ID
        val result = geminiApi.analyze(listing.photoUri, requestId = listingId)

        // Upsert instead of insert
        database.listingDao().upsert(listing.copy(
            description = result.description,
            status = Status.PROCESSED
        ))

        return Result.success()
    }
}
```

**Phase:** Core Features (Phase 2) - Worker implementation

---

### 13. Large Data in Work Input

**Problem:** WorkManager serializes input data to disk. Passing large data (like image bytes) through `inputData` causes performance issues and 10KB size limit errors.

**Warning Signs:**
- `IllegalStateException: Data cannot occupy more than 10240 bytes`
- Slow work enqueue times
- Database bloat with WorkManager tables
- OOM errors during work scheduling
- Workers fail before even starting

**Prevention Strategy:**
- Pass only IDs/references through inputData, not actual data
- Store images in files/database, pass file paths
- Keep inputData under 1KB if possible
- Use database to share large data between components
- Document inputData contract in Worker class

```kotlin
// Wrong: Passing image data
val inputData = workDataOf(
    "image_bytes" to imageByteArray // Will crash if >10KB
)

// Correct: Pass reference
val inputData = workDataOf(
    "listing_id" to listingId,
    "photo_path" to photoRelativePath
)

class ProcessPhotoWorker : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val listingId = inputData.getString("listing_id")!!
        val listing = database.listingDao().getById(listingId)
        // Fetch actual data from database
    }
}
```

**Phase:** Core Features (Phase 2) - Worker design

---

## Permission Handling

### 14. Runtime Permission Edge Cases

**Problem:** Android 11+ changes storage permissions, Android 13+ adds photo picker. Apps using old permission patterns break or show unnecessary permission dialogs.

**Warning Signs:**
- Permission requests fail silently
- Camera works on Pixel but not Samsung devices
- Photos accessible in testing but not production
- Different behavior on Android 11 vs 13
- Users report "permission denied" but it was granted

**Prevention Strategy:**
- Use MediaStore API instead of direct file access
- Adopt Photo Picker for Android 13+ (no permission needed)
- Test on Android 11, 12, 13, 14 minimum
- Handle "Don't ask again" state gracefully
- Provide manual permission instructions when permanently denied

```kotlin
// Modern approach: Photo Picker (Android 13+)
val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri ->
    // No storage permission needed
}

// For camera: Still need CAMERA permission
val cameraPermission = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) {
        startCamera()
    } else {
        // Show rationale or manual settings navigation
    }
}
```

**Phase:** Foundation (Phase 1) - Permission architecture

---

### 15. Permission Rationale Timing

**Problem:** Showing permission rationale AFTER denial instead of BEFORE first request leads to poor UX and lower grant rates. Android's `shouldShowRequestPermissionRationale` is confusing.

**Warning Signs:**
- Low permission grant rates in analytics
- Users deny camera permission immediately
- App doesn't explain why it needs camera
- No contextual permission requests
- Permission dialog appears out of context

**Prevention Strategy:**
- Show custom explanation dialog BEFORE first permission request
- Request permissions contextually (when user taps "Take Photo", not on app launch)
- Use Accompanist Permissions library for better UX
- Track permission grant/deny rates
- A/B test permission rationale messaging

```kotlin
@Composable
fun CameraPermissionRequest() {
    var showRationale by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Camera Access Needed") },
            text = { Text("QuickFlip needs camera access to photograph items for your listings.") },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    permissionState.launchPermissionRequest()
                }) {
                    Text("Grant Permission")
                }
            }
        )
    }

    Button(onClick = {
        if (permissionState.status.isGranted) {
            startCamera()
        } else {
            showRationale = true // Show rationale BEFORE requesting
        }
    }) {
        Text("Take Photo")
    }
}
```

**Phase:** Polish (Phase 3) - UX refinement

---

## Memory Management

### 16. Bitmap Memory Leaks

**Problem:** Loading full-resolution images without proper scaling causes OOM errors. Bitmaps aren't automatically garbage collected if referenced by UI components.

**Warning Signs:**
- `OutOfMemoryError` when loading multiple photos
- App crashes after taking 3-5 photos
- Memory profiler shows bitmap growth
- UI becomes sluggish with many images
- Crashes more frequent on low-end devices

**Prevention Strategy:**
- Use Coil or Glide for image loading (automatic memory management)
- Never decode full bitmaps, always use inSampleSize
- Implement image LRU cache with size limits
- Use Bitmap.recycle() for manual bitmap handling
- Test on 2GB RAM devices

```kotlin
// Wrong: Loading full bitmap
val bitmap = BitmapFactory.decodeFile(photoFile.path) // Might be 12MB

// Correct: Using Coil with size limits
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(photoFile)
        .size(800, 800) // Automatic downsampling
        .crossfade(true)
        .build(),
    contentDescription = "Product photo"
)
```

**Phase:** Foundation (Phase 1) - Image loading architecture

---

### 17. Unreleased Camera Resources

**Problem:** Not properly unbinding CameraX or releasing camera instances causes camera to stay active in background, draining battery and preventing other apps from using camera.

**Warning Signs:**
- Camera LED stays on when app backgrounds
- Other camera apps show "Camera in use" error
- Battery drain when app in background
- Camera hot to touch after using app
- Memory leaks in camera components

**Prevention Strategy:**
- Unbind all use cases in onPause/DisposableEffect cleanup
- Use ProcessCameraProvider.unbindAll()
- Test by opening app, using camera, backgrounding, opening native camera app
- Monitor battery usage in testing
- Add lifecycle logging to verify cleanup

**Phase:** Foundation (Phase 1) - Camera lifecycle management

---

## Testing Gaps

### 18. No Offline Testing

**Problem:** Apps often work perfectly on developer WiFi but fail completely offline due to missing offline-first logic, poor error handling, or UI states that assume connectivity.

**Warning Signs:**
- Users report app "doesn't work" without WiFi
- Empty states show instead of cached data
- Crash reports spike in low-connectivity regions
- Queue never shows pending items
- No user feedback during offline operation

**Prevention Strategy:**
- Test with airplane mode enabled throughout development
- Implement offline-first architecture (cache, then sync)
- Show clear offline indicators in UI
- Test WorkManager queue behavior without network
- Add integration tests with MockWebServer failures

**Phase:** Core Features (Phase 2) - During offline queue implementation

---

### 19. Ignoring Device Fragmentation

**Problem:** Testing only on emulator or your personal flagship phone misses real-world issues on Samsung, Xiaomi, low-end devices with custom Android skins and camera implementations.

**Warning Signs:**
- Crash reports concentrated on specific manufacturers
- Camera issues only on Samsung devices
- Permission flows break on MIUI (Xiaomi)
- Performance fine on Pixel but unusable on budget phones
- Storage access works on emulator but not real devices

**Prevention Strategy:**
- Test on physical devices from different manufacturers
- Use Firebase Test Lab for automated testing across devices
- Target Android API 24+ but test on API 29, 31, 33, 34
- Monitor crash analytics by device manufacturer
- Add performance benchmarks on low-end devices (2GB RAM, slow CPU)

**Phase:** Polish (Phase 3) - Before beta release

---

## Summary: Phase-Critical Pitfalls

### Must Address in Phase 1 (Foundation):
- #1: Lifecycle mismanagement in Compose
- #3: Photo file URI persistence
- #7: Blocking main thread
- #9: Database migration strategy
- #10: Storing absolute file paths
- #14: Runtime permission edge cases
- #16: Bitmap memory leaks
- #17: Unreleased camera resources

### Must Address in Phase 2 (Core Features):
- #4: Missing rate limit handling
- #5: Uncompressed image upload
- #6: Poor API error parsing
- #11: Work constraints too restrictive
- #12: Missing idempotency for retries
- #13: Large data in work input
- #18: No offline testing

### Address in Phase 3-4 (Polish/Optimization):
- #2: AndroidView recomposition overhead
- #8: Inefficient JSON type converters
- #15: Permission rationale timing
- #19: Ignoring device fragmentation

---

## Next Steps

1. Review each Phase 1 pitfall and incorporate prevention into architecture decisions
2. Add automated tests for critical pitfalls (camera lifecycle, API retry logic, offline mode)
3. Set up Firebase Crashlytics early to catch production issues
4. Create checklist for each phase to verify pitfalls addressed
5. Schedule dedicated testing sessions for device fragmentation and offline scenarios

