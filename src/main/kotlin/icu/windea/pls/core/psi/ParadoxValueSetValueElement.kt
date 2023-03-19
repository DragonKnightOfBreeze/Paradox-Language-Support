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
 * 值集值值并不存在一个真正意义上的声明处，用这个模拟。
 */
class ParadoxValueSetValueElement(
    parent: PsiElement,
    private val name: String,
    val valueSetNames: Set<String>,
    val gameType: ParadoxGameType,
    private val project: Project,
    val readWriteAccess: ReadWriteAccessDetector.Access,
) : ParadoxFakePsiElement(parent) {
    constructor(element: PsiElement, name: String, valueSetName: String, project: Project, gameType: ParadoxGameType, readWriteAccess: ReadWriteAccessDetector.Access)
        : this(element, name, setOf(valueSetName), gameType, project, readWriteAccess)
    
    val valueSetName = valueSetNames.first()
    
    val valueSetNamesText = valueSetNames.joinToString(" | ")
    
    override fun getIcon(): Icon {
        val valueSetName = valueSetNames.first() //first is ok
        return PlsIcons.ValueSetValue(valueSetName)
    }
    
    override fun getName(): String {
        return name
    }
    
    override fun getTypeName(): String {
        return PlsBundle.message("script.description.valueSetValue")
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
        return ParadoxValueSetValueElementPresentation(this)
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
        return other is ParadoxValueSetValueElement &&
            name == other.name &&
            valueSetNames.any { it in other.valueSetNames } &&
            project == other.project &&
            gameType == other.gameType
    }
    
    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
