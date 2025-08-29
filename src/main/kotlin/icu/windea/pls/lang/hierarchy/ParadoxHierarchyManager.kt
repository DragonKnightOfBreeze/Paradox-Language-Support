package icu.windea.pls.lang.hierarchy

import com.intellij.ide.hierarchy.HierarchyBrowserManager
import com.intellij.ide.util.treeView.AlphaComparator
import com.intellij.ide.util.treeView.NodeDescriptor
import com.intellij.openapi.project.Project

object ParadoxHierarchyManager {
    fun getComparator(project: Project): Comparator<NodeDescriptor<*>>? {
        val state = HierarchyBrowserManager.getInstance(project).state
        return if (state != null && state.SORT_ALPHABETICALLY) AlphaComparator.getInstance() else null
    }
}
