package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

data class ParadoxValueSetValueInfo(
    val name: String,
    val valueSetNames: Set<String>,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val elementOffset: Int,
    val gameType: ParadoxGameType
) {
    fun getConfig(project: Project): CwtComplexEnumConfig? {
        val valueSetName = valueSetNames.firstOrNull() ?: return null
        return getCwtConfig(project).get(gameType).complexEnums[enumName]
    }
}
