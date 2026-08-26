package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ReadWriteAccess
 */
class ReadWriteAccessTest {
    @Test
    fun optimized_deoptimized_roundTrip_test() {
        for (access in ReadWriteAccess.entries) {
            Assert.assertEquals(access, ReadWriteAccessC.deoptimized(access.optimized()))
        }
    }
}
