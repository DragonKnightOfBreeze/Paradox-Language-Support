package icu.windea.pls.core.util

interface Reversible {
    val operator: Boolean

    fun reversed(): Reversible
}
