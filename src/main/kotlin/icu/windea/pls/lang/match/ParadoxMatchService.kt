package icu.windea.pls.lang.match

import icu.windea.pls.base.PlsStates

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

    fun lenient(options: ParadoxMatchOptions? = null): Boolean {
        return options.normalized().lenient
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
