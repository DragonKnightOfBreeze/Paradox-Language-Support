package icu.windea.pls.core.util

import com.intellij.util.*
import icu.windea.pls.core.collections.*
import java.util.concurrent.*

class ParallelProcessor<T>(
    val delegate: Processor<T>,
    private val executorService: ExecutorService
) : Processor<T> {
    private val futures = mutableListOf<CompletableFuture<Boolean>>()
    
    override fun process(e: T): Boolean {
        val future = CompletableFuture.supplyAsync({ process(e) }, executorService)
        futures.add(future)
        return true
    }
    
    fun getResult(): Boolean {
        val anyFuture = CompletableFuture.anyOf(*futures.toTypedArray())
        val anyFutureResult = anyFuture.get() as Boolean
        if(!anyFutureResult) {
            futures.forEachFast { future ->
                if(!future.isDone) {
                    future.cancel(true)
                }
            }
            return false
        }
        futures.forEachFast {
            it.get()
        }
        return true
    }
}