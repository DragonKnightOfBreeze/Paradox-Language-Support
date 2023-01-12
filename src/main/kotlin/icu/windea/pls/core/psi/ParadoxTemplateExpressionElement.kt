package icu.windea.pls.core.psi

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.references.*
import javax.swing.*

class ParadoxTemplateExpressionElement(
    parent: PsiElement,
    private val name: String,
    val configExpression: CwtTemplateExpression,
    private val project: Project,
    val gameType: ParadoxGameType,
    val references: List<ParadoxInTemplateExpressionReference>,
): RenameableFakePsiElement(parent), PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getText(): String {
        return name
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
    
    override fun getIcon(): Icon {
        return PlsIcons.TemplateExpression
    }
    
    override fun getTextRange(): TextRange? {
        return null //return null to avoid incorrect highlight at file start
    }
    
    override fun getNameIdentifier(): PsiElement {
        return this
    }
    
    override fun getProject(): Project {
        return project
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
        var result = name.hashCode()
        result = 31 * result + configExpression.hashCode()
        result = 31 * result + project.hashCode()
        result = 31 * result + gameType.hashCode()
        return result
    }
}

