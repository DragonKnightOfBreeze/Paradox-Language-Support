package icu.windea.pls.lang.util

import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.util.ParadoxInlineScriptManager.inlineScriptKey
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

object ParadoxDefinitionInjectionManager {
    const val definitionInjectionKey = "definition_injection"

    /**
     * 检查输入的字符串是否匹配定义注入表达式。
     */
    fun isMatched(expression: String, gameType: ParadoxGameType? = null): Boolean {
        val prefix = expression.substringBefore('.', "")
        if (prefix.isEmpty()) return false
        if (gameType != null) {
            val config = PlsFacade.getConfigGroup(gameType).macroConfigs[inlineScriptKey]
            if (config == null) return false
            if (config.modeConfigs[prefix] == null) return false // 这里忽略 `prefix` 的大小写
        }
        return true
    }

    /**
     * 检查内联脚本用法在 [element] 对应的位置是否可用（但不一定支持或正确）。
     */
    fun isAvailable(element: ParadoxScriptPropertyKey): Boolean {
        return true // TODO 2.1.0
    }

    /**
     * 检查内联脚本用法在 [element] 对应的位置是否支持（但不一定正确）。
     */
    fun isSupported(element: ParadoxScriptPropertyKey): Boolean {
        return true // TODO 2.1.0
    }
}
