package icu.windea.pls.core.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*
import icu.windea.pls.core.*

@State(name = "ParadoxSettingsState", storages = [Storage("paradoxLanguageSupport.xml")])
data class ParadoxSettingsState(
	var defaultGameType: ParadoxGameType = ParadoxGameType.Stellaris,
	var preferOverridden: Boolean = false,
	@Deprecated("Consider for removal.")
	var maxCompleteSize:Int = 100,
	var scriptRenderLineComment: Boolean = false,
	var scriptRenderRelatedLocalisation: Boolean = true,
	var scriptRenderRelatedPictures: Boolean = true,
	var localisationRenderLineComment: Boolean = false,
	var localisationRenderLocalisation: Boolean = true
) : PersistentStateComponent<ParadoxSettingsState> {
	override fun getState() = this

	override fun loadState(state: ParadoxSettingsState) = XmlSerializerUtil.copyBean(state, this)

	companion object {
		@JvmStatic
		fun getInstance(): ParadoxSettingsState = ApplicationManager.getApplication().getService(ParadoxSettingsState::class.java)
	}
}
