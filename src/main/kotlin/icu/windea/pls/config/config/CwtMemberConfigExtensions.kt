package icu.windea.pls.config.config

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

val <T : CwtMemberElement> CwtMemberConfig<T>.isBlock: Boolean
    get() = configs != null

val CwtMemberConfig<*>.isRoot: Boolean
    get() = when(this) {
        is CwtPropertyConfig -> this.parentConfig == null
        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
    }

val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when(this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

val CwtValueConfig.isTagConfig: Boolean
    get() = findOptionValue("tag") != null

fun <T : CwtMemberElement> CwtMemberConfig<T>.toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
    val cardinality = this.cardinality ?: return Occurrence(0, null, null, false)
    val cardinalityMinDefine = this.cardinalityMinDefine
    val cardinalityMaxDefine = this.cardinalityMaxDefine
    val occurrence = Occurrence(0, cardinality.min, cardinality.max, cardinality.relaxMin)
    if(cardinalityMinDefine != null) {
        val defineValue = ParadoxDefineHandler.getDefineValue(contextElement, project, cardinalityMinDefine, Int::class.java)
        if(defineValue != null) {
            occurrence.min = defineValue
            occurrence.minDefine = cardinalityMinDefine
        }
    }
    if(cardinalityMaxDefine != null) {
        val defineValue = ParadoxDefineHandler.getDefineValue(contextElement, project, cardinalityMaxDefine, Int::class.java)
        if(defineValue != null) {
            occurrence.max = defineValue
            occurrence.maxDefine = cardinalityMaxDefine
        }
    }
    return occurrence
}