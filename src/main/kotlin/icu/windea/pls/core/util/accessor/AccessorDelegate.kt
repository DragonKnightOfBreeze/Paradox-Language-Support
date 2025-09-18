package icu.windea.pls.core.util.accessor

interface AccessorDelegate {
    /**
     * 尝试提升可访问性（如设置 Kotlin/Java 反射对象为可访问）。
     *
     * 返回是否成功，失败时调用方将回退到其他策略。
     */
    fun setAccessible(): Boolean
}
