package icu.windea.pls.core.collections

import org.junit.Assert
import org.junit.Test

/**
 * @see WalkingSequence
 */
class WalkingSequenceTest {
    @Test
    fun delegate_iteration_test() {
        val seq = WalkingSequence(listOf(1, 2, 3).asSequence())
        Assert.assertEquals(listOf(1, 2, 3), seq.toList())
    }

    @Test
    fun empty_by_default_test() {
        Assert.assertEquals(emptyList<Int>(), WalkingSequence<Int>().toList())
    }

    @Test
    fun transform_preserves_context_test() {
        val seq = WalkingSequence(listOf(1, 2, 3).asSequence())
        val transformed = seq.transform { map { it * 2 } }
        Assert.assertEquals(listOf(2, 4, 6), transformed.toList())
        // transform 后保留原上下文
        Assert.assertSame(seq.context, transformed.context)
    }

    @Test
    fun forward_default_true_test() {
        val seq = WalkingSequence<Int>()
        Assert.assertTrue(seq.context.forward)
    }

    @Test
    fun context_builder_sets_forward_test() {
        val seq = WalkingSequence<Int>()
        seq.context { forward(false) }
        Assert.assertFalse(seq.context.forward)
        // context 返回自身
        Assert.assertSame(seq, seq.context { forward(true) })
        Assert.assertTrue(seq.context.forward)
    }

    @Test
    fun forward_direct_assignment_test() {
        val context = WalkingContext()
        Assert.assertTrue(context.forward)
        context.forward = false
        Assert.assertFalse(context.forward)
    }

    @Test
    fun builder_plus_returns_value_test() {
        val builder = WalkingContext.Builder(WalkingContext())
        Assert.assertEquals(5, builder + 5)
        Assert.assertEquals("x", builder + "x")
    }
}
