package icu.windea.pls.script.usages

import com.intellij.navigation.NavigationItemFileStatus
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FileStatus
import com.intellij.psi.SmartPointerManager
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageViewSettings
import icu.windea.pls.core.compareToIgnoreCase
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.icon
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.util.*
import javax.swing.Icon

class ParadoxDefinitionInjectionUsageGroup(
    element: ParadoxScriptProperty,
    private val name: String,
    private val type: String,
    private val project: Project,
    private val usageViewSettings: UsageViewSettings,
) : UsageGroup {
    // com.intellij.usages.impl.rules.MethodGroupingRule.MethodUsageGroup

    private val icon = element.icon
    private val pointer = element.createPointer()

    override fun getIcon(): Icon? {
        return icon
    }

    override fun getPresentableGroupText(): String {
        return name.or.anonymous()
    }

    override fun getFileStatus(): FileStatus? {
        if (pointer.project.isDisposed) return null
        return pointer.containingFile?.let { NavigationItemFileStatus.get(it) }
    }

    override fun isValid(): Boolean {
        return pointer.element?.isValid == true
    }

    override fun canNavigate(): Boolean {
        return isValid
    }

    override fun navigate(requestFocus: Boolean) {
        if (isValid) pointer.element?.navigate(requestFocus)
    }

    override fun canNavigateToSource(): Boolean {
        return canNavigate()
    }

    override fun compareTo(other: UsageGroup?): Int {
        if (other !is ParadoxDefinitionInjectionUsageGroup) {
            return -1 // 不期望的结果
        } else if (SmartPointerManager.getInstance(project).pointToTheSameElement(pointer, other.pointer)) {
            return 0
        } else if (!usageViewSettings.isSortAlphabetically) {
            val segment1 = pointer.range
            val segment2 = other.pointer.range
            if (segment1 != null && segment2 != null) {
                return segment1.startOffset - segment2.startOffset
            }
        }
        return name.compareToIgnoreCase(other.name)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionInjectionUsageGroup
            && name == other.name
            && type == other.type
            && SmartPointerManager.getInstance(project).pointToTheSameElement(pointer, other.pointer)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, type)
    }
}
