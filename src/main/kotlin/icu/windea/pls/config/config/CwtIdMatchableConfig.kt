package icu.windea.pls.config.config

import com.intellij.psi.PsiElement

/**
 * 可匹配 ID 的规则的统一抽象。ID 可能不唯一或者有多种格式。
 */
interface CwtIdMatchableConfig<T: PsiElement>: CwtConfig<T>
