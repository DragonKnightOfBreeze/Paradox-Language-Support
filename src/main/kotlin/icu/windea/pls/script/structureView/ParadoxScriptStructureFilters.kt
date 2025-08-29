package icu.windea.pls.script.structureView

import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.ActionPresentationData
import com.intellij.ide.util.treeView.smartTree.Filter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.definitionInfo

interface ParadoxScriptStructureFilters {
    object VariablesFilter : Filter {
        override fun getName() = "PARADOX_SCRIPT_SHOW_VARIABLES"

        override fun isReverted() = true

        override fun isVisible(treeNode: TreeElement): Boolean {
            return treeNode !is ParadoxScriptVariableTreeElement
        }

        override fun getPresentation(): ActionPresentation {
            return ActionPresentationData(PlsBundle.message("script.structureView.showScriptedVariables"), null, PlsIcons.Nodes.ScriptedVariable)
        }
    }

    object DefinitionsFilter : Filter {
        override fun getName() = "PARADOX_SCRIPT_SHOW_DEFINITIONS"

        override fun isReverted() = true

        override fun isVisible(treeNode: TreeElement): Boolean {
            //忽略本身是文件的定义
            return treeNode !is ParadoxScriptPropertyTreeElement || treeNode.element?.definitionInfo == null
        }

        override fun getPresentation(): ActionPresentation {
            return ActionPresentationData(PlsBundle.message("script.structureView.showDefinitions"), null, PlsIcons.Nodes.Definition)
        }
    }

    object PropertiesFilter : Filter {
        override fun getName() = "PARADOX_SCRIPT_SHOW_PROPERTIES"

        override fun isReverted() = true

        override fun isVisible(treeNode: TreeElement): Boolean {
            return treeNode !is ParadoxScriptPropertyTreeElement || treeNode.element?.definitionInfo != null
        }

        override fun getPresentation(): ActionPresentation {
            return ActionPresentationData(PlsBundle.message("script.structureView.showProperties"), null, PlsIcons.Nodes.Property)
        }
    }

    object ValuesFilter : Filter {
        override fun getName() = "PARADOX_SCRIPT_SHOW_VALUES"

        override fun isReverted() = true

        override fun isVisible(treeNode: TreeElement): Boolean {
            return treeNode !is ParadoxScriptValueTreeElement
        }

        override fun getPresentation(): ActionPresentation {
            return ActionPresentationData(PlsBundle.message("script.structureView.showValues"), null, PlsIcons.Nodes.Value)
        }
    }
}
