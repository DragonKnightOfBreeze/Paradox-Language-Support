package icu.windea.pls.core.psi

import com.intellij.lang.*
import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.navigation.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import java.util.*
import javax.swing.*

/**
 * 用于为合成（注入的/合并后的）的CWT规则提供声明处。
 */
class CwtMemberConfigElement(
    parent: PsiElement,
    val config: CwtMemberConfig<*>,
    val gameType: ParadoxGameType,
    val project: Project
): ParadoxFakePsiElement(parent) {
    override fun getIcon(): Icon {
        return when(config) {
            is CwtPropertyConfig -> PlsIcons.CwtProperty
            is CwtValueConfig -> PlsIcons.CwtValue
        }
    }
    
    override fun getName(): String {
        return config.expression.expressionString
    }
    
    override fun setName(name: String): PsiElement {
        throw IncorrectOperationException() //cannot rename
    }
    
    override fun getTypeName(): String {
        return when(config) {
            is CwtPropertyConfig -> PlsBundle.message("cwt.description.property")
            is CwtValueConfig -> PlsBundle.message("cwt.description.value")
        }
    }
    
    override fun getText(): String {
        return config.toString()
    }
    
    override fun getTextRange(): TextRange? {
        return null //return null to avoid incorrect highlight at file start
    }
    
    override fun getNameIdentifier(): PsiElement {
        return this
    }
    
    override fun getPresentation(): ItemPresentation {
        return CwtItemPresentation(this)
    }
    
    override fun getLanguage(): Language {
        return CwtLanguage
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
        return other is CwtMemberConfigElement &&
            config == other.config &&
            project == other.project &&
            gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(config, project, gameType)
    }
}