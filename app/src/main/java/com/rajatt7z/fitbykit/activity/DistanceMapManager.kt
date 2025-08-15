package com.rajatt7z.fitbykit.activity

import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.events.MapEventsReceiver

class DistanceMapManager(
    private val mapView: MapView,
    private val resources: Resources,
    private val theme: Resources.Theme
) {
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var routeLine: Polyline? = null

    fun initializeMap(
        onSingleTap: (GeoPoint) -> Unit,
        onLongPress: () -> Unit
    ) {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { onSingleTap(it) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                onLongPress()
                return true
            }
        }

        val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(eventsOverlay)
        mapView.invalidate()
    }

    fun setStartMarker(point: GeoPoint, iconRes: Int = android.R.drawable.ic_menu_mylocation) {
        startMarker?.let { mapView.overlays.remove(it) }
        startMarker = Marker(mapView).apply {
            position = point
            title = "Start Point"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = resources.getDrawable(iconRes, theme)
        }
        mapView.overlays.add(startMarker)
        mapView.invalidate()
    }

    fun setEndMarker(point: GeoPoint, iconRes: Int = android.R.drawable.ic_menu_compass) {
        endMarker?.let { mapView.overlays.remove(it) }
        endMarker = Marker(mapView).apply {
            position = point
            title = "End Point"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = resources.getDrawable(iconRes, theme)
        }
        mapView.overlays.add(endMarker)
        mapView.invalidate()
    }

    fun addMyLocationMarker(point: GeoPoint, iconRes: Int = android.R.drawable.ic_notification_overlay) {
        val myLocationMarker = Marker(mapView).apply {
            position = point
            title = "You are here"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = resources.getDrawable(iconRes, theme)
        }
        mapView.overlays.add(myLocationMarker)
        mapView.invalidate()
    }

    fun drawRouteWithPath(routePoints: List<GeoPoint>) {
        routeLine?.let { mapView.overlays.remove(it) }

        routeLine = Polyline().apply {
            setPoints(routePoints)
            color = Color.BLUE
            width = 8f
        }

        mapView.overlays.add(routeLine)
        mapView.invalidate()

        if (routePoints.isNotEmpty()) {
            val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(routePoints)
            mapView.zoomToBoundingBox(boundingBox, true, 100)
        }
    }

    fun drawStraightLine(start: GeoPoint, end: GeoPoint): Double {
        routeLine?.let { mapView.overlays.remove(it) }
        routeLine = Polyline().apply {
            addPoint(start)
            addPoint(end)
            color = Color.BLUE
            width = 8f
        }
        mapView.overlays.add(routeLine)
        mapView.invalidate()

        val results = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
        return results[0].toDouble()
    }

    fun center(point: GeoPoint, zoom: Double? = null) {
        zoom?.let { mapView.controller.setZoom(it) }
        mapView.controller.setCenter(point)
    }

    fun reset() {
        startMarker?.let { mapView.overlays.remove(it) }
        endMarker?.let { mapView.overlays.remove(it) }
        routeLine?.let { mapView.overlays.remove(it) }
        startMarker = null
        endMarker = null
        routeLine = null
        mapView.invalidate()
    }
}