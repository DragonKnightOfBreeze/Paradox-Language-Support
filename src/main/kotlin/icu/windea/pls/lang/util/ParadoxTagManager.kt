package icu.windea.pls.lang.util

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.resolveElementWithConfig
import icu.windea.pls.lang.references.script.ParadoxScriptTagAwarePsiReference
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isBlockValue

object ParadoxTagManager {
    fun getTagType(element: ParadoxScriptValue): ParadoxTagType? {
        if (element !is ParadoxScriptString) return null
        if (!element.isBlockValue()) return null
        val references = element.references
        for (reference in references) {
            if (reference is ParadoxScriptTagAwarePsiReference) return reference.tagConfig?.tagType
        }
        return null
    }

    fun processConfigs(configs: List<CwtMemberConfig<*>>) {
        for (config in configs) {
            if (config is CwtValueConfig && config.tagType != null) {
                config.resolveElementWithConfig()
            }
        }
    }
}
