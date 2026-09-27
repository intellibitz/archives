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
 * $Id:FileFilterHelper.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FileFilterHelper.java $
 */

/**
 * $Id:FileFilterHelper.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/io/FileFilterHelper.java $
 */

package intellibitz.sted.io;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about.
 * <p/>
 * Extensions are of the type ".foo", which is typically found on Windows and
 * Unix boxes, but not on Macinthosh. Case is ignored.
 * <p/>
 * Example - create a new filter that filerts out all files but gif and jpg
 * image files:
 * <p/>
 * JFileChooser chooser = new JFileChooser(); SideFileFilter filter = new
 * SideFileFilter( new String{"gif", "jpg"}, "JPEG & GIF Images")
 * chooser.addChoosableFileFilter(filter); chooser.showOpenDialog(this);
 */
public class FileFilterHelper
        extends FileFilter {

//    private static String TYPE_UNKNOWN = "Type Unknown";
//    private static String HIDDEN_FILE = "Hidden File";

    private Map<String, FileFilterHelper> filters;
    private String description;
    private String fullDescription;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all files are
     * accepted.
     *
     * @see #addExtension
     */
    public FileFilterHelper() {
        filters = new HashMap<String, FileFilterHelper>();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new FileFilterHelper("jpg");
     *
     * @see #addExtension
     */
    public FileFilterHelper(String extension) {
        this(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type. Example: new
     * FileFilterHelper("jpg", "JPEG Image Images");
     * <p/>
     * Note that the "." before the extension is not needed. If provided, it
     * will be ignored.
     *
     * @see #addExtension
     */
    public FileFilterHelper(String extension, String description) {
        this();
        if (extension != null) {
            addExtension(extension);
        }
        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Creates a file filter from the given string array. Example: new
     * FileFilterHelper(String {"gif", "jpg"});
     * <p/>
     * Note that the "." before the extension is not needed adn will be
     * ignored.
     *
     * @see #addExtension
     */
    public FileFilterHelper(String[] filters) {
        this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new FileFilterHelper(String {"gif", "jpg"}, "Gif and JPG
     * Images");
     * <p/>
     * Note that the "." before the extension is not needed and will be
     * ignored.
     *
     * @see #addExtension
     */
    public FileFilterHelper(String[] filters, String description) {
        this();
        for (final String newVar : filters) {
            // add filters one by one
            addExtension(newVar);
        }
        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Return true if this file should be shown in the directory pane, false if
     * it shouldn't.
     * <p/>
     * Files that begin with "." are ignored.
     *
     * @see #getExtension
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            final String extension = getExtension(f);
            if (extension != null && filters.get(getExtension(f)) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @see #getExtension
     * @see FileFilter#accept
     */
    public static String getExtension(File f) {
        if (f != null) {
            final String filename = f.getName();
            final int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
                return filename.substring(i + 1).toLowerCase();
            }
        }
        return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     * <p/>
     * For example: the following code will create a filter that filters out all
     * files except those that end in ".jpg" and ".tif":
     * <p/>
     * FileFilterHelper filter = new FileFilterHelper();
     * filter.addExtension("jpg"); filter.addExtension("tif");
     * <p/>
     * Note that the "." before the extension is not needed and will be
     * ignored.
     */
    public void addExtension(String extension) {
        if (filters == null) {
            filters = new Hashtable<String, FileFilterHelper>(5);
        }
        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }


    /**
     * Returns the human readable description of this filter. For example: "JPEG
     * and GIF Image Files (*.jpg, *.png)"
     *
     * @see FileFilter#getDescription
     */
    public String getDescription() {
        if (fullDescription == null) {
            if (description == null || isExtensionListInDescription()) {
                fullDescription =
                        description == null ? "(" : description + " (";
                // build the description from the extension list
                final Iterator<String> extensions = filters.keySet().iterator();
                if (extensions != null) {
                    fullDescription += "." + extensions.next();
                    while (extensions.hasNext()) {
                        fullDescription += ", " + extensions.next();
                    }
                }
                fullDescription += ")";
            } else {
                fullDescription = description;
            }
        }
        return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For example:
     * filter.setDescription("Gif and JPG Images");
     */
    public void setDescription(String description) {
        this.description = description;
        fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .png, etc) should show up in
     * the human readable description.
     * <p/>
     * Only relevent if a description was provided in the constructor or using
     * setDescription();
     */
    public void setExtensionListInDescription(boolean b) {
        useExtensionsInDescription = b;
        fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .png, etc) should show up in
     * the human readable description.
     * <p/>
     * Only relevent if a description was provided in the constructor or using
     * setDescription();
     */
    public boolean isExtensionListInDescription() {
        return useExtensionsInDescription;
    }
}
