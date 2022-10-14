package icu.windea.pls.script.psi.impl

import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义的参数并不存在一个真正意义上的声明处，用这个模拟。
 */
class ParadoxParameterElement(
	element: PsiElement,
	private val name: String,
	val definitionName: String,
	val definitionType: String,
	private val project: Project,
	val gameType: ParadoxGameType
): RenameableFakePsiElement(element), ParadoxScriptNamedElement, Navigatable {
	override fun getText(): String {
		return name
	}
	
	override fun getName(): String {
		return name
	}
	
	override fun getTypeName(): String {
		return PlsBundle.message("script.description.parameter")
	}
	
	override fun getIcon(): Icon {
		return PlsIcons.Parameter
	}
	
	override fun getNameIdentifier(): PsiElement {
		return this
	}
	
	override fun getProject(): Project {
		return project
	}
	
	override fun navigate(requestFocus: Boolean) {
		
	}
	
	override fun canNavigate(): Boolean {
		return false // false -> click to show usages
	}
	
	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ParadoxParameterElement &&
			name == another.name &&
			definitionName == another.definitionName &&
			definitionType == another.definitionType &&
			project == another.project &&
			gameType == another.gameType
	}
}