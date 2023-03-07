package icu.windea.pls.core.psi

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.support.*
import javax.swing.*

/**
 * （生成的）修正可能并不存在一个真正意义上的声明处，用这个模拟。
 *
 * @see ParadoxModifierSupport
 */
class ParadoxModifierElement(
    parent: PsiElement,
    private val name: String,
    val modifierConfig: CwtModifierConfig?,
    val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxFakePsiElement(parent) {
    override fun getIcon(): Icon {
        return PlsIcons.Modifier
    }
    
    override fun getName(): String {
        return name
    }
    
    override fun setName(name: String): PsiElement {
        throw IncorrectOperationException() //cannot rename
    }
    
    override fun getTypeName(): String {
        return PlsBundle.message("script.description.modifier")
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
        return ParadoxModifierElementPresentation(this)
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
    
    override fun isEquivalentTo(other: PsiElement?): Boolean {
        return other is ParadoxModifierElement &&
            name == other.name &&
            project == other.project &&
            gameType == other.gameType
    }
}