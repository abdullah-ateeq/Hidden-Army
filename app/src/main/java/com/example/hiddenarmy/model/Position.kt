package com.example.hiddenarmy.model

import kotlin.math.abs

data class Position(
    val row: Int,
    val column: Int
) {
    fun isValid(boardSize: Int = 7): Boolean {
        return row in 0 until boardSize && column in 0 until boardSize
    }

    fun isOrthogonallyAdjacent(other: Position): Boolean {
        val rowDiff = abs(row - other.row)
        val colDiff = abs(column - other.column)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    fun orthogonalDistance(other: Position): Int {
        return abs(row - other.row) + abs(column - other.column)
    }
}
