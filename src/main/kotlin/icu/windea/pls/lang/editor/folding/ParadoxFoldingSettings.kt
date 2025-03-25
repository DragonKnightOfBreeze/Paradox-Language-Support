package icu.windea.pls.lang.editor.folding

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

/**
 * @property parameterConditionBlocks 是否默认折叠参数条件表达式块。默认不启用。
 * @property inlineMathBlocks 是否默认折叠内联数学表达式块。默认启用。
 * @property localisationReferencesFullyEnabled 是否允许折叠本地化引用。完全折叠。默认不启用。
 * @property localisationReferencesFully 是否默认折叠本地化引用。完全折叠。默认不启用。
 * @property localisationIconsFullyEnabled 是否允许折叠本地化图标。完全折叠。默认不启用。
 * @property localisationIconsFully 是否默认折叠本地化图标。完全折叠。默认不启用。
 * @property localisationCommandsEnabled 是否允许折叠本地化命令。默认不启用。
 * @property localisationCommands 是否默认折叠本地化命令。默认不启用。
 * @property localisationConceptsEnabled 是否允许折叠本地化概念。默认不启用。
 * @property localisationConcepts 是否默认折叠本地化概念。默认不启用。
 * @property localisationConceptTextsEnabled 是否允许折叠本地化概念的自定义文本。默认不启用。
 * @property localisationConceptTexts 是否默认折叠本地化概念的自定义文本。默认不启用。
 * @property scriptedVariableReferencesEnabled 是否允许折叠封装变量引用。折叠为解析后的值。默认启用。
 * @property scriptedVariableReferences 是否默认折叠封装变量引用。折叠为解析后的值。默认启用。
 * @property variableOperationExpressionsEnabled 是否允许折叠变量操作表达式。折叠为简化形式。默认启用。基于内置的规则文件。
 * @property variableOperationExpressions 是否默认折叠变量操作表达式。折叠为简化形式。默认启用。基于内置的规则文件。
 */
@State(name = "ParadoxFoldingSettings", storages = [Storage("editor.xml")], category = SettingsCategory.CODE)
class ParadoxFoldingSettings : PersistentStateComponent<ParadoxFoldingSettings> {
    var parameterConditionBlocks = false
    var inlineMathBlocks = true
    var localisationReferencesFullyEnabled = false
    var localisationReferencesFully = false
    var localisationIconsFullyEnabled = false
    var localisationIconsFully = false
    var localisationCommandsEnabled = false
    var localisationCommands = false
    var localisationConceptsEnabled = false
    var localisationConcepts = false
    var localisationConceptTextsEnabled = false
    var localisationConceptTexts = false
    var scriptedVariableReferencesEnabled = true
    var scriptedVariableReferences = true
    var variableOperationExpressionsEnabled = true
    var variableOperationExpressions = true

    override fun getState() = this

    override fun loadState(state: ParadoxFoldingSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        @JvmStatic
        fun getInstance() = service<ParadoxFoldingSettings>()
    }
}
