package icu.windea.pls.core.psi

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.references.*
import icu.windea.pls.lang.model.*
import java.util.*
import javax.swing.*

class ParadoxTemplateExpressionElement(
    parent: PsiElement,
    private val name: String,
    val configExpression: CwtTemplateExpression,
    val gameType: ParadoxGameType,
    private val project: Project,
    val references: List<ParadoxInTemplateExpressionReference>,
) : ParadoxFakePsiElement(parent) {
    override fun getIcon(): Icon {
        return PlsIcons.TemplateExpression
    }
    
    override fun getName(): String {
        return name
    }
    
    override fun setName(name: String): PsiElement {
        throw IncorrectOperationException() //cannot rename
    }
    
    override fun getTypeName(): String {
        return PlsBundle.message("script.description.templateExpression")
    }
    
    override fun getText(): String {
        return name
    }
    
    override fun getTextRange(): TextRange? {
        return null //return null to avoid incorrect highlight at file start
    }
    
    override fun getNameIdentifier(): PsiElement {
        return this
    }
    
    override fun getPresentation(): ItemPresentation {
        return ParadoxTemplateExpressionElementPresentation(this)
    }
    
    override fun getProject(): Project {
        return project
    }
    
    override fun navigate(requestFocus: Boolean) {
        //click to show usages
    }
    
    override fun canNavigate(): Boolean {
        return false // false -> click to show usages
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

