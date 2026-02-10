package icu.windea.pls.model

import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager

data class ParadoxDefinitionInjectionInfo(
    val mode: String, // must be valid
    val target: String?,
    val type: String?,
    val subtypes: List<String>,
    val modeConfig: CwtValueConfig,
    val typeConfig: CwtTypeConfig?,
    val subtypeConfigs: List<CwtSubtypeConfig>,
) {
    val configGroup: CwtConfigGroup get() = modeConfig.configGroup
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    val declarationConfig: CwtDeclarationConfig? get() = type?.orNull()?.let { configGroup.declarations.get(it) }

    val expression: String get() = ParadoxDefinitionInjectionManager.getExpression(mode, target)

    override fun toString(): String {
        return "ParadoxDefinitionInjectionInfo(mode=$mode, target=$target, type=$type, subtypes=$subtypes, gameType=$gameType)"
    }
}
