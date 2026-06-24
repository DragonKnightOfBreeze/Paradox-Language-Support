package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.resolve.ParadoxSyntaxService
import icu.windea.pls.script.formatter.ParadoxScriptCodeStyleSettings
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty

object ParadoxSyntaxManipulationService {
    fun reformatAroundPropertySeparator(property: ParadoxScriptProperty, separator: PsiElement, project: Project) {
        val prevSpaces = separator.prevSibling as? PsiWhiteSpace
        prevSpaces?.delete()
        val nextSpaces = separator.nextSibling as? PsiWhiteSpace
        nextSpaces?.delete()
        val spaceAroundPropertySeparator = ParadoxScriptCodeStyleSettings.getInstance(property.containingFile).SPACE_AROUND_PROPERTY_SEPARATOR
        if (spaceAroundPropertySeparator) {
            if (separator.elementType != ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN) {
                property.addBefore(ParadoxScriptElementFactory.createWhiteSpaceFromText(project, " "), separator)
            }
            property.addAfter(ParadoxScriptElementFactory.createWhiteSpaceFromText(project, " "), separator)
        }
    }

    fun replacePropertySeparator(separator: PsiElement, project: Project, newSeparatorText: String) {
        val newProperty = ParadoxScriptElementFactory.createProperty(project, "k${newSeparatorText}v")
        val newSeparator = newProperty.findChild { ParadoxSyntaxService.isPropertySeparator(it) } ?: return
        separator.replace(newSeparator)
    }
}
