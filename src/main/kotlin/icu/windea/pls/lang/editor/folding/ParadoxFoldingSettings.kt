package icu.windea.pls.lang.editor.folding

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

/**
 * @property parameterConditionBlocks 是否默认折叠参数条件表达式块。默认不启用。
 * @property inlineMathBlocks 是否默认折叠内联数学表达式块。默认启用。
 * @property scriptedVariableReferences 是否默认折叠封装变量引用。折叠为解析后的值。默认启用。
 * @property variableOperationExpressions （基于内置规则文件）是否默认折叠变量操作表达式。折叠为简化形式。默认启用。
 * @property localisationReferencesFully 是否允许且默认折叠本地化引用。完全折叠。默认不启用。
 * @property localisationIconsFully 是否允许且默认折叠本地化图标。完全折叠。默认不启用。
 */
@State(name = "ParadoxFoldingSettings", storages = [Storage("editor.xml")], category = SettingsCategory.CODE)
class ParadoxFoldingSettings : PersistentStateComponent<ParadoxFoldingSettings>{
	var parameterConditionBlocks = false
	var inlineMathBlocks = true
	var scriptedVariableReferences = true
	var variableOperationExpressions = true
	var localisationReferencesFully = false
	var localisationIconsFully = false
	
	override fun getState() = this
	
	override fun loadState(state: ParadoxFoldingSettings) = XmlSerializerUtil.copyBean(state, this)
	
	companion object {
		@JvmStatic
		fun getInstance() = service<ParadoxFoldingSettings>()
	}
}
