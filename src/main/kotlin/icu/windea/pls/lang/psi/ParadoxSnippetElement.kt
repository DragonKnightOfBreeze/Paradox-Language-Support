package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.core.util.UnaryTemplateInfo

/**
 * 作为片段的 [PsiElement]。
 *
 * 对应的文本实际上并非完整的（定义、本地化等目标的）引用文本，而是其去除特定的前后缀之后的形式。
 * 通过模板参数（[snippetTemplates]）可以解析得到完整的引用文本。
 *
 * @see CwtDataTypeSets.Snippet
 * @since 3.0.4
 */
interface ParadoxSnippetElement : NavigatablePsiElement {
    val snippetTemplates: List<UnaryTemplateInfo>
}
