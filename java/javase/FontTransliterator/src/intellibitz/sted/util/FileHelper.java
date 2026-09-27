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
 * $Id:FileHelper.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/util/FileHelper.java $
 */

/**
 * $Id:FileHelper.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/util/FileHelper.java $
 */

package intellibitz.sted.util;

import intellibitz.sted.io.FileFilterHelper;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.logging.Logger;

public class FileHelper {
    private static final Logger logger =
            Logger.getLogger("intellibitz.sted.util.FileHelper");

    private FileHelper() {
    }

    public static File openFont(Component parent) {
        return openFile("Please select Font location:", "ttf", "Fonts", parent);
    }

    public static File alertAndOpenFont(String alert, Component parent) {
        JOptionPane.showMessageDialog(parent, alert, "Missing Resource",
                JOptionPane.WARNING_MESSAGE);
        return openFont(parent);
    }

    public static File openFile(String title, String extension,
                                String description,
                                Component parent) {
        final JFileChooser jFileChooser =
                new JFileChooser(System.getProperty("user.dir"));
        jFileChooser.setDialogTitle(title);
        final FileFilterHelper fileFilterHelper =
                new FileFilterHelper(extension, description);
        jFileChooser.setFileFilter(fileFilterHelper);
        final int result = jFileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile();
        }
        return null;
    }

    public static String suffixFileSeparator(String path) {
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        return path;
    }

    public static void fileCopy(File source, String dest)
            throws IOException {
        final File newFile = new File(dest);
        final byte[] buffer = new byte[4096];
        final FileInputStream inputStream = new FileInputStream(source);
        final FileOutputStream outputStream = new FileOutputStream(newFile);
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.close();
    }

    /**
     * @param file the file to read
     * @return InputStream the input stread from the file
     * @throws java.io.FileNotFoundException if file cannot be read, not found
     */
    public static InputStream getInputStream(File file)
            throws FileNotFoundException {
        logger.entering(Resources.class.getName(), "getInputStream", file);
        InputStream inputStream;
        if (file.isAbsolute()) {
            logger.finest(
                    "file is absolute.. using ClassLoader.getSystemResourceAsStream " +
                            file.getAbsolutePath());
            inputStream = ClassLoader
                    .getSystemResourceAsStream(file.getAbsolutePath());
        } else {
            final String resource = file.getPath().replace('\\', '/');
            logger.finest(
                    "file is relative.. using Resources.class.getClass().getResourceAsStream with " +
                            resource);
            inputStream = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (inputStream == null) {
            inputStream = new FileInputStream(file);
        }
        logger.exiting(Resources.class.getName(), "getInputStream",
                inputStream);
        return inputStream;
    }

    public static String[] getSampleFontMapPaths(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.isDirectory()) {
            throw new IllegalArgumentException(dir + " Is not a directory");
        }
        String[] files = dirFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("xml");
            }
        });
        if (null != files && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].contains(dir)) {
                    files[i] = dir + files[i];
                }

            }

        }
        return files;
    }
}
