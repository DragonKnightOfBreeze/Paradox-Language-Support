package icu.windea.pls.lang.scope

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxScopeMergeService
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxScopeMergeServiceTest : BasePlatformTestCase(), ChronicleTestScope {
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

    // region mergeScopeId

    @Test
    fun mergeScopeId_forStellaris_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // null handling
        assertNull(ParadoxScopeMergeService.mergeScopeId(null, null, configGroup))
        assertEquals("country", ParadoxScopeMergeService.mergeScopeId(null, "country", configGroup))
        assertEquals("country", ParadoxScopeMergeService.mergeScopeId("country", null, configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId(null, "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId(null, "?", configGroup))

        // exact matches return the first scope
        assertEquals("country", ParadoxScopeMergeService.mergeScopeId("country", "country", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("?", "?", configGroup))
        assertEquals("", ParadoxScopeMergeService.mergeScopeId("", "", configGroup))
        assertEquals("__unresolved__", ParadoxScopeMergeService.mergeScopeId("__unresolved__", "__unresolved__", configGroup))

        // wildcard handling: "any" wins over "?"
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "country", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("country", "any", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "?", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("?", "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("?", "country", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("country", "?", configGroup))

        // "all" is not normalized by the merge service itself
        assertEquals("all", ParadoxScopeMergeService.mergeScopeId("all", "all", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "all", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("all", "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("all", "?", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("?", "all", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("all", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "all", configGroup))

        // scope ids are compared case-sensitively
        assertNull(ParadoxScopeMergeService.mergeScopeId("Country", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "Country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("Any", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "Any", configGroup))

        // degenerate and unresolved scope ids merge to null unless equal
        assertNull(ParadoxScopeMergeService.mergeScopeId("", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("__unresolved__", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "__unresolved__", configGroup))

        // unrelated scopes merge to null
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "leader", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "system", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("leader", "fleet", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("fleet", "carrier", configGroup))
    }

    @Test
    fun mergeScopeId_forVic3_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // null handling
        assertNull(ParadoxScopeMergeService.mergeScopeId(null, null, configGroup))
        assertEquals("country", ParadoxScopeMergeService.mergeScopeId(null, "country", configGroup))
        assertEquals("country", ParadoxScopeMergeService.mergeScopeId("country", null, configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId(null, "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId(null, "?", configGroup))

        // exact matches return the first scope
        assertEquals("country", ParadoxScopeMergeService.mergeScopeId("country", "country", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("?", "?", configGroup))
        assertEquals("", ParadoxScopeMergeService.mergeScopeId("", "", configGroup))
        assertEquals("__unresolved__", ParadoxScopeMergeService.mergeScopeId("__unresolved__", "__unresolved__", configGroup))

        // wildcard handling: "any" wins over "?"
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "country", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("country", "any", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "?", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("?", "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("?", "country", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("country", "?", configGroup))

        // "all" is not normalized by the merge service itself
        assertEquals("all", ParadoxScopeMergeService.mergeScopeId("all", "all", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("any", "all", configGroup))
        assertEquals("any", ParadoxScopeMergeService.mergeScopeId("all", "any", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("all", "?", configGroup))
        assertEquals("?", ParadoxScopeMergeService.mergeScopeId("?", "all", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("all", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "all", configGroup))

        // scope ids are compared case-sensitively
        assertNull(ParadoxScopeMergeService.mergeScopeId("Country", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "Country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("Any", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "Any", configGroup))

        // degenerate and unresolved scope ids merge to null unless equal
        assertNull(ParadoxScopeMergeService.mergeScopeId("", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("__unresolved__", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "__unresolved__", configGroup))

        // unrelated scopes merge to null
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "leader", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "system", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("leader", "fleet", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("fleet", "carrier", configGroup))
    }

    @Test
    fun mergeScopeId_forStellaris_byAlias() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // aliases of the same scope merge to the second scope (order-dependent)
        assertEquals("galactic_object", ParadoxScopeMergeService.mergeScopeId("system", "galactic_object", configGroup))
        assertEquals("system", ParadoxScopeMergeService.mergeScopeId("galactic_object", "system", configGroup))
        assertEquals("celestial_body", ParadoxScopeMergeService.mergeScopeId("world", "celestial_body", configGroup))
        assertEquals("world", ParadoxScopeMergeService.mergeScopeId("celestial_body", "world", configGroup))

        // alias of a sub scope merges to its parent
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("world", "carrier", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("carrier", "world", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("celestial_body", "carrier", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("carrier", "celestial_body", configGroup))

        // alias VS unrelated scope -> null
        assertNull(ParadoxScopeMergeService.mergeScopeId("celestial_body", "colony", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("world", "country", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("galactic_object", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("carrier", "galactic_object", configGroup))
    }

    @Test
    fun mergeScopeId_forVic3_byAlias() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> aliases do not merge
        assertNull(ParadoxScopeMergeService.mergeScopeId("system", "galactic_object", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("galactic_object", "system", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("world", "celestial_body", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("celestial_body", "world", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("world", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("carrier", "world", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("celestial_body", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("celestial_body", "colony", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("galactic_object", "carrier", configGroup))
    }

    @Test
    fun mergeScopeId_forStellaris_byParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // child VS parent -> parent (in either order)
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("planet", "carrier", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("carrier", "planet", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("ship", "carrier", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("carrier", "ship", configGroup))
        assertEquals("system", ParadoxScopeMergeService.mergeScopeId("star", "system", configGroup))
        assertEquals("system", ParadoxScopeMergeService.mergeScopeId("system", "star", configGroup))

        // child VS parent's alias -> parent's alias
        assertEquals("galactic_object", ParadoxScopeMergeService.mergeScopeId("star", "galactic_object", configGroup))
        assertEquals("galactic_object", ParadoxScopeMergeService.mergeScopeId("galactic_object", "star", configGroup))
        assertEquals("galactic_object", ParadoxScopeMergeService.mergeScopeId("moon", "galactic_object", configGroup))
        assertEquals("galactic_object", ParadoxScopeMergeService.mergeScopeId("galactic_object", "moon", configGroup))

        // multi-level parents
        assertEquals("ship", ParadoxScopeMergeService.mergeScopeId("army", "ship", configGroup))
        assertEquals("ship", ParadoxScopeMergeService.mergeScopeId("ship", "army", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("army", "carrier", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("carrier", "army", configGroup))

        // sibling scopes promote to the common parent
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("planet", "ship", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("ship", "planet", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("planet", "world", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("world", "ship", configGroup))
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("army", "planet", configGroup)) // multi-level
        assertEquals("carrier", ParadoxScopeMergeService.mergeScopeId("army", "world", configGroup))

        // sub scopes of an alias-declared parent promote to the input scope, or the shared alias (unordered, use `indexSet.firstOrNull()`)
        assertEquals("system", ParadoxScopeMergeService.mergeScopeId("moon", "system", configGroup))
        assertEquals("system", ParadoxScopeMergeService.mergeScopeId("system", "moon", configGroup))
        // assertEquals("galactic_object", ParadoxScopeMergeService.mergeScopeId("moon", "star", configGroup))
        assertEquals("System", ParadoxScopeMergeService.mergeScopeId("moon", "star", configGroup).let { configGroup.scopeAliasMap[it]?.name })
        assertEquals("system", ParadoxScopeMergeService.mergeScopeId("satellite", "system", configGroup))
        // assertEquals("galactic_object", ParadoxScopeMergeService.mergeScopeId("satellite", "star", configGroup))
        assertEquals("System", ParadoxScopeMergeService.mergeScopeId("satellite", "star", configGroup).let { configGroup.scopeAliasMap[it]?.name })

        // cyclic parents: merging promotes to the other scope (order-dependent)
        assertEquals("beta", ParadoxScopeMergeService.mergeScopeId("alpha", "beta", configGroup))
        assertEquals("alpha", ParadoxScopeMergeService.mergeScopeId("beta", "alpha", configGroup))

        // unrelated scopes -> null
        assertNull(ParadoxScopeMergeService.mergeScopeId("colony", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("colony", "planet", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("moon", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("star", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("army", "star", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("country", "fleet", configGroup))
    }

    @Test
    fun mergeScopeId_forVic3_byParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> parents do not merge
        assertNull(ParadoxScopeMergeService.mergeScopeId("planet", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("carrier", "planet", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("star", "system", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("star", "galactic_object", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("moon", "galactic_object", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("army", "ship", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("army", "carrier", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("planet", "ship", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("moon", "system", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("alpha", "beta", configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeId("colony", "carrier", configGroup))
    }

    // endregion

    // region mergeScope

    @Test
    fun mergeScope_forStellaris_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val nullScope: ParadoxScope? = null

        // null handling
        assertNull(ParadoxScopeMergeService.mergeScope(nullScope, nullScope, configGroup))
        assertEquals(ParadoxScope.resolve("country"), ParadoxScopeMergeService.mergeScope(nullScope, ParadoxScope.resolve("country"), configGroup))
        assertEquals(ParadoxScope.resolve("country"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), nullScope, configGroup))

        // exact matches return the first scope
        assertEquals(ParadoxScope.resolve("country"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("country"), configGroup))
        assertEquals(ParadoxScope.resolve(""), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve(""), ParadoxScope.resolve(""), configGroup))
        assertEquals(ParadoxScope.resolve("__unresolved__"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("__unresolved__"), ParadoxScope.resolve("__unresolved__"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("all"), ParadoxScope.resolve("all"), configGroup)) // "all" resolves to the any scope

        // wildcard handling: the any scope wins over the unknown scope
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("any"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("any"), ParadoxScope.resolve("country"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("?"), ParadoxScope.resolve("any"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("any"), ParadoxScope.resolve("?"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("all"), ParadoxScope.resolve("?"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("all"), ParadoxScope.resolve("country"), configGroup)) // "all" resolves to the any scope
        assertSame(ParadoxScope.Unknown, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("?"), ParadoxScope.resolve("country"), configGroup))
        assertSame(ParadoxScope.Unknown, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("?"), configGroup))

        // scope ids are compared case-sensitively
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("Country"), ParadoxScope.resolve("country"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("Country"), configGroup))

        // degenerate and unresolved scopes merge to null unless equal
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve(""), ParadoxScope.resolve("country"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("__unresolved__"), ParadoxScope.resolve("country"), configGroup))

        // unrelated scopes merge to null
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("leader"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("system"), configGroup))
    }

    @Test
    fun mergeScope_forVic3_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val nullScope: ParadoxScope? = null

        // null handling
        assertNull(ParadoxScopeMergeService.mergeScope(nullScope, nullScope, configGroup))
        assertEquals(ParadoxScope.resolve("country"), ParadoxScopeMergeService.mergeScope(nullScope, ParadoxScope.resolve("country"), configGroup))
        assertEquals(ParadoxScope.resolve("country"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), nullScope, configGroup))

        // exact matches return the first scope
        assertEquals(ParadoxScope.resolve("country"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("country"), configGroup))
        assertEquals(ParadoxScope.resolve(""), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve(""), ParadoxScope.resolve(""), configGroup))
        assertEquals(ParadoxScope.resolve("__unresolved__"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("__unresolved__"), ParadoxScope.resolve("__unresolved__"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("all"), ParadoxScope.resolve("all"), configGroup)) // "all" resolves to the any scope

        // wildcard handling: the any scope wins over the unknown scope
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("any"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("any"), ParadoxScope.resolve("country"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("?"), ParadoxScope.resolve("any"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("any"), ParadoxScope.resolve("?"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("all"), ParadoxScope.resolve("?"), configGroup))
        assertSame(ParadoxScope.Any, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("all"), ParadoxScope.resolve("country"), configGroup)) // "all" resolves to the any scope
        assertSame(ParadoxScope.Unknown, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("?"), ParadoxScope.resolve("country"), configGroup))
        assertSame(ParadoxScope.Unknown, ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("?"), configGroup))

        // scope ids are compared case-sensitively
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("Country"), ParadoxScope.resolve("country"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("Country"), configGroup))

        // degenerate and unresolved scopes merge to null unless equal
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve(""), ParadoxScope.resolve("country"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("__unresolved__"), ParadoxScope.resolve("country"), configGroup))

        // unrelated scopes merge to null
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("leader"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("country"), ParadoxScope.resolve("system"), configGroup))
    }

    @Test
    fun mergeScope_forStellaris_byAliasAndParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // aliases of the same scope merge to the second scope (order-dependent)
        assertEquals(ParadoxScope.resolve("galactic_object"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("system"), ParadoxScope.resolve("galactic_object"), configGroup))
        assertEquals(ParadoxScope.resolve("system"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("galactic_object"), ParadoxScope.resolve("system"), configGroup))
        assertEquals(ParadoxScope.resolve("celestial_body"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("world"), ParadoxScope.resolve("celestial_body"), configGroup))
        assertEquals(ParadoxScope.resolve("world"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("celestial_body"), ParadoxScope.resolve("world"), configGroup))

        // alias of a sub scope merges to its parent
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("world"), ParadoxScope.resolve("carrier"), configGroup))
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("carrier"), ParadoxScope.resolve("world"), configGroup))

        // child VS parent -> parent (in either order)
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("planet"), ParadoxScope.resolve("carrier"), configGroup))
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("carrier"), ParadoxScope.resolve("planet"), configGroup))
        assertEquals(ParadoxScope.resolve("system"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("star"), ParadoxScope.resolve("system"), configGroup))
        assertEquals(ParadoxScope.resolve("system"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("system"), ParadoxScope.resolve("star"), configGroup))

        // child VS parent's alias -> parent's alias
        assertEquals(ParadoxScope.resolve("galactic_object"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("star"), ParadoxScope.resolve("galactic_object"), configGroup))
        assertEquals(ParadoxScope.resolve("galactic_object"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("galactic_object"), ParadoxScope.resolve("star"), configGroup))
        assertEquals(ParadoxScope.resolve("galactic_object"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("moon"), ParadoxScope.resolve("galactic_object"), configGroup))

        // multi-level parents
        assertEquals(ParadoxScope.resolve("ship"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("army"), ParadoxScope.resolve("ship"), configGroup))
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("army"), ParadoxScope.resolve("carrier"), configGroup))
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("carrier"), ParadoxScope.resolve("army"), configGroup))

        // sibling scopes promote to the common parent
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("planet"), ParadoxScope.resolve("ship"), configGroup))
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("planet"), ParadoxScope.resolve("world"), configGroup))
        assertEquals(ParadoxScope.resolve("carrier"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("army"), ParadoxScope.resolve("planet"), configGroup))

        // sub scopes of an alias-declared parent promote to the input scope, or the shared alias (unordered, use `indexSet.firstOrNull()`)
        assertEquals(ParadoxScope.resolve("system"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("moon"), ParadoxScope.resolve("system"), configGroup))
        // assertEquals(ParadoxScope.resolve("galactic_object"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("moon"), ParadoxScope.resolve("star"), configGroup))
        assertEquals("System", ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("moon"), ParadoxScope.resolve("star"), configGroup)?.id?.let { configGroup.scopeAliasMap[it]?.name })
        assertEquals(ParadoxScope.resolve("system"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("satellite"), ParadoxScope.resolve("system"), configGroup))

        // cyclic parents: merging promotes to the other scope (order-dependent)
        assertEquals(ParadoxScope.resolve("beta"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("alpha"), ParadoxScope.resolve("beta"), configGroup))
        assertEquals(ParadoxScope.resolve("alpha"), ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("beta"), ParadoxScope.resolve("alpha"), configGroup))

        // unrelated scopes -> null
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("colony"), ParadoxScope.resolve("carrier"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("moon"), ParadoxScope.resolve("carrier"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("star"), ParadoxScope.resolve("carrier"), configGroup))
    }

    @Test
    fun mergeScope_forVic3_byAliasAndParent() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // mismatched game type -> aliases do not merge
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("system"), ParadoxScope.resolve("galactic_object"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("galactic_object"), ParadoxScope.resolve("system"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("world"), ParadoxScope.resolve("celestial_body"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("world"), ParadoxScope.resolve("carrier"), configGroup))

        // mismatched game type -> parents do not merge
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("planet"), ParadoxScope.resolve("carrier"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("star"), ParadoxScope.resolve("system"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("star"), ParadoxScope.resolve("galactic_object"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("moon"), ParadoxScope.resolve("galactic_object"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("army"), ParadoxScope.resolve("ship"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("army"), ParadoxScope.resolve("carrier"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("planet"), ParadoxScope.resolve("ship"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("moon"), ParadoxScope.resolve("system"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("alpha"), ParadoxScope.resolve("beta"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScope(ParadoxScope.resolve("colony"), ParadoxScope.resolve("carrier"), configGroup))
    }

    // endregion

    // region mergeScopeContextMap

    @Test
    fun mergeScopeContextMap_forStellaris_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // both empty -> null
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(emptyMap(), emptyMap(), configGroup))

        // keys from one map are kept (null VS value -> value)
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContextMap(emptyMap(), mapOf("this" to "country"), configGroup))
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), emptyMap(), configGroup))

        // exact matches return the first scope
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), mapOf("this" to "country"), configGroup))

        // unrelated scopes do not merge -> the key is dropped, and an empty result is null
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), mapOf("this" to "leader"), configGroup))

        // wildcards merge
        assertEquals(mapOf("this" to "any"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "any"), mapOf("this" to "country"), configGroup))
        assertEquals(mapOf("this" to "?"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "?"), mapOf("this" to "country"), configGroup))

        // child VS parent -> parent
        assertEquals(mapOf("this" to "carrier"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "planet"), mapOf("this" to "carrier"), configGroup))

        // sibling scopes promote to the common parent
        assertEquals(mapOf("this" to "carrier"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "planet"), mapOf("this" to "ship"), configGroup))

        // multi-level parents and alias-declared parents
        assertEquals(mapOf("this" to "carrier"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "army"), mapOf("this" to "planet"), configGroup))
        assertEquals(mapOf("this" to "system"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "moon"), mapOf("this" to "system"), configGroup))
    }

    @Test
    fun mergeScopeContextMap_forStellaris_byKeys() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val c = ParadoxScopeConstants

        // all keys are merged independently
        val mapA = mapOf(c.thisScope to "planet", c.rootScope to "country", c.fromScope to "system", c.from2Scope to "planet", c.from3Scope to "colony", c.from4Scope to "fleet")
        val mapB = mapOf(c.thisScope to "ship", c.rootScope to "country", c.fromScope to "galactic_object", c.from2Scope to "ship", c.from3Scope to "colony", c.from4Scope to "fleet")
        assertEquals(
            mapOf(c.thisScope to "carrier", c.rootScope to "country", c.fromScope to "galactic_object", c.from2Scope to "carrier", c.from3Scope to "colony", c.from4Scope to "fleet"),
            ParadoxScopeMergeService.mergeScopeContextMap(mapA, mapB, configGroup),
        )

        // keys merging to null are dropped, other keys are kept
        val mapC = mapOf(c.thisScope to "planet", c.rootScope to "country", c.fromScope to "system")
        val mapD = mapOf(c.thisScope to "ship", c.rootScope to "carrier", c.fromScope to "system")
        assertEquals(
            mapOf(c.thisScope to "carrier", c.fromScope to "system"),
            ParadoxScopeMergeService.mergeScopeContextMap(mapC, mapD, configGroup),
        )

        // prev keys are merged as well
        assertEquals(
            mapOf(c.prevScope to "carrier", c.prev2Scope to "system"),
            ParadoxScopeMergeService.mergeScopeContextMap(mapOf(c.prevScope to "planet", c.prev2Scope to "moon"), mapOf(c.prevScope to "ship", c.prev2Scope to "system"), configGroup),
        )
    }

    @Test
    fun mergeScopeContextMap_forStellaris_orUnknown() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)

        // orUnknown fills in missing this/root with "?" only when explicitly requested
        val mapA = mapOf("this" to "planet")
        assertEquals(mapOf("this" to "planet"), ParadoxScopeMergeService.mergeScopeContextMap(mapA, emptyMap(), configGroup))
        assertEquals(mapOf("this" to "planet", "root" to "?"), ParadoxScopeMergeService.mergeScopeContextMap(mapA, emptyMap(), configGroup, orUnknown = true))
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), mapOf("this" to "leader"), configGroup))
        assertEquals(mapOf("this" to "?", "root" to "?"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), mapOf("this" to "leader"), configGroup, orUnknown = true))
    }

    @Test
    fun mergeScopeContextMap_forVic3_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)

        // both empty -> null
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(emptyMap(), emptyMap(), configGroup))

        // keys from one map are kept (null VS value -> value)
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContextMap(emptyMap(), mapOf("this" to "country"), configGroup))
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), emptyMap(), configGroup))

        // exact matches return the first scope
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), mapOf("this" to "country"), configGroup))

        // unrelated scopes do not merge -> the key is dropped, and an empty result is null
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), mapOf("this" to "leader"), configGroup))

        // wildcards merge
        assertEquals(mapOf("this" to "any"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "any"), mapOf("this" to "country"), configGroup))
        assertEquals(mapOf("this" to "?"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "?"), mapOf("this" to "country"), configGroup))

        // mismatched game type -> related scopes do not merge
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "planet"), mapOf("this" to "carrier"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "planet"), mapOf("this" to "ship"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "army"), mapOf("this" to "planet"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "moon"), mapOf("this" to "system"), configGroup))

        // only mergeable keys are kept
        val mapA = mapOf("this" to "planet", "root" to "country", "from" to "system")
        val mapB = mapOf("this" to "ship", "root" to "country", "from" to "galactic_object")
        assertEquals(mapOf("root" to "country"), ParadoxScopeMergeService.mergeScopeContextMap(mapA, mapB, configGroup))

        // orUnknown fills in missing this/root with "?" only when explicitly requested
        assertEquals(mapOf("this" to "planet", "root" to "?"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "planet"), emptyMap(), configGroup, orUnknown = true))
        assertEquals(mapOf("this" to "?", "root" to "?"), ParadoxScopeMergeService.mergeScopeContextMap(mapOf("this" to "country"), mapOf("this" to "leader"), configGroup, orUnknown = true))
    }

    // endregion

    // region mergeScopeContext

    @Test
    fun mergeScopeContext_forStellaris_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val nullContext: ParadoxScopeContext? = null

        // null handling
        assertNull(ParadoxScopeMergeService.mergeScopeContext(nullContext, nullContext, configGroup))
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContext(nullContext, ParadoxScopeContext.resolve("country"), configGroup)?.toScopeIdMap())
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), nullContext, configGroup)?.toScopeIdMap())

        // exact matches
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("country"), configGroup)?.toScopeIdMap())

        // unrelated contexts do not merge
        assertNull(ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("leader"), configGroup))

        // wildcards merge
        assertEquals(mapOf("this" to "any"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("any"), configGroup)?.toScopeIdMap())
        assertEquals(mapOf("this" to "?"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("?"), configGroup)?.toScopeIdMap())

        // child VS parent and sibling scopes promote to the parent
        assertEquals(mapOf("this" to "carrier"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), ParadoxScopeContext.resolve("carrier"), configGroup)?.toScopeIdMap())
        assertEquals(mapOf("this" to "carrier"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), ParadoxScopeContext.resolve("ship"), configGroup)?.toScopeIdMap())
    }

    @Test
    fun mergeScopeContext_forStellaris_byKeys() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val c = ParadoxScopeConstants

        // contexts are merged key by key (this/root/from/fromfrom/fromfromfrom/fromfromfromfrom)
        val contextA = ParadoxScopeContext.resolve(mapOf(c.thisScope to "planet", c.rootScope to "country", c.fromScope to "system", c.from2Scope to "planet", c.from3Scope to "colony", c.from4Scope to "fleet"))!!
        val contextB = ParadoxScopeContext.resolve(mapOf(c.thisScope to "ship", c.rootScope to "country", c.fromScope to "galactic_object", c.from2Scope to "ship", c.from3Scope to "colony", c.from4Scope to "fleet"))!!
        assertEquals(
            mapOf(c.thisScope to "carrier", c.rootScope to "country", c.fromScope to "galactic_object", c.from2Scope to "carrier", c.from3Scope to "colony", c.from4Scope to "fleet"),
            ParadoxScopeMergeService.mergeScopeContext(contextA, contextB, configGroup)?.toScopeIdMap(),
        )

        // prev scopes are not preserved when merging contexts (they are dropped at the toScopeIdMap step)
        val prevContext = ParadoxScopeContext.resolve(mapOf(c.thisScope to "planet", c.prevScope to "carrier"))!!
        val mergedContext = ParadoxScopeMergeService.mergeScopeContext(prevContext, prevContext, configGroup)!!
        assertEquals(mapOf(c.thisScope to "planet"), mergedContext.toScopeIdMap())
        assertTrue(mergedContext.prevStack.isEmpty())
    }

    @Test
    fun mergeScopeContext_forStellaris_orUnknown() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Stellaris)
        val nullContext: ParadoxScopeContext? = null

        // orUnknown fills in missing this/root with "?" only when explicitly requested
        assertEquals(mapOf("this" to "planet"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), nullContext, configGroup)?.toScopeIdMap())
        assertEquals(mapOf("this" to "planet", "root" to "?"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), nullContext, configGroup, orUnknown = true)?.toScopeIdMap())
        assertNull(ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("leader"), configGroup))
        assertEquals(mapOf("this" to "?", "root" to "?"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("leader"), configGroup, orUnknown = true)?.toScopeIdMap())
    }

    @Test
    fun mergeScopeContext_forVic3_basic() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val nullContext: ParadoxScopeContext? = null

        // null handling
        assertNull(ParadoxScopeMergeService.mergeScopeContext(nullContext, nullContext, configGroup))
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContext(nullContext, ParadoxScopeContext.resolve("country"), configGroup)?.toScopeIdMap())
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), nullContext, configGroup)?.toScopeIdMap())

        // exact matches
        assertEquals(mapOf("this" to "country"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("country"), configGroup)?.toScopeIdMap())

        // unrelated contexts do not merge
        assertNull(ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("leader"), configGroup))

        // wildcards merge
        assertEquals(mapOf("this" to "any"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("any"), configGroup)?.toScopeIdMap())
        assertEquals(mapOf("this" to "?"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("?"), configGroup)?.toScopeIdMap())

        // mismatched game type -> related scopes do not merge
        assertNull(ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), ParadoxScopeContext.resolve("carrier"), configGroup))
        assertNull(ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), ParadoxScopeContext.resolve("ship"), configGroup))

        // only mergeable keys are kept; a missing this scope results in null
        val contextA = ParadoxScopeContext.resolve(mapOf("this" to "planet", "root" to "country", "from" to "system"))!!
        val contextB = ParadoxScopeContext.resolve(mapOf("this" to "ship", "root" to "country", "from" to "galactic_object"))!!
        assertNull(ParadoxScopeMergeService.mergeScopeContext(contextA, contextB, configGroup))
        assertEquals(mapOf("this" to "?", "root" to "country"), ParadoxScopeMergeService.mergeScopeContext(contextA, contextB, configGroup, orUnknown = true)?.toScopeIdMap())

        // orUnknown fills in missing this/root with "?" only when explicitly requested
        assertEquals(mapOf("this" to "planet"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), nullContext, configGroup)?.toScopeIdMap())
        assertEquals(mapOf("this" to "planet", "root" to "?"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("planet"), nullContext, configGroup, orUnknown = true)?.toScopeIdMap())
        assertEquals(mapOf("this" to "?", "root" to "?"), ParadoxScopeMergeService.mergeScopeContext(ParadoxScopeContext.resolve("country"), ParadoxScopeContext.resolve("leader"), configGroup, orUnknown = true)?.toScopeIdMap())
    }

    // endregion
}
