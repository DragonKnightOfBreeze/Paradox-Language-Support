package icu.windea.pls.inject

import icu.windea.pls.inject.annotations.*
import java.lang.reflect.*

data class CodeInjectorInfo(
    val codeInjector: CodeInjector,
    val injectTargetName: String,
    val injectPluginId: String,
    val injectMethods: Map<String, Method>,
    val injectMethodInfos: Map<String, MethodInfo>
) {
    data class MethodInfo(
        val pointer: InjectMethod.Pointer,
        val hasReceiver: Boolean
    )
}
