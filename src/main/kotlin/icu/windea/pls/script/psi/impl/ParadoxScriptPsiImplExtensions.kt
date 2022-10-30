package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*

//region ParadoxScriptProperty
class SmartParadoxScriptProperty : ParadoxScriptPropertyImpl, ParadoxScriptProperty {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptPropertyStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _name: String? = null
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxScriptExpressionType? = null
	@Volatile private var _pathName: String? = null
	@Volatile private var _originalPathName: String? = null
	@Volatile private var _parameterMap: Map<String, Set<SmartPsiElementPointer<ParadoxParameter>>>? = null
	
	override fun getName(): String {
		return _name ?: super.getName().also { _name = it }
	}
	
	override fun getValue(): String? {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val expressionType: ParadoxScriptExpressionType?
		get() = _valueType ?: super.expressionType.also { _valueType = it }
	
	override val pathName: String?
		get() = _pathName ?: super.pathName.also { _pathName = it }
	
	override val originalPathName: String
		get() = _originalPathName ?: super.originalPathName.also { _originalPathName = it }
	
	override val parameterMap: Map<String, Set<SmartPsiElementPointer<ParadoxParameter>>>
		get() = _parameterMap ?: super.parameterMap.also { _parameterMap = it }
	
	override fun subtreeChanged() {
		_name = null
		_value = null
		_valueType = null
		_pathName = null
		_originalPathName = null
		_parameterMap = null
		clearCachedData()
		super.subtreeChanged()
	}
	
	private fun clearCachedData() {
		//清除基于定义结构的配置信息
		PlsKeys.definitionConfigKeys.forEach { putUserData(it, null) }
	}
}
//endregion

//region ParadoxScriptPropertyKey
class SmartParadoxScriptPropertyKey : ParadoxScriptPropertyKeyImpl, ParadoxScriptPropertyKey {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptPropertyKeyStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxScriptExpressionType? = null
	
	override fun getValue(): String {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val expressionType: ParadoxScriptExpressionType
		get() = _valueType ?: super.expressionType.also { _valueType = it }
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		clearCachedData()
		super.subtreeChanged()
	}
	
	private fun clearCachedData() {
		//当key更改时，需要刷新key所在property以及下面的所有对应的definitionInfo和definitionElementInfo
		parent.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptProperty || element is ParadoxScriptValue || element is ParadoxScriptPropertyValue) {
					if(element is ParadoxScriptProperty) {
						element.putUserData(PlsKeys.cachedDefinitionInfoKey, null)
					}
					if(element is ParadoxScriptProperty || element is ParadoxScriptValue) {
						element.putUserData(PlsKeys.definitionElementInfoKey, null)
					}
					super.visitElement(element)
				}
			}
		})
	}
}
//endregion

//region ParadoxScriptString
class SmartParadoxScriptString : ParadoxScriptStringImpl, ParadoxScriptString {
	constructor(stub: ParadoxScriptStringStub, type: IStubElementType<*,*>): super(stub, type)
	
	constructor(node: ASTNode) : super(node)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxScriptExpressionType? = null
	
	override fun getValue(): String {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val expressionType: ParadoxScriptExpressionType
		get() = _valueType ?: super.expressionType.also { _valueType = it }
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		clearCachedData()
		super.subtreeChanged()
	}
	
	private fun clearCachedData() {
		//当value更改时，需要刷新value对应的definitionElementInfo
		putUserData(PlsKeys.definitionElementInfoKey, null)
	}
}
//endregion