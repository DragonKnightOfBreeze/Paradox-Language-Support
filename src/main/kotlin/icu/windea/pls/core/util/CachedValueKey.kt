package icu.windea.pls.core.util

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

class CachedValueKey<T>(
    val name: String,
    private val trackValue: Boolean,
    private val project: Project,
    private val provider: CachedValueProvider<T>
): KeyWithDefaultValue<CachedValue<T>>(name) {
    override fun getDefaultValue(): CachedValue<T> {
        return CachedValuesManager.getManager(project).createCachedValue(provider, trackValue)
    }
}

inline fun <T> createCachedValueKey(
    name: String,
    trackValue: Boolean = false,
    project: Project = getDefaultProject(),
    provider: CachedValueProvider<T>
) = CachedValueKey(name, trackValue, project, provider)