package icu.windea.pls.core.psi

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.navigation.*
import javax.swing.*

/**
 * （生成的）修正可能并不存在一个真正意义上的声明处，用这个模拟。
 */
class ParadoxModifierElement(
	element: PsiElement,
	private val name: String,
	val modifierConfig: CwtModifierConfig,
	val templateExpression: ParadoxTemplateExpression?,
	private val project: Project,
	val gameType: ParadoxGameType
) : RenameableFakePsiElement(element), PsiNameIdentifierOwner, NavigatablePsiElement {
	override fun getText(): String {
		return name
	}
	
	override fun getName(): String {
		return name
	}
	
	override fun getTypeName(): String {
		return PlsBundle.message("script.description.modifier")
	}
	
	override fun getIcon(): Icon {
		return PlsIcons.Modifier
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
		
	}
	
	override fun canNavigate(): Boolean {
		return false // false -> click to show usages
	}
	
	override fun equals(other: Any?): Boolean {
		return other is ParadoxModifierElement &&
			name == other.name &&
			templateExpression == other.templateExpression &&
			project == other.project &&
			gameType == other.gameType
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + templateExpression.hashCode()
		result = 31 * result + project.hashCode()
		result = 31 * result + gameType.hashCode()
		return result
	}
}