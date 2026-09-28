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
 * $Id:DocumentListenerTextField.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/ui/DocumentListenerTextField.kt $
 */

package intellibitz.sted.ui


import intellibitz.sted.fontmap.FontMap
import intellibitz.sted.fontmap.SampleTextConverter
import intellibitz.sted.util.MenuHandler
import intellibitz.sted.util.Resources
import intellibitz.sted.widgets.FontChangeTextField
import javax.swing.JCheckBoxMenuItem
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class DocumentListenerTextField : FontChangeTextField(), DocumentListener {
    private var converter: SampleTextConverter? = null
    private var fontMap: FontMap? = null
    private var mapperPanel: MapperPanel? = null
    //    private STEDWindow stedWindow;
    /*
        public DocumentListenerTextField(MapperPanel mapperPanel,
                STEDWindow stedWindow)
        {
            this();
            this.mapperPanel = mapperPanel;
            this.stedWindow = stedWindow;
        }
    */
    //    private STEDWindow stedWindow;
    constructor() : super()
    fun load() {
    
        
        }
    override fun insertUpdate(e: DocumentEvent) {
            convertSampleText(e)
        }
    override fun removeUpdate(e: DocumentEvent) {
            convertSampleText(e)
        }
    override fun changedUpdate(e: DocumentEvent) {
            convertSampleText(e)
        }
    private fun convertSampleText(e: DocumentEvent) {
            if (e.getDocument().getLength() > 0)
            {
                if (converter == null)
                {
                    converter = SampleTextConverter(mapperPanel)
                }
                converter.setFontMap(fontMap)
                val menuHandler: MenuHandler = MenuHandler.getInstance()
                val preserve: JCheckBoxMenuItem =
                        (menuHandler as JCheckBoxMenuItem).getMenuItem
                                (converter as Resources.ACTION_PRESERVE_TAGS).setHTMLAware(preserve.isSelected())
                val reverse: JCheckBoxMenuItem =
                        (menuHandler as JCheckBoxMenuItem).getMenuItem
                                (converter as Resources.ACTION_TRANSLITERATE_REVERSE).setReverseTransliterate(reverse.isSelected())
                SwingUtilities.invokeLater(converter)
            }
        
        }
    fun setFontMap(fontMap: FontMap) {
            this.fontMap = fontMap
        }
    fun setFontMapperPanel(mapperPanel: MapperPanel) {
            this.mapperPanel = mapperPanel
        }
}
