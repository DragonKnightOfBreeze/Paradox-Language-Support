package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.settings.ChronicleInternalSettings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.constants.DefaultStrings
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.psi.PsiPresentableElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.transformAndKeepQuotes
import icu.windea.pls.core.truncate
import icu.windea.pls.cwt.CwtLanguage

@Suppress("unused")
object CwtPsiService {
    private val presentableTextLimit get() =  ChronicleInternalSettings.getInstance().presentableTextLimit

    fun getPresentableText(element: PsiPresentableElement): String {
        return when(element) {
            is CwtProperty -> {
                var keyElement: CwtPropertyKey? = null
                var separatorElement: PsiElement? = null
                var valueElement: CwtValue? = null
                element.forEachChild { e ->
                    when {
                        e is CwtPropertyKey -> keyElement = e
                        isPropertySeparator(e) -> separatorElement = e
                        e is CwtValue -> valueElement = e
                    }
                }
                buildString {
                    if (keyElement != null) append(getPresentableText(keyElement)) else append(DefaultStrings.unresolved)
                    append(" ")
                    append(separatorElement?.text ?: "=")
                    append(" ")
                    if (valueElement != null) append(getPresentableText(valueElement)) else append(DefaultStrings.unresolved)
                }
            }
            is CwtOption -> {
                var keyElement: CwtOptionKey? = null
                var valueElement: CwtValue? = null
                element.forEachChild { e ->
                    when {
                        e is CwtOptionKey -> keyElement = e
                        e is CwtValue -> valueElement = e
                    }
                }
                buildString {
                    if (keyElement != null) append(keyElement.text) else append(DefaultStrings.unresolved)
                    append(" = ")
                    if (valueElement != null) append(getPresentableText(valueElement)) else append(DefaultStrings.unresolved)
                }
            }
            is CwtStringExpressionElement -> element.text.transformAndKeepQuotes { it.truncate(presentableTextLimit) }
            is CwtExpressionElement -> element.value
            else -> element.text
        }
    }

    fun canAttachComment(element: PsiElement): Boolean {
        return element is CwtProperty || (element is CwtString && element.isDirectValue())
    }

    fun isLenientMemberContext(element: PsiElement): Boolean {
        return element is CwtMemberContext
    }

    fun isStrictMemberContext(element: PsiElement): Boolean {
        return element is CwtFile || element.elementType in CwtTokenSets.MEMBER_CONTEXT_TOKENS
    }

    fun isPropertySeparator(element: PsiElement): Boolean {
        return element.elementType in CwtTokenSets.PROPERTY_SEPARATOR_TOKENS
    }

    fun isBeforeValueLeftBoundEnd(element: CwtProperty, offset: Int): Boolean {
        val value = element.propertyValue?.castOrNull<PsiBoundElement>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(value, offset)
    }

    fun isBeforeBlockLeftBoundEnd(element: CwtProperty, offset: Int): Boolean {
        val block = element.propertyValue?.castOrNull<CwtBlock>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(block, offset)
    }

    fun getOwnedDocComments(element: PsiElement): List<PsiComment> {
        return PsiService.getOwnedComments(element) { it is CwtDocComment }
    }

    fun getDocCommentText(comments: List<PsiComment>): String? {
        // 如果某行注释至少存在4个前导的 `#`，则将注释文本视为 Markdown 文本
        return PsiService.getDocCommentText(comments) { it.startsWith("####") }
    }

    fun findStringExpressionElementFromStartOffset(file: PsiFile, offset: Int): CwtStringExpressionElement? {
        if (offset < 0) return null
        if (file.language !== CwtLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType in CwtTokenSets.STRING_EXPRESSION_TOKENS }
            ?.parentOfType<CwtStringExpressionElement>()
    }

    @Suppress("unused")
    fun findPropertyFromStartOffset(file: PsiFile, offset: Int): CwtProperty? {
        if (offset < 0) return null
        if (file.language !== CwtLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType == CwtElementTypes.PROPERTY_KEY_TOKEN }
            ?.parentOfType<CwtProperty>()
    }
}
