package icu.windea.pls.core.util.markers

class OnceMarker(value: Boolean = false) : Marker {
    @Volatile private var value: Boolean = value

    override fun get(): Boolean {
        return value
    }

    override fun mark(): Boolean {
        if (value) {
            return true
        } else {
            value = true
            return false
        }
    }

    override fun reset() {
        value = false
    }
}
