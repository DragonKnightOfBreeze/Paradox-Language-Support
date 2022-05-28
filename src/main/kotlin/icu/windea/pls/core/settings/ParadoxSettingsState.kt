package icu.windea.pls.core.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*
import icu.windea.pls.*
import icu.windea.pls.core.*

/**
 * @property defaultGameType 默认游戏类型。
 * @property scriptIgnoredFileNames 需要忽略的文件名（不识别为脚本和本地化文件，逗号分隔，不区分大小写）
 * @property preferOverridden 是否优先使用重载后的引用。
 * @property maxCompleteSize 本地化在进行代码补全时的最大补全数量。
 * @property scriptRenderLineComment 对于脚本语言，是否需要渲染之前的单行注释文本到文档注释中。
 * @property scriptRenderRelatedLocalisation 对于脚本语言，是否需要为定义渲染相关本地化文本到文档注释中。
 * @property scriptRenderRelatedPictures 对于脚本语言，是否需要为定义渲染相关图片到文档注释中。
 * @property localisationPrimaryLocale 对于本地化语言，默认的语言区域。（用于文档和内嵌提示）
 * @property localisationTruncateLimit 对于本地化语言，当渲染后的本地化文本过长且需要考虑截断时的截断长度。（用于内嵌提示）
 * @property localisationRenderLineComment 对于本地化语言，是否需要渲染之前的单行注释文本到文档注释中。
 * @property localisationRenderLocalisation 对于本地化语言，是否需要为本地化渲染本地化文本到文档注释中。
 */
@State(name = "ParadoxSettingsState", storages = [Storage("paradoxLanguageSupport.xml")])
data class ParadoxSettingsState(
	@JvmField var defaultGameType: ParadoxGameType = ParadoxGameType.Stellaris,
	@JvmField var preferOverridden: Boolean = false,
	@Deprecated("Consider for removal.")
	@JvmField var maxCompleteSize: Int = 100,
	@JvmField var scriptIgnoredFileNames: String = "readme.txt,changelog.txt,license.txt,credits.txt",
	@JvmField var scriptRenderLineComment: Boolean = false,
	@JvmField var scriptRenderRelatedLocalisation: Boolean = true,
	@JvmField var scriptRenderRelatedPictures: Boolean = true,
	@JvmField var localisationPrimaryLocale: String = "",
	@JvmField var localisationTruncateLimit: Int = 30,
	@JvmField var localisationRenderLineComment: Boolean = false,
	@JvmField var localisationRenderLocalisation: Boolean = true
) : PersistentStateComponent<ParadoxSettingsState> {
	var finalScriptIgnoredFileNames = scriptIgnoredFileNames.toCommaDelimitedStringSet(ignoreCase = true)
	
	val locales by lazy {
		buildList {
			add("")
			addAll(getInternalConfig().localeMap.keys)
		}
	}
	
	override fun getState() = this
	
	override fun loadState(state: ParadoxSettingsState) = XmlSerializerUtil.copyBean(state, this)
	
	companion object {
		@JvmStatic
		fun getInstance(): ParadoxSettingsState = ApplicationManager.getApplication().getService(ParadoxSettingsState::class.java)
	}
}
