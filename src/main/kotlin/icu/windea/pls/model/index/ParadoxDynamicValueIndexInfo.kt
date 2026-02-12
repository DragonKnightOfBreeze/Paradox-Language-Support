package icu.windea.pls.model.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxDynamicValueIndexInfoSupport
 */
data class ParadoxDynamicValueIndexInfo(
    val name: String,
    val dynamicValueType: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
