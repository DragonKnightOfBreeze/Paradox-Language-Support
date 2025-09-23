package icu.windea.pls.lang.search.target

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.TextOccurenceProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.util.Processor
import icu.windea.pls.core.splitByBlank

/**
 * 基于本地化文本片段的目标（封装变量/定义/本地化）查询器 - 子字符串搜索。
 */
class ParadoxSubstringTextBasedTargetSearcher : ParadoxTextBasedTargetSearcher() {
    override fun process(context: Context, consumer: Processor<in PsiElement>) {
        val input = context.queryParameters.text.trim()
        if (input.isEmpty()) return // 预先过滤
        if (input.length >= MAX_INPUT_LENGTH) return // 排除输入文本过长的情况
        val snippets = getSnippets(input)
        if (snippets.isEmpty()) return
        for (snippet in snippets) {
            if (acceptSnippet(snippet)) {
                if (!processText(snippet, context, consumer)) return
            }
        }
    }

    private fun getSnippets(text: String): List<String> {
        // 按空白分隔
        return text.splitByBlank()
    }

    private fun acceptSnippet(snippet: String): Boolean {
        // 片段长度 >= MAX_SNIPPET_LENGTH
        return snippet.length >= MAX_SNIPPET_LENGTH
    }

    override fun processText(text: String, context: Context, consumer: Processor<in PsiElement>): Boolean {
        // 使用 PsiSearchHelper.processElementsWithWord 搜索出现了文本片段的节点 PSI
        // 查询上下文设置为 UsageSearchContext.IN_STRINGS
        // 参见：icu.windea.pls.localisation.editor.ParadoxLocalisationWordScanner

        val processor = TextOccurenceProcessor { element, _ ->
            ProgressManager.checkCanceled()
            processLeafElement(element, context, consumer)
            true
        }
        val scope = context.queryParameters.restrictedScope
        val searchContext = UsageSearchContext.IN_STRINGS
        return context.searchHelper.processElementsWithWord(processor, scope, text, searchContext, false)
    }

    companion object {
        private const val MAX_SNIPPET_LENGTH = 2
        private const val MAX_INPUT_LENGTH = 40
    }
}

