package icu.windea.pls.lang.psi.light

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.resolve.parameter.ParadoxLocalisationParameterSupport
import icu.windea.pls.model.ParadoxGameType
import java.util.*

/**
 * @see ParadoxLocalisationParameterSupport
 */
class ParadoxLocalisationParameterLightElement(
    parent: PsiElement,
    private val name: String,
    val localisationName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner {
    override fun getIcon(flags: Int) = PlsIcons.Nodes.Parameter

    override fun getName() = name

    override fun getText() = name

    override fun getProject() = project

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement {
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxLocalisationParameterLightElement
            && name == other.name
            && localisationName == other.localisationName
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, localisationName, project, gameType)
    }
}
