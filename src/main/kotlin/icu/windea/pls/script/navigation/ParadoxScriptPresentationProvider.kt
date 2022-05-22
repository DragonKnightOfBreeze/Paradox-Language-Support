package icu.windea.pls.script.navigation

import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPresentationProvider : ItemPresentationProvider<NavigatablePsiElement> {
	override fun getPresentation(item: NavigatablePsiElement): ItemPresentation? {
		return when {
			item is ParadoxScriptFile -> ParadoxScriptFilePresentation(item)
			item is ParadoxScriptVariable -> ParadoxScriptVariablePresentation(item)
			item is IParadoxScriptParameter -> ParadoxScriptParameterPresentation(item)
			item is ParadoxScriptProperty -> ParadoxScriptPropertyPresentation(item)
			else -> null
		}
	}
}