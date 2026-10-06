package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.getCachedValueOnDemand
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.resolve.ParadoxParameterContextService
import icu.windea.pls.script.psi.ParadoxScriptBlock

object ParadoxParameterContextManager {
    object Keys : KeyRegistry() {
        val cachedArguments by registerKey<CachedValue<List<Tuple2<String, String>>>>(Keys)
    }

    /**
     * 以 [element] 为参数上下文，得到其中的所有参数信息列表。
     *
     * 如果指定了 [excludeNames]，则排除匹配参数名的参数信息。
     *
     * @param element 作为参数上下文的 [PsiElement]。
     */
    fun getArguments(element: ParadoxScriptBlock, vararg excludeNames: String): List<Tuple2<String, String>> {
        val r = getArgumentsInternal(element)
        return if (excludeNames.isEmpty()) r else r.filter { (k) -> k !in excludeNames }
    }

    private fun getArgumentsInternal(element: ParadoxScriptBlock): List<Tuple2<String, String>> {
        return getCachedValueOnDemand(element, Keys.cachedArguments, ChronicleCapabilities.Cache.arguments) {
            runSmartReadAction {
                val value = ParadoxParameterContextService.resolveArguments(element).optimized()
                CachedValueProvider.Result.create(value, element)
            }
        }
    }

    /**
     * 得到 [element] 的文本，然后使用指定的一组 [arguments] 替换其中的占位符。
     *
     * 如果 [direct] 为 `true`，则直接将占位符 `$PARAM$` 替换成传入参数 `PARAM` 的值。此时：
     * - 值可以是多行字符串。
     * - 如果值是用双引号括起，替换时会被忽略。
     * - 允许重复的传入参数，按顺序进行替换。
     *
     * @param element 用于得到原始文本的 [PsiElement]。
     * @param arguments 传入参数的键值对。如果值是用双引号括起的，需要保留。
     */
    fun replaceTextWithArgs(element: PsiElement, arguments: List<Tuple2<String, String>>, direct: Boolean): String {
        return ParadoxParameterContextService.replaceTextWithArgs(element, arguments, direct)
    }
}
