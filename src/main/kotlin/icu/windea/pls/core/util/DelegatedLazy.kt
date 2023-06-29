package icu.windea.pls.core.util

class DelegatedLazy<T>(val delegate: Lazy<T>, val delegateAction: Lazy<T>.() -> T): Lazy<T> {
    override val value: T get() = delegate.delegateAction()
    
    override fun isInitialized(): Boolean {
        return delegate.isInitialized()
    }
}

fun <T> Lazy<T>.delegatedBy(delegateAction: Lazy<T>.() -> T): Lazy<T> = DelegatedLazy(this, delegateAction)