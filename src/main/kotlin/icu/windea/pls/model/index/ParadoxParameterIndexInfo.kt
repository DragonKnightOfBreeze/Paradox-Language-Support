package icu.windea.pls.model.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import icu.windea.pls.model.ParadoxGameType

data class ParadoxParameterIndexInfo(
    val name: String,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
) : ParadoxIndexInfo()
