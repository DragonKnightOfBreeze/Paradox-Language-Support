package icu.windea.pls.model.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import icu.windea.pls.model.ParadoxGameType

/**
 * @see icu.windea.pls.lang.index.ParadoxMergedIndex
 * @see icu.windea.pls.ep.index.ParadoxParameterIndexInfoSupport
 */
data class ParadoxParameterIndexInfo(
    val name: String,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
