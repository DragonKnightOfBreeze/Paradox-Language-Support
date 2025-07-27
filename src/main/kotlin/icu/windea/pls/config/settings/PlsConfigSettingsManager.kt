package icu.windea.pls.config.settings

import com.intellij.openapi.application.*
import icu.windea.pls.config.util.*
import icu.windea.pls.lang.listeners.*

object PlsConfigSettingsManager {
    fun onConfigDirectoriesChanged(callbackLock: MutableSet<String>? = null) {
        if (callbackLock != null && !callbackLock.add("onConfigDirectoriesChanged")) return

        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxConfigDirectoriesListener.TOPIC).onChange()
    }

    fun onRemoteConfigDirectoriesChanged(callbackLock: MutableSet<String>? = null) {
        if (callbackLock != null && !callbackLock.add("onRemoteConfigDirectoriesChanged")) return

        //NOTE 这里需要先验证是否真的需要刷新
        if (!PlsConfigRepositoryManager.isValidToSync()) return

        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxConfigRepositoryUrlsListener.TOPIC).onChange()

        //等到从远程仓库异步同步完毕后，再通知规则目录发生更改，从而允许刷新规则分组数据
    }
}
