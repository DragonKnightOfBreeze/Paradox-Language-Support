package icu.windea.pls.lang.ui.floating.actions.styling

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.constants.ChronicleStrings

class CreateIconAction : CreateRichTextAction() {
    override val startMarker = ChronicleStrings.iconStartMarker
    override val endMarker = ChronicleStrings.iconEndMarker

    override val wrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateIcon.text")
    override val wrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateIcon.description")
    override val unwrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateIcon.unwrap.text")
    override val unwrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateIcon.unwrap.description")

    override fun isAvailable(file: PsiFile): Boolean {
        return true
    }

    override fun isSelectedElement(startElement: PsiElement, endElement: PsiElement): Boolean {
        return startElement.elementType == ICON_START && endElement.elementType == ICON_END
    }
}
