package org.jrenner.learngl

import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.Pool
import org.jrenner.RenStringBuilder
import com.badlogic.gdx.utils.Array as Arr

class DebugPool<T>(val name: String, val newObjFunc: () -> T, initialCapacity: Int = 16) : Pool<T>(initialCapacity) {
    companion object {
        val list = Arr<DebugPool<*>>()
        val sb : RenStringBuilder by lazy {
            RenStringBuilder()
        }
        fun allDebugInfo(): String {
            sb.delete(0, sb.sbLength())
            for (pool in list) {
                sb.append(pool.debugInfo()).append("\n")
            }
            return sb.toString()
        }

    }
    init {
        list.add(this)
    }
    var objectsCreated = 0
        private set

    var objectsFreed = 0

    var objectsObtained = 0

    override fun obtain(): T {
        objectsObtained++
        synchronized(this) {
            return super.obtain()
        }
    }

    override fun newObject(): T {
        objectsCreated++
        return newObjFunc()
    }

    override fun free(obj: T) {
        synchronized(this) {
            super.free(obj)
        }
        objectsFreed++
    }

    override fun freeAll(objects: Arr<T>?) {
        throw GdxRuntimeException("DebugPool does not support freeAll method")
    }

    fun debugInfo(): String {
        return "[DebugPool: $name] created: $objectsCreated, obtained: $objectsObtained, freed: $objectsFreed, currently free: ${free}"
    }
}

