package icu.windea.pls.script.text

import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxScriptTextBuilder
 */
class ParadoxScriptTextBuilderTest {
    @Test
    fun inlineMath() {
        Assert.assertEquals("@[ 1+1 ]", ParadoxScriptTextBuilder.inlineMath("1+1"))
    }

    @Test
    fun parameter() {
        Assert.assertEquals("\$foo\$", ParadoxScriptTextBuilder.parameter("foo"))
    }

    @Test
    fun parameter_withDefault() {
        Assert.assertEquals("\$foo|bar\$", ParadoxScriptTextBuilder.parameter("foo", "bar"))
    }

    @Test
    fun conditionalBlock() {
        Assert.assertEquals("[[expr] block ]", ParadoxScriptTextBuilder.conditionalBlock("expr", "block"))
    }
}
