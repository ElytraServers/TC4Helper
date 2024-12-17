package cn.elytra.mod.tc4h

import thaumcraft.api.aspects.Aspect
import thaumcraft.api.aspects.AspectList
import java.util.*

object TCAspectChain {

    data class Route(
        val aspect1: Aspect,
        val aspect2: Aspect,

        var path: LinkedList<Aspect>,
    ) {
        val length get() = path.size

        companion object {
            fun simpleRoute(node: Aspect) = Route(node, node, LinkedList<Aspect>().apply { add(node) })
        }

        fun toExtendedRoutes(): List<Route> {
            return aspect2.relatedAspects.map { Route(aspect1, it, LinkedList(path).apply { add(it) }) }
        }

        fun calcValue(aspectList: AspectList): Double {
            return path.sumOf { 100.0 / aspectList.getAmount(it) }
        }
    }

    val RelatedAspect = buildMap<Aspect, HashSet<Aspect>> {
        Aspect.aspects.values.forEach { thisAspect ->
            val thisAspectRelated = computeIfAbsent(thisAspect) { HashSet() }
            if(!thisAspect.isPrimal) {
                thisAspect.components.forEach { thatAspect ->
                    thisAspectRelated += thatAspect
                    val thatAspectRelated = computeIfAbsent(thatAspect) { HashSet() }
                    thatAspectRelated += thisAspect
                }
            }
        }
    }

    internal val Aspect.relatedAspects get() = RelatedAspect[this]!!

    fun getAspectChains(from: Aspect, to: Aspect, minLength: Int): List<Route> {
        val queue = LinkedList<Route>()
        val results = LinkedList<Route>()

        queue.add(Route.simpleRoute(from))

        while(queue.isNotEmpty()) {
            val route = queue.poll()

            if(route.length >= minLength) {
                if(route.aspect2 == to) {
                    results.add(route)
                    continue
                }
            }

            if(route.length >= 10) {
                continue
            }

            queue.addAll(route.toExtendedRoutes())
        }

        return results
    }

}
