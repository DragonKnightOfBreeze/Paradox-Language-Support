package icu.windea.pls.images.editor

import com.intellij.ide.*
import com.intellij.ide.util.DeleteHandler.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.wm.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.components.panels.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.images.dds.*
import icu.windea.pls.images.tga.*
import org.intellij.images.*
import org.intellij.images.editor.*
import org.intellij.images.editor.ImageDocument.*
import org.intellij.images.options.*
import org.intellij.images.thumbnail.actionSystem.*
import org.intellij.images.thumbnail.actions.*
import org.intellij.images.ui.*
import org.jetbrains.annotations.*
import java.awt.*
import java.awt.datatransfer.*
import java.awt.event.*
import java.awt.image.*
import java.beans.*
import javax.swing.*
import javax.swing.event.*
import kotlin.math.*

//org.intellij.images.editor.impl.ImageEditorUI

class ImageEditorUI(
    private val editor: ImageEditor?,
    private val isEmbedded: Boolean = false,
    isOpaque: Boolean = false
) : JPanel(), UiDataProvider, CopyProvider, ImageComponentDecorator, Disposable {
    companion object {
        @NonNls
        private const val IMAGE_PANEL = "image"
        @NonNls
        private const val ERROR_PANEL = "error"
        @NonNls
        private const val ZOOM_FACTOR_PROP = "ImageEditor.zoomFactor"
        @NonNls
        private const val IMAGE_MAX_ZOOM_FACTOR = Double.MAX_VALUE
    }

    private val deleteProvider: DeleteProvider
    private val copyPasteSupport: CopyPasteSupport?

    private val zoomModel: ImageZoomModel = ImageZoomModelImpl()
    private val wheelAdapter: ImageWheelAdapter = ImageWheelAdapter()
    private val changeListener: ChangeListener = DocumentChangeListener()
    val imageComponent: ImageComponent = ImageComponent()
    val contentComponent: JComponent get() = contentPanel
    private val contentPanel: JPanel
    private var infoLabel: JLabel? = null

    private val myScrollPane: JScrollPane

    init {
        imageComponent.addPropertyChangeListener(ZOOM_FACTOR_PROP) { imageComponent.zoomFactor = zoomModel.zoomFactor }
        val options = OptionsManager.getInstance().options
        val editorOptions = options.editorOptions
        options.addPropertyChangeListener(OptionsChangeListener(), this)

        copyPasteSupport = if (editor != null) CopyPasteDelegator(editor.project, this) else null
        deleteProvider = DefaultDeleteProvider()

        val document = imageComponent.document
        document.addChangeListener(changeListener)

        // Set options
        val chessboardOptions = editorOptions.transparencyChessboardOptions
        val gridOptions = editorOptions.gridOptions
        imageComponent.transparencyChessboardCellSize = chessboardOptions.cellSize
        imageComponent.transparencyChessboardWhiteColor = chessboardOptions.whiteColor
        imageComponent.setTransparencyChessboardBlankColor(chessboardOptions.blackColor)
        imageComponent.gridLineZoomFactor = gridOptions.lineZoomFactor
        imageComponent.gridLineSpan = gridOptions.lineSpan
        imageComponent.gridLineColor = gridOptions.lineColor
        imageComponent.isBorderVisible = ShowBorderAction.isBorderVisible()

        // Create layout
        val view = ImageContainerPane(imageComponent)
        PopupHandler.installPopupMenu(view, ImageEditorActions.GROUP_POPUP, ImageEditorActions.ACTION_PLACE)
        view.addMouseListener(FocusRequester())

        myScrollPane = ScrollPaneFactory.createScrollPane(view, true)
        myScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        myScrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED

        // Zoom by wheel listener
        myScrollPane.addMouseWheelListener(wheelAdapter)

        // Construct UI
        layout = BorderLayout()


        // toolbar is disabled in embedded mode
        var toolbarPanel: JComponent? = null
        if (!isEmbedded) {
            val actionManager = ActionManager.getInstance()
            val actionGroup = actionManager.getAction(ImageEditorActions.GROUP_TOOLBAR) as ActionGroup
            val actionToolbar = actionManager.createActionToolbar(ImageEditorActions.ACTION_PLACE, actionGroup, true)
            actionToolbar.targetComponent = this

            toolbarPanel = actionToolbar.component
            toolbarPanel.setBackground(JBColor.lazy { getBackground() ?: UIUtil.getPanelBackground() })
            toolbarPanel.addMouseListener(FocusRequester())
        }

        val errorLabel = JLabel(
            ImagesBundle.message("error.broken.image.file.format"),
            Messages.getErrorIcon(), SwingConstants.CENTER
        )
        val errorPanel = JPanel(BorderLayout())
        errorPanel.add(errorLabel, BorderLayout.CENTER)
        contentPanel = JPanel(CardLayout())
        contentPanel.add(myScrollPane, IMAGE_PANEL)
        contentPanel.add(errorPanel, ERROR_PANEL)

        val topPanel: JPanel = NonOpaquePanel(BorderLayout())
        if (!isEmbedded) {
            topPanel.add(toolbarPanel!!, BorderLayout.WEST)
            infoLabel = JLabel(null as String?, SwingConstants.RIGHT)
            infoLabel!!.setBorder(JBUI.Borders.emptyRight(2))
            topPanel.add(infoLabel!!, BorderLayout.EAST)
        }
        add(topPanel, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
        myScrollPane.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                updateZoomFactor()
            }
        })

        if (!isOpaque) {
            //setOpaque(false)
            contentPanel.isOpaque = false
            myScrollPane.isOpaque = false
            myScrollPane.viewport.isOpaque = false
        }

        setBackground(JBColor.lazy {
            EditorColorsManager.getInstance().globalScheme.getColor(EditorColors.PREVIEW_BACKGROUND)
                ?: EditorColorsManager.getInstance().globalScheme.defaultBackground
        })
        updateInfo()
    }

    private fun updateInfo() {
        if (isEmbedded) return
        val infoLabel = infoLabel
        if (infoLabel == null) return
        val document = imageComponent.document
        val file = editor?.file
        val project = editor?.project
        val fileType by lazy { file?.fileType }
        val format = document.format?.orNull().let { if (it != null) StringUtil.toUpperCase(it) else ImagesBundle.message("unknown.format") }
        val fileSize = if (file != null) StringUtil.formatFileSize(file.length) else null
        run {
            if (file == null || project == null) return@run
            if (fileType != DdsFileType) return@run
            val metadata = runReadAction { service<DdsMetadataIndex>().getMetadata(file, project) } ?: return@run
            infoLabel.text = buildString {
                append(metadata.width).append("x").append(metadata.height)
                append(" ").append(format)
                append(" (")
                append(metadata.d3dFormat?.orNull() ?: "UNKNOWN")
                append(", ")
                append(metadata.dxgiFormat?.orNull() ?: "UNKNOWN")
                append(")")
                append(" ").append(fileSize)
            }
            return
        }
        run {
            if (file == null || project == null) return@run
            if (fileType != TgaFileType) return@run
            val metadata = runReadAction { service<TgaMetadataIndex>().getMetadata(file, project) } ?: return@run
            infoLabel.text = buildString {
                append(metadata.width).append("x").append(metadata.height)
                append(" ").append(format)
                append(" ").append(fileSize)
            }
            return
        }
        run {
            val image = document.value ?: return@run
            infoLabel.text = buildString {
                append(image.width).append("x").append(image.height)
                append(" ").append(format)
                fileSize?.orNull()?.let { append(" ").append(it) }
            }
            return
        }
        infoLabel.text = null
    }

    override fun dispose() {
        imageComponent.removeMouseWheelListener(wheelAdapter)
        imageComponent.document.removeChangeListener(changeListener)
        removeAll()
    }

    override fun setTransparencyChessboardVisible(visible: Boolean) {
        imageComponent.isTransparencyChessboardVisible = visible
        repaint()
    }

    override fun isTransparencyChessboardVisible(): Boolean {
        return imageComponent.isTransparencyChessboardVisible
    }

    override fun isEnabledForActionPlace(place: String): Boolean {
        // Disable for thumbnails action
        return ThumbnailViewActions.ACTION_PLACE != place
    }

    override fun setGridVisible(visible: Boolean) {
        imageComponent.isGridVisible = visible
        repaint()
    }

    override fun isGridVisible(): Boolean {
        return imageComponent.isGridVisible
    }

    override fun getZoomModel(): ImageZoomModel {
        return zoomModel
    }

    fun setImageProvider(imageProvider: ScaledImageProvider?, format: String?) {
        val document = imageComponent.document
        val previousImage = document.value
        document.setValue(imageProvider)
        if (imageProvider == null) return
        document.format = format
        if (previousImage == null || !zoomModel.isZoomLevelChanged) {
            val zoomOptions = zoomOptions
            if (!(zoomOptions.isSmartZooming && updateZoomFactor())) {
                zoomModel.zoomFactor = 1.0
            }
        }
    }

    private fun updateZoomFactor(): Boolean {
        val zoomOptions = zoomOptions
        if (zoomOptions.isSmartZooming && !zoomModel.isZoomLevelChanged) {
            val smartZoomFactor = getSmartZoomFactor(zoomOptions)
            if (smartZoomFactor != null) {
                zoomModel.zoomFactor = smartZoomFactor
                return true
            }
        }
        return false
    }

    private val zoomOptions: ZoomOptions
        get() {
            val editor = editor
            if (editor != null) {
                val options = editor.zoomModel.customZoomOptions
                if (options != null) {
                    return options
                }
            }
            val options = OptionsManager.getInstance().options
            return options.editorOptions.zoomOptions
        }

    private inner class ImageContainerPane(private val imageComponent: ImageComponent) : JBLayeredPane() {
        init {
            setLayout(Layout())
            add(imageComponent)
            putClientProperty(Magnificator.CLIENT_PROPERTY_KEY, Magnificator { scale, at ->
                val locationBefore = imageComponent.location
                val model = if (editor != null) editor.zoomModel else zoomModel
                val factor = model.zoomFactor
                model.zoomFactor = scale * factor
                Point(
                    ((at.x - max(if (scale > 1.0) locationBefore.x else 0, 0)) * scale).toInt(),
                    ((at.y - max(if (scale > 1.0) locationBefore.y else 0, 0)) * scale).toInt()
                )
            })
        }

        private fun centerComponents() {
            val bounds = bounds
            val point = imageComponent.location
            // in embedded mode images should be left-side aligned
            point.x = if (isEmbedded) 0 else (bounds.width - imageComponent.width) / 2
            point.y = (bounds.height - imageComponent.height) / 2
            imageComponent.location = point
        }

        override fun getPreferredSize(): Dimension? {
            return imageComponent.size
        }

        private inner class Layout : LayoutManager {
            override fun addLayoutComponent(name: String?, comp: Component?) {}

            override fun removeLayoutComponent(comp: Component?) {}

            override fun preferredLayoutSize(parent: Container?): Dimension? {
                return imageComponent.getPreferredSize()
            }

            override fun minimumLayoutSize(parent: Container?): Dimension? {
                return imageComponent.getMinimumSize()
            }

            override fun layoutContainer(parent: Container?) {
                centerComponents()
            }
        }
    }

    private inner class ImageWheelAdapter : MouseWheelListener {
        override fun mouseWheelMoved(e: MouseWheelEvent) {
            val options = OptionsManager.getInstance().options
            val editorOptions = options.editorOptions
            val zoomOptions = editorOptions.zoomOptions
            if (zoomOptions.isWheelZooming && e.isControlDown) {
                val rotation = e.wheelRotation
                val oldZoomFactor = zoomModel.zoomFactor
                val oldPosition = myScrollPane.viewport.viewPosition
                if (rotation > 0) {
                    zoomModel.zoomOut()
                } else if (rotation < 0) {
                    zoomModel.zoomIn()
                }

                // reset view, otherwise view size is not obtained correctly sometimes
                val view = myScrollPane.viewport.view
                myScrollPane.viewport = null
                myScrollPane.setViewportView(view)
                if (oldZoomFactor > 0 && rotation != 0) {
                    val mousePoint = e.point
                    val zoomChange = zoomModel.zoomFactor / oldZoomFactor
                    val newPosition = Point(max(0.0, (oldPosition.getX() + mousePoint.getX()) * zoomChange - mousePoint.getX()).toInt(), max(0.0, (oldPosition.getY() + mousePoint.getY()) * zoomChange - mousePoint.getY()).toInt())
                    myScrollPane.viewport.viewPosition = newPosition
                }
                e.consume()
            }
        }
    }

    private inner class ImageZoomModelImpl : ImageZoomModel {
        private var _customZoomOptions: ZoomOptions? = null
        private var _zoomLevelChanged = false
        private var _zoomFactor = 0.0

        override fun getZoomFactor(): Double {
            return _zoomFactor
        }

        override fun setZoomFactor(zoomFactor: Double) {
            val oldZoomFactor = getZoomFactor()
            if (oldZoomFactor.compareTo(zoomFactor) == 0) return
            this._zoomFactor = zoomFactor

            // Change current size
            updateImageComponentSize()
            revalidate()
            repaint()
            _zoomLevelChanged = false
            imageComponent.firePropertyChange(ZOOM_FACTOR_PROP, oldZoomFactor, zoomFactor)
        }

        private val maximumZoomFactor: Double
            get() {
                val factor = IMAGE_MAX_ZOOM_FACTOR
                return min(factor, ImageZoomModel.MACRO_ZOOM_LIMIT)
            }
        private val minimumZoomFactor: Double
            get() {
                val bounds = imageComponent.document.bounds
                val factor = if (bounds != null) 1.0 / bounds.getWidth() else 0.0
                return max(factor, ImageZoomModel.MICRO_ZOOM_LIMIT)
            }

        override fun fitZoomToWindow() {
            val zoomOptions = zoomOptions
            val smartZoomFactor = getSmartZoomFactor(zoomOptions)
            if (smartZoomFactor != null) {
                zoomModel.zoomFactor = smartZoomFactor
            } else {
                zoomModel.zoomFactor = 1.0
            }
            _zoomLevelChanged = false
        }

        override fun zoomOut() {
            zoomFactor = nextZoomOut
            _zoomLevelChanged = true
        }

        override fun zoomIn() {
            zoomFactor = nextZoomIn
            _zoomLevelChanged = true
        }

        // Macro
        private val nextZoomOut: Double
            get() {
                var factor = zoomFactor
                if (factor > 1.0) {
                    // Macro
                    factor /= ImageZoomModel.MACRO_ZOOM_RATIO
                    factor = max(factor, 1.0)
                } else {
                    // Micro
                    factor /= ImageZoomModel.MICRO_ZOOM_RATIO
                }
                return max(factor, minimumZoomFactor)
            }// Micro

        // Macro
        private val nextZoomIn: Double
            get() {
                var factor = zoomFactor
                if (factor >= 1.0) {
                    // Macro
                    factor *= ImageZoomModel.MACRO_ZOOM_RATIO
                } else {
                    // Micro
                    factor *= ImageZoomModel.MICRO_ZOOM_RATIO
                    factor = min(factor, 1.0)
                }
                return min(factor, maximumZoomFactor)
            }

        override fun canZoomOut(): Boolean {
            // Ignore small differences caused by floating-point arithmetic.
            return zoomFactor - 1.0e-14 > minimumZoomFactor
        }

        override fun canZoomIn(): Boolean {
            return zoomFactor < maximumZoomFactor
        }

        override fun setZoomLevelChanged(value: Boolean) {
            _zoomLevelChanged = value
        }

        override fun isZoomLevelChanged(): Boolean {
            return _zoomLevelChanged
        }

        override fun getCustomZoomOptions(): ZoomOptions? {
            return _customZoomOptions
        }

        override fun setCustomZoomOptions(zoomOptions: ZoomOptions?) {
            _customZoomOptions = zoomOptions
        }
    }

    private fun getSmartZoomFactor(zoomOptions: ZoomOptions): Double? = zoomOptions.getSmartZoomFactor(
        imageComponent.document.bounds, myScrollPane.viewport.extentSize,
        ImageComponent.IMAGE_INSETS
    )

    private fun updateImageComponentSize() {
        val bounds = imageComponent.document.bounds
        if (bounds != null) {
            val zoom = zoomModel.zoomFactor
            imageComponent.setCanvasSize(ceil(bounds.width * zoom).toInt(), ceil(bounds.height * zoom).toInt())
        }
    }

    private inner class DocumentChangeListener : ChangeListener {
        override fun stateChanged(e: ChangeEvent) {
            updateImageComponentSize()
            val document = imageComponent.document
            val value = document.value
            val layout = contentPanel.layout as CardLayout
            layout.show(contentPanel, if (value != null) IMAGE_PANEL else ERROR_PANEL)
            updateInfo()
            revalidate()
            repaint()
        }
    }

    @Suppress("DEPRECATION")
    private inner class FocusRequester : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown { IdeFocusManager.getGlobalInstance().requestFocus(this@ImageEditorUI, true) }
        }
    }

    override fun uiDataSnapshot(sink: DataSink) {
        sink[PlatformDataKeys.COPY_PROVIDER] = this
        sink[ImageComponentDecorator.DATA_KEY] = editor ?: this
        if (editor == null) return
        sink[IMAGE_DOCUMENT_DATA_KEY] = editor.document
        sink[CommonDataKeys.PROJECT] = editor.project
        sink[CommonDataKeys.VIRTUAL_FILE] = editor.file
        sink[CommonDataKeys.VIRTUAL_FILE_ARRAY] = arrayOf(editor.file)
        sink[PlatformDataKeys.CUT_PROVIDER] = copyPasteSupport?.cutProvider
        sink[PlatformDataKeys.DELETE_ELEMENT_PROVIDER] = deleteProvider
        sink.lazy(CommonDataKeys.PSI_FILE) { findPsiFile() }
        sink.lazy(CommonDataKeys.PSI_ELEMENT) { findPsiFile() }
        sink.lazy(PlatformCoreDataKeys.PSI_ELEMENT_ARRAY) { findPsiFile()?.toSingletonArray() ?: PsiElement.EMPTY_ARRAY }
    }

    private fun findPsiFile(): PsiFile? {
        if (editor == null) return null
        val file = editor.file
        return if (file.isValid) PsiManager.getInstance(editor.project).findFile(file) else null
    }

    override fun performCopy(dataContext: DataContext) {
        val document = imageComponent.document
        val image = document.value
        CopyPasteManager.getInstance().setContents(ImageTransferable(image))
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun isCopyEnabled(dataContext: DataContext): Boolean {
        return true
    }

    override fun isCopyVisible(dataContext: DataContext): Boolean {
        return true
    }

    private class ImageTransferable(private val myImage: BufferedImage) : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> {
            return arrayOf(DataFlavor.imageFlavor)
        }

        override fun isDataFlavorSupported(dataFlavor: DataFlavor): Boolean {
            return DataFlavor.imageFlavor.equals(dataFlavor)
        }

        @Throws(UnsupportedFlavorException::class)
        override fun getTransferData(dataFlavor: DataFlavor): Any {
            if (!DataFlavor.imageFlavor.equals(dataFlavor)) {
                throw UnsupportedFlavorException(dataFlavor)
            }
            return myImage
        }
    }

    private inner class OptionsChangeListener : PropertyChangeListener {
        override fun propertyChange(evt: PropertyChangeEvent) {
            val options = evt.source as Options
            val editorOptions = options.editorOptions
            val chessboardOptions = editorOptions.transparencyChessboardOptions
            val gridOptions = editorOptions.gridOptions

            imageComponent.transparencyChessboardCellSize = chessboardOptions.cellSize
            imageComponent.transparencyChessboardWhiteColor = chessboardOptions.whiteColor
            imageComponent.setTransparencyChessboardBlankColor(chessboardOptions.blackColor)
            imageComponent.gridLineZoomFactor = gridOptions.lineZoomFactor
            imageComponent.gridLineSpan = gridOptions.lineSpan
            imageComponent.gridLineColor = gridOptions.lineColor
        }
    }
}
