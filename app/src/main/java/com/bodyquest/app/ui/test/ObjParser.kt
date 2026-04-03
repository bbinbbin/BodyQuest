package com.bodyquest.app.ui.test

import android.content.Context
import kotlin.math.sqrt

data class ObjModel(
    val vertexData: FloatArray,  // interleaved: px, py, pz, nx, ny, nz (6 floats/vertex)
    val vertexCount: Int,
    val center: FloatArray,
    val scale: Float
) {
    override fun equals(other: Any?) = other is ObjModel && vertexCount == other.vertexCount
    override fun hashCode() = vertexCount
}

object ObjParser {
    // 렌더링 대상 삼각형 수 (메모리·성능 균형)
    private const val TARGET_TRIANGLES = 60_000

    fun parse(context: Context, fileName: String): ObjModel? {
        return try {
            // ── Pass 1: 라인 카운트만 (빠른 스캔) ─────────────────────────────
            var vertCount = 0
            var faceCount = 0
            context.assets.open(fileName).bufferedReader().use { r ->
                r.lineSequence().forEach { line ->
                    when {
                        line.startsWith("v ") || line.startsWith("v\t") -> vertCount++
                        line.startsWith("f ") || line.startsWith("f\t") -> faceCount++
                    }
                }
            }
            if (vertCount == 0 || faceCount == 0) return null

            // 서브샘플링 스텝: 쿼드를 2삼각형으로 가정, TARGET_TRIANGLES 목표
            val step = maxOf(1, faceCount * 2 / TARGET_TRIANGLES)

            // ── 메모리 할당 ────────────────────────────────────────────────────
            // 정점 위치: 박싱 없이 compact FloatArray
            val posData = FloatArray(vertCount * 3)
            var posWritten = 0

            // 출력 버퍼: 최대 (TARGET_TRIANGLES + 여유) × 3verts × 6floats
            val maxOutVerts = (TARGET_TRIANGLES + 2000) * 3
            val outData = FloatArray(maxOutVerts * 6)
            var outWritten = 0   // floats 단위

            var faceIdx = 0

            // ── Pass 2: 실제 파싱 ──────────────────────────────────────────────
            context.assets.open(fileName).bufferedReader().use { r ->
                r.lineSequence().forEach line@{ rawLine ->
                    val line = rawLine.trim()
                    if (line.isEmpty() || line[0] == '#') return@line

                    when {
                        // 정점 위치 (Nomad Sculpt: "v x y z r g b" — xyz만 사용)
                        line.startsWith("v ") || line.startsWith("v\t") -> {
                            if (posWritten < vertCount) {
                                parseVertex(line, posData, posWritten)
                                posWritten++
                            }
                        }

                        // 면 — 서브샘플링 적용
                        line.startsWith("f ") || line.startsWith("f\t") -> {
                            if (faceIdx % step == 0) {
                                val spaceIdx = line.indexOfFirst { it == ' ' || it == '\t' }
                                val tokens = line.substring(spaceIdx + 1)
                                    .trim()
                                    .split("\\s+".toRegex())
                                val indices = IntArray(tokens.size) {
                                    parseFaceIndex(tokens[it], posWritten)
                                }
                                // 팬 삼각분할
                                for (i in 1 until indices.size - 1) {
                                    if (outWritten + 18 > outData.size) return@line
                                    addTriangle(
                                        outData, outWritten,
                                        indices[0], indices[i], indices[i + 1],
                                        posData, posWritten
                                    )
                                    outWritten += 18
                                }
                            }
                            faceIdx++
                        }
                    }
                }
            }

            if (outWritten == 0) return null

            val result = outData.copyOf(outWritten)
            val (center, scale) = computeNormalization(posData, posWritten)
            ObjModel(result, outWritten / 6, center, scale)

        } catch (_: Exception) {
            null
        }
    }

    // "v x y z ..."  → posData[base], [base+1], [base+2]
    private fun parseVertex(line: String, posData: FloatArray, idx: Int) {
        var start = 2  // skip "v "
        var fieldIdx = 0
        val base = idx * 3
        while (start < line.length && fieldIdx < 3) {
            // skip whitespace
            while (start < line.length && (line[start] == ' ' || line[start] == '\t')) start++
            var end = start
            while (end < line.length && line[end] != ' ' && line[end] != '\t') end++
            if (start < end) {
                posData[base + fieldIdx] = line.substring(start, end).toFloatOrNull() ?: 0f
                fieldIdx++
            }
            start = end
        }
    }

    // "1", "1/2", "1//3", "1/2/3" → 0-based index
    private fun parseFaceIndex(token: String, posCount: Int): Int {
        val slashIdx = token.indexOf('/')
        val vStr = if (slashIdx < 0) token else token.substring(0, slashIdx)
        val vi = vStr.toIntOrNull() ?: 1
        return (if (vi > 0) vi - 1 else posCount + vi).coerceIn(0, maxOf(posCount - 1, 0))
    }

    // 삼각형 추가 (박싱 없이 전부 primitive 연산)
    private fun addTriangle(
        out: FloatArray, outStart: Int,
        ai: Int, bi: Int, ci: Int,
        pos: FloatArray, posCount: Int
    ) {
        val safeMax = (posCount - 1).coerceAtLeast(0)
        val ia = ai.coerceIn(0, safeMax) * 3
        val ib = bi.coerceIn(0, safeMax) * 3
        val ic = ci.coerceIn(0, safeMax) * 3

        val pax = pos[ia];   val pay = pos[ia+1]; val paz = pos[ia+2]
        val pbx = pos[ib];   val pby = pos[ib+1]; val pbz = pos[ib+2]
        val pcx = pos[ic];   val pcy = pos[ic+1]; val pcz = pos[ic+2]

        // 면 법선 (cross product)
        val ux = pbx - pax; val uy = pby - pay; val uz = pbz - paz
        val vx = pcx - pax; val vy = pcy - pay; val vz = pcz - paz
        val nx = uy * vz - uz * vy
        val ny = uz * vx - ux * vz
        val nz = ux * vy - uy * vx
        val len = sqrt((nx*nx + ny*ny + nz*nz).toDouble()).toFloat().coerceAtLeast(1e-6f)
        val nnx = nx / len; val nny = ny / len; val nnz = nz / len

        var i = outStart
        out[i++] = pax; out[i++] = pay; out[i++] = paz; out[i++] = nnx; out[i++] = nny; out[i++] = nnz
        out[i++] = pbx; out[i++] = pby; out[i++] = pbz; out[i++] = nnx; out[i++] = nny; out[i++] = nnz
        out[i++] = pcx; out[i++] = pcy; out[i++] = pcz; out[i++] = nnx; out[i++] = nny; out[i  ] = nnz
    }

    private fun computeNormalization(pos: FloatArray, count: Int): Pair<FloatArray, Float> {
        if (count == 0) return Pair(floatArrayOf(0f, 0f, 0f), 1f)
        var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
        for (i in 0 until count) {
            val x = pos[i * 3]; val y = pos[i * 3 + 1]; val z = pos[i * 3 + 2]
            if (x < minX) minX = x; if (x > maxX) maxX = x
            if (y < minY) minY = y; if (y > maxY) maxY = y
            if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
        }
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val cz = (minZ + maxZ) / 2f
        val maxExt = maxOf(maxX - minX, maxY - minY, maxZ - minZ).coerceAtLeast(0.001f)
        return Pair(floatArrayOf(cx, cy, cz), 2f / maxExt)
    }
}
