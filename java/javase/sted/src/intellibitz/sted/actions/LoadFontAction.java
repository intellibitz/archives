/**
 * Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 * 168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 * http://www.intellibitz.com
 * training@intellibitz.com
 * +91 44 2247 5106
 * http://groups.google.com/group/etoe
 * http://sted.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * STED, Copyright (C) 2007 IntelliBitz Technologies
 * STED comes with ABSOLUTELY NO WARRANTY;
 * This is free software, and you are welcome
 * to redistribute it under the GNU GPL conditions;
 *
 * Visit http://www.gnu.org/ for GPL License terms.
 */

/**
 * $Id:LoadFontAction.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/LoadFontAction.java $
 */

package intellibitz.sted.actions;

import intellibitz.sted.ui.FontKeypad;
import intellibitz.sted.util.FileHelper;
import intellibitz.sted.util.Resources;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.File;

public class LoadFontAction
        extends STEDWindowAction
{
    private FontKeypad fontKeypad;

    public LoadFontAction(FontKeypad fontSelectPanel)
    {
        super();
        putValue(Action.NAME, Resources.getSetting(Resources.LABEL_FONT_LOAD));
        fontKeypad = fontSelectPanel;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        loadFont();
    }

    private void loadFont()
    {
        final File file = FileHelper.openFont(getSTEDWindow());
        if (file != null)
        {
            fontKeypad.loadFont(file);
        }
    }


}

