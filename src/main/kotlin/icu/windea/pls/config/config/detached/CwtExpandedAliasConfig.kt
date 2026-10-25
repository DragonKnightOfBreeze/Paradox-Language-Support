package icu.windea.pls.config.config.detached

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtExpandedConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.manipulation.CwtConfigExpansionService

/**
 * 展开后的别名规则。
 *
 * @property configExpression 这组候选项对应的数据表达式。
 * @property config 这组候选项对应的第一个规则。
 * @property configs 这组候选项对应的所有规则。
 *
 * @see CwtAliasConfig
 * @see CwtConfigExpansionService.expandAlias
 */
data class CwtExpandedAliasConfig(
    override val configExpression: CwtDataExpression,
    val config: CwtAliasConfig,
    val configs: List<CwtAliasConfig>,
) : CwtDetachedConfig, CwtExpandedConfig<PsiElement>
