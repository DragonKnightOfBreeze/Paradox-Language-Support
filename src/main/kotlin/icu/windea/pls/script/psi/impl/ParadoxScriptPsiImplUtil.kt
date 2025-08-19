package icu.windea.pls.script.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.impl.source.resolve.reference.*
import com.intellij.psi.search.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.color.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.references.script.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.navigation.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import java.awt.*
import javax.swing.*

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptPsiImplUtil {
    //region ParadoxScriptRootBlock

    @JvmStatic
    fun getValue(element: ParadoxScriptRootBlock): String {
        return PlsStringConstants.blockFolder
    }

    //endregion

    //region ParadoxScriptScriptedVariable

    @JvmStatic
    fun getIcon(element: ParadoxScriptScriptedVariable, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxScriptScriptedVariable): String? {
        //不包含作为前缀的"@"
        runReadAction { element.stub }?.name?.let { return it }
        return element.scriptedVariableName.name
    }

    @JvmStatic
    fun setName(element: ParadoxScriptScriptedVariable, name: String): ParadoxScriptScriptedVariable {
        val nameElement = element.scriptedVariableName
        if (nameElement.idElement == null) throw IncorrectOperationException() //不支持重命名
        val newNameElement = ParadoxScriptElementFactory.createScriptedVariableName(element.project, name)
        nameElement.replace(newNameElement)
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
        return element.scriptedVariableValue?.text?.unquote()
    }

    @JvmStatic
    fun getUnquotedValue(element: ParadoxScriptScriptedVariable): String? {
        return element.scriptedVariableValue?.text
    }

    @JvmStatic
    fun getIElementType(element: ParadoxScriptScriptedVariable): IElementType {
        return SCRIPTED_VARIABLE
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxScriptScriptedVariable, another: PsiElement): Boolean {
        //name & gameType
        return another is ParadoxScriptScriptedVariable
            && element.name?.equals(another.name) == true
            && selectGameType(element) == selectGameType(another)
    }

    @JvmStatic
    fun toString(element: ParadoxScriptScriptedVariable): String {
        return "ParadoxScriptScriptedVariable: ${element.name}"
    }

    //endregion

    //region ParadoxScriptScriptedVariableName

    @JvmStatic
    fun getIdElement(element: ParadoxScriptScriptedVariableName): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == SCRIPTED_VARIABLE_NAME_TOKEN }
    }

    @JvmStatic
    fun getName(element: ParadoxScriptScriptedVariableName): String? {
        //不包含作为前缀的"@"
        return element.text.removePrefix("@").orNull()
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptScriptedVariableName): String? {
        return element.name
    }

    //endregion

    //region ParadoxScriptProperty

    @JvmStatic
    fun getIcon(element: ParadoxScriptProperty, @Iconable.IconFlags flags: Int): Icon {
        val definitionInfo = runReadAction { element.definitionInfo }
        if (definitionInfo != null) return PlsIcons.Nodes.Definition(definitionInfo.type)
        val isInlineScriptInvocation = element.name == ParadoxInlineScriptManager.inlineScriptKey
        if (isInlineScriptInvocation) return PlsIcons.Nodes.InlineScript
        return PlsIcons.Nodes.Property
    }

    @JvmStatic
    fun getName(element: ParadoxScriptProperty): String {
        //注意：这里需要得到element.stub.rootKey，而非element.stub.name，因为这里需要的是PSI元素的名字而非定义的名字
        runReadAction { element.stub }?.rootKey?.let { return it }
        return element.propertyKey.name
    }

    @JvmStatic
    fun setName(element: ParadoxScriptProperty, name: String): ParadoxScriptProperty {
        //仅允许重命名定义，如果定义的名字来自某个定义属性，则修改那个属性的值
        val definitionInfo = element.definitionInfo
        if (definitionInfo == null) throw IncorrectOperationException()
        val nameField = definitionInfo.typeConfig.nameField
        if (nameField != null) {
            val nameProperty = element.findProperty(nameField) //不处理内联的情况
            if (nameProperty != null) {
                val nameElement = nameProperty.propertyValue<ParadoxScriptString>()
                nameElement?.setValue(name)
                return element
            } else {
                throw IncorrectOperationException()
            }
        }
        val nameElement = element.propertyKey
        val newNameElement = ParadoxScriptElementFactory.createPropertyKey(element.project, name)
        nameElement.replace(newNameElement)
        return element
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
    fun getDepth(element: ParadoxScriptProperty): Int {
        //得到相对于rootBlock的深度，最大为1（element.parent is ParadoxScriptRootBlock）
        var current: PsiElement? = element
        var depth = 0
        while (true) {
            current = current?.parent ?: break
            if (current is PsiFile) break
            if (current is ParadoxScriptBlock) depth++
            if (current is ParadoxScriptRootBlock) break
        }
        return depth
    }

    @JvmStatic
    fun getBlock(element: ParadoxScriptProperty): ParadoxScriptBlock? {
        return element.findChild<ParadoxScriptBlock>(forward = false)
    }

    @JvmStatic
    fun getIElementType(element: ParadoxScriptProperty): IElementType {
        return PROPERTY
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxScriptProperty, another: PsiElement): Boolean {
        //for definition: definitionName & definitionType & gameType
        //for others: never
        return another is ParadoxScriptProperty && element.definitionInfo?.equals(another.definitionInfo) == true
    }

    @JvmStatic
    fun toString(element: ParadoxScriptProperty): String {
        return "ParadoxScriptProperty: ${element.name}"
    }

    //endregion

    //region ParadoxScriptPropertyKey

    @JvmStatic
    fun getIdElement(element: ParadoxScriptPropertyKey): PsiElement? {
        return element.firstChild?.takeIf { it.nextSibling == null && it.elementType == PROPERTY_KEY_TOKEN }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptPropertyKey, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Property
    }

    @JvmStatic
    fun getName(element: ParadoxScriptPropertyKey): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptPropertyKey): String {
        return element.text.unquote()
    }

    @JvmStatic
    fun setValue(element: ParadoxScriptPropertyKey, value: String): ParadoxScriptPropertyKey {
        val newElement = ParadoxScriptElementFactory.createPropertyKey(element.project, value)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun toString(element: ParadoxScriptPropertyKey): String {
        return "ParadoxScriptPropertyKey: ${element.value}"
    }

    //endregion

    //region ParadoxScriptScriptedVariableReference

    @JvmStatic
    fun getIdElement(element: ParadoxScriptScriptedVariableReference): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.nextSibling == null && it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptScriptedVariableReference, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxScriptScriptedVariableReference): String {
        return element.text.removePrefix("@")
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptScriptedVariableReference): String {
        return element.name
    }

    @JvmStatic
    fun setName(element: ParadoxScriptScriptedVariableReference, name: String): ParadoxScriptScriptedVariableReference {
        if (element.idElement == null) throw IncorrectOperationException() //不支持重命名
        val newElement = ParadoxScriptElementFactory.createVariableReference(element.project, name)
        return element.replace(newElement).cast()
    }

    //endregion

    //region ParadoxScriptString

    @JvmStatic
    fun getIdElement(element: ParadoxScriptString): PsiElement? {
        return element.firstChild?.takeIf { it.nextSibling == null && it.elementType == STRING_TOKEN }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptString, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Value
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptString): String {
        if (element.text.containsLineBreak()) return "..."
        return element.text.unquote()
    }

    @JvmStatic
    fun setValue(element: ParadoxScriptString, value: String): ParadoxScriptString {
        val newElement = ParadoxScriptElementFactory.createString(element.project, value.quoteIfNecessary())
        return element.replace(newElement).cast()
    }

    //endregion

    //region ParadoxScriptColor

    @JvmStatic
    fun getValue(element: ParadoxScriptColor): String {
        return element.text
    }

    @JvmStatic
    fun getColorType(element: ParadoxScriptColor): String {
        return element.text.substringBefore('{').trim()
    }

    @JvmStatic
    fun getColorArgs(element: ParadoxScriptColor): List<String> {
        return element.text.substringIn('{', '}').trim().splitByBlank()
    }

    private val colorSupport by lazy { ParadoxColorSupport.EP_NAME.findExtensionOrFail(ParadoxScriptColorColorSupport::class.java) }

    @JvmStatic
    fun getColor(element: ParadoxScriptColor): Color? {
        return colorSupport.getColor(element)
    }

    @JvmStatic
    fun setColor(element: ParadoxScriptColor, color: Color) {
        colorSupport.setColor(element, color)
    }

    //endregion

    //region ParadoxScriptBlock

    @JvmStatic
    fun getIcon(element: ParadoxScriptBlock, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Block
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptBlock): String {
        return PlsStringConstants.blockFolder
    }

    //endregion

    //region ParadoxScriptValue

    @JvmStatic
    fun getIcon(element: ParadoxScriptValue, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Value
    }

    @JvmStatic
    fun getName(element: ParadoxScriptValue): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptValue): String {
        return element.text
    }

    @JvmStatic
    fun setValue(element: ParadoxScriptValue, value: String): ParadoxScriptValue {
        val newElement = ParadoxScriptElementFactory.createValue(element.project, value)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun toString(element: ParadoxScriptValue): String {
        return "${element.javaClass.simpleName}: ${element.value}"
    }

    //endregion

    //region ParadoxScriptParameterCondition

    @JvmStatic
    fun getIcon(element: ParadoxScriptParameterCondition, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.ParameterCondition
    }

    @JvmStatic
    fun getConditionExpression(element: ParadoxScriptParameterCondition): String? {
        val conditionExpression = element.parameterConditionExpression ?: return null
        var builder: StringBuilder? = null
        conditionExpression.processChild {
            when {
                it.elementType == NOT_EQUAL_SIGN -> {
                    val builderToUse = builder ?: StringBuilder().apply { builder = this }
                    builderToUse.append("!")
                    true
                }
                it is ParadoxScriptParameterConditionParameter -> {
                    val builderToUse = builder ?: StringBuilder().apply { builder = this }
                    builderToUse.append(it.name)
                    false
                }
                else -> true
            }
        }
        return builder?.toString()
    }

    @JvmStatic
    fun getPresentationText(element: ParadoxScriptParameterCondition): String? {
        return element.conditionExpression?.let { PlsStringConstants.parameterConditionFolder(it) }
    }

    //endregion

    //region ParadoxScriptInlineParameterCondition

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineParameterCondition, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.ParameterCondition
    }

    @JvmStatic
    fun getConditionExpression(element: ParadoxScriptInlineParameterCondition): String? {
        val conditionExpression = element.parameterConditionExpression ?: return null
        var builder: StringBuilder? = null
        conditionExpression.processChild {
            when {
                it.elementType == NOT_EQUAL_SIGN -> {
                    val builderToUse = builder ?: StringBuilder().apply { builder = this }
                    builderToUse.append("!")
                    true
                }
                it is ParadoxScriptParameterConditionParameter -> {
                    val builderToUse = builder ?: StringBuilder().apply { builder = this }
                    builderToUse.append(it.name)
                    false
                }
                else -> true
            }
        }
        return builder?.toString()
    }

    @JvmStatic
    fun getPresentationText(element: ParadoxScriptInlineParameterCondition): String? {
        return element.conditionExpression?.let { PlsStringConstants.parameterConditionFolder(it) }
    }

    //endregion

    //region ParadoxScriptParameterConditionParameter

    @JvmStatic
    fun getIdElement(element: ParadoxScriptParameterConditionParameter): PsiElement {
        return element.findChild { it.elementType == CONDITION_PARAMETER_TOKEN }!!
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptParameterConditionParameter, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Parameter
    }

    @JvmStatic
    fun getName(element: ParadoxScriptParameterConditionParameter): String {
        return element.idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxScriptParameterConditionParameter, name: String): ParadoxScriptParameterConditionParameter {
        val newElement = ParadoxScriptElementFactory.createParameterConditionParameter(element.project, name)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptParameterConditionParameter): String {
        return element.name
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxScriptParameterConditionParameter): Int {
        return element.node.startOffset
    }

    @JvmStatic
    fun getReference(element: ParadoxScriptParameterConditionParameter): ParadoxConditionParameterPsiReference {
        val nameElement = element.idElement
        return ParadoxConditionParameterPsiReference(element, nameElement.textRangeInParent)
    }

    //endregion

    //region ParadoxScriptInlineMath

    @JvmStatic
    fun getValue(element: ParadoxScriptInlineMath): String {
        return PlsStringConstants.inlineMathFolder
    }

    @JvmStatic
    fun getTokenElement(element: ParadoxScriptInlineMath): PsiElement? {
        return element.findChild { it.elementType == INLINE_MATH_TOKEN }
    }

    //endregion

    //region ParadoxScriptInlineMathNumber

    @JvmStatic
    fun getValue(element: ParadoxScriptInlineMathNumber): String {
        return element.text
    }

    //endregion

    //region ParadoxScriptInlineMathVariableReference

    @JvmStatic
    fun getIdElement(element: ParadoxScriptInlineMathScriptedVariableReference): PsiElement? {
        return element.firstChild?.takeIf { it.nextSibling == null && it.elementType == INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN }
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineMathScriptedVariableReference, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxScriptInlineMathScriptedVariableReference): String? {
        return element.idElement?.text
    }

    @JvmStatic
    fun setName(element: ParadoxScriptInlineMathScriptedVariableReference, name: String): ParadoxScriptInlineMathScriptedVariableReference {
        if (element.idElement == null) throw IncorrectOperationException() //不支持重命名
        val newElement = ParadoxScriptElementFactory.createInlineMathVariableReference(element.project, name)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptInlineMathScriptedVariableReference): String? {
        return element.name
    }

    //endregion

    //region ParadoxScriptParameter

    @JvmStatic
    fun getIdElement(element: ParadoxScriptParameter): PsiElement? {
        return element.findChild { it.elementType == PARAMETER_TOKEN }
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxScriptParameter): ParadoxScriptParameterArgument? {
        return element.findChild<_>(forward = false)
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptParameter, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Parameter
    }

    @JvmStatic
    fun getName(element: ParadoxScriptParameter): String? {
        return element.idElement?.text
    }

    @JvmStatic
    fun setName(element: ParadoxScriptParameter, name: String): ParadoxScriptParameter {
        if (element.idElement == null) throw IncorrectOperationException() //不支持重命名
        val newElement = ParadoxScriptElementFactory.createParameterSmartly(element.project, name)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptParameter): String? {
        return element.name
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxScriptParameter): Int {
        return element.node.startOffset + 1
    }

    @JvmStatic
    fun getDefaultValue(element: ParadoxScriptParameter): String? {
        //兼容默认值为空字符串的情况
        return element.argumentElement?.idElement?.text
    }

    @JvmStatic
    fun getReference(element: ParadoxScriptParameter): ParadoxParameterPsiReference? {
        val nameElement = element.idElement ?: return null
        return ParadoxParameterPsiReference(element, nameElement.textRangeInParent)
    }

    //endregion

    //region ParadoxScriptInlineMathParameter

    @JvmStatic
    fun getIdElement(element: ParadoxScriptInlineMathParameter): PsiElement? {
        return element.findChild { it.elementType == PARAMETER_TOKEN }
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxScriptInlineMathParameter): ParadoxScriptParameterArgument? {
        return element.findChild<_>(forward = false)
    }

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineMathParameter, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Parameter
    }

    @JvmStatic
    fun getName(element: ParadoxScriptInlineMathParameter): String? {
        return element.idElement?.text
    }

    @JvmStatic
    fun setName(element: ParadoxScriptInlineMathParameter, name: String): ParadoxScriptInlineMathParameter {
        if (element.idElement == null) throw IncorrectOperationException() //不支持重命名
        val newElement = ParadoxScriptElementFactory.createInlineMathParameterSmartly(element.project, name)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptInlineMathParameter): String? {
        return element.name
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxScriptInlineMathParameter): Int {
        return element.node.startOffset + 1
    }

    @JvmStatic
    fun getDefaultValue(element: ParadoxScriptInlineMathParameter): String? {
        //兼容默认值为空字符串的情况
        return element.argumentElement?.idElement?.text
    }

    @JvmStatic
    fun getReference(element: ParadoxScriptInlineMathParameter): ParadoxParameterPsiReference? {
        val nameElement = element.idElement ?: return null
        return ParadoxParameterPsiReference(element, nameElement.textRangeInParent)
    }

    //endregion

    //region ParadoxScriptParameterArgument

    @JvmStatic
    fun getIdElement(element: ParadoxScriptParameterArgument): PsiElement? {
        return element.findChild(forward = false) { it.elementType == ARGUMENT_TOKEN }
    }

    //endregion

    @JvmStatic
    fun getMemberList(element: PsiElement): List<ParadoxScriptMemberElement> {
        return element.findChildren<_>()
    }

    @JvmStatic
    fun getComponents(element: PsiElement): List<PsiElement> {
        return element.findChildren { isComponent(it) }
    }

    private fun isComponent(element: PsiElement): Boolean {
        //允许混合value和property
        return element is ParadoxScriptScriptedVariable || element is ParadoxScriptMemberElement || element is ParadoxScriptParameterCondition
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
    fun getPresentation(element: PsiElement): ItemPresentation {
        return ParadoxScriptItemPresentation(element)
    }

    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementUseScope(element)
    }
}
