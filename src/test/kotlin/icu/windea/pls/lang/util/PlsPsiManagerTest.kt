package icu.windea.pls.lang.util

import com.intellij.psi.PsiComment
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.findChild
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class PlsPsiManagerTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testGetAttachedComments() {
        myFixture.configureByFile("script/t_attached_comments.txt")
        val file = myFixture.file as ParadoxScriptFile
        val rootBlock = file.block!!

        val rootProperty = rootBlock.findChild<ParadoxScriptProperty> { it.name == "root" }!!
        val block = rootProperty.block!!
        val rootAttachedComments = PlsPsiManager.getAttachedComments(rootProperty)
        Assert.assertEquals(1, rootAttachedComments.size)
        Assert.assertEquals("root attached comment", rootAttachedComments[0].commentText)

        val property1 = block.findChild<ParadoxScriptProperty> { it.name == "key_1" }!!
        val attachedComments1 = PlsPsiManager.getAttachedComments(property1)
        Assert.assertEquals(1, attachedComments1.size)
        Assert.assertEquals("attached comment", attachedComments1[0].commentText)

        val property2 = block.findChild<ParadoxScriptProperty> { it.name == "key_2" }!!
        val attachedComments2 = PlsPsiManager.getAttachedComments(property2)
        Assert.assertEquals(0, attachedComments2.size)
    }

    fun testGetAttachingElement() {
        myFixture.configureByFile("script/t_attached_comments.txt")
        val file = myFixture.file as ParadoxScriptFile
        val rootBlock = file.block!!

        val rootProperty = rootBlock.findChild<ParadoxScriptProperty> { it.name == "root" }!!
        val block = rootProperty.block!!

        val rootComment1 = rootBlock.findChild<PsiComment> { it.commentText == "root attached comment" }!!
        Assert.assertEquals(rootProperty, PlsPsiManager.getAttachingElement(rootComment1))

        val rootComment2 = rootBlock.findChild<PsiComment> { it.commentText == "root comment" }!!
        Assert.assertNull(PlsPsiManager.getAttachingElement(rootComment2))

        val comment1 = block.findChild<PsiComment> { it.commentText == "comment" }!!
        Assert.assertNull(PlsPsiManager.getAttachingElement(comment1))

        val comment2 = block.findChild<PsiComment> { it.commentText == "attached comment" }!!
        Assert.assertNotNull(PlsPsiManager.getAttachingElement(comment2))

        val comment3 = block.findChild<PsiComment> { it.commentText == "detached comment" }!!
        Assert.assertNull(PlsPsiManager.getAttachingElement(comment3))
    }

    private val PsiComment.commentText get() = text.trimStart('#').trim()
}
