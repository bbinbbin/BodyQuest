package com.bodyquest.app.ui.test

import android.content.Context
import com.bodyquest.app.util.AppLogger
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.sqrt

object GlbParser {

    private const val TARGET_TRIANGLES = 120_000
    private const val MAGIC_GLTF = 0x46546C67.toInt()
    private const val CHUNK_JSON  = 0x4E4F534A.toInt()
    private const val CHUNK_BIN   = 0x004E4942.toInt()
    private const val COMP_FLOAT  = 5126
    private const val COMP_USHORT = 5123
    private const val COMP_UINT   = 5125

    private val IDENTITY = floatArrayOf(
        1f,0f,0f,0f, 0f,1f,0f,0f, 0f,0f,1f,0f, 0f,0f,0f,1f
    )

    private data class Accessor(
        val bufferViewIndex: Int,
        val byteOffset: Int,
        val componentType: Int,
        val count: Int,
        val type: String
    )

    private data class BufferView(
        val byteOffset: Int,
        val byteStride: Int
    )

    // 월드 공간으로 변환된 메쉬 데이터
    private data class MeshData(
        val worldPos: FloatArray,  // count*3, 월드 공간 위치
        val indices: IntArray?,
        val posCount: Int,
        val triCount: Int
    )

    fun parse(context: Context, fileName: String): ObjModel? {
        return try {
            // ── 파일 읽기 ──────────────────────────────────────────────────────
            val bytes = context.assets.open(fileName).readBytes()
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

            // ── GLB 헤더 검증 ──────────────────────────────────────────────────
            if (buf.getInt(0) != MAGIC_GLTF) return null

            // ── JSON chunk ────────────────────────────────────────────────────
            val jsonLen = buf.getInt(12)
            if (buf.getInt(16) != CHUNK_JSON) return null
            val json = JSONObject(String(bytes, 20, jsonLen, Charsets.UTF_8))

            // ── BIN chunk ─────────────────────────────────────────────────────
            val binHeaderOffset = 20 + jsonLen
            if (binHeaderOffset + 8 > bytes.size) return null
            if (buf.getInt(binHeaderOffset + 4) != CHUNK_BIN) return null
            val binLen = buf.getInt(binHeaderOffset)
            if (binHeaderOffset + 8 + binLen > bytes.size) return null
            val bin: ByteBuffer = ByteBuffer.wrap(bytes, binHeaderOffset + 8, binLen)
                .slice().order(ByteOrder.LITTLE_ENDIAN)

            // ── JSON 파싱 ─────────────────────────────────────────────────────
            val accessorsJ  = json.optJSONArray("accessors")  ?: return null
            val bufViewsJ   = json.optJSONArray("bufferViews") ?: return null
            val meshesJ     = json.optJSONArray("meshes")      ?: return null
            val nodesJ      = json.optJSONArray("nodes")       ?: return null

            val accessors = List(accessorsJ.length()) { i ->
                val a = accessorsJ.getJSONObject(i)
                Accessor(
                    bufferViewIndex = a.optInt("bufferView", -1),
                    byteOffset      = a.optInt("byteOffset", 0),
                    componentType   = a.optInt("componentType", 0),
                    count           = a.optInt("count", 0),
                    type            = a.optString("type", "")
                )
            }

            val bufViews = List(bufViewsJ.length()) { i ->
                val bv = bufViewsJ.getJSONObject(i)
                BufferView(
                    byteOffset = bv.optInt("byteOffset", 0),
                    byteStride = bv.optInt("byteStride", 0)
                )
            }

            // ── 노드 계층 순회 → 월드 변환 행렬 계산 ─────────────────────────
            val worldMatrices = computeWorldMatrices(json, nodesJ)

            // ── 각 노드의 메쉬 읽기 (모든 메쉬 병합) ─────────────────────────
            val meshDataList = mutableListOf<MeshData>()
            var minX =  Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY =  Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ =  Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            for (ni in 0 until nodesJ.length()) {
                val node = nodesJ.getJSONObject(ni)
                val meshIdx = node.optInt("mesh", -1)
                if (meshIdx < 0 || meshIdx >= meshesJ.length()) continue

                val wm = worldMatrices[ni] ?: IDENTITY
                val mesh = meshesJ.getJSONObject(meshIdx)
                val primsJ = mesh.optJSONArray("primitives") ?: continue

                // posAccIdx → 월드변환된 위치 캐시 (같은 POSITION accessor를 공유하는 primitive들)
                val posCache = HashMap<Int, FloatArray>()

                for (pi in 0 until primsJ.length()) {
                    val prim  = primsJ.getJSONObject(pi)
                    val attrs = prim.optJSONObject("attributes") ?: continue
                    val posAccIdx = attrs.optInt("POSITION", -1)
                    if (posAccIdx !in 0 until accessors.size) continue
                    val posAcc = accessors[posAccIdx]
                    if (posAcc.componentType != COMP_FLOAT || posAcc.type != "VEC3") continue
                    if (posAcc.bufferViewIndex < 0) continue

                    // 캐시에 없으면 읽어서 월드 변환 + AABB 갱신
                    val worldPos = posCache.getOrPut(posAccIdx) {
                        val rawPos = readVec3(posAcc, bufViews, bin) ?: return@getOrPut FloatArray(0)
                        for (i in 0 until posAcc.count) {
                            val lx = rawPos[i*3]; val ly = rawPos[i*3+1]; val lz = rawPos[i*3+2]
                            val wx = wm[0]*lx + wm[4]*ly + wm[8]*lz  + wm[12]
                            val wy = wm[1]*lx + wm[5]*ly + wm[9]*lz  + wm[13]
                            val wz = wm[2]*lx + wm[6]*ly + wm[10]*lz + wm[14]
                            rawPos[i*3] = wx; rawPos[i*3+1] = wy; rawPos[i*3+2] = wz
                            if (wx < minX) minX = wx; if (wx > maxX) maxX = wx
                            if (wy < minY) minY = wy; if (wy > maxY) maxY = wy
                            if (wz < minZ) minZ = wz; if (wz > maxZ) maxZ = wz
                        }
                        rawPos
                    }
                    if (worldPos.isEmpty()) continue

                    val idxAccIdx = prim.optInt("indices", -1)
                    val indices   = if (idxAccIdx in 0 until accessors.size)
                        readIndices(accessors[idxAccIdx], bufViews, bin)
                    else null

                    val triCount = if (indices != null) indices.size / 3 else posAcc.count / 3
                    if (triCount > 0) meshDataList.add(MeshData(worldPos, indices, posAcc.count, triCount))
                }
            }

            if (meshDataList.isEmpty()) return null

            // ── 전체 AABB로 정규화 계산 ────────────────────────────────────────
            val cx = (minX + maxX) / 2f
            val cy = (minY + maxY) / 2f
            val cz = (minZ + maxZ) / 2f
            val maxExt = maxOf(maxX - minX, maxY - minY, maxZ - minZ).coerceAtLeast(0.001f)
            val center = floatArrayOf(cx, cy, cz)
            val scale  = 2f / maxExt

            // ── 서브샘플링 + outData 조립 ─────────────────────────────────────
            val totalTris = meshDataList.sumOf { it.triCount }
            val step = max(1, totalTris / TARGET_TRIANGLES)

            val maxOutVerts = (TARGET_TRIANGLES + 3000) * 3
            val outData = FloatArray(maxOutVerts * 6)
            var outWritten = 0

            for (md in meshDataList) {
                val pos     = md.worldPos
                val indices = md.indices
                val safeMax = (md.posCount - 1).coerceAtLeast(0)

                for (triIdx in 0 until md.triCount) {
                    if (triIdx % step != 0) continue
                    if (outWritten + 18 > outData.size) break

                    val ai: Int; val bi: Int; val ci: Int
                    if (indices != null) {
                        if (triIdx * 3 + 2 >= indices.size) continue
                        ai = indices[triIdx * 3]
                        bi = indices[triIdx * 3 + 1]
                        ci = indices[triIdx * 3 + 2]
                    } else {
                        ai = triIdx * 3; bi = ai + 1; ci = ai + 2
                    }

                    val sia = ai.coerceIn(0, safeMax)
                    val sib = bi.coerceIn(0, safeMax)
                    val sic = ci.coerceIn(0, safeMax)

                    val pax = pos[sia*3]; val pay = pos[sia*3+1]; val paz = pos[sia*3+2]
                    val pbx = pos[sib*3]; val pby = pos[sib*3+1]; val pbz = pos[sib*3+2]
                    val pcx = pos[sic*3]; val pcy = pos[sic*3+1]; val pcz = pos[sic*3+2]

                    // face normal (cross product)
                    val ux = pbx-pax; val uy = pby-pay; val uz = pbz-paz
                    val vx = pcx-pax; val vy = pcy-pay; val vz = pcz-paz
                    val fnx = uy*vz - uz*vy
                    val fny = uz*vx - ux*vz
                    val fnz = ux*vy - uy*vx
                    val len = sqrt((fnx*fnx + fny*fny + fnz*fnz).toDouble()).toFloat().coerceAtLeast(1e-6f)
                    val nnx = fnx/len; val nny = fny/len; val nnz = fnz/len

                    var i = outWritten
                    outData[i++]=pax; outData[i++]=pay; outData[i++]=paz; outData[i++]=nnx; outData[i++]=nny; outData[i++]=nnz
                    outData[i++]=pbx; outData[i++]=pby; outData[i++]=pbz; outData[i++]=nnx; outData[i++]=nny; outData[i++]=nnz
                    outData[i++]=pcx; outData[i++]=pcy; outData[i++]=pcz; outData[i++]=nnx; outData[i++]=nny; outData[i  ]=nnz
                    outWritten += 18
                }
            }

            if (outWritten == 0) return null
            ObjModel(outData.copyOf(outWritten), outWritten / 6, center, scale)

        } catch (e: Exception) {
            AppLogger.w("GlbParser", "GLB 파싱 실패", e)
            null
        }
    }

    // ── 노드 계층 BFS → 월드 행렬 맵 ────────────────────────────────────────
    private fun computeWorldMatrices(json: JSONObject, nodesJ: JSONArray): Map<Int, FloatArray> {
        val result = HashMap<Int, FloatArray>()
        val scenesJ = json.optJSONArray("scenes") ?: return result
        val activeScene = json.optInt("scene", 0)
        if (activeScene >= scenesJ.length()) return result
        val rootNodesJ = scenesJ.getJSONObject(activeScene).optJSONArray("nodes") ?: return result

        // (nodeIndex, parentWorldMatrix) BFS 큐
        val queue = ArrayDeque<Pair<Int, FloatArray>>()
        for (i in 0 until rootNodesJ.length()) {
            queue.add(rootNodesJ.getInt(i) to IDENTITY.copyOf())
        }

        while (queue.isNotEmpty()) {
            val (ni, parentWM) = queue.removeFirst()
            if (ni < 0 || ni >= nodesJ.length()) continue
            val node = nodesJ.getJSONObject(ni)
            val localM  = nodeLocalMatrix(node)
            val worldM  = matMul4(parentWM, localM)
            result[ni]  = worldM

            val childrenJ = node.optJSONArray("children")
            if (childrenJ != null) {
                for (ci in 0 until childrenJ.length()) {
                    queue.add(childrenJ.getInt(ci) to worldM.copyOf())
                }
            }
        }
        return result
    }

    // 노드 로컬 행렬 파싱 (column-major 16개 float)
    private fun nodeLocalMatrix(node: JSONObject): FloatArray {
        val matJ = node.optJSONArray("matrix")
        if (matJ != null && matJ.length() == 16) {
            return FloatArray(16) { matJ.getDouble(it).toFloat() }
        }
        return IDENTITY.copyOf()
    }

    // 4×4 column-major 행렬 곱 (a × b)
    private fun matMul4(a: FloatArray, b: FloatArray): FloatArray {
        val r = FloatArray(16)
        for (col in 0 until 4) {
            for (row in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) sum += a[k * 4 + row] * b[col * 4 + k]
                r[col * 4 + row] = sum
            }
        }
        return r
    }

    // VEC3 FLOAT accessor → FloatArray(count×3)
    private fun readVec3(acc: Accessor, bvs: List<BufferView>, bin: ByteBuffer): FloatArray? {
        val bv = bvs.getOrNull(acc.bufferViewIndex) ?: return null
        val stride = if (bv.byteStride > 0) bv.byteStride else 12
        val base   = bv.byteOffset + acc.byteOffset
        val endByte = base.toLong() + (acc.count.toLong() - 1) * stride + 12
        if (acc.count <= 0 || endByte > bin.capacity()) return null

        val result = FloatArray(acc.count * 3)
        for (i in 0 until acc.count) {
            val off = base + i * stride
            result[i*3]   = bin.getFloat(off)
            result[i*3+1] = bin.getFloat(off + 4)
            result[i*3+2] = bin.getFloat(off + 8)
        }
        return result
    }

    // SCALAR UINT/USHORT accessor → IntArray(count)
    private fun readIndices(acc: Accessor, bvs: List<BufferView>, bin: ByteBuffer): IntArray? {
        val bv = bvs.getOrNull(acc.bufferViewIndex) ?: return null
        val elemSize = when (acc.componentType) {
            COMP_USHORT -> 2
            COMP_UINT   -> 4
            else        -> return null
        }
        val stride  = if (bv.byteStride > 0) bv.byteStride else elemSize
        val base    = bv.byteOffset + acc.byteOffset
        val endByte = base.toLong() + (acc.count.toLong() - 1) * stride + elemSize
        if (acc.count <= 0 || endByte > bin.capacity()) return null

        val result = IntArray(acc.count)
        for (i in 0 until acc.count) {
            val off = base + i * stride
            result[i] = when (acc.componentType) {
                COMP_USHORT -> bin.getShort(off).toInt() and 0xFFFF
                else        -> bin.getInt(off)
            }
        }
        return result
    }
}
