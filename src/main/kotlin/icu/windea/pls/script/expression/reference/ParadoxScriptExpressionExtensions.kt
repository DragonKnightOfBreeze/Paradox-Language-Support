package icu.windea.pls.script.expression.reference

import com.intellij.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.script.references.*

fun PsiReference.canResolveParameter() : Boolean{
	return when(this){
		is ParadoxScriptExpressionReference -> this.isKey
		is ParadoxSvParameterReference -> true
		is ParadoxParameterPsiReference -> true
		else -> false
	}
}

fun PsiReference.canResolveValueSetValue(): Boolean{
	return when(this){
		is ParadoxScriptExpressionReference -> true
		is ParadoxScriptScopeFieldDataSourceReference -> true
		is ParadoxScriptValueFieldDataSourceReference -> true
		is ParadoxScriptValueSetValueReference -> true
		is ParadoxLocalisationCommandScopePsiReference -> true //value[event_target], value[global_event_target]
		is ParadoxLocalisationCommandFieldPsiReference -> true //value[variable]
		else -> false
	}
}
