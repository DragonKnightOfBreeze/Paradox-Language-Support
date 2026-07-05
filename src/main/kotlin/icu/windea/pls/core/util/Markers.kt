package icu.windea.pls.core.util

interface Marker {
    fun get(): Boolean

    fun mark(): Boolean

    fun reset()
}

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
