package icu.windea.pls.model

import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup

data class ParadoxComplexEnumValueInfo(
    val name: String,
    val enumName: String,
    val config: CwtComplexEnumConfig,
) {
    val configGroup: CwtConfigGroup get() = config.configGroup
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    override fun toString(): String {
        return "ParadoxComplexEnumValueInfo(name=$name, enumName=$enumName, gameType=$gameType)"
    }
}

