package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupDataHolder
import icu.windea.pls.config.match.CwtConfigMatchService

/**
 * 可匹配 ID 的规则的统一抽象。
 *
 * 说明：
 * - 同种规则的 ID 可以不唯一。
 * - 某些规则的 ID 可以忽略大小写（参见：[CwtConfigGroupDataHolder]）。
 * - 某些规则的 ID 可以有多种格式。例如，对于别名规则（[CwtAliasConfig]）可以是 `trigger`，也可以是 `trigger:if`。
 *
 * @see CwtConfigMatchService.processMatchedConfigsById
 */
interface CwtIdMatchableConfig<T : PsiElement> : CwtConfig<T>
