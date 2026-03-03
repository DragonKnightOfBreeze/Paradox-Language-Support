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

    @Optimized
    fun resolveConfigType(element: CwtMember, file: PsiFile): CwtConfigType? {
        if (element !is CwtProperty && element !is CwtValue) return null
        if (isInternalFile(file)) return null // 排除内部规则文件
        val configPath = getConfigPath(element) ?: return null
        if (configPath.isEmpty()) return null

        val isProperty = element is CwtProperty
        val length = configPath.length
        val s0 = configPath.get(0)

        // depth 1: single_alias[*], alias[*], directive[*]
        if (length == 1 && isProperty) {
            return when {
                s0.surroundsWith("single_alias[", "]") -> CwtConfigTypes.SingleAlias
                s0.surroundsWith("alias[", "]") -> {
                    val aliasName = s0.substringIn('[', ']', "").substringBefore(':', "")
                    when (aliasName) {
                        "modifier" -> CwtConfigTypes.Modifier
                        "trigger" -> CwtConfigTypes.Trigger
                        "effect" -> CwtConfigTypes.Effect
                        else -> CwtConfigTypes.Alias
                    }
                }
                s0.surroundsWith("directive[", "]") -> CwtConfigTypes.Directive
                else -> null
            }
        }

        // depth 2+: 基于首段精确分发
        return when (s0) {
            "types" -> {
                if (!isProperty) return null
                val s1 = configPath.get(1)
                if (!s1.surroundsWith("type[", "]")) return null
                when (length) {
                    2 -> CwtConfigTypes.Type
                    else -> {
                        val s2 = configPath.get(2)
                        when {
                            length == 3 && s2.surroundsWith("subtype[", "]") -> CwtConfigTypes.Subtype
                            s2 == "modifiers" -> {
                                val s3 = configPath.get(3)
                                when {
                                    s3.surroundsWith("subtype[", "]") -> if (length == 5) CwtConfigTypes.Modifier else null
                                    else -> if (length == 4) CwtConfigTypes.Modifier else null
                                }
                            }
                            else -> null
                        }
                    }
                }
            }
            "rows" -> if (isProperty && length == 2 && configPath.get(1).surroundsWith("row[", "]")) CwtConfigTypes.Row else null
            "enums" -> {
                val s1 = configPath.get(1)
                when {
                    s1.surroundsWith("enum[", "]") -> when {
                        isProperty && length == 2 -> CwtConfigTypes.Enum
                        !isProperty && length == 3 -> CwtConfigTypes.EnumValue
                        else -> null
                    }
                    s1.surroundsWith("complex_enum[", "]") -> if (isProperty && length == 2) CwtConfigTypes.ComplexEnum else null
                    else -> null
                }
            }
            "values" -> {
                val s1 = configPath.get(1)
                if (!s1.surroundsWith("value[", "]")) return null
                when {
                    isProperty && length == 2 -> CwtConfigTypes.DynamicValueType
                    !isProperty && length == 3 -> CwtConfigTypes.DynamicValue
                    else -> null
                }
            }
            // isProperty + length == 2
            "links" -> if (isProperty && length == 2) CwtConfigTypes.Link else null
            "localisation_links" -> if (isProperty && length == 2) CwtConfigTypes.LocalisationLink else null
            "localisation_promotions" -> if (isProperty && length == 2) CwtConfigTypes.LocalisationPromotion else null
            "localisation_commands" -> if (isProperty && length == 2) CwtConfigTypes.LocalisationCommand else null
            "modifier_categories" -> if (isProperty && length == 2) CwtConfigTypes.ModifierCategory else null
            "modifiers" -> if (isProperty && length == 2) CwtConfigTypes.Modifier else null
            "scopes" -> if (isProperty && length == 2) CwtConfigTypes.Scope else null
            "scope_groups" -> if (isProperty && length == 2) CwtConfigTypes.ScopeGroup else null
            "database_object_types" -> if (isProperty && length == 2) CwtConfigTypes.DatabaseObjectType else null
            "system_scopes" -> if (isProperty && length == 2) CwtConfigTypes.SystemScope else null
            "locales" -> if (isProperty && length == 2) CwtConfigTypes.Locale else null
            // extended: length == 2, 不检查元素类型
            "scripted_variables" -> if (length == 2) CwtConfigTypes.ExtendedScriptedVariable else null
            "definitions" -> if (length == 2) CwtConfigTypes.ExtendedDefinition else null
            "game_rules" -> if (length == 2) CwtConfigTypes.ExtendedGameRule else null
            "on_actions" -> if (length == 2) CwtConfigTypes.ExtendedOnAction else null
            "inline_scripts" -> if (length == 2) CwtConfigTypes.ExtendedInlineScript else null
            "parameters" -> if (length == 2) CwtConfigTypes.ExtendedParameter else null
            // extended: length == 3, 不检查元素类型
            "complex_enum_values" -> if (length == 3) CwtConfigTypes.ExtendedComplexEnumValue else null
            "dynamic_values" -> if (length == 3) CwtConfigTypes.ExtendedDynamicValue else null
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
