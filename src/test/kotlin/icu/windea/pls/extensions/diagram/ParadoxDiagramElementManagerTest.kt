package icu.windea.pls.extensions.diagram

import com.intellij.diagram.DiagramBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import icu.windea.pls.extensions.diagram.provider.ParadoxDefinitionDiagramProvider
import icu.windea.pls.extensions.diagram.provider.ParadoxEventTreeDiagramProvider
import icu.windea.pls.extensions.diagram.provider.ParadoxTechTreeDiagramProvider
import icu.windea.pls.extensions.diagram.provider.StellarisEventTreeDiagramProvider
import icu.windea.pls.extensions.diagram.provider.StellarisTechTreeDiagramProvider
import icu.windea.pls.extensions.diagram.settings.ParadoxDiagramSettings
import icu.windea.pls.images.ImageDescriptor
import icu.windea.pls.lang.presentation.StellarisTechnologyCardAssets
import icu.windea.pls.lang.presentation.StellarisTechnologyCardPresentationModel
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.image.BufferedImage
import java.lang.reflect.Proxy
import java.nio.file.Path
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 验证元素管理器在完全不持有读锁的情况下，直接读取预加载的节点展示模型。
 *
 * 通过轻量的 `DiagramBuilder` 测试替身，仅提供 `dataModel` / `project` / `provider`，
 * 从而聚焦于核心查找路径与零读锁读取。
 *
 * @see ParadoxDefinitionDiagramNodeModel
 * @see ParadoxEventTreeDiagramProvider.ElementManager
 * @see ParadoxTechTreeDiagramProvider.ElementManager
 */
@RunWith(JUnit4::class)
class ParadoxDiagramElementManagerTest : BasePlatformTestCase(), ChronicleTestScope {
    private val gameType = ParadoxGameType.Stellaris

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/diagram")
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun eventElementManager_readsPreloadedModelWithoutReadAccess() {
        val provider = StellarisEventTreeDiagramProvider()
        val dataModel = TestEventDataModel(project, provider)
        val property = configureProperty()
        val propertyText = SimpleColoredText("picture = none", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        val model = ParadoxDefinitionDiagramNodeModel(
            title = "preloaded_title",
            typeText = "event",
            presentableName = "Preloaded Name",
            subtypes = setOf("country_event"),
            properties = listOf(ParadoxDiagramPropertyItem(propertyText)),
        )
        dataModel.register(property, model)
        val builder = newBuilder(provider, dataModel)
        val elementManager = provider.elementManager

        val result = ApplicationManager.getApplication().executeOnPooledThread<ElementManagerResult> {
            check(!ApplicationManager.getApplication().isReadAccessAllowed)
            val title = elementManager.getElementTitle(property, builder)
            val items = elementManager.getNodeItems(property, builder)
            val propertyItem = items.filterIsInstance<ParadoxEventTreeDiagramProvider.Items.Property>().single()
            val propertyName = elementManager.getItemName(property, propertyItem, builder)
            val presentableItem = items.filterIsInstance<ParadoxEventTreeDiagramProvider.Items.PresentableName>().single()
            val component = elementManager.getItemComponent(property, presentableItem, builder)
            ElementManagerResult(title, items, propertyName, component)
        }.get()

        assertEquals("preloaded_title", result.title)
        assertTrue(result.items.any { it is ParadoxEventTreeDiagramProvider.Items.Type })
        assertTrue(result.items.any { it is ParadoxEventTreeDiagramProvider.Items.PresentableName })
        assertEquals(propertyText, result.propertyName)
        assertNotNull(result.component)
    }

    @Test
    fun techElementManager_buildsCardFromPreloadedAssetsWithoutReadAccess() {
        val provider = StellarisTechTreeDiagramProvider()
        val dataModel = TestTechDataModel(project, provider)
        val property = configureProperty()
        val cardModel = StellarisTechnologyCardPresentationModel(
            nameText = "Card",
            costNumber = 42,
            costColor = null,
            iconDescriptor = ImageDescriptor(Path.of("dummy_icon.png")),
            categoryIconDescriptor = null,
            backgroundIconDescriptor = ImageDescriptor(Path.of("dummy_bg.png")),
            bottomLineIconDescriptor = ImageDescriptor(Path.of("dummy_line.png")),
        )
        val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val model = ParadoxDefinitionDiagramNodeModel(
            title = "preloaded_tech",
            typeText = "technology",
            presentableName = "Card",
            subtypes = setOf("rare"),
            properties = emptyList(),
            cardModel = cardModel,
        )
        // 资产已就绪：组件组装应当是纯内存、零锁、零 IO 的
        model.cardAssets = StellarisTechnologyCardAssets(ImageIcon(image), null, image, image)
        dataModel.register(property, model)
        val builder = newBuilder(provider, dataModel)
        val elementManager = provider.elementManager

        val component = ApplicationManager.getApplication().executeOnPooledThread<JComponent?> {
            check(!ApplicationManager.getApplication().isReadAccessAllowed)
            val items = elementManager.getNodeItems(property, builder)
            val presentationItem = items.filterIsInstance<ParadoxTechTreeDiagramProvider.Items.Presentation>().single()
            elementManager.getItemComponent(property, presentationItem, builder)
        }.get()

        assertNotNull(component)
        assertTrue(component is JPanel)
        assertEquals(452, (component as JPanel).preferredSize.width)
        assertEquals(97, component.preferredSize.height)
    }

    // region Helpers

    private data class ElementManagerResult(
        val title: String?,
        val items: Array<Any>,
        val propertyName: SimpleColoredText?,
        val component: javax.swing.JComponent?,
    )

    private fun configureProperty(): ParadoxScriptProperty {
        markFileInfo(gameType, "common/diagram/test_events.txt")
        myFixture.configureByText("test_events.txt", "test_event = { }\n")
        return ReadAction.compute<ParadoxScriptProperty, RuntimeException> {
            PsiTreeUtil.findChildOfType(myFixture.file as ParadoxScriptFile, ParadoxScriptProperty::class.java)!!
        }
    }

    private fun newBuilder(provider: ParadoxDefinitionDiagramProvider, dataModel: ParadoxDiagramDataModel): DiagramBuilder {
        return Proxy.newProxyInstance(
            DiagramBuilder::class.java.classLoader,
            arrayOf(DiagramBuilder::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getDataModel" -> dataModel
                "getProject" -> project
                "getProvider" -> provider
                "toString" -> "TestDiagramBuilder"
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                else -> defaultValue(method.returnType)
            }
        } as DiagramBuilder
    }

    private fun defaultValue(type: Class<*>): Any? = when (type) {
        java.lang.Boolean.TYPE -> false
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Double.TYPE -> 0.0
        java.lang.Float.TYPE -> 0f
        java.lang.Short.TYPE -> 0.toShort()
        java.lang.Byte.TYPE -> 0.toByte()
        java.lang.Character.TYPE -> '\u0000'
        else -> null
    }

    private class TestEventDataModel(
        project: Project,
        provider: ParadoxDefinitionDiagramProvider,
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, null, provider) {
        override fun showNode(definition: ParadoxDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean = true

        fun register(element: PsiElement, model: ParadoxDefinitionDiagramNodeModel) = putNodeModel(element, model)
    }

    private class TestTechDataModel(
        project: Project,
        provider: ParadoxDefinitionDiagramProvider,
    ) : ParadoxTechTreeDiagramProvider.DataModel(project, null, provider) {
        override fun showNode(definition: ParadoxDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean = true

        fun register(element: PsiElement, model: ParadoxDefinitionDiagramNodeModel) = putNodeModel(element, model)
    }

    // endregion
}
