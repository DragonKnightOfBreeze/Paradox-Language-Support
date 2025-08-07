@file:Suppress("unused")

package icu.windea.pls.core.util.accessor

class UnsupportedAccessorException : UnsupportedOperationException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
