package com.github.eclipsecolortheme.mapper;

import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Maps color themes to Eclipse preferences.
 * XXX: Subject to refactoring/rewrite, mappings require way too much code.
 */
public abstract class ThemePreferenceMapper {
    /** The associated Eclipse preferences. */
    protected IEclipsePreferences preferences;

    /**
     * Creates a new mapper.
     * @param plugin The ID of the Eclipse plugin whose preferences should be
     *               altered.
     */
    public ThemePreferenceMapper(String plugin) {
        preferences = new InstanceScope().getNode(plugin);
    }

    /**
     * Writes and applies the modified preferences.
     * @throws BackingStoreException
     */
    public void flush() throws BackingStoreException {
        preferences.flush();
    }

    /**
     * Maps the @a theme to the associated Eclipse preferences.
     * @param theme The color theme to map.
     */
    public abstract void map(Map<String, String> theme);

    /**
     * Clears the associated Eclipse preferences.
     * This resets every preference to its default value.
     */
    public abstract void clear();

    /**
     * Creates a preference entry for this editor.
     * @param color The color to set.
     * @return A preference entry.
     */
    protected String entry(String color) {
        return (color == null) ? null : hexToRGB(color);
    }

    /**
     * Converts a hexadecimal color value to an RGB string.
     * @param hexColor Hexadecimal colour value starting with a hash, as common
     *                 in CSS.
     * @return Comma-separated list of red, green and blue decimal values.
     */
    protected String hexToRGB(String hexColor) {
        String hexR = hexColor.substring(1, 3);
        String hexG = hexColor.substring(3, 5);
        String hexB = hexColor.substring(5, 7);
        return hexToDec(hexR) + ", " + hexToDec(hexG) + ", " + hexToDec(hexB);
    }

    private String hexToDec(String hex) {
        return String.valueOf(Integer.parseInt(hex, 16));
    }
}