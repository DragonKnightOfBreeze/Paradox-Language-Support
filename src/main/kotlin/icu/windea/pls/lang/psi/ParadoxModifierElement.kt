package icu.windea.pls.lang.psi

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import java.util.*
import javax.swing.*

/**
 * （生成的）修正可能并不存在一个真正意义上的声明处，用这个模拟。
 *
 * @see ParadoxModifierSupport
 * @see ParadoxModifierManager
 */
class ParadoxModifierElement(
    parent: PsiElement,
    private val name: String,
    val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxFakePsiElement(parent) {
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
        return PlsBundle.message("cwt.description.modifier")
    }

    override fun getText(): String {
        return name
    }

    override fun getPresentation(): ItemPresentation {
        return ParadoxModifierElementPresentation(this)
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
