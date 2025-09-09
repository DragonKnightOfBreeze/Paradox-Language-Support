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

val <T : CwtMemberElement> CwtMemberConfig<T>.isBlock: Boolean
    get() = configs != null

// val CwtMemberConfig<*>.isRoot: Boolean
//    get() = when (this) {
//        is CwtPropertyConfig -> this.parentConfig == null
//        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
//    }

val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when (this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

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

class CwtPropertyPointer(
    private val delegate: SmartPsiElementPointer<CwtProperty>
) : SmartPsiElementPointer<CwtProperty> by delegate {
    val valuePointer: SmartPsiElementPointer<CwtValue>? = delegate.element?.propertyValue?.createPointer()
}
