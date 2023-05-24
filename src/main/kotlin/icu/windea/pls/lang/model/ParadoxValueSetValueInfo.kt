package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*

data class ParadoxValueSetValueInfo(
    val name: String,
    val valueSetName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val elementOffset: Int,
    val gameType: ParadoxGameType
) {
    fun getConfig(project: Project): CwtEnumConfig? {
        return getCwtConfig(project).get(gameType).values[valueSetName]
    }
}
