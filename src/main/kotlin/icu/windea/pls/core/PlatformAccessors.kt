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

/** 访问 `DefaultActionGroup.mySortedChildren`（内部字段）。*/
val DefaultActionGroup.children: MutableList<AnAction> by memberProperty<DefaultActionGroup, _>("mySortedChildren")

/** 访问 `HierarchyBrowserBaseEx.project`（内部字段）。*/
val HierarchyBrowserBaseEx.project: Project by memberProperty<HierarchyBrowserBase, _>("myProject")

/** 访问 `HierarchyBrowserBaseEx.hierarchyBase`（内部字段）。*/
val HierarchyBrowserBaseEx.element: PsiElement by memberProperty<HierarchyBrowserBaseEx, _>("hierarchyBase")

/** 访问 `SearchRequestCollectory.wordRequests`（内部字段）。*/
val SearchRequestCollector.wordRequests: MutableList<PsiSearchRequest> by memberProperty<SearchRequestCollector, _>("myWordRequests")
//val SearchRequestCollector.queryRequests: MutableList<QuerySearchRequest> by memberProperty<SearchRequestCollector, _>("myQueryRequests")

//com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl.getShreds
/** 调用 `DocumentWindowImpl.getShreds()`，返回语言注入片段集合。*/
fun DocumentWindow.getShreds(): Place? {
    val function = memberFunction("getShreds", "com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl")
    return runCatchingCancelable { function(this) }.getOrNull()?.cast()
}

//com.intellij.codeInsight.documentation.DocumentationFontSize.getDocumentationFontSize
/** 调用 `DocumentationFontSize.getDocumentationFontSize()`，获取文档窗口字体大小。失败时返回 [FontSize.SMALL]。*/
fun getDocumentationFontSize(): FontSize {
    val function = staticFunction("getDocumentationFontSize", "com.intellij.codeInsight.documentation.DocumentationFontSize")
    return runCatchingCancelable { function() }.getOrNull()?.cast() ?: FontSize.SMALL
}

//com.intellij.lang.documentation.psi.psiDocumentationTargets
/** 调用 `psiDocumentationTargets(element, originalElement)`，获取 PSI 文档目标列表。*/
fun psiDocumentationTargets(element: PsiElement, originalElement: PsiElement?): List<DocumentationTarget> {
    val function = staticFunction("psiDocumentationTargets", "com.intellij.lang.documentation.psi.UtilKt")
    return runCatchingCancelable { function(element, originalElement) }.getOrNull()?.cast() ?: emptyList()
}
