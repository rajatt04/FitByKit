package com.rajatt7z.fitbykit.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.rajatt7z.fitbykit.database.TrackingDatabase
import com.rajatt7z.fitbykit.database.TrackingRecord
import com.rajatt7z.fitbykit.databinding.ActivityDistanceTrackerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.util.Locale

@Suppress("DEPRECATION")
class DistanceTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDistanceTrackerBinding
    private lateinit var database: TrackingDatabase
    private lateinit var geocoder: Geocoder
    private lateinit var mapManager: DistanceMapManager
    private lateinit var locationHelper: DistanceLocationHelper

    private var startPoint: GeoPoint? = null
    private var endPoint: GeoPoint? = null
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
        mapManager = DistanceMapManager(binding.mapView, resources, theme)
        locationHelper = DistanceLocationHelper(this)

        setupMap()
        checkLocationPermissions()
        setupClickListeners()
        setupEditTextListeners()
    }

    private fun setupMap() {
        mapManager.initializeMap(
            onSingleTap = { p ->
                if (!isTracking) {
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
            },
            onLongPress = { resetPoints() }
        )
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

                    mapManager.center(geoPoint, 16.0)

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
                    DistanceRoutingService.getRouteFromORS(start, end)
                }

                if (route.isNotEmpty()) {
                    routePoints = route
                    mapManager.drawRouteWithPath(route)
                    calculateRouteDistance()
                    updateStatus("Route found!")
                } else {
                    currentDistance = mapManager.drawStraightLine(start, end)
                    updateDistance()
                    updateStatus("Using direct route (routing service unavailable)")
                }
            } catch (e: Exception) {
                Log.e("DistanceTracker", "Routing error: ${e.message}", e)
                currentDistance = mapManager.drawStraightLine(start, end)
                updateDistance()
                updateStatus("Using direct route (routing failed)")
                Toast.makeText(this@DistanceTrackerActivity,
                    "Routing failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnStart.isEnabled = true
            }
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
        locationHelper.requestSingleHighAccuracyUpdate { location ->
            val currentGeoPoint = GeoPoint(location.latitude, location.longitude)
            mapManager.center(currentGeoPoint, 17.0)
            mapManager.addMyLocationMarker(currentGeoPoint)
        }
    }

    private fun resetPoints() {
        startPoint = null
        endPoint = null
        routePoints = emptyList()
        binding.etStartPoint.setText("")
        binding.etEndPoint.setText("")
        mapManager.reset()
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
        mapManager.setStartMarker(point)
        updateStatus("Set end point or search address")
        updateButtonStates()
    }

    private fun setEndPoint(point: GeoPoint) {
        endPoint = point
        mapManager.setEndMarker(point)
        updateStatus("Points set. Tap 'Find Route' for optimal path")
        updateButtonStates()

        if (startPoint != null) {
            findRoute()
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

            locationHelper.getLastLocation { location ->
                if (location != null) {
                    val currentPoint = GeoPoint(location.latitude, location.longitude)

                    if (startPoint == null) {
                        setStartPoint(currentPoint)
                        updateAddressFromPoint(currentPoint, isStartPoint = true)
                    } else if (endPoint == null) {
                        setEndPoint(currentPoint)
                        updateAddressFromPoint(currentPoint, isStartPoint = false)
                    }

                    mapManager.center(currentPoint)
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