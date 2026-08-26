package icu.windea.pls.core.optimizer

import org.junit.Assert
import org.junit.Test

/**
 * @see ByteOptimizer
 */
class ByteOptimizerTest {
    @Test
    fun optimize_deoptimize_roundTrip_test() {
        val optimizer: ByteOptimizer<String> = OptimizerFactory.create(
            optimizeAction = { it.length.toByte() },
            deoptimizeAction = { it.toInt().toString() },
        )
        Assert.assertEquals(3.toByte(), optimizer.optimize("abc"))
        Assert.assertEquals(3.toByte(), optimizer.optimizeByte("abc"))
        Assert.assertEquals("3", optimizer.deoptimize(3.toByte()))
        Assert.assertEquals("3", optimizer.deoptimizeByte(3.toByte()))
    }
}
