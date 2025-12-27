package icu.windea.pls.lang.util

import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

object ParadoxDefinitionInjectionManager {
    const val definitionInjectionKey = "definition_injection"

    // /**
    //  * 检测指定的游戏类型是否支持内联脚本。
    //  */
    // fun isSupported(gameType: ParadoxGameType?): Boolean {
    //     if (gameType == null) return false
    //     val configs = PlsFacade.getConfigGroup(gameType).inlineConfigGroup[inlineScriptKey]
    //     if (configs.isNullOrEmpty()) return false
    //     return true
    // }

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
        val mode = expression.substringBefore('.', "")
        return isSupported(mode, gameType)
    }

    /**
     * 检查内联脚本用法在 [element] 对应的位置是否可用（但不一定支持或正确）。这意味着至少会提供代码高亮。
     */
    fun isAvailable(element: ParadoxScriptProperty): Boolean {
        if (element.propertyValue !is ParadoxScriptBlock) return false // 属性的值必须是子句
        if (element.parent !is ParadoxScriptRootBlock) return false // 属性必须位于文件顶级（就目前看来）
        if (!ParadoxPsiFileMatcher.isScriptFile(element.containingFile)) return false // 额外检查
        // 这里目前不继续检查当前位置是否匹配任意定义类型
        return true // TODO 2.1.0
    }

    /**
     * 检查内联脚本用法在 [element] 对应的位置是否支持（但不一定正确）。这意味着会提供引用解析，但不会传递规则上下文。
     */
    fun isSupported(element: ParadoxScriptProperty): Boolean {
        return true // TODO 2.1.0
    }

    fun getInfo(element: ParadoxScriptProperty, gameType: ParadoxGameType? = selectGameType(element)): ParadoxDefinitionInjectionInfo? {
        if (gameType == null) return null
        val expression = element.name
        if (!isMatched(expression, gameType)) return null
        if (!isAvailable(element)) return null

        val mode = expression.substringBefore('.', "")
        if (mode.isEmpty()) return null
        val target = expression.substringAfter('.', "")
        val config = PlsFacade.getConfigGroup(gameType).macroConfigs[mode]
        if (config == null) return null
        val modeConfig = config.modeConfigs[mode]
        if (modeConfig == null) return null
        return ParadoxDefinitionInjectionInfo(mode, target, modeConfig)
    }
}
