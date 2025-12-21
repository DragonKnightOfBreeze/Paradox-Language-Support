package icu.windea.pls.lang.util.calculators

sealed interface MathToken {
    fun render(): String

    data class Operand(val operand: MathCalculationResult) : MathToken {
        override fun render() = operand.normalized().toString()
    }

    sealed interface Operator : MathToken {
        data object Plus : Operator {
            override fun render() = "+"
        }

        data object Minus : Operator {
            override fun render() = "-"
        }

        data object Times : Operator {
            override fun render() = "*"
        }

        data object Div : Operator {
            override fun render() = "/"
        }

        data object Mod : Operator {
            override fun render() = "%"
        }

        data object Pow : Operator {
            override fun render() = "^"
        }

        data object LeftAbs : Operator {
            override fun render() = "|"
        }

        data object RightAbs : Operator {
            override fun render() = "|"
        }

        data object LeftPar : Operator {
            override fun render() = "("
        }

        data object RightPar : Operator {
            override fun render() = ")"
        }
    }
}
