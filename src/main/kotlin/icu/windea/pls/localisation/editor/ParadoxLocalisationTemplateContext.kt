package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.template.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationTemplateContext : TemplateContextType(paradoxLocalisationId, paradoxLocalisationName) {
	override fun isInContext(file: PsiFile, offset: Int): Boolean {
		val element = file.findElementAt(offset)
		return element?.parentOfType<ParadoxLocalisationPropertyValue>(true) != null
	}
}
