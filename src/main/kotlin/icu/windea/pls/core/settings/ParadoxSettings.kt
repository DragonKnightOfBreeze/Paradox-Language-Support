package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.model.*

/**
 * PLS设置。可以在设置页面`Settings > Languages & Frameworks > Paradox Language Support`中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "ParadoxSettings", storages = [Storage("paradox-language-support.xml")])
class ParadoxSettings : SimplePersistentStateComponent<ParadoxSettingsState>(ParadoxSettingsState())

/**
 * @property defaultGameType 默认游戏类型。
 * @property preferredLocale 偏好的语言区域。
 * @property ignoredFileNames 需要忽略的文件名（不识别为脚本和本地化文件，逗号分隔，不区分大小写）
 */
class ParadoxSettingsState : BaseState() {
    var defaultGameType: ParadoxGameType by enum(ParadoxGameType.Stellaris)
    var preferredLocale by string("auto")
    var ignoredFileNames by string("readme.txt,changelog.txt,license.txt,credits.txt")
    
    @get:Property(surroundWithTag = false)
    var documentation by property(DocumentationState())
    @get:Property(surroundWithTag = false)
    var completion by property(CompletionState())
    @get:Property(surroundWithTag = false)
    var generation by property(GenerationState())
    @get:Property(surroundWithTag = false)
    var inference by property(InferenceState())
    @get:Property(surroundWithTag = false)
    var hierarchy by property(HierarchyState())
    @get:Property(surroundWithTag = false)
    var others by property(OthersState())
    
    /**
     * @property renderLineComment 是否需要渲染之前的单行注释文本到文档中。
     * @property renderRelatedLocalisationsForDefinitions 是否需要为定义渲染相关本地化文本到文档中。
     * @property renderRelatedImagesForDefinitions 是否需要为定义渲染相关图片到文档中。
     * @property renderRelatedLocalisationsForModifiers 是否需要为修正渲染相关本地化文本到文档中。
     * @property renderIconForModifiers 是否需要为修正渲染图标到文档中。
     * @property renderLocalisationForLocalisations 是否需要为本地化渲染本地化文本到文档中。
     * @property showScopes 是否需要在文档中显示作用域信息（如果支持且存在）。
     * @property showScopeContext 是否需要在文档中显示作用域上下文（如果支持且存在）。
     * @property showParameters 是否需要在文档中显示参数信息（如果支持且存在。）
     */
    @Tag("documentation")
    class DocumentationState : BaseState() {
        var renderLineComment by property(false)
        var renderRelatedLocalisationsForDefinitions by property(true)
        var renderRelatedImagesForDefinitions by property(true)
        var renderRelatedLocalisationsForModifiers by property(true)
        var renderIconForModifiers by property(true)
        var renderLocalisationForLocalisations by property(true)
        var showScopes by property(true)
        var showScopeContext by property(true)
        var showParameters by property(true)
    }
    
    /**
     * @property completeVariableNames 进行代码补全时，是否需要在效果的子句中提示变量名。
     * @property completeWithValue 进行代码补全时，如果可能，将会另外提供提示项，自动插入常量字符串或者花括号。
     * @property completeWithClauseTemplate 进行代码补全时，如果可能，将会另外提供提示项，自动插入从句内联模版。
     * @property maxExpressionCountInOneLine 当插入从句内联模版时，当要插入的从句中的属性的个数不超过时，会把所有属性放到同一行。
     * @property completeOnlyScopeIsMatched 如果存在，是否仅提供匹配当前作用域的提示项。
     * @property completeByLocalizedName 是否也根据定义和修正的本地化名字来进行代码补全。
     */
    @Tag("completion")
    class CompletionState : BaseState() {
        var completeScriptedVariableNames by property(true)
        var completeDefinitionNames by property(true)
        var completeLocalisationNames by property(false)
        var completeVariableNames by property(true)
        var completeWithValue by property(true)
        var completeWithClauseTemplate by property(true)
        var maxExpressionCountInOneLine by property(2)
        var completeOnlyScopeIsMatched by property(true)
        var completeByLocalizedName by property(false)
    }
    
    /**
     * @property localisationTextGenerationStrategy 生成本地化时如何生成本地化文本。
     * @property localisationText 生成本地化时如果使用特定文本填充本地化文本，这个特定文本是什么。
     * @property localisationTextLocale 生成本地化时如果基于特定语言区域的已有本地化文本，这个特定语言区域是什么。
     */
    @Tag("generation")
    class GenerationState : BaseState() {
        var localisationTextGenerationStrategy by enum(LocalisationTextGenerationStrategy.SpecificText)
        var localisationText by string("REPLACE_ME")
        var localisationTextLocale by string("auto")
        var fileNamePrefix by string("000000_")
    }
    
    /**
     * @property argumentValueConfig 是否推断参数值对应的脚本表达式，以便为其提供高级语言功能支持。
     * @property inlineScriptConfig 是否推断内联脚本的使用位置，以便为其提供高级语言功能支持。
     * @property scopeContext 是否基于scripted_trigger、scripted_effect等的调用推断它们的作用域上下文。
     * @property eventScopeContextFromOnAction 是否基于在on_action中的调用推断事件的作用域上下文。
     * @property eventScopeContextFromEffect 是否基于在effect子句中的调用推断事件的作用域上下文。
     * @property onActionScopeContextFromEffect 是否基于在effect子句中的调用推断on_action的作用域上下文。
     */
    @Tag("inference")
    class InferenceState : BaseState() {
        var argumentValueConfig by property(true)
        var inlineScriptConfig by property(true)
        var scopeContext by property(false)
        var eventScopeContextFromOnAction by property(false)
        var eventScopeContextFromEffect by property(false)
        var onActionScopeContextFromEffect by property(false)
    }
    
    @Tag("hierarchy")
    class HierarchyState : BaseState() {
        var showScriptedVariablesInCallHierarchy by property(true)
        var showDefinitionsInCallHierarchy by property(true)
        var showLocalisationsInCallHierarchy by property(true)
        var definitionTypeBindingsInCallHierarchy by map<String, String>()
        
        fun showDefinitionsInCallHierarchy(rootDefinitionInfo: ParadoxDefinitionInfo?, definitionInfo: ParadoxDefinitionInfo?): Boolean {
            if(rootDefinitionInfo == null || definitionInfo == null) return true
            val bindings = definitionTypeBindingsInCallHierarchy
            if(bindings.isEmpty()) return true
            val matchedBindings = bindings.filterKeys { k -> ParadoxDefinitionTypeExpression.resolve(k).matches(rootDefinitionInfo) }
            if(matchedBindings.isEmpty()) return true
            return matchedBindings.values.any { v ->
                v.toCommaDelimitedStringSet().any { e -> ParadoxDefinitionTypeExpression.resolve(e).matches(definitionInfo) }
            }
        }
    }
    
    /**
     * @property defaultDiffGroup 进行DIFF时，初始打开的DIFF分组。默认初始打开VS副本的DIFF分组。
     */
    @Tag("others")
    class OthersState : BaseState() {
        var showEditorFloatingToolbar by property(true)
        var defaultDiffGroup by enum(DiffGroupStrategy.VsCopy)
    }
    
    val ignoredFileNameSet by ::ignoredFileNames.observe { it?.toCommaDelimitedStringSet(caseInsensitiveStringSet()).orEmpty() }
    
    val localeList by lazy {
        buildList {
            add("auto")
            addAll(getCwtConfig().core.localisationLocalesNoDefault.keys)
        }
    }
}

enum class LocalisationTextGenerationStrategy {
    EmptyText,
    SpecificText,
    FromLocale,
}

enum class DiffGroupStrategy {
    VsCopy,
    First,
    Last,
}