package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectFieldCache
import icu.windea.pls.inject.annotations.InjectTarget

interface ParadoxCsvPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.csv.psi.ParadoxCsvColumn */
    @InjectTarget("icu.windea.pls.csv.psi.impl.ParadoxCsvColumnImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getValue", cleanUp = "subtreeChanged")
    class Column : CodeInjectorBase()
}
