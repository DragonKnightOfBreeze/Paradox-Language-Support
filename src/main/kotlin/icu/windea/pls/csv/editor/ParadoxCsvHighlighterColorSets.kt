package icu.windea.pls.csv.editor

import icu.windea.pls.csv.editor.ParadoxCsvHighlighterColors as Colors

object ParadoxCsvHighlighterColorSets {
    @JvmField val SEPARATOR = arrayOf(Colors.SEPARATOR)
    @JvmField val COMMENT = arrayOf(Colors.COMMENT)
    @JvmField val STRING = arrayOf(Colors.STRING)
    @JvmField val VALID_ESCAPE = arrayOf(Colors.VALID_ESCAPE)
    @JvmField val INVALID_ESCAPE = arrayOf(Colors.INVALID_ESCAPE)
    @JvmField val BAD_CHARACTER = arrayOf(Colors.BAD_CHARACTER)
}
