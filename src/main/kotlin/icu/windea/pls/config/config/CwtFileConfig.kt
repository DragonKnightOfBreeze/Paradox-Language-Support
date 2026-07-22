@file:Optimized

package icu.windea.pls.config.config

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.filterIsInstanceFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.psi.CwtFile

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

    /** 尝试将 [configs] 作为当前规则的子规则列表。返回是否操作成功。 */
    fun withConfigs(configs: List<CwtMemberConfig<*>>): Boolean = false

    companion object {
        @JvmStatic
        fun create(
            pointer: SmartPsiElementPointer<CwtFile>,
            configGroup: CwtConfigGroup,
            fileName: String,
            filePath: String,
            configs: List<CwtMemberConfig<*>> = emptyList(),
        ): CwtFileConfig = CwtFileConfigResolver.create(pointer, configGroup, fileName, filePath, configs)

        @JvmStatic
        fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig = CwtFileConfigResolver.resolve(file, configGroup, filePath)

        @JvmStatic
        fun resolve(file: VirtualFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig? = CwtFileConfigResolver.resolve(file, configGroup, filePath)
    }
}

// region Implementations

private object CwtFileConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun create(
        pointer: SmartPsiElementPointer<CwtFile>,
        configGroup: CwtConfigGroup,
        fileName: String,
        filePath: String,
        configs: List<CwtMemberConfig<*>>,
    ): CwtFileConfig {
        val withConfigs = configs.isNotEmpty()
        val config = when (withConfigs) {
            true -> CwtFileConfigImplWithConfigs(pointer, configGroup, fileName, filePath)
                .also { it.configs = configs.optimized() } // optimized to optimize memory
            else -> CwtFileConfigImpl(pointer, configGroup, fileName, filePath)
        }
        return config
    }

    fun resolve(file: CwtFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig {
        val pointer = file.createPointer()
        val fileName = file.name
        val rootBlock = file.block
        val configs = CwtConfigResolverManager.getConfigs(rootBlock, file, configGroup).orEmpty()
        val config = create(pointer, configGroup, fileName, filePath, configs)
        when {
            configs.isEmpty() -> logger.debug { "Resolved file config (path: ${config.path}, empty member configs).".withLocationPrefix(file, configGroup) }
            else -> logger.debug { "Resolved file config (path: ${config.path}, ${configs.size} member configs).".withLocationPrefix(file, configGroup) }
        }
        return config
    }

    fun resolve(file: VirtualFile, configGroup: CwtConfigGroup, filePath: String): CwtFileConfig? {
        if (file.fileType != CwtFileType) return null
        val psiFile = runCatchingCancelable { file.toPsiFile(configGroup.project) }.onFailure { logger.warn(it) }.getOrNull()
        if (psiFile !is CwtFile) return null
        return resolve(psiFile, configGroup, filePath)
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
) : CwtFileConfigBase() {
    override val properties: List<CwtPropertyConfig> get() = configs.filterIsInstanceFast()
    override val values: List<CwtValueConfig> get() = configs.filterIsInstanceFast()
}

// 12 + 5 * 4 = 32 -> 32
private class CwtFileConfigImpl(
    pointer: SmartPsiElementPointer<CwtFile>,
    configGroup: CwtConfigGroup,
    name: String,
    path: String,
) : CwtFileConfigImplBase(pointer, configGroup, name, path) {
    override val configs: List<CwtMemberConfig<*>> get() = emptyList()
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

    override fun withConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
        this.configs = configs.optimized() // optimized to optimize memory
        this.memberType = CwtMembersType.UNSET
        return true
    }
}

// endregion
