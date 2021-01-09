package com.windea.plugin.idea.paradox.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.*

@State(name = "ParadoxSettingsState", storages = [Storage("paradoxLanguageSupport.xml")])
class ParadoxSettingsState : PersistentStateComponent<ParadoxSettingsState> {
	@JvmField var resolveStringReferences = true
	@JvmField var validateScript = true
	@JvmField var renderLocalisationText = true
	
	override fun getState() = this

	override fun loadState(state: ParadoxSettingsState) = XmlSerializerUtil.copyBean(state, this)

	companion object {
		@JvmStatic
		fun getInstance(): ParadoxSettingsState = ServiceManager.getService(ParadoxSettingsState::class.java)
	}
}
