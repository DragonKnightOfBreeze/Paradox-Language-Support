package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class SmartParadoxScriptProperty : ParadoxScriptPropertyImpl, ParadoxScriptProperty {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptPropertyStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _name: String? = null
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxDataType? = null
	@Volatile private var _parameters: Map<String, ParadoxParameterInfo>? = null
	
	override fun getName(): String {
		return _name ?: super.getName().also { _name = it }
	}
	
	override fun getValue(): String? {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val type: ParadoxDataType?
		get() = _valueType ?: super.type.also { _valueType = it }
	
	override val parameters: Map<String, ParadoxParameterInfo>
		get() = _parameters ?: super.parameters.also { _parameters = it }
	
	override fun subtreeChanged() {
		_name = null
		_value = null
		_valueType = null
		_parameters = null
		super.subtreeChanged()
	}
}
