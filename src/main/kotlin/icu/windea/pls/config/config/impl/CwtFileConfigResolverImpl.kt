package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.config.util.CwtConfigResolverUtil
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.ifNotEmpty
import icu.windea.pls.core.createPointer
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.CwtMemberType

internal class CwtFileConfigResolverImpl : CwtFileConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig {
        val pointer = file.createPointer()
        val fileName = file.name
        val rootBlock = file.block
        if (rootBlock == null) {
            logger.debug { "Resolved empty file config.".withLocationPrefix() }
            return CwtFileConfigImpl(pointer, configGroup, fileName, filePath)
        }
        val configs = CwtConfigResolverUtil.getConfigs(rootBlock, file, configGroup).orEmpty()
        logger.debug { "Resolved file config (${configs.size} member configs).".withLocationPrefix() }
        val memberType = CwtConfigResolverUtil.checkMemberType(configs)
        return when (memberType) {
            null -> CwtFileConfigImplWithConfigs(pointer, configGroup, fileName, filePath, configs)
            CwtMemberType.PROPERTY -> CwtFileConfigImplWithPropertyConfigs(pointer, configGroup, fileName, filePath, configs)
            CwtMemberType.VALUE -> CwtFileConfigImplWithValueConfigs(pointer, configGroup, fileName, filePath, configs)
        }
    }
}

private abstract class CwtFileConfigBase : UserDataHolderBase(), CwtFileConfig {
    override val properties: List<CwtPropertyConfig> get() = configs.ifNotEmpty { filterIsInstance<CwtPropertyConfig>() }
    override val values: List<CwtValueConfig> get() = configs.ifNotEmpty { filterIsInstance<CwtValueConfig>() }

    override fun toString() = "CwtFileConfigImpl(name='$name', path='$path')"
}

private class CwtFileConfigImpl(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
) : CwtFileConfigBase() {
    override val configs: List<CwtMemberConfig<*>> get() = emptyList()
}

private class CwtFileConfigImplWithConfigs(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
    override val configs: List<CwtMemberConfig<*>>,
) : CwtFileConfigBase()

private class CwtFileConfigImplWithPropertyConfigs(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
    override val configs: List<CwtMemberConfig<*>>,
) : CwtFileConfigBase() {
    override val properties: List<CwtPropertyConfig> get() = configs.cast()
    override val values: List<CwtValueConfig> get() = emptyList()
}

private class CwtFileConfigImplWithValueConfigs(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
    override val configs: List<CwtMemberConfig<*>>,
) : CwtFileConfigBase() {
    override val properties: List<CwtPropertyConfig> get() = emptyList()
    override val values: List<CwtValueConfig> get() = configs.cast()
}
