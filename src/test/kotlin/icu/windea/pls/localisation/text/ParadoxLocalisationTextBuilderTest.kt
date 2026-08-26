package icu.windea.pls.localisation.text

import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLocalisationTextBuilder
 */
class ParadoxLocalisationTextBuilderTest {
    @Test
    fun colorfulText() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("§Rtext§!",colorfulText("R", "text"))
        }
    }

    @Test
    fun parameter() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("\$name$", parameter("name"))
        }
    }

    @Test
    fun parameter_withArgument() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("\$name|arg$", parameter("name", "arg"))
        }
    }

    @Test
    fun scriptedVariableReference() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("$@name$", scriptedVariableReference("name"))
        }
    }

    @Test
    fun command() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("[name]", command("name"))
        }
    }

    @Test
    fun icon() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("£name£", icon("name"))
        }
    }

    @Test
    fun icon_withArgument() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("£name|arg£", icon("name", "arg"))
        }
    }

    @Test
    fun conceptCommand() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("['name']", conceptCommand("name"))
        }
    }

    @Test
    fun conceptCommand_withText() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("['name', text]", conceptCommand("name", "text"))
        }
    }

    @Test
    fun textFormat() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("#name text#!", textFormat("name", "text"))
        }
    }

    @Test
    fun textIcon() {
        with(ParadoxLocalisationTextBuilder) {
            Assert.assertEquals("@name!", textIcon("name"))
        }
    }

    @Test
    fun complex() {
        val expect = "colorful text: §RRed text§!, parameter: \$NAME$, command : [Root.GetName]"
        val actual = buildLocalisationText {
            "colorful text: ${colorfulText("R", "Red text")}, parameter: ${parameter("NAME")}, command : ${command("Root.GetName")}"
        }
        Assert.assertEquals(expect, actual)
    }
}
