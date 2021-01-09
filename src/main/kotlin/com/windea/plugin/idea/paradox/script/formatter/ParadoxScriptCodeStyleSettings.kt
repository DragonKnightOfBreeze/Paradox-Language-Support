@file:Suppress("PropertyName")

package com.windea.plugin.idea.paradox.script.formatter

import com.intellij.psi.codeStyle.*
import com.windea.plugin.idea.paradox.*

class ParadoxScriptCodeStyleSettings(
	container: CodeStyleSettings
): CustomCodeStyleSettings(paradoxScriptNamePc,container){
	//这里需要声明自定义设置项对应的var，名字需要对应，需要添加@JvmField
	@JvmField var SPACE_WITHIN_BRACES = true
	@JvmField var SPACE_AROUND_SEPARATOR = true

	//以下相当于自定义设置项的KEY
	enum class Option {
		SPACE_WITHIN_BRACES,
		SPACE_AROUND_SEPARATOR
	}
}
