package org.jrenner.learngl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import org.jrenner.smartfont.SmartFontGenerator
import kotlin.properties.Delegates

class Fonts {

    var normal: BitmapFont by Delegates.notNull()

    init {
        val gen = SmartFontGenerator()
        val fileHandle = Gdx.files.local("fonts/Exo-Regular.otf")
        val size = 18
        val fontName = "exo$size"
        println("$fontName")
        val chars = FreeTypeFontGenerator.DEFAULT_CHARS
        normal = gen.createFont(fileHandle, fontName, size, chars)
    }

    fun dispose() {
        normal.dispose()
    }

}
