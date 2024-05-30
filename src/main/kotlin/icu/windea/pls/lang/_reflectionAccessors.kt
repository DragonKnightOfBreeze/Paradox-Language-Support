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
import icu.windea.pls.lang.*

val DefaultActionGroup.children: MutableList<AnAction> by memberProperty<DefaultActionGroup, _>("mySortedChildren")

val HierarchyBrowserBaseEx.project: Project by memberProperty<HierarchyBrowserBase, _>("myProject")
val HierarchyBrowserBaseEx.element: PsiElement by memberProperty<HierarchyBrowserBaseEx, _>("hierarchyBase")

val SearchRequestCollector.wordRequests: MutableList<PsiSearchRequest> by memberProperty<SearchRequestCollector, _>("myWordRequests")
val SearchRequestCollector.queryRequests: MutableList<QuerySearchRequest> by memberProperty<SearchRequestCollector, _>("myQueryRequests")

//com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl.getShreds
fun DocumentWindow.getShreds(): Place? {
    val function = memberFunction("getShreds", "com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl")
    return runCatchingCancelable { function(this) }.getOrNull()?.cast()
}

//com.intellij.codeInsight.documentation.DocumentationFontSize.getDocumentationFontSize
fun getDocumentationFontSize(): FontSize {
    val function = staticFunction("getDocumentationFontSize", "com.intellij.codeInsight.documentation.DocumentationFontSize")
    return runCatchingCancelable { function() }.getOrNull()?.cast() ?: FontSize.SMALL
}

//com.intellij.lang.documentation.psi.psiDocumentationTargets
fun psiDocumentationTargets(element: PsiElement, originalElement: PsiElement?): List<DocumentationTarget> {
    val function = staticFunction("psiDocumentationTargets", "com.intellij.lang.documentation.psi.UtilKt")
    return runCatchingCancelable { function(element, originalElement) }.getOrNull()?.cast() ?: emptyList()
}
