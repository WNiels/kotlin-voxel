package org.jrenner.learngl.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import org.jrenner.learngl.Main
import org.jrenner.learngl.View


object DesktopLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            for (arg in args) {
                processArg(arg)
            }
        } catch (e: Exception) {
            System.err.println("ERROR PROCESSING COMMAND LINE ARGUMENTS:\n")
            e.printStackTrace()
            System.exit(0)
        }

        val config = LwjglApplicationConfiguration()
        config.backgroundFPS = 60
        config.foregroundFPS = 60
        config.vSyncEnabled = true
        //config.width = 1024;
        //config.height = 768;
        config.width = 1880
        config.height = 1000
        config.fullscreen = false
        config.samples = 0
        LwjglApplication(Main(), config)
    }

    @Throws(Exception::class)
    private fun processArg(arg: String) {
        val pieces = arg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (pieces[0] == "-dist") {
            val viewDist = Integer.parseInt(pieces[1])
            val max = 250
            if (viewDist > max) {
                println("VIEW DIST CANNOT BE OVER $max")
                System.exit(0)
            } else {
                View.maxViewDist = viewDist.toFloat()
                println("COMMAND LINE ARG VIEW DIST SET: $viewDist")
            }

        }
    }
}
