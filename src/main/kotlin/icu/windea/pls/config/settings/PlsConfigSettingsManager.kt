package icu.windea.pls.config.settings

import com.intellij.util.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.listeners.*

object PlsConfigSettingsManager {
    fun onConfigDirectoriesChanged(callbackLock: CallbackLock) {
        if (!callbackLock.check("onConfigDirectoriesChanged")) return

        application.messageBus.syncPublisher(ParadoxConfigDirectoriesListener.TOPIC).onChange()
    }

    fun onRemoteConfigDirectoriesChanged(callbackLock: CallbackLock) {
        if (!callbackLock.check("onRemoteConfigDirectoriesChanged")) return

        //NOTE 这里需要先验证是否真的需要刷新
        if (!CwtConfigRepositoryManager.isValidToSync()) return

        application.messageBus.syncPublisher(ParadoxConfigRepositoryUrlsListener.TOPIC).onChange()

        //等到从远程仓库异步同步完毕后，再通知规则目录发生更改，从而允许刷新规则分组数据
    }
}
