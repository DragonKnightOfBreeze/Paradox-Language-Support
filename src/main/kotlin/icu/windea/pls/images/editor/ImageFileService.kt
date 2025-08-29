package icu.windea.pls.images.editor

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.images.vfs.IfsUtil

//org.intellij.images.editor.impl.ImageFileService

private val logger = logger<ImageFileService>()

@Service(Service.Level.PROJECT)
class ImageFileService(
    private val coroutineScope: CoroutineScope
) {
    fun createImageFileLoader(target: ImageEditorImpl): ImageFileLoader =
        ImageFileLoaderImpl(
            target,
            coroutineScope
        )

    class ImageFileLoaderImpl(private val target: ImageEditorImpl, childScope: CoroutineScope) : ImageFileLoader {

        private val flow = MutableSharedFlow<VirtualFile>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

        @Suppress("ObsoleteDispatchersEdt")
        private val job = childScope.launch(CoroutineName("ImageFileLoader for $target")) {
            flow.collectLatest { file ->
                try {
                    val imageProvider = withContext(Dispatchers.IO) { IfsUtil.getImageProvider(file) }
                    val format = withContext(Dispatchers.IO) { IfsUtil.getFormat(file) }
                    withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) {
                        target.setImageProvider(imageProvider, format)
                    }
                } catch (e: CancellationException) {
                    throw e // We don't care why it's cancelled: the editor is disposed or the next request has arrived.
                } catch (e: Exception) { // Likely an I/O error.
                    logger.warn("Exception when loading the image from $file", e)
                    target.setImageProvider(null, null)
                }
            }
        }

        override fun loadFile(file: VirtualFile?) {
            if (file == null) {
                target.setImageProvider(null, null)
                return
            }
            flow.tryEmit(file)
        }

        override fun dispose() {
            job.cancel()
        }
    }
}
