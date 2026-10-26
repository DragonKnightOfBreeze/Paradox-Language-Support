package icu.windea.pls.config.config.detached

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtExpandedConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.manipulation.CwtConfigExpansionService

/**
 * 展开后的作为候选项的并集规则。
 *
 * @property configExpression 对应的数据表达式。
 * @property config 对应的规则。
 *
 * @see CwtUnionConfig
 * @see CwtConfigExpansionService.expandUnion
 */
data class CwtExpandedUnionConfig(
    override val configExpression: CwtDataExpression,
    val config: CwtValueConfig,
) : CwtDetachedConfig, CwtExpandedConfig<PsiElement>
