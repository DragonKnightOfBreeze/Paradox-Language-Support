package icu.windea.pls.core.psi

import com.intellij.codeInsight.highlighting.*
import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import java.util.*
import javax.swing.*

/**
 * 定义的参数并不存在一个真正意义上的声明处，用这个模拟。
 *
 * @property contextKey 用于判断参数是否拥有相同的上下文。
 * @see ParadoxParameterSupport
 */
class ParadoxParameterElement(
    parent: PsiElement,
    private val name: String,
    val contextName: String,
    val contextIcon: Icon,
    val contextKey: String,
    val rangeInParent: TextRange?,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxFakePsiElement(parent) {
    override fun getIcon(): Icon {
        return PlsIcons.Parameter
    }
    
    override fun getName(): String {
        return name
    }
    
    override fun getTypeName(): String {
        return PlsBundle.message("script.description.parameter")
    }
    
    override fun getText(): String {
        return name
    }
    
    override fun getPresentation(): ItemPresentation {
        return ParadoxParameterElementPresentation(this)
    }
    
    override fun getProject(): Project {
        return project
    }
    
    override fun equals(other: Any?): Boolean {
        return other is ParadoxParameterElement &&
            name == other.name &&
            contextKey == other.contextKey &&
            project == other.project &&
            gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(name, contextKey, project, gameType)
    }
}

