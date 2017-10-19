package org.jrenner.learngl.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.jrenner.learngl.Main;
import org.jrenner.learngl.View;


public class DesktopLauncher {

	public static void main (String[] args) {
		try {
			for (String arg : args) {
				processArg(arg);
			}
		} catch (Exception e) {
			System.err.println("ERROR PROCESSING COMMAND LINE ARGUMENTS:\n");
			e.printStackTrace();
			System.exit(0);
		}
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.backgroundFPS = 60;
		config.foregroundFPS = 60;
		config.vSyncEnabled = true;
		//config.width = 1024;
		//config.height = 768;
		config.width = 1880;
		config.height = 1000;
		config.fullscreen = false;
		config.samples = 0;
		new LwjglApplication(new Main(), config);
	}

	private static void processArg(String arg) throws Exception {
		String[] pieces = arg.split("=");
		if (pieces[0].equals("-dist")) {
			int viewDist = Integer.parseInt(pieces[1]);
			int max = 250;
			if (viewDist > max) {
				System.out.println("VIEW DIST CANNOT BE OVER " + max);
				System.exit(0);
			} else {
				View.Companion.setMaxViewDist(viewDist);
				System.out.println("COMMAND LINE ARG VIEW DIST SET: " + viewDist);
			}

		}
	}
}
