package icu.windea.pls.localisation.psi

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

@Deprecated("UNUSED") //以后可能需要基于上下文进行解析
class ParadoxLocalisationParsingContext(
    val project: Project?,
    val fileInfo: ParadoxFileInfo?
) {
    val gameType get() = fileInfo?.rootInfo?.gameType
    
    var currentKey: String? = null
}