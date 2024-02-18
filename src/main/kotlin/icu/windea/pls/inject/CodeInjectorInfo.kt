package icu.windea.pls.inject

import java.lang.reflect.*

data class CodeInjectorInfo(
    val codeInjector: CodeInjector,
    val injectTargetName: String,
    val injectPluginId: String,
    val injectMethodInfos: Map<String, MethodInfo>
) {
    data class MethodInfo(
        val method: Method,
        val pointer: InjectMethod.Pointer,
        val hasReceiver: Boolean,
        val hasReturnValue: Boolean,
        val static: Boolean
    )
}
