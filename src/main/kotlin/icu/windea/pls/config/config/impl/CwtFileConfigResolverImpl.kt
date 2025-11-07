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
import icu.windea.pls.core.collections.filterIsInstanceFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.CwtMemberType

internal class CwtFileConfigResolverImpl : CwtFileConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun create(
        pointer: SmartPsiElementPointer<CwtFile>,
        configGroup: CwtConfigGroup,
        fileName: String,
        filePath: String,
        configs: List<CwtMemberConfig<*>>,
    ): CwtFileConfig {
        val noConfigs = configs.isEmpty()
        if (noConfigs) {
            return CwtFileConfigImpl(pointer, configGroup, fileName, filePath)
        }
        val configs = configs.optimized() // optimized to optimize memory
        val memberType = CwtConfigResolverUtil.checkMemberType(configs)
        return when (memberType) {
            null -> CwtFileConfigImplWithConfigs(pointer, configGroup, fileName, filePath, configs)
            CwtMemberType.PROPERTY -> CwtFileConfigImplWithPropertyConfigs(pointer, configGroup, fileName, filePath, configs)
            CwtMemberType.VALUE -> CwtFileConfigImplWithValueConfigs(pointer, configGroup, fileName, filePath, configs)
        }
    }

    override fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig {
        val pointer = file.createPointer()
        val fileName = file.name
        val rootBlock = file.block
        val configs = CwtConfigResolverUtil.getConfigs(rootBlock, file, configGroup).orEmpty()
        val config = create(pointer, configGroup, fileName, filePath, configs)
        when (configs.isEmpty()) {
            true -> logger.debug { "Resolved empty file config.".withLocationPrefix() }
            else -> logger.debug { "Resolved file config (${configs.size} member configs).".withLocationPrefix() }
        }
        return config
    }
}

private abstract class CwtFileConfigBase : UserDataHolderBase(), CwtFileConfig {
    override val properties: List<CwtPropertyConfig> get() = configs.filterIsInstanceFast<CwtPropertyConfig>()
    override val values: List<CwtValueConfig> get() = configs.filterIsInstanceFast<CwtValueConfig>()

    override fun toString() = "CwtFileConfig(name='$name', path='$path')"
}

private abstract class CwtFileConfigImplBase(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
) : CwtFileConfigBase()

private class CwtFileConfigImpl(
    pointer: SmartPsiElementPointer<CwtFile>,
    configGroup: CwtConfigGroup,
    name: String,
    path: String,
) : CwtFileConfigImplBase(pointer, configGroup, name, path) {
    override val configs: List<CwtMemberConfig<*>> get() = emptyList()
}

private open class CwtFileConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<CwtFile>,
    configGroup: CwtConfigGroup,
    name: String,
    path: String,
    override val configs: List<CwtMemberConfig<*>>,
) : CwtFileConfigImplBase(pointer, configGroup, name, path)

private class CwtFileConfigImplWithPropertyConfigs(
    pointer: SmartPsiElementPointer<CwtFile>,
    configGroup: CwtConfigGroup,
    name: String,
    path: String,
    configs: List<CwtMemberConfig<*>>,
) : CwtFileConfigImplWithConfigs(pointer, configGroup, name, path, configs) {
    override val properties: List<CwtPropertyConfig> get() = configs.cast()
    override val values: List<CwtValueConfig> get() = emptyList()
}

private class CwtFileConfigImplWithValueConfigs(
    pointer: SmartPsiElementPointer<CwtFile>,
    configGroup: CwtConfigGroup,
    name: String,
    path: String,
    configs: List<CwtMemberConfig<*>>,
) : CwtFileConfigImplWithConfigs(pointer, configGroup, name, path, configs) {
    override val properties: List<CwtPropertyConfig> get() = emptyList()
    override val values: List<CwtValueConfig> get() = configs.cast()
}
