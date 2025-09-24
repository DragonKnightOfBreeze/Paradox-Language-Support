package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.project.Project
import com.intellij.ui.StateRestoringCheckBox
import icu.windea.pls.PlsBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import javax.swing.BoxLayout
import javax.swing.JPanel

class ParadoxFindLocalisationUsagesDialog(
    element: ParadoxLocalisationProperty,
    project: Project,
    findOptions: ParadoxLocalisationFindUsagesOptions,
    toShowInNewTab: Boolean,
    mustOpenInNewTab: Boolean,
    isSingleFile: Boolean,
    handler: ParadoxLocalisationFindUsagesHandler
) : ParadoxFindUsagesDialog(element, project, findOptions, toShowInNewTab, mustOpenInNewTab, isSingleFile, handler) {
    private val findOptions get() = myFindUsagesOptions as ParadoxLocalisationFindUsagesOptions

    private var cbUsages: StateRestoringCheckBox? = null
    // private var cbCrossLocales: StateRestoringCheckBox? = null

    override fun calcFindUsagesOptions(options: FindUsagesOptions) {
        options as ParadoxLocalisationFindUsagesOptions
        super.calcFindUsagesOptions(options)
        if (isToChange(cbUsages)) {
            options.isUsages = isSelected(cbUsages)
        }
        //if(isToChange(cbCrossLocales)) {
        //    options.isCrossLocales = isSelected(cbCrossLocales)
        //}
    }

    override fun createFindWhatPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        cbUsages = addCheckboxToPanel(PlsBundle.message("find.what.usages.checkbox"), findOptions.isUsages, panel, true)
        return panel
    }

    // override fun addUsagesOptions(panel: JPanel) {
    //     cbCrossLocales = addCheckboxToPanel(PlsBundle.message("find.options.crossLocales.checkbox"), findOptions.isCrossLocales, panel, true)
    //     super.addUsagesOptions(panel)
    // }

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
