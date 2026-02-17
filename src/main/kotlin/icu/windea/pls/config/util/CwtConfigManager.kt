package icu.windea.pls.config.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
import icu.windea.pls.config.config.delegated.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.configExpression.CwtConfigExpressionService
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupFileSource
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtRootBlock
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.paths.CwtConfigPath
import icu.windea.pls.model.paths.ParadoxPath
import kotlin.io.path.name

object CwtConfigManager {
    object Keys : KeyRegistry() {
        val gameTypeIdFromRepoFile by registerKey<String>(Keys)
        val cachedConfigPath by registerKey<CachedValue<CwtConfigPath>>(Keys)
        val cachedConfigType by registerKey<CachedValue<CwtConfigType>>(Keys)
        val cachedDocumentation by registerKey<CachedValue<String>>(Keys)
        val filePathPatterns by registerKey<Set<String>>(Keys)
        val filePathPatternsForPriority by registerKey<Set<String>>(Keys)

        /** 用于在解析引用时，将规则临时写入到对应的PSI的用户数据中。 */
        val config by registerKey<CwtConfig<*>>(this)
    }

    fun getContainingConfigGroup(element: PsiElement): CwtConfigGroup? {
        val file = runReadActionSmartly { element.containingFile } ?: return null
        val vFile = file.virtualFile ?: return null
        return getContainingConfigGroup(vFile, file.project)
    }

    fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        CwtConfigService.getContainingConfigGroupFromFileProviders(file, project)?.let { return it }

        // 兼容插件或者规则仓库中的 CWT 文件（此时将其视为规则文件）
        CwtConfigService.getContainingConfigGroupForRepo(file, project)?.let { return it }

        return null
    }

    fun getBuiltInConfigRootDirectories(project: Project): List<VirtualFile> {
        return CwtConfigGroupFileProvider.EP_NAME.extensionList
            .filter { it.source == CwtConfigGroupFileSource.BuiltIn }
            .mapNotNull { it.getRootDirectory(project) }
    }

    fun isInternalFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return isInternalFile(vFile, file.project)
    }

    fun isInternalFile(file: VirtualFile, project: Project): Boolean {
        val filePath = getFilePath(file, project)
        if (filePath == null) {
            // 兼容插件或者规则仓库中的 CWT 文件（此时将其视为规则文件）
            val gameType = CwtConfigService.getGameTypeFromRepoFile(file, project)
            if (gameType == null) return false
            return file.parent.toNioPathOrNull()?.any { it.name == "internal" } ?: false
        }
        return filePath.startsWith("internal/")
    }

    fun getFilePath(file: PsiFile): String? {
        val vFile = file.virtualFile ?: return null
        return getFilePath(vFile, file.project)
    }

    fun getFilePath(file: VirtualFile, project: Project): String? {
        if (file.fileType !is CwtFileType) return null
        val configGroup = getContainingConfigGroup(file, project) ?: return null
        return CwtConfigService.resolveFilePath(file, configGroup)
    }

    fun getConfigPath(element: PsiElement): CwtConfigPath? {
        if (element.language !is CwtLanguage) return null
        if (element is CwtFile || element is CwtRootBlock) return CwtConfigPath.resolveEmpty()
        val memberElement = element.parentOfType<CwtMember>(withSelf = true) ?: return null
        // from cache (invalidated on file modification)
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigPath) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val file = element.containingFile
                val value = CwtConfigService.resolveConfigPath(memberElement)?.normalize()
                value.withDependencyItems(file)
            }
        }
    }

    fun getConfigType(element: PsiElement): CwtConfigType? {
        if (element.language !is CwtLanguage) return null
        val memberElement = element.parentOfType<CwtMember>(withSelf = true) ?: return null
        // from cache (invalidated on file modification)
        return CachedValuesManager.getCachedValue(memberElement, Keys.cachedConfigType) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val file = memberElement.containingFile
                val value = CwtConfigService.resolveConfigType(memberElement, file)
                value.withDependencyItems(file)
            }
        }
    }

    fun getNameByConfigType(text: String, configType: CwtConfigType): String? {
        return CwtConfigService.resolveNameByConfigType(text, configType)?.optimized() // optimized to optimize memory
    }

    fun getDocumentation(config: CwtMemberConfig<*>): String? {
        val memberElement = config.pointer.element ?: return null
        // from cache (invalidated on file modification)
        return CachedValuesManager.getCachedValue(memberElement, Keys.cachedDocumentation) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val file = memberElement.containingFile
                val value = CwtConfigService.getDocumentation(memberElement)
                value.withDependencyItems(file)
            }
        }
    }

    fun getFilePathPatterns(config: CwtFilePathMatchableConfig): Set<String> {
        return config.getOrPutUserData(Keys.filePathPatterns) {
            CwtConfigService.getFilePathPatterns(config).optimized() // optimized to optimize memory
        }
    }

    fun getFilePathPatternsForPriority(config: CwtFilePathMatchableConfig): Set<String> {
        return config.getOrPutUserData(Keys.filePathPatternsForPriority) {
            CwtConfigService.getFilePathPatternsForPriority(config).optimized() // optimized to optimize memory
        }
    }

    fun matchesFilePathPattern(config: CwtFilePathMatchableConfig, filePath: ParadoxPath): Boolean {
        // This method should be very fast
        // 1.4.2 use optimized match logic, DO NOT use config.filePathPatterns here

        val pathPatterns = config.pathPatterns
        if (pathPatterns.isNotEmpty()) {
            if (pathPatterns.any { filePath.path.matchesAntPattern(it) }) return true
        }
        val pathFile = config.pathFile
        if (pathFile.isNotNullOrEmpty()) {
            if (pathFile != filePath.fileName) return false
        } else {
            val pathExtension = config.pathExtension
            if (pathExtension.isNotNullOrEmpty()) {
                if (filePath.fileExtension == null || !pathExtension.equals(filePath.fileExtension, true)) return false
            }
        }
        val paths = config.paths
        if (paths.isNotEmpty()) {
            val pathStrict = config.pathStrict
            for (path in paths) {
                if (path.matchesPath(filePath.path, strict = pathStrict)) return true
            }
            return false
        } else {
            val pathExtension = config.pathExtension
            if (pathFile.isNullOrEmpty() && pathExtension.isNullOrEmpty()) return false
            return true
        }
    }

    fun isRemoved(config: CwtConfig<*>): Boolean {
        if (config !is CwtSingleAliasConfig && config !is CwtAliasConfig) return false
        return config.config.optionData.apiStatus == CwtApiStatus.Removed
    }

    fun getAliasKeys(configGroup: CwtConfigGroup, aliasName: String, key: String): Set<String> {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) // 不区分大小写
        if (constKey != null) return setOf(constKey)
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return emptySet()
        return keys
    }

    fun getEntryName(config: CwtConfig<*>): String? {
        return when {
            config is CwtPropertyConfig -> config.key
            config is CwtValueConfig && config.propertyConfig != null -> getEntryName(config.propertyConfig!!)
            config is CwtValueConfig -> null
            config is CwtAliasConfig -> config.subName
            else -> null
        }
    }

    fun getEntryConfigs(config: CwtConfig<*>): List<CwtMemberConfig<*>> {
        val configGroup = config.configGroup
        return when (config) {
            is CwtPropertyConfig -> {
                config.inlineConfig?.let { return getEntryConfigs(it) }
                config.aliasConfig?.let { return getEntryConfigs(it) }
                config.singleAliasConfig?.let { return getEntryConfigs(it) }
                config.parentConfig?.configs?.filter { it is CwtPropertyConfig && it.key == config.key }?.let { return it }
                config.to.singletonList()
            }
            is CwtValueConfig -> {
                config.propertyConfig?.let { return getEntryConfigs(it) }
                config.parentConfig?.configs?.filterIsInstance<CwtValueConfig>()?.let { return it }
                config.to.singletonList()
            }
            is CwtSingleAliasConfig -> {
                config.config.to.singletonListOrEmpty()
            }
            is CwtAliasConfig -> {
                configGroup.aliasGroups.get(config.name)?.get(config.subName)?.map { it.config }.orEmpty()
            }
            is CwtDirectiveConfig -> {
                config.config.to.singletonListOrEmpty()
            }
            else -> {
                emptyList()
            }
        }
    }

    fun getFullNamesFromSuffixAware(config: CwtConfig<*>, name: String): List<String> {
        val suffixes = config.configExpression?.suffixes
        if (suffixes.isNullOrEmpty()) return listOf(name)
        return suffixes.map { name + it }
    }

    fun findLiterals(configs: List<CwtMemberConfig<*>>): Set<String> {
        val configGroup = configs.firstOrNull()?.configGroup ?: return emptySet()
        val result = mutableSetOf<String>()
        for (config in configs) {
            val configExpression = config.configExpression
            CwtConfigExpressionService.findLiterals(configExpression, configGroup, result)
        }
        return result
    }
}
