package icu.windea.pls.images.editor

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.vfs.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import org.intellij.images.vfs.*

//org.intellij.images.editor.impl.ImageFileService

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
                    LOG.warn("Exception when loading the image from $file", e)
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

private val LOG = logger<ImageFileService>()
