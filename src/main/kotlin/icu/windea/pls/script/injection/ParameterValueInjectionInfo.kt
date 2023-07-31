package icu.windea.pls.script.injection

import com.intellij.openapi.util.*
import icu.windea.pls.core.psi.*

class ParameterValueInjectionInfo(
    val rangeInsideHost: TextRange,
    parameterElementProvider: () -> ParadoxParameterElement? //这里必须使用懒加载，否则调用element.references时会导致SOF
) {
    val parameterElement by lazy { parameterElementProvider() }
}
