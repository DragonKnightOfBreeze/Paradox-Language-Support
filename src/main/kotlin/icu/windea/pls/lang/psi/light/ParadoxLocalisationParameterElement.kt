package icu.windea.pls.lang.psi.light

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.resolve.parameter.ParadoxLocalisationParameterSupport
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * @see ParadoxLocalisationParameterSupport
 */
class ParadoxLocalisationParameterElement(
    parent: PsiElement,
    private val name: String,
    val localisationName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxMockPsiElement(parent) {
    override fun getIcon(): Icon {
        return PlsIcons.Nodes.Parameter
    }

    override fun getName(): String {
        return name
    }

    override fun getTypeName(): String {
        return PlsBundle.message("localisation.description.parameter")
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxLocalisationParameterElement
            && name == other.name
            && localisationName == other.localisationName
            && project == other.project
            && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, localisationName, project, gameType)
    }
}
