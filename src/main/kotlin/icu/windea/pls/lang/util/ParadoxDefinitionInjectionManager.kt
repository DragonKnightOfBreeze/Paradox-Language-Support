package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

object ParadoxDefinitionInjectionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInjectionInfo by createKey<CachedValue<ParadoxDefinitionInjectionInfo>>(Keys)
    }

    const val definitionInjectionKey = "definition_injection"

    /**
     * 检查指定的游戏类型是否支持指定模式的定义注入。
     */
    fun isSupported(mode: String, gameType: ParadoxGameType?): Boolean {
        if (gameType == null) return false
        if (mode.isEmpty()) return false
        val config = PlsFacade.getConfigGroup(gameType).macroConfigs[definitionInjectionKey]
        if (config == null) return false
        if (config.modeConfigs[mode] == null) return false // 这里忽略 `prefix` 的大小写
        return true
    }

    /**
     * 检查输入的字符串是否匹配定义注入的键。
     */
    fun isMatched(expression: String, gameType: ParadoxGameType?): Boolean {
        if (gameType == null) return false
        val mode = expression.substringBefore(':', "")
        return isSupported(mode, gameType)
    }

    /**
     * 检查内联脚本用法在 [element] 对应的位置是否可用（但不一定支持或正确）。这意味着至少会提供代码高亮。
     */
    fun isAvailable(element: ParadoxScriptProperty): Boolean {
        if (element.propertyValue !is ParadoxScriptBlock) return false // 属性的值必须是子句
        if (element.parent !is ParadoxScriptRootBlock) return false // 属性必须位于文件顶级（就目前看来）
        if (!ParadoxPsiFileMatcher.isScriptFile(element.containingFile)) return false // 额外检查
        return true // 这里目前不继续检查当前位置是否匹配任意定义类型
    }

    /**
     * 检查内联脚本用法在 [element] 对应的位置是否支持（但不一定正确）。这意味着会提供引用解析，但不会传递规则上下文。
     */
    fun isSupported(element: ParadoxScriptProperty): Boolean {
        return true // TODO 2.1.0
    }

    fun getInfo(element: ParadoxScriptProperty): ParadoxDefinitionInjectionInfo? {
        // mode must exist
        if (getModeFromExpression(element.name).isEmpty()) return null
        // get from cache
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxScriptProperty): ParadoxDefinitionInjectionInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInjectionInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = runReadActionSmartly { doGetInfo(element, file) }
            val tracker = ParadoxModificationTrackers.ScriptFile
            value.withDependencyItems(tracker)
        }
    }

    private fun doGetInfo(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefinitionInjectionInfo? {
        doGetInfoFromStub(element, file)?.let { return it }
        return doGetInfoFromPsi(element, file)
    }

    fun doGetInfoFromStub(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefinitionInjectionInfo? {
        val stub = getStub(element) ?: return null
        val mode = stub.mode
        val target = stub.definitionName
        val type = stub.definitionType
        val gameType = stub.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 project
        val macroConfig = configGroup.macroConfigs[definitionInjectionKey] ?: return null
        val modeConfig = macroConfig.modeConfigs[mode] ?: return null
        val typeConfig = configGroup.types[type] ?: return null
        return ParadoxDefinitionInjectionInfo(mode, target, type, modeConfig, typeConfig, gameType)
    }

    private fun doGetInfoFromPsi(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefinitionInjectionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType // 这里还是基于 `fileInfo` 获取 `gameType`
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 project
        val expression = element.name
        if (!isMatched(expression, gameType)) return null
        if (!isAvailable(element)) return null
        if (expression.isParameterized()) return null // 忽略带参数的情况
        val mode = getModeFromExpression(expression)
        if (mode.isEmpty()) return null
        val macroConfig = configGroup.macroConfigs[definitionInjectionKey] ?: return null
        val modeConfig = macroConfig.modeConfigs[mode] ?: return null
        val target = getTargetFromExpression(expression)
        if (target.isEmpty()) return null
        val path = fileInfo.path
        val elementPath = ParadoxElementPath.resolve(listOf(target))
        val typeKey = target
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfig(element, configGroup, path, elementPath, typeKey, null) ?: return null
        if (!canApply(typeConfig)) return null // 排除不期望匹配的类型规则
        val type = typeConfig.name
        return ParadoxDefinitionInjectionInfo(mode, target, type, modeConfig, typeConfig, gameType)
    }

    fun getModeFromExpression(expression: String): String {
        return expression.substringBefore(':', "")
    }

    fun getTargetFromExpression(expression: String): String {
        return expression.substringAfter(':', "")
    }

    fun getStub(element: ParadoxScriptProperty): ParadoxScriptPropertyStub.DefinitionInjection? {
        return element.greenStub?.castOrNull()
    }

    fun canApply(definitionInfo: ParadoxDefinitionInfo): Boolean {
        if (definitionInfo.name.isEmpty()) return false
        if (definitionInfo.type.isEmpty()) return false
        val typeConfig = definitionInfo.typeConfig
        if (!canApply(typeConfig)) return false
        return true
    }

    fun canApply(typeConfig: CwtTypeConfig): Boolean {
        if (typeConfig.nameField != null || typeConfig.skipRootKey != null) return false // 排除不期望匹配的类型规则
        return true
    }
}
