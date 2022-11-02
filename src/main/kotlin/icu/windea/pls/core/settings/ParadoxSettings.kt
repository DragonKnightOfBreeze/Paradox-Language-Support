package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*

/**
 * PLS设置。可以在设置页面`Settings > Languages & Frameworks > Paradox Language Support`中进行配置。
 * @property defaultGameType 默认游戏类型。
 * @property scriptIgnoredFileNames 需要忽略的文件名（不识别为脚本和本地化文件，逗号分隔，不区分大小写）
 * @property preferOverridden 是否优先使用重载后的引用。
 * @property maxCompleteSize 本地化在进行代码补全时的最大补全数量。
 * @property scriptRenderLineComment 对于脚本语言，是否需要渲染之前的单行注释文本到文档中。
 * @property scriptRenderRelatedLocalisation 对于脚本语言，是否需要为定义渲染相关本地化文本到文档中。
 * @property scriptRenderRelatedImages 对于脚本语言，是否需要为定义渲染相关图片到文档中。
 * @property scriptShowParameters 对于脚本语言，是否需要在文档中显示已使用的参数的列表（如果支持且存在）。
 * @property localisationPreferredLocale 对于本地化语言，默认的语言区域。（用于文档和内嵌提示）
 * @property localisationRenderLineComment 对于本地化语言，是否需要渲染之前的单行注释文本到文档中。
 * @property localisationRenderLocalisation 对于本地化语言，是否需要为本地化渲染本地化文本到文档中。
 */
@State(name = "ParadoxSettings", storages = [Storage("paradox-language-support.xml")])
class ParadoxSettings : SimplePersistentStateComponent<ParadoxSettings.State>(State()) {
	var defaultGameType by state::defaultGameType
	var preferOverridden by state::preferOverridden
	@Deprecated("Consider for removal.")
	var maxCompleteSize by state::maxCompleteSize
	
	var scriptIgnoredFileNames by state::scriptIgnoredFileNames
	var scriptRenderLineComment by state::scriptRenderLineComment
	var scriptRenderRelatedLocalisation by state::scriptRenderRelatedLocalisation
	var scriptRenderRelatedImages by state::scriptRenderRelatedImages
	var scriptShowParameters by state::scriptShowParameters
	
	var localisationPreferredLocale by state::localisationPreferredLocale
	var localisationRenderLineComment by state::localisationRenderLineComment
	var localisationRenderLocalisation by state::localisationRenderLocalisation
	
	var generationFileNamePrefix by state::generationFileNamePrefix
	
	val locales by lazy {
		buildList {
			add("auto")
			addAll(InternalConfigHandler.getLocaleMap(includeDefault = false).keys)
		}
	}
	
	class State : BaseState() {
		var defaultGameType: ParadoxGameType by enum(ParadoxGameType.Stellaris)
		var preferOverridden: Boolean by property(true)
		var maxCompleteSize: Int by property(100)
		
		var scriptIgnoredFileNames by string("readme.txt,changelog.txt,license.txt,credits.txt")
		var scriptRenderLineComment by property(false)
		var scriptRenderRelatedLocalisation by property(true)
		var scriptRenderRelatedImages by property(true)
		var scriptShowParameters by property(true)
		
		var localisationPreferredLocale by string("auto")
		var localisationRenderLineComment by property(false)
		var localisationRenderLocalisation by property(true)
		
		var generationFileNamePrefix by string("000000_")
	}
}
