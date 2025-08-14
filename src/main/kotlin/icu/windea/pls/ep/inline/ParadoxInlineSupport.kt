package icu.windea.pls.ep.inline

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本片段的内联逻辑的支持。
 *
 * 注意嵌套内联目前是允许的，但需要特别处理递归嵌套内联的情况。
 *
 * 目前仅限内联脚本。
 */
@WithGameTypeEP
interface ParadoxInlineSupport {
    fun getInlinedElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxInlineSupport>("icu.windea.pls.inlineSupport")

        fun getInlinedElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
            val gameType = selectGameType(element)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getInlinedElement(element)
            }
        }
    }
}

