package icu.windea.pls.lang.match

import icu.windea.pls.lang.PlsStates

object ParadoxMatchOptionsUtil {
    fun fallback(options: ParadoxMatchOptions? = null): Boolean {
        return options.normalized().fallback
    }

    fun acceptDefinition(options: ParadoxMatchOptions? = null): Boolean {
        return options.normalized().acceptDefinition
    }

    fun relax(options: ParadoxMatchOptions? = null): Boolean {
        return options.normalized().relax
    }

    fun skipIndex(options: ParadoxMatchOptions? = null): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return options.normalized().skipIndex
    }

    fun skipScope(options: ParadoxMatchOptions? = null): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return options.normalized().skipScope
    }
}
