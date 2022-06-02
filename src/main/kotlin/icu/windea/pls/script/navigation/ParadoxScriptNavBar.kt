package icu.windea.pls.script.navigation

import com.intellij.ide.navigationToolbar.*
import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.*
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
			o is ParadoxScriptVariable -> "@" + o.name
			o is ParadoxScriptProperty -> o.definitionInfo?.name ?: o.name
			o is ParadoxScriptValue && o.isLonely() -> o.value
			o is ParadoxScriptParameterCondition -> o.expression?.let { "[$it]" }
			else -> null
		}
	}
}