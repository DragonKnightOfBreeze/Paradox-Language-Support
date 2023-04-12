package icu.windea.pls.core.hierarchy

import com.intellij.ide.hierarchy.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*

object ParadoxHierarchyHandler {
    fun getComparator(project: Project): Comparator<NodeDescriptor<*>> {
        val state = HierarchyBrowserManager.getInstance(project).state
        return if(state != null && state.SORT_ALPHABETICALLY) AlphaComparator.INSTANCE else SourceComparator.INSTANCE
    }
}