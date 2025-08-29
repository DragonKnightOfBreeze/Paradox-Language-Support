package icu.windea.pls.localisation.psi

import icu.windea.pls.core.castOrNull
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyListImpl
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyStub

// region PSI Accessors

val ParadoxLocalisationPropertyList.greenStub: ParadoxLocalisationPropertyListStub?
    get() = this.castOrNull<ParadoxLocalisationPropertyListImpl>()?.greenStub

val ParadoxLocalisationProperty.greenStub: ParadoxLocalisationPropertyStub?
    get() = this.castOrNull<ParadoxLocalisationPropertyImpl>()?.greenStub

// endregion

// region Predicates

fun ParadoxLocalisationExpressionElement.isComplexExpression(): Boolean {
    return isCommandExpression() || isDatabaseObjectExpression()
}

fun ParadoxLocalisationExpressionElement.isCommandExpression(): Boolean {
    return this is ParadoxLocalisationCommandText // 简单判断
}

fun ParadoxLocalisationExpressionElement.isDatabaseObjectExpression(strict: Boolean = false): Boolean {
    return this is ParadoxLocalisationConceptName && (!strict || this.textContains(':')) // 简单判断
}

// endregion

