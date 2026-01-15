package icu.windea.pls.inject.injectors

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.OptimizedField
import icu.windea.pls.inject.annotations.InjectionTarget
import it.unimi.dsi.fastutil.objects.ObjectArraySet

interface OptimizedFieldCodeInjectors {
    // 用于优化内存

    /** @see com.intellij.codeInsight.hints.presentation.BasePresentation */
    @InjectionTarget("com.intellij.codeInsight.hints.presentation.BasePresentation")
    @OptimizedField("listeners", MutableSet::class, ObjectArraySet::class)
    class BasePresentation : CodeInjectorBase()
}
