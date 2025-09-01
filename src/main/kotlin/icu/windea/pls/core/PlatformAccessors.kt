package icu.windea.pls.core

import com.intellij.ide.hierarchy.*
import com.intellij.injected.editor.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*

/** 访问 `DefaultActionGroup` 的排序后动作列表。 */
val DefaultActionGroup.children: MutableList<AnAction> by memberProperty<DefaultActionGroup, _>("mySortedChildren")

/** 访问层级浏览器的项目对象。 */
val HierarchyBrowserBaseEx.project: Project by memberProperty<HierarchyBrowserBase, _>("myProject")
/** 访问层级浏览器的根元素。 */
val HierarchyBrowserBaseEx.element: PsiElement by memberProperty<HierarchyBrowserBaseEx, _>("hierarchyBase")

/** 访问 `SearchRequestCollector` 已收集的词级请求。 */
val SearchRequestCollector.wordRequests: MutableList<PsiSearchRequest> by memberProperty<SearchRequestCollector, _>("myWordRequests")
//val SearchRequestCollector.queryRequests: MutableList<QuerySearchRequest> by memberProperty<SearchRequestCollector, _>("myQueryRequests")

//com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl.getShreds
/** 取得注入文档的分片（Shreds），失败时返回 null。 */
fun DocumentWindow.getShreds(): Place? {
    val function = memberFunction("getShreds", "com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl")
    return runCatchingCancelable { function(this) }.getOrNull()?.cast()
}

//com.intellij.codeInsight.documentation.DocumentationFontSize.getDocumentationFontSize
/** 获取 IDE 文档视图的字体大小，失败时回退为 SMALL。 */
fun getDocumentationFontSize(): FontSize {
    val function = staticFunction("getDocumentationFontSize", "com.intellij.codeInsight.documentation.DocumentationFontSize")
    return runCatchingCancelable { function() }.getOrNull()?.cast() ?: FontSize.SMALL
}

//com.intellij.lang.documentation.psi.psiDocumentationTargets
/** 获取 PSI 的文档目标集合（支持 originalElement 作为上下文）。 */
fun psiDocumentationTargets(element: PsiElement, originalElement: PsiElement?): List<DocumentationTarget> {
    val function = staticFunction("psiDocumentationTargets", "com.intellij.lang.documentation.psi.UtilKt")
    return runCatchingCancelable { function(element, originalElement) }.getOrNull()?.cast() ?: emptyList()
}
