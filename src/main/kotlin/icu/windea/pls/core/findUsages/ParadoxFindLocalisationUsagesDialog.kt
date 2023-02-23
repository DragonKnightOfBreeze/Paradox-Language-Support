package icu.windea.pls.core.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.project.*
import com.intellij.ui.*
import icu.windea.pls.*
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
    private val findOptions = findOptions
    private lateinit var cbUsages: StateRestoringCheckBox
    private lateinit var cbCrossLocales: StateRestoringCheckBox
    
    override fun getPreferredFocusedComponent(): JComponent {
        return cbUsages
    }
    
    override fun calcFindUsagesOptions(options: FindUsagesOptions) {
        options as ParadoxLocalisationFindUsagesOptions
        super.calcFindUsagesOptions(options)
        if(isToChange(cbUsages)) {
            options.isUsages = isSelected(cbUsages)
        }
        if(isToChange(cbCrossLocales)) {
            options.isCrossLocales = isSelected(cbCrossLocales)
        }
    }
    
    override fun createFindWhatPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        cbUsages = addCheckboxToPanel(PlsBundle.message("find.what.usages.checkbox"), findOptions.isUsages, panel, true)
        return panel
    }
    
    override fun addUsagesOptions(panel: JPanel) {
        cbCrossLocales = addCheckboxToPanel(PlsBundle.message("find.options.crossLocales.checkbox"), findOptions.isCrossLocales, panel, true)
        super.addUsagesOptions(panel)
    }
    
    override fun update() {
        if(myCbToSearchForTextOccurrences != null) {
            if(isSelected(cbUsages)) {
                cbCrossLocales.makeSelectable()
                myCbToSearchForTextOccurrences.makeSelectable()
            } else {
                cbCrossLocales.makeUnselectable(false)
                myCbToSearchForTextOccurrences.makeUnselectable(false)
            }
        }
        val hasSelected = isSelected(cbUsages) || isSelected(cbCrossLocales)
        isOKActionEnabled = hasSelected
    }
}