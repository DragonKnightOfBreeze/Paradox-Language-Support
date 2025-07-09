package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.localisation.psi.*
import java.util.concurrent.atomic.*

data class ParadoxLocalisationManipulationContext(
    val project: Project,
    val file: PsiFile?,
    val elements: List<ParadoxLocalisationProperty>
) {
    var selectedLocale: CwtLocaleConfig? = null

    val elementsAndContexts = elements.map { it to ParadoxLocalisationContext.from(it) }
    val elementsAndContextsToHandle = elementsAndContexts.filter { (_, context) -> context.shouldHandle }
    val errorRef = AtomicReference<Throwable>()

    companion object {
        @JvmStatic
        fun from(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>): ParadoxLocalisationManipulationContext {
            return ParadoxLocalisationManipulationContext(project, file, elements)
        }

        @JvmStatic
        fun from(file: PsiFile): ParadoxLocalisationManipulationContext? {
            if (file !is ParadoxLocalisationFile) return null
            val elements = file.propertyList?.propertyList?.orNull() ?: return null
            val project = file.project
            return ParadoxLocalisationManipulationContext(project, file, elements)
        }

        @JvmStatic
        fun from(propertyList: ParadoxLocalisationPropertyList): ParadoxLocalisationManipulationContext? {
            val file = propertyList.containingFile
            val elements = propertyList.propertyList.orNull() ?: return null
            val project = file.project
            return ParadoxLocalisationManipulationContext(project, file, elements)
        }
    }
}
