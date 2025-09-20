package icu.windea.pls.model

enum class ParadoxModSource {
    Local,
    Steam,
    Paradox,
    ;

    companion object {
        @JvmStatic
        fun resolve(text: String): ParadoxModSource {
            return entries.firstOrNull { it.name.equals(text, true) } ?: Local
        }
    }
}
