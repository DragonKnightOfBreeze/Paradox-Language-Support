package icu.windea.pls.dds.editor

import com.intellij.ide.*
import com.intellij.ide.util.DeleteHandler.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.wm.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import org.intellij.images.editor.*
import org.intellij.images.editor.ImageDocument.*
import org.intellij.images.options.*
import org.intellij.images.thumbnail.actionSystem.*
import org.intellij.images.thumbnail.actions.*
import org.intellij.images.ui.*
import java.awt.*
import java.awt.datatransfer.*
import java.awt.event.*
import java.awt.image.*
import java.beans.*
import javax.swing.*
import javax.swing.event.*
import kotlin.math.*

//org.intellij.images.editor.impl.ImageEditorUI

private const val IMAGE_PANEL = "image"
private const val ERROR_PANEL = "error"
private const val ZOOM_FACTOR_PROP = "DdsEditor.zoomFactor"

class DdsEditorUI(
	private val editor: ImageEditor?,
	private val isEmbedded: Boolean = false,
	isOpaque: Boolean = true
) : JPanel(), DataProvider, CopyProvider, ImageComponentDecorator, Disposable {
	private val deleteProvider: DeleteProvider
	private val copyPasteSupport: CopyPasteSupport?
	private val zoomModel: ImageZoomModel = ImageZoomModelImpl()
	private val wheelAdapter: ImageWheelAdapter = ImageWheelAdapter()
	private val changeListener: ChangeListener = DocumentChangeListener()
	private val imageComponent = ImageComponent()
	private val contentPanel: JPanel
	private var infoLabel: JLabel? = null
	private val myScrollPane: JScrollPane
	
	init {
		imageComponent.addPropertyChangeListener(ZOOM_FACTOR_PROP) { imageComponent.zoomFactor = getZoomModel().zoomFactor }
		val options = OptionsManager.getInstance().options
		val editorOptions = options.editorOptions
		options.addPropertyChangeListener(OptionsChangeListener(), this)
		copyPasteSupport = if(editor != null) CopyPasteDelegator(editor.project, this) else null
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
		PopupHandler.installPopupMenu(view, DdsEditorActions.GROUP_POPUP, DdsEditorActions.ACTION_PLACE)
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
		if(!isEmbedded) {
			val actionManager = ActionManager.getInstance()
			val actionGroup = actionManager.getAction(DdsEditorActions.GROUP_TOOLBAR) as ActionGroup
			val actionToolbar = actionManager.createActionToolbar(DdsEditorActions.ACTION_PLACE, actionGroup, true)
			actionToolbar.targetComponent = this
			toolbarPanel = actionToolbar.component
			toolbarPanel.addMouseListener(FocusRequester())
		}
		val errorLabel = JLabel(
			PlsBundle.message("error.broken.image.file.format"),
			Messages.getErrorIcon(), SwingConstants.CENTER
		)
		val errorPanel = JPanel(BorderLayout())
		errorPanel.add(errorLabel, BorderLayout.CENTER)
		contentPanel = JPanel(CardLayout())
		contentPanel.add(myScrollPane, IMAGE_PANEL)
		contentPanel.add(errorPanel, ERROR_PANEL)
		val topPanel = JPanel(BorderLayout())
		if(!isEmbedded) {
			toolbarPanel?.let {
				topPanel.add(it, BorderLayout.WEST)
			}
			infoLabel = JLabel(null as String?, SwingConstants.RIGHT)
			infoLabel?.let {
				it.border = JBUI.Borders.emptyRight(2)
				topPanel.add(it, BorderLayout.EAST)
			}
		}
		add(topPanel, BorderLayout.NORTH)
		add(contentPanel, BorderLayout.CENTER)
		myScrollPane.addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent) {
				updateZoomFactor()
			}
		})
		if(!isOpaque) {
			setOpaque(false)
			contentPanel.isOpaque = false
			myScrollPane.isOpaque = false
			myScrollPane.viewport.isOpaque = false
		}
		updateInfo()
	}
	
	private fun updateInfo() {
		if(isEmbedded) return
		val document = imageComponent.document
		val image = document.value
		if(image != null) {
			var format = document.format
			format = if(format == null) {
				if(editor != null) PlsBundle.message("unknown.format") else ""
			} else {
				StringUtil.toUpperCase(format)
			}
			val file = editor?.file
			infoLabel?.let { infoLabel ->
				val fileSize = if(file != null) StringUtil.formatFileSize(file.length) else ""
				infoLabel.text = PlsBundle.message("dds.info", image.width, image.height, format, fileSize)
			}
		} else {
			infoLabel?.let { infoLabel ->
				infoLabel.text = null
			}
		}
	}
	
	override fun getActionUpdateThread(): ActionUpdateThread {
		return ActionUpdateThread.BGT
	}
	
	@Suppress("unused")
	fun getContentComponent(): JComponent {
		return contentPanel
	}
	
	fun getImageComponent(): ImageComponent {
		return imageComponent
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
		if(imageProvider == null) return
		document.format = format
		if(previousImage == null || !zoomModel.isZoomLevelChanged) {
			val zoomOptions = zoomOptions
			if(!(zoomOptions.isSmartZooming && updateZoomFactor())) {
				zoomModel.zoomFactor = 1.0
			}
		}
	}
	
	private fun updateZoomFactor(): Boolean {
		val zoomOptions = zoomOptions
		if(zoomOptions.isSmartZooming && !zoomModel.isZoomLevelChanged) {
			val smartZoomFactor = getSmartZoomFactor(zoomOptions)
			if(smartZoomFactor != null) {
				zoomModel.zoomFactor = smartZoomFactor
				return true
			}
		}
		return false
	}
	
	private val zoomOptions: ZoomOptions
		get() {
			val editor = editor
			if(editor != null) {
				val options = editor.zoomModel.customZoomOptions
				if(options != null) {
					return options
				}
			}
			val options = OptionsManager.getInstance().options
			return options.editorOptions.zoomOptions
		}
	
	private inner class ImageContainerPane(private val imageComponent: ImageComponent) : JBLayeredPane() {
		init {
			add(imageComponent)
			putClientProperty(Magnificator.CLIENT_PROPERTY_KEY, Magnificator { scale, at ->
				val locationBefore = imageComponent.location
				val model = if(editor != null) editor.zoomModel else getZoomModel()
				val factor = model.zoomFactor
				model.zoomFactor = scale * factor
				Point(((at.x - max(if(scale > 1.0) locationBefore.x else 0, 0)) * scale).toInt(),
					((at.y - max(if(scale > 1.0) locationBefore.y else 0, 0)) * scale).toInt())
			})
		}
		
		private fun centerComponents() {
			val bounds = bounds
			val point = imageComponent.location
			// in embedded mode images should be left-side aligned
			point.x = if(isEmbedded) 0 else (bounds.width - imageComponent.width) / 2
			point.y = (bounds.height - imageComponent.height) / 2
			imageComponent.location = point
		}
		
		override fun invalidate() {
			centerComponents()
			super.invalidate()
		}
		
		override fun getPreferredSize(): Dimension {
			return imageComponent.size
		}
	}
	
	private inner class ImageWheelAdapter : MouseWheelListener {
		override fun mouseWheelMoved(e: MouseWheelEvent) {
			val options = OptionsManager.getInstance().options
			val editorOptions = options.editorOptions
			val zoomOptions = editorOptions.zoomOptions
			if(zoomOptions.isWheelZooming && e.isControlDown) {
				val rotation = e.wheelRotation
				val oldZoomFactor = zoomModel.zoomFactor
				val oldPosition = myScrollPane.viewport.viewPosition
				if(rotation > 0) {
					zoomModel.zoomOut()
				} else if(rotation < 0) {
					zoomModel.zoomIn()
				}
				
				// reset view, otherwise view size is not obtained correctly sometimes
				val view = myScrollPane.viewport.view
				myScrollPane.viewport = null
				myScrollPane.setViewportView(view)
				if(oldZoomFactor > 0 && rotation != 0) {
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
		private var myCustomZoomOptions: ZoomOptions? = null
		private var myZoomLevelChanged = false
		private val IMAGE_MAX_ZOOM_FACTOR = Double.MAX_VALUE
		private var zoomFactor = 0.0
		
		override fun getZoomFactor(): Double {
			return zoomFactor
		}
		
		override fun setZoomFactor(zoomFactor: Double) {
			val oldZoomFactor = getZoomFactor()
			if(oldZoomFactor.compareTo(zoomFactor) == 0) return
			this.zoomFactor = zoomFactor
			
			// Change current size
			updateImageComponentSize()
			revalidate()
			repaint()
			myZoomLevelChanged = false
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
				val factor = if(bounds != null) 1.0 / bounds.getWidth() else 0.0
				return max(factor, ImageZoomModel.MICRO_ZOOM_LIMIT)
			}
		
		override fun fitZoomToWindow() {
			val zoomOptions: ZoomOptions = zoomOptions
			val smartZoomFactor = getSmartZoomFactor(zoomOptions)
			if(smartZoomFactor != null) {
				zoomModel.zoomFactor = smartZoomFactor
			} else {
				zoomModel.zoomFactor = 1.0
			}
			myZoomLevelChanged = false
		}
		
		override fun zoomOut() {
			setZoomFactor(nextZoomOut)
			myZoomLevelChanged = true
		}
		
		override fun zoomIn() {
			setZoomFactor(nextZoomIn)
			myZoomLevelChanged = true
		}// Micro
		
		// Macro
		private val nextZoomOut: Double
			get() {
				var factor = getZoomFactor()
				if(factor > 1.0) {
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
				var factor = getZoomFactor()
				if(factor >= 1.0) {
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
			return getZoomFactor() - 1.0e-14 > minimumZoomFactor
		}
		
		override fun canZoomIn(): Boolean {
			return getZoomFactor() < maximumZoomFactor
		}
		
		override fun setZoomLevelChanged(value: Boolean) {
			myZoomLevelChanged = value
		}
		
		override fun isZoomLevelChanged(): Boolean {
			return myZoomLevelChanged
		}
		
		override fun getCustomZoomOptions(): ZoomOptions? {
			return myCustomZoomOptions
		}
		
		override fun setCustomZoomOptions(zoomOptions: ZoomOptions?) {
			myCustomZoomOptions = zoomOptions
		}
	}
	
	private fun getSmartZoomFactor(zoomOptions: ZoomOptions): Double? {
		val bounds = imageComponent.document.bounds ?: return null
		if(bounds.getWidth() == 0.0 || bounds.getHeight() == 0.0) return null
		val width = bounds.width
		val height = bounds.height
		val preferredMinimumSize = zoomOptions.prefferedSize
		if(width < preferredMinimumSize.width &&
			height < preferredMinimumSize.height) {
			val factor = (preferredMinimumSize.getWidth() / width.toDouble() +
				preferredMinimumSize.getHeight() / height.toDouble()) / 2.0
			return ceil(factor)
		}
		val canvasSize = myScrollPane.viewport.extentSize
		canvasSize.height -= ImageComponent.IMAGE_INSETS * 2
		canvasSize.width -= ImageComponent.IMAGE_INSETS * 2
		if(canvasSize.width <= 0 || canvasSize.height <= 0) return null
		return if(canvasSize.width < width ||
			canvasSize.height < height) {
			min(canvasSize.height.toDouble() / height,
				canvasSize.width.toDouble() / width)
		} else 1.0
	}
	
	private fun updateImageComponentSize() {
		val bounds = imageComponent.document.bounds
		if(bounds != null) {
			val zoom = getZoomModel().zoomFactor
			imageComponent.setCanvasSize(ceil(bounds.width * zoom).toInt(), ceil(bounds.height * zoom).toInt())
		}
	}
	
	private inner class DocumentChangeListener : ChangeListener {
		override fun stateChanged(e: ChangeEvent) {
			updateImageComponentSize()
			val document = imageComponent.document
			val value = document.value
			val layout = contentPanel.layout as CardLayout
			layout.show(contentPanel, if(value != null) IMAGE_PANEL else ERROR_PANEL)
			updateInfo()
			revalidate()
			repaint()
		}
	}
	
	private inner class FocusRequester : MouseAdapter() {
		override fun mousePressed(e: MouseEvent) {
			IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown { IdeFocusManager.getGlobalInstance().requestFocus(this@DdsEditorUI, true) }
		}
	}
	
	override fun getData(dataId: String): Any? {
		if(CommonDataKeys.PROJECT.`is`(dataId)) {
			return editor?.project
		} else if(CommonDataKeys.VIRTUAL_FILE.`is`(dataId)) {
			return editor?.file
		} else if(CommonDataKeys.VIRTUAL_FILE_ARRAY.`is`(dataId)) {
			return if(editor != null) arrayOf(editor.file) else VirtualFile.EMPTY_ARRAY
		} else if(PlatformDataKeys.COPY_PROVIDER.`is`(dataId)) {
			return this
		} else if(PlatformDataKeys.CUT_PROVIDER.`is`(dataId) && copyPasteSupport != null) {
			return copyPasteSupport.cutProvider
		} else if(PlatformDataKeys.DELETE_ELEMENT_PROVIDER.`is`(dataId)) {
			return deleteProvider
		} else if(ImageComponentDecorator.DATA_KEY.`is`(dataId)) {
			return editor ?: this
		} else if(PlatformCoreDataKeys.BGT_DATA_PROVIDER.`is`(dataId)) {
			return DataProvider { slowId: String -> getSlowData(slowId) }
		}
		return null
	}
	
	private fun getSlowData(dataId: String): Any? {
		if(CommonDataKeys.PSI_FILE.`is`(dataId)) {
			return findPsiFile()
		} else if(CommonDataKeys.PSI_ELEMENT.`is`(dataId)) {
			return findPsiFile()
		} else if(PlatformCoreDataKeys.PSI_ELEMENT_ARRAY.`is`(dataId)) {
			val psi: PsiElement? = findPsiFile()
			return if(psi != null) arrayOf(psi) else PsiElement.EMPTY_ARRAY
		}
		return null
	}
	
	private fun findPsiFile(): PsiFile? {
		if(editor == null) return null
		val file = editor.file
		return if(file.isValid) PsiManager.getInstance(editor.project).findFile(file) else null
	}
	
	override fun performCopy(dataContext: DataContext) {
		val document = imageComponent.document
		val image = document.value
		CopyPasteManager.getInstance().setContents(ImageTransferable(image))
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
		
		@Throws(UnsupportedFlavorException::class) override fun getTransferData(dataFlavor: DataFlavor): Any {
			if(!DataFlavor.imageFlavor.equals(dataFlavor)) {
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
