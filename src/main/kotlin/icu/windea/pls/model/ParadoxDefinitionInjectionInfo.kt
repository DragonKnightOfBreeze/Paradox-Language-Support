package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager

/**
 * 定义注入的解析信息。
 *
 * @property mode 注入模式。必须合法。
 * @property target 目标定义的名字。可以为 `null`。
 * @property type 目标定义的类型。可以为 `null`。
 * @property modeConfig 注入模式对应的规则。
 * @property typeConfig 目标定义的类型对应的规则。
 */
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

    val declarationConfig: CwtDeclarationConfig? get() = type?.let { configGroup.declarations.get(it) }

    val expression: String get() = ParadoxDefinitionInjectionManager.getExpression(mode, target)

    fun getDeclaration(element: PsiElement): CwtPropertyConfig? = ParadoxDefinitionInjectionManager.getDeclaration(this, element)
    fun isRelaxMode(): Boolean = ParadoxDefinitionInjectionManager.isRelaxMode(this)
    fun isTargetExist(context: Any? = null): Boolean = ParadoxDefinitionInjectionManager.isTargetExist(this, context)

    override fun toString(): String {
        return "ParadoxDefinitionInjectionInfo(mode=$mode, target=$target, type=$type, subtypes=$subtypes, gameType=$gameType)"
    }
}
