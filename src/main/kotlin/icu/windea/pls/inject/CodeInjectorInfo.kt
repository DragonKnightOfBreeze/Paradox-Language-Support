package icu.windea.pls.inject

import java.lang.reflect.*

data class CodeInjectorInfo(
    val codeInjector: CodeInjector,
    val injectTargetName: String,
    val injectMethods: Map<String, Method>,
    val injectMethodInfos: Map<String, InjectMethodInfo>
) 

