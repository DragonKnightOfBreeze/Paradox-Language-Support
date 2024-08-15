package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*

interface CwtConfig<out T : PsiElement> {
    val pointer: SmartPsiElementPointer<out T>
    val configGroup: CwtConfigGroup
    
    val expression: CwtDataExpression? get() = null
}

/**
 * 解析为被内联的CWT规则，或者返回自身。
 */
@Suppress("UNCHECKED_CAST")
fun <T: CwtConfig<*>> T.resolved(): T {
    return when {
        this is CwtMemberConfig<*> -> inlineableConfig?.config as? T ?: this
        else -> this
    }
}

/**
 * 解析为被内联的规则，或者返回null。
 */
@Suppress("UNCHECKED_CAST")
fun <T: CwtConfig<*>> T.resolvedOrNull(): T? {
    return when {
        this is CwtMemberConfig<*> -> inlineableConfig?.config as? T
        else -> this
    }
}
