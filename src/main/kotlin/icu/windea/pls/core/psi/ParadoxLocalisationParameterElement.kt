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
import java.util.*
import javax.swing.*

/**
 * 本地化的参数并不存在一个真正意义上的声明处，用这个模拟。
 * @see ParadoxLocalisationParameterSupport
 */
class ParadoxLocalisationParameterElement(
    parent: PsiElement,
    private val name: String,
    val localisationName: String,
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
        return ParadoxLocalisationParameterElementPresentation(this)
    }
    
    override fun getProject(): Project {
        return project
    }
    
    override fun equals(other: Any?): Boolean {
        return other is ParadoxLocalisationParameterElement &&
            name == other.name &&
            localisationName == other.localisationName &&
            project == other.project &&
            gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(name, localisationName, project, gameType)
    }
}