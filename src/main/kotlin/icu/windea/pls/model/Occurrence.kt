package icu.windea.pls.model

data class Occurrence(
    var actual: Int,
    var min: Int?,
    var max: Int?,
    val relaxMin: Boolean = false,
    val relaxMax: Boolean = false
) {
    var minDefine: String? = null
    var maxDefine: String? = null

    fun isValid(relax: Boolean = false): Boolean {
        if (!(relax && relaxMin)) {
            if (actual < (min ?: 1)) return false
        }
        if (!(relax && relaxMax)) {
            if (max != null && actual > (max ?: Int.MAX_VALUE)) return false
        }
        return true
    }
}
