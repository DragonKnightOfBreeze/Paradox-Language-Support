package icu.windea.pls.core.math

/**
 * 数学运算符。
 *
 * @property precedence 运算优先级。
 * @property rightAssociative 是否是左相关的。
 */
sealed interface MathOperator {
    val precedence: Int
    val rightAssociative: Boolean

    sealed interface Unary : MathOperator {
        override val rightAssociative get() = true

        data object Plus : Unary {
            override val precedence get() = 3
        }

        data object Minus : Unary {
            override val precedence get() = 3
        }

        data object Abs : Unary {
            override val precedence get() = 3
        }
    }

    sealed interface Binary : MathOperator {
        override val rightAssociative get() = false

        data object Plus : Binary {
            override val precedence get() = 1
        }

        data object Minus : Binary {
            override val precedence get() = 1
        }

        data object Times : Binary {
            override val precedence get() = 2
        }

        data object Div : Binary {
            override val precedence get() = 2
        }

        data object Mod : Binary {
            override val precedence get() = 2
        }

        data object Pow : Binary {
            override val precedence get() = 2
        }
    }
}
