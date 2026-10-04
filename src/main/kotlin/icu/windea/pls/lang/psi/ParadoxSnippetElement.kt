package icu.windea.pls.lang.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.util.TemplateInfo

/**
 * 作为片段的 [com.intellij.psi.PsiElement]。
 *
 * 对应的文本实际上并非完整的（定义、本地化等目标的）引用文本，而是其去除特定的前后缀之后的形式。
 * 通过模板参数（[snippetTemplates]）可以解析得到完整的引用文本。
 *
 * @see icu.windea.pls.config.CwtDataTypes.DefinitionSnippet
 * @see icu.windea.pls.config.CwtDataTypes.LocalisationSnippet
 */
interface ParadoxSnippetElement : NavigatablePsiElement {
    val snippetTemplates: List<TemplateInfo<String>>
}
