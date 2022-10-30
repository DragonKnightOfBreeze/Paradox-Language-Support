package icu.windea.pls.script.expression.reference

import com.intellij.psi.*
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
		else -> false
	}
}