package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.resolve.ParadoxDefinitionInjectionService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

object ParadoxDefinitionInjectionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInjectionInfo by registerKey<CachedValue<ParadoxDefinitionInjectionInfo>>(Keys)
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
        if (element.parent !is ParadoxScriptRootBlock) return false // 属性必须位于文件顶级（就目前看来）
        if (element.propertyValue !is ParadoxScriptBlock) return false // 属性的值必须是子句
        if (!ParadoxPsiFileMatcher.isScriptFile(element.containingFile)) return false // 额外检查
        return true // 这里目前不继续检查当前位置是否匹配任意定义类型
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

    fun getInfo(element: ParadoxScriptProperty): ParadoxDefinitionInjectionInfo? {
        // mode must exist
        if (getModeFromExpression(element.name).isNullOrEmpty()) return null
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
        val target = stub.target
        val type = stub.type
        val gameType = stub.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 project
        val config = configGroup.directivesModel.definitionInjection ?: return null
        val modeConfig = config.modeConfigs[mode] ?: return null
        val typeConfig = configGroup.types[type]
        return ParadoxDefinitionInjectionInfo(mode, target, type, modeConfig, typeConfig)
    }

    private fun doGetInfoFromPsi(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefinitionInjectionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType // 这里还是基于 `fileInfo` 获取 `gameType`
        val expression = element.name
        if (!isMatched(expression, gameType)) return null
        if (!isAvailable(element)) return null
        if (expression.isParameterized()) return null // 忽略带参数的情况
        val mode = getModeFromExpression(expression)
        if (mode.isNullOrEmpty()) return null
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 project
        val config = configGroup.directivesModel.definitionInjection ?: return null
        val modeConfig = config.modeConfigs[mode] ?: return null
        val target = getTargetFromExpression(expression)
        run {
            if (target.isNullOrEmpty()) return@run
            val path = fileInfo.path
            val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfigForInjection(configGroup, path) ?: return@run
            val type = typeConfig.name
            return ParadoxDefinitionInjectionInfo(mode, target, type, modeConfig, typeConfig)
        }
        // 兼容目标为空或者目标类型无法解析的情况
        return ParadoxDefinitionInjectionInfo(mode, target, null, modeConfig, null)
    }

    @Suppress("unused")
    fun getTarget(element: ParadoxScriptProperty): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.target }
        return element.definitionInjectionInfo?.target
    }

    fun getType(element: ParadoxScriptProperty): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.type }
        return element.definitionInjectionInfo?.type
    }

    fun getStub(element: ParadoxScriptProperty): ParadoxScriptPropertyStub.DefinitionInjection? {
        return element.greenStub?.castOrNull()
    }

    fun canApply(definitionInfo: ParadoxDefinitionInfo): Boolean {
        if (definitionInfo.name.isEmpty()) return false
        if (definitionInfo.type.isEmpty()) return false
        if (!ParadoxConfigMatchService.canApplyForInjection(definitionInfo.typeConfig)) return false // 排除不期望匹配的类型规则
        return true
    }

    fun getDeclaration(element: PsiElement, definitionInjectionInfo: ParadoxDefinitionInjectionInfo): CwtPropertyConfig? {
        return ParadoxDefinitionInjectionService.resolveDeclaration(element, definitionInjectionInfo)
    }

    fun isRelaxMode(definitionInjectionInfo: ParadoxDefinitionInjectionInfo): Boolean {
        val mode = definitionInjectionInfo.mode
        val gameType = definitionInjectionInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(gameType)
        val config = configGroup.directivesModel.definitionInjection ?: return false
        return mode in config.relaxModes
    }

    fun isTargetExist(definitionInjectionInfo: ParadoxDefinitionInjectionInfo, context: Any? = null): Boolean {
        if (definitionInjectionInfo.target.isNullOrEmpty()) return false
        if (definitionInjectionInfo.type.isNullOrEmpty()) return false
        if (definitionInjectionInfo.typeConfig == null) return false
        val name = definitionInjectionInfo.target
        val typeExpression = definitionInjectionInfo.type
        val selector = selector(definitionInjectionInfo.project, context).definition()
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
    }
}
