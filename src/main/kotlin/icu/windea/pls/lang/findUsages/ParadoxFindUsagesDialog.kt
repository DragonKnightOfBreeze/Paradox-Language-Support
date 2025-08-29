package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.CommonFindUsagesDialog
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.ui.StateRestoringCheckBox
import icu.windea.pls.PlsBundle
import javax.swing.BoxLayout
import javax.swing.JPanel

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
        if (isToChange(cbUsages)) {
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
        if (myCbToSearchForTextOccurrences != null) {
            if (isSelected(cbUsages)) {
                myCbToSearchForTextOccurrences.makeSelectable()
            } else {
                myCbToSearchForTextOccurrences.makeUnselectable(false)
            }
        }
        val hasSelected = isSelected(cbUsages)
        isOKActionEnabled = hasSelected
    }
}

