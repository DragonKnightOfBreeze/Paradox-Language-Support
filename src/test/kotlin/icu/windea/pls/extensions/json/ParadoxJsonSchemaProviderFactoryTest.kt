package icu.windea.pls.extensions.json

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxJsonSchemaProviderFactory
 */
@RunWith(JUnit4::class)
class ParadoxJsonSchemaProviderFactoryTest : BasePlatformTestCase() {
    @Test
    fun testGetProviders() {
        val providers = ParadoxJsonSchemaProviderFactory().getProviders(project)

        assertEquals(2, providers.size)
        assertTrue(providers[0] is ParadoxLauncherSettingsJsonSchemaProvider)
        assertTrue(providers[1] is ParadoxMetadataJsonSchemaProvider)
    }

    @Test
    fun testGetProviders_returnsStableProviders() {
        val factory = ParadoxJsonSchemaProviderFactory()

        assertSame(factory.getProviders(project), factory.getProviders(project))
    }

}
