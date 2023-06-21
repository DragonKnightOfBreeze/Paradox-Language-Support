package icu.windea.pls.lang.config

import com.intellij.psi.*

/**
 * 用于基于通用的逻辑获取脚本表达式所在的CWT规则上下文。
 *
 * @see ParadoxConfigContext
 */
interface ParadoxConfigContextProvider {
    fun getContext(contextElement: PsiElement): ParadoxConfigContext?
}
