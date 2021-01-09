package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.codeInsight.template.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationTemplateContext : TemplateContextType(paradoxLocalisationNameSsc, paradoxLocalisationName) {
	override fun isInContext(file: PsiFile, offset: Int): Boolean {
		val element = file.findElementAt(offset)
		return element?.parentOfType<ParadoxLocalisationPropertyValue>(true) != null
	}
}
