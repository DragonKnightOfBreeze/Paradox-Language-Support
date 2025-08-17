package icu.windea.pls.lang.util

import com.intellij.testFramework.*
import com.intellij.testFramework.fixtures.*

@TestDataPath("\$CONTENT_ROOT/testData")
class PlsPsiManagerTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testGetAttachedComment() {
        myFixture.configureByFile("script/t_attached_comments.txt")
        val language = myFixture.file.language
        println(language)
        // TODO: Create or load a PSI element with an attached comment
        // val element = ...
        // val comment = getAttachedComment(element)
        // assertEquals(expectedComment, comment)
    }

    fun testGetAttachingElement() {
        // TODO: Create or load a comment PSI element
        // val commentElement = ...
        // val attachingElement = getAttachingElement(commentElement)
        // assertEquals(expectedElement, attachingElement)
    }
}
