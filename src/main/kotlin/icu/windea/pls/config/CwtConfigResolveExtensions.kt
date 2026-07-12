@file:Suppress("unused")

package icu.windea.pls.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.emptyPointer

// region Config Predicates

/**
 * 判断两个规则对象是否指向同一 [PsiElement]。
 */
infix fun CwtConfig<*>?.isSamePointer(other: CwtConfig<*>?): Boolean {
    if (this == null || other == null) return false
    // NOTE 2.1.1 reference equals can be used here & empty pointers are never same
    return pointer === other.pointer && pointer !== emptyPointer<PsiElement>()
}

// endregion

// region Config Resolve Extensions

/**
 * 尝试解析当前规则指向的 [PsiElement]，并绑定当前规则。
 */
fun <T : PsiElement> CwtConfig<T>?.resolveElementWithConfig(): T? {
    val element = this?.pointer?.element ?: return null
    element.putUserData(CwtConfigManager.Keys.config, this)
    return element
}

/** 解析为被内联的规则，或者返回自身。 */
@Suppress("UNCHECKED_CAST")
fun <T : CwtConfig<*>> T.resolved(): T {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config ?: this
        else -> this
    } as T
}

/** 解析为被内联的规则，或者返回 `null`。 */
@Suppress("UNCHECKED_CAST")
fun <T : CwtConfig<*>> T.resolvedOrNull(): T? {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config
        else -> this
    } as? T
}

// endregion
