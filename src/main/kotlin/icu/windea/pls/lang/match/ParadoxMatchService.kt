package icu.windea.pls.lang.match

import icu.windea.pls.lang.PlsStates

object ParadoxMatchService {
    fun isDumb(options: ParadoxMatchOptions? = null): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return options.normalized().skipIndex || options.normalized().skipScope
    }

    fun fallback(options: ParadoxMatchOptions? = null): Boolean {
        return options.normalized().fallback
    }

    fun forDeclarationRoot(options: ParadoxMatchOptions? = null): Boolean {
        return options.normalized().forDeclarationRoot
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
