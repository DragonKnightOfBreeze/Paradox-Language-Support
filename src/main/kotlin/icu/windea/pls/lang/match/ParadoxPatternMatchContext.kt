package icu.windea.pls.lang.match

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.ep.match.ParadoxPatternMatcher
import icu.windea.pls.model.ParadoxGameType

/**
 * 模式匹配的上下文。
 *
 * 要匹配的文本以及匹配时是否忽略大小写等选项并未保存在此上下文对象中。
 *
 * @property element 上下文 PSI 元素。
 * @property configExpression 可作为模式来源的规则表达式。
 * @property configGroup 规则分组。
 *
 * @see ParadoxPatternMatcher
 */
data class ParadoxPatternMatchContext(
    val element: PsiElement,
    val configExpression: CwtDataExpression,
    val configGroup: CwtConfigGroup,
    val options: ParadoxMatchOptions? = null,
) {
    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
}
