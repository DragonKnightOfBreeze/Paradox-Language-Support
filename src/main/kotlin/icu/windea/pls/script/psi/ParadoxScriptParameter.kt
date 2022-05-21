package icu.windea.pls.script.psi

import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import javax.swing.*

/**
 * 脚本参数。
 */
interface ParadoxScriptParameter : ParadoxScriptExpression {
	override fun getIcon(flags: Int): Icon {
		return PlsIcons.scriptParameterIcon
	}
	
	var name: String
		get() = parameterId.text
		set(value) {
			throw IncorrectOperationException() //TODO
		}
	
	val defaultValue: String? get() = defaultValueToken?.text
	
	override val valueType: ParadoxValueType get() = ParadoxValueType.NumberType
}