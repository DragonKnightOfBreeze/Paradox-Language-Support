package icu.windea.pls.lang.psi

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see CwtPsiService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtPsiServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun getOwnedDocComments_basic() {
        // TODO 3.0.1+
    }

    @Test
    fun getDocCommentText_basic() {
        // TODO 3.0.1+
    }
}
