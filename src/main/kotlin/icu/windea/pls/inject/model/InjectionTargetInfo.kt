package icu.windea.pls.inject.model

import icu.windea.pls.inject.CodeInjector

data class InjectionTargetInfo(
    val codeInjector: CodeInjector,
    val injectTargetName: String,
    val injectPluginId: String,
)
