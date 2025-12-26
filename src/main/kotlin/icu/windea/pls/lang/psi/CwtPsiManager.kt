package icu.windea.pls.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.escapeXml
import icu.windea.pls.cwt.psi.CwtDocComment

object CwtPsiManager {
    fun getOwnedComments(element: PsiElement): List<PsiComment> {
        return PlsPsiManager.getOwnedComments(element) { it is CwtDocComment }
    }

    @Inferred
    fun getDocCommentText(comments: List<PsiComment>, lineSeparator: String = "\n"): String? {
        // NOTE 2.0.7+ 这里需要考虑如何推断是否需要换行

        // - 忽略所有前导的 '#'，然后再忽略所有首尾空白
        // - 如果某行注释以 '@' 开始，则输出时移除并直接输出为 HTML，否则，输出前进行转义
        // - 如果某行注释以 '\' 结束，则输出时不要在这里换行（并且，还会忽略所有末尾的 '\'）
        // - 如果某行注释以逗号（中文/英文）结束，则输出时不要在这里换行

        if (comments.isEmpty()) return null
        return buildString {
            for (comment in comments) {
                val line = comment.text.trimStart('#').trim()
                if (line.isEmpty()) continue
                val l = line.trimEnd('/').let { if (it.startsWith('@')) it.drop(1) else it.escapeXml() }
                append(l)
                if (line.last() in "/,，") continue
                append(lineSeparator)
            }
        }.trimEnd()
    }

}
