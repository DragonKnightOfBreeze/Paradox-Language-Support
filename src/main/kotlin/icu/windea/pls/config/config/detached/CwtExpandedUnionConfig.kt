package icu.windea.pls.config.config.detached

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtExpandedConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.manipulation.CwtConfigExpansionService

/**
 * 展开后的并集规则。
 *
 * @property configExpression 这组候选项对应的数据表达式。
 * @property config 这组候选项对应的第一个规则。
 * @property config 这组候选项对应的所有规则。
 *
 * @see CwtUnionConfig
 * @see CwtConfigExpansionService.expandUnion
 */
data class CwtExpandedUnionConfig(
    override val configExpression: CwtDataExpression,
    val config: CwtValueConfig,
    val configs: List<CwtValueConfig>,
) : CwtDetachedConfig, CwtExpandedConfig<PsiElement>
