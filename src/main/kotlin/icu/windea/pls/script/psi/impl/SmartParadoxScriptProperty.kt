package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

class SmartParadoxScriptProperty : ParadoxScriptPropertyImpl, ParadoxScriptProperty {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptPropertyStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _name: String? = null
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxDataType? = null
	@Volatile private var _pathName: String? = null
	@Volatile private var _originalPathName: String? = null
	@Volatile private var _parameterMap: Map<String, Set<SmartPsiElementPointer<ParadoxParameter>>>? = null
	
	override fun getName(): String {
		return _name ?: super.getName().also { _name = it }
	}
	
	override fun getValue(): String? {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val type: ParadoxDataType?
		get() = _valueType ?: super.type.also { _valueType = it }
	
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
		super.subtreeChanged()
	}
}
