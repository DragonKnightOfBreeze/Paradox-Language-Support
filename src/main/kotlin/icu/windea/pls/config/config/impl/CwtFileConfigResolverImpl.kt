package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.processChild
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue

internal class CwtFileConfigResolverImpl : CwtFileConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig {
        val rootBlock = file.block
        val properties = mutableListOf<CwtPropertyConfig>()
        val values = mutableListOf<CwtValueConfig>()
        rootBlock?.processChild { e ->
            when (e) {
                is CwtProperty -> CwtPropertyConfig.resolve(e, file, configGroup)?.also { properties += it }
                is CwtValue -> CwtValueConfig.resolve(e, file, configGroup).also { values += it }
            }
            true
        }
        val memberSize = properties.size + values.size
        logger.debug { "Resolved file config ($memberSize member configs).".withLocationPrefix() }
        // optimized to optimize memory
        return CwtFileConfigImpl(file.createPointer(), configGroup, file.name, filePath, properties.optimized(), values.optimized())
    }
}

private class CwtFileConfigImpl(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
    override val properties: List<CwtPropertyConfig>,
    override val values: List<CwtValueConfig>,
) : UserDataHolderBase(), CwtFileConfig {
    override fun toString() = "CwtFileConfigImpl(name='$name', path='$path')"
}
