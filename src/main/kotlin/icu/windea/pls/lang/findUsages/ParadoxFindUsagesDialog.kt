package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
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
    private val findOptions get() = myFindUsagesOptions as ParadoxFindUsagesOptions
    
    private var cbUsages: StateRestoringCheckBox? = null
    
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

