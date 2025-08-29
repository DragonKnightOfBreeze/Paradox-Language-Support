package icu.windea.pls.script.usages

import com.intellij.navigation.NavigationItemFileStatus
import com.intellij.openapi.vcs.FileStatus
import com.intellij.psi.SmartPointerManager
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageViewSettings
import icu.windea.pls.core.compareToIgnoreCase
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.icon
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import javax.swing.Icon

class ParadoxDefinitionUsageGroup(
    definition: ParadoxScriptDefinitionElement,
    definitionInfo: ParadoxDefinitionInfo,
    private val usageViewSettings: UsageViewSettings
) : UsageGroup {
    //com.intellij.usages.impl.rules.MethodGroupingRule.MethodUsageGroup

    private val _name = definitionInfo.name
    private val _icon = definition.icon
    private val _project = definitionInfo.project
    private val _pointer = definition.createPointer()

    override fun getIcon(): Icon? {
        return _icon
    }

    override fun getPresentableGroupText(): String {
        return _name.or.anonymous()
    }

    override fun getFileStatus(): FileStatus? {
        if (_pointer.project.isDisposed) return null
        return _pointer.containingFile?.let { NavigationItemFileStatus.get(it) }
    }

    override fun isValid(): Boolean {
        return _pointer.element?.isValid == true
    }

    override fun canNavigate(): Boolean {
        return isValid
    }

    override fun navigate(requestFocus: Boolean) {
        if (isValid) _pointer.element?.navigate(requestFocus)
    }

    override fun canNavigateToSource(): Boolean {
        return canNavigate()
    }

    override fun compareTo(other: UsageGroup?): Int {
        if (other !is ParadoxDefinitionUsageGroup) {
            return -1 //不期望的结果
        } else if (SmartPointerManager.getInstance(_project).pointToTheSameElement(_pointer, other._pointer)) {
            return 0
        } else if (!usageViewSettings.isSortAlphabetically) {
            val segment1 = _pointer.range
            val segment2 = other._pointer.range
            if (segment1 != null && segment2 != null) {
                return segment1.startOffset - segment2.startOffset
            }
        }
        return _name.compareToIgnoreCase(other._name)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionUsageGroup && _name == other._name
            && SmartPointerManager.getInstance(_project).pointToTheSameElement(_pointer, other._pointer)
    }

    override fun hashCode(): Int {
        return _name.hashCode()
    }
}
