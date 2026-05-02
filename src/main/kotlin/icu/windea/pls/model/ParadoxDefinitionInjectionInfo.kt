package icu.windea.pls.model

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.model.ParadoxDefinitionSource

/**
 * 定义注入的解析信息。
 *
 * @property mode 注入模式。必须合法。
 * @property target 目标定义的名字。可以为 `null`。
 * @property type 目标定义的类型。可以为 `null`。
 * @property modeConfig 注入模式对应的规则。
 * @property typeConfig 目标定义的类型对应的规则。
 * @property expression 定义注入表达式。格式：`{mode}:{target}`。
 */
data class ParadoxDefinitionInjectionInfo(
    val mode: String,
    val target: String?,
    override val type: String?,
    val modeConfig: CwtValueConfig,
    override val typeConfig: CwtTypeConfig?,
) : UserDataHolderBase(), ParadoxDefinitionCandidateInfo {
    @Volatile var element: ParadoxScriptProperty? = null

    val expression: String get() = ParadoxDefinitionInjectionManager.getExpression(mode, target)

    override val source: ParadoxDefinitionSource get() = ParadoxDefinitionSource.Injection
    override val configGroup: CwtConfigGroup get() = modeConfig.configGroup

    /** @see ParadoxDefinitionInjectionManager.getSubtypeConfigs */
    override fun getSubtypeConfigs(options: ParadoxMatchOptions?): List<CwtSubtypeConfig> = ParadoxDefinitionInjectionManager.getSubtypeConfigs(this, options)

    /** @see ParadoxDefinitionInjectionManager.getDeclaration */
    override fun getDeclaration(options: ParadoxMatchOptions?): CwtPropertyConfig? = ParadoxDefinitionInjectionManager.getDeclaration(this, options)

    /** @see ParadoxDefinitionInjectionManager.isRelaxMode */
    fun isRelaxMode(): Boolean = ParadoxDefinitionInjectionManager.isRelaxMode(this)

    /** @see ParadoxDefinitionInjectionManager.isReplaceMode */
    fun isReplaceMode(): Boolean = ParadoxDefinitionInjectionManager.isReplaceMode(this)

    // /** @see ParadoxDefinitionInjectionManager.isCreateMode */
    // fun isCreateMode(): Boolean = ParadoxDefinitionInjectionManager.isCreateMode(this)

    /** @see ParadoxDefinitionInjectionManager.isTargetExist */
    fun isTargetExist(context: Any? = null): Boolean = ParadoxDefinitionInjectionManager.isTargetExist(this, context)

    override fun toString(): String {
        return "ParadoxDefinitionInjectionInfo(mode=$mode, target=$target, type=$type, gameType=$gameType)"
    }
}
