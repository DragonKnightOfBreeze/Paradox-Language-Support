package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

data class ParadoxComplexEnumValueInfo(
    val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val gameType: ParadoxGameType
) {
    fun getConfig(project: Project): CwtComplexEnumConfig? {
        return getCwtConfig(project).getValue(gameType).complexEnums[enumName]
    }
}