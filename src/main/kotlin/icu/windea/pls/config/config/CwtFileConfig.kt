@file:Optimized

package icu.windea.pls.config.config

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.model.CwtMembersType

/**
 * 文件规则。
 *
 * 对应一整个 CWT 规则文件。
 *
 * @property name 文件名。
 * @property path 文件路径（相对于所属规则分组的目录）。
 * @property configs 子规则列表（其中的属性与值对应的成员规则）。
 * @property properties 子属性规则列表（其中的属性对应的成员规则）。
 * @property values 子值规则列表（其中的值对应的成员规则）。
 *
 * @see CwtFile
 */
interface CwtFileConfig : CwtMemberContainerConfig<CwtFile> {
    val name: String
    val path: String
    override val configs: List<CwtMemberConfig<*>>
    override val properties: List<CwtPropertyConfig>
    override val values: List<CwtValueConfig>

    interface Resolver {
        fun create(
            pointer: SmartPsiElementPointer<CwtFile>,
            configGroup: CwtConfigGroup,
            fileName: String,
            filePath: String,
            configs: List<CwtMemberConfig<*>> = emptyList(),
        ): CwtFileConfig

        fun withConfigs(config: CwtFileConfig, configs: List<CwtMemberConfig<*>>): Boolean

        fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig
    }

    companion object : Resolver by CwtFileConfigResolverImpl()
}

// region Implementations

private class CwtFileConfigResolverImpl : CwtFileConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun create(
        pointer: SmartPsiElementPointer<CwtFile>,
        configGroup: CwtConfigGroup,
        fileName: String,
        filePath: String,
        configs: List<CwtMemberConfig<*>>,
    ): CwtFileConfig {
        val withConfigs = configs.isNotEmpty()
        val config = when (withConfigs) {
            true -> CwtFileConfigImplWithConfigs(pointer, configGroup, fileName, filePath)
            else -> CwtFileConfigImpl(pointer, configGroup, fileName, filePath)
        }
        if (withConfigs) withConfigs(config, configs)
        return config
    }

    override fun withConfigs(config: CwtFileConfig, configs: List<CwtMemberConfig<*>>): Boolean {
        if (config is CwtFileConfigImplWithConfigs) {
            config.configs = configs.optimized() // optimized to optimize memory
            config.memberType = CwtMembersType.UNSET
            return true
        }
        return false
    }

    override fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig {
        val pointer = file.createPointer()
        val fileName = file.name
        val rootBlock = file.block
        val configs = CwtConfigResolverManager.getConfigs(rootBlock, file, configGroup).orEmpty()
        val config = create(pointer, configGroup, fileName, filePath, configs)
        when (configs.isEmpty()) {
            true -> logger.debug { "Resolved empty file config.".withLocationPrefix() }
            else -> logger.debug { "Resolved file config (${configs.size} member configs).".withLocationPrefix() }
        }
        return config
    }
}

private sealed class CwtFileConfigBase : UserDataHolderBase(), CwtFileConfig {
    override fun toString() = "CwtFileConfig(name='$name', path='$path')"
}

// 12 + 4 * 4 = 28 -> 32
private sealed class CwtFileConfigImplBase(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
) : CwtFileConfigBase()

// 12 + 5 * 4 = 32 -> 32
private class CwtFileConfigImpl(
    pointer: SmartPsiElementPointer<CwtFile>,
    configGroup: CwtConfigGroup,
    name: String,
    path: String,
) : CwtFileConfigImplBase(pointer, configGroup, name, path) {
    override val configs: List<CwtMemberConfig<*>> get() = emptyList()
    override val properties: List<CwtPropertyConfig> get() = emptyList()
    override val values: List<CwtValueConfig> get() = emptyList()
}

// 12 + 7 * 4 = 40 -> 40
private class CwtFileConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<CwtFile>,
    configGroup: CwtConfigGroup,
    name: String,
    path: String,
) : CwtFileConfigImplBase(pointer, configGroup, name, path) {
    @Volatile override var configs: List<CwtMemberConfig<*>> = emptyList()
    @Volatile var memberType: CwtMembersType = CwtMembersType.UNSET

    override val properties: List<CwtPropertyConfig>
        get() {
            if (memberType == CwtMembersType.UNSET) memberType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getProperties(configs, memberType)
        }
    override val values: List<CwtValueConfig>
        get() {
            if (memberType == CwtMembersType.UNSET) memberType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getValues(configs, memberType)
        }
}

// endregion
