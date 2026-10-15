package com.yourcompany.atlas.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yourcompany.atlas.data.MemoryEntity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    memories: List<MemoryEntity>,
    onLocationSelected: (Double, Double, Long?) -> Unit
) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(true)
                controller.setZoom(3.0)
                controller.setCenter(GeoPoint(20.0, 0.0)) // Default view on world map

                // Handle map tap events
                val mapEventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onLocationSelected(p.latitude, p.longitude, null)
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
                overlays.add(MapEventsOverlay(mapEventsReceiver))
            }
        },
        update = { mapView ->
            // Clear existing markers and re-add from database
            mapView.overlays.removeIf { it is Marker }

            memories.forEach { memory ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(memory.latitude, memory.longitude)
                    title = memory.title.ifEmpty { "Memory Spot" }
                    setOnMarkerClickListener { _, _ ->
                        onLocationSelected(memory.latitude, memory.longitude, memory.id)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}