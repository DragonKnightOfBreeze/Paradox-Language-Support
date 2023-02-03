package icu.windea.pls.cwt.codeStyle

import com.intellij.psi.codeStyle.*
import icu.windea.pls.cwt.*

@Suppress("unused")
class CwtCodeStyleSettings(
	container: CodeStyleSettings
) : CustomCodeStyleSettings(CwtLanguage.id, container) {
	//这里需要声明自定义设置项对应的var，名字需要对应，需要添加@JvmField
	@JvmField var SPACE_AROUND_OPTION_SEPARATOR = true
	@JvmField var SPACE_AROUND_PROPERTY_SEPARATOR = true
	@JvmField var SPACE_WITHIN_BRACES = true
}