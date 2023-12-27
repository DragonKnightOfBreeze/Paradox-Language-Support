package icu.windea.pls.script.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import org.intellij.lang.annotations.*

/**
 * 用于兼容语言注入功能 - 启用编辑代码碎片的意向操作。
 */
class ParadoxScriptStringExpressionManipulator: AbstractElementManipulator<ParadoxScriptStringExpressionElement>() {
    override fun handleContentChange(element: ParadoxScriptStringExpressionElement, range: TextRange, newContent: String): ParadoxScriptStringExpressionElement {
        return element.setValue(range.replace(element.text, newContent))
    }
}