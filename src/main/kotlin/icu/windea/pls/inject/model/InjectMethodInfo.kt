package icu.windea.pls.inject.model

import icu.windea.pls.inject.annotations.InjectMethod
import java.lang.reflect.Method

data class InjectMethodInfo(
    val method: Method,
    val name: String,
    val pointer: InjectMethod.Pointer,
    val static: Boolean,
    val hasReceiver: Boolean,
    val hasReturnValue: Boolean
)
