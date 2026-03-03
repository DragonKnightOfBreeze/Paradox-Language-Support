package icu.windea.pls.lang.navigation

import com.intellij.util.indexing.FindSymbolParameters

object PlsChooseByNameUtil {
    fun getAdjustedName(name: String, parameters: FindSymbolParameters): String {
        // NOTE 2.1.4 tricky but necessary in some situations (e.g., for event ids, like `namespace.1`)
        val pattern = parameters.localPatternName
        val nameWithLeadingDot = ".$name"
        val endIndex = pattern.indexOf(nameWithLeadingDot) // the dot character will be never in the parameter `name`
        if (endIndex == -1) return name
        val startIndex = pattern.indexOfFirst { it != '$' && !it.isWhitespace() } // try to find identifier start index
        if (startIndex == -1 || startIndex >= endIndex) return name
        return pattern.substring(startIndex, endIndex) + nameWithLeadingDot
    }
}
