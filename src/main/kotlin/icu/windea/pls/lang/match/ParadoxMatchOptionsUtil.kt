package icu.windea.pls.lang.match

import icu.windea.pls.lang.PlsStates

object ParadoxMatchOptionsUtil {
    fun fallback(options: ParadoxMatchOptions? = null): Boolean {
        return options.orDefault().fallback
    }

    fun acceptDefinition(options: ParadoxMatchOptions? = null): Boolean {
        return options.orDefault().acceptDefinition
    }

    fun relax(options: ParadoxMatchOptions? = null): Boolean {
        return options.orDefault().relax
    }

    fun skipIndex(options: ParadoxMatchOptions? = null): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return options.orDefault().skipIndex
    }

    fun skipScope(options: ParadoxMatchOptions? = null): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return options.orDefault().skipScope
    }
}
