package icu.windea.pls.model

enum class ParadoxModSource(val id: String) {
    Local("local"),
    Steam("steam"),
    Paradox("pdx"),
    ;

    override fun toString(): String = id

    companion object {
        @JvmStatic
        fun resolve(id: String): ParadoxModSource {
            return entries.firstOrNull { it.id == id } ?: Local
        }
    }
}
