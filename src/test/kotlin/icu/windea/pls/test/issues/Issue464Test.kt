package icu.windea.pls.test.issues

import com.intellij.psi.PsiFile
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.codeInsight.documentation.buildDocumentation
import icu.windea.pls.core.select.one
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.util.io.deleteQuietly
import icu.windea.pls.images.ImageContext
import icu.windea.pls.images.ImageDescriptor
import icu.windea.pls.images.ImageRenderService
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.inspections.script.common.MissingLocalisationInspection
import icu.windea.pls.lang.resolve.ParadoxLocationResolveResult
import icu.windea.pls.lang.resolve.ParadoxLocationService
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * See: [#464](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/464)
 *
 * Quick documentation icons were broken in several places:
 * - a civic without an explicit `icon` property did not show its icon, even though the subtype-inferred
 *   path (`gfx/interface/icons/governments/civics/$.dds`) existed;
 * - an icon rendered into quick documentation was scaled down to the documentation text height, so a
 *   normal definition image (e.g., a civic icon) became tiny;
 * - a `scripted_modifier` with the `hidden` subtype still reported a missing localisation, although its
 *   localisation is declared only under `subtype[!hidden]`.
 *
 * Cause:
 * - `ParadoxDefinitionInfo` eagerly initialized subtype-dependent derived properties (`images`,
 *   `localisations`, ...) in the constructor, while its `element` is assigned only after construction.
 *   Subtypes were therefore resolved from an empty element, which dropped subtype-scoped rules. This was
 *   fixed by `f0243c563f`, which restored these properties as computed getters.
 * - `ImageRenderService.applyImageScaling` scaled every image whose height did not exceed
 *   `quickDocTextIconHeightLimit`, without distinguishing inline localisation `textIcon`s from other
 *   documentation images. Small definition icons were therefore misclassified as text icons.
 *
 * Solution:
 * - Mark images resolved from localisation text icons as text icons in the render session, and let
 *   `applyImageScaling` scale only those marked requests.
 *
 * @see icu.windea.pls.model.ParadoxDefinitionInfo
 * @see icu.windea.pls.lang.resolve.ParadoxDefinitionService
 * @see icu.windea.pls.images.ImageRenderService
 * @see icu.windea.pls.images.ImageRenderSession
 * @see icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextQuickDocRenderer
 * @see icu.windea.pls.lang.inspections.script.common.MissingLocalisationInspection
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue464Test : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris
    private val imageRoot = Path.of("build/tmp/issue-464")

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/464")
        markConfigDirectory("issues/464/.config")
        initInjectedConfigGroups(project, gameType)
        // Reproduce DDS metadata reading (used by `applyImageScaling`) without relying on quick doc rendering
        ImageContext.init()
        ImageRenderService.getInstance().invalidateMemoryCache()
        imageRoot.deleteQuietly(recursive = true)
        imageRoot.createDirectories()
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region quick doc image scaling

    @Test
    fun testApplyImageScaling_ScalesOnlyTextIcons() = runBlocking {
        val service = ImageRenderService.getInstance()
        val session = service.newRenderSession(this)
        val relatedSource = copyDdsFixture("scaling-related.dds")
        val textIconSource = copyDdsFixture("scaling-text-icon.dds")

        // A normal definition image (e.g., a civic icon) is resolved without the text-icon marker
        val relatedUrl = service.withRenderSession(session) {
            ImageDescriptor(relatedSource).resolveUrl(project)
        }!!
        // An inline localisation text icon is resolved with the text-icon marker
        val textIconUrl = service.withRenderSession(session) {
            ImageDescriptor(textIconSource).resolveUrl(project, textIcon = true)
        }!!
        val builder = buildDocumentation {
            image(relatedUrl)
            image(textIconUrl)
        }

        val textHeight = 18
        service.applyImageScaling(builder, session, textHeight = textHeight, textIconSizeLimit = 36)

        // The 25x25 DDS is no taller than the limit, but it is not a text icon and must keep its original size
        Assert.assertTrue(
            "Non-text-icon images should keep their original size: ${builder.content}",
            builder.content.contains("<img src=\"${relatedUrl.toFileUrl()}\"/>"),
        )
        // The marked text icon is scaled to the documentation text height (25 -> 18)
        Assert.assertTrue(
            "Text icons should be scaled to the text height: ${builder.content}",
            builder.content.contains("width=\"18\" height=\"$textHeight\""),
        )
    }

    // endregion

    // region civic icon

    @Test
    fun testCivicIcon_ResolvedFromSubtypeInferredPath() {
        val civic = setUpCivics()

        expectScope {
            val info = civic.definitionInfo.expectNotNull()
            info.type.expectEquals("civic_or_origin")
            "civic".expectIn(info.subtypes)
            // The direct `icon = icon` rule and the subtype-scoped inferred rule are both retained
            info.images.map { it.key }.expectEquals(listOf("icon", "icon"))
        }

        expectScope {
            val info = civic.definitionInfo.expectNotNull()
            val inferred = info.images.first { it.locationExpression.toString().contains("civics/$.dds") }
            val resolved = ParadoxLocationService.resolve(inferred.locationExpression, civic, info, toFile = true)
                .expectIs<ParadoxLocationResolveResult.Image.Static>()
            resolved.name.expectEquals("gfx/interface/icons/governments/civics/test_civic.dds")
            resolved.element.expectIs<PsiFile>().name.expectEquals("test_civic.dds")
        }

        expectScope {
            val relatedImage = ParadoxDefinitionManager.getRelatedImages(civic, onlyOne = true).firstOrNull().expectNotNull()
            relatedImage.name.expectEquals("test_civic.dds")
        }
    }

    // endregion

    // region hidden scripted modifier localisation

    @Test
    fun testHiddenScriptedModifier_DoesNotRequireLocalisation() {
        setUpScriptedModifiers()
        myFixture.enableInspections(MissingLocalisationInspection::class.java)

        expectScope {
            val info = findDefinition("test_hidden").definitionInfo.expectNotNull()
            info.type.expectEquals("scripted_modifier")
            "hidden".expectIn(info.subtypes)
            // `localisation = { subtype[!hidden] = { name = "mod_$" } }` must not match a hidden definition
            info.localisations.isEmpty().expectTrue()
        }
        Assert.assertFalse(
            "Hidden scripted modifiers should not report missing localisations",
            hasMissingLocalisationHighlight("mod_test_hidden"),
        )
    }

    @Test
    fun testVisibleScriptedModifier_RequiresLocalisation() {
        setUpScriptedModifiers()
        myFixture.enableInspections(MissingLocalisationInspection::class.java)

        expectScope {
            val info = findDefinition("test_visible").definitionInfo.expectNotNull()
            info.type.expectEquals("scripted_modifier")
            info.subtypes.contains("hidden").expectFalse()
            "name".expectIn(info.localisations.map { it.key })
        }
        Assert.assertTrue(
            "Visible scripted modifiers should still report missing localisations",
            hasMissingLocalisationHighlight("mod_test_visible"),
        )
    }

    // endregion

    // region helpers

    private fun copyDdsFixture(name: String): Path {
        val source = Path.of("src/test/testData/issues/464/gfx/interface/icons/governments/civics/test_civic.dds")
        val target = imageRoot.resolve(name)
        Files.copy(source, target)
        return target
    }

    private fun setUpCivics(): ParadoxScriptProperty {
        markFileInfo(gameType, "common/governments/civics/test_civics.txt")
        myFixture.configureByFile("issues/464/common/governments/civics/test_civics.txt")
        // 263: inject file info onto the copied binary fixture directly instead of using a pending `markFileInfo`
        myFixture.copyFileToProject("issues/464/gfx/interface/icons/governments/civics/test_civic.dds")
            .injectFileInfo(gameType, "gfx/interface/icons/governments/civics/test_civic.dds")
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val file = myFixture.file as ParadoxScriptFile
        return selectScope { file.queryBy("test_civic").asProperty().one() }!!
    }

    private fun setUpScriptedModifiers() {
        markFileInfo(gameType, "common/scripted_modifiers/test_scripted_modifiers.txt")
        myFixture.configureByFile("issues/464/common/scripted_modifiers/test_scripted_modifiers.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }

    private fun findDefinition(name: String): ParadoxScriptProperty {
        val file = myFixture.file as ParadoxScriptFile
        return selectScope { file.queryBy(name).asProperty().one() }!!
    }

    private fun hasMissingLocalisationHighlight(name: String): Boolean {
        return myFixture.doHighlighting().any { highlight ->
            highlight.description?.contains("is not defined for locale") == true && highlight.description?.contains(name) == true
        }
    }

    // endregion
}
