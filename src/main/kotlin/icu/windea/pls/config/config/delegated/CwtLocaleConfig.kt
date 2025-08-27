@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.PlsDocBundle
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.ParadoxLocaleManager

interface CwtLocaleConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val id: String
    val codes: List<String>
    val text: String

    val shortId: String get() = id.removePrefix("l_")
    val idWithText: String get() = if(text.isEmpty()) id else "$id ($text)"

    companion object {
        val AUTO: CwtLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO)
        val AUTO_OS: CwtLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO_OS)
        val FALLBACK: CwtLocaleConfig = FallbackCwtLocaleConfig(ParadoxLocaleManager.ID_FALLBACK)

        fun resolve(config: CwtPropertyConfig): CwtLocaleConfig = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtLocaleConfig {
    val id = config.key
    val codes = config.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }.orEmpty()
    return CwtLocaleConfigImpl(config, id, codes)
}

private class CwtLocaleConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val codes: List<String>
) : UserDataHolderBase(), CwtLocaleConfig {
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocaleConfig && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "CwtLocaleConfig(id='$id')"
    }
}

private class AutoCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocaleConfig && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "AutoCwtLocaleConfig(id='$id')"
    }
}

private class FallbackCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocaleConfig && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "FallbackCwtLocaleConfig(id='$id')"
    }
}
