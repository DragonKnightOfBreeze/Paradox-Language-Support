package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.script.exp.*
import icu.windea.pls.script.psi.*

class SmartParadoxScriptString : ParadoxScriptStringImpl, ParadoxScriptString {
	constructor(stub: ParadoxScriptStringStub, type: IStubElementType<*,*>): super(stub, type)
	
	constructor(node: ASTNode) : super(node)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxDataType? = null
	
	override fun getValue(): String {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val expressionType: ParadoxDataType
		get() = _valueType ?: super.expressionType.also { _valueType = it }
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		super.subtreeChanged()
	}
}