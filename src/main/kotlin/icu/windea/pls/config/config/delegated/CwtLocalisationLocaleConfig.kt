@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*

interface CwtLocalisationLocaleConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val id: String
    val codes: List<String>
    val description: String
    val text: String

    val shortId: String get() = id.removePrefix("l_")

    companion object {
        val AUTO: CwtLocalisationLocaleConfig = AutoCwtLocalisationLocaleConfig(ParadoxLocaleManager.ID_AUTO)
        val AUTO_OS: CwtLocalisationLocaleConfig = AutoCwtLocalisationLocaleConfig(ParadoxLocaleManager.ID_AUTO_OS)
        val FALLBACK: CwtLocalisationLocaleConfig = FallbackCwtLocalisationLocaleConfig(ParadoxLocaleManager.ID_FALLBACK)

        fun resolve(config: CwtPropertyConfig): CwtLocalisationLocaleConfig = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtLocalisationLocaleConfig {
    val id = config.key
    val codes = config.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }.orEmpty()
    return CwtLocalisationLocaleConfigImpl(config, id, codes)
}

private class CwtLocalisationLocaleConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val codes: List<String>
) : UserDataHolderBase(), CwtLocalisationLocaleConfig {
    override val description: String get() = PlsDocBundle.locale(id)
    override val text get() = if(description.isEmpty()) id else "$id ($description)"

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationLocaleConfig && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return description
    }
}

private class AutoCwtLocalisationLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocalisationLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val description: String get() = PlsDocBundle.locale(id)
    override val text: String get() = description

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationLocaleConfig && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return description
    }
}

private class FallbackCwtLocalisationLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocalisationLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val description: String get() = PlsDocBundle.locale(id)
    override val text get() = if(description.isEmpty()) id else "$id ($description)"

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationLocaleConfig && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return description
    }
}
