@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.localisation.usages

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vcs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.usages.*
import com.intellij.usages.impl.*
import com.intellij.usages.rules.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

//com.intellij.usages.impl.rules.JavaMethodGroupRuleProvider
//org.jetbrains.kotlin.idea.findUsages.KotlinDeclarationGroupingRule

/**
 * 文件结构支持 - 本地化语言区域的使用分组规则。
 */
class ParadoxLocalisationLocaleFileStructureGroupRuleProvider : FileStructureGroupRuleProvider {
    override fun getUsageGroupingRule(project: Project): UsageGroupingRule {
        return getUsageGroupingRule(project, UsageViewSettings.instance)
    }
    
    override fun getUsageGroupingRule(project: Project, usageViewSettings: UsageViewSettings): UsageGroupingRule {
        return LocalisationLocaleGroupingRule(usageViewSettings)
    }
}

//com.intellij.usages.impl.rules.MethodGroupingRule

class LocalisationLocaleGroupingRule(
    private val usageViewSettings: UsageViewSettings
) : SingleParentUsageGroupingRule() {
    private fun getLocalisationLocale(usage: Usage, targets: Array<out UsageTarget>): ParadoxLocalisationLocale? {
        var element = usage.castOrNull<PsiElementUsage>()?.element ?: return null
        if(element.containingFile !is ParadoxLocalisationFile) return null
        if(element is ParadoxLocalisationFile) {
            val offset = usage.castOrNull<UsageInfo2UsageAdapter>()?.usageInfo?.navigationOffset
            if(offset != null) {
                element = element.findElementAt(offset) ?: element
            }
        }
        return element.parentOfType<ParadoxLocalisationPropertyList>()?.locale
	}
    
    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        val localisationLocale = getLocalisationLocale(usage, targets) ?: return null
        return LocalisationLocaleGroup(localisationLocale, usageViewSettings)
    }
}

//com.intellij.usages.impl.rules.MethodGroupingRule.MethodUsageGroup

class LocalisationLocaleGroup(
    localisationLocale: ParadoxLocalisationLocale,
    private val usageViewSettings: UsageViewSettings
) : UsageGroup {
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
        if(_pointer.project.isDisposed) return null
        return _pointer.containingFile?.let { NavigationItemFileStatus.get(it) }
    }
    
    override fun isValid(): Boolean {
        return _pointer.element?.isValid == true
    }
    
    override fun canNavigate(): Boolean {
        return isValid
    }
    
    override fun navigate(requestFocus: Boolean) {
        if(isValid) _pointer.element?.navigate(requestFocus)
    }
    
    override fun canNavigateToSource(): Boolean {
        return canNavigate()
    }
    
    override fun compareTo(other: UsageGroup?): Int {
        if(other !is LocalisationLocaleGroup) {
            return -1 //不期望的结果
        } else if(SmartPointerManager.getInstance(_project).pointToTheSameElement(_pointer, other._pointer)) {
            return 0
        } else if(!usageViewSettings.isSortAlphabetically) {
            val segment1 = _pointer.range
            val segment2 = other._pointer.range
            if(segment1 != null && segment2 != null) {
                return segment1.startOffset - segment2.startOffset
            }
        }
        return _name.compareToIgnoreCase(other._name)
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is LocalisationLocaleGroup && _name == other._name
            && SmartPointerManager.getInstance(_project).pointToTheSameElement(_pointer, other._pointer)
    }
    
    override fun hashCode(): Int {
        return _name.hashCode()
    }
}
