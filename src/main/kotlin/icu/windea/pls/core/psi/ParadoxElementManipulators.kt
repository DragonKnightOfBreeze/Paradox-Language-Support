package icu.windea.pls.core.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*

//这些 ElementManipulator 用于：
//* 兼容语言注入功能 - 启用编辑代码碎片的意向操作
//* 委托实现 com.intellij.psi.PsiLanguageInjectionHost.updateText

class ParadoxScriptPropertyKeyExpressionManipulator: AbstractElementManipulator<ParadoxScriptPropertyKey>() {
    override fun handleContentChange(element: ParadoxScriptPropertyKey, range: TextRange, newContent: String): ParadoxScriptPropertyKey {
        val text = element.text
        val quoted = text.isLeftQuoted() || text.isRightQuoted()
        val newText = range.replace(text, newContent).unquote()
            .let { buildString { ParadoxEscapeManager.escapeScriptExpression(it, this) } }
            .quoteIfNecessary(or = quoted)
        val newElement = ParadoxScriptElementFactory.createPropertyKeyFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class ParadoxScriptStringManipulator: AbstractElementManipulator<ParadoxScriptString>() {
    override fun handleContentChange(element: ParadoxScriptString, range: TextRange, newContent: String): ParadoxScriptString {
        val text = element.text
        val quoted = text.isLeftQuoted() || text.isRightQuoted()
        val newText = range.replace(text, newContent).unquote()
            .let { buildString { ParadoxEscapeManager.escapeScriptExpression(it, this) } }
            .quoteIfNecessary(or = quoted)
        val newElement = ParadoxScriptElementFactory.createStringFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class ParadoxScriptParameterManipulator: AbstractElementManipulator<ParadoxScriptParameter>() {
    override fun handleContentChange(element: ParadoxScriptParameter, range: TextRange, newContent: String): ParadoxScriptParameter {
        val newText = range.replace(element.text, newContent)
            .let { buildString { ParadoxEscapeManager.escapeScriptExpression(it, this) } }
        val newElement = ParadoxScriptElementFactory.createParameterFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}

class ParadoxScriptInlineMathParameterManipulator: AbstractElementManipulator<ParadoxScriptInlineMathParameter>() {
    override fun handleContentChange(element: ParadoxScriptInlineMathParameter, range: TextRange, newContent: String): ParadoxScriptInlineMathParameter {
        val newText = range.replace(element.text, newContent)
            .let { buildString { ParadoxEscapeManager.escapeScriptExpression(it, this) } }
        val newElement = ParadoxScriptElementFactory.createInlineMathParameterFromText(element.project, newText)
        return element.replace(newElement).cast()
    }
}