package com.windea.plugin.idea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@State(name = "ParadoxSettingsState", storages = [Storage("paradoxLanguageSupport.xml")])
class ParadoxSettingsState : PersistentStateComponent<ParadoxSettingsState> {
	@JvmField var preferOverridden = false
	@JvmField var renderLineCommentText = false
	@JvmField var renderDefinitionText = true
	@JvmField var renderLocalisationText = true
	
	override fun getState() = this

	override fun loadState(state: ParadoxSettingsState) = XmlSerializerUtil.copyBean(state, this)

	companion object {
		@JvmStatic
		fun getInstance(): ParadoxSettingsState = ServiceManager.getService(ParadoxSettingsState::class.java)
	}
}
