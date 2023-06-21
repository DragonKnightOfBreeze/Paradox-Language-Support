package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.cwt.config.*

data class ParadoxComplexEnumValueInfo(
    val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    override val file: PsiFile? get() = null //unused yet
    
    fun getConfig(project: Project): CwtComplexEnumConfig? {
        return getCwtConfig(project).get(gameType).complexEnums[enumName]
    }
}
