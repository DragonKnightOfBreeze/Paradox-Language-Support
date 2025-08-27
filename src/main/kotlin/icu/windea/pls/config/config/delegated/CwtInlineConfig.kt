@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtInlineConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String

    fun inline(): CwtPropertyConfig

    companion object {
        fun resolve(config: CwtPropertyConfig): CwtInlineConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtInlineConfig? {
    val key = config.key
    val name = key.removeSurroundingOrNull("inline[", "]")?.orNull()?.intern() ?: return null
    return CwtInlineConfigImpl(config, name)
}

private class CwtInlineConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtInlineConfig {
    override fun inline(): CwtPropertyConfig {
        val other = this.config
        val inlined = other.copy(
            key = name,
            configs = CwtConfigManipulator.deepCopyConfigs(other)
        )
        inlined.configs?.forEach { it.parentConfig = inlined }
        inlined.inlineConfig = this
        return inlined
    }

    override fun toString(): String {
        return "CwtInlineConfigImpl(name='$name')"
    }
}
