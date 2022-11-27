package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.model.*

/**
 * PLS设置。可以在设置页面`Settings > Languages & Frameworks > Paradox Language Support`中进行配置。
 */
@State(name = "ParadoxSettings", storages = [Storage("paradox-language-support.xml")])
class ParadoxSettings : SimplePersistentStateComponent<ParadoxSettingsState>(ParadoxSettingsState())

/**
 * @property defaultGameType 默认游戏类型。
 * @property preferredLocale 偏好的语言区域。
 * @property ignoredFileNames 需要忽略的文件名（不识别为脚本和本地化文件，逗号分隔，不区分大小写）
 * @property preferOverridden 是否优先使用重载后的引用。
 */
class ParadoxSettingsState : BaseState() {
	var defaultGameType: ParadoxGameType by enum(ParadoxGameType.Stellaris)
	var preferredLocale by string("auto")
	var ignoredFileNames by string("readme.txt,changelog.txt,license.txt,credits.txt")
	var preferOverridden: Boolean by property(true)
	
	@get:Tag("documentation")
	var documentation by property(DocumentationState())
	
	/**
	 * @property renderLineComment 是否需要渲染之前的单行注释文本到文档中。
	 * @property renderRelatedLocalisationsForDefinitions 是否需要为定义渲染相关本地化文本到文档中。
	 * @property renderRelatedImagesForDefinitions 是否需要为定义渲染相关图片到文档中。
	 * @property renderRelatedLocalisationsForModifiers 是否需要为修饰符渲染相关本地化文本到文档中。
	 * @property renderLocalisationForLocalisations 是否需要为本地化渲染本地化文本到文档中。
	 * @property showParameters 是否需要在文档中显示参数信息（如果支持且存在）。
	 * @property showScopes 是否需要在文档中显示作用域信息（如果支持且存在）。
	 */
	class DocumentationState: BaseState(){
		var renderLineComment by property(false)
		var renderRelatedLocalisationsForDefinitions by property(true)
		var renderRelatedImagesForDefinitions by property(true)
		var renderRelatedLocalisationsForModifiers by property(true)
		var renderLocalisationForLocalisations by property(true)
		var showParameters by property(true)
		var showScopes by property(true)
	}
	
	@get:Tag("completion")
	var completion by property(CompletionState())
	
	/**
	 * @property maxCompleteSize 本地化在进行代码补全时的最大补全数量。
	 */
	class CompletionState: BaseState(){
		var maxCompleteSize: Int by property(100)
	}
	
	@get:Tag("generation")
	var generation by property(GenerationState())
	
	class GenerationState: BaseState(){
		var fileNamePrefix by string("000000_")
	}
	
	val locales by lazy {
		buildList {
			add("auto")
			addAll(InternalConfigHandler.getLocaleMap(includeDefault = false).keys)
		}
	}
}
