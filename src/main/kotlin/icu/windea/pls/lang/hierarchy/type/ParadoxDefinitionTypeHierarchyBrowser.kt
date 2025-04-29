package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.script.psi.*
import java.text.*
import java.util.function.*
import javax.swing.*

class ParadoxDefinitionTypeHierarchyBrowser(project: Project, element: PsiElement) : HierarchyBrowserBaseEx(project, element) {
    companion object {
        @Suppress("InvalidBundleOrProperty")
        fun getDefinitionHierarchyType() = PlsBundle.message("title.hierarchy.definition")
        @Suppress("InvalidBundleOrProperty")
        fun getDefinitionHierarchyTypeWithSubtypes() = PlsBundle.message("title.hierarchy.definition.with.subtypes")
    }

    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        val tree1 = createTree(true)
        PopupHandler.installPopupMenu(tree1, PlsActions.DefinitionHierarchyPopupMenu, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP)
        trees.put(getDefinitionHierarchyType(), tree1)

        val tree2 = createTree(true)
        PopupHandler.installPopupMenu(tree2, PlsActions.DefinitionHierarchyPopupMenu, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP)
        trees.put(getDefinitionHierarchyTypeWithSubtypes(), tree2)
    }

    override fun createHierarchyTreeStructure(type: String, psiElement: PsiElement): HierarchyTreeStructure? {
        return when (type) {
            getDefinitionHierarchyType() -> {
                val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
                val typeConfig = definitionInfo.typeConfig
                val typeElement = typeConfig.pointer.element ?: return null
                ParadoxDefinitionTypeHierarchyTreeStructure(myProject, psiElement, typeElement, typeConfig, false)
            }
            getDefinitionHierarchyTypeWithSubtypes() -> {
                val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
                val typeConfig = definitionInfo.typeConfig
                val typeElement = typeConfig.pointer.element ?: return null
                ParadoxDefinitionTypeHierarchyTreeStructure(myProject, psiElement, typeElement, typeConfig, true)
            }
            else -> null
        }
    }

    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is CwtProperty || element is ParadoxScriptDefinitionElement
    }

    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
        val type = definitionInfo.type
        return MessageFormat.format(typeName, type)
    }

    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        return descriptor.psiElement
    }

    override fun getComparator(): Comparator<NodeDescriptor<*>>? {
        return ParadoxHierarchyManager.getComparator(myProject)
    }

    override fun getPresentableNameMap(): MutableMap<String, Supplier<String>> {
        val map = mutableMapOf<String, Supplier<String>>()
        map.put(getDefinitionHierarchyType()) { getDefinitionHierarchyType() }
        map.put(getDefinitionHierarchyTypeWithSubtypes()) { getDefinitionHierarchyTypeWithSubtypes() }
        return map
    }

    override fun createLegendPanel(): JPanel? {
        return null
    }

    override fun getActionPlace(): String {
        return ActionPlaces.TYPE_HIERARCHY_VIEW_TOOLBAR
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun prependActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(ViewDefinitionHierarchyAction())
        actionGroup.add(ViewDefinitionHierarchyWithSubtypesAction())
        actionGroup.add(AlphaSortAction())
        actionGroup.add(ChangeScopeTypeAction(this, getHierarchySettings()))
    }

    override fun getPreviousOccurenceActionName(): String {
        return prevOccurenceActionNameImpl
    }

    override fun getPrevOccurenceActionNameImpl(): String {
        return PlsBundle.message("hierarchy.definition.prev.occurrence.name")
    }

    override fun getNextOccurenceActionName(): String {
        return nextOccurenceActionNameImpl
    }

    override fun getNextOccurenceActionNameImpl(): String {
        return PlsBundle.message("hierarchy.definition.next.occurrence.name")
    }

    private fun getHierarchySettings() = ParadoxDefinitionTypeHierarchyBrowserSettings.getInstance(myProject)
}

