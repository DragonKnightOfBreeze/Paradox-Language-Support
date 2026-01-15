package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.FieldCache
import icu.windea.pls.inject.annotations.InjectionTarget

interface ParadoxCsvPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.csv.psi.ParadoxCsvColumn */
    @InjectionTarget("icu.windea.pls.csv.psi.impl.ParadoxCsvColumnImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getValue", cleanUp = "subtreeChanged")
    class Column : CodeInjectorBase()
}
