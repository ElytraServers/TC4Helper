package cn.elytra.mod.tc4h

import cn.elytra.mod.tc4h.TCResearchMap.Node
import org.apache.logging.log4j.LogManager
import thaumcraft.api.aspects.Aspect
import thaumcraft.api.aspects.AspectList
import thaumcraft.common.lib.research.ResearchNoteData
import thaumcraft.common.lib.utils.HexUtils
import java.util.*

object TCResearchResolver {

    private val Log = LogManager.getLogger()

    data class Line(
        val node1: Node,
        val node2: Node,
    ) {
        override fun hashCode(): Int {
            return node1.hashCode() + node2.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if(this === other) return true
            if(other !is Line) return false

            if(node1 != other.node1 && node1 != other.node2) return false
            if(node2 != other.node2 && node2 != other.node1) return false

            return true
        }
    }

    data class TheAnswer(
        val path: TCResearchMap.Route,
        val aspectPath: TCAspectChain.Route,
        val cost: Double,
    ) {
        companion object {
            val INVALID_ANSWER = TheAnswer(
                TCResearchMap.Route.simpleRoute(Node(HexUtils.Hex(0, 0))),
                TCAspectChain.Route.simpleRoute(Aspect.VOID),
                99999.0
            )
        }
    }

    /**
     * Resolve the Research and perform the action.
     *
     * It is VERY heavy, you should not run it on the rendering thread!
     */
    fun execute(data: ResearchNoteData, aspectList: AspectList, gui: IGuiResearchTableHelper) {
        val map = TCResearchMap(data)
        val roots = map.roots

        // the edges of the roots
        val lines: List<Line> = buildList {
            for(i in 0 until roots.size) {
                for(j in i + 1 until roots.size) {
                    add(Line(roots[i], roots[j]))
                }
            }
        }

        val theAnswers: Map<Line, TheAnswer> = lines.associateWith { (n1, n2) ->
            Log.info("Looking for the best resolution for roots {} and {}", n1, n2)

            // the shortest map route, to initialize the bargain length
            var shortestMapRoute = map.findShortestRoute(n1.hex, n2.hex)
            if(shortestMapRoute == null) return@associateWith TheAnswer.INVALID_ANSWER

            // all aspect routes group by the length
            val allAspectRoutes = TCAspectChain.getAspectChains(n1.aspect!!, n2.aspect!!, shortestMapRoute.length)
                .groupBy { it.length }
            if(allAspectRoutes.isEmpty()) return@associateWith TheAnswer.INVALID_ANSWER

            // the length to test if both routes meet the requirements
            // it is slowly increased by 1 in the loop below to find a good length
            var bargainLength = shortestMapRoute.length
            while(true) {
                Log.info("Bargain length {}", bargainLength)
                // check if there is a nice aspect route
                val aspectRoutesAtTheLength = allAspectRoutes[bargainLength]
                if(aspectRoutesAtTheLength == null) { // cannot find aspect route at the length
                    Log.info("Aspect routes not fulfilled, increasing bargain length")
                    bargainLength++
                    continue
                }

                // check if there is a nice map route
                val mapRouteAtTheLength = map.findRoutesByLength(n1.hex, n2.hex, bargainLength)
                if(mapRouteAtTheLength.isEmpty()) {
                    Log.info("Map routes not fulfilled, increasing bargain length")
                    bargainLength++
                    continue
                }

                if(bargainLength == shortestMapRoute.length * 2) {
                    // not possible to find anymore, we give up
                    Log.warn("Bargain length {} is exceeding the limit, skipped!", bargainLength)
                    return@associateWith TheAnswer.INVALID_ANSWER
                }

                val (selectedAspectRoute, selectedAspectRouteCost) = aspectRoutesAtTheLength
                    .associateWith { getAspectRouteCheapness(it.path, aspectList) }
                    .minBy { it.value }
                return@associateWith TheAnswer(
                    mapRouteAtTheLength.first(),
                    selectedAspectRoute,
                    selectedAspectRouteCost
                )
            }

            error("unreachable")
        }.filterValues { it != TheAnswer.INVALID_ANSWER }
        Log.info("Root-to-root edges prepared")

        // minimum spanning tree

        val sortedEdges = theAnswers.entries
            .sortedBy { it.value.cost }
        val parent = mutableMapOf<Node, Node>()
        val rank = mutableMapOf<Node, Int>()

        fun find(node: Node): Node {
            if(parent[node] != node) {
                parent[node] = find(parent[node]!!)
            }
            return parent[node]!!
        }

        fun union(node1: Node, node2: Node) {
            val root1 = find(node1)
            val root2 = find(node2)

            if(root1 != root2) {
                if(rank[root1]!! < rank[root2]!!) {
                    parent[root1] = root2
                } else if(rank[root1]!! > rank[root2]!!) {
                    parent[root2] = root1
                } else {
                    parent[root2] = root1
                    rank[root1] = rank[root1]!! + 1
                }
            }
        }

        theAnswers.keys.flatMap { listOf(it.node1, it.node2) }.distinct().forEach { node ->
            parent[node] = node
            rank[node] = 0
        }

        val performResult = mutableListOf<Pair<Line, TheAnswer>>()
        for((edge, answer) in sortedEdges) {
            val node1 = edge.node1
            val node2 = edge.node2

            if(find(node1) != find(node2)) {
                performResult.add(edge to answer)
                union(node1, node2)
            }
        }

        Log.info("Edges connected")

        performResult.forEach { (l, answer) ->
            answer.path.path.zip(answer.aspectPath.path).forEach { (node, aspectToPlace) ->
                Log.debug("Placing {} to hex {}", aspectToPlace.tag, node.hex)
                gui.`tc4h$placeAspect`(node.hex, aspectToPlace)
            }
        }

        Log.info("Aspects placed; it should be completed!")
    }

    private fun getAspectCheapness(aspect: Aspect, aspectList: AspectList?): Double {
        return if(aspectList != null) {
            1000.0 / aspectList.getAmount(aspect)
        } else 1000.0
    }

    private fun getAspectRouteCheapness(aspectRoute: LinkedList<Aspect>?, aspectList: AspectList?): Double {
        return aspectRoute?.sumOf { getAspectCheapness(it, aspectList) } ?: 999999.0
    }

}
