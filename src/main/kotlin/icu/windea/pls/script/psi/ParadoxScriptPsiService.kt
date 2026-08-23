package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.settings.ChronicleInternalSettings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.script.ParadoxScriptLanguage

@Suppress("unused")
object ParadoxScriptPsiService {
    fun getPresentableText(element: ParadoxScriptProperty): String {
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
        return buildString {
            if (keyElement != null) append(getPresentableText(keyElement)) else append(FallbackStrings.unresolved)
            if (separatorElement?.elementType != ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) append(" ")
            append(separatorElement?.text ?: "=")
            append(" ")
            if (valueElement != null) append(getPresentableText(valueElement)) else append(FallbackStrings.unresolved)
        }
    }

    fun getPresentableText(element: ParadoxScriptExpressionElement): String {
        if (element is ParadoxScriptStringExpressionElement) {
            val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
            return element.text.truncateAndKeepQuotes(limit)
        }
        return element.value
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
}
