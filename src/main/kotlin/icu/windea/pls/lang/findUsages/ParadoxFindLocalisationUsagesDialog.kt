package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.project.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

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
    //private var cbCrossLocales: StateRestoringCheckBox? = null
    
    override fun calcFindUsagesOptions(options: FindUsagesOptions) {
        options as ParadoxLocalisationFindUsagesOptions
        super.calcFindUsagesOptions(options)
        if(isToChange(cbUsages)) {
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
    
    //override fun addUsagesOptions(panel: JPanel) {
    //    cbCrossLocales = addCheckboxToPanel(PlsBundle.message("find.options.crossLocales.checkbox"), findOptions.isCrossLocales, panel, true)
    //    super.addUsagesOptions(panel)
    //}
    
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