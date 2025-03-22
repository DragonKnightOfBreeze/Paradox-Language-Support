package icu.windea.pls.lang.psi

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.model.*
import java.util.*
import javax.swing.*

class ParadoxTemplateExpressionElement(
    parent: PsiElement,
    private val name: String,
    val configExpression: CwtTemplateExpression,
    val gameType: ParadoxGameType,
    private val project: Project,
    val references: List<ParadoxTemplateSnippetPsiExpressionReference>,
) : ParadoxFakePsiElement(parent) {
    var canRename = false

    override fun getIcon(): Icon {
        return PlsIcons.Nodes.TemplateExpression
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement? {
        if (!canRename) throw IncorrectOperationException() //cannot rename
        return null
    }

    override fun getTypeName(): String {
        return PlsBundle.message("script.description.templateExpression")
    }

    override fun getText(): String {
        return name
    }

    override fun getPresentation(): ItemPresentation {
        return ParadoxTemplateExpressionElementPresentation(this)
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxTemplateExpressionElement &&
            name == other.name &&
            configExpression == other.configExpression &&
            project == other.project &&
            gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, configExpression, project, gameType)
    }
}

