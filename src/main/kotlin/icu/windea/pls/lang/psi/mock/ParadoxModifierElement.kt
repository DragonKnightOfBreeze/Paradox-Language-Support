package icu.windea.pls.lang.psi.mock

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.modifier.ParadoxModifierSupport
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * （生成的）修正可能并不存在一个真正意义上的声明处，用这个模拟。
 *
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
        if (!canRename) throw IncorrectOperationException() //cannot rename
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
        return other is ParadoxModifierElement &&
            name == other.name &&
            project == other.project &&
            gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
