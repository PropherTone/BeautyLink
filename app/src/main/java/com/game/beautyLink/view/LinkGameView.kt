package com.game.beautyLink.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.game.beautyLink.TAG
import java.util.LinkedList
import kotlin.math.roundToInt

class LinkGameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val itemArr = arrayOf(
        intArrayOf(-1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1),
        intArrayOf(-1, -1, -1, -1)
    )

    private var mode = 0
    private var itemW = 0

    private var activeItemCount = 0
    private var childMap = mutableMapOf<Point, View>()
    private var activeChildMap = mutableMapOf<Int, Point>()
    private val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 12f
    }
    private var path: List<Point>? = null
    private var drawLine = false
    private var gameFinishListener: GameFinishListener? = null

    fun onGameFinish(gameFinishListener: GameFinishListener) {
        this.gameFinishListener = gameFinishListener
    }

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val tempH = (h / 7)
        val tempW = tempH * 4f
        itemW = (if (tempW > w) {
            tempH * (w / tempW)
        } else 1f * tempH).roundToInt()
    }

    fun reset(items: List<Int>, hardCore: Boolean = false) {
        activeChildMap.clear()
        activeItemCount = 0
        childMap.clear()
        removeAllViews()
        post {
            mode = (0..1).random()
            val startX = (width - itemW * 4) / 2f
            generateItemArr(items, hardCore)
            walkItemArr { indexX, indexY ->
                addView(LinkItemView(context).also {
                    it.layoutParams = LayoutParams(itemW, itemW)
                    it.x = (itemW * indexX + startX)
                    it.y = (itemW * indexY).toFloat()
                    childMap[Point(indexX, indexY)] = it
                    if (itemArr[indexY][indexX] == -1) return@also
                    it.setIcon(itemArr[indexY][indexX].also { res ->
                        it.tag = res
                    }) { isActive, itemView ->
                        if (isActive) {
                            activeChildMap[indexOfChild(itemView)] = Point(indexX, indexY)
                            if (++activeItemCount == 2) {
                                val keys = activeChildMap.keys.toList()
                                val child1 = activeChildMap[keys[0]] ?: return@setIcon
                                val child2 = activeChildMap[keys[1]] ?: return@setIcon
                                val path = findShortestPath(itemArr, child1, child2)
                                var sameItem = false
                                postDelayed({
                                    this.path = null
                                    invalidate()
                                    keys.forEach { key ->
                                        val childAt = getChildAt(key)
                                        if (childAt is LinkItemView) {
                                            childAt.negative()
                                            activeItemCount--
                                            val point = activeChildMap.remove(key)
                                            if (sameItem && point != null) {
                                                itemArr[point.y][point.x] = -1
                                                childAt.clear()
                                            }
                                            checkResult()
                                        }
                                    }
                                }, 500L)
                                if (path == null) return@setIcon
                                linePaint.color =
                                    if (childMap[child1]?.tag == childMap[child2]?.tag) {
                                        sameItem = true
                                        Color.GREEN
                                    } else Color.RED
                                this.path = path
                                drawLine = true
                                invalidate()
                            }
                        } else {
                            activeItemCount--
                            activeChildMap.remove(indexOfChild(it))
                        }
                    }
                })
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!drawLine) return
        drawLine = false
        path?.let { points ->
            if (points.size <= 1) return
            for (i in 1 until points.size) {
                val preChild = childMap[points[i - 1]]
                val child = childMap[points[i]]
                if (preChild == null || child == null) return
                val offsite = child.width / 2
                canvas?.drawLine(
                    preChild.x + offsite,
                    preChild.y + offsite,
                    child.x + offsite,
                    child.y + offsite,
                    linePaint
                )
            }
        }
    }

    private inline fun walkItemArr(walker: (Int, Int) -> Unit) {
        itemArr.forEachIndexed { indexY, ints ->
            ints.forEachIndexed { indexX, _ ->
                walker(indexX, indexY)
            }
        }
    }

    private fun generateItemArr(items: List<Int>, hardCore: Boolean) {
        val filledList = mutableListOf<Point>()
        walkItemArr { x, y ->
            itemArr[y][x] = -1
            when {
                y == 3 && !hardCore -> return@walkItemArr
                mode == 0 && (y == 0 || y == 2 || y == 4 || y == 6) && (x == 0 || x == 3) -> return@walkItemArr
                mode == 1 && (y == 0 || y == 6) -> return@walkItemArr
                else -> filledList.add(Point(x, y))
            }
        }
        fun getPoint(): Point {
            val randomIndex = (filledList.indices).random()
            val point = filledList[randomIndex]
            filledList.removeAt(randomIndex)
            return point
        }
        while (filledList.size > 0) {
            val point = getPoint()
            val point1 = getPoint()
            val item = items[(items.indices).random()]
            itemArr[point.y][point.x] = item
            itemArr[point1.y][point1.x] = item
        }
    }

    private fun checkResult() {
        var success = true
        walkItemArr { x, y ->
            success = success && itemArr[y][x] == -1
        }
        if (success) gameFinishListener?.onFinish()
    }

    private fun findShortestPath(matrix: Array<IntArray>, start: Point, end: Point): List<Point>? {
        val visited = mutableSetOf(start)
        val queue = LinkedList(listOf(listOf(start)))

        while (queue.isNotEmpty()) {
            val path = queue.pop()
            val last = path.last()

            if (last == end) return path

            for ((dx, dy) in listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
                val next = Point(last.x + dx, last.y + dy)
                if (next.y !in matrix.indices || next.x !in matrix[0].indices) continue
                if (next in visited) continue
                if (matrix[next.y][next.x] != -1 && next != end) continue
                visited.add(next)
                queue.add(path + next)
            }
        }

        return null
    }

    data class Point(val x: Int, val y: Int)

    fun interface GameFinishListener {
        fun onFinish()
    }
}