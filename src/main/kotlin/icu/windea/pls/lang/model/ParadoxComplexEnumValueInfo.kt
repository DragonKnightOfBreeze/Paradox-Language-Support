package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.config.*

data class ParadoxComplexEnumValueInfo(
    val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
    
    fun getConfig(project: Project): CwtComplexEnumConfig? {
        return getCwtConfig(project).get(gameType).complexEnums[enumName]
    }
}
