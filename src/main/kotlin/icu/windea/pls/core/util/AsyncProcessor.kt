package icu.windea.pls.core.util

import com.intellij.util.*
import java.util.concurrent.*

/**
 * 并发执行的[Processor]。
 * 
 * 委托给它的[delegate]将会并发执行。
 * 当任意任务执行完毕且执行结果是false时，所有其他任务都会终止。
 */
class AsyncProcessor<T>(private val delegate: Processor<T>, private val executorService: ExecutorService) : Processor<T> {
    private val futures = mutableListOf<Future<Boolean>>()
    
    override fun process(e: T): Boolean {
        futures.add(executorService.submit(Callable {delegate.process(e)  }))
        var result = true
        
        while (result && futures.isNotEmpty()) {
            val future = futures.removeAt(0)
            try {
                result = future.get()
            } catch (e: Exception) {
                result = false
            }
            
            if (!result) {
                futures.forEach { it.cancel(true) }
                executorService.shutdownNow()
            }
        }
        
        return result
    }
}