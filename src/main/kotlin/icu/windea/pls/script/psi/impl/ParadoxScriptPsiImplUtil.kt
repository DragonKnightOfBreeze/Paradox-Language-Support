package icu.windea.pls.script.psi.impl

import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.settings.ChronicleInternalSettings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.constants.DefaultStrings
import icu.windea.pls.core.containsLineBreak
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processChild
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.select.listBy
import icu.windea.pls.core.select.oneBy
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.substringIn
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.transformAndKeepQuotes
import icu.windea.pls.core.truncate
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.lang.codeInsight.color.ParadoxColorService
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiService
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptConditionalExpression
import icu.windea.pls.script.psi.ParadoxScriptConditionalParameter
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementManipulationService
import icu.windea.pls.script.psi.ParadoxScriptElementPresentation
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineMathExpression
import icu.windea.pls.script.psi.ParadoxScriptInlineMathNumber
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptNormalParameter
import icu.windea.pls.script.psi.ParadoxScriptParameterArgument
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptPsiService
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptStatement
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.text.ParadoxScript
import java.awt.Color
import javax.swing.Icon

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptPsiImplUtil {
    // region ParadoxScriptFile

    fun getBlock(element: ParadoxScriptFile): ParadoxScriptRootBlock? {
        return element.children().oneBy()
    }

    @JvmStatic
    fun getMemberContainer(element: ParadoxScriptFile): ParadoxScriptRootBlock? {
        return getBlock(element)
    }

    @JvmStatic
    fun getMembers(element: ParadoxScriptFile): List<ParadoxScriptMember> {
        val memberContainer = getMemberContainer(element)
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxScriptFile, another: PsiElement?): Boolean {
        if (element === another) return true
        if (another !is ParadoxScriptFile) return false
        return ParadoxFileManager.isEquivalentFile(element, another)
    }

    // endregion

    // region ParadoxScriptRootBlock

    @JvmStatic
    fun getMemberContainer(element: ParadoxScriptRootBlock): ParadoxScriptRootBlock {
        return element
    }

    @JvmStatic
    fun getMembers(element: ParadoxScriptRootBlock): List<ParadoxScriptMember> {
        val memberContainer = getMemberContainer(element)
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun getComponents(element: ParadoxScriptRootBlock): List<PsiElement> {
        return element.children().listBy<ParadoxScriptStatement>()
    }

    // endregion

    // region ParadoxScriptProperty

    @JvmStatic
    fun getMemberContainer(element: ParadoxScriptProperty): ParadoxScriptBlock? {
        return element.propertyValue.castOrNull()
    }

    @JvmStatic
    fun getMembers(element: ParadoxScriptProperty): List<ParadoxScriptMember>? {
        val memberContainer = getMemberContainer(element) ?: return null
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun getBlock(element: ParadoxScriptProperty): ParadoxScriptBlock? {
        return element.children(forward = false).oneBy()
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptProperty, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Property
    }

    @JvmStatic
    fun getName(element: ParadoxScriptProperty): String {
        element.stub?.name?.orNull()?.let { return it }
        return element.propertyKey.name
    }

    @JvmStatic
    fun setName(element: ParadoxScriptProperty, name: String): ParadoxScriptProperty {
        element.definitionInfo?.let { return ParadoxPsiService.renameDefinition(element, name, it) }
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun getNameIdentifier(element: ParadoxScriptProperty): PsiElement? {
        return element.propertyKey.idElement
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptProperty): String? {
        return element.propertyValue?.value
    }

    @JvmStatic
    fun getIElementType(element: ParadoxScriptProperty): IElementType {
        return PROPERTY
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxScriptProperty, another: PsiElement?): Boolean {
        if (element === another) return true
        if (another !is ParadoxScriptProperty) return false
        // for definitions: by definitionInfo
        // for others: never
        if (element.definitionInfo.let { it == null || it != another.definitionInfo }) return false
        // if (selectGameType(element) != selectGameType(another)) return false // unnecessary
        return true
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptProperty): String {
        var keyElement: ParadoxScriptPropertyKey? = null
        var separatorElement: PsiElement? = null
        var valueElement: ParadoxScriptValue? = null
        element.forEachChild { e ->
            when {
                e is ParadoxScriptPropertyKey -> keyElement = e
                ParadoxScriptPsiService.isPropertySeparator(e) -> separatorElement = e
                e is ParadoxScriptValue -> valueElement = e
            }
        }
        return buildString {
            if (keyElement != null) append(keyElement.presentableText) else append(DefaultStrings.unresolved)
            if (separatorElement?.elementType != SAFE_CALL_ASSIGN_SIGN) append(" ")
            append(separatorElement?.text ?: "=")
            append(" ")
            if (valueElement != null) append(valueElement.presentableText) else append(DefaultStrings.unresolved)
        }
    }

    // endregion

    // region ParadoxScriptPropertyKey

    @JvmStatic
    fun getIdElement(element: ParadoxScriptPropertyKey): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == PROPERTY_KEY_TOKEN }?.takeIf { ParadoxScriptPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptPropertyKey, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Property
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptPropertyKey): String {
        return element.text.unquote(QuotePatterns.ParadoxScript)
    }

    @JvmStatic
    fun setValue(element: ParadoxScriptPropertyKey, value: String): ParadoxScriptPropertyKey {
        return ParadoxScriptElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: ParadoxScriptPropertyKey, content: String, range: TextRange): ParadoxScriptPropertyKey {
        return ParadoxScriptElementManipulationService.changeContent(element, content, range)
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptPropertyKey): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.transformAndKeepQuotes { it.truncate(limit) }
    }

    // endregion

    // region ParadoxScriptValue

    @JvmStatic
    fun getIcon(element: ParadoxScriptValue, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Value
    }

    @JvmStatic
    fun setValue(element: ParadoxScriptValue, value: String): ParadoxScriptValue {
        return ParadoxScriptElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: ParadoxScriptValue, content: String, range: TextRange): ParadoxScriptValue {
        return ParadoxScriptElementManipulationService.changeContent(element, content, range)
    }

    // endregion

    // region ParadoxScriptString

    @JvmStatic
    fun getIdElement(element: ParadoxScriptString): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == STRING_TOKEN }?.takeIf { ParadoxScriptPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptString, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Value
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptString): String {
        if (element.text.containsLineBreak()) return "..."
        return element.text.unquote(QuotePatterns.ParadoxScript)
    }

    @JvmStatic
    fun setValue(element: ParadoxScriptString, value: String): ParadoxScriptValue {
        return ParadoxScriptElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: ParadoxScriptString, content: String, range: TextRange): ParadoxScriptValue {
        return ParadoxScriptElementManipulationService.changeContent(element, content, range)
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptString): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.transformAndKeepQuotes { it.truncate(limit) }
    }

    // endregion

    // region ParadoxScriptBlock

    @JvmStatic
    fun getMemberContainer(element: ParadoxScriptBlock): ParadoxScriptBlock {
        return element
    }

    @JvmStatic
    fun getMembers(element: ParadoxScriptBlock): List<ParadoxScriptMember> {
        val memberContainer = getMemberContainer(element)
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun getLeftBound(element: ParadoxScriptBlock): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == LEFT_BRACE }
    }

    @JvmStatic
    fun getRightBound(element: ParadoxScriptBlock): PsiElement? {
        return element.lastChild?.takeIf { it.elementType == RIGHT_BRACE }
    }

    @JvmStatic
    fun getComponents(element: ParadoxScriptBlock): List<PsiElement> {
        return element.children().listBy<ParadoxScriptStatement>()
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptBlock, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Block
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptBlock): String {
        return ChronicleStrings.blockFolder
    }

    // endregion

    // region ParadoxScriptScriptedVariable

    @JvmStatic
    fun getIcon(element: ParadoxScriptScriptedVariable, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxScriptScriptedVariable): String? {
        element.stub?.name?.orNull()?.let { return it }
        return element.scriptedVariableName.name.orNull()
    }

    @JvmStatic
    fun setName(element: ParadoxScriptScriptedVariable, name: String): ParadoxScriptScriptedVariable {
        val nameElement = element.scriptedVariableName
        val idElement = nameElement.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxScriptElementFactory.createScriptedVariableNameFromText(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getNameIdentifier(element: ParadoxScriptScriptedVariable): PsiElement? {
        return element.scriptedVariableName.idElement
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxScriptScriptedVariable): Int {
        return element.node.startOffset + 1
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptScriptedVariable): String? {
        return element.scriptedVariableValue?.value
    }

    @JvmStatic
    fun getIElementType(element: ParadoxScriptScriptedVariable): IElementType {
        return SCRIPTED_VARIABLE
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxScriptScriptedVariable, another: PsiElement): Boolean {
        if (element === another) return true
        if (another !is ParadoxScriptScriptedVariable) return false
        // by name and gameType
        if (element.name.let { it.isNullOrEmpty() || it != another.name }) return false
        if (selectGameType(element) != selectGameType(another)) return false
        return true
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptScriptedVariable): String {
        var nameElementElement: ParadoxScriptScriptedVariableName? = null
        var valueElement: ParadoxScriptValue? = null
        element.forEachChild { e ->
            when {
                e is ParadoxScriptScriptedVariableName -> nameElementElement = e
                e is ParadoxScriptValue -> valueElement = e
            }
        }
        return buildString {
            if (nameElementElement != null) append(nameElementElement.presentableText) else append(DefaultStrings.unresolved)
            append(" = ")
            if (valueElement != null) append(valueElement.presentableText) else append(DefaultStrings.unresolved)
        }
    }

    // endregion

    // region ParadoxScriptScriptedVariableName

    @JvmStatic
    fun getIdElement(element: ParadoxScriptScriptedVariableName): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType == SCRIPTED_VARIABLE_NAME_TOKEN }?.takeIf { ParadoxScriptPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getName(element: ParadoxScriptScriptedVariableName): String {
        // remove leading `@` & can be parameterized & optimized to optimize memory
        return element.text.removePrefix("@").optimized()
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptScriptedVariableName): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.truncate(limit)
    }

    // endregion

    // region ParadoxScriptScriptedVariableReference

    @JvmStatic
    fun getIdElement(element: ParadoxScriptScriptedVariableReference): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }?.takeIf { ParadoxScriptPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptScriptedVariableReference, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxScriptScriptedVariableReference): String {
        // remove leading `@` & can be parameterized & optimized to optimize memory
        return element.text.removePrefix("@").optimized()
    }

    @JvmStatic
    fun setName(element: ParadoxScriptScriptedVariableReference, name: String): ParadoxScriptScriptedVariableReference {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxScriptElementFactory.createScriptedVariableReference(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptScriptedVariableReference): String {
        return element.name
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptScriptedVariableReference): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.truncate(limit)
    }

    // endregion

    // region ParadoxScriptInlineMathScriptedVariableReference

    @JvmStatic
    fun getIdElement(element: ParadoxScriptInlineMathScriptedVariableReference): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }?.takeIf { ParadoxScriptPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineMathScriptedVariableReference, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxScriptInlineMathScriptedVariableReference): String {
        // remove leading `@` & can be parameterized & optimized to optimize memory
        return element.text.removePrefix("@").optimized()
    }

    @JvmStatic
    fun setName(element: ParadoxScriptInlineMathScriptedVariableReference, name: String): ParadoxScriptInlineMathScriptedVariableReference {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxScriptElementFactory.createInlineMathScriptedVariableReference(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptInlineMathScriptedVariableReference): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.truncate(limit)
    }

    // endregion

    // region ParadoxScriptColor

    @JvmStatic
    fun getColorType(element: ParadoxScriptColor): String {
        return element.text.substringBefore('{').trim().optimized() // optimized to optimize memory
    }

    @JvmStatic
    fun getColorArgs(element: ParadoxScriptColor): List<String> {
        return element.text.substringIn('{', '}').trim().splitByBlank()
    }

    @JvmStatic
    fun getColor(element: ParadoxScriptColor): Color? {
        return ParadoxColorService.getColor(element, fromToken = false)
    }

    @JvmStatic
    fun setColor(element: ParadoxScriptColor, color: Color) {
        ParadoxColorService.setColor(element, color, fromToken = false)
    }

    // endregion

    // region ParadoxScriptInlineMath

    @JvmStatic
    fun getTokenElement(element: ParadoxScriptInlineMath): PsiElement? {
        return element.children().oneBy(INLINE_MATH_TOKEN)
    }

    @JvmStatic
    fun getInlineMathExpression(element: ParadoxScriptInlineMath): ParadoxScriptInlineMathExpression? {
        return element.children().oneBy(INLINE_MATH_TOKEN).children().oneBy()
    }

    @JvmStatic
    fun getLeftBound(element: ParadoxScriptInlineMath): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == INLINE_MATH_START }
    }

    @JvmStatic
    fun getRightBound(element: ParadoxScriptInlineMath): PsiElement? {
        return element.lastChild?.takeIf { it.elementType == INLINE_MATH_END }
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptInlineMath): String {
        return ChronicleStrings.inlineMathFolder
    }

    // endregion

    // region ParadoxScriptInlineMathNumber

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineMathNumber, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Value
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptInlineMathNumber): String {
        return element.text
    }

    // endregion

    // region ParadoxScriptNormalParameter

    @JvmStatic
    fun getIdElement(element: ParadoxScriptNormalParameter): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType === PARAMETER_TOKEN }
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxScriptNormalParameter): ParadoxScriptParameterArgument? {
        return element.children(forward = false).oneBy()
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptNormalParameter, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Parameter
    }

    @JvmStatic
    fun getName(element: ParadoxScriptNormalParameter): String? {
        return element.idElement?.text
    }

    @JvmStatic
    fun setName(element: ParadoxScriptNormalParameter, name: String): ParadoxScriptNormalParameter {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxScriptElementFactory.createParameter(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxScriptNormalParameter): Int {
        return element.node.startOffset + 1
    }

    @JvmStatic
    fun getDefaultValue(element: ParadoxScriptNormalParameter): String? {
        // 兼容默认值为空字符串的情况
        return element.argumentElement?.idElement?.text
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptNormalParameter): String {
        val name = element.name
        return ChronicleStrings.parameterFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxScriptInlineMathParameter

    @JvmStatic
    fun getIdElement(element: ParadoxScriptInlineMathParameter): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType === PARAMETER_TOKEN }
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxScriptInlineMathParameter): ParadoxScriptParameterArgument? {
        return element.children(forward = false).oneBy()
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineMathParameter, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Parameter
    }

    @JvmStatic
    fun getName(element: ParadoxScriptInlineMathParameter): String? {
        return element.idElement?.text
    }

    @JvmStatic
    fun setName(element: ParadoxScriptInlineMathParameter, name: String): ParadoxScriptInlineMathParameter {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxScriptElementFactory.createInlineMathParameter(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxScriptInlineMathParameter): Int {
        return element.node.startOffset + 1
    }

    @JvmStatic
    fun getDefaultValue(element: ParadoxScriptInlineMathParameter): String? {
        // 兼容默认值为空字符串的情况
        return element.argumentElement?.idElement?.text
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptInlineMathParameter): String {
        val name = element.name
        return ChronicleStrings.parameterFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxScriptParameterArgument

    @JvmStatic
    fun getIdElement(element: ParadoxScriptParameterArgument): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == ARGUMENT_TOKEN }
    }

    // endregion

    // region ParadoxScriptConditionalBlock

    @JvmStatic
    fun getMemberContainer(element: ParadoxScriptNormalConditionalBlock): ParadoxScriptNormalConditionalBlock {
        return element
    }

    @JvmStatic
    fun getMembers(element: ParadoxScriptNormalConditionalBlock): List<ParadoxScriptMember> {
        val memberContainer = getMemberContainer(element)
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun getLeftBound(element: ParadoxScriptNormalConditionalBlock): PsiElement? {
        // use simple implementation is enough here
        return element.children().oneBy { it.elementType == NESTED_RIGHT_BRACKET }
    }

    @JvmStatic
    fun getRightBound(element: ParadoxScriptNormalConditionalBlock): PsiElement? {
        return element.lastChild?.takeIf { it.elementType == RIGHT_BRACKET }
    }

    @JvmStatic
    fun getComponents(element: ParadoxScriptNormalConditionalBlock): List<PsiElement> {
        return element.children().listBy<ParadoxScriptStatement>()
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptNormalConditionalBlock, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.ConditionalBlock
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptNormalConditionalBlock): String {
        val expressionText = element.conditionalExpression?.presentableText
        return ChronicleStrings.conditionalBlockFolder(expressionText.or.unresolved())
    }

    // endregion

    // region ParadoxScriptInlineConditionalBlock

    @JvmStatic
    fun getLeftBound(element: ParadoxScriptInlineConditionalBlock): PsiElement? {
        // use simple implementation is enough here
        return element.children().oneBy(NESTED_RIGHT_BRACKET)
    }

    @JvmStatic
    fun getRightBound(element: ParadoxScriptInlineConditionalBlock): PsiElement? {
        return element.lastChild?.takeIf { it.elementType == RIGHT_BRACKET }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineConditionalBlock, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.ConditionalBlock
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptInlineConditionalBlock): String {
        val expressionText = element.conditionalExpression?.presentableText
        return ChronicleStrings.conditionalBlockFolder(expressionText.or.unresolved())
    }

    // endregion

    // region ParadoxScriptConditionalExpression

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptConditionalExpression): String {
        return buildString {
            element.processChild {
                when {
                    it is ParadoxScriptConditionalParameter -> {
                        append(it.name)
                        false
                    }
                    it.elementType == NOT_EQUAL_SIGN -> {
                        append("!")
                        true
                    }
                    else -> true
                }
            }
        }
    }

    // endregion

    // region ParadoxScriptConditionalParameter

    @JvmStatic
    fun getIdElement(element: ParadoxScriptConditionalParameter): PsiElement {
        return element.children().oneBy(CONDITION_PARAMETER_TOKEN)!!
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptConditionalParameter, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Parameter
    }

    @JvmStatic
    fun getName(element: ParadoxScriptConditionalParameter): String {
        return element.idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxScriptConditionalParameter, name: String): ParadoxScriptConditionalParameter {
        val idElement = element.idElement
        val newIdElement = ParadoxScriptElementFactory.createConditionalParameter(element.project, name).idElement
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxScriptConditionalParameter): Int {
        return element.node.startOffset
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptConditionalParameter): String {
        return element.text
    }

    // endregion

    // region ParadoxScriptExpressionElement

    @JvmStatic
    fun getName(element: ParadoxScriptExpressionElement): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptExpressionElement): String {
        return element.text
    }

    @JvmStatic
    fun setValue(element: ParadoxScriptExpressionElement, value: String): ParadoxScriptExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun setContent(element: ParadoxScriptExpressionElement, content: String, range: TextRange): ParadoxScriptExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxScriptExpressionElement): String {
        return element.value
    }

    // endregion

    // region Common Methods

    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementUseScope(element)
    }

    @JvmStatic
    fun toString(element: PsiElement): String {
        return PsiService.toPresentableString(element)
    }

    @JvmStatic
    fun getPresentation(element: NavigatablePsiElement): ParadoxScriptElementPresentation {
        return ParadoxScriptElementPresentation(element)
    }

    @JvmStatic
    fun getQuotePattern(element: PsiQuoteAwareElement): QuotePattern {
        return QuotePatterns.ParadoxScript
    }

    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }

    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element)
    }

    @JvmStatic
    fun getReferences(element: ParadoxScriptExpressionElement): Array<out PsiReference> {
        return ParadoxExpressionManager.getReferences(element)
    }

    // endregion
}
