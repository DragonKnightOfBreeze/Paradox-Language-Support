package icu.windea.pls.ep.resolve

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.hierarchy.call.ParadoxCalleeHierarchyTreeStructure
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 提供对脚本片段的内联逻辑的支持。
 *
 * 嵌套内联是允许的，但是需要在实现中妥善处理。
 *
 * 这个扩展点目前主要用于以下功能：
 *
 * - 跨内联向下的查询。
 * - 跨内联向下的调用层次视图（[ParadoxCalleeHierarchyTreeStructure]）。
 */
interface ParadoxInlineSupport {
    /**
     * 从指定的定义成员得到需要被内联的 PSI。
     *
     * **注意**：需要避免递归。
     */
    fun getInlinedElement(element: ParadoxScriptMember): ParadoxScriptMember?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxInlineSupport>("icu.windea.pls.inlineSupport")
    }
}
