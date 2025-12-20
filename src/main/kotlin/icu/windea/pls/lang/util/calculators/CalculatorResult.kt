package icu.windea.pls.lang.util.calculators

interface CalculatorResult {
    var value: Float

    fun resolveValue(): Number = value
}
