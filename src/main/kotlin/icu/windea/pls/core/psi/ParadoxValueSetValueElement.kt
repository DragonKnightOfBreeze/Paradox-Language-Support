package icu.windea.pls.core.psi

import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 值集值值并不存在一个真正意义上的声明处，用这个模拟。
 */
class ParadoxValueSetValueElement(
	element: PsiElement,
	private val name: String,
	val valueSetName: String,
	private val project: Project,
	val gameType: ParadoxGameType,
	val read: Boolean = true
) : RenameableFakePsiElement(element), ParadoxScriptNamedElement, Navigatable {
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
		return when(valueSetName) {
			"variable" -> PlsIcons.Variable
			else -> PlsIcons.ValueSetValue
		}
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
			valueSetName == other.valueSetName &&
			project == other.project &&
			gameType == other.gameType
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + valueSetName.hashCode()
		result = 31 * result + project.hashCode()
		result = 31 * result + gameType.hashCode()
		return result
	}
}
