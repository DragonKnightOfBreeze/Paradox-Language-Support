package icu.windea.pls.script.navigation

import com.intellij.ide.navigationToolbar.*
import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class ParadoxScriptNavBar : StructureAwareNavBarModelExtension() {
	override val language: Language = ParadoxScriptLanguage
	
	override fun getIcon(o: Any?): Icon? {
		return when {
			o is PsiElement -> o.icon
			else -> null
		}
	}
	
	override fun getPresentableText(o: Any?): String? {
		return when {
			o is ParadoxScriptScriptedVariable -> "@" + o.name
			o is ParadoxScriptProperty -> o.definitionInfo?.name ?: o.name
			o is ParadoxScriptValue && !o.isPropertyValue() -> o.value
			o is ParadoxScriptParameterCondition -> o.conditionExpression?.let { "[$it]" }
			else -> null
		}
	}
	
	//FIXME 没有排除作为属性的值的值
}