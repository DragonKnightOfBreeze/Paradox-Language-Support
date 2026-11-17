package icu.windea.pls.extensions.diagram

import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import icu.windea.pls.extensions.diagram.provider.StellarisTechTreeDiagramProvider
import icu.windea.pls.lang.presentation.StellarisTechnologyCardAssets
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

/**
 * 轻量节点模型与科技树边框颜色的单元测试。
 *
 * @see ParadoxDefinitionDiagramNodeModel
 * @see StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor
 */
@RunWith(JUnit4::class)
class ParadoxDiagramModelTest {
    @Test
    fun nodeModel_storesPreloadedData_andCachesCardAssets() {
        @Suppress("DialogTitleCapitalization")
        val propertyText = SimpleColoredText("cost = 100", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val model = ParadoxDefinitionDiagramNodeModel(
            title = "tech_diagram_test",
            typeText = "technology",
            presentableName = "Diagram Test Tech",
            subtypes = setOf("rare"),
            properties = listOf(ParadoxDiagramPropertyItem(propertyText)),
        )

        assertEquals("tech_diagram_test", model.title)
        assertEquals("technology", model.typeText)
        assertEquals("Diagram Test Tech", model.presentableName)
        assertEquals(setOf("rare"), model.subtypes)
        assertEquals(1, model.properties.size)
        assertEquals(propertyText, model.properties.first().text)
        assertNull(model.cardAssets)

        val image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        model.cardAssets = StellarisTechnologyCardAssets(ImageIcon(image), null, image, image)
        assertNotNull(model.cardAssets)
    }

    @Test
    fun nodeBorderColor_bySubtypes() {
        val dangerousAndRare = StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor(setOf("dangerous", "rare"), null)
        assertEquals(java.awt.Color.decode("#e8514f"), dangerousAndRare)

        val dangerous = StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor(setOf("dangerous"), null)
        assertEquals(java.awt.Color.decode("#e8514f"), dangerous)

        val rare = StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor(setOf("rare"), null)
        assertEquals(java.awt.Color.decode("#9743c4"), rare)
    }

    @Test
    fun nodeBorderColor_byArea() {
        // 未命中的子类型由研究领域决定颜色
        assertEquals(java.awt.Color.decode("#2370af"), StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor(emptySet(), "physics"))
        assertEquals(java.awt.Color.decode("#47a05f"), StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor(emptySet(), "society"))
        assertEquals(java.awt.Color.decode("#fbaa29"), StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor(emptySet(), "engineering"))
        assertNull(StellarisTechTreeDiagramProvider.Factory.resolveNodeBorderColor(emptySet(), null))
    }
}
