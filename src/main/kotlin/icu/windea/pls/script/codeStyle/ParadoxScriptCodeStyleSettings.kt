package icu.windea.pls.script.codeStyle

import com.intellij.psi.codeStyle.*
import icu.windea.pls.*

@Suppress("unused")
class ParadoxScriptCodeStyleSettings(
	container: CodeStyleSettings
): CustomCodeStyleSettings(paradoxScriptId,container){
	//这里需要声明自定义设置项对应的var，名字需要对应，需要添加@JvmField
	@JvmField var SPACE_AROUND_VARIABLE_SEPARATOR = true
	@JvmField var SPACE_AROUND_PROPERTY_SEPARATOR = true
	@JvmField var SPACE_AROUND_INLINE_MATH_OPERATOR = true
	@JvmField var SPACE_WITHIN_BRACES = true
	@JvmField var SPACE_WITHIN_INLINE_MATH_BRACKETS = true
}
