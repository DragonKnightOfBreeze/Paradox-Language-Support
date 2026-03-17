package icu.windea.pls.lang.util

import org.junit.Assert
import org.junit.Test

class ParadoxLocalisationManagerTest {
    @Test
    fun isRichTextTest() {
        Assert.assertFalse(ParadoxLocalisationManager.isRichText(""))
        Assert.assertFalse(ParadoxLocalisationManager.isRichText(" "))
        Assert.assertFalse(ParadoxLocalisationManager.isRichText("abc"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichText("abc["))
        Assert.assertTrue(ParadoxLocalisationManager.isRichText("abc[["))
        Assert.assertFalse(ParadoxLocalisationManager.isRichText("abc]"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichText("abc$"))
        Assert.assertFalse(ParadoxLocalisationManager.isRichText("abc\\$"))

        Assert.assertFalse(ParadoxLocalisationManager.isRichText("", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isRichText(" ", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isRichText("abc", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichText("abc[", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichText("abc[[", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isRichText("abc]", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichText("abc$", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichText("abc\\$", checkEscape = false))
    }
}
