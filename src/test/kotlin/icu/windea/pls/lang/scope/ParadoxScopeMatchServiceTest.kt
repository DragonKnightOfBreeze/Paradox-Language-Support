package icu.windea.pls.lang.scope

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.promotions
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxScopeMatchService
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxScopeMatchServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("chronicle")
        markConfigDirectory("features/scope/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region matchesScopeId

    @Test
    fun matchesScopeId_forStellaris_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "country", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("country", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("system", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("galactic_object", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("carrier", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("planet", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("ship", "any", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("ship", "?", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "planet", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "ship", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "planet", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "ship", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("country", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("country", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "ship", configGroup))
    }

    @Test
    fun matchesScopeId_forVic3_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "country", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("country", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("system", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("galactic_object", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("carrier", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("planet", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("ship", "any", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("ship", "?", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "planet", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "ship", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "?", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "planet", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "ship", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("country", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("country", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "ship", configGroup))
    }

    @Test
    fun matchesScopeId_forStellaris_byAlias() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        assertTrue(ParadoxScopeMatchService.matchesScopeId("system", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("system", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("system", "galactic_object", configGroup)) // base VS alias -> match
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "ship", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("galactic_object", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("galactic_object", "system", configGroup)) // base VS alias -> match
        assertTrue(ParadoxScopeMatchService.matchesScopeId("galactic_object", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "ship", configGroup))
    }

    @Test
    fun matchesScopeId_forVic3_byAlias() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        assertTrue(ParadoxScopeMatchService.matchesScopeId("system", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("system", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "galactic_object", configGroup)) // base VS alias -> mismatched game type
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "ship", configGroup))

        assertTrue(ParadoxScopeMatchService.matchesScopeId("galactic_object", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "system", configGroup)) // base VS alias ->  mismatched game type
        assertTrue(ParadoxScopeMatchService.matchesScopeId("galactic_object", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "ship", configGroup))
    }

    @Test
    fun matchesScopeId_forStellaris_byParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        assertTrue(ParadoxScopeMatchService.matchesScopeId("carrier", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("carrier", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "planet", configGroup)) // parent VS child -> not match
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "ship", configGroup)) // parent VS child -> not match

        assertTrue(ParadoxScopeMatchService.matchesScopeId("planet", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("planet", "carrier", configGroup)) // child VS parent -> match
        assertTrue(ParadoxScopeMatchService.matchesScopeId("planet", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "ship", configGroup)) // child VS another child -> not match
    }

    @Test
    fun matchesScopeId_forVic3_byParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        assertTrue(ParadoxScopeMatchService.matchesScopeId("carrier", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("carrier", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "planet", configGroup)) // parent VS child -> not match
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "ship", configGroup)) // parent VS child -> not match

        assertTrue(ParadoxScopeMatchService.matchesScopeId("planet", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "carrier", configGroup)) // child VS parent ->  mismatched game type
        assertTrue(ParadoxScopeMatchService.matchesScopeId("planet", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("planet", "ship", configGroup)) // child VS another child -> not match
    }

    @Test
    fun matchesScopeId_forStellaris_specialScopes() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // null scope always matches
        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "all", configGroup))

        // "all" is not normalized by the match service itself, and only matches literally or via wildcard scope
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "all", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "all", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("all", "all", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("all", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("all", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("all", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "all", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "all", configGroup))

        // empty scope ids only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScopeId("", "", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "", configGroup))
    }

    @Test
    fun matchesScopeId_forVic3_specialScopes() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // null scope always matches
        assertTrue(ParadoxScopeMatchService.matchesScopeId(null, "all", configGroup))

        // "all" is not normalized by the match service itself, and only matches literally or via wildcard scope
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "all", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "all", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("all", "all", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("all", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("all", "?", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("all", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "all", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "all", configGroup))

        // empty scope ids only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScopeId("", "", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("", "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("any", "", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("?", "", configGroup))
    }

    @Test
    fun matchesScopeId_forStellaris_caseSensitive() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // scope ids are compared case-sensitively, and are not normalized during matching
        assertFalse(ParadoxScopeMatchService.matchesScopeId("Country", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "Country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("ANY", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "ANY", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "System", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("System", "system", configGroup))
    }

    @Test
    fun matchesScopeId_forVic3_caseSensitive() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // scope ids are compared case-sensitively, and are not normalized during matching
        assertFalse(ParadoxScopeMatchService.matchesScopeId("Country", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "Country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("ANY", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "ANY", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("system", "System", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("System", "system", configGroup))
    }

    @Test
    fun matchesScopeId_forStellaris_otherScopes() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // colony has no is_subscope_of declared, so it never matches carrier
        assertTrue(ParadoxScopeMatchService.matchesScopeId("colony", "colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("colony", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("colony", "planet", configGroup))

        // ship is a sub scope of carrier
        assertTrue(ParadoxScopeMatchService.matchesScopeId("ship", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "ship", configGroup))

        // unrelated scopes never match
        assertTrue(ParadoxScopeMatchService.matchesScopeId("fleet", "fleet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("fleet", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("leader", "leader", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("leader", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "fleet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "carrier", configGroup))

        // unresolved scopes only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "?", configGroup))
    }

    @Test
    fun matchesScopeId_forVic3_otherScopes() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // colony has no is_subscope_of declared, so it never matches carrier
        assertTrue(ParadoxScopeMatchService.matchesScopeId("colony", "colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("colony", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("colony", "planet", configGroup))

        // mismatched game type -> no scope model available
        assertFalse(ParadoxScopeMatchService.matchesScopeId("ship", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "ship", configGroup))

        // unrelated scopes never match
        assertTrue(ParadoxScopeMatchService.matchesScopeId("fleet", "fleet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("fleet", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("leader", "leader", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("leader", "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("country", "fleet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("galactic_object", "carrier", configGroup))

        // unresolved scopes only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "__unresolved__", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "any", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("__unresolved__", "?", configGroup))
    }

    @Test
    fun matchesScopeId_forStellaris_byAliasAndParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // alias of a sub scope matches its parent
        assertTrue(ParadoxScopeMatchService.matchesScopeId("world", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("celestial_body", "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("world", "celestial_body", configGroup)) // alias VS another alias -> match
        assertTrue(ParadoxScopeMatchService.matchesScopeId("celestial_body", "world", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("celestial_body", "planet", configGroup)) // unrelated scopes -> not match
        assertFalse(ParadoxScopeMatchService.matchesScopeId("world", "ship", configGroup))

        // parent declared via alias: both base name and aliases are matched
        assertTrue(ParadoxScopeMatchService.matchesScopeId("moon", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("moon", "system", configGroup))

        // parent declared via base name: the parent's alias is also matched
        assertTrue(ParadoxScopeMatchService.matchesScopeId("star", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("star", "galactic_object", configGroup))

        // multi-level parents are collected recursively
        assertTrue(ParadoxScopeMatchService.matchesScopeId("army", "ship", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("army", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("army", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "army", configGroup)) // parent VS child -> not match
        assertFalse(ParadoxScopeMatchService.matchesScopeId("ship", "army", configGroup)) // parent VS child -> not match

        // multi-level parent chain through an alias-declared parent
        assertTrue(ParadoxScopeMatchService.matchesScopeId("satellite", "moon", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("satellite", "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("satellite", "system", configGroup)) // recursion stops at the alias-declared parent

        // cyclic parents are guarded during scope model computation
        assertTrue(ParadoxScopeMatchService.matchesScopeId("alpha", "alpha", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("alpha", "beta", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("beta", "alpha", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("beta", "beta", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("alpha", "carrier", configGroup))
    }

    @Test
    fun matchesScopeId_forVic3_byAliasAndParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> alias of a sub scope does not match its parent
        assertFalse(ParadoxScopeMatchService.matchesScopeId("world", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("celestial_body", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("world", "celestial_body", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("celestial_body", "world", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("celestial_body", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("world", "ship", configGroup))

        // mismatched game type -> parents are not matched at all
        assertFalse(ParadoxScopeMatchService.matchesScopeId("moon", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("moon", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("star", "star", configGroup)) // exact match still works
        assertFalse(ParadoxScopeMatchService.matchesScopeId("star", "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("star", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("army", "ship", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("army", "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("army", "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("carrier", "army", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("ship", "army", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("satellite", "moon", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("satellite", "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("satellite", "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("alpha", "alpha", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("alpha", "beta", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("beta", "alpha", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeId("beta", "beta", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeId("alpha", "carrier", configGroup))
    }

    // endregion

    // region matchesScope (ParadoxScope)

    @Test
    fun matchesScope_forStellaris_withScope() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val nullScope: ParadoxScope? = null

        // null scope always matches
        assertTrue(ParadoxScopeMatchService.matchesScope(nullScope, "country", configGroup))

        // wildcard scopes
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("any"), "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("all"), "country", configGroup)) // "all" resolves to the any scope
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("?"), "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("?"), "?", configGroup))

        // exact matches
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("country"), "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("country"), "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("country"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("planet"), "planet", configGroup))

        // by alias
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("system"), "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("galactic_object"), "system", configGroup))

        // by parent (parent scope will not match child scope)
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("planet"), "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("ship"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("carrier"), "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("carrier"), "ship", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("colony"), "carrier", configGroup))

        // case-sensitive
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("Country"), "country", configGroup))

        // unresolved scopes only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("__unresolved__"), "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("__unresolved__"), "country", configGroup))
    }

    @Test
    fun matchesScope_forVic3_withScope() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val nullScope: ParadoxScope? = null

        // null scope always matches
        assertTrue(ParadoxScopeMatchService.matchesScope(nullScope, "country", configGroup))

        // wildcard scopes
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("any"), "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("all"), "country", configGroup)) // "all" resolves to the any scope
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("?"), "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("?"), "?", configGroup))

        // exact matches
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("country"), "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("country"), "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("country"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("planet"), "planet", configGroup))

        // mismatched game type -> alias does not match
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("system"), "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("galactic_object"), "system", configGroup))

        // mismatched game type -> parent does not match
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("planet"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("ship"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("carrier"), "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("carrier"), "ship", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("colony"), "carrier", configGroup))

        // case-sensitive
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("Country"), "country", configGroup))

        // unresolved scopes only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("__unresolved__"), "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("__unresolved__"), "country", configGroup))
    }

    @Test
    fun matchesScope_forStellaris_byAliasAndParent_withScope() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // alias of a sub scope matches its parent
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("celestial_body"), "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("celestial_body"), "world", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("world"), "ship", configGroup))

        // parent declared via alias: both base name and aliases are matched
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("moon"), "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("moon"), "system", configGroup))

        // parent declared via base name: the parent's alias is also matched
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("star"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("star"), "galactic_object", configGroup))

        // multi-level parents are collected recursively
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("army"), "ship", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("army"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("carrier"), "army", configGroup))

        // cyclic parents are guarded during scope model computation
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("alpha"), "beta", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("beta"), "alpha", configGroup))
    }

    @Test
    fun matchesScope_forVic3_byAliasAndParent_withScope() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> alias of a sub scope does not match its parent
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("celestial_body"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("celestial_body"), "world", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("world"), "ship", configGroup))

        // mismatched game type -> parents are not matched at all
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("moon"), "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("moon"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("star"), "star", configGroup)) // exact match still works
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("star"), "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("star"), "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("army"), "ship", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("army"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("carrier"), "army", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("alpha"), "beta", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScope.resolve("beta"), "alpha", configGroup))
    }

    // endregion

    // region matchesScope (ParadoxScopeContext)

    @Test
    fun matchesScope_forStellaris_withScopeContext() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val nullScopeContext: ParadoxScopeContext? = null

        // null context always matches
        assertTrue(ParadoxScopeMatchService.matchesScope(nullScopeContext, "country", configGroup))

        // wildcard scope contexts
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("any"), "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("?"), "country", configGroup))

        // exact matches
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("country"), "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("country"), "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("country"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("planet"), "planet", configGroup))

        // by alias
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("system"), "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("galactic_object"), "system", configGroup))

        // by parent (parent scope will not match child scope)
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("planet"), "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("ship"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), "ship", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("colony"), "carrier", configGroup))

        // case-sensitive
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("Country"), "country", configGroup))

        // unresolved scopes only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), "country", configGroup))
    }

    @Test
    fun matchesScope_forVic3_withScopeContext() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val nullScopeContext: ParadoxScopeContext? = null

        // null context always matches
        assertTrue(ParadoxScopeMatchService.matchesScope(nullScopeContext, "country", configGroup))

        // wildcard scope contexts
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("any"), "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("?"), "country", configGroup))

        // exact matches
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("country"), "any", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("country"), "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("country"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("planet"), "planet", configGroup))

        // mismatched game type -> alias does not match
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("system"), "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("galactic_object"), "system", configGroup))

        // mismatched game type -> parent does not match
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("planet"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("ship"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), "ship", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("colony"), "carrier", configGroup))

        // case-sensitive
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("Country"), "country", configGroup))

        // unresolved scopes only match themselves exactly (or wildcards)
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), "__unresolved__", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), "country", configGroup))
    }

    @Test
    fun matchesScope_forStellaris_withScopesSet() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val nullScopeContext: ParadoxScopeContext? = null
        val countryContext = ParadoxScopeContext.resolve("country")

        // null context always matches
        assertTrue(ParadoxScopeMatchService.matchesScope(nullScopeContext, emptySet(), configGroup))

        // null or empty scope sets always match
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, null, configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, emptySet(), configGroup))

        // a set that is exactly {"any"} always matches; a mixed set does not (even if it contains "any")
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, setOf("any"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("any", "system"), configGroup))

        // "?" in the set does not act as a wildcard for concrete scopes
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("?"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("?", "any"), configGroup))

        // wildcard scope contexts
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("any"), emptySet(), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("?"), setOf("country"), configGroup))

        // exact matches
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, setOf("country"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, setOf("country", "system"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("system"), configGroup))

        // by alias
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("system"), setOf("galactic_object"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("galactic_object"), setOf("system", "country"), configGroup))

        // by parent (parent scope will not match child scope)
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("planet"), setOf("carrier"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("ship"), setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), setOf("planet", "ship"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("colony"), setOf("carrier"), configGroup))

        // unresolved scopes only match themselves exactly
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), setOf("__unresolved__"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), setOf("country"), configGroup))
    }

    @Test
    fun matchesScope_forVic3_withScopesSet() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val nullScopeContext: ParadoxScopeContext? = null
        val countryContext = ParadoxScopeContext.resolve("country")

        // null context always matches
        assertTrue(ParadoxScopeMatchService.matchesScope(nullScopeContext, emptySet(), configGroup))

        // null or empty scope sets always match
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, null, configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, emptySet(), configGroup))

        // a set that is exactly {"any"} always matches; a mixed set does not (even if it contains "any")
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, setOf("any"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("any", "system"), configGroup))

        // "?" in the set does not act as a wildcard for concrete scopes
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("?"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("?", "any"), configGroup))

        // wildcard scope contexts
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("any"), emptySet(), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("?"), setOf("country"), configGroup))

        // exact matches
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, setOf("country"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, setOf("country", "system"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("system"), configGroup))

        // mismatched game type -> alias does not match
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("system"), setOf("galactic_object"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("galactic_object"), setOf("system", "country"), configGroup))

        // mismatched game type -> parent does not match
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("planet"), setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("ship"), setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), setOf("planet", "ship"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("colony"), setOf("carrier"), configGroup))

        // unresolved scopes only match themselves exactly
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), setOf("__unresolved__"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("__unresolved__"), setOf("country"), configGroup))
    }

    @Test
    fun matchesScope_forStellaris_byAliasAndParent_withScopeContext() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // alias of a sub scope matches its parent
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), "carrier", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), "world", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("world"), "ship", configGroup))

        // parent declared via alias: both base name and aliases are matched
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), "system", configGroup))

        // parent declared via base name: the parent's alias is also matched
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), "galactic_object", configGroup))

        // multi-level parents are collected recursively
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), "ship", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), "army", configGroup))

        // cyclic parents are guarded during scope model computation
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("alpha"), "beta", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("beta"), "alpha", configGroup))
    }

    @Test
    fun matchesScope_forVic3_byAliasAndParent_withScopeContext() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> alias of a sub scope does not match its parent
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), "world", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("world"), "ship", configGroup))

        // mismatched game type -> parents are not matched at all
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), "system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), "star", configGroup)) // exact match still works
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), "ship", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), "army", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("alpha"), "beta", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("beta"), "alpha", configGroup))
    }

    @Test
    fun matchesScope_forStellaris_byAliasAndParent_withScopesSet() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // alias of a sub scope matches its parent
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), setOf("carrier"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), setOf("world"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("world"), setOf("ship", "planet"), configGroup))

        // parent declared via alias: both base name and aliases are matched
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), setOf("galactic_object"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), setOf("system"), configGroup))

        // parent declared via base name: the parent's alias is also matched
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), setOf("system"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), setOf("galactic_object"), configGroup))

        // multi-level parents are collected recursively
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), setOf("ship", "planet"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), setOf("army"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), setOf("planet"), configGroup))

        // cyclic parents are guarded during scope model computation
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("alpha"), setOf("beta"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("beta"), setOf("alpha"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("alpha"), setOf("carrier"), configGroup))
    }

    @Test
    fun matchesScope_forVic3_byAliasAndParent_withScopesSet() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> alias of a sub scope does not match its parent
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("celestial_body"), setOf("world"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("world"), setOf("ship", "planet"), configGroup))

        // mismatched game type -> parents are not matched at all
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), setOf("galactic_object"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("moon"), setOf("system"), configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), setOf("star"), configGroup)) // exact match still works
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), setOf("system"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("star"), setOf("galactic_object"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), setOf("ship", "planet"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("carrier"), setOf("army"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("army"), setOf("planet"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("alpha"), setOf("beta"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("beta"), setOf("alpha"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(ParadoxScopeContext.resolve("alpha"), setOf("carrier"), configGroup))
    }

    @Test
    fun matchesScope_forStellaris_withPromotions() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // promotion matches by id, and also matches via the scope model (e.g., the promoted scope's parent)
        val countryContext = ParadoxScopeContext.resolve("country")
        countryContext.promotions = setOf("planet")
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, "planet", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, "system", configGroup))

        // promotions match against scope sets as well
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("system"), configGroup))

        // promotions match scope groups as well
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(countryContext, "planet_and_colony", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(countryContext, "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(countryContext, "galactic", configGroup))

        // promotion by alias matches the base scope id via the scope model
        val otherContext = ParadoxScopeContext.resolve("country")
        otherContext.promotions = setOf("galactic_object")
        assertTrue(ParadoxScopeMatchService.matchesScope(otherContext, "galactic_object", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(otherContext, "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(otherContext, "carrier", configGroup))

        // multiple promotions are all checked
        val multiContext = ParadoxScopeContext.resolve("country")
        multiContext.promotions = setOf("leader", "planet")
        assertTrue(ParadoxScopeMatchService.matchesScope(multiContext, "leader", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(multiContext, "planet", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(multiContext, "carrier", configGroup)) // via the second promotion's parent
        assertFalse(ParadoxScopeMatchService.matchesScope(multiContext, "system", configGroup))

        // wildcard ids in promotions do not act as wildcards
        val wildcardContext = ParadoxScopeContext.resolve("country")
        wildcardContext.promotions = setOf("any")
        assertTrue(ParadoxScopeMatchService.matchesScope(wildcardContext, "any", configGroup)) // via scopeToMatch
        assertFalse(ParadoxScopeMatchService.matchesScope(wildcardContext, "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(wildcardContext, "carrier", configGroup))

        // no promotions behave as no promotions
        val emptyContext = ParadoxScopeContext.resolve("country")
        assertFalse(ParadoxScopeMatchService.matchesScope(emptyContext, "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(emptyContext, "planet_and_colony", configGroup))
    }

    @Test
    fun matchesScope_forVic3_withPromotions() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // promotion matches by id; without a scope model the promoted scope's relations are not matched
        val countryContext = ParadoxScopeContext.resolve("country")
        countryContext.promotions = setOf("planet")
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(countryContext, "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("carrier"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(countryContext, setOf("system"), configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(countryContext, "planet_and_colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(countryContext, "carrier_based", configGroup))

        // mismatched game type -> promotion by alias does not match the base scope id
        val otherContext = ParadoxScopeContext.resolve("country")
        otherContext.promotions = setOf("galactic_object")
        assertTrue(ParadoxScopeMatchService.matchesScope(otherContext, "galactic_object", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(otherContext, "system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(otherContext, "carrier", configGroup))

        // multiple promotions are all checked; without a scope model the promoted scopes' relations are not matched
        val multiContext = ParadoxScopeContext.resolve("country")
        multiContext.promotions = setOf("leader", "planet")
        assertTrue(ParadoxScopeMatchService.matchesScope(multiContext, "leader", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScope(multiContext, "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(multiContext, "carrier", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(multiContext, "system", configGroup))

        // wildcard ids in promotions do not act as wildcards
        val wildcardContext = ParadoxScopeContext.resolve("country")
        wildcardContext.promotions = setOf("any")
        assertTrue(ParadoxScopeMatchService.matchesScope(wildcardContext, "any", configGroup)) // via scopeToMatch
        assertFalse(ParadoxScopeMatchService.matchesScope(wildcardContext, "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScope(wildcardContext, "carrier", configGroup))

        // no promotions behave as no promotions
        val emptyContext = ParadoxScopeContext.resolve("country")
        assertFalse(ParadoxScopeMatchService.matchesScope(emptyContext, "planet", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(emptyContext, "planet_and_colony", configGroup))
    }

    // endregion

    // region matchesScopeGroup

    @Test
    fun matchesScopeGroup_forStellaris_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val nullScopeContext: ParadoxScopeContext? = null

        // null context always matches (even for undefined groups)
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(nullScopeContext, "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(nullScopeContext, "nonexistent", configGroup))

        // wildcard scope contexts always match (even for undefined groups)
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("any"), "nonexistent", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("?"), "nonexistent", configGroup))

        // undefined groups do not match concrete scopes
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "nonexistent", configGroup))

        // group names are case-insensitive
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "Country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("leader"), "country", configGroup))

        // exact matches against group values
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("leader"), "leader_and_system", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("system"), "leader_and_system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "leader_and_system", configGroup))

        // by alias
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("galactic_object"), "leader_and_system", configGroup))

        // group matching is value-based (no inheritance relation between the grouped scopes is required)
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("planet"), "planet_and_colony", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("colony"), "planet_and_colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("ship"), "planet_and_colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("carrier"), "planet_and_colony", configGroup))

        // an empty group matches no concrete scope
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "empty", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("any"), "empty", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("?"), "empty", configGroup))
    }

    @Test
    fun matchesScopeGroup_forVic3_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val nullScopeContext: ParadoxScopeContext? = null

        // null context always matches (even for undefined groups)
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(nullScopeContext, "country", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(nullScopeContext, "nonexistent", configGroup))

        // wildcard scope contexts always match (even for undefined groups)
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("any"), "nonexistent", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("?"), "nonexistent", configGroup))

        // undefined groups do not match concrete scopes
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "nonexistent", configGroup))

        // mismatched game type -> no scope groups are defined at all
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "Country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("leader"), "country", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("leader"), "leader_and_system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("system"), "leader_and_system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("galactic_object"), "leader_and_system", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("planet"), "planet_and_colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("colony"), "planet_and_colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("ship"), "planet_and_colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("carrier"), "planet_and_colony", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "empty", configGroup))

        // wildcard scope contexts still match regardless of the group values
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("any"), "empty", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("?"), "empty", configGroup))
    }

    @Test
    fun matchesScopeGroup_forStellaris_byAliasAndParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // group containing the parent: sub scopes (and their aliases) match it
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("carrier"), "carrier_based", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("planet"), "carrier_based", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("ship"), "carrier_based", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("world"), "carrier_based", configGroup)) // alias of a sub scope
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("celestial_body"), "carrier_based", configGroup)) // alias of a sub scope
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("army"), "carrier_based", configGroup)) // multi-level parent
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("moon"), "carrier_based", configGroup))

        // group containing an alias: the base scope and sub scopes declared via that alias match it
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("system"), "galactic", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("galactic_object"), "galactic", configGroup))
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("moon"), "galactic", configGroup)) // sub scope declared via the alias
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("satellite"), "galactic", configGroup)) // multi-level parent via the alias
        assertTrue(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("star"), "galactic", configGroup)) // sub scope declared via the base name also matches the alias
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "galactic", configGroup))
    }

    @Test
    fun matchesScopeGroup_forVic3_byAliasAndParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> no scope groups are defined at all
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("carrier"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("planet"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("ship"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("world"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("celestial_body"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("army"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("moon"), "carrier_based", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("system"), "galactic", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("galactic_object"), "galactic", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("moon"), "galactic", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("satellite"), "galactic", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("star"), "galactic", configGroup))
        assertFalse(ParadoxScopeMatchService.matchesScopeGroup(ParadoxScopeContext.resolve("country"), "galactic", configGroup))
    }

    // endregion
}
