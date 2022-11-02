package icu.windea.pls.cwt.codeStyle

import com.intellij.psi.codeStyle.*
import icu.windea.pls.*
import icu.windea.pls.core.*

@Suppress("unused")
class CwtCodeStyleSettings(
	container: CodeStyleSettings
): CustomCodeStyleSettings(cwtId,container){
	//这里需要声明自定义设置项对应的var，名字需要对应，需要添加@JvmField
	@JvmField var SPACE_AROUND_OPTION_SEPARATOR = true
	@JvmField var SPACE_AROUND_PROPERTY_SEPARATOR = true
	@JvmField var SPACE_WITHIN_BRACES = true
}