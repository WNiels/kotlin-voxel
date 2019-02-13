package org.jrenner.learngl.gameworld

import org.jrenner.learngl.chunkPool
import org.jrenner.learngl.cube.CubeDataGrid
import org.jrenner.learngl.utils.refresh
import org.jrenner.learngl.view
import kotlin.properties.Delegates

class Chunk {
    companion object {
        var chunkSize = 16
        var chunkSizef = chunkSize.toFloat()
        val maxIndex = chunkSize - 1
        fun obtain(cdg: CubeDataGrid): Chunk {
            //val chunk = Pools.obtain(javaClass<Chunk>())
            val chunk = chunkPool.obtain()
            chunk.dataGrid = cdg
            chunk.dirty = true
            /*val p = Pools.get(javaClass<Chunk>())
            val peakFree = p.peak
            val freeCount = p.getFree()
            println("chunk pool, peak: $peakFree, free count: $freeCount")*/

            return chunk
        }
    }

    var dirty = false

    var dataGrid: CubeDataGrid by Delegates.notNull()

    val chunkMesh: ChunkMesh by lazy {
        ChunkMesh()
    }

    fun update() {
        if (dirty) {
            refresh()
            dirty = false
        }
    }



    fun inFrustum(): Boolean {
        val x = dataGrid.center.x
        val y = dataGrid.center.y
        val z = dataGrid.center.z
        val w = CubeDataGrid.width
        val h = CubeDataGrid.height
        val d = CubeDataGrid.depth
        return view.camera.frustum.boundsInFrustum(x, y, z, w/2f, h/2f, d/2f)
    }

    fun refresh() {
        dataGrid.refresh()
        chunkMesh.reset(dataGrid)
        chunkMesh.buildMesh()
    }


    fun dispose() {
        dataGrid.free()
        //chunkMesh.dispose()
        //Pools.free(this)
        chunkPool.free(this)
    }

    override fun hashCode(): Int {
        return dataGrid.hashCode()
    }
}