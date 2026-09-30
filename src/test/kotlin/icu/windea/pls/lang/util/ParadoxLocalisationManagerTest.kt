package icu.windea.pls.lang.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLocalisationManager
 */
class ParadoxLocalisationManagerTest {
    @Test
    fun isNormalLocalisationText_test() {
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText(""))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText(" "))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc"))

        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc["))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc[["))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\["))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc]"))

        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc$"))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc\\$"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc§"))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc\\§"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc£"))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc\\£"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc#"))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc\\#"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc@"))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc\\@"))

        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc|||def"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc|def")) // also true
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc\\|def"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc&!t"))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc&t")) // also true
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc\\&t"))

        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText(" ", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc", checkEscape = false))

        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc[", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc[[", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\[", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isRichLocalisationText("abc]", checkEscape = false))

        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc$", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\$", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc§", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\§", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc£", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\£", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc#", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\#", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc@", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\@", checkEscape = false))

        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc|||def", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc|def", checkEscape = false)) // also true
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\|def", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc&!t", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc&t", checkEscape = false)) // also true
        Assert.assertTrue(ParadoxLocalisationManager.isRichLocalisationText("abc\\&t", checkEscape = false))
    }
}
