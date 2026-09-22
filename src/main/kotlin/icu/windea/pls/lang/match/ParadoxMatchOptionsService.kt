package icu.windea.pls.lang.match

object ParadoxMatchOptionsService {
    fun isDumb(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipIndex || options.skipScope || ParadoxMatchOptions.isDumb()
    }

    fun lenient(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.lenient
    }

    fun forExpression(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.forExpression
    }

    fun forDeclarationRoot(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.forDeclarationRoot
    }

    fun skipBlock(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipBlock
    }

    fun skipIndex(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipIndex || ParadoxMatchOptions.isDumb()
    }

    fun skipScope(options: ParadoxMatchOptions? = null): Boolean {
        val options = options.normalized()
        return options.skipScope || ParadoxMatchOptions.isDumb()
    }
}
