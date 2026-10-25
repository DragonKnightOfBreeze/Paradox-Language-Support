package icu.windea.pls.core.util.markers

class ToggleMarker(value: Boolean = false) : Marker {
    @Volatile private var value: Boolean = value

    override fun get(): Boolean {
        return value
    }

    override fun mark(): Boolean {
        val r = value
        value = !value
        return r
    }

    override fun reset() {
        value = false
    }
}
