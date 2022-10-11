package icu.windea.pls.script.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.command.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.expression.reference.*
import icu.windea.pls.script.navigation.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.reference.*
import org.apache.commons.imaging.color.*
import java.awt.*
import javax.swing.*

@Suppress("UNUSED_PARAMETER")
object ParadoxScriptPsiImplUtil {
	//region ParadoxFile
	//@JvmStatic
	//fun getValueSetValueMap(file: ParadoxScriptFile): Map<String, Set<SmartPsiElementPointer<ParadoxScriptExpressionElement>>> {
	//	val result = sortedMapOf<String, MutableSet<SmartPsiElementPointer<ParadoxScriptExpressionElement>>>() //按名字进行排序
	//	file.acceptChildren(object : ParadoxScriptRecursiveExpressionElementWalkingVisitor() {
	//		override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
	//			ProgressManager.checkCanceled()
	//			val config = element.getConfig() ?: return
	//			val dataType = config.expression.type
	//			if(dataType != CwtDataTypes.Value && dataType != CwtDataTypes.ValueSet) return
	//			val valueSetName = config.expression.value ?: return
	//			result.getOrPut(valueSetName) { mutableSetOf() }.add(element.createPointer(file))
	//			//不需要继续向下遍历
	//		}
	//	})
	//	return result
	//}
	//endregion
	
	//region ParadoxScriptRootBlock
	@JvmStatic
	fun getValue(element: ParadoxScriptRootBlock): String {
		return PlsFolders.blockFolder
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
		return element.filterChildOfType { isRootBlockComponent(it) }
	}
	
	private fun isRootBlockComponent(element: PsiElement): Boolean {
		return element is ParadoxScriptVariable || element is ParadoxScriptProperty || element is ParadoxScriptValue
	}
	//endregion
	
	//region ParadoxScriptVariable
	@JvmStatic
	fun getIcon(element: ParadoxScriptVariable, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.ScriptedVariable
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
		return element.node.startOffset + 1
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
	
	@JvmStatic
	fun getPresentation(element: ParadoxScriptVariable): ItemPresentation {
		return ParadoxScriptVariablePresentation(element)
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
		return PlsIcons.ScriptParameter
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptParameter): String? {
		return element.parameterId?.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptParameter, name: String): ParadoxScriptParameter {
		val nameElement = element.parameterId ?: return element
		val newNameElement = ParadoxScriptElementFactory.createParameter(element.project, name).parameterId!!
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptParameter): PsiElement? {
		return element.parameterId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptParameter): Int {
		return element.node.startOffset + 1
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptParameter): String {
		return element.text
	}
	
	@JvmStatic
	fun getDefaultValue(element: ParadoxScriptParameter): String? {
		return element.defaultValueToken?.text
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptParameter): ParadoxParameterReference? {
		val nameElement = element.parameterId ?: return null
		return ParadoxParameterReference(element, nameElement.textRangeInParent)
	}
	//endregion
	
	//region ParadoxScriptProperty
	@JvmStatic
	fun getIcon(element: ParadoxScriptProperty, @Iconable.IconFlags flags: Int): Icon {
		if(element.definitionInfo != null) return PlsIcons.Definition
		return PlsIcons.ScriptProperty
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
		return if(!element.propertyKey.isParameterAwareExpression()) element.firstChild  else null
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
	fun getDefinitionType(element: ParadoxScriptProperty): String? {
		return element.definitionInfo?.typesText
	}
	
	@JvmStatic
	fun getConfigExpression(element: ParadoxScriptProperty): String? {
		return element.getPropertyConfig()?.propertyConfigExpression
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptProperty): ParadoxValueType? {
		return element.propertyValue?.value?.valueType
	}
	
	@JvmStatic
	fun getPathName(element: ParadoxScriptProperty): String? {
		return element.propertyKey.text
	}
	
	@JvmStatic
	fun getOriginalPathName(element: ParadoxScriptProperty): String {
		return element.propertyKey.value
	}
	
	@JvmStatic
	fun getParameterMap(element: ParadoxScriptProperty): Map<String, Set<SmartPsiElementPointer<IParadoxScriptParameter>>> {
		val file = element.containingFile
		val result = sortedMapOf<String, MutableSet<SmartPsiElementPointer<IParadoxScriptParameter>>>() //按名字进行排序
		element.acceptChildren(object : ParadoxScriptRecursiveElementWalkingVisitor() {
			override fun visitIParadoxScriptParameter(e: IParadoxScriptParameter) {
				ProgressManager.checkCanceled()
				result.getOrPut(e.name) { mutableSetOf() }.add(e.createPointer(file))
				//不需要继续向下遍历
			}
		})
		return result
	}
	
	@JvmStatic
	fun getPresentation(element: ParadoxScriptProperty): ItemPresentation {
		return ParadoxScriptPropertyPresentation(element)
	}
	//endregion
	
	//region ParadoxScriptPropertyKey
	@JvmStatic
	fun getName(element: ParadoxScriptPropertyKey): String {
		return getValue(element)
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptPropertyKey, value: String): ParadoxScriptPropertyKey {
		return setValue(element, value)
	}
	
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
	fun getNameIdentifier(element: ParadoxScriptPropertyKey): PsiElement? {
		return if(!element.isParameterAwareExpression()) element.firstChild  else null
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptPropertyKey): ParadoxScriptKeyReference? {
		return element.references.firstOrNull().castOrNull()
	}
	
	@JvmStatic
	fun getReferences(element: ParadoxScriptPropertyKey): Array<out PsiReference> {
		return PsiReferenceService.getService().getContributedReferences(element)
	}
	
	@JvmStatic
	fun getConfigExpression(element: ParadoxScriptPropertyKey): String? {
		return element.getPropertyConfig()?.keyConfigExpression
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptPropertyKey): ParadoxValueType {
		element.processChild {
			when(it.elementType) {
				PROPERTY_KEY_TOKEN -> return ParadoxValueType.infer(it.text)
				QUOTED_PROPERTY_KEY_TOKEN -> return ParadoxValueType.StringType
				PARAMETER_ID -> return ParadoxValueType.ParameterType
				else -> end()
			}
		}
		return ParadoxValueType.UnknownType
	}
	//endregion
	
	//region ParadoxScriptVariableReference
	@JvmStatic
	fun getIcon(element: ParadoxScriptVariableReference, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.ScriptedVariable
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
	fun getReference(element: ParadoxScriptVariableReference): ParadoxScriptedVariableReference {
		val rangeInElement = element.variableReferenceId.textRangeInParent
		return ParadoxScriptedVariableReference(element, rangeInElement)
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptVariableReference): ParadoxValueType {
		return element.reference.resolve()?.valueType ?: ParadoxValueType.UnknownType
	}
	//endregion
	
	//region ParadoxScriptValue
	@JvmStatic
	fun getIcon(element: ParadoxScriptValue, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.ScriptValue
	}
	
	@JvmStatic
	fun getValue(element: ParadoxScriptValue): String {
		return element.text
	}
	
	@JvmStatic
	fun getConfigExpression(element: ParadoxScriptValue): String? {
		return element.getValueConfig()?.valueConfigExpression
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptValue): ParadoxValueType {
		return ParadoxValueType.UnknownType
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
	//endregion
	
	//region ParadoxScriptString
	@JvmStatic
	fun getIcon(element: ParadoxScriptString, @Iconable.IconFlags flags: Int): Icon {
		//特殊处理字符串需要被识别为标签的情况
		if(element.resolveTagConfig() != null) return PlsIcons.Tag
		return PlsIcons.ScriptValue
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptString): String {
		return getValue(element)
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptString, value: String): ParadoxScriptString {
		return setValue(element, value)
	}
	
	
	@JvmStatic
	fun getValue(element: ParadoxScriptString): String {
		return element.text.unquote()
	}
	
	@JvmStatic
	fun setValue(element: ParadoxScriptString, name: String): ParadoxScriptString {
		val newElement = ParadoxScriptElementFactory.createString(element.project, name)
		element.replace(newElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptString): PsiElement? {
		return if(!element.isParameterAwareExpression()) element.firstChild else null
	}
	
	@JvmStatic
	fun getStringValue(element: ParadoxScriptString): String {
		return element.value
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptString): ParadoxScriptValueReference? {
		return element.references.firstOrNull().castOrNull()
	}
	
	@JvmStatic
	fun getReferences(element: ParadoxScriptString): Array<out PsiReference> {
		return PsiReferenceService.getService().getContributedReferences(element)
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptString): ParadoxValueType {
		return ParadoxValueType.StringType
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
				"rgb" -> args.takeIf { it.size == 3 }?.map { it.toInt() }?.let { Color(it[0], it[1], it[2]) }
				"rgba" -> args.takeIf { it.size == 3 || it.size == 4 }?.map { it.toInt() }?.let { Color(it[0], it[1], it[2], it.getOrElse(3) { 255 }) }
				"hsb" -> args.takeIf { it.size == 3 }?.map { it.toFloat() }?.let { Color.getHSBColor(it[0], it[1], it[2]) }
				"hsv" -> args.takeIf { it.size == 3 }?.map { it.toDouble() }?.let { ColorHsv(it[0], it[1], it[2]).toColor() }
				"hsl" -> args.takeIf { it.size == 3 }?.map { it.toDouble() }?.let { ColorHsl(it[0], it[1], it[2]).toColor() }
				else -> null
			}
		}.getOrNull()
	}
	
	@JvmStatic
	fun setColor(element: ParadoxScriptColor, color: Color) {
		//忽略异常
		runCatching {
			val project = element.project
			val text = element.text
			val colorType = text.substringBefore('{').trim()
			//仅支持设置rgb/rgba颜色
			if(colorType != "rgb" && colorType != "rgba") return //中断操作
			val shouldBeRgba = color.alpha != 255
			val newText = when {
				colorType == "rgba" || shouldBeRgba -> "rgba { ${color.run { "$red $green $blue $alpha" }} }"
				else -> "rgb { ${color.run { "$red $green $blue" }} }"
			}
			//不支持根据不同的颜色类型生成不同的文本
			//val newText = when(colorType) {
			//	"rgb" -> "rgb { ${color.run { "$red $green $blue" }} }"
			//	"rgba" -> "rgba { ${color.run { "$red $green $blue $alpha" }} }"
			//	"hsv" -> "hsv { ${color.toColorHsv().run { "$H $S $V" }} }"
			//	"hsl" -> "hsl { ${color.toColorHsl().run { "$H $S $L" }} }"
			//	else -> "rgba { ${color.run { "$red $green $blue $alpha" }} }"
			//}
			val newColor = ParadoxScriptElementFactory.createValue(project, newText) as? ParadoxScriptColor
			if(newColor != null) {
				val command = Runnable {
					element.replace(newColor)
				}
				val document = PsiDocumentManager.getInstance(project).getDocument(element.containingFile)
				CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
			}
		}
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptColor): ParadoxValueType {
		return ParadoxValueType.ColorType
	}
	//endregion
	
	//region ParadoxScriptBlock
	@JvmStatic
	fun getIcon(element: ParadoxScriptBlock, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.ScriptBlock
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
	fun getComponents(element: ParadoxScriptBlock): List<PsiElement> {
		//允许混合value和property
		return element.filterChildOfType { isBlockComponent(it) }
	}
	
	private fun isBlockComponent(element: PsiElement): Boolean {
		return element is ParadoxScriptVariable || element is ParadoxScriptProperty || element is ParadoxScriptValue
			|| element is ParadoxScriptParameterCondition
	}
	
	@JvmStatic
	fun getColor(element: ParadoxScriptBlock): Color? {
		//忽略异常
		return runCatching {
			val parent = element.parent
			val colorTypeOptionLocation = when {
				parent is ParadoxScriptPropertyValue -> parent.parent.castOrNull<ParadoxScriptProperty>()?.getPropertyConfig(allowDefinitionSelf = true)
				else -> element.getValueConfig()
			}
			val colorTypeOption = colorTypeOptionLocation?.options?.find { it.key == "color_type" } ?: return@runCatching null
			val colorType = colorTypeOption.stringValue ?: return@runCatching null
			//目前仅支持rgb和rgba
			if(colorType == "rgb" || colorType == "rgba") {
				val values = element.findValues<ParadoxScriptValue>()
				getColorFromValues(colorType, values)
			} else {
				null
			}
		}.getOrNull()
	}
	
	private fun getColorFromValues(colorType: String, values: List<ParadoxScriptValue>): Color? {
		when(colorType) {
			"rgb" -> {
				if(values.size != 3) return null
				when {
					values.all { it is ParadoxScriptInt } -> {
						return values.map { it.cast<ParadoxScriptInt>().intValue }.let { Color(it[0], it[1], it[2]) }
					}
					values.all { it is ParadoxScriptFloat } -> {
						return values.map { it.cast<ParadoxScriptFloat>().floatValue }.let { Color(it[0], it[1], it[2]) }
					}
					else -> return null
				}
			}
			"rgba" -> {
				if(values.size != 3 && values.size != 4) return null
				when {
					values.all { it is ParadoxScriptInt } -> {
						return values.map { it.cast<ParadoxScriptInt>().intValue }.let { Color(it[0], it[1], it[2], it.getOrElse(3) { 255 }) }
					}
					values.all { it is ParadoxScriptFloat } -> {
						return values.map { it.cast<ParadoxScriptFloat>().floatValue }.let { Color(it[0], it[1], it[2], it.getOrElse(3) { 1.0f }) }
					}
					else -> return null
				}
			}
			else -> return null
		}
	}
	
	@JvmStatic
	fun setColor(element: ParadoxScriptBlock, color: Color) {
		//FIXME 首次选择颜色后不关闭取色器，继续选择颜色，文档不会发生相应的变更，得到的document=null
		runCatching {
			val project = element.project
			val values = element.findValues<ParadoxScriptValue>()
			//仅支持设置rgb/rgba颜色
			if(values.size != 3 && values.size != 4) return //中断操作
			val isRgba = values.size == 4
			val newText = color.run {
				when {
					values.all { it is ParadoxScriptInt } -> {
						if(isRgba) "{ $red $green $blue $alpha }" else "{ $red $green $blue }"
					}
					values.all { it is ParadoxScriptFloat } -> {
						if(isRgba) "{ ${red.asFloat()} ${green.asFloat()} ${blue.asFloat()} ${alpha.asFloat()} }"
						else "{ ${red.asFloat()} ${green.asFloat()} ${blue.asFloat()} }"
					}
					else -> return //中断操作
				}
			}
			val newBlock = ParadoxScriptElementFactory.createValue(project, newText) as? ParadoxScriptBlock
			if(newBlock != null) {
				val documentManager = PsiDocumentManager.getInstance(project)
				val document = documentManager.getDocument(element.containingFile)?:return
				val command = Runnable {
					element.replace(newBlock)
				}
				documentManager.doPostponedOperationsAndUnblockDocument(document)
				CommandProcessor.getInstance().executeCommand(project, command, PlsBundle.message("script.command.changeColor.name"), null, document)
			}
		}
	}
	
	private fun Int.asFloat(): String {
		return (this / 255.0).format(-2)
	}
	
	@JvmStatic
	fun getValueType(element: ParadoxScriptBlock): ParadoxValueType {
		return ParadoxValueType.BlockType
	}
	//endregion
	
	//region ParadoxScriptParameterCondition
	@JvmStatic
	fun getIcon(element: ParadoxScriptParameterCondition, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.ScriptParameterCondition
	}
	
	@JvmStatic
	fun getExpression(element: ParadoxScriptParameterCondition): String? {
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
		return element.filterChildOfType { isParameterConditionComponent(it) }
	}
	
	private fun isParameterConditionComponent(element: PsiElement): Boolean {
		return element is ParadoxScriptVariable || element is ParadoxScriptProperty || element is ParadoxScriptValue
	}
	//endregion
	
	//region ParadoxScriptParameterConditionParameter
	@JvmStatic
	fun getIcon(element: ParadoxScriptParameterConditionParameter, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.ScriptParameter
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptParameterConditionParameter): String {
		return element.parameterId.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptParameterConditionParameter, name: String): ParadoxScriptParameterConditionParameter {
		val nameElement = element.parameterId
		val newNameElement = ParadoxScriptElementFactory.createInlineMathParameter(element.project, name).parameterId!!
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptParameterConditionParameter): PsiElement {
		return element.parameterId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptParameterConditionParameter): Int {
		return element.node.startOffset
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptParameterConditionParameter): ParadoxParameterReference {
		val nameElement = element.parameterId
		return ParadoxParameterReference(element, nameElement.textRangeInParent)
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
		return PlsIcons.ScriptedVariable
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
	fun getReference(element: ParadoxScriptInlineMathVariableReference): ParadoxScriptedVariableReference {
		val rangeInElement = element.variableReferenceId.textRangeInParent
		return ParadoxScriptedVariableReference(element, rangeInElement)
	}
	//endregion
	
	//region ParadoxScriptInlineMathParameter
	@JvmStatic
	fun getIcon(element: ParadoxScriptInlineMathParameter, @Iconable.IconFlags flags: Int): Icon {
		return PlsIcons.ScriptParameter
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptInlineMathParameter): String? {
		return element.parameterId?.text
	}
	
	@JvmStatic
	fun setName(element: ParadoxScriptInlineMathParameter, name: String): ParadoxScriptInlineMathParameter {
		val nameElement = element.parameterId ?: return element
		val newNameElement = ParadoxScriptElementFactory.createInlineMathParameter(element.project, name).parameterId!!
		nameElement.replace(newNameElement)
		return element
	}
	
	@JvmStatic
	fun getNameIdentifier(element: ParadoxScriptInlineMathParameter): PsiElement? {
		return element.parameterId
	}
	
	@JvmStatic
	fun getTextOffset(element: ParadoxScriptInlineMathParameter): Int {
		return element.node.startOffset + 1
	}
	
	@JvmStatic
	fun getDefaultValue(element: ParadoxScriptInlineMathParameter): String? {
		return element.defaultValueToken?.text
	}
	
	@JvmStatic
	fun getReference(element: ParadoxScriptInlineMathParameter): ParadoxParameterReference? {
		val nameElement = element.parameterId ?: return null
		return ParadoxParameterReference(element, nameElement.textRangeInParent)
	}
	//endregion
}
