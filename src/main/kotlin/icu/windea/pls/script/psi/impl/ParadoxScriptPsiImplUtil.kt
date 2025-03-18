package icu.windea.pls.script.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.color.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
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
        return PlsConstants.Strings.blockFolder
    }

    @JvmStatic
    fun isEmpty(element: ParadoxScriptRootBlock): Boolean {
        element.forEachChild {
            when {
                it is ParadoxScriptProperty -> return false
                it is ParadoxScriptValue -> return false
            }
        }
        return true
    }

    @JvmStatic
    fun isNotEmpty(element: ParadoxScriptRootBlock): Boolean {
        element.forEachChild {
            when {
                it is ParadoxScriptProperty -> return true
                it is ParadoxScriptValue -> return true
            }
        }
        return false
    }

    @JvmStatic
    fun getComponents(element: ParadoxScriptRootBlock): List<PsiElement> {
        //允许混合value和property
        return element.findChildrenOfType { isRootBlockComponent(it) }
    }

    private fun isRootBlockComponent(element: PsiElement): Boolean {
        return element is ParadoxScriptScriptedVariable || element is ParadoxScriptProperty || element is ParadoxScriptValue
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
    fun getPresentation(element: ParadoxScriptScriptedVariable): ItemPresentation {
        return ParadoxScriptScriptedVariablePresentation(element)
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxScriptScriptedVariable, another: PsiElement): Boolean {
        //name & gameType
        return another is ParadoxScriptScriptedVariable
            && element.name == another.name
            && selectGameType(element) == selectGameType(another)
    }

    @JvmStatic
    fun toString(element: ParadoxScriptScriptedVariable): String {
        return "ParadoxScriptScriptedVariable(name=${element.name})"
    }

    //endregion

    //region ParadoxScriptScriptedVariableName

    @JvmStatic
    fun getName(element: ParadoxScriptScriptedVariableName): String? {
        // 不包含作为前缀的"@"
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
        val definitionInfo = element.definitionInfo
        if (definitionInfo != null) return PlsIcons.Nodes.Definition(definitionInfo.type)
        return PlsIcons.ScriptNodes.Property
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
        return element.findChild()
    }

    @JvmStatic
    fun getPresentation(element: ParadoxScriptProperty): ItemPresentation {
        val definitionInfo = element.definitionInfo
        if (definitionInfo != null) return ParadoxDefinitionPresentation(element, definitionInfo)
        return BaseParadoxItemPresentation(element)
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxScriptProperty, another: PsiElement): Boolean {
        //for definition: definitionName & definitionType & gameType
        //for others: never
        return another is ParadoxScriptProperty
            && element.definitionInfo == another.definitionInfo
    }

    @JvmStatic
    fun toString(element: ParadoxScriptProperty): String {
        return "ParadoxScriptProperty(name=${element.name})"
    }

    //endregion

    //region ParadoxScriptPropertyKey

    @JvmStatic
    fun getIcon(element: ParadoxScriptPropertyKey, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.ScriptNodes.Property
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
        return "ParadoxScriptPropertyKey(value=${element.value})"
    }

    //endregion

    //region ParadoxScriptScriptedVariableReference

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

    @JvmStatic
    fun getReference(element: ParadoxScriptScriptedVariableReference): ParadoxScriptedVariablePsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.idElement?.textRangeInParent ?: return@run null
                ParadoxScriptedVariablePsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }

    //endregion

    //region ParadoxScriptValue

    @JvmStatic
    fun getIcon(element: ParadoxScriptValue, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.ScriptNodes.Value
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

    //endregion

    //region ParadoxScriptBoolean

    @JvmStatic
    fun getBooleanValue(element: ParadoxScriptBoolean): Boolean {
        return element.value.toBooleanYesNo()
    }

    //endregion

    //region ParadoxScriptInt

    @JvmStatic
    fun getIntValue(element: ParadoxScriptInt): Int {
        return element.value.toIntOrNull() ?: 0
    }


    //endregion

    //region ParadoxScriptFloat

    @JvmStatic
    fun getFloatValue(element: ParadoxScriptFloat): Float {
        return element.value.toFloatOrNull() ?: 0f
    }

    //endregion

    //region ParadoxScriptString

    @JvmStatic
    fun getIcon(element: ParadoxScriptString, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.ScriptNodes.Value
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

    @JvmStatic
    fun getStringValue(element: ParadoxScriptString): String {
        return element.value
    }

    @JvmStatic
    fun toString(element: ParadoxScriptString): String {
        return "ParadoxScriptString(value=${element.value})"
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
        return PlsIcons.ScriptNodes.Block
    }

    @JvmStatic
    fun getValue(element: ParadoxScriptBlock): String {
        return PlsConstants.Strings.blockFolder
    }

    @JvmStatic
    fun isEmpty(element: ParadoxScriptBlock): Boolean {
        element.forEachChild {
            when {
                it is ParadoxScriptProperty -> return false
                it is ParadoxScriptValue -> return false
                it is ParadoxScriptParameterCondition && it.isNotEmpty -> return false
            }
        }
        return true
    }

    @JvmStatic
    fun isNotEmpty(element: ParadoxScriptBlock): Boolean {
        element.forEachChild {
            when {
                it is ParadoxScriptProperty -> return true
                it is ParadoxScriptValue -> return true
                it is ParadoxScriptParameterCondition && it.isNotEmpty -> return true
            }
        }
        return false
    }

    @JvmStatic
    fun getPropertyList(element: ParadoxScriptBlock): List<ParadoxScriptProperty> {
        return buildList { element.processProperty(conditional = true, inline = true) { add(it) } }
    }

    @JvmStatic
    fun getValueList(element: ParadoxScriptBlock): List<ParadoxScriptValue> {
        return buildList { element.processValue(conditional = true, inline = true) { add(it) } }
    }

    @JvmStatic
    fun getComponents(element: ParadoxScriptBlock): List<PsiElement> {
        //允许混合value和property
        return element.findChildrenOfType { isBlockComponent(it) }
    }

    private fun isBlockComponent(element: PsiElement): Boolean {
        return element is ParadoxScriptScriptedVariable
            || element is ParadoxScriptProperty
            || element is ParadoxScriptValue
            || element is ParadoxScriptParameterCondition
    }

    //endregion

    //region ParadoxScriptParameterCondition

    @JvmStatic
    fun getIcon(element: ParadoxScriptParameterCondition, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.ScriptNodes.ParameterCondition
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
    fun isEmpty(element: ParadoxScriptParameterCondition): Boolean {
        element.forEachChild {
            when {
                it is ParadoxScriptProperty -> return false
                it is ParadoxScriptValue -> return false
            }
        }
        return true
    }

    @JvmStatic
    fun isNotEmpty(element: ParadoxScriptParameterCondition): Boolean {
        element.forEachChild {
            when {
                it is ParadoxScriptProperty -> return true
                it is ParadoxScriptValue -> return true
            }
        }
        return false
    }

    @JvmStatic
    fun getComponents(element: ParadoxScriptParameterCondition): List<PsiElement> {
        //允许混合value和property
        return element.findChildrenOfType { isParameterConditionComponent(it) }
    }

    private fun isParameterConditionComponent(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty
            || element is ParadoxScriptValue
            || element is ParadoxScriptParameterCondition
    }

    @JvmStatic
    fun getPresentationText(element: ParadoxScriptParameterCondition): String? {
        return element.conditionExpression?.let { PlsConstants.Strings.parameterConditionFolder(it) }
    }

    //endregion

    //region ParadoxScriptInlineParameterCondition

    @JvmStatic
    fun getIcon(element: ParadoxScriptInlineParameterCondition, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.ScriptNodes.ParameterCondition
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
        return element.conditionExpression?.let { PlsConstants.Strings.parameterConditionFolder(it) }
    }

    //endregion

    //region ParadoxScriptParameterConditionParameter

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
        return PlsConstants.Strings.inlineMathFolder
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

    @JvmStatic
    fun getReference(element: ParadoxScriptInlineMathScriptedVariableReference): ParadoxScriptedVariablePsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.idElement?.textRangeInParent ?: return@run null
                ParadoxScriptedVariablePsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }

    //endregion

    //region ParadoxScriptParameter

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
        if (element.findChild(PIPE)?.takeIf { it.nextSibling?.elementType == PARAMETER_END } != null) return ""
        return element.defaultValueToken?.text
    }

    @JvmStatic
    fun getReference(element: ParadoxScriptParameter): ParadoxParameterPsiReference? {
        val nameElement = element.idElement ?: return null
        return ParadoxParameterPsiReference(element, nameElement.textRangeInParent)
    }

    //endregion

    //region ParadoxScriptInlineMathParameter

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
        if (element.findChild(PIPE)?.takeIf { it.nextSibling?.elementType == PARAMETER_END } != null) return ""
        return element.defaultValueToken?.text
    }

    @JvmStatic
    fun getReference(element: ParadoxScriptInlineMathParameter): ParadoxParameterPsiReference? {
        val nameElement = element.idElement ?: return null
        return ParadoxParameterPsiReference(element, nameElement.textRangeInParent)
    }

    //endregion

    @JvmStatic
    fun getType(element: PsiElement): ParadoxType {
        return when (element) {
            is ParadoxScriptScriptedVariable -> element.scriptedVariableValue?.type ?: ParadoxType.Unknown
            is ParadoxScriptProperty -> element.propertyValue?.type ?: ParadoxType.Unknown
            is ParadoxScriptPropertyKey -> ParadoxTypeManager.resolve(element.value)
            is ParadoxScriptScriptedVariableReference -> element.reference?.resolve()?.type ?: ParadoxType.Unknown
            is ParadoxScriptBoolean -> ParadoxType.Boolean
            is ParadoxScriptInt -> ParadoxType.Int
            is ParadoxScriptFloat -> ParadoxType.Float
            is ParadoxScriptString -> ParadoxType.String
            is ParadoxScriptColor -> ParadoxType.Color
            is ParadoxScriptBlock -> ParadoxType.Block
            is ParadoxScriptInlineMath -> ParadoxType.InlineMath
            is ParadoxScriptInlineMathNumber -> ParadoxTypeManager.resolve(element.text)
            is ParadoxScriptInlineMathScriptedVariableReference -> element.reference?.resolve()?.type ?: ParadoxType.Unknown
            else -> ParadoxType.Unknown
        }
    }

    @JvmStatic
    fun getExpression(element: PsiElement): String {
        return when (element) {
            is ParadoxScriptProperty -> {
                val keyExpression = element.propertyKey.expression
                val valueExpression = element.propertyValue?.expression ?: PlsConstants.Strings.unresolved
                "$keyExpression = $valueExpression"
            }
            is ParadoxScriptBlock -> PlsConstants.Strings.blockFolder
            is ParadoxScriptInlineMath -> PlsConstants.Strings.inlineMathFolder
            else -> element.text
        }
    }

    @JvmStatic
    fun getConfigExpression(element: PsiElement): String? {
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
        return when (element) {
            is ParadoxScriptProperty -> {
                if (config !is CwtPropertyConfig) return null
                "${config.key} = ${config.value}"
            }
            is ParadoxScriptPropertyKey -> {
                if (config !is CwtPropertyConfig) return null
                config.key
            }
            is ParadoxScriptValue -> {
                if (config !is CwtValueConfig) return null
                config.value
            }
            else -> null
        }
    }

    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }

    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        //这里不需要进行缓存
        return PsiReferenceService.getService().getContributedReferences(element)
    }

    @JvmStatic
    fun getPresentation(element: PsiElement): ItemPresentation {
        return BaseParadoxItemPresentation(element)
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
