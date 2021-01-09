package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationGoToDeclarationHandler : GotoDeclarationHandler {
	override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor?): Array<out PsiElement?>? {
		return when(sourceElement) {
			is ParadoxLocalisationProperty -> {
				val name = sourceElement.name
				val locale = (sourceElement.containingFile as? ParadoxLocalisationFile)?.paradoxLocale
				//查找当前项目
				findLocalisationProperties(name, locale, sourceElement.project).toTypedArray()
			}
			else -> null
		}
	}
}
