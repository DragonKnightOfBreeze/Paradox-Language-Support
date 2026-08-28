package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.settings.ChronicleInternalSettings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.constants.DefaultStrings
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.processChild
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.psi.PsiPresentableElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.transformAndKeepQuotes
import icu.windea.pls.core.truncate
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.ParadoxScriptLanguage
import java.util.*
import java.util.function.IntUnaryOperator

@Suppress("unused")
object ParadoxScriptPsiService {
    private val presentableTextLimit get() = ChronicleInternalSettings.getInstance().presentableTextLimit

    fun getPresentableText(element: PsiPresentableElement): String {
        return when (element) {
            is ParadoxScriptProperty -> {
                var keyElement: ParadoxScriptPropertyKey? = null
                var separatorElement: PsiElement? = null
                var valueElement: ParadoxScriptValue? = null
                element.forEachChild { e ->
                    when {
                        e is ParadoxScriptPropertyKey -> keyElement = e
                        isPropertySeparator(e) -> separatorElement = e
                        e is ParadoxScriptValue -> valueElement = e
                    }
                }
                buildString {
                    if (keyElement != null) append(keyElement.presentableText) else append(DefaultStrings.unresolved)
                    if (separatorElement?.elementType != ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) append(" ")
                    append(separatorElement?.text ?: "=")
                    append(" ")
                    if (valueElement != null) append(valueElement.presentableText) else append(DefaultStrings.unresolved)
                }
            }
            is ParadoxScriptScriptedVariable -> {
                var nameElementElement: ParadoxScriptScriptedVariableName? = null
                var valueElement: ParadoxScriptValue? = null
                element.forEachChild { e ->
                    when {
                        e is ParadoxScriptScriptedVariableName -> nameElementElement = e
                        e is ParadoxScriptValue -> valueElement = e
                    }
                }
                buildString {
                    if (nameElementElement != null) append(nameElementElement.text) else append(DefaultStrings.unresolved)
                    append(" = ")
                    if (valueElement != null) append(valueElement.presentableText) else append(DefaultStrings.unresolved)
                }
            }
            is ParadoxScriptNormalParameter -> {
                element.text // use original text
            }
            is ParadoxScriptConditionalBlock -> {
                val expressionText = element.conditionalExpression?.presentableText
                ChronicleStrings.conditionalBlockFolder(expressionText.or.unresolved())
            }
            is ParadoxScriptConditionalExpression -> {
                buildString {
                    element.processChild {
                        when {
                            it is ParadoxScriptConditionalParameter -> {
                                append(it.name)
                                false
                            }
                            it.elementType == ParadoxScriptElementTypes.NOT_EQUAL_SIGN -> {
                                append("!")
                                true
                            }
                            else -> true
                        }
                    }
                }
            }
            is ParadoxScriptStringExpressionElement -> element.text.transformAndKeepQuotes { it.truncate(presentableTextLimit) }
            is ParadoxScriptExpressionElement -> element.value
            else -> element.text
        }
    }

    fun canAttachComment(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty || (element is ParadoxScriptString && element.isDirectValue())
    }

    fun isIdElement(element: PsiElement): Boolean {
        if (element.nextSibling.elementType in ParadoxScriptTokenSets.INTERPOLATION_TOKENS) return false
        if (element.prevSibling.elementType in ParadoxScriptTokenSets.INTERPOLATION_TOKENS) return false
        return true
    }

    fun isLenientMemberContext(element: PsiElement): Boolean {
        return element is ParadoxScriptMemberContext
    }

    fun isStrictMemberContext(element: PsiElement): Boolean {
        return element is ParadoxScriptFile || element.elementType in ParadoxScriptTokenSets.MEMBER_CONTEXT_TOKENS
    }

    fun isPropertySeparator(element: PsiElement): Boolean {
        return element.elementType in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS
    }

    fun isBeforeValueLeftBoundEnd(element: ParadoxScriptProperty, offset: Int): Boolean {
        val value = element.propertyValue?.castOrNull<PsiBoundElement>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(value, offset)
    }

    fun isBeforeBlockLeftBoundEnd(element: ParadoxScriptProperty, offset: Int): Boolean {
        val block = element.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(block, offset)
    }

    @Suppress("unused")
    fun findStringExpressionElementFromStartOffset(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        if (offset < 0) return null
        if (file.language !== ParadoxScriptLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType in ParadoxScriptTokenSets.STRING_EXPRESSION_TOKENS }
            ?.parentOfType<ParadoxScriptStringExpressionElement>()
    }

    fun findPropertyFromStartOffset(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        if (offset < 0) return null
        if (file.language !== ParadoxScriptLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType == ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN }
            ?.parentOfType<ParadoxScriptProperty>()
    }

    fun parseExpressionCharacters(chars: String, out: StringBuilder, sourceOffsets: IntArray?): Boolean {
        if (chars.none { c -> c == '\\' }) {
            if (sourceOffsets != null) Arrays.setAll(sourceOffsets, IntUnaryOperator.identity())
            out.append(chars)
            return true
        }

        val outOffset = out.length
        var index = 0
        while (index < chars.length) {
            val c = chars[index++]
            if (sourceOffsets != null) {
                sourceOffsets[out.length - outOffset] = index - 1
                sourceOffsets[out.length + 1 - outOffset] = index
            }
            if (c != '\\') {
                out.append(c)
                continue
            }
            if (index == chars.length) return false
            when (val c1 = chars[index++]) {
                '"' -> {
                    out.append('"')
                    if (sourceOffsets != null) {
                        sourceOffsets[out.length - outOffset] = index
                    }
                }
                '\\' -> {
                    out.append('\\')
                    if (sourceOffsets != null) {
                        sourceOffsets[out.length - outOffset] = index
                    }
                }
                else -> {
                    out.append('\\').append(c1)
                }
            }
        }
        return true
    }
}
