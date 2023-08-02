package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.generation.*
import com.intellij.icons.*
import com.intellij.ide.util.*
import com.intellij.openapi.project.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*

class ParadoxGenerateLocalisationsChooser(
    elements: Array<out Localisation>,
    project: Project
) : MemberChooser<ParadoxGenerateLocalisationsChooser.Localisation>(elements, true, true, project) {
    init {
        this.setCopyJavadocVisible(false)
    }
    
    override fun getShowContainersAction(): ShowContainersAction {
        return ShowContainersAction(
            PlsBundle.lazyMessage("generation.localisation.showContainers"),
            AllIcons.Actions.GroupBy
        )
    }
    
    class Localisation(
        val name: String,
        val info: ParadoxLocalisationCodeInsightInfo,
        val context: ParadoxLocalisationCodeInsightContext
    ) : MemberChooserObjectBase(name, PlsIcons.Localisation), ClassMember {
        override fun getParentNodeDelegate(): MemberChooserObject {
            return when(context.type) {
                Type.File -> Unresolved(context)
                Type.Definition -> Definition(context.name, context)
                Type.Modifier -> Modifier(context.name, context)
            }
        }
    }
    
    class Definition(
        val name: String,
        val context: ParadoxLocalisationCodeInsightContext
    ) : MemberChooserObjectBase(name, PlsIcons.Definition)
    
    class Modifier(
        val name: String,
        val context: ParadoxLocalisationCodeInsightContext
    ) : MemberChooserObjectBase(name, PlsIcons.Modifier)
    
    class Unresolved(
        val context: ParadoxLocalisationCodeInsightContext
    ) : MemberChooserObjectBase(PlsBundle.message("generation.localisation.unresolved"))
}