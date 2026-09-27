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
 * $Id:GCButton.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/widgets/GCButton.java $
 */

package intellibitz.sted.widgets;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GCButton
        extends JButton
        implements ActionListener
{
    private final Runtime runtime;

    public GCButton(Icon icon, Icon rollOverIcon)
    {
        super();
        setIcon(icon);
        setRolloverIcon(rollOverIcon);
        setRolloverEnabled(true);
        setMargin(new Insets(0, 0, 0, 0));
        setSize(new Dimension(icon.getIconHeight(), icon.getIconWidth()));
        setToolTipText("Runs Garbage Collector");
        runtime = Runtime.getRuntime();
        // add the button as the self action listener
        // the button knows what to do anyways
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        runtime.runFinalization();
        runtime.gc();
    }

}
