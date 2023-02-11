package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.psi.*

@Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED")
class SmartParadoxScriptPropertyKey : ParadoxScriptPropertyKeyImpl, ParadoxScriptPropertyKey {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptPropertyKeyStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _value: String? = null
	@Volatile private var _valueType: ParadoxDataType? = null
	
	override var value: String
		@get:JvmName("getValue")
		get() {
			return _value ?: super.value.also { _value = it }
		}
		set(value) { setValue(value) }
	
	override val type: ParadoxDataType
		get() = _valueType ?: super.type.also { _valueType = it }
	
	override fun subtreeChanged() {
		_value = null
		_valueType = null
		super.subtreeChanged()
	}
}
