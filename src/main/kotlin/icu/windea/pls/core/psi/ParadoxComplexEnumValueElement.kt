package icu.windea.pls.core.psi

import com.intellij.codeInsight.highlighting.*
import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.lang.model.*
import java.util.*
import javax.swing.*

/**
 * 复杂枚举值其实也不存在一个真正意义上的声明处，用这个模拟。（通过complexEnum规则匹配到的那些是可以同名的）
 */
class ParadoxComplexEnumValueElement(
    parent: PsiElement,
    private val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxFakePsiElement(parent) {
    constructor(parent: PsiElement, info: ParadoxComplexEnumValueInfo, project: Project)
        : this(parent, info.name, info.enumName, info.readWriteAccess, info.gameType, project)
    
    val searchScopeType = ParadoxSearchScopeTypes.get(getCwtConfig(project).getValue(gameType).complexEnums.get(enumName)?.searchScopeType)
    
    override fun getIcon(): Icon {
        return PlsIcons.ComplexEnumValue
    }
    
    override fun getName(): String {
        return name
    }
    
    override fun getTypeName(): String {
        return PlsBundle.message("script.description.complexEnumValue")
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
        return ParadoxComplexEnumValueElementPresentation(this)
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
        return other is ParadoxComplexEnumValueElement &&
            name == other.name &&
            enumName == other.enumName &&
            project == other.project &&
            gameType == other.gameType &&
            searchScopeType.findRoot(project, parent) == other.searchScopeType.findRoot(other.project, other.parent) 
    }
    
    override fun hashCode(): Int {
        return Objects.hash(name, enumName, project, gameType)
    }
}
