package icu.windea.pls.core.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.search.scopes.*
import javax.swing.*

//com.intellij.find.findUsages.FindClassUsagesDialog

open class ParadoxFindUsagesDialog(
    element: PsiElement,
    project: Project,
    findOptions: ParadoxFindUsagesOptions,
    toShowInNewTab: Boolean,
    mustOpenInNewTab: Boolean,
    isSingleFile: Boolean,
    handler: ParadoxFindUsagesHandler
) : CommonFindUsagesDialog(element, project, findOptions, toShowInNewTab, mustOpenInNewTab, isSingleFile, handler) {
    private val element = element
    private val findOptions = findOptions
    private val isSingleFile = isSingleFile
    private lateinit var cbUsages: StateRestoringCheckBox
    
    override fun init() {
        if(!isSingleFile) {
            //全局查找使用时，如果可以获取，使用期望的查询作用域
            val scopeToUse = ParadoxGlobalSearchScope.fromElement(element)
            if(scopeToUse != null) {
                findOptions.searchScope = scopeToUse
            }
        }
        super.init()
    }
    
    override fun calcFindUsagesOptions(options: FindUsagesOptions) {
        options as ParadoxFindUsagesOptions
        super.calcFindUsagesOptions(options)
        if(isToChange(cbUsages)) {
            options.isUsages = isSelected(cbUsages)
        }
    }
    
    override fun createFindWhatPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        cbUsages = addCheckboxToPanel(PlsBundle.message("find.what.usages.checkbox"), findOptions.isUsages, panel, true)
        return panel
    }
    
    override fun update() {
        if(myCbToSearchForTextOccurrences != null) {
            if(isSelected(cbUsages)) {
                myCbToSearchForTextOccurrences.makeSelectable()
            } else {
                myCbToSearchForTextOccurrences.makeUnselectable(false)
            }
        }
        val hasSelected = isSelected(cbUsages)
        isOKActionEnabled = hasSelected
    }
}

