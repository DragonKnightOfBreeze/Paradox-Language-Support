package icu.windea.pls.model

enum class ParadoxModSource(val id: String) {
    Local("local"),
    Steam("steam"),
    Paradox("pdx"),
    ;

    override fun toString(): String = id

    companion object {
        @JvmStatic
        private val map = entries.associateBy { it.id }

        @JvmStatic
        fun get(id: String): ParadoxModSource? = map[id]
    }
}
