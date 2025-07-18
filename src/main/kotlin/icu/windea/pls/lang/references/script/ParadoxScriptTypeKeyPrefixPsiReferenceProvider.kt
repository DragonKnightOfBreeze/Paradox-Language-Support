package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 解析定义键前缀引用。
 *
 * 直接在定义声明之前的，作为前缀的字符串，将其视为引用，并解析为对应的规则（类型规则中，属性`type_key_prefix`的值）。
 */
class ParadoxScriptTypeKeyPrefixPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxScriptString) return PsiReference.EMPTY_ARRAY
        if (!element.isBlockMember()) return PsiReference.EMPTY_ARRAY
        if (element.text.isParameterized()) return PsiReference.EMPTY_ARRAY //不应当带有参数
        val rangeInElement = getRangeInElement(element) ?: return PsiReference.EMPTY_ARRAY
        val nextProperty = findNextProperty(element) ?: return PsiReference.EMPTY_ARRAY //之后的兄弟节点必须是属性（跳过空白和注释）
        val typeConfig = nextProperty.definitionInfo?.typeConfig ?: return PsiReference.EMPTY_ARRAY //并且必须是定义
        if (typeConfig.typeKeyPrefix.let { it == null || !it.equals(element.value, ignoreCase = true) }) return PsiReference.EMPTY_ARRAY //前缀规则必须存在且一致
        val config = typeConfig.typeKeyPrefixConfig ?: return PsiReference.EMPTY_ARRAY
        val reference = ParadoxScriptTypeKeyPrefixPsiReference(element, rangeInElement, config)
        return arrayOf(reference)
    }

    private fun getRangeInElement(element: ParadoxScriptString): TextRange? {
        val text = element.text
        val range = TextRange.create(0, text.length).unquote(text)
        if (range.isEmpty) return null
        return range
    }

    private tailrec fun findNextProperty(element: PsiElement): ParadoxScriptProperty? {
        val nextSibling = element.nextSibling ?: return null
        if (nextSibling is PsiWhiteSpace || nextSibling is PsiComment) return findNextProperty(nextSibling)
        if (nextSibling !is ParadoxScriptProperty) return null
        return nextSibling
    }
}
