package com.rajatt7z.fitbykit.activity

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rajatt7z.fitbykit.database.TrackingDatabase
import com.rajatt7z.fitbykit.database.TrackingRecord
import com.rajatt7z.fitbykit.databinding.ActivityDistanceTrackerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

@Suppress("DEPRECATION")
class DistanceTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDistanceTrackerBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: TrackingDatabase
    private lateinit var geocoder: Geocoder

    private var startPoint: GeoPoint? = null
    private var endPoint: GeoPoint? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var routeLine: Polyline? = null
    private var routePoints: List<GeoPoint> = emptyList()

    private var isTracking = false
    private var startTime = 0L
    private var currentDistance = 0.0

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                updateDuration()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.any { it.value }) {
            initializeLocation()
        } else {
            Toast.makeText(this, "Location permission required for tracking", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDistanceTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAddressAutocomplete(binding.etStartPoint, isStart = true)
        setupAddressAutocomplete(binding.etEndPoint, isStart = false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                statusBarInsets.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        database = TrackingDatabase.getDatabase(this)
        geocoder = Geocoder(this, Locale.getDefault())

        initializeMap()
        checkLocationPermissions()
        setupClickListeners()
        setupEditTextListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (!isTracking && p != null) {
                    when {
                        binding.etStartPoint.hasFocus() -> {
                            setStartPoint(p)
                            updateAddressFromPoint(p, isStartPoint = true)
                        }
                        binding.etEndPoint.hasFocus() -> {
                            setEndPoint(p)
                            updateAddressFromPoint(p, isStartPoint = false)
                        }
                        startPoint == null -> {
                            setStartPoint(p)
                            updateAddressFromPoint(p, isStartPoint = true)
                        }
                        endPoint == null -> {
                            setEndPoint(p)
                            updateAddressFromPoint(p, isStartPoint = false)
                        }
                        else -> Toast.makeText(
                            this@DistanceTrackerActivity,
                            "Both points set. Long press to reset.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                resetPoints()
                return true
            }
        }

        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
        binding.mapView.overlays.add(eventsOverlay)
        binding.mapView.invalidate()
    }

    private fun setupEditTextListeners() {
        binding.etStartPoint.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Optional: Add auto-complete functionality here
            }
        })

        binding.etEndPoint.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Optional: Add auto-complete functionality here
            }
        })

        // Search buttons for geocoding
        binding.btnSearchStart.setOnClickListener {
            val address = binding.etStartPoint.text.toString().trim()
            if (address.isNotEmpty()) {
                searchAddress(address, isStartPoint = true)
            }
        }

        binding.btnSearchEnd.setOnClickListener {
            val address = binding.etEndPoint.text.toString().trim()
            if (address.isNotEmpty()) {
                searchAddress(address, isStartPoint = false)
            }
        }

        binding.btnFindRoute.setOnClickListener {
            if (startPoint != null && endPoint != null) {
                findRoute()
            } else {
                Toast.makeText(this, "Please set both start and end points", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchAddress(address: String, isStartPoint: Boolean) {
        lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(address, 1)
                }

                if (!results.isNullOrEmpty()) {
                    val location = results[0]
                    val geoPoint = GeoPoint(location.latitude, location.longitude)

                    if (isStartPoint) {
                        setStartPoint(geoPoint)
                        updateStatus("Start point set: ${location.getAddressLine(0)}")
                    } else {
                        setEndPoint(geoPoint)
                        updateStatus("End point set: ${location.getAddressLine(0)}")
                    }

                    // Center map on the found location
                    binding.mapView.controller.setCenter(geoPoint)
                    binding.mapView.controller.setZoom(16.0)

                } else {
                    Toast.makeText(this@DistanceTrackerActivity,
                        "Address not found: $address", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this@DistanceTrackerActivity,
                    "Error searching address: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAddressFromPoint(point: GeoPoint, isStartPoint: Boolean) {
        lifecycleScope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(point.latitude, point.longitude, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    if (isStartPoint) {
                        binding.etStartPoint.setText(address)
                    } else {
                        binding.etEndPoint.setText(address)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun findRoute() {
        val start = startPoint ?: return
        val end = endPoint ?: return

        binding.progressBar.visibility = View.VISIBLE
        updateStatus("Finding route...")

        lifecycleScope.launch {
            try {
                val route = withContext(Dispatchers.IO) {
                    getRouteFromORS(start, end)
                }

                if (route.isNotEmpty()) {
                    routePoints = route
                    drawRouteWithPath(route)
                    calculateRouteDistance()
                    updateStatus("Route found!")
                } else {
                    // Fallback to straight line if routing fails
                    drawRouteLine()
                    updateStatus("Using direct route (routing service unavailable)")
                }
            } catch (e: Exception) {
                Log.e("DistanceTracker", "Routing error: ${e.message}", e)
                // Fallback to straight line
                drawRouteLine()
                updateStatus("Using direct route (routing failed)")
                Toast.makeText(this@DistanceTrackerActivity,
                    "Routing failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnStart.isEnabled = true
            }
        }
    }

    private fun setupAddressAutocomplete(autoCompleteTextView: AutoCompleteTextView, isStart: Boolean) {
        val adapter = ArrayAdapter<String>(this, R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)

        var searchJob: Job? = null

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.length < 3) return

                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(1000) // avoid API spam while typing
                    fetchAddressSuggestions(text) { suggestions ->
                        adapter.clear()
                        adapter.addAll(suggestions)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener
            autoCompleteTextView.setText(selected)

            lifecycleScope.launch {
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(selected, 1)
                }
                if (!results.isNullOrEmpty()) {
                    val point = GeoPoint(results[0].latitude, results[0].longitude)
                    if (isStart) {
                        setStartPoint(point)
                    } else {
                        setEndPoint(point)
                    }
                }
            }
        }
    }

    private fun fetchAddressSuggestions(query: String, callback: (List<String>) -> Unit) {
        lifecycleScope.launch {
            try {
                val suggestions = withContext(Dispatchers.IO) {
                    val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjJhNGYwZjNlODlkNTQ0OWFhZTNlMzY3ZmQyZTZiZjM0IiwiaCI6Im11cm11cjY0In0="
                    val urlStr = "https://api.openrouteservice.org/geocode/autocomplete" +
                            "?api_key=$apiKey&text=${URLEncoder.encode(query, "UTF-8")}&boundary.country=IN"

                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000

                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = conn.inputStream.bufferedReader().readText()
                        val json = JSONObject(response)
                        val features = json.optJSONArray("features") ?: return@withContext emptyList<String>()

                        (0 until features.length()).map { i ->
                            features.getJSONObject(i).getJSONObject("properties").getString("label")
                        }
                    } else {
                        emptyList()
                    }
                }
                callback(suggestions)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }
    }

    private fun getRouteFromORS(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
        return try {
            val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjJhNGYwZjNlODlkNTQ0OWFhZTNlMzY3ZmQyZTZiZjM0IiwiaCI6Im11cm11cjY0In0="

            Log.d("DistanceTracker", "Making routing request with API key: ${apiKey.take(10)}...")

            val urlString = "https://api.openrouteservice.org/v2/directions/driving-car?" +
                    "api_key=$apiKey&" +
                    "start=${start.longitude},${start.latitude}&" +
                    "end=${end.longitude},${end.latitude}"

            Log.d("DistanceTracker", "Request URL: $urlString")

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/geo+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            Log.d("DistanceTracker", "Response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("DistanceTracker", "Response: ${response.take(200)}...")

                val json = JSONObject(response)

                if (json.has("features") && json.getJSONArray("features").length() > 0) {
                    val coordinates = json.getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")

                    val points = mutableListOf<GeoPoint>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        points.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
                    }
                    Log.d("DistanceTracker", "Route found with ${points.size} points")
                    points
                } else {
                    Log.e("DistanceTracker", "No features in response")
                    emptyList()
                }
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("DistanceTracker", "HTTP Error $responseCode: $errorResponse")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DistanceTracker", "Exception in routing: ${e.message}", e)
            emptyList()
        }
    }

    private fun drawRouteWithPath(routePoints: List<GeoPoint>) {
        routeLine?.let { binding.mapView.overlays.remove(it) }

        routeLine = Polyline().apply {
            setPoints(routePoints)
            color = Color.BLUE
            width = 8f
        }

        binding.mapView.overlays.add(routeLine)
        binding.mapView.invalidate()

        // Fit map to show entire route
        if (routePoints.isNotEmpty()) {
            val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(routePoints)
            binding.mapView.zoomToBoundingBox(boundingBox, true, 100)
        }
    }

    private fun calculateRouteDistance() {
        if (routePoints.size < 2) return

        var totalDistance = 0.0
        for (i in 0 until routePoints.size - 1) {
            val results = FloatArray(1)
            Location.distanceBetween(
                routePoints[i].latitude, routePoints[i].longitude,
                routePoints[i + 1].latitude, routePoints[i + 1].longitude,
                results
            )
            totalDistance += results[0]
        }

        currentDistance = totalDistance
        updateDistance()
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).setMaxUpdates(1)
            .setMinUpdateIntervalMillis(500)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    val currentGeoPoint = GeoPoint(location.latitude, location.longitude)

                    binding.mapView.controller.setZoom(17.0)
                    binding.mapView.controller.setCenter(currentGeoPoint)

                    val myLocationMarker = Marker(binding.mapView).apply {
                        position = currentGeoPoint
                        title = "You are here"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = resources.getDrawable(android.R.drawable.ic_notification_overlay, theme)
                    }
                    binding.mapView.overlays.add(myLocationMarker)
                    binding.mapView.invalidate()

                    fusedLocationClient.removeLocationUpdates(this)
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun resetPoints() {
        startPoint = null
        endPoint = null
        routePoints = emptyList()
        binding.etStartPoint.setText("")
        binding.etEndPoint.setText("")
        startMarker?.let { binding.mapView.overlays.remove(it) }
        endMarker?.let { binding.mapView.overlays.remove(it) }
        routeLine?.let { binding.mapView.overlays.remove(it) }
        binding.mapView.invalidate()
        updateStatus("Enter addresses or tap to set points")
        binding.btnStart.isEnabled = false
    }

    private fun checkLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            initializeLocation()
        } else {
            locationPermissionLauncher.launch(permissions)
        }
    }

    private fun setStartPoint(point: GeoPoint) {
        startPoint = point
        startMarker?.let { binding.mapView.overlays.remove(it) }
        startMarker = createMarker(point, "Start Point", android.R.drawable.ic_menu_mylocation)
        updateStatus("Set end point or search address")
        updateButtonStates()
    }

    private fun setEndPoint(point: GeoPoint) {
        endPoint = point
        endMarker?.let { binding.mapView.overlays.remove(it) }
        endMarker = createMarker(point, "End Point", android.R.drawable.ic_menu_compass)
        updateStatus("Points set. Tap 'Find Route' for optimal path")
        updateButtonStates()

        // Auto-find route if both points are set
        if (startPoint != null) {
            findRoute()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createMarker(point: GeoPoint, title: String, iconRes: Int): Marker {
        return Marker(binding.mapView).apply {
            position = point
            this.title = title
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = resources.getDrawable(iconRes, theme)
            binding.mapView.overlays.add(this)
            binding.mapView.invalidate()
        }
    }

    @Suppress("DEPRECATION")
    private fun drawRouteLine() {
        startPoint?.let { start ->
            endPoint?.let { end ->
                routeLine?.let { binding.mapView.overlays.remove(it) }
                routeLine = Polyline().apply {
                    addPoint(start)
                    addPoint(end)
                    color = Color.BLUE
                    width = 8f
                }
                binding.mapView.overlays.add(routeLine)
                val results = FloatArray(1)
                Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
                currentDistance = results[0].toDouble()
                updateDistance()
                binding.mapView.invalidate()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnStart.setOnClickListener { startTracking() }
        binding.btnStop.setOnClickListener { stopTracking() }
        binding.btnShare.setOnClickListener { shareTrackingData() }
        binding.btnDownload.setOnClickListener { saveTrackingData() }
        binding.btnReset.setOnClickListener { resetPoints() }
        binding.btnCurrentLocation.setOnClickListener { getCurrentLocation() }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentPoint = GeoPoint(location.latitude, location.longitude)

                    // Set as start point if none exists, otherwise as end point
                    if (startPoint == null) {
                        setStartPoint(currentPoint)
                        updateAddressFromPoint(currentPoint, isStartPoint = true)
                    } else if (endPoint == null) {
                        setEndPoint(currentPoint)
                        updateAddressFromPoint(currentPoint, isStartPoint = false)
                    }

                    binding.mapView.controller.setCenter(currentPoint)
                }
            }
        }
    }

    private fun startTracking() {
        if (startPoint != null && endPoint != null) {
            isTracking = true
            startTime = System.currentTimeMillis()
            updateStatus("Tracking in progress...")
            updateButtonStates(tracking = true)
            handler.post(updateRunnable)
            Toast.makeText(this, "Tracking started!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopTracking() {
        if (isTracking) {
            isTracking = false
            updateStatus("Tracking completed")
            updateButtonStates(tracking = false, completed = true)
            Toast.makeText(this, "Tracking stopped!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareTrackingData() {
        val shareText = """
            Distance Tracking Results:
            Distance: %.2f km
            Duration: ${formatDuration(System.currentTimeMillis() - startTime)}
            Start: ${binding.etStartPoint.text}
            End: ${binding.etEndPoint.text}
            
            Tracked with Distance Tracker App
        """.trimIndent().format(currentDistance / 1000)

        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }, "Share Tracking Data"))
    }

    private fun saveTrackingData() {
        val start = startPoint ?: return
        val end = endPoint ?: return
        val endTime = System.currentTimeMillis()
        val record = TrackingRecord(
            startLatitude = start.latitude,
            startLongitude = start.longitude,
            endLatitude = end.latitude,
            endLongitude = end.longitude,
            distance = currentDistance,
            startTime = startTime,
            endTime = endTime,
            duration = endTime - startTime
        )
        lifecycleScope.launch {
            val id = withContext(Dispatchers.IO) { database.trackingDao().insertRecord(record) }
            Toast.makeText(this@DistanceTrackerActivity, "Tracking data saved! (ID: $id)", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateStatus(status: String) {
        binding.tvStatus.text = status
    }

    @SuppressLint("SetTextI18n")
    private fun updateDistance() {
        binding.tvDistance.text = "Distance: %.2f km".format(currentDistance / 1000)
    }

    @SuppressLint("SetTextI18n")
    private fun updateDuration() {
        binding.tvDuration.text = "Duration: ${formatDuration(System.currentTimeMillis() - startTime)}"
    }

    private fun formatDuration(ms: Long): String {
        val sec = (ms / 1000) % 60
        val min = (ms / 60000) % 60
        val hrs = (ms / 3600000)
        return "%02d:%02d:%02d".format(hrs, min, sec)
    }

    private fun updateButtonStates(tracking: Boolean = false, completed: Boolean = false) {
        binding.btnStart.isEnabled = !tracking && !completed && startPoint != null && endPoint != null
        binding.btnStop.isEnabled = tracking
        binding.btnShare.isEnabled = completed
        binding.btnDownload.isEnabled = completed
        binding.btnFindRoute.isEnabled = !tracking && startPoint != null && endPoint != null
        binding.btnReset.isEnabled = !tracking
        binding.btnCurrentLocation.isEnabled = !tracking
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
}