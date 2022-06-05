package icu.windea.pls.core.settings

import com.intellij.openapi.components.*

@State(name = "ParadoxInternalProjectSettings", storages = [Storage("paradox-language-support.internal.xml")])
class ParadoxInternalSettings : SimplePersistentStateComponent<ParadoxInternalSettingsState>(ParadoxInternalSettingsState())

/**
 * @property annotateUnresolvedKeyExpression 如果定义声明中的属性的键无法匹配CWT规则，是否需要标注为错误。
 * @property annotateUnresolvedValueExpression 如果定义声明中的（属性的）值无法匹配CWT规则，是否需要标注为错误。
 */
class ParadoxInternalSettingsState : BaseState() {
	var debug by property(false)
	var annotateUnresolvedKeyExpression by property(false)
	var annotateUnresolvedValueExpression by property(false)
}