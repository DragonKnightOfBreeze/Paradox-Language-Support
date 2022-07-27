package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.expression.*
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
	@Volatile private var _parameterMap: Map<String, Set<SmartPsiElementPointer<IParadoxScriptParameter>>>? = null
	
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
	
	override val parameterMap: Map<String, Set<SmartPsiElementPointer<IParadoxScriptParameter>>>
		get() = _parameterMap ?: super.parameterMap.also { _parameterMap = it }
	
	override fun subtreeChanged() {
		_name = null
		_value = null
		_valueType = null
		_pathName = null
		_originalPathName = null
		_parameterMap = null
		clearDefinitionElementInfo() //清除其中的定义元素信息
		PlsKeys.definitionConfigKeys.forEach { putUserData(it, null) } //清除基于定义结构的配置信息
		super.subtreeChanged()
	}
}
//endregion

//region ParadoxScriptPropertyKey
class SmartParadoxScriptPropertyKey : ParadoxScriptPropertyKeyImpl, ParadoxScriptPropertyKey {
	constructor(node: ASTNode) : super(node)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxValueType? = null
	@Volatile private var _expressionInfo: ParadoxKvExpressionInfo? = null
	
	override fun getValue(): String {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val valueType: ParadoxValueType
		get() = _valueType ?: super.valueType.also { _valueType = it }
	
	internal fun getExpressionInfo(): ParadoxKvExpressionInfo {
		return _expressionInfo ?: doGetExpressionInfo().also { _expressionInfo = it }
	}
	
	private fun doGetExpressionInfo(): ParadoxKvExpressionInfo {
		val children = children
		if(children.isNotEmpty()) {
			when(children.first().elementType) {
				PROPERTY_KEY_TOKEN -> {
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
				QUOTED_PROPERTY_KEY_TOKEN -> pass()
				else -> pass()
			}
		}
		return ParadoxKvExpressionInfo(ParadoxKvExpressionType.LiteralType, textRangeInParent, listOf(textRangeInParent))
	}
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		_expressionInfo = null
		super.subtreeChanged()
	}
}

val ParadoxScriptPropertyKey.expressionInfo: ParadoxKvExpressionInfo? get() = if(this is SmartParadoxScriptPropertyKey) this.getExpressionInfo() else null
//endregion

//region ParadoxScriptString
class SmartParadoxScriptString : ParadoxScriptStringImpl, ParadoxScriptString {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptValueStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxValueType? = null
	@Volatile private var _expressionInfo: ParadoxKvExpressionInfo? = null
	
	override val value: String
		get() = _value ?: super.value.also { _value = it }
	
	override val valueType: ParadoxValueType
		get() = _valueType ?: super.valueType.also { _valueType = it }
	
	internal fun getExpressionInfo(): ParadoxKvExpressionInfo {
		return _expressionInfo ?: doGetKvExpressionInfo().also { _expressionInfo = it }
	}
	
	private fun doGetKvExpressionInfo(): ParadoxKvExpressionInfo {
		val children = children
		val wholeRange = TextRange.create(0, textLength)
		if(children.isNotEmpty()) {
			val firstChild = children.first()
			when(firstChild.elementType) {
				STRING_TOKEN -> {
					
				}
				PARAMETER, VALUE_STRING_SNIPPET -> {
					if(children.size == 1) {
						return ParadoxKvExpressionInfo(ParadoxKvExpressionType.ParameterType, wholeRange, listOf(wholeRange))
					} else {
						return ParadoxKvExpressionInfo(ParadoxKvExpressionType.StringTemplateType, wholeRange, children.map { it.textRangeInParent })
					}
				}
				QUOTED_STRING_TOKEN -> pass()
				else -> pass()
			}
		}
		return ParadoxKvExpressionInfo(ParadoxKvExpressionType.LiteralType, wholeRange, listOf(wholeRange))
	}
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		_expressionInfo = null
		super.subtreeChanged()
	}
}

val ParadoxScriptString.expressionInfo: ParadoxKvExpressionInfo? get() = if(this is SmartParadoxScriptString) this.getExpressionInfo() else null
//endregion