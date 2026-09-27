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
 * $Id:MappingTableRenderer.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/MappingTableRenderer.java $
 */

package intellibitz.sted.ui;

import intellibitz.sted.fontmap.FontMap;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

class MappingTableRenderer
        extends DefaultTableCellRenderer
{
    MappingTableRenderer()
    {
        super();
    }

    public void setFontMap(FontMap fontMap)
    {
        this.fontMap = fontMap;
    }

    public Component getTableCellRendererComponent
            (JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row,
                    int column)
    {
        final DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)
                super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
        if (column == 0)
        {
            renderer.setFont(fontMap.getFont1());
            renderer.setHorizontalAlignment(JLabel.RIGHT);
        }
        else if (column == 1)
        {
            renderer.setHorizontalAlignment(JLabel.CENTER);
            renderer.setFocusable(false);
        }
        else if (column == 2)
        {
            renderer.setFont(fontMap.getFont2());
            renderer.setHorizontalAlignment(JLabel.LEFT);
        }
        else if (column == 5 || column == 6)
        {
            renderer.setFont(fontMap.getFont1());
        }
        else
        {
            renderer.setHorizontalAlignment(JLabel.CENTER);
        }
        return renderer;
    }

    private FontMap fontMap;
}

