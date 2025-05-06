package icu.windea.pls.localisation.usages

import com.intellij.navigation.*
import com.intellij.openapi.vcs.*
import com.intellij.psi.*
import com.intellij.usages.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

class ParadoxLocalisationLocaleGroup(
    localisationLocale: ParadoxLocalisationLocale,
    private val usageViewSettings: UsageViewSettings
) : UsageGroup {
    //com.intellij.usages.impl.rules.MethodGroupingRule.MethodUsageGroup

    private val _name = localisationLocale.name
    private val _icon = localisationLocale.icon
    private val _project = localisationLocale.project
    private val _pointer = localisationLocale.createPointer()

    override fun getIcon(): Icon? {
        return _icon
    }

    override fun getPresentableGroupText(): String {
        return _name
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
        if (other !is ParadoxLocalisationLocaleGroup) {
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
        return this === other || other is ParadoxLocalisationLocaleGroup && _name == other._name
            && SmartPointerManager.getInstance(_project).pointToTheSameElement(_pointer, other._pointer)
    }

    override fun hashCode(): Int {
        return _name.hashCode()
    }
}
