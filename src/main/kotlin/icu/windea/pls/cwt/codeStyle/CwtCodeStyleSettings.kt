package icu.windea.pls.cwt.codeStyle

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import icu.windea.pls.cwt.CwtLanguage

class CwtCodeStyleSettings(
    container: CodeStyleSettings
) : CustomCodeStyleSettings(CwtLanguage.id, container) {
    // 自定义配置项对应的字段（需要是@JvmField var）

    // spacing settings

    @JvmField
    var SPACE_AROUND_OPTION_SEPARATOR = true
    @JvmField
    var SPACE_AROUND_PROPERTY_SEPARATOR = true
    @JvmField
    var SPACE_WITHIN_BRACES = true

    // commenter settings

    @JvmField
    var OPTION_COMMENT_ADD_SPACE = true
    @JvmField
    var DOC_COMMENT_ADD_SPACE = true
}
