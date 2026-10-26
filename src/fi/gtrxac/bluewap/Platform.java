package fi.gtrxac.bluewap;

import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import java.io.*;
import java.util.*;

public class Platform extends Canvas {
    // Dummy canvas for querying system graphics info
	public Platform() {
		super();
	}

	protected void paint(Graphics g) {}

	public static boolean checkClass(String s) {
		try {
			Class.forName(s);
			return true;
		}
		catch (Throwable e) {}

		return false;
	}

    public static final String plat;

	public static final boolean isTouch;
	public static final boolean isJ2MELoader;
	public static final int vectorRenderScale;
	public static final boolean useUnderlinedFont;
	public static final boolean hasRoundRectSizeBug;
	public static final boolean preferModernConnector;
	public static final boolean allowSecondaryConnector;

	static {
		String p = System.getProperty("microedition.platform");
		if (p == null) p = "";
		plat = p;

		// On the Samsung S7220 and S7350, hasPointerEvents() returns true
		// even though they are not touchscreen phones
		isTouch = (new Platform()).hasPointerEvents() && 
			plat.indexOf("S7220") == -1 && plat.indexOf("S7350") == -1;

		isJ2MELoader = "The Android Project".equals(System.getProperty("java.vendor"));

		// Rendering scale used for dynamically generated vector graphics for some UI elements
		// Larger numbers will look nicer, but above 5 there isn't much of a noticeable difference
		// Nokia 6600 will freeze when rendering onto too large images - to stay safe we use 1
		// Other devices (even e.g. Nokia 3230, 6230) have been fine with 5
		vectorRenderScale = plat.startsWith("Nokia6600/") ? 1 : 5;

		boolean isNokia = plat.startsWith("Nokia");
		boolean isSonyEricsson = plat.startsWith("SonyEr");

		// Underlined fonts do not render correctly on Samsung touchscreen phones
		// (either all onscreen fonts show as underlined or none do)
		// and they do not look different from the standard font on Motorola
		// They seem to be safe to use on all Nokia, Sony Ericsson, BlackBerry phones
		// Other devices need more testing
		useUnderlinedFont = isNokia || isSonyEricsson || plat.startsWith("BlackB");

		// On some Motorola phones (seemingly those with only one font size, like the V3 and L6)
		// drawRoundRect() will draw with a width and height one pixel larger than specified
		Image img = Image.createImage(5, 5);
		Graphics g = img.getGraphics();
		g.setColor(0x000000);
		g.drawRoundRect(0, 0, 3, 3, 2, 2);
		int[] pixel = new int[1];
		img.getRGB(pixel, 0, 5, 4, 1, 1, 1);
		hasRoundRectSizeBug = ((pixel[0] & 0x00FFFFFF) == 0x000000);

		// Which type of Connector(s) to prefer/use for TLS on different platforms:

		// Symbian: always use standard Connector, as you're meant to use TLS 1.2 patch
		if (isSymbian()) {
			preferModernConnector = false;
			allowSecondaryConnector = false;
		}

		// Nokia S40 and SE: always use ModernConnector, as standard Connector will never have TLS 1.2.
		// We'll also do this for Android even though it's not always optimal
		else if (isNokia || isSonyEricsson || isJ2MELoader) {
			preferModernConnector = true;
			allowSecondaryConnector = false;
		}

		// Other: prefer ModernConnector but also try standard Connector
		else {
			preferModernConnector = true;
			allowSecondaryConnector = true;
		}
	}

	// Samsung platform strings are inconsistent but one fairly effective way to
	// check for them is to look for a firmware version number
	// (platform string ends with 4 uppercase letters and 1 digit).
	// Not a perfect check (sometimes plat string is just "j2me") but it's
	// good enough for the touchscreen-era models
	public static boolean isSamsung() {
		if (plat.indexOf("Samsung") != -1 || plat.indexOf("GT-") != -1) return true;
		if (plat.length() < 5) return false;

		for (int i = 2; i < 6; i++) {
			char c = plat.charAt(plat.length() - i);
			if (!Character.isUpperCase(c)) return false;
		}

		char last = plat.charAt(plat.length() - 1);
		return Character.isDigit(last);
	}

    public static boolean isSymbian() {
        return plat.indexOf("platform=S60") != -1 ||
			System.getProperty("com.symbian.midp.serversocket.support") != null ||
			System.getProperty("com.symbian.default.to.suite.icon") != null ||
			checkClass("com.symbian.midp.io.protocol.http.Protocol") ||
			checkClass("com.symbian.lcdjava.io.File");
    }
}