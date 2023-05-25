package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED")
class SmartParadoxScriptPropertyKey : ParadoxScriptPropertyKeyImpl, ParadoxScriptPropertyKey {
	constructor(node: ASTNode) : super(node)
	
	@Volatile private var _value: String? = null
	@Volatile private var _type: ParadoxType? = null
	@Volatile private var _text: String? = null
	
	override var value: String
		@get:JvmName("getValue")
		get() {
			return _value ?: super.value.also { _value = it }
		}
		set(value) { setValue(value) }
	
	override val type: ParadoxType
		get() = _type ?: super.type.also { _type = it }
	
	override fun getText(): String {
		return _text ?: super.getText().also { _text = it }
	}
	
	override fun subtreeChanged() {
		_value = null
		_type = null
		_text = null
		super.subtreeChanged()
	}
}
