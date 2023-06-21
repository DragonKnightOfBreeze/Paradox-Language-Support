package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.core.collections.*

sealed interface CwtOptionMemberConfig<out T: PsiElement>: CwtConfig<T>, CwtValueAware, CwtOptionsAware

fun CwtOptionMemberConfig<*>.getOptionValue():String? = stringValue?.intern()

fun CwtOptionMemberConfig<*>.getOptionValues():Set<String>? = findOptionValues()?.mapNotNullTo(mutableSetOf()) { it.stringValue?.intern() }

fun CwtOptionMemberConfig<*>.getOptionValueOrValues():Set<String>? = getOptionValue()?.toSingletonSet() ?: getOptionValues()