package icu.windea.pls.core.util.markers

interface Marker {
    fun get(): Boolean

    fun mark(): Boolean

    fun reset()
}
