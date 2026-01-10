package icu.windea.pls.config.config

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.Occurrence

// region CwtMemberConfig Extensions

// val CwtMemberConfig<*>.isRoot: Boolean
//    get() = when (this) {
//        is CwtPropertyConfig -> this.parentConfig == null
//        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
//    }

/**
 * 如果当前成员规则对应属性的值，则返回所属的属性规则。否则返回自身。
 */
val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when (this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

/**
 * 构建子规则的出现次数（[Occurrence]）。
 *
 * 基于规则选项中的基数配置（`cardinality`）生成出现次数区间，并结合 `cardinalityMinDefine`、`cardinalityMaxDefine`
 * 对应的 `define` 值进行覆盖，适用于 UI 展示与校验提示。
 *
 * 说明：参数通过内联描述体现——[contextElement] 与 [project] 共同用于解析 `define` 的当前值。
 */
fun <T : CwtMember> CwtMemberConfig<T>.toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
    val cardinality = this.optionData.cardinality ?: return Occurrence(0, null, null)
    val cardinalityMinDefine = this.optionData.cardinalityMinDefine
    val cardinalityMaxDefine = this.optionData.cardinalityMaxDefine
    val occurrence = Occurrence(0, cardinality.min, cardinality.max, cardinality.relaxMin, cardinality.relaxMax)
    run {
        if (cardinalityMinDefine == null) return@run
        val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMinDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
        occurrence.min = defineValue
        occurrence.minDefine = cardinalityMinDefine
    }
    run {
        if (cardinalityMaxDefine == null) return@run
        val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMaxDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
        occurrence.max = defineValue
        occurrence.maxDefine = cardinalityMaxDefine
    }
    return occurrence
}


inline fun CwtMemberConfig<*>.processParent(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    var parent = this.parentConfig
    while (parent != null) {
        val result = processor(parent)
        if (!result) return false
        parent = parent.parentConfig
    }
    return true
}

fun CwtMemberConfig<*>.processDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    return doProcessDescendants(processor)
}

private fun CwtMemberConfig<*>.doProcessDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    processor(this).also { if (!it) return false }
    this.configs?.process { it.doProcessDescendants(processor) }?.also { if (!it) return false }
    return true
}

// endregion

// region CwtOptionMemberConfig Extensions

fun CwtOptionMemberConfig<*>.getOptionValues(): Set<String>? {
    val optionConfigs = optionConfigs ?: return null
    if (optionConfigs.isEmpty()) return emptySet()
    val result = FastSet<String>()
    optionConfigs.forEachFast f@{ optionConfig ->
        if (optionConfig !is CwtOptionValueConfig) return@f
        val v = optionConfig.stringValue ?: return@f
        result.add(v)
    }
    return result
}

fun CwtOptionMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
    return stringValue?.singleton?.set() ?: getOptionValues()
}

// endregion
