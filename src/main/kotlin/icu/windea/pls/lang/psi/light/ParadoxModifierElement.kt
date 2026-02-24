package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.resolve.modifier.ParadoxModifierSupport
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * @see ParadoxModifierSupport
 */
class ParadoxModifierElement(
    parent: PsiElement,
    private val name: String,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxMockPsiElement(parent) {
    var canRename = false

    override fun getIcon(): Icon {
        return PlsIcons.Nodes.Modifier
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement? {
        if (!canRename) throw IncorrectOperationException() // cannot rename
        return null
    }

    override fun getTypeName(): String {
        return PlsBundle.message("cwt.config.description.modifier")
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxModifierElement
            && name == other.name
            && project == other.project
            && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
