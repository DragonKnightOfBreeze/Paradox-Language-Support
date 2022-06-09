package icu.windea.pls.core.settings

import com.intellij.openapi.components.*

@State(name = "ParadoxInternalProjectSettings", storages = [Storage("paradox-language-support.internal.xml")])
class ParadoxInternalSettings : SimplePersistentStateComponent<ParadoxInternalSettingsState>(ParadoxInternalSettingsState())

/**
 * PLS内部设置，可以通过设置系统属性或者更改`idea.properties`进行配置。（如：`pls.debug=true`）
 * @property debug 是否开启调试模式。调试模式会启用额外的检查。
 * @property annotateUnresolvedKeyExpression 如果定义声明中的属性的键无法匹配CWT规则，是否需要标注为错误。
 * @property annotateUnresolvedValueExpression 如果定义声明中的（属性的）值无法匹配CWT规则，是否需要标注为错误。
 * @see icu.windea.pls.PlsProperties
 */
class ParadoxInternalSettingsState : BaseState() {
	var debug by property(false)
	var annotateUnresolvedKeyExpression by property(false)
	var annotateUnresolvedValueExpression by property(false)
}