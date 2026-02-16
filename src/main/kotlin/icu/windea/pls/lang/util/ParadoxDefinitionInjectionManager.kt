package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.orDefault
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.resolve.ParadoxDefinitionInjectionService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

@Suppress("unused")
object ParadoxDefinitionInjectionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInjectionInfo by registerKey<CachedValue<ParadoxDefinitionInjectionInfo>>(Keys)
        val cachedDefinitionInjectionSubtypeConfigs by registerKey<CachedValue<List<CwtSubtypeConfig>>>(Keys)
        val cachedDefinitionInjectionDeclaration by registerKey<CachedValue<Any>>(Keys) // Any: CwtPropertyConfig | EMPTY_OBJECT
    }

    /**
     * 检查指定的游戏类型是否支持定义注入。
     */
    fun isSupported(gameType: ParadoxGameType?): Boolean {
        if (gameType == null) return false
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val config = configGroup.directivesModel.definitionInjection
        if (config == null) return false
        return true
    }

    /**
     * 检查指定的游戏类型是否支持指定模式的定义注入。
     */
    fun isSupported(mode: String, gameType: ParadoxGameType?): Boolean {
        if (gameType == null) return false
        if (mode.isEmpty()) return false
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val config = configGroup.directivesModel.definitionInjection
        if (config == null) return false
        if (config.modeConfigs[mode] == null) return false // 这里忽略 `prefix` 的大小写
        return true
    }

    /**
     * 检查输入的字符串是否匹配定义注入的键（会从 [context] 选取游戏类型并检查）。
     */
    fun isMatched(expression: String, context: Any?): Boolean {
        if (context == null) return false
        val mode = expression.substringBefore(':', "")
        if (mode.isEmpty()) return false
        return isSupported(mode, selectGameType(context))
    }

    /**
     * 检查定义注入在 [element] 对应的位置是否可用（不一定实际受游戏支持，格式也不一定正确）。这意味着至少会提供代码高亮。
     */
    fun isAvailable(element: ParadoxScriptProperty): Boolean {
        if (element.parent !is ParadoxScriptRootBlock) return false // 属性必须位于文件顶级（就目前看来）
        val propertyValue = element.propertyValue ?: return false
        if (propertyValue !is ParadoxScriptBlock) return false // 属性的值必须是子句
        val file = element.containingFile ?: return false
        if (!ParadoxPsiFileMatcher.isScriptFile(file, ParadoxPathConstraint.AcceptDefinitionInjection)) return false // 额外检查
        // 这里目前不继续检查当前位置是否匹配任意定义类型
        return true
    }

    fun getModeFromExpression(expression: String): String? {
        val index = expression.indexOf(':')
        if (index == -1) return null
        return expression.substring(0, index)
    }

    fun getTargetFromExpression(expression: String): String? {
        val index = expression.indexOf(':')
        if (index == -1) return null
        return expression.substring(index + 1)
    }

    fun getExpression(mode: String, target: String? = null): String {
        return mode + ":" + target.orEmpty()
    }

    fun getTarget(element: ParadoxScriptProperty): String? {
        return element.definitionInjectionInfo?.target
    }

    fun getType(element: ParadoxScriptProperty): String? {
        return element.definitionInjectionInfo?.type
    }

    fun getInfo(element: ParadoxScriptProperty): ParadoxDefinitionInjectionInfo? {
        // mode must exist
        if (getModeFromExpression(element.name).isNullOrEmpty()) return null
        // from cache
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInjectionInfo) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val file = element.containingFile
                val value = ParadoxDefinitionInjectionService.resolveInfo(element, file)
                val dependencies = ParadoxDefinitionInjectionService.getDependencies(element, file)
                value.withDependencyItems(dependencies)
            }
        }
    }

    fun getSubtypeConfigs(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): List<CwtSubtypeConfig> {
        val candidates = definitionInjectionInfo.typeConfig?.subtypes
        if (candidates.isNullOrEmpty()) return emptyList()
        val element = definitionInjectionInfo.element ?: return emptyList()
        val finalOptions = options.orDefault()
        return if (finalOptions == ParadoxMatchOptions.DEFAULT) {
            // 经过缓存
            CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInjectionSubtypeConfigs) {
                ProgressManager.checkCanceled()
                runReadActionSmartly {
                    val value = ParadoxDefinitionInjectionService.resolveSubtypeConfigs(definitionInjectionInfo).optimized()
                    val dependencies = ParadoxDefinitionInjectionService.getSubtypeAwareDependencies(element, definitionInjectionInfo)
                    value.withDependencyItems(dependencies)
                }
            }
        } else {
            // 不经过缓存
            runReadActionSmartly {
                ParadoxDefinitionInjectionService.resolveSubtypeConfigs(definitionInjectionInfo, options).optimized()
            }
        }
    }

    fun getDeclaration(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, options: ParadoxMatchOptions? = null): CwtPropertyConfig? {
        val element = definitionInjectionInfo.element ?: return null
        val finalOptions = options.orDefault()
        return if (finalOptions == ParadoxMatchOptions.DEFAULT) {
            // 经过缓存
            CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInjectionDeclaration) {
                ProgressManager.checkCanceled()
                runReadActionSmartly {
                    val value = ParadoxDefinitionInjectionService.resolveDeclaration(definitionInjectionInfo, null) ?: EMPTY_OBJECT
                    val dependencies = ParadoxDefinitionInjectionService.getSubtypeAwareDependencies(element, definitionInjectionInfo)
                    value.withDependencyItems(dependencies)
                }
            }.castOrNull()
        } else {
            // 不经过缓存
            runReadActionSmartly {
                ParadoxDefinitionInjectionService.resolveDeclaration(definitionInjectionInfo, options)
            }
        }
    }

    fun isRelaxMode(definitionInjectionInfo: ParadoxDefinitionInjectionInfo): Boolean {
        val mode = definitionInjectionInfo.mode
        val gameType = definitionInjectionInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val config = configGroup.directivesModel.definitionInjection ?: return false
        return mode in config.relaxModes
    }

    fun isReplaceMode(definitionInjectionInfo: ParadoxDefinitionInjectionInfo): Boolean {
        val mode = definitionInjectionInfo.mode
        val gameType = definitionInjectionInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val config = configGroup.directivesModel.definitionInjection ?: return false
        return mode in config.replaceModes
    }

    /**
     * 检查定义注入是否应被识别为定义声明（可以被索引和搜索）。
     * 这适用于 REPLACE_OR_CREATE 等模式。
     */
    fun isDefinitionMode(definitionInjectionInfo: ParadoxDefinitionInjectionInfo): Boolean {
        val mode = definitionInjectionInfo.mode
        val gameType = definitionInjectionInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val config = configGroup.directivesModel.definitionInjection ?: return false
        return mode in config.createModes
    }

    /**
     * 检查指定模式是否应被识别为定义声明（可以被索引和搜索）。
     */
    fun isDefinitionMode(mode: String, gameType: ParadoxGameType?): Boolean {
        if (gameType == null) return false
        if (mode.isEmpty()) return false
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val config = configGroup.directivesModel.definitionInjection ?: return false
        return mode in config.createModes
    }

    fun isTargetExist(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, context: Any? = null): Boolean {
        if (definitionInjectionInfo.target.isNullOrEmpty()) return false
        if (definitionInjectionInfo.type.isNullOrEmpty()) return false
        if (definitionInjectionInfo.typeConfig == null) return false
        val name = definitionInjectionInfo.target
        val typeExpression = definitionInjectionInfo.type
        val selector = selector(definitionInjectionInfo.project, context).definition()
        return ParadoxDefinitionSearch.searchProperty(name, typeExpression, selector).findFirst() != null
    }

    fun canApply(definitionInfo: ParadoxDefinitionInfo): Boolean {
        if (definitionInfo.name.isEmpty()) return false
        if (definitionInfo.type.isEmpty()) return false
        if (!ParadoxConfigMatchService.canApplyForInjection(definitionInfo.typeConfig)) return false // 排除不期望匹配的类型规则
        return true
    }
}
