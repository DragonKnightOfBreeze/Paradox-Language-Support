package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.formatter.ParadoxScriptCodeStyleSettings
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptString
import javax.swing.Icon

@Suppress("unused")
object ParadoxCompletionLookupProvider {
    // TODO 3.0.1 重构……避免某些 manager 过大……

    // region Constants

    private val LOOKUP_ELEMENT_YES = LookupElementBuilder.create("yes").bold()
        .withPriority(ParadoxCompletionPriorities.keyword).withCompletionId()
    private val LOOKUP_ELEMENT_NO = LookupElementBuilder.create("no").bold()
        .withPriority(ParadoxCompletionPriorities.keyword).withCompletionId()
    private val LOOKUP_ELEMENT_BLOCK = LookupElementBuilder.create("").withPresentableText(ChronicleStrings.blockFolder)
        .withPriority(ParadoxCompletionPriorities.keyword).withCompletionId(ChronicleStrings.blockFolder)
        .withInsertHandler(BlockInsertHandler())
    private val LOOKUP_ELEMENT_KEYWORD = listOf(LOOKUP_ELEMENT_YES, LOOKUP_ELEMENT_NO, LOOKUP_ELEMENT_BLOCK)
    private val LOOKUP_ELEMENT_BOOL = listOf(LOOKUP_ELEMENT_YES, LOOKUP_ELEMENT_NO)

    // endregion

    fun forYesKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_YES
    fun forNoKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_NO
    fun forBlockKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_BLOCK
    fun forKeyword(): List<LookupElementBuilder> = LOOKUP_ELEMENT_KEYWORD
    fun forBool(): List<LookupElementBuilder> = LOOKUP_ELEMENT_BOOL

    fun processScriptedVariable(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptScriptedVariable): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name?.orNull() ?: return true
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTailText(tailText, true)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.ScriptedVariable)
            .withScriptedVariablePresentableNames(element)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefinition(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxDefinitionElement): Boolean {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return true
        val name = element.name.orNull() ?: return true // skip anonymous definitions
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.Definition(definitionInfo.type))
            .withPatchableTailText(context.patchableTailText)
            .withDefinitionPresentableNames(element)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefineNamespace(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineNamespace)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun processDefineVariable(context: ParadoxCompletionContext, result: CompletionResultSet, element: ParadoxScriptProperty): Boolean {
        // 不自动插入后面的等号
        ProgressManager.checkCanceled()
        val name = element.name.orNull() ?: return true
        val typeFile = element.containingFile
        val lookupElement = LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineVariable)
            .wrapForExpression(context)
        result.addElement(lookupElement, context)
        return true
    }

    fun getConfigBasedPatchableTailText(context: ParadoxCompletionContext, config: CwtConfig<*>?, withConfigExpression: Boolean = true, withFileName: Boolean = true): String {
        context.patchableTailText?.let { return it }

        return buildString {
            if (withConfigExpression) {
                val configExpression = config?.configExpression
                if (configExpression != null) {
                    append(" by ").append(configExpression)
                }
            }
            if (withFileName) {
                val fileName = config?.resolved()?.pointer?.containingFile?.name
                if (fileName != null) {
                    append(" in ").append(fileName)
                }
            }
        }
    }

    fun wrapForExpression(lookupElement: LookupElementBuilder, context: ParadoxCompletionContext): LookupElementBuilder? {
        // check whether scope is matched again here
        if ((!lookupElement.scopeMatched || !context.scopeMatched) && ChronicleSettings.getInstance().state.completion.completeOnlyScopeIsMatched) return null

        val config = context.config
        val completeWithValue = ChronicleSettings.getInstance().state.completion.completeWithValue
        val targetConfig = when {
            config is CwtPropertyConfig -> config
            config is CwtAliasConfig -> config.config
            config is CwtSingleAliasConfig -> config.config
            config is CwtMacroConfig -> config.config
            else -> null
        }?.let { c -> CwtConfigManipulationService.inlineForConfig(c) } // 这里需要进行必要的内联

        val contextElement = context.contextElement
        val isKeyElement = contextElement is ParadoxScriptPropertyKey
        val isStringElement = contextElement is ParadoxScriptString
        val isBlockConfig = targetConfig?.let { it.valueType == CwtExpressionType.Block } ?: false

        val lookupString = when {
            context.leftQuoted -> lookupElement.lookupString // already quoted
            else -> lookupElement.lookupString.quoteIfNeeded() // #369 should be quoted if is blank or contains blank
        }
        val constantValue = when {
            completeWithValue -> targetConfig?.valueExpression?.takeIf { it.type == CwtDataTypes.Constant }?.expressionString
            else -> null
        }
        val insertCurlyBraces = when {
            lookupElement.forceInsertCurlyBraces -> true
            completeWithValue -> isBlockConfig
            else -> false
        }
        val withValueText = when {
            isKeyElement || (isStringElement && context.isKey != true) -> ""
            constantValue != null -> " = $constantValue"
            insertCurlyBraces -> " = {...}"
            else -> ""
        }

        // 排除重复项
        val completionId = lookupString + withValueText
        if (!context.completionIds.add(completionId)) return null

        var result = lookupElement

        result = result.withBaseLookupString(lookupString) // #369
        result = result.patchIcon(config)
        result = result.patchTailText(withValueText)
        result = result.addPresentableNames()

        if (!isKeyElement && !isStringElement) return result // not in a key or value position
        if (context.isKey == null) return result // not complete full key or value

        if (isKeyElement || !context.isKey) { // key or value only
            result = result.withInsertHandler(KeyOrValueOnlyInsertHandler(context))
        } else { // key with value
            result = result.withInsertHandler(KeyWithValueInsertHandler(context, insertCurlyBraces))
        }

        val extraLookupElements = mutableListOf<LookupElement>()

        // 进行提示并在提示后插入子句内联模板（仅当子句中允许键为常量字符串的属性时才会提示）
        if (context.isKey && !isKeyElement && isBlockConfig && config != null) {
            val extraLookupElement = ParadoxClauseTemplateCompletionManager.buildLookupElement(context, config, result)
            if (extraLookupElement != null) extraLookupElements.add(extraLookupElement)
        }

        result.extraLookupElements = extraLookupElements
        return result
    }

    private fun LookupElementBuilder.patchIcon(config: CwtConfig<*>?): LookupElementBuilder {
        val patchableIcon = patchableIcon
        if (patchableIcon == null) return this
        val patchedIcon = getPatchedIcon(patchableIcon, config)
        return withIcon(patchedIcon)
    }

    private fun getPatchedIcon(icon: Icon?, config: CwtConfig<*>?): Icon? {
        if (icon == null) return null
        when (config) {
            is CwtValueConfig -> {
                if (config.tagType != null) return ChronicleIcons.Nodes.Tag
            }
            is CwtAliasConfig -> {
                val aliasConfig = config
                val type = aliasConfig.configExpression.type
                if (type !in CwtDataTypeSets.ConstantAware) return icon
                val aliasName = aliasConfig.name
                return when {
                    aliasName == "modifier" -> ChronicleIcons.Nodes.Modifier
                    aliasName == "trigger" -> ChronicleIcons.Nodes.Trigger
                    aliasName == "effect" -> ChronicleIcons.Nodes.Effect
                    else -> icon
                }
            }
        }
        return icon
    }

    private fun LookupElementBuilder.patchTailText(withValueText: String): LookupElementBuilder {
        val patchableTailText = patchableTailText
        val patchedTailText = getPatchedTailText(withValueText, patchableTailText)
        if (patchedTailText.isEmpty()) return this
        return withTailText(patchedTailText, true)
    }

    private fun getPatchedTailText(withValueText: String, patchableTailText: String?): String = buildString {
        append(withValueText)
        if (patchableTailText != null) append(patchableTailText)
    }

    private fun LookupElementBuilder.addPresentableNames(): LookupElementBuilder {
        val presentableNames = presentableNames
        if (presentableNames.isNullOrEmpty()) return this
        return withLookupStrings(presentableNames)
    }

    // region Insert Handlers

    private open class BlockInsertHandler<T : LookupElement> : InsertHandler<T> {
        override fun handleInsert(c: InsertionContext, item: T) {
            // 插入成对的花括号
            val codeStyleSettings = ParadoxScriptCodeStyleSettings.getInstance(c.file)
            val spaceWithinBraces = codeStyleSettings.SPACE_WITHIN_BRACES
            val text = if (spaceWithinBraces) "{  }" else "{}"
            val length = if (spaceWithinBraces) text.length - 2 else text.length - 1
            EditorModificationUtil.insertStringAtCaret(c.editor, text, false, true, length)
        }
    }

    private open class KeyOrValueOnlyInsertHandler<T : LookupElement>(
        private val context: ParadoxCompletionContext,
    ) : InsertHandler<T> {
        override fun handleInsert(c: InsertionContext, item: T) {
            // `isKey` 如果是 `null`，则表示已经填充的只是键或值的其中一部分
            if (!context.leftQuoted) return
            val editor = c.editor
            val caretOffset = editor.caretModel.offset
            val charsSequence = editor.document.charsSequence
            val rightQuoted = charsSequence.get(caretOffset) == '"' && charsSequence.get(caretOffset - 1) != '\\'
            if (rightQuoted) {
                // 在必要时将光标移到右双引号之后
                if (context.isKey != null) editor.caretModel.moveToOffset(caretOffset + 1)
            } else {
                // 插入缺失的右双引号，且在必要时将光标移到右双引号之后
                EditorModificationUtil.insertStringAtCaret(editor, "\"", false, context.isKey != null)
            }
        }
    }

    private open class KeyWithValueInsertHandler<T : LookupElement>(
        context: ParadoxCompletionContext,
        private val insertCurlyBraces: Boolean,
    ) : KeyOrValueOnlyInsertHandler<T>(context) {
        override fun handleInsert(c: InsertionContext, item: T) {
            // call super first
            super.handleInsert(c, item)

            val editor = c.editor
            val codeStyleSettings = ParadoxScriptCodeStyleSettings.getInstance(c.file)
            val spaceAroundPropertySeparator = codeStyleSettings.SPACE_AROUND_PROPERTY_SEPARATOR
            val spaceWithinBraces = codeStyleSettings.SPACE_WITHIN_BRACES
            val text = buildString {
                if (spaceAroundPropertySeparator) append(" ")
                append("=")
                if (spaceAroundPropertySeparator) append(" ")
                if (insertCurlyBraces) {
                    if (spaceWithinBraces) append("{  }") else append("{}")
                }
            }
            val length = if (insertCurlyBraces) {
                if (spaceWithinBraces) text.length - 2 else text.length - 1
            } else {
                text.length
            }
            EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
        }
    }

    // endregion
}
