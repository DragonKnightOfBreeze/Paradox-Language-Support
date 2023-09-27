package icu.windea.pls.core

import com.intellij.ide.hierarchy.*
import com.intellij.injected.editor.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.*
import com.intellij.psi.search.*

val DefaultActionGroup.children: MutableList<AnAction> by memberProperty<DefaultActionGroup, _>("mySortedChildren")

val HierarchyBrowserBaseEx.project: Project by memberProperty<HierarchyBrowserBase, _>("myProject")
val HierarchyBrowserBaseEx.element: PsiElement by memberProperty<HierarchyBrowserBaseEx, _>("hierarchyBase")

val SearchRequestCollector.wordRequests: MutableList<PsiSearchRequest> by memberProperty<SearchRequestCollector, _>("myWordRequests")
val SearchRequestCollector.queryRequests: MutableList<QuerySearchRequest> by memberProperty<SearchRequestCollector, _>("myQueryRequests")

//com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl.getShreds
private val DocumentWindow_getShreds = memberFunction("getShreds", "com.intellij.psi.impl.source.tree.injected.DocumentWindowImpl")
fun DocumentWindow.getShreds(): Place? = runCatchingCancelable { DocumentWindow_getShreds(this) }.getOrNull()?.cast()

//com.intellij.codeInsight.documentation.DocumentationFontSize.getDocumentationFontSize
private val _getDocumentationFontSize = staticFunction("getDocumentationFontSize", "com.intellij.codeInsight.documentation.DocumentationFontSize")
fun getDocumentationFontSize(): FontSize = runCatchingCancelable {  _getDocumentationFontSize() }.getOrNull()?.cast() ?: FontSize.SMALL