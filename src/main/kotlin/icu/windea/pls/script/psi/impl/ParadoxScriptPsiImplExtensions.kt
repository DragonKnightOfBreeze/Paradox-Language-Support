package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

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
	
	override fun getValue(): String {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val valueType: ParadoxValueType
		get() = _valueType ?: super.valueType.also { _valueType = it }
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		super.subtreeChanged()
	}
}
//endregion

//region ParadoxScriptString
class SmartParadoxScriptString : ParadoxScriptStringImpl, ParadoxScriptString {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptValueStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxValueType? = null
	
	override val value: String
		get() = _value ?: super.value.also { _value = it }
	
	override val valueType: ParadoxValueType
		get() = _valueType ?: super.valueType.also { _valueType = it }
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		super.subtreeChanged()
	}
}
//endregion