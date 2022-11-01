package icu.windea.pls.script.expression.reference

import com.intellij.psi.*
import icu.windea.pls.localisation.reference.*
import icu.windea.pls.script.reference.*

fun PsiReference.canResolveParameter() : Boolean{
	return when(this){
		is ParadoxScriptExpressionReference -> this.isKey
		is ParadoxOuterParameterReference -> true
		is ParadoxParameterReference -> true
		else -> false
	}
}

fun PsiReference.canResolveValueSetValue(): Boolean{
	return when(this){
		is ParadoxScriptExpressionReference -> true
		is ParadoxScriptScopeFieldDataSourceReference -> true
		is ParadoxScriptValueFieldDataSourceReference -> true
		is ParadoxScriptValueSetValueReference -> true
		is ParadoxLocalisationCommandScopeReference -> true //value[event_target], value[global_event_target]
		is ParadoxLocalisationCommandFieldReference -> true //value[variable]
		else -> false
	}
}