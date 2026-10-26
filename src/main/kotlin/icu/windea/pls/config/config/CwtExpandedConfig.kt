package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtDataExpression

/**
 * 展开后的作为候选项的规则的统一抽象。
 *
 * 此类规则可能对应多个来自规则文件的规则，例如多个别名规则。
 *
 * @see CwtExpandableConfig
 */
interface CwtExpandedConfig<T : PsiElement> : CwtConfig<T> {
    override val configExpression: CwtDataExpression
}
