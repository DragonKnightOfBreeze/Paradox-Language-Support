package icu.windea.pls.core.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.KeyWithDefaultValue
import com.intellij.psi.util.*
import icu.windea.pls.core.*

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