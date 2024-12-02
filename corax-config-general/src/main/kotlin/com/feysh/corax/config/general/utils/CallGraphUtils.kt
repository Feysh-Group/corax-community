package com.feysh.corax.config.general.utils

import com.feysh.corax.cache.coroutines.RecCoroutineLoadingCache
import com.feysh.corax.config.api.PreAnalysisApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import soot.MethodOrMethodContext
import soot.jimple.toolkits.callgraph.Edge

object CallGraphUtils {

    context(PreAnalysisApi)
    fun <T> edgeTraversalCache(
        forward: Boolean = true,
        weakKeyAssociateByValue: (value: T) -> Array<Any?>,
        edgeVisit: suspend RecCoroutineLoadingCache<MethodOrMethodContext, T>.(edgeIterator: Iterator<Edge>) -> T,
    ): RecCoroutineLoadingCache<MethodOrMethodContext, T> {
        val cg = cg
        return fastCache.buildRecCoroutineLoadingCache(
            scope = scope,
            weakKeyAssociateByValue = weakKeyAssociateByValue
        ) {
            val edgeIterator = if (forward) cg.edgesOutOf(it) else cg.edgesInto(it)
            edgeVisit(edgeIterator)
        }
    }

    context(PreAnalysisApi)
    fun edgeTraversalCache(
        forward: Boolean = true,
        matches: (edge: Edge) -> Boolean,
    ): RecCoroutineLoadingCache<MethodOrMethodContext, Boolean> {
        return edgeTraversalCache(forward, weakKeyAssociateByValue = { arrayOf() }) res@{ edgeIterator ->
            val jobs: MutableList<Deferred<Boolean>> = mutableListOf()
            for (edge in edgeIterator) {
                val tgt = edge.tgt()
                if (matches(edge)) {
                    jobs.forEach(Job::cancel)
                    return@res true
                } else {
                    get(tgt)?.let { jobs.add(it) }
                }
            }
            if (jobs.isEmpty()) {
                return@res false
            }
            for (j in jobs) {
                if (j.await()) {
                    jobs.forEach(Job::cancel)
                    return@res true
                }
            }
            return@res false
        }
    }


}
