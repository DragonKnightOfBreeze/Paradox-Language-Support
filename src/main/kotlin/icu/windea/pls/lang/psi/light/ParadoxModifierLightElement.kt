package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import java.util.*
import icu.windea.pls.model.ParadoxGameType

/**
 * @see ParadoxModifierSupport
 */
class ParadoxModifierLightElement(
    parent: PsiElement,
    private val name: String,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner {
    var canRename = false

    override fun getIcon(flags: Int) = PlsIcons.Nodes.Modifier

    override fun getName() = name

    override fun getText() = name

    override fun getProject() = project

    override fun setName(name: String): PsiElement {
        if (!canRename) throw IncorrectOperationException() // cannot rename
        return this
    }

    override fun getNameIdentifier(): PsiElement {
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxModifierLightElement
            && name == other.name
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
