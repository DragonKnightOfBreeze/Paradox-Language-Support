package icu.windea.pls.lang.injection

import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.model.injection.ParadoxParameterValueInjectionInfo

object ParadoxLanguageInjectionKeys : KeyRegistry() {
    val parameterValueInjectionInfos by registerKey<List<ParadoxParameterValueInjectionInfo>>(this)
}
