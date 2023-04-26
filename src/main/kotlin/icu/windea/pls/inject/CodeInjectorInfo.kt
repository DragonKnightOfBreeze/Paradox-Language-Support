package icu.windea.pls.inject

import java.lang.reflect.*

data class CodeInjectorInfo(
    val codeInjector: CodeInjector,
    val injectTargetName: String,
    val injectPluginId: String,
    val injectMethods: Map<String, Method>,
    val injectMethodInfos: Map<String, InjectMethodInfo>
)

data class InjectMethodInfo(
    val pointer: Inject.Pointer,
    val hasReceiver: Boolean
)