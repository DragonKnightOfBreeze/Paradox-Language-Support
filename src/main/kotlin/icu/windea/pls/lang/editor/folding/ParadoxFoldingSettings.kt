package icu.windea.pls.lang.editor.folding

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@State(name = "ParadoxFoldingSettings", storages = [Storage("editor.xml")], category = SettingsCategory.CODE)
class ParadoxFoldingSettings : PersistentStateComponent<ParadoxFoldingSettings>{
	var collapseParameterConditions = false
	var collapseInlineMathBlocks = true //by default true
	var collapseScriptedVariableReferences = true //by default true
	var collapseVariableOperationExpressions = true //by default true
	
	override fun getState() = this
	
	override fun loadState(state: ParadoxFoldingSettings) = XmlSerializerUtil.copyBean(state, this)
	
	companion object {
		@JvmStatic
		fun getInstance() = service<ParadoxFoldingSettings>()
	}
}
