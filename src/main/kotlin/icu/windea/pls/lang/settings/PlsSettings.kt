package icu.windea.pls.lang.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.settings.PlsStrategies.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * PLS设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "ParadoxSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsSettings : SimplePersistentStateComponent<PlsSettingsState>(PlsSettingsState())

/**
 * @property defaultGameType 默认的游戏类型。
 * @property defaultGameDirectories 默认的游戏目录映射。
 * @property preferredLocale 偏好的语言区域。
 * @property ignoredFileNames 需要忽略的文件名（不识别为脚本和本地化文件，逗号分隔，不区分大小写）
 */
class PlsSettingsState : BaseState() {
    var defaultGameType by enum(ParadoxGameType.Stellaris)
    var defaultGameDirectories by map<String, String>()
    var preferredLocale by string(ParadoxLocaleManager.ID_AUTO)
    var ignoredFileNames by string("readme.txt,changelog.txt,license.txt,credits.txt")

    val ignoredFileNameSet by ::ignoredFileNames.fromCommandDelimitedString(true)

    @get:Property(surroundWithTag = false)
    var documentation by property(DocumentationState())
    @get:Property(surroundWithTag = false)
    var completion by property(CompletionState())
    @get:Property(surroundWithTag = false)
    var folding by property(FoldingState())
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
     * @property renderNameDescForModifiers 是否需要为修正渲染相关本地化文本到文档中。
     * @property renderIconForModifiers 是否需要为修正渲染图标到文档中。
     * @property renderLocalisationForLocalisations 是否需要为本地化渲染本地化文本到文档中。
     * @property renderRelatedLocalisationsForComplexEnumValues 是否需要为复杂枚举值渲染相关本地化文本到文档中（同名的本地化）。
     * @property renderRelatedLocalisationsForDynamicValues 是否需要为动态值渲染相关本地化文本到文档中（同名的本地化）。
     * @property showScopes 是否需要在文档中显示作用域信息（如果支持且存在）。
     * @property showScopeContext 是否需要在文档中显示作用域上下文（如果支持且存在）。
     * @property showParameters 是否需要在文档中显示参数信息（如果支持且存在）。
     * @property showGeneratedModifiers 是否需要在文档中显示生成的修正的信息（如果支持且存在）。
     */
    @Tag("documentation")
    class DocumentationState : BaseState() {
        var renderLineComment by property(false)
        var renderRelatedLocalisationsForDefinitions by property(true)
        var renderRelatedImagesForDefinitions by property(true)
        var renderNameDescForModifiers by property(true)
        var renderIconForModifiers by property(true)
        var renderLocalisationForLocalisations by property(true)
        var renderRelatedLocalisationsForComplexEnumValues by property(true)
        var renderRelatedLocalisationsForDynamicValues by property(true)
        var showScopes by property(true)
        var showScopeContext by property(true)
        var showParameters by property(true)
        var showGeneratedModifiers by property(true)
    }

    /**
     * @property completeInlineScriptInvocations 进行代码补全时，是否需要提供对内联脚本调用的代码补全。
     * @property completeVariableNames 进行代码补全时，是否需要在效果的子句中提示变量名。
     * @property completeWithValue 进行代码补全时，如果可能，将会另外提供提示项，自动插入常量字符串或者花括号。
     * @property completeWithClauseTemplate 进行代码补全时，如果可能，将会另外提供提示项，自动插入从句模版。
     * @property completeOnlyScopeIsMatched 如果存在，是否仅提供匹配当前作用域的提示项。
     * @property completeByLocalizedName 是否也根据定义和修正的本地化名字来进行代码补全。
     * @property completeByExtendedConfigs 是否也根据扩展的规则来进行代码补全。
     */
    @Tag("completion")
    class CompletionState : BaseState() {
        var completeScriptedVariableNames by property(true)
        var completeDefinitionNames by property(true)
        var completeLocalisationNames by property(true)
        var completeInlineScriptInvocations by property(false)
        var completeVariableNames by property(true)
        var completeWithValue by property(true)
        var completeWithClauseTemplate by property(true)
        var completeOnlyScopeIsMatched by property(true)
        var completeByLocalizedName by property(false)
        var completeByExtendedConfigs by property(false)

        @get:Property(surroundWithTag = false)
        var clauseTemplate by property(ClauseTemplateState())

        /**
         * @property maxMemberCountInOneLine 当插入从句模版时，当要插入的从句中的属性的个数不超过时，会把所有属性放到同一行。
         */
        @Tag("clauseTemplate")
        class ClauseTemplateState : BaseState() {
            var maxMemberCountInOneLine by property(2)
        }
    }

    /**
     * 注意：某些折叠规则总是启用，不可配置。
     *
     * @property comment 是否允许折叠多行注释。适用于脚本文件和本地化文件。
     * @property commentByDefault 是否默认折叠多行注释。适用于脚本文件和本地化文件。
     * @property parameterConditionBlocksByDefault 是否允许折叠参数条件表达式块。
     * @property inlineMathBlocksByDefault 是否默认折叠内联数学表达式块。
     * @property localisationReferencesFully 是否允许折叠本地化引用。完全折叠。
     * @property localisationReferencesFullyByDefault 是否默认折叠本地化引用。完全折叠。
     * @property localisationIconsFully 是否允许折叠本地化图标。完全折叠。
     * @property localisationIconsFullyByDefault 是否默认折叠本地化图标。完全折叠。
     * @property localisationCommands 是否允许折叠本地化命令。
     * @property localisationCommandsByDefault 是否默认折叠本地化命令。
     * @property localisationConceptCommands 是否允许折叠本地化概念命令。
     * @property localisationConceptCommandsByDefault 是否默认折叠本地化概念命令。
     * @property localisationConceptTexts 是否允许折叠本地化概念的自定义文本。
     * @property localisationConceptTextsByDefault 是否默认折叠本地化概念的自定义文本。
     * @property scriptedVariableReferences 是否允许折叠封装变量引用。折叠为解析后的值。
     * @property scriptedVariableReferencesByDefault 是否默认折叠封装变量引用。折叠为解析后的值。
     * @property variableOperationExpressions 是否允许折叠变量操作表达式。折叠为简化形式。基于内置的规则文件。
     * @property variableOperationExpressionsByDefault 是否默认折叠变量操作表达式。折叠为简化形式。基于内置的规则文件。
     */
    @Tag("folding")
    class FoldingState : BaseState() {
        var comment by property(false)
        var commentByDefault by property(false)
        var parameterConditionBlocksByDefault by property(false)
        var inlineMathBlocksByDefault by property(true)
        var localisationReferencesFully by property(false)
        var localisationReferencesFullyByDefault by property(false)
        var localisationIconsFully by property(false)
        var localisationIconsFullyByDefault by property(false)
        var localisationCommands by property(false)
        var localisationCommandsByDefault by property(false)
        var localisationConceptCommands by property(false)
        var localisationConceptCommandsByDefault by property(false)
        var localisationConceptTexts by property(false)
        var localisationConceptTextsByDefault by property(false)
        var scriptedVariableReferences by property(true)
        var scriptedVariableReferencesByDefault by property(true)
        var variableOperationExpressions by property(true)
        var variableOperationExpressionsByDefault by property(true)
    }

    /**
     * @property localisationStrategy 生成本地化时如何生成本地化文本。
     * @property localisationStrategyText 生成本地化时如果使用特定文本填充本地化文本，这个特定文本是什么。
     * @property localisationStrategyLocale 生成本地化时如果基于特定语言区域的已有本地化文本，这个特定语言区域是什么。
     */
    @Tag("generation")
    class GenerationState : BaseState() {
        var fileNamePrefix by string("000000_")
        var localisationStrategy by enum(LocalisationGeneration.SpecificText)
        var localisationStrategyText by string("REPLACE_ME")
        var localisationStrategyLocale by string(ParadoxLocaleManager.ID_AUTO)
    }

    /**
     * @property showLocalizedName 是否在层级视图中额外显示本地化名字。
     * @property showLocationInfo 是否在层级视图中额外显示位置信息。
     * @property showLocationInfoByPath 显示位置信息时，是否显示路经。
     * @property showLocationInfoByRootInfo 显示位置信息时，是否显示游戏或模组信息。
     * @property showScriptedVariablesInCallHierarchy 是否在调用层级视图中显示封装变量。
     * @property showDefinitionsInCallHierarchyByBindings 是否在调用层级视图中显示定义。
     * @property showLocalisationsInCallHierarchy 是否在调用层级视图中显示本地化。
     * @property definitionTypeBindingsInCallHierarchy 调用层级视图中，定义类型的绑定。
     * @property showEventInfo 是否在事件树的层级视图中显示事件信息。
     * @property showEventInfoByType 显示事件信息时，是否显示事件的类型。
     * @property showEventInfoByAttributes 显示事件信息时，是否显示事件的特性。
     * @property showTechInfoByTier 显示科技信息时，是否显示科技的等级。
     * @property showTechInfoByArea 显示科技信息时，是否显示科技的研究领域。
     * @property showTechInfoByCategories 显示科技信息时，是否显示科技的类别。
     * @property showTechInfoByAttributes 显示科技信息时，是否显示科技的特性。
     * @property showTechInfo 是否在科技树的层级视图中显示科技信息。
     * @property eventTreeGrouping 事件树的层级视图使用的分组策略。
     * @property techTreeGrouping 科技树的层级视图使用的分组策略。
     */
    @Tag("hierarchy")
    class HierarchyState : BaseState() {
        var showLocalizedName by property(true)
        var showLocationInfo by property(true)
        var showLocationInfoByPath by property(true)
        var showLocationInfoByRootInfo by property(false)

        var showScriptedVariablesInCallHierarchy by property(true)
        var showDefinitionsInCallHierarchy by property(true)
        var showLocalisationsInCallHierarchy by property(true)
        var definitionTypeBindingsInCallHierarchy by map<String, String>() //TODO 1.3.35+ 优化：考虑更好的配置方式

        var showEventInfo by property(false)
        var showEventInfoByType by property(true)
        var showEventInfoByAttributes by property(false)

        var showTechInfo by property(false)
        var showTechInfoByTier by property(true)
        var showTechInfoByArea by property(true)
        var showTechInfoByCategories by property(true)
        var showTechInfoByAttributes by property(false)

        var eventTreeGrouping by enum(EventTreeGrouping.None)
        var techTreeGrouping by enum(TechTreeGrouping.None)

        fun showDefinitionsInCallHierarchyByBindings(rootDefinitionInfo: ParadoxDefinitionInfo?, definitionInfo: ParadoxDefinitionInfo?): Boolean {
            if (rootDefinitionInfo == null || definitionInfo == null) return true
            val bindings = definitionTypeBindingsInCallHierarchy
            if (bindings.isEmpty()) return true
            val matchedBindings = bindings.filterKeys { k -> ParadoxDefinitionTypeExpression.resolve(k).matches(rootDefinitionInfo) }
            if (matchedBindings.isEmpty()) return true
            return matchedBindings.values.any { v ->
                v.toCommaDelimitedStringSet().any { e -> ParadoxDefinitionTypeExpression.resolve(e).matches(definitionInfo) }
            }
        }
    }

    /**
     * 注意：仅可配置是否启用基于使用的推断，基于自定义规则的推断是始终启用的。
     *
     * @property configContextForParameters 是否推断参数值的规则上下文。
     * @property configContextForParametersFast 推断参数的规则上下文时，是否进行快速推断。
     * @property configContextForInlineScripts 是否推断内联脚本的规则上下文。
     * @property configContextForInlineScriptsFast 推断内联脚本的规则上下文时，是否进行快速推断。
     * @property scopeContext 是否推断scripted_trigger、scripted_effect等的作用域上下文。
     * @property scopeContextForEvents 是否推断event的作用域上下文。
     * @property scopeContextForOnActions 是否推断on_action的作用域上下文。
     */
    @Tag("inference")
    class InferenceState : BaseState() {
        var configContextForParameters by property(true)
        var configContextForParametersFast by property(true)
        var configContextForInlineScripts by property(true)
        var configContextForInlineScriptsFast by property(true)
        var scopeContext by property(false)
        var scopeContextForEvents by property(false)
        var scopeContextForOnActions by property(false)
    }

    /**
     * @property showEditorContextToolbar 是否在编辑器右上角显示上下文工具栏。
     * @property showLocalisationFloatingToolbar 是否在选中本地化文本时显示悬浮工具栏。
     * @property highlightLocalisationColorId 是否用对应的颜色高亮本地化颜色ID。
     * @property renderLocalisationColorfulText 是否用对应的颜色渲染本地化彩色文本。
     * @property defaultDiffGroup 进行DIFF时，初始打开的DIFF分组。默认初始打开VS副本的DIFF分组。
     */
    @Tag("others")
    class OthersState : BaseState() {
        var showEditorContextToolbar by property(true)
        var showLocalisationFloatingToolbar by property(true)
        var highlightLocalisationColorId by property(true)
        var renderLocalisationColorfulText by property(true)
        var defaultDiffGroup by enum(DiffGroup.VsCopy)
    }
}
