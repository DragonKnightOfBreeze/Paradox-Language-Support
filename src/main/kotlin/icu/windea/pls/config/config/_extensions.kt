package icu.windea.pls.config.config

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.createPointer
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.model.Occurrence

// region CwtMemberConfig Extensions

/**
 * 是否为“块规则”（存在子规则）。
 *
 * 仅基于是否存在子规则进行判断，并不代表语义上的“容器规则”。
 */
val <T : CwtMemberElement> CwtMemberConfig<T>.isBlock: Boolean
    get() = configs != null

// val CwtMemberConfig<*>.isRoot: Boolean
//    get() = when (this) {
//        is CwtPropertyConfig -> this.parentConfig == null
//        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
//    }

/**
 * 返回与当前规则关联的“成员规则”（属性或值）。
 *
 * - 对于属性规则，返回自身；
 * - 对于值规则，若存在 `propertyConfig` 则返回其属性规则，否则返回自身（孤立值规则）。
 */
val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when (this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

/**
 * 构建出现次数（`Occurrence`）。
 *
 * 基于规则选项中的基数配置（`cardinality`）生成出现次数区间，并结合 `cardinalityMinDefine`、`cardinalityMaxDefine`
 * 对应的 `define` 值进行覆盖，适用于 UI 展示与校验提示。
 *
 * 说明：参数通过内联描述体现——`contextElement` 与 `project` 共同用于解析 `define` 的当前值。
 */
fun <T : CwtMemberElement> CwtMemberConfig<T>.toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
    val cardinality = this.optionData { cardinality } ?: return Occurrence(0, null, null)
    val cardinalityMinDefine = this.optionData { cardinalityMinDefine }
    val cardinalityMaxDefine = this.optionData { cardinalityMaxDefine }
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

// endregion

/**
 * `CwtProperty` 的智能指针包装。
 *
 * 保留对属性值（`CwtValue`）的指针，便于跨线程/缓存安全地访问属性及其值。
 *
 * - `valuePointer`：指向属性值的智能指针（若存在）。
 */
class CwtPropertyPointer(
    private val delegate: SmartPsiElementPointer<CwtProperty>
) : SmartPsiElementPointer<CwtProperty> by delegate {
    val valuePointer: SmartPsiElementPointer<CwtValue>? = delegate.element?.propertyValue?.createPointer()
}
