package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*
import icu.windea.pls.core.*

@State(name = "ParadoxSettingsState", storages = [Storage("paradoxLanguageSupport.xml")])
data class ParadoxSettingsState(
	@JvmField var defaultGameType: ParadoxGameType = ParadoxGameType.Stellaris,
	@JvmField var preferOverridden: Boolean = false,
	@Deprecated("Consider for removal.")
	@JvmField var maxCompleteSize:Int = 100,
	@JvmField var scriptRenderLineComment: Boolean = false,
	@JvmField var scriptRenderRelatedLocalisation: Boolean = true,
	@JvmField var scriptRenderRelatedPictures: Boolean = true,
	@JvmField var localisationRenderLineComment: Boolean = false,
	@JvmField var localisationRenderLocalisation: Boolean = true
) : PersistentStateComponent<ParadoxSettingsState> {
	override fun getState() = this

	override fun loadState(state: ParadoxSettingsState) = XmlSerializerUtil.copyBean(state, this)

	companion object {
		@JvmStatic
		fun getInstance(): ParadoxSettingsState = ServiceManager.getService(ParadoxSettingsState::class.java)
	}
}
