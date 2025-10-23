package icu.windea.pls.lang.match

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.core.util.withOperator
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.stringValue
import org.jetbrains.kotlin.analysis.utils.printer.parentOfType

object ParadoxMatchManager {
    /**
     * 根据附加到 [config] 上的 `## predicate` 选项中的元数据，以及 [element] 所在的块（[icu.windea.pls.script.psi.ParadoxScriptBlockElement]）中的结构，进行简单的结构匹配。
     */
    fun matchesByPredicate(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        run {
            val predicate = config.optionData { predicate }
            if (predicate.isEmpty()) return@run
            val parentBlock = element.parentOfType<ParadoxScriptBlockElement>(withSelf = false) ?: return@run
            predicate.forEach f@{ (pk, pv) ->
                val p1 = parentBlock.findProperty(pk, inline = true)
                val pv1 = p1?.propertyValue?.stringValue()
                val pr = pv.withOperator { it == pv1 }
                if (!pr) return false
            }
        }
        return true
    }
}
