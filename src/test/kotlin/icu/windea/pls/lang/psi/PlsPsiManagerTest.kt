package icu.windea.pls.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.findChild
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class PlsPsiManagerTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private val PsiComment.commentText get() = text.trimStart('#').trim()

    @Test
    fun getAttachedComments() {
        myFixture.configureByFile("script/stubs/attached_comments.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val rootBlock = file.block!!

        val rootProperty = rootBlock.findChild<ParadoxScriptProperty> { it.name == "root" }!!
        val block = rootProperty.block!!
        val rootAttachedComments = PsiService.getAttachedComments(rootProperty).toList()
        Assert.assertEquals(1, rootAttachedComments.size)
        Assert.assertEquals("root attached comment", rootAttachedComments[0].commentText)

        val property1 = block.findChild<ParadoxScriptProperty> { it.name == "key_1" }!!
        val attachedComments1 = PsiService.getAttachedComments(property1).toList()
        Assert.assertEquals(1, attachedComments1.size)
        Assert.assertEquals("attached comment", attachedComments1[0].commentText)

        val property2 = block.findChild<ParadoxScriptProperty> { it.name == "key_2" }!!
        val attachedComments2 = PsiService.getAttachedComments(property2).toList()
        Assert.assertEquals(0, attachedComments2.size)
    }

    @Test
    fun getAttachingElement() {
        myFixture.configureByFile("script/stubs/attached_comments.test.txt")
        val file = myFixture.file as ParadoxScriptFile
        val rootBlock = file.block!!

        val rootProperty = rootBlock.findChild<ParadoxScriptProperty> { it.name == "root" }!!
        val block = rootProperty.block!!

        val rootComment1 = rootBlock.findChild<PsiComment> { it.commentText == "root attached comment" }!!
        Assert.assertEquals(rootProperty, PsiService.getAttachingElement(rootComment1))

        val rootComment2 = rootBlock.findChild<PsiComment> { it.commentText == "root comment" }!!
        Assert.assertNull(PsiService.getAttachingElement(rootComment2))

        val comment1 = block.findChild<PsiComment> { it.commentText == "comment" }!!
        Assert.assertNull(PsiService.getAttachingElement(comment1))

        val comment2 = block.findChild<PsiComment> { it.commentText == "attached comment" }!!
        Assert.assertNotNull(PsiService.getAttachingElement(comment2))

        val comment3 = block.findChild<PsiComment> { it.commentText == "detached comment" }!!
        Assert.assertNull(PsiService.getAttachingElement(comment3))
    }
}
