package icu.windea.pls.core.codeInsight

import com.intellij.codeInsight.lookup.LookupElement

/**
 * 组合的补全项。
 *
 * @property element 主补全项
 * @property extraElements 额外的补全项（用于一起插入或展示）
 */
class CompositeLookupElement(
    val element: LookupElement,
    val extraElements: List<LookupElement> = emptyList()
)
