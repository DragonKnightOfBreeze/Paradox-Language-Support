package icu.windea.pls.images

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import kotlinx.coroutines.CoroutineScope

/**
 * 将图片渲染会话附加到 hints/documentation 等通用上下文。
 */
object ImageRenderContext {
    private val sessionKey = Key.create<ImageRenderSession>("chronicle.image.renderSession")

    fun getSession(context: UserDataHolder): ImageRenderSession? {
        return context.getUserData(sessionKey)
    }

    fun setSession(context: UserDataHolder, session: ImageRenderSession) {
        context.putUserData(sessionKey, session)
    }

    fun getOrCreateSession(context: UserDataHolder, scope: CoroutineScope): ImageRenderSession {
        return getSession(context) ?: ImageRenderService.getInstance().newRenderSession(scope).also { setSession(context, it) }
    }
}
