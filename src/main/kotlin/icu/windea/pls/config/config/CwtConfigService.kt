package icu.windea.pls.config.config

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.config.delegated.CwtFilePathMatchableConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigManager.Keys
import icu.windea.pls.config.util.CwtConfigManager.getConfigPath
import icu.windea.pls.config.util.CwtConfigManager.isInternalFile
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.executeCommand
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.substringIn
import icu.windea.pls.core.substringInLast
import icu.windea.pls.core.surroundsWith
import icu.windea.pls.core.toPsiDirectory
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue
import icu.windea.pls.ep.config.config.CwtConfigPostProcessor
import icu.windea.pls.ep.config.config.CwtInjectedConfigProvider
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.lang.psi.CwtPsiManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.CwtConfigPath

object CwtConfigService {
    /**
     * @see CwtConfigPostProcessor.postProcess
     */
    @Optimized
    fun postProcess(config: CwtMemberConfig<*>) {
        val eps = CwtConfigPostProcessor.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (!ep.supports(config)) return@f
            if (ep.deferred(config)) {
                val deferredActions = CwtConfigResolverManager.getPostProcessActions(config.configGroup)
                deferredActions += Runnable { ep.postProcess(config) }
            } else {
                ep.postProcess(config)
            }
        }
    }

    /**
     * @see CwtInjectedConfigProvider.injectConfigs
     */
    @Optimized
    fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        var r = false
        val eps = CwtInjectedConfigProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (!ep.supports(parentConfig)) return@f
            r = r || ep.injectConfigs(parentConfig, configs)
        }
        return r
    }

    fun getContainingConfigGroupFromFileProviders(file: VirtualFile, project: Project): CwtConfigGroup? {
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroup = fileProviders.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(file, project)
        }
        return configGroup
    }

    fun getContainingConfigGroupForRepo(file: VirtualFile, project: Project): CwtConfigGroup? {
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
            ProgressManager.checkCanceled()
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

    fun resolveFilePath(file: VirtualFile, configGroup: CwtConfigGroup): @NlsSafe String? {
        val project = configGroup.project
        val gameType = configGroup.gameType
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType) ?: return@f
            val directory = rootDirectory.findChild(directoryName) ?: return@f
            val relativePath = VfsUtil.getRelativePath(file, directory) ?: return@f
            return relativePath
        }
        return null
    }

    fun resolveConfigPath(element: CwtMember): CwtConfigPath? {
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

    fun resolveConfigType(element: CwtMember, file: PsiFile): CwtConfigType? {
        if (element !is CwtProperty && element !is CwtValue) return null
        if (isInternalFile(file)) return null // 排除内部规则文件
        val configPath = getConfigPath(element) ?: return null
        if (configPath.isEmpty()) return null

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
            element is CwtProperty && configPath.path.matchesAntPattern("directive[*]") -> {
                CwtConfigTypes.Directive
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

    fun resolveNameByConfigType(text: String, configType: CwtConfigType): String? {
        return when (configType) {
            CwtConfigTypes.Type -> text.removeSurroundingOrNull("type[", "]")
            CwtConfigTypes.Subtype -> text.removeSurroundingOrNull("subtype[", "]")
            CwtConfigTypes.Row -> text.removeSurroundingOrNull("row[", "]")
            CwtConfigTypes.Enum -> text.removeSurroundingOrNull("enum[", "]")
            CwtConfigTypes.ComplexEnum -> text.removeSurroundingOrNull("complex_enum[", "]")
            CwtConfigTypes.DynamicValueType -> text.removeSurroundingOrNull("value[", "]")
            CwtConfigTypes.SingleAlias -> text.removeSurroundingOrNull("single_alias[", "]")
            CwtConfigTypes.Alias -> text.removeSurroundingOrNull("alias[", "]")
            CwtConfigTypes.Trigger -> text.removeSurroundingOrNull("alias[trigger:", "]")
            CwtConfigTypes.Effect -> text.removeSurroundingOrNull("alias[effect:", "]")
            CwtConfigTypes.Modifier -> text.removeSurroundingOrNull("alias[modifier:", "]") ?: text
            CwtConfigTypes.Directive -> text.removeSurroundingOrNull("directive[", "]")
            else -> text
        }?.orNull()
    }

    fun getDocumentation(element: CwtMember): String? {
        val ownedComments = CwtPsiManager.getOwnedComments(element)
        val documentation = CwtPsiManager.getDocCommentText(ownedComments, "<br>")
        return documentation
    }

    fun getFilePathPatterns(config: CwtFilePathMatchableConfig): Set<String> {
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
        return result
    }

    fun getFilePathPatternsForPriority(config: CwtFilePathMatchableConfig): Set<String> {
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
        return result
    }
}
