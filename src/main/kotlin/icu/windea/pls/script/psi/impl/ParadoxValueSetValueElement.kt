package icu.windea.pls.script.psi.impl

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import icons.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 值集中的值并不存在一个真正意义上的声明处，用这个代替。
 */
class ParadoxValueSetValueElement(
	private val text: String,
	val valueSetName: String,
	private val project: Project,
	val gameType: ParadoxGameType
): RenameableFakePsiElement(null), ParadoxScriptNamedElement {
	private val name = text.substringBefore('@')
	
	override fun getText(): String {
		return text
	}
	
	override fun getName(): String {
		return name
	}
	
	override fun getTypeName(): String {
		return valueSetName
	}
	
	override fun getProject(): Project {
		return project
	}
	
	override fun navigate(requestFocus: Boolean) {
		
	}
	
	override fun getIcon(): Icon {
		return PlsIcons.ValueSetValue
	}
	
	override fun getNameIdentifier(): PsiElement {
		return this
	}
	
	override fun getTextOffset(): Int {
		return super.getTextOffset()
	}
	
	override fun isEquivalentTo(another: PsiElement?): Boolean {
		return another is ParadoxValueSetValueElement &&
			name == another.name &&
			valueSetName == another.valueSetName &&
			project == another.project &&
			gameType == another.gameType
	}
}