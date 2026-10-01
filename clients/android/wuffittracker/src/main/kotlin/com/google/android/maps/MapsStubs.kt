package com.google.android.maps

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity

open class GeoPoint(val latitudeE6: Int, val longitudeE6: Int) {
    override fun toString(): String = "$latitudeE6,$longitudeE6"
}

open class Overlay

abstract class ItemizedOverlay<Item : OverlayItem>(protected val defaultMarker: Drawable?) : Overlay() {
    abstract fun createItem(i: Int): Item
    abstract fun size(): Int

    open fun clear() {}

    protected open fun onTap(i: Int): Boolean = false

    protected fun populate() {}

    protected fun getItem(position: Int): Item = createItem(position)

    companion object {
        @JvmStatic
        fun boundCenter(balloon: Drawable?): Drawable? = balloon

        @JvmStatic
        fun boundCenterBottom(balloon: Drawable?): Drawable? = balloon
    }
}

open class OverlayItem(
    val point: GeoPoint,
    val title: String?,
    val snippet: String?
)

open class MapController {
    open fun animateTo(geoPoint: GeoPoint) {}
    open fun setZoom(zoom: Int) {}
}

open class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlays = mutableListOf<Overlay>()
    private val controller = MapController()

    fun getController(): MapController = controller
    fun getOverlays(): MutableList<Overlay> = overlays
    fun getZoomControls(): View = View(context)
}

open class MyLocationOverlay(
    context: Context,
    mapView: MapView
) : Overlay() {
    private var location = GeoPoint(0, 0)

    fun runOnFirstFix(runnable: Runnable) {
        runnable.run()
    }

    fun getMyLocation(): GeoPoint = location

    fun enableMyLocation(): Boolean = true

    fun disableMyLocation() {}
}

abstract class MapActivity : AppCompatActivity() {
    protected abstract fun isRouteDisplayed(): Boolean
}
