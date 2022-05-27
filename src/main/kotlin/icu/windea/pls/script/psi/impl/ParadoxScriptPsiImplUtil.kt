package icu.windea.pls.script.psi.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.reference.*
import org.apache.commons.imaging.color.*
import java.awt.*
import javax.swing.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptPsiImplUtil {
	//region ParadoxScriptVariable
	@JvmStatic
	fun getIcon(element: ParadoxScriptVariable, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.scriptVariableIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptVariable): String {
		//注意：element.stub可能会导致ProcessCanceledException
		// 不包含作为前缀的"@"
		runCatching { element.stub?.name }.getOrNull()?.let { return it }
		return element.variableName.variableNameId.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptVariable, name: String): ParadoxScriptVariable {
		// 不包含作为前缀的"@"
		val nameElement = element.variableName.variableNameId
		val newNameElement = ParadoxScriptElementFactory.createVariableName(element.project, name).variableNameId
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptVariable): PsiElement {
		return element.variableName.variableNameId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptVariable): Int {
		return element.nameIdentifier.textOffset
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptVariable): String? {
		return element.variableValue?.value?.text?.unquote()
	}
	
	@JvmStatic
	fun getUnquotedValue(element: ParadoxScriptVariable): String? {
		return element.variableValue?.value?.text
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptVariable): ParadoxValueType? {
		return element.variableValue?.value?.valueType
	}
	//endregion
	
	//region ParadoxScriptVariableName
	@JvmStatic
	fun getName(element: ParadoxScriptVariableName): String {
		// 不包含作为前缀的"@"
		return element.variableNameId.text
	}
	//endregion
	
	//region ParadoxScriptParameter
	@JvmStatic
	fun getIcon(element: ParadoxScriptParameter, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.scriptParameterIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptParameter): String {
		return element.parameterId.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptParameter, name: String): ParadoxScriptParameter {
		val nameElement = element.parameterId
		val newNameElement = ParadoxScriptElementFactory.createParameter(element.project, name).parameterId
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptParameter): PsiElement {
		return element.parameterId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptParameter): Int {
		return element.nameIdentifier.textOffset
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptParameter): String {
		return PlsFolders.parameterFolder
	}
	
	@JvmStatic
	fun getDefaultValue(element: ParadoxScriptParameter): String? {
		return element.defaultValueToken?.text
	}
	//endregion
	
	//region ParadoxScriptProperty
	@JvmStatic
	fun getIcon(element: ParadoxScriptProperty, @Iconable.IconFlags flags: Int): Icon {
		if(element.definitionInfo != null) return PlsIcons.definitionIcon
		return PlsIcons.scriptPropertyIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptProperty): String {
		//注意：element.stub可能会导致ProcessCanceledException
		//注意：这里需要得到element.stub.rootKey，而非element.stub.name，因为这里需要的是PSI元素的名字而非定义的名字
		runCatching { element.stub?.rootKey }.getOrNull()?.let { return it }
		return element.propertyKey.text.unquote()
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptProperty, name: String): ParadoxScriptProperty {
		val nameElement = element.propertyKey
		val newNameElement = ParadoxScriptElementFactory.createPropertyKey(element.project, name)
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptProperty): PsiElement? {
		return element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId }
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptProperty): Int {
		return element.propertyKey.textOffset
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptProperty): String? {
		return element.propertyValue?.value?.value
	}
	
	//得到相对于rootBlock的深度，最大为1（element.parent is ParadoxScriptRootBlock）
	@JvmStatic
	fun getDepth(element: ParadoxScriptProperty): Int {
		var current: PsiElement? = element
		var depth = 0
		while(true) {
			current = current?.parent ?: break
			if(current is PsiFile) break
			if(current is ParadoxScriptBlock) depth++
			if(current is ParadoxScriptRootBlock) break
		}
		return depth
	}
	
	@JvmStatic
	fun getBlock(element: ParadoxScriptProperty): ParadoxScriptBlock? {
		return element.propertyValue?.value?.castOrNull()
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptProperty): ParadoxValueType? {
		return element.propertyValue?.value?.valueType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptProperty): String? {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) {
			return definitionInfo.typesText
		}
		return null //TODO 支持定义元素的类型
	}
	
	@JvmStatic
	fun getPathName(element: ParadoxScriptProperty): String? {
		return element.propertyKey.text
	}
	
	@JvmStatic
	fun getOriginalPathName(element: ParadoxScriptProperty): String {
		return element.propertyKey.value
	}
	//endregion
	
	//region ParadoxScriptPropertyKey
	@JvmStatic
	fun getValue(element: ParadoxScriptPropertyKey): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun setValue(element: ParadoxScriptPropertyKey, value: String): ParadoxScriptPropertyKey {
		val newElement = ParadoxScriptElementFactory.createPropertyKey(element.project, value)
		element.replace(newElement)
		return element
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptPropertyKey): ParadoxScriptPropertyKeyReference? {
		if(element.parameter != null) return null //propertyKey为parameter的时直接返回null
		val rangeInElement = TextRange(0, element.textLength) //包括可能的括起字符串的双引号
		return ParadoxScriptPropertyKeyReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxScriptVariableReference
	@JvmStatic
	fun getIcon(element: ParadoxScriptVariableReference, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.scriptVariableIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptVariableReference): String {
		// 不包含作为前缀的"@"
		return element.variableReferenceId.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptVariableReference, name: String): ParadoxScriptVariableReference {
		// 不包含作为前缀的"@"
		val nameElement = element.variableReferenceId
		val newNameElement = ParadoxScriptElementFactory.createVariableReference(element.project, name).variableReferenceId
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptVariableReference): ParadoxScriptVariableReferenceReference {
		val rangeInElement = element.variableReferenceId.textRangeInParent
		return ParadoxScriptVariableReferenceReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxScriptValue
	@JvmStatic
	fun getIcon(element: ParadoxScriptValue, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.scriptValueIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptValue): String {
		return element.text
	}
	//endregion
	
	//region ParadoxScriptBoolean
	@JvmStatic
	fun getBooleanValue(element: ParadoxScriptBoolean): Boolean {
		return element.value.toBooleanYesNo()
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptBoolean): ParadoxValueType {
		return ParadoxValueType.BooleanType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptBoolean): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptNumber
	@JvmStatic
	fun getValueType(element: ParadoxScriptNumber): ParadoxValueType {
		return ParadoxValueType.NumberType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptNumber): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptInt
	@JvmStatic
	fun getIntValue(element: ParadoxScriptInt): Int {
		return element.value.toIntOrNull() ?: 0
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptInt): ParadoxValueType {
		return ParadoxValueType.IntType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptInt): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptFloat
	@JvmStatic
	fun getFloatValue(element: ParadoxScriptFloat): Float {
		return element.value.toFloatOrNull() ?: 0f
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptFloat): ParadoxValueType {
		return ParadoxValueType.FloatType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptFloat): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptString
	@JvmStatic
	fun getIcon(element: ParadoxScriptString, @Iconable.IconFlags flags: Int): Icon {
		//特殊处理字符串需要被识别为标签的情况
		if(element.resolveTagConfig() != null) return PlsIcons.tagIcon
		return PlsIcons.scriptValueIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptString): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun setValue(element: ParadoxScriptString, name: String): ParadoxScriptString {
		val newElement = ParadoxScriptElementFactory.createString(element.project, name.quote())
		element.replace(newElement)
		return element
	}
	
	@JvmStatic
	fun getStringValue(element: ParadoxScriptString): String {
		return element.value
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptString): ParadoxScriptStringReference {
		val rangeInElement = TextRange(0, element.textLength) //包括可能的括起字符串的双引号
		return ParadoxScriptStringReference(element, rangeInElement)
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptString): ParadoxValueType {
		return ParadoxValueType.StringType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptString): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptStringTemplate
	@JvmStatic
	fun getValue(element: ParadoxScriptStringTemplate): String {
		return PlsFolders.stringTemplateFolder
	}
	//endregion
	
	//region ParadoxScriptStringTemplateEntry
	@JvmStatic
	fun getValue(element: ParadoxScriptLiteralStringTemplateEntry): String {
		return element.text
	}
	//endregion
	
	//region ParadoxScriptColor
	@JvmStatic
	fun getValue(element: ParadoxScriptColor): String {
		return element.text
	}
	
	@JvmStatic
	fun getColor(element: ParadoxScriptColor): Color? {
		//忽略异常
		return runCatching {
			val text = element.text
			val colorType = text.substringBefore('{').trim()
			val args = text.substringAfter('{').substringBefore('}').trim().split("\\s+".toRegex())
			
			//根据不同的颜色类型得到不同的颜色对象
			when(colorType) {
				"rgb" -> args.let { (r, g, b) -> Color(r.toInt(), g.toInt(), b.toInt()) }
				"rgba" -> args.let { (r, g, b, a) -> Color(r.toInt(), g.toInt(), b.toInt(), a.toInt()) }
				"hsb" -> args.let { (h, s, b) -> Color.getHSBColor(h.toFloat(), s.toFloat(), b.toFloat()) }
				"hsv" -> args.let { (h, s, v) -> ColorHsv(h.toDouble(), s.toDouble(), v.toDouble()).toColor() }
				"hsl" -> args.let { (h, s, l) -> ColorHsl(h.toDouble(), s.toDouble(), l.toDouble()).toColor() }
				else -> null
			}
		}.getOrNull()
	}
	
	@JvmStatic
	fun setColor(element: ParadoxScriptColor, color: Color) {
		runCatching {
			val text = element.text
			val colorType = text.substringBefore('{').trim()
			
			//使用rgb或者rgba
			val shouldBeRgba = color.alpha != 255
			val newText = when {
				colorType == "rgba" || shouldBeRgba -> "rgba { ${color.run { "$red $green $blue $alpha" }} }"
				else -> "rgb { ${color.run { "$red $green $blue" }} }"
			}
			
			////根据不同的颜色类型生成不同的文本
			//val newText = when(colorType) {
			//	"rgb" -> "rgb { ${color.run { "$red $green $blue" }} }"
			//	"rgba" -> "rgba { ${color.run { "$red $green $blue $alpha" }} }"
			//	"hsv" -> "hsv { ${color.toColorHsv().run { "$H $S $V" }} }"
			//	"hsl" -> "hsl { ${color.toColorHsl().run { "$H $S $L" }} }"
			//	else -> "rgba { ${color.run { "$red $green $blue $alpha" }} }"
			//}
			val newColor = ParadoxScriptElementFactory.createValue(element.project, newText) as? ParadoxScriptColor
			if(newColor != null) element.replace(newColor)
		}
	}
	
	private fun ColorHsl.toColor() = Color(ColorConversions.convertHSLtoRGB(this))
	
	//private fun Color.toColorHsl() = ColorConversions.convertRGBtoHSL(this.rgb)
	
	private fun ColorHsv.toColor() = Color(ColorConversions.convertHSVtoRGB(this))
	
	//private fun Color.toColorHsv() = ColorConversions.convertRGBtoHSV(this.rgb)
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptColor): ParadoxValueType {
		return ParadoxValueType.ColorType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptColor): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptBlock
	@JvmStatic
	fun getIcon(element: ParadoxScriptBlock, @Iconable.IconFlags flags: Int): Icon{
		return PlsIcons.scriptBlockIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptBlock): String {
		return PlsFolders.blockFolder
	}
	
	@JvmStatic
	fun isEmpty(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			when {
				it is ParadoxScriptProperty -> return false
				it is ParadoxScriptValue -> return false
				it is ParadoxScriptParameterCondition && it.isNotEmpty() -> return false
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
				it is ParadoxScriptParameterCondition && it.isNotEmpty() -> return true
			}
		}
		return false
	}
	
	@JvmStatic
	fun getComponents(element: ParadoxScriptBlock): List<PsiElement> {
		//允许混合value和property
		return element.filterChildOfType { isBlockComponent(it) }
	}
	
	private fun isBlockComponent(element: PsiElement): Boolean {
		return element is ParadoxScriptVariable || element is ParadoxScriptProperty || element is ParadoxScriptValue
			|| element is ParadoxScriptParameterCondition
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptBlock): ParadoxValueType {
		return ParadoxValueType.BlockType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptBlock): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptVariableValue
	@JvmStatic
	fun getValue(element: ParadoxScriptVariableValue): ParadoxScriptNumber {
		return element.number
	}
	//endregion
	
	//region ParadoxScriptParameterCondition
	@JvmStatic
	fun getIcon(element: ParadoxScriptParameterCondition, @Iconable.IconFlags flags: Int): Icon{
		return PlsIcons.scriptParameterConditionIcon
	}
	
	@JvmStatic
	fun getExpression(element: ParadoxScriptParameterCondition): String? {
		val conditionExpression = element.parameterConditionExpression ?: return null
		val builder = StringBuilder()
		conditionExpression.processChildren {
			when {
				element.elementType == ParadoxScriptElementTypes.NOT_EQUAL_SIGN -> {
					builder.append("!")
					true
				}
				element is ParadoxScriptParameterConditionParameter -> {
					builder.append(element.name)
					false
				}
				else -> true
			}
		}
		return builder.toString()
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
		return element.filterChildOfType { isParameterConditionComponent(it) }
	}
	
	private fun isParameterConditionComponent(element: PsiElement): Boolean {
		return element is ParadoxScriptVariable || element is ParadoxScriptProperty || element is ParadoxScriptValue
	}
	//endregion
	
	//region ParadoxScriptParameterConditionParameter
	@JvmStatic
	fun getIcon(element: ParadoxScriptParameterConditionParameter, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.scriptParameterIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptParameterConditionParameter): String {
		return element.parameterId.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptParameterConditionParameter, name: String): ParadoxScriptParameterConditionParameter {
		val nameElement = element.parameterId
		val newNameElement = ParadoxScriptElementFactory.createInlineMathParameter(element.project, name).parameterId
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptParameterConditionParameter): PsiElement {
		return element.parameterId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptParameterConditionParameter): Int {
		return element.nameIdentifier.textOffset
	}
	//endregion
	
	//region ParadoxScriptInlineMath
	@JvmStatic
	fun getValue(element: ParadoxScriptInlineMath): String {
		return PlsFolders.inlineMathFolder
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptInlineMath): ParadoxValueType {
		return ParadoxValueType.InlineMathType
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptInlineMath): String? {
		return null //TODO 支持定义元素的类型
	}
	//endregion
	
	//region ParadoxScriptInlineMathNumber
	@JvmStatic
	fun getValue(element: ParadoxScriptInlineMathNumber): String {
		return element.text
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptInlineMathNumber): ParadoxValueType {
		return ParadoxValueType.infer(element.text)
	}
	//endregion
	
	//region ParadoxScriptInlineMathVariableReference
	@JvmStatic
	fun getIcon(element: ParadoxScriptInlineMathVariableReference, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.scriptVariableIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptInlineMathVariableReference): String {
		return element.variableReferenceId.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptInlineMathVariableReference, name: String): ParadoxScriptInlineMathVariableReference {
		val nameElement = element.variableReferenceId
		val newNameElement = ParadoxScriptElementFactory.createInlineMathVariableReference(element.project, name).variableReferenceId
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptInlineMathVariableReference): ParadoxScriptVariableReferenceReference {
		val rangeInElement = element.variableReferenceId.textRangeInParent
		return ParadoxScriptVariableReferenceReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxScriptInlineMathParameter
	@JvmStatic
	fun getIcon(element: ParadoxScriptInlineMathParameter, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.scriptParameterIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptInlineMathParameter): String {
		return element.parameterId.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptInlineMathParameter, name: String): ParadoxScriptInlineMathParameter {
		val nameElement = element.parameterId
		val newNameElement = ParadoxScriptElementFactory.createInlineMathParameter(element.project, name).parameterId
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptInlineMathParameter): PsiElement {
		return element.parameterId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptInlineMathParameter): Int {
		return element.nameIdentifier.textOffset
	}
	
	@JvmStatic
	fun getDefaultValue(element: ParadoxScriptInlineMathParameter): String? {
		return element.defaultValueToken?.text
	}
	//endregion
}
