package org.jrenner.learngl.gameworld.blocks

/**
 * Created by NielsW
 * Last Update: 14/02/2019 at 14:13
 * Email: westphal.niels@gmail.com
 */
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable

open class Block(texture: Texture, private val type: Type) : Disposable {
    private val material: Material
    val model: Model
    val instance: ModelInstance

    val position: Vector3
        get() {
            val x = instance.transform.values[Matrix4.M03]
            val y = instance.transform.values[Matrix4.M13]
            val z = instance.transform.values[Matrix4.M23]
            return Vector3(x, y, z)
        }

    init {
        material = Material(TextureAttribute.createDiffuse(texture))
        modelBuilder.begin()
        modelBuilder.node()
        val mesh_part_builder = modelBuilder.part("box", GL20.GL_TRIANGLES, (VertexAttributes.Usage.Position
                or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong(), material)
        BoxShapeBuilder.build(mesh_part_builder, 5f, 5f, 5f)
        model = modelBuilder.end()

        instance = ModelInstance(model)
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        instance.transform = Matrix4().translate(x, y, z)
    }

    override fun dispose() {
        model.dispose()
    }

    enum class Type {
        DirtBlock,
        StoneBlock
    }

    companion object {
        private val modelBuilder = ModelBuilder()
    }
}
