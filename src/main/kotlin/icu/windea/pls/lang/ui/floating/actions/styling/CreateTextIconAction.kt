package icu.windea.pls.lang.ui.floating.actions.styling

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

class CreateTextIconAction : CreateRichTextAction() {
    override val startMarker = "@"
    override val endMarker = "!"

    override val wrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.text")
    override val wrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.description")
    override val unwrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.unwrap.text")
    override val unwrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateTextIcon.unwrap.description")

    override fun isAvailable(file: PsiFile): Boolean {
        return ParadoxSyntaxConstraint.LocalisationTextIcon.testTarget(file)
    }

    override fun isSelectedElement(startElement: PsiElement, endElement: PsiElement): Boolean {
        return startElement.elementType == TEXT_ICON_START && endElement.elementType == TEXT_ICON_END
    }
}
