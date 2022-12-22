package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.util.*
import kotlin.properties.*

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
	 * @property renderIconForModifiers 是否需要为修饰符渲染图标到文档中。
	 * @property renderLocalisationForLocalisations 是否需要为本地化渲染本地化文本到文档中。
	 * @property showScopes 是否需要在文档中显示作用域信息（如果支持且存在）。
	 */
	class DocumentationState : BaseState() {
		var renderLineComment by property(false)
		var renderRelatedLocalisationsForDefinitions by property(true)
		var renderRelatedImagesForDefinitions by property(true)
		var renderRelatedLocalisationsForModifiers by property(true)
		var renderIconForModifiers by property(true)
		var renderLocalisationForLocalisations by property(true)
		var showScopes by property(true)
	}
	
	@get:Tag("completion")
	var completion by property(CompletionState())
	
	/**
	 * @property maxCompleteSize 本地化在进行代码补全时的最大补全数量。
	 * @property completeWithValue 当补全定义属性时，如果可能的值可以是常量字符串或者子句，是否另外提供提示项，自动插入常量字符串或花括号。
	 * @property completeWithClauseTemplate 当补全定义属性时，如果可能的值可以是子句，且其中的属性名可以是常量字符串，且是有限的，是否另外提供提示项，自动插入从句内联模版。
	 * @property maxExpressionCountInOneLine 当插入从句内联模版时，当要插入的从句中的属性的个数不超过时，会把所有属性放到同一行
	 */
	class CompletionState : BaseState() {
		var maxCompleteSize by property(100)
		var completeWithValue by property(true)
		var completeWithClauseTemplate by property(true)
		var maxExpressionCountInOneLine by property(2)
	}
	
	@get:Tag("generation")
	var generation by property(GenerationState())
	
	class GenerationState : BaseState() {
		var fileNamePrefix by string("000000_")
	}
	
	val ignoredFileNameSet by ::ignoredFileNames.observe { it?.toCommaDelimitedStringSet().orEmpty() }
	var oldIgnoredFileNameSet = ignoredFileNameSet
	
	val locales by lazy {
		buildList {
			add("auto")
			addAll(getCwtConfig().core.localisationLocalesNoDefault.keys)
		}
	}
}
