package icu.windea.pls.lang.hierarchy.call

import com.intellij.icons.*
import com.intellij.ide.hierarchy.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

class ViewCallerHierarchyAction: ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.caller.hierarchy"),
    PlsBundle.message("action.description.view.caller.hierarchy"),
    AllIcons.Hierarchy.Supertypes
) {
    override fun getTypeName(): String {
        return CallHierarchyBrowserBase.getCallerType()
    }
}

