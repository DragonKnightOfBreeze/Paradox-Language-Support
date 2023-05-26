package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.core.collections.*

sealed interface CwtOptionMemberConfig<out T: PsiElement>: CwtConfig<T>, CwtValueAware, CwtOptionsAware

fun CwtOptionMemberConfig<*>.getOptionValue():String? = stringValue

fun CwtOptionMemberConfig<*>.getOptionValues():Set<String>? = findOptionValues()?.mapNotNullTo(mutableSetOf()) { it.stringValue }

fun CwtOptionMemberConfig<*>.getOptionValueOrValues():Set<String>? = getOptionValue()?.toSingletonSet() ?: getOptionValues()