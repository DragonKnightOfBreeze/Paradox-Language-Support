@file:Suppress("unused")

package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.emptyPointer

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

// region Process Extensions

/** @see CwtConfigManipulationService.expandEnumCandidates */
fun CwtEnumConfig.expandEnumCandidates(processor: (CwtValueConfig) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandEnumCandidates(this, processor)
}

/** @see CwtConfigManipulationService.expandUnionCandidates */
fun CwtUnionConfig.expandUnionCandidates(processor: (CwtValueConfig) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandUnionCandidates(this, processor)
}

/** @see CwtConfigManipulationService.expandBySubtypeExpression */
fun CwtMemberConfig<*>.expandBySubtypeExpression(processor: (CwtMemberConfig<*>, String) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandBySubtypeExpression(this, processor)
}

/** @see CwtConfigManipulationService.expandConfigExpression */
fun CwtConfig<*>.expandConfigExpression(processor: (CwtDataExpression) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandConfigExpression(this, processor)
}

/** @see CwtConfigManipulationService.expandConfigExpression */
fun Collection<CwtConfig<*>>.expandConfigExpression(processor: (CwtDataExpression) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandConfigExpression(this, processor)
}

/** @see CwtConfigManipulationService.expandKeyExpression */
fun CwtPropertyConfig.expandKeyExpression(processor: (CwtDataExpression) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandKeyExpression(this, processor)
}

/** @see CwtConfigManipulationService.expandKeyExpression */
fun Collection<CwtPropertyConfig>.expandKeyExpression(processor: (CwtDataExpression) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandKeyExpression(this, processor)
}

/** @see CwtConfigManipulationService.expandValueExpression */
fun CwtMemberConfig<*>.expandValueExpression(processor: (CwtDataExpression) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandValueExpression(this, processor)
}

/** @see CwtConfigManipulationService.expandValueExpression */
fun Collection<CwtMemberConfig<*>>.expandValueExpression(processor: (CwtDataExpression) -> Boolean): Boolean {
    return CwtConfigManipulationService.expandValueExpression(this, processor)
}

// endregion

// region Sequence Builders

fun CwtMemberContainerConfig<*>.members(): Sequence<CwtMemberConfig<*>> {
    return configs?.orNull()?.asSequence().orEmpty()
}

fun CwtMemberContainerConfig<*>.properties(): Sequence<CwtPropertyConfig> {
    return properties?.orNull()?.asSequence().orEmpty()
}

fun CwtMemberContainerConfig<*>.values(): Sequence<CwtValueConfig> {
    return values?.orNull()?.asSequence().orEmpty()
}

fun CwtMemberConfig<*>.parents(withSelf: Boolean = false): Sequence<CwtMemberConfig<*>> {
    val current = if (withSelf) this else this.parentConfig
    return generateSequence(current) { it.parentConfig }
}

// endregion
