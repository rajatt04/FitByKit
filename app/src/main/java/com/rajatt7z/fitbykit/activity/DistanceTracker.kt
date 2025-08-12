package com.rajatt7z.fitbykit.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.rajatt7z.fitbykit.database.TrackingDatabase
import com.rajatt7z.fitbykit.database.TrackingRecord
import com.rajatt7z.fitbykit.databinding.ActivityDistanceTrackerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.*

class DistanceTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDistanceTrackerBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: TrackingDatabase

    private var startPoint: GeoPoint? = null
    private var endPoint: GeoPoint? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var routeLine: Polyline? = null

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

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        database = TrackingDatabase.getDatabase(this)
        initializeMap()
        checkLocationPermissions()
        setupClickListeners()
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
                        startPoint == null -> setStartPoint(p)
                        endPoint == null -> setEndPoint(p)
                        else -> Toast.makeText(
                            this@DistanceTrackerActivity,
                            "Both points set. Long press to reset.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // return true to indicate we've handled the tap
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // optional: reset points on long press
                resetPoints()
                return true
            }
        }

        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
        binding.mapView.overlays.add(eventsOverlay)
        binding.mapView.invalidate()
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                binding.mapView.controller.setCenter(userGeoPoint)
            } else {
                // If last location is null, request a fresh one
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 2000
                ).setMaxUpdates(1).build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val freshLocation = result.lastLocation
                            if (freshLocation != null) {
                                val freshGeoPoint = GeoPoint(freshLocation.latitude, freshLocation.longitude)
                                binding.mapView.controller.setCenter(freshGeoPoint)
                            }
                            fusedLocationClient.removeLocationUpdates(this)
                        }
                    },
                    Looper.getMainLooper()
                )
            }
        }
    }

    private fun resetPoints() {
        startPoint = null
        endPoint = null
        startMarker?.let { binding.mapView.overlays.remove(it) }
        endMarker?.let { binding.mapView.overlays.remove(it) }
        routeLine?.let { binding.mapView.overlays.remove(it) }
        binding.mapView.invalidate()
        updateStatus("Tap to set start point")
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

//    @SuppressLint("MissingPermission")
//    private fun initializeLocation() {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//            location?.let {
//                binding.mapView.controller.setCenter(GeoPoint(it.latitude, it.longitude))
//            }
//        }
//    }

    private fun setStartPoint(point: GeoPoint) {
        startPoint = point
        startMarker?.let { binding.mapView.overlays.remove(it) }
        startMarker = createMarker(point, "Start Point", android.R.drawable.ic_menu_mylocation)
        updateStatus("Tap on map to set end point")
        binding.btnStart.isEnabled = false
    }

    private fun setEndPoint(point: GeoPoint) {
        endPoint = point
        endMarker?.let { binding.mapView.overlays.remove(it) }
        endMarker = createMarker(point, "End Point", android.R.drawable.ic_menu_compass)
        drawRouteLine()
        updateStatus("Ready to start tracking")
        binding.btnStart.isEnabled = true
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
            Start: ${startPoint?.latitude}, ${startPoint?.longitude}
            End: ${endPoint?.latitude}, ${endPoint?.longitude}
            
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
        binding.btnStart.isEnabled = !tracking && !completed
        binding.btnStop.isEnabled = tracking
        binding.btnShare.isEnabled = completed
        binding.btnDownload.isEnabled = completed
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