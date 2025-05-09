package icu.windea.pls.lang.util

object ParadoxLocalisationArgumentManager {
    fun isTextColorChar(c: Char): Boolean {
        return ParadoxTextColorManager.isColorId(c)
    }
}
