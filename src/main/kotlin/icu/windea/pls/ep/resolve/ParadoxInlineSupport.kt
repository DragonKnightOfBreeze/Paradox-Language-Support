package icu.windea.pls.ep.resolve

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.hierarchy.call.ParadoxCalleeHierarchyTreeStructure
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.script.psi.ParadoxScriptMemberElement

/**
 * 用于提供对脚本片段的内联逻辑的支持。
 *
 * 嵌套内联是允许的，但是需要在实现中妥善处理。
 *
 * 这个扩展点目前主要用于以下功能：
 *
 * - 跨内联向下的查询。
 * - 跨内联向下的调用层次视图（[ParadoxCalleeHierarchyTreeStructure]）。
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
