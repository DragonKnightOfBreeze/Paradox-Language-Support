package icu.windea.pls.model.expressionInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

data class ParadoxDynamicValueInfo(
    val name: String,
    val dynamicValueType: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
) : ParadoxExpressionInfo {
    @Volatile override var virtualFile: VirtualFile? = null
    
    fun getConfig(project: Project): CwtDynamicValueTypeConfig? {
        return getConfigGroup(project, gameType).dynamicValueTypes[dynamicValueType]
    }
}
