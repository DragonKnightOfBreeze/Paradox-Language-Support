package icu.windea.pls.lang.ui.actions.styling

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.constants.ChronicleStrings

class CreateParameterAction : CreateRichTextAction() {
    override val startMarker = ChronicleStrings.parameterStartMarker
    override val endMarker = ChronicleStrings.parameterEndMarker

    override val wrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateParameter.text")
    override val wrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateParameter.description")
    override val unwrapActionName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateParameter.unwrap.text")
    override val unwrapActionDescription get() = ChronicleBundle.message("action.Pls.Localisation.Styling.CreateParameter.unwrap.description")

    override fun isAvailable(file: ParadoxLocalisationFile): Boolean {
        return true
    }

    override fun isSelectedElement(startElement: PsiElement, endElement: PsiElement): Boolean {
        return startElement.elementType == PARAMETER_START && endElement.elementType == PARAMETER_END
    }
}
