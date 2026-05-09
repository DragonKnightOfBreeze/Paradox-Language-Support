package icu.windea.pls.lang.match

data class ParadoxMatchOccurrence(
    var actual: Int,
    var min: Int?,
    var max: Int?,
    val lenientMin: Boolean = false,
    val lenientMax: Boolean = false
) {
    var minDefine: String? = null
    var maxDefine: String? = null

    fun isValid(relax: Boolean = false): Boolean {
        if (!(relax && lenientMin)) {
            if (actual < (min ?: 1)) return false
        }
        if (!(relax && lenientMax)) {
            if (max != null && actual > (max ?: Int.MAX_VALUE)) return false
        }
        return true
    }
}
