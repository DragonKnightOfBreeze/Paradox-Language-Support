package icu.windea.pls.localisation.text

import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLocalisationTextBuilder
 */
class ParadoxLocalisationTextBuilderTest {
    @Test
    fun colorfulText() {
        Assert.assertEquals("§Rtext§!", ParadoxLocalisationTextBuilder.colorfulText("R", "text"))
    }

    @Test
    fun parameter() {
        Assert.assertEquals("\$name\$", ParadoxLocalisationTextBuilder.parameter("name"))
    }

    @Test
    fun parameter_withArgument() {
        Assert.assertEquals("\$name|arg\$", ParadoxLocalisationTextBuilder.parameter("name", "arg"))
    }

    @Test
    fun scriptedVariableReference() {
        Assert.assertEquals("\$@name\$", ParadoxLocalisationTextBuilder.scriptedVariableReference("name"))
    }

    @Test
    fun command() {
        Assert.assertEquals("[name]", ParadoxLocalisationTextBuilder.command("name"))
    }

    @Test
    fun icon() {
        Assert.assertEquals("£name£", ParadoxLocalisationTextBuilder.icon("name"))
    }

    @Test
    fun icon_withArgument() {
        Assert.assertEquals("£name|arg£", ParadoxLocalisationTextBuilder.icon("name", "arg"))
    }

    @Test
    fun conceptCommand() {
        Assert.assertEquals("['name']", ParadoxLocalisationTextBuilder.conceptCommand("name"))
    }

    @Test
    fun conceptCommand_withText() {
        Assert.assertEquals("['name', text]", ParadoxLocalisationTextBuilder.conceptCommand("name", "text"))
    }

    @Test
    fun textFormat() {
        Assert.assertEquals("#name text#!", ParadoxLocalisationTextBuilder.textFormat("name", "text"))
    }

    @Test
    fun textIcon() {
        Assert.assertEquals("@name!", ParadoxLocalisationTextBuilder.textIcon("name"))
    }
}
