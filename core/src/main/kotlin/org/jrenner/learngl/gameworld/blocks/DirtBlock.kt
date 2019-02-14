package org.jrenner.learngl.gameworld.blocks

/**
 * Created by NielsW
 * Last Update: 14/02/2019 at 14:15
 * Email: westphal.niels@gmail.com
 */
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture

class DirtBlock : Block(Texture(Gdx.files.internal("texture/Dirt.PNG")), Block.Type.DirtBlock)

