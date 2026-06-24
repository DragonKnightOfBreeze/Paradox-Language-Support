package icu.windea.pls.test

import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ParallelMethodRunner(klass: Class<*>?) : BlockJUnit4ClassRunner(klass) {
    override fun run(notifier: RunNotifier?) {
        val executor = Executors.newCachedThreadPool() // 线程数
        for (method in children) {
            executor.submit {
                // 每个方法独立运行，注意 RunNotifier 是线程安全的
                runChild(method, notifier)
            }
        }
        executor.shutdown()
        try {
            // 等待所有线程执行完毕
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
