package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig

/**
 * 可展开为一组候选项的规则的统一抽象。
 *
 * 说明：
 * - 从规则上下文解析得到一组作为上下文的规则时，以及后续得到一组匹配的规则时，会按需展开别名规则和单别名规则（内联为子规则或属性值规则）。
 *
 * @see CwtUnionConfig
 * @see CwtAliasConfig
 * @see CwtSingleAliasConfig
 */
interface CwtExpandableConfig<T : PsiElement> : CwtConfig<T>
