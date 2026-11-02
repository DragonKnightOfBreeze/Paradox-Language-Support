package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.script.ParadoxScriptLanguage

@Suppress("unused")
object ParadoxScriptPsiService {
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
        return element.elementType in ParadoxScriptTokenSets.PROPERTY_SEPARATORS
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

    fun findBlockFromSelfOrBraces(element: PsiElement): ParadoxScriptBlock? {
        if (element is ParadoxScriptBlock) return element
        if (element.elementType in ParadoxScriptTokenSets.BLOCK_BRACES) return element.parent?.castOrNull()
        return null
    }

    fun findConditionalBlockFromSelfOrBrackets(element: PsiElement): ParadoxScriptConditionalBlock? {
        if (element is ParadoxScriptConditionalBlock) return element
        if (element.elementType in ParadoxScriptTokenSets.CONDITIONAL_BLOCK_BRACKETS) return element.parent?.castOrNull()
        return null
    }

    fun findInlineMathFromSelfOrBrackets(element: PsiElement): ParadoxScriptInlineMath? {
        if (element is ParadoxScriptInlineMath) return element
        if (element.elementType in ParadoxScriptTokenSets.INLINE_MATH_BRACKETS) return element.parent?.castOrNull()
        return null
    }

    fun findStartElementToExtract(element: PsiElement): PsiElement? {
        val elements = when (element) {
            is ParadoxScriptBlock, is ParadoxScriptConditionalBlock -> PsiService.collectBetweenLenientBounds(element, forward = true)
            is ParadoxScriptFile -> element.block?.children(forward = true)
            else -> element.children(forward = true)
        }
        return elements?.find { it.elementType != TokenType.WHITE_SPACE }
    }

    fun findEndElementToExtract(element: PsiElement): PsiElement? {
        // for extract/unwrap/inline operations
        val elements = when (element) {
            is ParadoxScriptBlock, is ParadoxScriptConditionalBlock -> PsiService.collectBetweenLenientBounds(element, forward = false)
            is ParadoxScriptFile -> element.block?.children(forward = false)
            else -> element.children(forward = false)
        }
        return elements?.find { it.elementType != TokenType.WHITE_SPACE }
    }
}
