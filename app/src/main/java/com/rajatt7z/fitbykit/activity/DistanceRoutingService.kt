package com.rajatt7z.fitbykit.activity

import android.util.Log
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL

object DistanceRoutingService {
    fun getRouteFromORS(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
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
}