package icu.windea.pls.core.psi

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.navigation.*
import javax.swing.*

/**
 * 值集值值并不存在一个真正意义上的声明处，用这个模拟。
 */
class ParadoxValueSetValueElement(
	element: PsiElement,
	private val name: String,
	val valueSetNames: List<String>,
	private val project: Project,
	val gameType: ParadoxGameType,
	val read: Boolean
) : RenameableFakePsiElement(element), PsiNameIdentifierOwner, NavigatablePsiElement {
	constructor(
		element: PsiElement,
		name: String,
		valueSetName: String,
		project: Project,
		gameType: ParadoxGameType,
		read: Boolean = true
	) : this(element, name, listOf(valueSetName), project, gameType, read)
	
	val valueSetName = valueSetNames.first()
	
	val valueSetNamesText = valueSetNames.joinToString(" | ")
	
	override fun getText(): String {
		return name
	}
	
	override fun getName(): String {
		return name
	}
	
	override fun getTypeName(): String {
		return PlsBundle.message("script.description.valueSetValue")
	}
	
	override fun getIcon(): Icon {
		val valueSetName = valueSetNames.first() //first is ok
		return PlsIcons.ValueSetValue(valueSetName)
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
		
	}
	
	override fun canNavigate(): Boolean {
		return false // false -> click to show usages
	}
	
	override fun equals(other: Any?): Boolean {
		return other is ParadoxValueSetValueElement &&
			name == other.name &&
			valueSetNamesText == other.valueSetNamesText &&
			project == other.project &&
			gameType == other.gameType
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + valueSetNamesText.hashCode()
		result = 31 * result + project.hashCode()
		result = 31 * result + gameType.hashCode()
		return result
	}
}
