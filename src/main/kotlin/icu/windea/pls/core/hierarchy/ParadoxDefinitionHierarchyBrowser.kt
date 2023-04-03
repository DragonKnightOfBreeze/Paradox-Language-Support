package icu.windea.pls.core.hierarchy

import com.intellij.ide.hierarchy.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import javax.swing.*

@Suppress("DialogTitleCapitalization")
class ParadoxDefinitionHierarchyBrowser(project: Project, element: PsiElement) : HierarchyBrowserBaseEx(project, element) {
    companion object {
        const val definitionHierarchyType1 = "definition1"
        const val definitionHierarchyType2 = "definition2"
    }
    
    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        createTreeAndSetupCommonActions(trees, IdeActions.GROUP_TYPE_HIERARCHY_POPUP)
    }
    
    private fun createTreeAndSetupCommonActions(trees: MutableMap<in String, in JTree>, groupId: String) {
        val tree1 = createTree(true)
        PopupHandler.installPopupMenu(tree1, groupId, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP)
        trees.put(definitionHierarchyType1, tree1)
        val tree2 = createTree(true)
        PopupHandler.installPopupMenu(tree1, groupId, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP)
        trees.put(definitionHierarchyType2, tree2)
    }
    
    override fun createHierarchyTreeStructure(type: String, psiElement: PsiElement): HierarchyTreeStructure? {
        return when(type) {
            definitionHierarchyType1 -> {
                val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
                val typeConfig = definitionInfo.typeConfig
                val typeElement = typeConfig.pointer.element ?: return null
                ParadoxDefinitionTypeHierarchyTreeStructure(myProject, psiElement, typeElement, typeConfig, false)
            }
            definitionHierarchyType2 -> {
                val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
                val typeConfig = definitionInfo.typeConfig
                val typeElement = typeConfig.pointer.element ?: return null
                ParadoxDefinitionTypeHierarchyTreeStructure(myProject, psiElement, typeElement, typeConfig, true)
            }
            else -> null
        }
    }
    
    override fun createLegendPanel(): JPanel? {
        return null
    }
    
    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        if(descriptor !is ParadoxDefinitionHierarchyNodeDescriptor) return null
        return descriptor.psiElement
    }
    
    override fun getPrevOccurenceActionNameImpl(): String {
        return PlsBundle.message("hierarchy.definition.prev.occurrence.name")
    }
    
    override fun getNextOccurenceActionNameImpl(): String {
        return PlsBundle.message("hierarchy.definition.next.occurrence.name")
    }
    
    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is ParadoxScriptDefinitionElement && element.definitionInfo != null
    }
    
    override fun getComparator(): Comparator<NodeDescriptor<*>> {
        val state = HierarchyBrowserManager.getInstance(myProject).state
        return if(state != null && state.SORT_ALPHABETICALLY) AlphaComparator.INSTANCE else SourceComparator.INSTANCE
    }
    
    override fun getActionPlace(): String {
        return ActionPlaces.TYPE_HIERARCHY_VIEW_TOOLBAR
    }
}

