package icu.windea.pls.lang

import com.intellij.ide.hierarchy.*
import com.intellij.injected.editor.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*

val DefaultActionGroup.children: MutableList<AnAction> by memberProperty<DefaultActionGroup, _>("mySortedChildren")

val HierarchyBrowserBaseEx.project: Project by memberProperty<HierarchyBrowserBase, _>("myProject")
val HierarchyBrowserBaseEx.element: PsiElement by memberProperty<HierarchyBrowserBaseEx, _>("hierarchyBase")

val SearchRequestCollector.wordRequests: MutableList<PsiSearchRequest> by memberProperty<SearchRequestCollector, _>("myWordRequests")
val SearchRequestCollector.queryRequests: MutableList<QuerySearchRequest> by memberProperty<SearchRequestCollector, _>("myQueryRequests")

//com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl.getShreds
private val DocumentWindow_getShreds = memberFunction("getShreds", "com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl")
fun DocumentWindow.getShreds(): Place? {
    return runCatchingCancelable { DocumentWindow_getShreds(this) }.getOrNull()?.cast()
}

//com.intellij.codeInsight.documentation.DocumentationFontSize.getDocumentationFontSize
private val _getDocumentationFontSize = staticFunction("getDocumentationFontSize", "com.intellij.codeInsight.documentation.DocumentationFontSize")
fun getDocumentationFontSize(): FontSize {
    return runCatchingCancelable { _getDocumentationFontSize() }.getOrNull()?.cast() ?: FontSize.SMALL
}

//com.intellij.lang.documentation.psi.psiDocumentationTargets
private val _psiDocumentationTargets = staticFunction("psiDocumentationTargets", "com.intellij.lang.documentation.psi.UtilsKt")
fun psiDocumentationTargets(element: PsiElement, originalElement: PsiElement?): List<DocumentationTarget> {
    return runCatchingCancelable { _psiDocumentationTargets(element, originalElement) }.getOrNull()?.cast() ?: emptyList()
}
