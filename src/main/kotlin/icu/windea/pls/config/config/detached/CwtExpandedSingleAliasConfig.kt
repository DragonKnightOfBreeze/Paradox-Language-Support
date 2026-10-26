package icu.windea.pls.config.config.detached

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtExpandedConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.manipulation.CwtConfigExpansionService

/**
 * 展开后的作为候选项的单别名规则。
 *
 * @property configExpression 对应的数据表达式。
 * @property config 对应的唯一一个规则。
 *
 * @see CwtSingleAliasConfig
 * @see CwtConfigExpansionService.expandSingleAlias
 */
data class CwtExpandedSingleAliasConfig(
    override val configExpression: CwtDataExpression,
    val config: CwtSingleAliasConfig,
) : CwtDetachedConfig, CwtExpandedConfig<PsiElement>
