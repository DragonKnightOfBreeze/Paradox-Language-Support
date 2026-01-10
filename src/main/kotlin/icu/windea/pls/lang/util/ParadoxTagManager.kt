package icu.windea.pls.lang.util

import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.tagType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptTagAwarePsiReference
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember

object ParadoxTagManager {
    fun getTagType(element: ParadoxScriptValue): ParadoxTagType? {
        if (element !is ParadoxScriptString) return null
        if (!element.isBlockMember()) return null
        val references = element.references
        element.run {
            val tagReference = references.firstNotNullOfOrNull { it.castOrNull<ParadoxScriptTagAwarePsiReference>() }
            if (tagReference == null) return@run
            return tagReference.config.tagType
        }
        element.run {
            val expressionReference = references.firstNotNullOfOrNull { it.castOrNull<ParadoxScriptExpressionPsiReference>() }
            if (expressionReference == null) return@run
            return expressionReference.config.castOrNull<CwtValueConfig>()?.tagType
        }
        return null
    }
}
