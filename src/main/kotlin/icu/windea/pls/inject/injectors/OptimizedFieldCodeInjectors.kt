package icu.windea.pls.inject.injectors

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectOptimizedField
import icu.windea.pls.inject.annotations.InjectTarget
import it.unimi.dsi.fastutil.objects.ObjectArraySet

interface OptimizedFieldCodeInjectors {
    // 用于优化内存

    /** @see com.intellij.codeInsight.hints.presentation.BasePresentation */
    @InjectTarget("com.intellij.codeInsight.hints.presentation.BasePresentation")
    @InjectOptimizedField("listeners", MutableSet::class, ObjectArraySet::class)
    class BasePresentation : CodeInjectorBase()
}
