package icu.windea.pls.lang.hierarchy.call

import com.intellij.icons.*
import com.intellij.ide.hierarchy.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

class ViewCalleeHierarchyAction: ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.callee.hierarchy"),
    PlsBundle.message("action.description.view.callee.hierarchy"),
    AllIcons.Hierarchy.Subtypes
) {
    override fun getTypeName(): String {
        return CallHierarchyBrowserBase.getCalleeType()
    }
}