package icu.windea.pls.core.util

class ToggleMarker(value: Boolean = false) {
    @Volatile
    private var value: Boolean = value

    fun get(): Boolean {
        return value
    }

    fun mark(): Boolean {
        val r = value
        value = !value
        return r
    }

    fun reset() {
        value = false
    }
}

class OnceMarker(value: Boolean = false) {
    @Volatile
    private var value: Boolean = value

    fun get(): Boolean {
        return value
    }

    fun mark(): Boolean {
        if (value) {
            return true
        } else {
            value = true
            return false
        }
    }

    fun reset() {
        value = false
    }
}
