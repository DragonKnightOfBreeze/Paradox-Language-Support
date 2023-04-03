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
import java.text.*
import java.util.function.*
import javax.swing.*

@Suppress("DialogTitleCapitalization")
class ParadoxDefinitionHierarchyBrowser(project: Project, element: PsiElement) : HierarchyBrowserBaseEx(project, element) {
    companion object {
        @Suppress("InvalidBundleOrProperty")
        fun getDefinitionHierarchyType1() = PlsBundle.message("title.hierarchy.definition.1")
        @Suppress("InvalidBundleOrProperty")
        fun getDefinitionHierarchyType2() = PlsBundle.message("title.hierarchy.definition.2")
    }
    
    override fun createTrees(trees: MutableMap<in String, in JTree>) {
        createTreeAndSetupCommonActions(trees, IdeActions.GROUP_TYPE_HIERARCHY_POPUP)
    }
    
    private fun createTreeAndSetupCommonActions(trees: MutableMap<in String, in JTree>, groupId: String) {
        val tree1 = createTree(true)
        PopupHandler.installPopupMenu(tree1, groupId, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP)
        trees.put(getDefinitionHierarchyType1(), tree1)
        val tree2 = createTree(true)
        PopupHandler.installPopupMenu(tree1, groupId, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP)
        trees.put(getDefinitionHierarchyType2(), tree2)
    }
    
    override fun createHierarchyTreeStructure(type: String, psiElement: PsiElement): HierarchyTreeStructure? {
        return when(type) {
            getDefinitionHierarchyType1() -> {
                val definitionInfo = psiElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
                val typeConfig = definitionInfo.typeConfig
                val typeElement = typeConfig.pointer.element ?: return null
                ParadoxDefinitionTypeHierarchyTreeStructure(myProject, psiElement, typeElement, typeConfig, false)
            }
            getDefinitionHierarchyType2() -> {
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
    
    override fun getContentDisplayName(typeName: String, element: PsiElement): String? {
        val definitionInfo = element.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo ?: return null
        val type = definitionInfo.type
        return MessageFormat.format(typeName, type)
    }
    
    override fun getElementFromDescriptor(descriptor: HierarchyNodeDescriptor): PsiElement? {
        if(descriptor !is ParadoxDefinitionHierarchyNodeDescriptor) return null
        return descriptor.psiElement
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
    
    override fun isApplicableElement(element: PsiElement): Boolean {
        return element is ParadoxScriptDefinitionElement
    }
    
    override fun getComparator(): Comparator<NodeDescriptor<*>> {
        val state = HierarchyBrowserManager.getInstance(myProject).state
        return if(state != null && state.SORT_ALPHABETICALLY) AlphaComparator.INSTANCE else SourceComparator.INSTANCE
    }
    
    override fun getPresentableNameMap(): MutableMap<String, Supplier<String>> {
        val map = mutableMapOf<String, Supplier<String>>()
        map.put(getDefinitionHierarchyType1()) { getDefinitionHierarchyType1() }
        map.put(getDefinitionHierarchyType2()) { getDefinitionHierarchyType2() }
        return map
    }
    
    override fun prependActions(actionGroup: DefaultActionGroup) {
        actionGroup.add(ViewDefinitionHierarchy1Action())
        actionGroup.add(ViewDefinitionHierarchy2Action())
        actionGroup.add(AlphaSortAction())
    }
    
    override fun getActionPlace(): String {
        return ActionPlaces.TYPE_HIERARCHY_VIEW_TOOLBAR
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}

