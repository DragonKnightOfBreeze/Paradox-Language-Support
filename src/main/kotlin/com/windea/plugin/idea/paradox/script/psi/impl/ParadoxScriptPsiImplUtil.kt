@file:Suppress("UNUSED_PARAMETER", "UNUSED_DESTRUCTURED_PARAMETER_ENTRY", "IntroduceWhenSubject", "MoveVariableDeclarationIntoWhen")

package com.windea.plugin.idea.paradox.script.psi.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptElementFactory.createPropertyKey
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptElementFactory.createValue
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptElementFactory.createVariableName
import com.windea.plugin.idea.paradox.script.reference.*
import org.apache.commons.imaging.color.*
import java.awt.*
import javax.swing.*
import kotlin.collections.List
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4

object ParadoxScriptPsiImplUtil {
	//region ParadoxScriptVariable
	@JvmStatic
	fun getName(element: ParadoxScriptVariable): String {
		return element.stub?.key ?: element.variableName.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptVariable, name: String): PsiElement {
		element.variableName.replace(createVariableName(element.project, name))
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptVariable): PsiElement {
		return element.variableName.variableNameId
	}
	
	@JvmStatic
	fun getIcon(element: ParadoxScriptVariable, @Iconable.IconFlags flags: Int): Icon {
		return paradoxScriptVariableIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptVariable): String? {
		return element.variableValue?.text?.unquote()
	}
	
	@JvmStatic
	fun getUnquotedValue(element:ParadoxScriptVariable):String?{
		return element.variableValue?.text
	}
	//endregion
	
	//region ParadoxScriptProperty
	@JvmStatic
	fun getName(element: ParadoxScriptProperty): String {
		return element.stub?.key ?: element.propertyKey.text.unquote()
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptProperty, name: String): PsiElement {
		element.propertyKey.replace(createPropertyKey(element.project, name))
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptProperty): PsiElement? {
		return element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId }
	}
	
	@JvmStatic
	fun getIcon(element: ParadoxScriptProperty, @Iconable.IconFlags flags: Int): Icon {
		return paradoxScriptPropertyIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptProperty): String? {
		return element.propertyValue?.text?.unquote()
	}
	
	@JvmStatic
	fun getUnquotedValue(element:ParadoxScriptProperty):String?{
		return element.propertyValue?.text
	}
	
	@JvmStatic
	fun getTruncatedValue(element:ParadoxScriptProperty):String?{
		return element.propertyValue?.value?.let{ if(it is ParadoxScriptBlock) blockFolder else it.text }
	}
	
	@JvmStatic
	fun findProperty(element:ParadoxScriptProperty,propertyName:String):ParadoxScriptProperty?{
		val block = element.propertyValue?.value as? ParadoxScriptBlock ?: return null
		return block.propertyList.find { it.name == propertyName }
	}
	
	@JvmStatic
	fun findValue(element:ParadoxScriptProperty,value:String): ParadoxScriptValue? {
		val block = element.propertyValue?.value as? ParadoxScriptBlock ?: return null
		return block.valueList.find { it.value == value }
	}
	//endregion
	
	//region ParadoxScriptVariableReference
	@JvmStatic
	fun getName(element: ParadoxScriptVariableReference): String {
		return element.variableReferenceId.text.orEmpty()
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptVariableReference, name: String): PsiElement {
		element.replace(createValue(element.project, name))
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptVariableReference): PsiElement {
		return element.variableReferenceId
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptVariableReference): ParadoxScriptVariablePsiReference {
		return ParadoxScriptVariablePsiReference(element, TextRange(0, element.textLength))
	}
	
	@JvmStatic
	fun getReferenceValue(element:ParadoxScriptVariableReference):ParadoxScriptValue?{
		return element.reference.resolve()?.variableValue?.value
	}
	//endregion
	
	//region ParadoxScriptValue
	@JvmStatic
	fun getIcon(element: ParadoxScriptValue, @Iconable.IconFlags flags: Int): Icon {
		return paradoxScriptValueIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptValue): String {
		return element.text
	}
	//endregion
	
	//region ParadoxScriptBoolean
	@JvmStatic
	fun getValue(element: ParadoxScriptBoolean): String {
		return element.text
	}
	//endregion
	
	//region ParadoxScriptNumber
	@JvmStatic
	fun getValue(element: ParadoxScriptNumber): String {
		return element.text
	}
	//endregion
	
	//region ParadoxScriptStringValue
	@JvmStatic
	fun getValue(element: ParadoxScriptStringValue): String {
		return element.text
	}
	//endregion
	
	//region ParadoxScriptString
	@JvmStatic
	fun getValue(element: ParadoxScriptString): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptString): ParadoxScriptStringAsPropertyPsiReference {
		return ParadoxScriptStringAsPropertyPsiReference(element, TextRange(0, element.textLength))
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
			val newColor = createValue(element.project, newText) as? ParadoxScriptColor
			if(newColor != null) element.replace(newColor)
		}
	}
	
	private fun ColorHsl.toColor() = Color(ColorConversions.convertHSLtoRGB(this))
	
	private fun Color.toColorHsl() = ColorConversions.convertRGBtoHSL(this.rgb)
	
	private fun ColorHsv.toColor() = Color(ColorConversions.convertHSVtoRGB(this))
	
	private fun Color.toColorHsv() = ColorConversions.convertRGBtoHSV(this.rgb)
	//endregion
	
	//region ParadoxScriptBlock
	@JvmStatic
	fun isEmpty(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			if(it is ParadoxScriptProperty || it is ParadoxLocalisationProperty) return false
		}
		return true
	}
	
	@JvmStatic
	fun isNotEmpty(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			if(it is ParadoxScriptProperty || it is ParadoxLocalisationProperty) return true
		}
		return true
	}
	
	@JvmStatic
	fun isObject(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			when(it) {
				is ParadoxScriptProperty -> return true
				is ParadoxScriptValue -> return false
			}
		}
		return false
	}
	
	@JvmStatic
	fun isArray(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			when(it) {
				is ParadoxScriptProperty -> return false
				is ParadoxScriptValue -> return true
			}
		}
		return false
	}
	
	@JvmStatic
	fun findProperty(element:ParadoxScriptBlock,propertyName:String):ParadoxScriptProperty?{
		return element.propertyList.find { it.name == propertyName }
	}
	
	@JvmStatic
	fun findValue(element:ParadoxScriptBlock,value:String): ParadoxScriptValue? {
		return element.valueList.find { it.value == value }
	}
	
	@JvmStatic
	fun getComponents(element: ParadoxScriptBlock): List<PsiElement> {
		//如果存在元素为property，则认为所有合法的元素都是property
		return if(element.isObject) element.propertyList else element.valueList
	}
	//endregion
}
