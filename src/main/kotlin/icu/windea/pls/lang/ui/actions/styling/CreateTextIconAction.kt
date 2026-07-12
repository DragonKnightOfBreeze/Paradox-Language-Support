package icu.windea.pls.lang.ui.actions.styling

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

class CreateTextIconAction : CreateRichTextAction() {
    override val startMarker = ChronicleStrings.textIconStartMarker
    override val endMarker = ChronicleStrings.textIconEndMarker

    override val wrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.text")
    override val wrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.description")
    override val unwrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.unwrap.text")
    override val unwrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.unwrap.description")

    override fun isAvailable(file: ParadoxLocalisationFile): Boolean {
        return ParadoxSyntaxConstraint.LocalisationTextIcon.testTarget(file)
    }

    override fun isSelectedElement(startElement: PsiElement, endElement: PsiElement): Boolean {
        return startElement.elementType == TEXT_ICON_START && endElement.elementType == TEXT_ICON_END
    }
}
