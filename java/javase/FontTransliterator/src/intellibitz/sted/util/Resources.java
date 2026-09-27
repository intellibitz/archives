/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * <p>
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 * <p>
 * Visit http://www.gnu.org/ for GPL License terms.
 * <p>
 * $Id:Resources.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/util/Resources.java $
 */

/**
 * $Id:Resources.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/util/Resources.java $
 */

package intellibitz.sted.util;

import intellibitz.sted.fontmap.FontInfo;
import intellibitz.sted.io.SettingsXMLHandler;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;

public class Resources {

    public static final String COLON = ":";
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String EQUALS = "=";
    public static final String SYMBOL_ASTERISK = "*";
    public static final String NEWLINE_DELIMITER = "\n";

    public static final String SYSTEM = "SYSTEM";
    public static final String RESOURCE = "RESOURCE";

    public static final String STED_CONFIG_NAME = "config.sted";
    public static final String STED_VERSION = "sted.version";
    public static final String LOG_CONFIG_NAME = "config.log";
    public static final String MENU_CONFIG_NAME = "config.menu";
    public static final String FONTMAP_FILE = "fontmap.file";

    public static final String LABEL_ADD = "label.add";
    public static final String LABEL_CLEAR = "label.clear";
    public static final String LABEL_FONT_LOAD = "label.font.load";

    public static final String ICON_FILE_INPUT = "icon.file.input";
    public static final String ICON_FILE_OUTPUT = "icon.file.output";
    public static final String ICON_FILE_NORMAL_STATE =
            "icon.file.normal.state";
    public static final String ICON_FILE_EDIT_STATE = "icon.file.edit.state";
    public static final String ICON_STED = "icon.sted";
    public static final String ICON_HELP = "icon.help";
    public static final String ICON_HELP_HOME = "icon.help.home";
    public static final String ICON_HELP_BACK = "icon.help.back";
    public static final String ICON_HELP_FORWARD = "icon.help.forward";
    public static final String ICON_LOCK = "icon.status.lock";
    public static final String ICON_UNLOCK = "icon.status.unlock";

    public static final String TITLE_ABOUT_STED = "title.about.sted";
    public static final String TITLE_KEYPAD = "title.keypad";
    public static final String TITLE_MAPPING = "title.mapping";
    public static final String TITLE_MAPPING_RULE = "title.mapping.rule";
    public static final String TITLE_MAPPING_PREVIEW = "title.mapping.preview";

    public static final String KEYPAD_COLUMN_COUNT = "keypad.column.count";
    public static final String FONT_CHAR_MAXINDEX = "font.char.maxindex";


    public static final String TITLE_TAB_INPUT = "title.tab.input";

    public static final String TITLE_TAB_OUTPUT = "title.tab.output";

    public static final String TITLE_TAB_FONTMAP = "title.tab.fontmap";
    public static final String TIP_TAB_FONTMAP = "tip.tab.fontmap";

    public static final String TITLE_TABLE_COLUMN_SYMBOL1 =
            "title.table.column.symbol1";
    public static final String TITLE_TABLE_COLUMN_SYMBOL2 =
            "title.table.column.symbol2";
    public static final String TITLE_TABLE_COLUMN_EQUALS =
            "title.table.column.equals";
    public static final String TITLE_TABLE_COLUMN_FIRST_LETTER =
            "title.table.column.letter1";
    public static final String TITLE_TABLE_COLUMN_LAST_LETTER =
            "title.table.column.letter2";
    public static final String TITLE_TABLE_COLUMN_FOLLOWED_BY =
            "title.table.column.followed";
    public static final String TITLE_TABLE_COLUMN_PRECEDED_BY =
            "title.table.column.preceded";

    public static final String TITLE_HELP = "title.help";
    public static final String HELP_INDEX = "help.index";
    public static final String SETTINGS_STED_UI = "settings.sted.ui";
    public static final String SETTINGS_STED_USER = "settings.sted.user";
    public static final String SETTINGS_PATH = "sted.settings.path";
    public static final String STED_HOME_PATH = "sted.home.path";
    public static final String RESOURCE_PATH = "sted.resource.path";
    public static final String LOG_PATH = "sted.log.path";

    public static final String SAMPLE_INPUT_TEXT = "sample.input.text";

    public static final String ACTION_CONVERT_NAME = "Convert";
    public static final String ACTION_STOP_NAME = "Stop";
    public static final String ACTION_PRESERVE_TAGS = "Preserve <Tags>";
    public static final String ACTION_TRANSLITERATE_REVERSE =
            "Reverse Transliterate";
    public static final String ACTION_FILE_RELOAD = "ReLoad from Disk";
    public static final String ACTION_VIEW_LAF = "Look & Feel";

    public static final String ACTION_FILE_NEW_COMMAND = "New";
    public static final String ACTION_FILE_RELOAD_COMMAND = "ReLoad";
    public static final String ACTION_FILE_REOPEN_COMMAND = "ReOpen";
    public static final String ACTION_FILE_SAVE_COMMAND = "Save";
    public static final String ACTION_FILE_SAVEAS_COMMAND = "Save As...";
    public static final String ACTION_FILE_CLOSE_COMMAND = "Close";

    public static final String ACTION_SELECT_INPUT_FILE_COMMAND = "Input";
    public static final String ACTION_SELECT_OUTPUT_FILE_COMMAND = "Output";
    public static final String ACTION_DELETE_COMMAND = "Delete";
    public static final String ACTION_CUT_COMMAND = "Cut";
    public static final String ACTION_COPY_COMMAND = "Copy";
    public static final String ACTION_UNDO_COMMAND = "Undo";
    public static final String ACTION_REDO_COMMAND = "Redo";
    public static final String ACTION_PASTE_COMMAND = "Paste";
    public static final String ACTION_SELECT_ALL_COMMAND = "Select All";

    public static final String ACTION_VIEW_TOOLBAR_COMMAND = "ViewTool";
    public static final String ACTION_VIEW_STATUS_COMMAND = "ViewStatus";
    public static final String ACTION_VIEW_SAMPLE_COMMAND = "ViewSample";
    public static final String ACTION_VIEW_SAMPLE = "Mapping Preview";
    public static final String ACTION_VIEW_MAPPING_COMMAND = "ViewMapping";
    public static final String ACTION_VIEW_MAPPING = "Mapping Rules";

    public static final String MENU_POPUP_MAPPING = "Mapping";

    public static final String FOLLOWED_BY = "Followed By: ";
    public static final String PRECEDED_BY = "Preceded By: ";
    public static final String TITLE = "title";
    public static final String XML = "xml";
    public static final String ENTRIES = "entries";

    public static final String ENTRY_CONDITIONAL_AND = "AND";
    public static final String ENTRY_CONDITIONAL_OR = "OR";
    public static final String ENTRY_CONDITIONAL_NOT = "NOT";
    public static final String ENTRY_TOSTRING_DELIMITER = ":";

    public static final String HTML_TAG_START = "<";
    public static final String HTML_TAG_START_ESCAPE = "&";
    public static final String HTML_TAG_END = ">";
    public static final String HTML_TAG_END_ESCAPE = ";";

    public static final String RULE_TITLE = " If <> is: ";

    public static final int ENTRY_STATUS_ADD = 1;
    public static final int ENTRY_STATUS_EDIT = 2;
    public static final int ENTRY_STATUS_DELETE = 3;

    // @since version 0.61
    public static final String MENUBAR_STED = "STED-MenuBar";
    public static final String MENUBAR_FONTMAP = "FontMap-MenuBar";

    // @since version 0.62
    public static final String INPUT_FILE = "input.file";
    public static final String OUTPUT_FILE = "output.file";
    public static final String ICON_GC = "icon.gc";
    public static final String ICON_GC2 = "icon.gc.rollover";
    public static final int DEFAULT_MENU_COUNT = 2;
    public static final String SAMPLE_FONTMAP = "sample.fontmap";
    public static final String MENU_SAMPLES_NAME = "Samples";


    private static ResourceBundle resourceBundle;
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.util.Resources");
    private static final Map<String, String> settings =
            new HashMap<String, String>();
    public static final String SETTINGS_FILE_PATH_USER;
    public static final Map<String, ImageIcon> imageIcons =
            new HashMap<String, ImageIcon>();
    private static int id;
    static final Map<String, FontInfo> fonts =
            new HashMap<String, FontInfo>();
    private static String RESOURCE_PATH_VAL;


    static {
        resourceBundle = ResourceBundle
                .getBundle(Resources.STED_CONFIG_NAME, Locale.getDefault());
        logger.finest("retrieved resource bundle " + resourceBundle);
        RESOURCE_PATH_VAL =
                System.getProperty(Resources.RESOURCE_PATH, "./resource/");
        String settingsPath =
                System.getProperty(Resources.SETTINGS_PATH, "./settings/");
        settingsPath = FileHelper.suffixFileSeparator(settingsPath);
        String SETTINGS_FILE_PATH_STED = settingsPath
                + getResource(
                Resources.SETTINGS_STED_UI);
        SETTINGS_FILE_PATH_USER = settingsPath
                + getResource(Resources.SETTINGS_STED_USER);
        try {
            readSettings(SETTINGS_FILE_PATH_STED);
        } catch (IOException e) {
            logger.throwing("Resources", "static initializer block", e);
            logger.severe(
                    "Unable to read settings - IOException " + e.getMessage());
        } catch (SAXException e) {
            logger.throwing("Resources", "static initializer block", e);
            logger.severe(
                    "Unable to read settings - SAXException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.throwing("Resources", "static initializer block", e);
            logger.severe(
                    "Unable to read settings - ParserConfigurationException " +
                            e.getMessage());
        }
        try {
            readSettings(SETTINGS_FILE_PATH_USER);
        } catch (IOException e) {
            logger.throwing("Resources", "static initializer block", e);
            logger.severe(
                    "Unable to read settings - IOException " + e.getMessage());
            logger.info(
                    "NOTE: Safely ignore the user.xml not found error if starting STED for the first time");
        } catch (SAXException e) {
            logger.throwing("Resources", "static initializer block", e);
            logger.severe(
                    "Unable to read settings - SAXException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            logger.throwing("Resources", "static initializer block", e);
            logger.severe(
                    "Unable to read settings - ParserConfigurationException " +
                            e.getMessage());
        }
        Resources.getImageIcons()
                .put(Resources.ICON_STED, Resources.getSystemResourceIcon
                        (Resources.getResource(Resources.ICON_STED)));

        // load all system fonts
        loadFonts(getLocalGraphicsEnvironment().getAllFonts());
    }

    private Resources() {
    }

    protected void finalize()
            throws Throwable {
        resourceBundle = null;
        super.finalize();
    }

    public static String getSTEDTitle() {
        return getResource(TITLE);
    }

    public static Image getSTEDImage() {
        return getSTEDIcon().getImage();
    }

    public static ImageIcon getSTEDIcon() {
        return
                getSystemResourceIcon(getSetting(ICON_STED));
    }

    public static ImageIcon getCleanIcon() {
        return getSystemResourceIcon(
                getResource(ICON_FILE_NORMAL_STATE));
    }

    public static ImageIcon getDirtyIcon() {
        return getSystemResourceIcon(
                getResource(ICON_FILE_EDIT_STATE));
    }

    public static ImageIcon getLockIcon() {
        return
                getSystemResourceIcon(getResource(ICON_LOCK));
    }

    public static ImageIcon getUnLockIcon() {
        return
                getSystemResourceIcon(getResource(ICON_UNLOCK));
    }

    public static String getVersion() {
        return getResource(STED_VERSION);
    }

    public static String getResource(String name) {
        String val = null;
        try {
            val = resourceBundle.getString(name);
        } catch (MissingResourceException e) {
            // ignore, since we will try alternates
        }
        // if not from resource bundle.. try system property.. or the settings file
        if (val == null) {
            val = getPropertySettings(name);
        }
        if (val == null) {
            logger.severe("Resource not found for: " + name);
        }
        return val;
    }

    private static String getPropertySettings(String name) {
        // try the system property - might come from command line
        // system property will override the options in the settings file
        String val = System.getProperty(name);
        if (val == null) {
            // read from the settings file
            val = getSetting(name);
        }
        return val;
    }

    public static String getSetting(String name) {
        return settings.get(name);
    }

    public static ArrayList<String> getSettingBeginsWith(String prefix) {
        final Iterator<String> keys = settings.keySet().iterator();
        final ArrayList<String> result = new ArrayList<String>();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (key.startsWith(prefix)) {
                final String val = settings.get(key);
                result.add(val);
            }
        }
        return result;
    }


    private static void readSettings(String path)
            throws IOException, SAXException, ParserConfigurationException {
        final SettingsXMLHandler settingsXMLHandler =
                new SettingsXMLHandler(new File(path));
        settingsXMLHandler.read();
        settings.putAll(settingsXMLHandler.getSettings());
    }

    public static ImageIcon getSystemResourceIcon(String iconName) {
        if (iconName != null) {
            try {
                ImageIcon imageIcon = imageIcons.get(iconName);
                if (null != imageIcon) {
                    return imageIcon;
                }
                // try with the filename
                imageIcon = new ImageIcon(iconName);
                if (null != imageIcon) {
                    imageIcons.put(iconName, imageIcon);
                    return imageIcon;
                }
                final URL url = ClassLoader.getSystemResource(iconName);
                if (url != null) {
                    imageIcon = new ImageIcon(url);
                    imageIcons.put(iconName, imageIcon);
                }
                return imageIcon;
            } catch (NullPointerException e) {
                logger.warning("No icon found or can be loaded for " + iconName
                        + " " + e.getMessage() +
                        e.getStackTrace().toString());
            }
        }
        logger.warning(
                "Cannot find Resource. Returning null as icon for " + iconName);
        return null;
    }

    public static Map<String, ImageIcon> getImageIcons() {
        return imageIcons;
    }

    public static String getSampleFontMap() {
        return prefixResourcePath(getResource(SAMPLE_FONTMAP));
    }

    public static String prefixResourcePath(String path) {
        return getResourceDirPath() +
                path;

    }

    public static String getResourceDirPath() {
        return FileHelper.suffixFileSeparator(RESOURCE_PATH_VAL);
    }


    public static int getId() {
        return ++id;
    }

    static void loadFonts(Font[] allFonts) {
        for (final Font font : allFonts) {
            Font f = font.deriveFont(Font.PLAIN, 14);
            fonts.put(font.getName(), new FontInfo(f, SYSTEM));
        }
    }

    public static Map<String, FontInfo> getFonts() {
        return fonts;
    }

    public static FontInfo getFont(String fontName) {
        return fonts.get(fontName);
    }
}
