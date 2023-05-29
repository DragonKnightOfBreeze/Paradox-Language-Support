package icu.windea.pls.core.util

class CatchableLazy<T>(val delegate: Lazy<T>, val catchingAction: (Throwable) -> T) : Lazy<T> {
    override val value: T
        get() {
            return try {
                delegate.value
            } catch(e: Exception) {
                catchingAction(e)
            }
        }
    
    override fun isInitialized(): Boolean {
        return delegate.isInitialized()
    }
}

fun <T> Lazy<T>.catching(catchingAction: (Throwable) -> T): Lazy<T> = CatchableLazy(this, catchingAction)