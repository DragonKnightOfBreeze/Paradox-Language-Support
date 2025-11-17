package icu.windea.pls.lang.match

import com.intellij.util.BitUtil
import icu.windea.pls.lang.PlsStates

object ParadoxMatchUtil {
    fun skipIndex(): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return false
    }

    fun skipIndex(options: Int): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return BitUtil.isSet(options, ParadoxMatchOptions.SkipIndex)
    }

    fun skipScope(options: Int): Boolean {
        if (PlsStates.processMergedIndex.get() == true) return true
        return BitUtil.isSet(options, ParadoxMatchOptions.SkipIndex)
    }
}
