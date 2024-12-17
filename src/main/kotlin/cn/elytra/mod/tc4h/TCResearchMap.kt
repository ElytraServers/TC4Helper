package cn.elytra.mod.tc4h

import thaumcraft.api.aspects.Aspect
import thaumcraft.common.lib.utils.HexUtils
import thaumcraft.common.lib.research.ResearchNoteData
import java.util.LinkedList
import java.util.Objects

class TCResearchMap(data: ResearchNoteData) {

    data class Node(
        val hex: HexUtils.Hex,
        var type: Int = 0,
        var aspect: Aspect? = null,
        val neighbors: MutableList<Node> = mutableListOf(),
    ) {
        fun init(type: Int, aspect: Aspect?) {
            this.type = type
            this.aspect = aspect
        }

        override fun toString(): String {
            return "[$hex] $type $aspect"
        }

        override fun hashCode(): Int {
            return Objects.hash(hex, type, aspect)
        }

        override fun equals(other: Any?): Boolean {
            if(this === other) return true
            if(other !is Node) return false

            if(type != other.type) return false
            if(hex != other.hex) return false
            if(aspect != other.aspect) return false
            if(neighbors != other.neighbors) return false

            return true
        }
    }

    data class Route(
        val node1: Node,
        val node2: Node,

        var path: LinkedList<Node>,
    ) {

        val length: Int get() = path.size

        companion object {
            fun simpleRoute(node: Node) = Route(node, node, LinkedList<Node>().apply { add(node) })
        }

        fun toExtendedRoutes(): List<Route> {
            return node2.neighbors.filter { it !in path }.map { Route(node1, it, LinkedList(path).apply { add(it) }) }
        }
    }

    val map: MutableMap<String, Node> = mutableMapOf()
    val roots: MutableList<Node> = mutableListOf()

    init {
        data.hexes.keys.forEach { hexKey ->
            val hex = data.hexes[hexKey]!!
            val hexEntry = data.hexEntries[hexKey]!!

            val node = map.computeIfAbsent(hexKey) { Node(hex) }
            node.init(hexEntry.type, hexEntry.aspect)

            for(hexDirection in 0..<6) {
                val neighborHex = node.hex.getNeighbour(hexDirection)
                val neighborHexKey = neighborHex.toString()
                if(neighborHexKey in data.hexes) {
                    val neighborNode = map.computeIfAbsent(neighborHexKey) { Node(neighborHex) }
                    node.neighbors += neighborNode
                }
            }

            if(node.type == 1) {
                roots += node
            }
        }
    }

    fun findShortestRoute(from: HexUtils.Hex, to: HexUtils.Hex): Route? {
        val fromNode = map[from.toString()]!!
        val toNode = map[to.toString()]!!

        val queue = LinkedList<Route>()
        queue.push(Route.simpleRoute(fromNode))

        while(queue.isNotEmpty()) {
            val route = queue.pop()

            assert(route.node1 == fromNode)

            if(route.node2 == toNode) {
                return route
            }

            queue.addAll(route.toExtendedRoutes())
        }

        return null
    }

    fun findRoutesByLength(from: HexUtils.Hex, to: HexUtils.Hex, length: Int): Set<Route> {
        val fromNode = map[from.toString()]!!
        val toNode = map[to.toString()]!!

        val queue = LinkedList<Route>()
        queue.push(Route.simpleRoute(fromNode))
        val result = mutableSetOf<Route>()

        while(queue.isNotEmpty()) {
            val route = queue.pop()

            assert(route.node1 == fromNode)

            if(route.length >= length) {
                if(route.node2 == toNode) {
                    result.add(route)
                    continue
                }
            }

            if(route.length > length * 2) {
                continue
            }

            queue.addAll(route.toExtendedRoutes())
        }

        return result
    }

}
