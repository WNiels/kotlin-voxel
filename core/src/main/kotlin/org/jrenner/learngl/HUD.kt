package org.jrenner.learngl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import org.jrenner.RenStringBuilder
import org.jrenner.learngl.utils.plus

class HUD {
    val stage = Stage()
    val table = Table()
    val info: Label = Label("Info", skin)

    init {
        refresh()
    }

    fun refresh() {
        table.clear()
        table.setFillParent(true)
        table.align(Align.left or Align.top)
        fun label(act: Actor) = table.add(act).align(Align.left or Align.top).row()
        label(info)
        stage.addActor(table)
    }

    var accumulatedTime = 0f

    fun isReadyForUpdate(): Boolean {
        val interval = 0.1f
        if (accumulatedTime >= interval) {
            accumulatedTime -= interval
            return true
        }
        return false
    }

    val sb = RenStringBuilder()

    fun update(dt: Float) {
        if (isReadyForUpdate()) {
            sb.delete(0, sb.sbLength())
            sb + "FPS: " + Gdx.graphics.framesPerSecond.toString()
            sb + "\nMemory\n\tJava: ${Gdx.app.javaHeap / 1000000} MB\n\tNative: ${Gdx.app.nativeHeap / 1000000} MB"
            sb + "\nChunks: ${world.chunks.size}, Rendered: ${view.chunksRendered}"
            sb + "\nChunkQueue: ${world.chunkCreationQueue.size}"
            val c = view.camera.position
            sb + "\nChunks, created: ${world.chunksCreated}, removed: ${world.chunksRemoved}"
            val camElev = world.getElevation(c.x, c.z)
            sb + "\nCamera: %.1f, %.1f %.1f\n\tAltitude above ground: $camElev".format(c.x, c.y, c.z)
            val moveMode = if (view.walkingEnabled) "Walking" else "Flying"
            sb + "\nMovement mode: $moveMode"
            sb + "\nView Distance: ${View.maxViewDist}"
            sb + "\nWARNING: \nHigh view distances require\nexponentially large amounts of memory"
            sb + "\n\nCONTROLS:\n\tW, A, S, D to move\n\tClick and hold mouse to look around\n\t- and + to change view distance"
            sb + "\nSpace to switch flying/walking"
            info.setText(sb.toString())
        }
    }

    var enabled = true

    fun render(dt: Float) {
        accumulatedTime += dt
        if (enabled) {
            update(dt)
            stage.act(dt)
            stage.draw()
        }
    }
}