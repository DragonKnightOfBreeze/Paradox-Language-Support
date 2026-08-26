package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ProcessorFactory
 * @see ProcessorScope
 */
class ProcessorScopeTest {
    @Test
    fun processorScope_dsl_test() {
        // findFrom：结果为最后一个匹配（process 不自动停止）
        Assert.assertEquals(4, ProcessorScope.findFrom<Int>({ process(1); process(2); process(3); process(4) }) { it % 2 == 0 })

        Assert.assertEquals(listOf(1, 2, 3), ProcessorScope.collectFrom {
            process(1); process(2); process(3)
        })

        Assert.assertTrue(ProcessorScope.duplicateFrom { process(1); process(2) })
        Assert.assertFalse(ProcessorScope.duplicateFrom { process(1) })

        Assert.assertTrue(ProcessorScope.allFrom({ process(1); process(2) }) { it > 0 })
        Assert.assertFalse(ProcessorScope.allFrom({ process(1); process(-1) }) { it > 0 })

        Assert.assertTrue(ProcessorScope.anyFrom({ process(1); process(2); process(3) }) { it > 2 })
        Assert.assertFalse(ProcessorScope.anyFrom({ process(1); process(2) }) { it > 10 })

        Assert.assertTrue(ProcessorScope.noneFrom({ process(1); process(2) }) { it > 10 })
        Assert.assertFalse(ProcessorScope.noneFrom({ process(1); process(2) }) { it > 1 })
    }
}
