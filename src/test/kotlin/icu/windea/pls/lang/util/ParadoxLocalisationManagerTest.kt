package icu.windea.pls.lang.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxLocalisationManager
 */
class ParadoxLocalisationManagerTest {
    @Test
    fun isRichText_test() {
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText(""))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText(" "))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc"))

        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc["))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc[["))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\["))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc]"))

        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc$"))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc\\$"))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc§"))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc\\§"))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc£"))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc\\£"))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc#"))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc\\#"))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc@"))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc\\@"))

        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc|||def"))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc|def")) // also true
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc\\|def"))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc&!t"))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc&t")) // also true
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc\\&t"))

        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText(" ", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc", checkEscape = false))

        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc[", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc[[", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\[", checkEscape = false))
        Assert.assertFalse(ParadoxLocalisationManager.isNormalLocalisationText("abc]", checkEscape = false))

        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc$", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\$", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc§", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\§", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc£", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\£", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc#", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\#", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc@", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\@", checkEscape = false))

        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc|||def", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc|def", checkEscape = false)) // also true
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\|def", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc&!t", checkEscape = false))
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc&t", checkEscape = false)) // also true
        Assert.assertTrue(ParadoxLocalisationManager.isNormalLocalisationText("abc\\&t", checkEscape = false))
    }
}
