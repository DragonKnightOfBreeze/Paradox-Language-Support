package icu.windea.pls.lang.ui.floating.actions.styling

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class CreateCommandAction : CreateRichTextAction() {
    override val startMarker = "["
    override val endMarker = "]"

    override val wrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateCommand.text")
    override val wrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateCommand.description")
    override val unwrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateCommand.unwrap.text")
    override val unwrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateCommand.unwrap.description")

    override fun isAvailable(file: PsiFile): Boolean {
        return true
    }

    override fun isSelectedElement(startElement: PsiElement, endElement: PsiElement): Boolean {
        return startElement.elementType == LEFT_BRACKET && endElement.elementType == RIGHT_BRACKET
    }
}
