@file:Suppress("unused")

package icu.windea.pls.localisation.psi

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

