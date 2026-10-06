package icu.windea.pls.lang.resolve.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.psi.PsiReadWriteAccessAwareElement
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptParameter

object ParadoxReadWriteAccessFactory {
    fun from(element: PsiElement): ReadWriteAccess {
        return when {
            element is PsiReadWriteAccessAwareElement -> element.readWriteAccess
            element is ParadoxScriptParameter -> ReadWriteAccess.Read
            element is ParadoxScriptConditionParameter -> ReadWriteAccess.Read
            else -> ReadWriteAccess.Write
        }
    }

    fun from(configExpression: CwtDataExpression): Access {
        return when (configExpression.type) {
            CwtDataTypes.EnumValue -> Access.Read
            CwtDataTypes.Value -> Access.Read
            CwtDataTypes.ValueSet -> Access.Write
            CwtDataTypes.DynamicValue -> Access.ReadWrite
            CwtDataTypes.Parameter -> Access.Write
            CwtDataTypes.LocalisationParameter -> Access.Write
            CwtDataTypes.ShaderEffect -> Access.Read
            CwtDataTypes.MeshLocator -> Access.Read
            else -> Access.ReadWrite
        }
    }
}
