package icu.windea.pls.config.util

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.aliasKeysGroupConst
import icu.windea.pls.config.configGroup.aliasKeysGroupNoConst
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.singleAliases
import icu.windea.pls.core.executeCommand
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.substringIn
import icu.windea.pls.core.substringInLast
import icu.windea.pls.core.surroundsWith
import icu.windea.pls.core.toPsiDirectory
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtRootBlock
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.lang.psi.PlsPsiManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.CwtConfigPath
import icu.windea.pls.model.paths.ParadoxPath
import kotlin.io.path.name

object CwtConfigManager {
    object Keys : KeyRegistry() {
        val gameTypeIdFromRepoFile by createKey<String>(Keys)
        val cachedConfigPath by createKey<CachedValue<CwtConfigPath>>(Keys)
        val cachedConfigType by createKey<CachedValue<CwtConfigType>>(Keys)
        val cachedDocumentation by createKey<CachedValue<String>>(Keys)
        val filePathPatterns by createKey<Set<String>>(Keys)
        val filePathPatternsForPriority by createKey<Set<String>>(Keys)
    }

    fun getContainingConfigGroup(element: PsiElement): CwtConfigGroup? {
        val file = runReadAction { element.containingFile } ?: return null
        val vFile = file.virtualFile ?: return null
        return getContainingConfigGroup(vFile, file.project)
    }

    fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup? {
        doGetContainingConfigGroupFromFileProviders(file, project)?.let { return it }

        // 兼容插件或者规则仓库中的 CWT 文件（此时将其视为规则文件）
        doGetContainingConfigGroupForRepo(file, project)?.let { return it }

        return null
    }

    private fun doGetContainingConfigGroupFromFileProviders(file: VirtualFile, project: Project): CwtConfigGroup? {
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroup = fileProviders.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(file, project)
        }
        return configGroup
    }

    private fun doGetContainingConfigGroupForRepo(file: VirtualFile, project: Project): CwtConfigGroup? {
        val gameType = getGameTypeFromRepoFile(file, project)
        if (gameType == null) return null
        return PlsFacade.getConfigGroup(project, gameType)
    }

    fun getGameTypeFromRepoFile(file: VirtualFile, project: Project): ParadoxGameType? {
        // 使用缓存以优化性能
        val parents = generateSequence(file.parent) { it.parent }
        val root = parents.find { it.findChild(".git") != null } ?: return null
        val rootPsi = root.toPsiDirectory(project) ?: return null
        val gameTypeId = rootPsi.getOrPutUserData(Keys.gameTypeIdFromRepoFile) {
            runCatching {
                val command = "git remote -v"
                val workDirectory = root.toNioPath().toFile()
                val commandResult = executeCommand(command, workDirectory = workDirectory)
                val gameTypeId = commandResult.lines()
                    .mapNotNull { it.splitByBlank(3).getOrNull(1) }
                    .firstNotNullOfOrNull t@{
                        when {
                            it.contains("Paradox-Language-Support") -> {
                                val coreRoot = VfsUtil.findRelativeFile(root, "cwt/core")
                                val r = coreRoot != null && VfsUtil.isAncestor(coreRoot, file, true)
                                if (r) "core" else null
                            }
                            else -> it.substringInLast("cwtools-", "-config", "").orNull()
                        }
                    }
                gameTypeId
            }.getOrNull()
        }
        if (gameTypeId.isNullOrEmpty()) return null
        return ParadoxGameType.get(gameTypeId, withCore = true)
    }

    fun getBuiltInConfigRootDirectories(project: Project): List<VirtualFile> {
        return CwtConfigGroupFileProvider.EP_NAME.extensionList
            .filter { it.type == CwtConfigGroupFileProvider.Type.BuiltIn }
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
            val gameType = getGameTypeFromRepoFile(file, project)
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
        val gameType = configGroup.gameType
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType)
            val directory = rootDirectory.findChild(directoryName) ?: return@f
            val relativePath = VfsUtil.getRelativePath(file, directory) ?: return@f
            return relativePath
        }
        return null
    }

    fun getConfigPath(element: PsiElement): CwtConfigPath? {
        if (element.language !is CwtLanguage) return null
        if (element is CwtFile || element is CwtRootBlock) return CwtConfigPath.resolveEmpty()
        val memberElement = element.parentOfType<CwtMember>(withSelf = true)
        if (memberElement == null) return null
        return doGetConfigPathFromCache(memberElement)
    }

    private fun doGetConfigPathFromCache(element: CwtMember): CwtConfigPath? {
        // invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigPath) {
            runReadAction {
                val file = element.containingFile
                val value = doGetConfigPath(element)?.normalize()
                value.withDependencyItems(file)
            }
        }
    }

    private fun doGetConfigPath(element: CwtMember): CwtConfigPath? {
        var current: PsiElement = element
        var depth = 0
        val subPaths = ArrayDeque<String>()
        while (current !is PsiFile) {
            when {
                current is CwtProperty -> {
                    subPaths.addFirst(current.name)
                    depth++
                }
                current is CwtValue && current.isBlockValue() -> {
                    subPaths.addFirst("-")
                    depth++
                }
            }
            current = current.parent ?: break
        }
        if (current !is CwtFile) return null // unexpected
        return CwtConfigPath.resolve(subPaths)
    }

    fun getConfigType(element: PsiElement): CwtConfigType? {
        if (element.language !is CwtLanguage) return null
        val memberElement = element.parentOfType<CwtMember>(withSelf = true)
        if (memberElement == null) return null
        return doGetConfigTypeFromCache(memberElement)
    }

    private fun doGetConfigTypeFromCache(element: CwtMember): CwtConfigType? {
        // invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigType) {
            runReadAction {
                val file = element.containingFile
                val value = doGetConfigType(element, file)
                value.withDependencyItems(file)
            }
        }
    }

    private fun doGetConfigType(element: CwtMember, file: PsiFile): CwtConfigType? {
        if (element !is CwtProperty && element !is CwtValue) return null
        if (isInternalFile(file)) return null // 排除内部规则文件
        val configPath = getConfigPath(element)
        if (configPath == null || configPath.isEmpty()) return null

        return when {
            element is CwtProperty && configPath.path.matchesAntPattern("types/type[*]") -> {
                CwtConfigTypes.Type
            }
            element is CwtProperty && configPath.path.matchesAntPattern("types/type[*]/subtype[*]") -> {
                CwtConfigTypes.Subtype
            }
            element is CwtProperty && configPath.path.matchesAntPattern("types/type[*]/modifiers/**") -> {
                when {
                    configPath.get(3).surroundsWith("subtype[", "]") -> {
                        if (configPath.length == 5) return CwtConfigTypes.Modifier
                    }
                    else -> {
                        if (configPath.length == 4) return CwtConfigTypes.Modifier
                    }
                }
                null
            }
            element is CwtProperty && configPath.path.matchesAntPattern("rows/row[*]") -> {
                CwtConfigTypes.Row
            }
            element is CwtProperty && configPath.path.matchesAntPattern("enums/enum[*]") -> {
                CwtConfigTypes.Enum
            }
            element is CwtValue && configPath.path.matchesAntPattern("enums/enum[*]/*") -> {
                CwtConfigTypes.EnumValue
            }
            element is CwtProperty && configPath.path.matchesAntPattern("enums/complex_enum[*]") -> {
                CwtConfigTypes.ComplexEnum
            }
            element is CwtProperty && configPath.path.matchesAntPattern("values/value[*]") -> {
                CwtConfigTypes.DynamicValueType
            }
            element is CwtValue && configPath.path.matchesAntPattern("values/value[*]/*") -> {
                CwtConfigTypes.DynamicValue
            }
            element is CwtProperty && configPath.path.matchesAntPattern("inline[*]") -> {
                CwtConfigTypes.Inline
            }
            element is CwtProperty && configPath.path.matchesAntPattern("single_alias[*]") -> {
                CwtConfigTypes.SingleAlias
            }
            element is CwtProperty && configPath.path.matchesAntPattern("alias[*]") -> {
                val aliasName = configPath.get(0).substringIn('[', ']', "").substringBefore(':', "")
                when {
                    aliasName == "modifier" -> return CwtConfigTypes.Modifier
                    aliasName == "trigger" -> return CwtConfigTypes.Trigger
                    aliasName == "effect" -> return CwtConfigTypes.Effect
                }
                CwtConfigTypes.Alias
            }
            element is CwtProperty && configPath.path.matchesAntPattern("links/*") -> {
                CwtConfigTypes.Link
            }
            element is CwtProperty && configPath.path.matchesAntPattern("localisation_links/*") -> {
                CwtConfigTypes.LocalisationLink
            }
            element is CwtProperty && configPath.path.matchesAntPattern("localisation_promotions/*") -> {
                CwtConfigTypes.LocalisationPromotion
            }
            element is CwtProperty && configPath.path.matchesAntPattern("localisation_commands/*") -> {
                CwtConfigTypes.LocalisationCommand
            }
            element is CwtProperty && configPath.path.matchesAntPattern("modifier_categories/*") -> {
                CwtConfigTypes.ModifierCategory
            }
            element is CwtProperty && configPath.path.matchesAntPattern("modifiers/*") -> {
                CwtConfigTypes.Modifier
            }
            element is CwtProperty && configPath.path.matchesAntPattern("scopes/*") -> {
                CwtConfigTypes.Scope
            }
            element is CwtProperty && configPath.path.matchesAntPattern("scope_groups/*") -> {
                CwtConfigTypes.ScopeGroup
            }
            element is CwtProperty && configPath.path.matchesAntPattern("database_object_types/*") -> {
                CwtConfigTypes.DatabaseObjectType
            }
            element is CwtProperty && configPath.path.matchesAntPattern("system_scopes/*") -> {
                CwtConfigTypes.SystemScope
            }
            element is CwtProperty && configPath.path.matchesAntPattern("locales/*") -> {
                CwtConfigTypes.Locale
            }
            configPath.path.matchesAntPattern("scripted_variables/*") -> {
                CwtConfigTypes.ExtendedScriptedVariable
            }
            configPath.path.matchesAntPattern("definitions/*") -> {
                CwtConfigTypes.ExtendedDefinition
            }
            configPath.path.matchesAntPattern("game_rules/*") -> {
                CwtConfigTypes.ExtendedGameRule
            }
            configPath.path.matchesAntPattern("on_actions/*") -> {
                CwtConfigTypes.ExtendedOnAction
            }
            configPath.path.matchesAntPattern("inline_scripts/*") -> {
                CwtConfigTypes.ExtendedInlineScript
            }
            configPath.path.matchesAntPattern("parameters/*") -> {
                CwtConfigTypes.ExtendedParameter
            }
            configPath.path.matchesAntPattern("complex_enum_values/*/*") -> {
                CwtConfigTypes.ExtendedComplexEnumValue
            }
            configPath.path.matchesAntPattern("dynamic_values/*/*") -> {
                CwtConfigTypes.ExtendedDynamicValue
            }
            else -> null
        }
    }

    fun getNameByConfigType(text: String, configType: CwtConfigType): String? {
        return when (configType) {
            CwtConfigTypes.Type -> text.removeSurroundingOrNull("type[", "]")
            CwtConfigTypes.Subtype -> text.removeSurroundingOrNull("subtype[", "]")
            CwtConfigTypes.Row -> text.removeSurroundingOrNull("row[", "]")
            CwtConfigTypes.Enum -> text.removeSurroundingOrNull("enum[", "]")
            CwtConfigTypes.ComplexEnum -> text.removeSurroundingOrNull("complex_enum[", "]")
            CwtConfigTypes.Inline -> text.removeSurroundingOrNull("inline[", "]")
            CwtConfigTypes.SingleAlias -> text.removeSurroundingOrNull("single_alias[", "]")
            CwtConfigTypes.Alias -> text.removeSurroundingOrNull("alias[", "]")
            CwtConfigTypes.Trigger -> text.removeSurroundingOrNull("alias[trigger:", "]")
            CwtConfigTypes.Effect -> text.removeSurroundingOrNull("alias[effect:", "]")
            CwtConfigTypes.Modifier -> text.removeSurroundingOrNull("alias[modifier:", "]") ?: text
            else -> text
        }?.orNull()?.optimized() // optimized to optimize memory
    }

    fun getDocumentation(config: CwtMemberConfig<*>): String? {
        val element = config.pointer.element ?: return null
        return doGetDocumentationFromCache(element)
    }

    private fun doGetDocumentationFromCache(element: CwtMember): String? {
        // invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedDocumentation) {
            runReadAction {
                val file = element.containingFile
                val value = doGetDocumentation(element)
                value.withDependencyItems(file)
            }
        }
    }

    private fun doGetDocumentation(element: CwtMember): String? {
        val ownedComments = PlsPsiManager.getOwnedComments(element) { it is CwtDocComment }
        val documentation = PlsPsiManager.getDocCommentText(ownedComments, "<br>")
        return documentation
    }

    fun getFilePathPatterns(config: CwtFilePathMatchableConfig): Set<String> {
        return config.getOrPutUserData(Keys.filePathPatterns) { doGetFilePathPatterns(config) }
    }

    private fun doGetFilePathPatterns(config: CwtFilePathMatchableConfig): Set<String> {
        val paths = config.paths
        val pathFile = config.pathFile
        val pathExtension = config.pathExtension
        val pathStrict = config.pathStrict
        val pathPatterns = config.pathPatterns
        val result = sortedSetOf<String>()
        val filePattern = when {
            pathFile.isNotNullOrEmpty() -> pathFile
            pathExtension.isNotNullOrEmpty() -> "*.${pathExtension}"
            else -> null
        }
        if (paths.isNotEmpty()) {
            for (path in paths) {
                if (path.isNotEmpty()) {
                    result += buildString {
                        append(path)
                        if (pathStrict) {
                            if (filePattern.isNotNullOrEmpty()) {
                                append("/").append(filePattern)
                            } else {
                                append("/*")
                            }
                        } else {
                            if (filePattern.isNotNullOrEmpty()) {
                                append("/**/").append(filePattern)
                            } else {
                                append("/**")
                            }
                        }
                    }
                } else if (filePattern.isNotNullOrEmpty()) {
                    result += filePattern
                }
            }
        } else if (filePattern.isNotNullOrEmpty()) {
            result += filePattern
        }
        if (pathPatterns.isNotEmpty()) {
            result += pathPatterns
        }
        return result.optimized() // optimized to optimize memory
    }

    fun getFilePathPatternsForPriority(config: CwtFilePathMatchableConfig): Set<String> {
        return config.getOrPutUserData(Keys.filePathPatternsForPriority) { doGetFilePathPatternsForPriority(config) }
    }

    private fun doGetFilePathPatternsForPriority(config: CwtFilePathMatchableConfig): Set<String> {
        val paths = config.paths
        val pathFile = config.pathFile
        val pathStrict = config.pathStrict
        val pathPatterns = config.pathPatterns
        val result = sortedSetOf<String>()
        if (paths.isNotEmpty()) {
            if (pathFile.isNotNullOrEmpty() && pathStrict) {
                result += paths.map { "$it/$pathFile" }
            } else {
                result += paths
            }
        } else if (pathFile.isNotNullOrEmpty()) {
            result += pathFile
        }
        if (pathPatterns.isNotEmpty()) {
            result += pathPatterns.map { it.substringBefore("/**", "").orNull() ?: it.substringBeforeLast("/") }
        }
        return result.optimized() // optimized to optimize memory
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

    // fun getConfigByPathExpression(configGroup: CwtConfigGroup, pathExpression: String): List<CwtMemberConfig<*>> {
    //     val separatorIndex = pathExpression.indexOf('#')
    //     if (separatorIndex == -1) return emptyList()
    //     val filePath = pathExpression.substring(0, separatorIndex)
    //     if (filePath.isEmpty()) return emptyList()
    //     val fileConfig = configGroup.files[filePath] ?: return emptyList()
    //     val configPath = pathExpression.substring(separatorIndex + 1)
    //     if (configPath.isEmpty()) return emptyList()
    //     val pathList = configPath.split('/')
    //     var r: List<CwtMemberConfig<*>> = emptyList()
    //     pathList.forEach { p ->
    //         if (p == "-") {
    //             if (r.isEmpty()) {
    //                 r = fileConfig.values
    //             } else {
    //                 r = buildList {
    //                     r.forEach { c1 ->
    //                         c1.configs?.forEach { c2 ->
    //                             if (c2 is CwtValueConfig) this += c2
    //                         }
    //                     }
    //                 }
    //             }
    //         } else {
    //             if (r.isEmpty()) {
    //                 r = fileConfig.properties.filter { c -> c.key == p }
    //             } else {
    //                 r = buildList {
    //                     r.forEach { c1 ->
    //                         c1.configs?.forEach { c2 ->
    //                             if (c2 is CwtPropertyConfig && c2.key == p) this += c2
    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //         if (r.isEmpty()) return emptyList()
    //     }
    //     return r
    // }

    fun isRemoved(config: CwtConfig<*>): Boolean {
        if (config !is CwtSingleAliasConfig && config !is CwtAliasConfig) return false
        return config.config.optionData { apiStatus } == CwtApiStatus.Removed
    }

    fun findLiterals(configs: List<CwtMemberConfig<*>>): Set<String> {
        val configGroup = configs.firstOrNull()?.configGroup ?: return emptySet()
        val result = mutableSetOf<String>()
        for (config in configs) {
            val configExpression = config.configExpression
            findLiterals(configExpression, configGroup, result)
        }
        return result
    }

    private fun findLiterals(configExpression: CwtDataExpression, configGroup: CwtConfigGroup, result: MutableSet<String>) {
        val dataType = configExpression.type
        when (dataType) {
            CwtDataTypes.Bool -> {
                result += "yes"
                result += "no"
            }
            CwtDataTypes.Constant -> {
                val v = configExpression.value ?: return
                result += v
            }
            CwtDataTypes.EnumValue -> {
                val name = configExpression.value ?: return
                val nextConfig = configGroup.enums[name] ?: return
                val values = nextConfig.values
                result += values
            }
            CwtDataTypes.AliasName, CwtDataTypes.AliasKeysField -> {
                val name = configExpression.value ?: return
                val aliasConfigGroup = configGroup.aliasGroups[name] ?: return
                withRecursionGuard { // 这里需要防止递归
                    for (aliasConfigs in aliasConfigGroup.values) {
                        val e = aliasConfigs.firstOrNull()?.configExpression ?: continue
                        withRecursionCheck(e) {
                            findLiterals(e, configGroup, result)
                        }
                    }
                }
            }
            CwtDataTypes.SingleAliasRight -> {
                val name = configExpression.value ?: return
                val singleAliasConfig = configGroup.singleAliases[name] ?: return
                withRecursionGuard { // 这里需要防止递归
                    val e = singleAliasConfig.config.valueExpression
                    withRecursionCheck(e) {
                        findLiterals(e, configGroup, result)
                    }
                }
            }
        }
    }

    fun getAliasKeys(configGroup: CwtConfigGroup, aliasName: String, key: String): Set<String> {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) // 不区分大小写
        if (constKey != null) return setOf(constKey)
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return emptySet()
        return keys
    }
}
