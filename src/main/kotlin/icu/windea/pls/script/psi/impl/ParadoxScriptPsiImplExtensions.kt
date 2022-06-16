package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

//region ParadoxScriptProperty
class SmartParadoxScriptProperty : ParadoxScriptPropertyImpl, ParadoxScriptProperty {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptPropertyStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _name: String? = null
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxValueType? = null
	@Volatile private var _pathName: String? = null
	@Volatile private var _originalPathName: String? = null
	@Volatile private var _parameterNames: Set<String>? = null
	
	override fun getName(): String {
		return _name ?: super.getName().also { _name = it }
	}
	
	override fun getValue(): String? {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val valueType: ParadoxValueType?
		get() = _valueType ?: super.valueType.also { _valueType = it }
	
	override val pathName: String?
		get() = _pathName ?: super.pathName.also { _pathName = it }
	
	override val originalPathName: String
		get() = _originalPathName ?: super.originalPathName.also { _originalPathName = it }
	
	internal fun getParameterNames(): Set<String>? {
		return _parameterNames ?: doGetParameterNames().also { _parameterNames = it }
	}
	
	private fun doGetParameterNames(): Set<String>? {
		if(!CwtConfigHandler.supportsParameters(this)) return null
		val result = sortedSetOf<String>() //按名字进行排序
		acceptChildren(object : PsiRecursiveElementVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is IParadoxScriptParameter) {
					result.add(element.name)
					return
				}
				super.visitElement(element)
			}
		})
		return result
	}
	
	override fun subtreeChanged() {
		_name = null
		_value = null
		_valueType = null
		_pathName = null
		_originalPathName = null
		_parameterNames = null
		super.subtreeChanged()
	}
}

/**
 * 得到特定定义声明（scripted_effect/scripted_trigger）中所有被引用的参数的名字。
 */
val ParadoxDefinitionProperty.parameterNames: Set<String>? get() = if(this is SmartParadoxScriptProperty) this.getParameterNames() else null
//endregion

//region ParadoxScriptPropertyKey
class SmartParadoxScriptPropertyKey : ParadoxScriptPropertyKeyImpl, ParadoxScriptPropertyKey {
	constructor(node: ASTNode) : super(node)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxValueType? = null
	@Volatile private var _kvExpressionInfo: ParadoxKvExpressionInfo? = null
	
	override fun getValue(): String {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val valueType: ParadoxValueType
		get() = _valueType ?: super.valueType.also { _valueType = it }
	
	internal fun getKvExpressionInfo(): ParadoxKvExpressionInfo {
		return _kvExpressionInfo ?: doGetKvExpressionInfo().also { _kvExpressionInfo = it }
	}
	
	private fun doGetKvExpressionInfo(): ParadoxKvExpressionInfo {
		val children = children
		if(children.isNotEmpty()) {
			when(children.first().elementType) {
				PROPERTY_KEY_TOKEN -> {
					val text = text
					val dotIndices = text.indicesOf('.')
					if(dotIndices.isNotEmpty()){
						val propertyConfig = getPropertyConfig()
						if(propertyConfig != null && CwtConfigHandler.supportsScopes(propertyConfig)){
							val textRangeInParent = textRangeInParent
							val ranges =  MutableList(dotIndices.size + 1) { i ->
								val start = textRangeInParent.startOffset
								val end = textRangeInParent.endOffset
								when {
									i == 0 -> TextRange.create(start, start + dotIndices[i])
									i == dotIndices.size -> TextRange.create(start + dotIndices[i - 1] + 1, end)
									else -> TextRange.create(start + dotIndices[i - 1] + 1, start + dotIndices[i])
								}
							}
							return ParadoxKvExpressionInfo(ParadoxKvExpressionType.ScopeExpression, textRangeInParent, ranges)
						}
					}
					//TODO linkValue
					//TODO scriptValue
				}
				PARAMETER, KEY_STRING_SNIPPET -> {
					if(children.size == 1) {
						return ParadoxKvExpressionInfo(ParadoxKvExpressionType.ParameterType, textRangeInParent, listOf(textRangeInParent))
					} else {
						return ParadoxKvExpressionInfo(ParadoxKvExpressionType.StringTemplateType, textRangeInParent, children.map { it.textRangeInParent })
					}
				}
				QUOTED_PROPERTY_KEY_TOKEN ->pass()
				else -> pass()
			}
		}
		return ParadoxKvExpressionInfo(ParadoxKvExpressionType.LiteralType, textRangeInParent, listOf(textRangeInParent))
	}
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		_kvExpressionInfo = null
		super.subtreeChanged()
	}
}

val ParadoxScriptPropertyKey.kvExpressionInfo: ParadoxKvExpressionInfo? get() = if(this is SmartParadoxScriptPropertyKey) this.getKvExpressionInfo() else null
//endregion

//region ParadoxScriptString
class SmartParadoxScriptString : ParadoxScriptStringImpl, ParadoxScriptString {
	constructor(node: ASTNode) : super(node)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxValueType? = null
	@Volatile private var _kvExpressionInfo: ParadoxKvExpressionInfo? = null
	
	override fun getValue(): String {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val valueType: ParadoxValueType
		get() = _valueType ?: super.valueType.also { _valueType = it }
	
	internal fun getKvExpressionInfo(): ParadoxKvExpressionInfo {
		return _kvExpressionInfo ?: doGetKvExpressionInfo().also { _kvExpressionInfo = it }
	}
	
	private fun doGetKvExpressionInfo(): ParadoxKvExpressionInfo {
		val children = children
		if(children.isNotEmpty()) {
			when(children.first().elementType) {
				STRING_TOKEN -> {
					val text = text
					val dotIndices = text.indicesOf('.')
					if(dotIndices.isNotEmpty()){
						val valueConfig = getValueConfig()
						if(valueConfig != null && CwtConfigHandler.supportsScopes(valueConfig)){
							val textRangeInParent = textRangeInParent
							val ranges =  MutableList(dotIndices.size + 1) { i ->
								val start = textRangeInParent.startOffset
								val end = textRangeInParent.endOffset
								when {
									i == 0 -> TextRange.create(start, start + dotIndices[i])
									i == dotIndices.size -> TextRange.create(start + dotIndices[i - 1] + 1, end)
									else -> TextRange.create(start + dotIndices[i - 1] + 1, start + dotIndices[i])
								}
							}
							return ParadoxKvExpressionInfo(ParadoxKvExpressionType.ScopeExpression, textRangeInParent, ranges)
						}
					}
					//TODO linkValue
					//TODO scriptValue
				}
				PARAMETER, VALUE_STRING_SNIPPET -> {
					if(children.size == 1) {
						return ParadoxKvExpressionInfo(ParadoxKvExpressionType.ParameterType, textRangeInParent, listOf(textRangeInParent))
					} else {
						return ParadoxKvExpressionInfo(ParadoxKvExpressionType.StringTemplateType, textRangeInParent, children.map { it.textRangeInParent })
					}
				}
				QUOTED_STRING_TOKEN ->pass()
				else -> pass()
			}
		}
		return ParadoxKvExpressionInfo(ParadoxKvExpressionType.LiteralType, textRangeInParent, listOf(textRangeInParent))
	}
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		_kvExpressionInfo = null
		super.subtreeChanged()
	}
}

val ParadoxScriptString.kvExpressionInfo: ParadoxKvExpressionInfo? get() = if(this is SmartParadoxScriptString) this.getKvExpressionInfo() else null
//endregion