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
 * $Id: FontChangeTextField.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/widgets/FontChangeTextField.java $
 */

package intellibitz.sted.widgets;

import intellibitz.sted.event.IKeypadListener;
import intellibitz.sted.event.KeypadEvent;
import intellibitz.sted.ui.FontKeypad;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class FontChangeTextField
        extends JTextField
        implements ItemListener,
        ActionListener,
        TableModelListener,
        IKeypadListener
{
    public FontChangeTextField()
    {
    }

    public void itemStateChanged(ItemEvent e)
    {
        Font f = new Font(e.getItem().toString(), Font.PLAIN, 14);
        setFont(f);
    }

    public void actionPerformed(ActionEvent e)
    {
        setText(getText() + ((JButton) e.getSource()).getText());
    }

    /**
     * This fine grain notification tells listeners the exact range of cells,
     * rows, or columns that changed.
     */
    public void tableChanged(TableModelEvent e)
    {
        requestFocus();
    }

    public void keypadReset(KeypadEvent event)
    {
        FontKeypad fontKeypad = (FontKeypad) event.getEventSource();
        List<JButton> keys = fontKeypad.getKeys();
        final int sz = keys.size();
        for (int i = 0; i < sz; i++)
        {
            (keys.get(i)).addActionListener(this);
        }
    }
}
