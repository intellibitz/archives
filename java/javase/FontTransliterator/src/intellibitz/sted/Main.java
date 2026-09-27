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
 * $Id:Main.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/Main.java $
 */

/**
 * $Id:Main.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/Main.java $
 */

package intellibitz.sted;

import intellibitz.sted.launch.STEDConsole;
import intellibitz.sted.launch.STEDGUI;
import intellibitz.sted.launch.STEDLogManager;

import java.util.logging.Logger;

/**
 * Main<br> Invoke STED Console or the GUI based on the arguments
 */
public class Main {
    private static Logger logger;


    private Main() {
    }

    /**
     * Entry point
     *
     * @param args
     */
    public static void main(String[] args) {
        logger = Logger.getLogger("intellibitz.sted.Main");
        STEDLogManager.getLogmanager().addLogger(logger);
        if (args != null && args.length > 0) {
            final String param1 = args[0];
            // launch Console
            if (param1.toLowerCase().startsWith("-c")) {
                logger.info("Launching STED Console: ");
                final int len = args.length;
                final String[] args1 = new String[len - 1];
                System.arraycopy(args, 1, args1, 0, len - 1);
                STEDConsole.main(args1);
            }
        } else {
            logger.info("Launching STED GUI: ");
            // launch GUI
            STEDGUI.main(args);
        }
    }

}
