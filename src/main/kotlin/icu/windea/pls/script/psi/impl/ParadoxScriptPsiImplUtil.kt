package icu.windea.pls.script.psi.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementFactory.createPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptElementFactory.createValue
import icu.windea.pls.script.psi.ParadoxScriptElementFactory.createVariableName
import icu.windea.pls.script.reference.*
import org.apache.commons.imaging.color.*
import java.awt.*
import javax.swing.*
import kotlin.collections.List
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4
import kotlin.math.*

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptPsiImplUtil {
	//region ParadoxScriptVariable
	@JvmStatic
	fun getIcon(element: ParadoxScriptVariable, @Iconable.IconFlags flags: Int): Icon {
		return scriptVariableIcon
	}
	
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
	fun checkRename(element: ParadoxScriptVariable) {
		
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptVariable): PsiElement {
		return element.variableName.variableNameId
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptVariable): String? {
		return element.variableValue?.text?.unquote()
	}
	
	@JvmStatic
	fun getUnquotedValue(element: ParadoxScriptVariable): String? {
		return element.variableValue?.text
	}
	//endregion
	
	//region ParadoxScriptProperty
	@JvmStatic
	fun getIcon(element: ParadoxScriptProperty, @Iconable.IconFlags flags: Int): Icon {
		return scriptPropertyIcon
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptProperty): String {
		return element.stub?.name ?: element.propertyKey.text.unquote()
	}
	
	//TODO 检查是否是项目中的definition，这样才允许重命名
	@JvmStatic
	fun setName(element: ParadoxScriptProperty, name: String): PsiElement {
		element.propertyKey.replace(createPropertyKey(element.project, name))
		return element
	}
	
	@JvmStatic
	fun checkRename(element: ParadoxScriptProperty) {
		
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptProperty): PsiElement? {
		return element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId }
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptProperty): String? {
		return element.propertyValue?.text?.unquote()
	}
	
	@JvmStatic
	fun getUnquotedValue(element: ParadoxScriptProperty): String? {
		return element.propertyValue?.value?.text
	}
	
	@JvmStatic
	fun getTruncatedValue(element: ParadoxScriptProperty): String? {
		return element.propertyValue?.value?.let { if(it is ParadoxScriptBlock) blockFolder else it.text.truncate(truncateLimit) }
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
	fun findProperty(element: ParadoxScriptProperty, propertyName: String): ParadoxScriptProperty? {
		val block = element.propertyValue?.value as? ParadoxScriptBlock ?: return null
		return block.propertyList.find { it.name == propertyName }
	}
	
	@JvmStatic
	fun findProperties(element: ParadoxScriptProperty, propertyName: String): List<ParadoxScriptProperty> {
		val block = element.propertyValue?.value as? ParadoxScriptBlock ?: return emptyList()
		return block.propertyList.filter { it.name == propertyName }
	}
	
	@JvmStatic
	fun findValue(element: ParadoxScriptProperty, value: String): ParadoxScriptValue? {
		val block = element.propertyValue?.value as? ParadoxScriptBlock ?: return null
		return block.valueList.find { it.value == value }
	}
	
	@JvmStatic
	fun findValues(element: ParadoxScriptProperty, value: String): List<ParadoxScriptValue> {
		val block = element.propertyValue?.value as? ParadoxScriptBlock ?: return emptyList()
		return block.valueList.filter { it.value == value }
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
	fun getReference(element: ParadoxScriptVariableReference): ParadoxScriptVariablePsiReference {
		return ParadoxScriptVariablePsiReference(element, TextRange(0, element.textLength))
	}
	
	@JvmStatic
	fun getReferenceValue(element: ParadoxScriptVariableReference): ParadoxScriptValue? {
		return element.reference.resolve()?.variableValue?.value
	}
	//endregion
	
	//region ParadoxScriptValue
	@JvmStatic
	fun getIcon(element: ParadoxScriptValue, @Iconable.IconFlags flags: Int): Icon {
		return scriptValueIcon
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptValue): String {
		return element.text
	}
	
	@JvmStatic
	fun getTruncatedValue(element: ParadoxScriptValue):String{
		return element.value
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
		return element.value.toIntOrNull()?:0
	}
	//endregion
	
	//region ParadoxScriptFloat
	@JvmStatic
	fun getFloatValue(element: ParadoxScriptFloat): Float {
		return element.value.toFloatOrNull()?:0f
	}
	//endregion
	
	//region ParadoxScriptString
	@JvmStatic
	fun getValue(element: ParadoxScriptString): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun setValue(element: ParadoxScriptString, name: String): PsiElement {
		element.replace(createValue(element.project, name.quoteAsStringLike()))
		return element
	}
	
	@JvmStatic
	fun getStringValue(element: ParadoxScriptString): String {
		return element.value
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptString): ParadoxScriptStringPropertyPsiReference {
		return ParadoxScriptStringPropertyPsiReference(element, TextRange(0, element.textLength))
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
	fun getValue(element: ParadoxScriptBlock): String {
		return emptyBlockString
	}
	
	@JvmStatic
	fun getTruncatedValue(element: ParadoxScriptBlock): String {
		return blockFolder
	}
	
	@JvmStatic
	fun isEmpty(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			if(it is ParadoxScriptProperty || it is ParadoxScriptValue) return false
		}
		return true
	}
	
	@JvmStatic
	fun isNotEmpty(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			if(it is ParadoxScriptProperty || it is ParadoxScriptValue) return true
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
		return true
	}
	
	@JvmStatic
	fun isArray(element: ParadoxScriptBlock): Boolean {
		element.forEachChild {
			when(it) {
				is ParadoxScriptProperty -> return false
				is ParadoxScriptValue -> return true
			}
		}
		return true
	}
	
	@JvmStatic
	fun getComponents(element: ParadoxScriptBlock): List<PsiElement> {
		//如果存在元素为property，则认为所有合法的元素都是property
		return if(element.isObject) element.propertyList else element.valueList
	}
	
	@JvmStatic
	fun findProperty(element: ParadoxScriptBlock, propertyName: String): ParadoxScriptProperty? {
		return element.propertyList.find { it.name == propertyName }
	}
	
	@JvmStatic
	fun findValue(element: ParadoxScriptBlock, value: String): ParadoxScriptValue? {
		return element.valueList.find { it.value == value }
	}
	//endregion
}
