package icu.windea.pls.cwt.navigation

import com.intellij.ide.navigationToolbar.*
import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import javax.swing.*

class CwtNavBar : StructureAwareNavBarModelExtension() {
	override val language: Language = CwtLanguage
	
	override fun getIcon(o: Any?): Icon? {
		return when {
			o is PsiElement -> o.icon
			else -> null
		}
	}
	
	override fun getPresentableText(o: Any?): String? {
		return when {
			o is CwtProperty -> o.name
			o is CwtValue && o.isLonely() -> o.value
			else -> null
		}
	}
	
	//FIXME 没有排除作为属性的值的值
	//FIXME 鼠标放置在选项注释或文档注释上时，需要定位到对应的属性或值
	
	override fun acceptParentFromModel(psiElement: PsiElement?): Boolean {
		return psiElement != null
	}
	
	override fun adjustElement(psiElement: PsiElement): PsiElement? {
		val parent = psiElement.parentOfTypes(CwtOptionComment::class, CwtDocumentationComment::class, CwtValue::class, CwtProperty::class, withSelf = true)
		if(parent == null || parent is CwtProperty || parent is CwtValue) return psiElement
		val next = parent.siblings().findLast { it is CwtOptionComment || it is CwtDocumentationComment || it.isSpaceOrSingleLineBreak() }?.nextSibling
		if(next is CwtProperty || next is CwtValue) return next
		return null
	}
}