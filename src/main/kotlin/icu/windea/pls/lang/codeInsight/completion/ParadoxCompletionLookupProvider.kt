package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.prefixFromArgument
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.codeInsight.completion.AddCharInsertHandler
import icu.windea.pls.core.codeInsight.completion.AddParenthesesInsertHandler
import icu.windea.pls.core.icon
import icu.windea.pls.core.orNull
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.lang.defineNamespaceInfo
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.formatter.ParadoxScriptCodeStyleSettings
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptString
import javax.swing.Icon

object ParadoxCompletionLookupProvider {
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

    // region Providers (keywords)

    fun forYesKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_YES
    fun forNoKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_NO
    fun forBlockKeyword(): LookupElementBuilder = LOOKUP_ELEMENT_BLOCK
    fun forKeyword(): List<LookupElementBuilder> = LOOKUP_ELEMENT_KEYWORD
    fun forBool(): List<LookupElementBuilder> = LOOKUP_ELEMENT_BOOL

    // endregion

    // region Providers (wrapped)

    fun fromScriptedVariable(context: ParadoxCompletionContext, element: ParadoxScriptScriptedVariable, hintText: String? = null): LookupElementBuilder? {
        // 不自动插入后面的等号
        val name = element.name?.orNull() ?: return null
        val tailText = element.value?.let { " = $it" }
        val typeFile = element.containingFile
        return LookupElementBuilder.create(element, name)
            .withTailText(tailText, true)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.ScriptedVariable)
            .withPatchableTailText(hintText)
            .withScriptedVariablePresentableNames(element)
            .wrapForExpression(context)
    }

    fun fromDefinition(context: ParadoxCompletionContext, element: ParadoxDefinitionElement, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        // skip anonymous definitions
        val definitionInfo = element.definitionInfo ?: return null
        val name = definitionInfo.name.orNull() ?: return null
        val typeFile = element.containingFile
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.Definition(definitionInfo.type))
            .withPatchableTailText(hintText ?: context.patchableTailText)
            .withScopeMatched(scopeMatched)
            .withDefinitionPresentableNames(element)
            .wrapForExpression(context)
    }

    fun fromLocalisation(context: ParadoxCompletionContext, element: ParadoxLocalisationProperty, hintText: String? = null): LookupElementBuilder? {
        val name = element.name.orNull() ?: return null
        val typeFile = element.containingFile
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.Localisation)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromDefineNamespace(context: ParadoxCompletionContext, element: ParadoxScriptProperty, hintText: String? = null): LookupElementBuilder? {
        // 不自动插入后面的等号
        val defineNamespaceInfo = element.defineNamespaceInfo ?: return null
        val name = defineNamespaceInfo.namespace.orNull() ?: return null
        val typeFile = element.containingFile
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineNamespace)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromDefineVariable(context: ParadoxCompletionContext, element: ParadoxScriptProperty, hintText: String? = null): LookupElementBuilder? {
        // 不自动插入后面的等号
        val defineVariableInfo = element.defineVariableInfo ?: return null
        val name = defineVariableInfo.variable.orNull() ?: return null
        val typeFile = element.containingFile
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile.name, typeFile.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DefineVariable)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromPathReference(context: ParadoxCompletionContext, element: PsiElement, name: String, typeFile: PsiFile?, icon: Icon?, hintText: String? = null): LookupElementBuilder? {
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(icon)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromStaticEnumValue(context: ParadoxCompletionContext, element: PsiElement, name: String, typeFile: PsiFile?, hintText: String? = null): LookupElementBuilder? {
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withPriority(ParadoxCompletionPriorities.enumValue)
            .withPatchableIcon(ChronicleIcons.Nodes.EnumValue)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromComplexEnumValue(context: ParadoxCompletionContext, element: PsiElement, name: String, typeFile: PsiFile?, enumName: String, hintText: String? = null, caseInsensitive: Boolean = false): LookupElementBuilder? {
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(!caseInsensitive)
            .withPriority(ParadoxCompletionPriorities.complexEnumValue)
            .withPatchableIcon(ChronicleIcons.Nodes.ComplexEnumValue(enumName))
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromPredefinedDynamicValue(context: ParadoxCompletionContext, element: PsiElement, name: String, typeFile: PsiFile?, hintText: String? = null, dynamicValueType: String? = null): LookupElementBuilder? {
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPatchableIcon(ChronicleIcons.Nodes.DynamicValue(dynamicValueType))
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromConstant(context: ParadoxCompletionContext, element: PsiElement, name: String, typeFile: PsiFile?, icon: Icon?): LookupElementBuilder? {
        val scopeMatched = context.scopeMatched
        return LookupElementBuilder.create(element, name)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withPriority(ParadoxCompletionPriorities.constant)
            .withPatchableIcon(icon)
            .withScopeMatched(scopeMatched)
            .wrapForExpression(context)
    }

    fun fromRootKey(context: ParadoxCompletionContext, key: String, element: PsiElement?, typeFile: PsiFile?, icon: Icon?, hintText: String? = null, forceInsertCurlyBraces: Boolean = false): LookupElementBuilder? {
        return LookupElementBuilder.create(key).withPsiElement(element)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withPatchableIcon(icon)
            .withPatchableTailText(hintText)
            .withForceInsertCurlyBraces(forceInsertCurlyBraces)
            .withPriority(ParadoxCompletionPriorities.rootKey)
            .wrapForExpression(context)
    }

    fun fromLightElement(context: ParadoxCompletionContext, element: LightElementBase, hintText: String? = null): LookupElementBuilder? {
        val name = element.name ?: return null
        val icon = element.icon
        return LookupElementBuilder.create(element, name)
            .withPatchableIcon(icon)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    fun fromInlineScriptMacro(context: ParadoxCompletionContext, config: CwtMacroConfig.InlineScript, hintText: String? = null): LookupElementBuilder? {
        val name = config.name
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.Macro)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withPatchableTailText(hintText)
            .withPriority(ParadoxCompletionPriorities.constant)
            .wrapForExpression(context)
    }

    fun fromDefinitionInjectionMode(context: ParadoxCompletionContext, config: CwtValueConfig, hintText: String? = null): LookupElementBuilder? {
        val name = config.value
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(ChronicleIcons.Nodes.Macro)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withInsertHandler(AddCharInsertHandler(':'))
            .withPriority(ParadoxCompletionPriorities.macro)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }

    // endregion

    // region Providers (unwrapped)

    fun forLocale(config: CwtLocaleConfig): LookupElementBuilder? {
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, config.name)
            .withIcon(ChronicleIcons.Nodes.LocalisationLocale)
            .withTailText(" " + config.text, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withInsertHandler(LocaleInsertHandler())
    }

    fun forLocalisationName(element: PsiElement, name: String, typeFile: PsiFile?, icon: Icon?): LookupElementBuilder {
        return LookupElementBuilder.create(element, name)
            .withIcon(icon)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
    }

    fun forConcept(element: PsiElement, name: String, typeFile: PsiFile?, tailText: String?, icon: Icon?): LookupElementBuilder {
        return LookupElementBuilder.create(element, name)
            .withIcon(icon)
            .withTailText(tailText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCompletionId()
    }

    fun forTextFormat(element: PsiElement, name: String, typeFile: PsiFile?, tailText: String?, icon: Icon?): LookupElementBuilder {
        return LookupElementBuilder.create(element, name)
            .withIcon(icon)
            .withTailText(tailText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCompletionId()
            .withCaseSensitivity(false) // ignore case
    }

    fun forHeaderColumn(config: CwtPropertyConfig): LookupElementBuilder? {
        val name = config.key
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.Column)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPriority(ParadoxCompletionPriorities.constant)
    }

    fun forNegated(): LookupElementBuilder {
        return LookupElementBuilder.create("not").bold()
            .withTailText("(...)", true)
            .withInsertHandler(AddParenthesesInsertHandler())
            .withPriority(ParadoxCompletionPriorities.keyword)
            .withCompletionId()
    }

    fun forSystemScope(config: CwtSystemScopeConfig, hintText: String? = null): LookupElementBuilder? {
        val name = config.name.orNull() ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.SystemScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withPriority(ParadoxCompletionPriorities.systemScope)
            .withCompletionId()
    }

    fun forStaticScope(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.name
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.StaticScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.scope)
            .withCompletionId()
    }

    fun forScopePrefixFromArgument(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean): LookupElementBuilder? {
        val name = config.prefixFromArgument ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(ChronicleIcons.Nodes.DynamicScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withInsertHandler(AddParenthesesInsertHandler())
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
    }

    fun forScopePrefixFromData(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.prefix ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(ChronicleIcons.Nodes.DynamicScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
    }

    fun forStaticValueField(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.name
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.StaticValueField)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withScopeMatched(scopeMatched)
            .withCompletionId()
    }

    fun forValueFieldPrefixFromArgument(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean): LookupElementBuilder? {
        val name = config.prefixFromArgument ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(ChronicleIcons.Nodes.DynamicValueField)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withInsertHandler(AddParenthesesInsertHandler())
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
    }

    fun forValueFieldPrefixFromData(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean): LookupElementBuilder? {
        val name = config.prefix ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(ChronicleIcons.Nodes.DynamicValueField)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
    }

    fun forSystemCommandScope(config: CwtSystemScopeConfig, hintText: String? = null): LookupElementBuilder? {
        val name = config.name.orNull() ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.SystemCommandScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withPriority(ParadoxCompletionPriorities.systemScope)
            .withCompletionId()
    }

    fun forStaticCommandScope(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        // optimize: make first char uppercase (e.g., owner -> Owner)
        val name = config.name.replaceFirstChar { it.uppercaseChar() }
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.StaticCommandScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.scope)
            .withCompletionId()
    }

    fun forCommandScopePrefixFromArgument(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean): LookupElementBuilder? {
        val name = config.prefixFromArgument ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(ChronicleIcons.Nodes.DynamicCommandScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withInsertHandler(AddParenthesesInsertHandler())
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
    }

    fun forCommandScopePrefixFromData(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.prefix ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(ChronicleIcons.Nodes.DynamicCommandScope)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withScopeMatched(scopeMatched)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
    }

    fun forCommandField(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.name
        val icon = ChronicleIcons.Nodes.StaticCommandField
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(icon)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withScopeMatched(scopeMatched)
            .withCompletionId()
    }

    fun forCommandFieldPrefixFromData(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.prefix ?: return null
        val icon = ChronicleIcons.Nodes.DynamicCommandField
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(icon)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withScopeMatched(scopeMatched)
            .withCompletionId()
    }

    fun forCommandFieldPrefixFromArgument(config: CwtLinkConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.prefixFromArgument ?: return null
        val icon = ChronicleIcons.Nodes.DynamicCommandField
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name).bold()
            .withIcon(icon)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withInsertHandler(AddParenthesesInsertHandler())
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withScopeMatched(scopeMatched)
            .withCompletionId()
    }

    fun forLocalisationCommand(config: CwtLocalisationCommandConfig, hintText: String? = null, scopeMatched: Boolean = true): LookupElementBuilder? {
        val name = config.name.orNull() ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.StaticCommandField)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withCaseSensitivity(false) // ignore case
            .withScopeMatched(scopeMatched)
            .withCompletionId()
    }

    fun forDatabaseObjectType(config: CwtDatabaseObjectTypeConfig, hintText: String? = null): LookupElementBuilder? {
        val name = config.name.orNull() ?: return null
        val element = config.pointer.element ?: return null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(element, name)
            .withIcon(ChronicleIcons.Nodes.DatabaseObjectType)
            .withTailText(hintText, true)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withPriority(ParadoxCompletionPriorities.prefix)
            .withCompletionId()
    }

    // endregion

    // region Providers (extended)

    fun forExtendedConfig(config: CwtConfig<*>, name: String, icon: Icon?): LookupElementBuilder {
        val element = config.pointer.element // can be null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(name).withPsiElement(element)
            .withIcon(icon)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withItemTextUnderlined(true)
            .withCompletionId()
    }

    fun fromExtendedConfig(context: ParadoxCompletionContext, config: CwtConfig<*>, name: String, icon: Icon?, hintText: String? = null): LookupElementBuilder? {
        val element = config.pointer.element // can be null
        val typeFile = config.pointer.containingFile
        return LookupElementBuilder.create(name).withPsiElement(element)
            .withTypeText(typeFile?.name, typeFile?.icon, true)
            .withItemTextUnderlined(true)
            .withPatchableIcon(icon)
            .withPatchableTailText(hintText)
            .wrapForExpression(context)
    }


    // endregion

    // region Wrappers

    fun getConfigBasedHintText(context: ParadoxCompletionContext, config: CwtConfig<*>?, withConfigExpression: Boolean = true, withFileName: Boolean = true): String {
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

    // endregion

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

    private class LocaleInsertHandler<T : LookupElement> : InsertHandler<T> {
        override fun handleInsert(c: InsertionContext, item: T) {
            // 如果之后没有英文冒号，则插入英文冒号（如果之后没有更多行，则还要插入换行符和必要的缩进），否则光标移到冒号之后
            val editor = c.editor
            val chars = editor.document.charsSequence
            val colonIndex = chars.indexOf(':', c.startOffset)
            if (colonIndex != -1) {
                editor.caretModel.moveToOffset(colonIndex + 1)
            } else {
                val settings = CodeStyle.getSettings(c.file)
                val indentOptions = settings.getIndentOptions(ParadoxLocalisationFileType)
                val insertLineBreak = editor.document.getLineNumber(editor.caretModel.offset) == editor.document.lineCount - 1
                val s = buildString {
                    append(":")
                    if (insertLineBreak) {
                        append("\n")
                        repeat(indentOptions.INDENT_SIZE) { append(" ") }
                    }
                }
                EditorModificationUtil.insertStringAtCaret(editor, s)
            }
        }
    }

    // endregion
}
