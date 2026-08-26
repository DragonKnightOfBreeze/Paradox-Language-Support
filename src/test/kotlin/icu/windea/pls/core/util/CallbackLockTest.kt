package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see CallbackLock
 */
class CallbackLockTest {
    @Test
    fun check_and_reset_test() {
        val lock = CallbackLock()
        Assert.assertTrue(lock.check("a"))
        Assert.assertFalse(lock.check("a")) // 重复键返回 false
        Assert.assertTrue(lock.check("b"))
        lock.reset()
        Assert.assertTrue(lock.check("a")) // 重置后可再次通过
    }
}
