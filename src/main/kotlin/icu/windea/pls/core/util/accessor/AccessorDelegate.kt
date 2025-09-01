package icu.windea.pls.core.util.accessor

/**
 * 访问器委托。
 *
 * 提供底层可访问性控制的能力，例如反射场景下设置可访问标记。
 */
interface AccessorDelegate {
    /** 尝试设置可访问，返回是否成功。 */
    fun setAccessible(): Boolean
}
