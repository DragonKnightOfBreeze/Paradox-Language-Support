package icu.windea.pls.core.util

class OnceMarker {
    @Volatile
    private var value: Boolean = false

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
