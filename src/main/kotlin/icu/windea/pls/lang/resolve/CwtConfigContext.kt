package icu.windea.pls.lang.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.resolve.config.CwtConfigContextProvider
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 规则上下文。
 *
 * 用于后续获取对应的上下文规则（即所有可能的规则）以及匹配的规则，从而提供各种高级语言功能。例如代码高亮、引用解析、代码补全。
 *
 * 规则上下文不一定存在对应的上下文规则。
 * 如果一个规则上下文开始存在对应的上下文规则，并且需要在子上下文中展开，则视作根上下文。
 *
 * @param memberPathFromFile 相对于所在文件的成员路径。
 * @param memberPath 相对于根上下文的成员路径。
 *
 * @see CwtConfigContextProvider
 */
class CwtConfigContext(
    val element: ParadoxScriptMember, // use element directly here
    val memberPathFromFile: ParadoxMemberPath?,
    val memberPath: ParadoxMemberPath?,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    lateinit var provider: CwtConfigContextProvider

    fun getConfigs(matchOptions: Int = ParadoxMatchOptions.Default): List<CwtMemberConfig<*>> {
        return ParadoxConfigService.getConfigsForConfigContext(this, matchOptions)
    }

    fun skipMissingExpressionCheck(): Boolean {
        return provider.skipMissingExpressionCheck(this)
    }

    fun skipTooManyExpressionCheck(): Boolean {
        return provider.skipTooManyExpressionCheck(this)
    }

    object Keys : KeyRegistry()
}

// Accessors

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterElement: ParadoxParameterElement? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.parameterValueQuoted: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptExpression: String? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasConflict: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.inlineScriptHasRecursion: Boolean? by registerKey(CwtConfigContext.Keys)
var CwtConfigContext.definitionInjectionInfo: ParadoxDefinitionInjectionInfo? by registerKey(CwtConfigContext.Keys)

// Extensions

fun CwtConfigContext.isRootForDefinition(): Boolean {
    return memberPath.let { it != null && it.isEmpty() }
        && (definitionInfo != null || definitionInjectionInfo != null)
}

fun CwtConfigContext.inRoot(): Boolean {
    return memberPath != null
}
