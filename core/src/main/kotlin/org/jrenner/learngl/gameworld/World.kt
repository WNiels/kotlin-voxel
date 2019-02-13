package org.jrenner.learngl.gameworld

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.DelayedRemovalArray
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import org.jrenner.learngl.*
import org.jrenner.learngl.cube.CubeDataGrid
import org.jrenner.learngl.cube.WorldChunkData
import org.jrenner.learngl.utils.calculateHiddenFaces
import org.jrenner.learngl.utils.threeIntegerHashCode
import com.badlogic.gdx.utils.Array as Arr

class World(val width: Int, val height: Int, val depth: Int) {
    
    var updater: WorldUpdater? = null

    var updatesEnabled = true

    private fun chunkCount(dimen: Int): Int {
        val chunkSize = Chunk.chunkSize
        return (if (dimen % chunkSize > 0) 1 else 0) + dimen / chunkSize
    }

    var chunksCreated = 0L
    var chunksRemoved = 0L

    val numChunksX = chunkCount(width)
    val numChunksY = chunkCount(height)
    val numChunksZ = chunkCount(depth)
    /** Array of existing chunks for fast iteration */
    val chunks = DelayedRemovalArray<Chunk>(200)
    /** map of chunk hashCodes to Chunks, mostly for fast checking of chunk existence in the world*/
    val chunkHashCodeMap = ObjectMap<Int, Chunk>(200)
    /** keeps track of which chunks are queued for creation */
    val chunkCreationQueue = ObjectSet<CubeDataGrid>()
    val worldData = WorldChunkData(width, height, depth, this)

    /**
     * Processes the Chunk creation queue.
     *
     * Get's the chunk with the minimal distance to the camera, create and adds it to the World.
     */
    private fun processCreationQueue() {
        synchronized(this) {
            if (chunkCreationQueue.size == 0) return
            val cdg = chunkCreationQueue.minBy { it.center.dst2(view.camera.position) }!!
            //val cdg = chunkCreationQueue.first()
            chunkCreationQueue.remove(cdg)
            val chunk = Chunk.obtain(cdg)
            addChunk(chunk)
        }
    }

    /**
     * Processes the Chunk deletion queue.
     *
     * Removes chunks that are out of view range from World and disposes it.
     */
    private fun removeChunksOutOfViewRange() {
        synchronized(this) {
            chunks.begin()
            for (i in 0 until chunks.size) {
                val chunk = chunks[i]
                val dist2 = chunk.dataGrid.center.dst2(view.camera.position)
                if (dist2 > View.maxViewDist * View.maxViewDist) {
                    val hash: Int = chunk.hashCode()
                    chunks.removeIndex(i)
                    chunkHashCodeMap.remove(hash)
                    chunk.dispose()
                    chunksRemoved++
                }
            }
            chunks.end()
        }
    }

    //var updatedOnce = false

    fun update(dt: Float) {
        initUpdater()
        // the bigger the backlog, the harder we work to catch up
        val chunksPerFrame = 1 + chunkCreationQueue.size / 10
        for (n in 1..chunksPerFrame) {
            processCreationQueue()
        }
        if (frame % 30 == 0L) {
            removeChunksOutOfViewRange()
        }
        for (chunk in chunks) {
            chunk.update()
        }
    }

    private fun initUpdater() {
        if (!updatesEnabled) return
        if (updater == null) {
            updater = WorldUpdater(this)
            val t = Thread(updater)
            t.name = "WorldUpdater"
            t.start()
        }
    }

    /** @see CubeDataGrid.origin */
    fun snapToChunkOrigin(value: Float): Float {
        //val centerOffset = Chunk.chunkSize / 2f
        val div = MathUtils.floor(value) / Chunk.chunkSize
        return (div * Chunk.chunkSize).toFloat()
    }

    /** @see CubeDataGrid.center */
    fun snapToChunkCenter(value: Float): Float {
        return snapToChunkOrigin(value) + Chunk.chunkSize / 2
    }

    /** for use in tests mostly */
    fun calculateHiddenFaces() {
        for (chunk in chunks) {
            chunk.dataGrid.calculateHiddenFaces(this)
        }
    }

    fun hasCubeAt(x: Float, y: Float, z: Float): Boolean {
        return if (!hasChunkAt(x, y, z)) {
            false
        } else {
            val chunk = getChunkAt(x, y, z)
            if (chunk.dataGrid.hasCubeAt(x, y, z)) {
                true
            } else {
                throw GdxRuntimeException("world.hasCubeAt($x, $y, $z), error: world has cube, but chunk doesn't!")
            }
        }
    }

    fun getCubeAt(x: Float, y: Float, z: Float):CubeData {
        val chunk = getChunkAt(x, y, z)
        return chunk.dataGrid.getCubeAt(x, y, z)
    }

    fun hasChunkAt(x: Float, y: Float, z: Float): Boolean {
        val sx = snapToChunkOrigin(x).toInt()
        val sy = snapToChunkOrigin(y).toInt()
        val sz = snapToChunkOrigin(z).toInt()
        return chunkHashCodeMap.containsKey(threeIntegerHashCode(sx, sy, sz))
/*        for (i in 0..chunks.size - 1) {
            val chunk = chunks[i]
            if (chunk.dataGrid.hasCubeAt(x, y, z)) {
                return true
            }
        }
        return false*/
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun addChunk(chunk: Chunk) {
        chunks.add(chunk)
        chunkHashCodeMap.put(chunk.hashCode(), chunk)
        chunksCreated++
    }

    fun getChunkAt(x: Float, y: Float, z: Float): Chunk {
        val sx = snapToChunkOrigin(x).toInt()
        val sy = snapToChunkOrigin(y).toInt()
        val sz = snapToChunkOrigin(z).toInt()
        return chunkHashCodeMap.get(threeIntegerHashCode(sx, sy, sz), null)!!
        /*for (i in 0..chunks.size - 1) {
            val chunk = chunks[i]
            if(chunk.dataGrid.hasCubeAt(x, y, z)) return chunk
        }*/
        //throw GdxRuntimeException("no chunk contains cube at: $x, $y, $z")
    }

    //val elevTimer = SimpleTimer("Elevations")

    // for finding elevation
    private val verticalChunks = Arr<Chunk>()

    fun getElevation(x: Float, z: Float): Int {
        //elevTimer.start()
        verticalChunks.clear()
        var y = world.height.toFloat()
        while(y >= 0f) {
            if (hasChunkAt(x, y, z)) {
                verticalChunks.add(getChunkAt(x, y, z))
            }
            y -= CubeDataGrid.height
        }
        var result = -1
        for (chunk in verticalChunks) {
            val elev = chunk.dataGrid.getElevation(x, z)
            if (elev > -1) {
                result = elev
                break
            }
        }
        //elevTimer.stop()
        return result
    }

    private val bboxElevations = IntArray(4)

    fun getBoundingBoxElevation(x: Float, z: Float, width: Float, depth: Float): Int {
        val elevs = bboxElevations
        elevs[0] = getElevation(x, z)
        elevs[1] = getElevation(x+width, z)
        elevs[2] = getElevation(x+width, z+depth)
        elevs[3] = getElevation(x, z+depth)
        return elevs.max()!!
    }

    /** should only be used by test package */
    fun createAllChunks() {
        chunks.clear()
        val chunkSize = Chunk.chunkSize
        var x: Float
        var y = 0f
        var z: Float
        var chunkCount = 0
        val origin = Vector3()
        while (y < height) {
            x = 0f
            while (x < width) {
                z = 0f
                while (z < depth) {
                    chunkCount++
                    /*var chunkWidth = Math.min(chunkSize, (width - x).toInt())
                    var chunkHeight = Math.min(chunkSize, (height - y).toInt())
                    var chunkDepth = Math.min(chunkSize, (depth - z).toInt())*/
                    //println("[$chunkCount]chunk divide w,h,d: $width, $height, $depth")
                    origin.set(x, y, z)
                    val cdg = CubeDataGrid.create(origin.x, origin.y, origin.z)
                    cdg.init(origin.x, origin.y, origin.z)
                    for (chunk in cdg) {
                        chunk.cubeType = CubeType.Grass
                    }
                    val chunk = Chunk.obtain(cdg)
                    addChunk(chunk)
                    z += chunkSize
                }
                x += chunkSize
            }
            y += chunkSize
        }
    }

    /**
     * Representing a single layer of SimplexNoise.
     */
    class NoiseLayer(val freq: Float, val weight: Float) {
        val elevations = Array(Chunk.chunkSize) { FloatArray(Chunk.chunkSize) }
        fun generateNoise(originX: Float, originZ: Float) {
            SimplexNoise.generateSimplexNoise(originX.toInt(), originZ.toInt(), CubeDataGrid.width, CubeDataGrid.depth, freq, elevations)
        }
    }

    /**
     * Manages NoiseLayers's.
     *
     * Sum's up and returns noise for coordinates x, z.
     */
    object NoiseLayerManager {
        val allLayers = Arr<NoiseLayer>()

        fun addLayer(freq: Float, weight: Float) {
            allLayers.add(NoiseLayer(freq, weight))
        }

        init {
            addLayer(0.003f, 1.0f)
            addLayer(0.01f, 0.2f)
            addLayer(0.03f, 0.1f)
        }

        fun generateAllNoise(cdg: CubeDataGrid) {
            for (layer in allLayers) {
                layer.generateNoise(cdg.origin.x, cdg.origin.z)
            }
        }

        fun getNoise(x: Int, z: Int): Float {
            var noise = 0f
            for (i in 0 until allLayers.size) {
                val layer = allLayers[i]
                noise += layer.elevations[x][z] * layer.weight
            }
            return noise
        }
    }

    /**
     * Apply's noise information to cubes.
     */
    fun applyWorldData(cdg: CubeDataGrid, wor: World) {
        //val start = TimeUtils.nanoTime()
        NoiseLayerManager.generateAllNoise(cdg)
        for (cube in cdg) {
            val x = (cube.xf - cdg.origin.x).toInt()
            val z = (cube.zf - cdg.origin.z).toInt()
            val elev = NoiseLayerManager.getNoise(x, z) * wor.height
            if (cube.yf > elev) {
                cube.cubeType = CubeType.Void
            } else if (cube.yf == elev) {
                cube.cubeType = CubeType.Grass
            } else {
                cube.cubeType = CubeType.Dirt
            }
        }
        //val elapsed = TimeUtils.nanoTime() - start
        //println("applyWorldData: ${TimeUtils.nanosToMillis(elapsed)}")
    }
    
    fun processUpdateFromWorldUpdater(upd:WorldUpdater) {
        for (cdg in upd.tempCreationQueue) {
            chunkCreationQueue.add(cdg)
        }
    }
    
}