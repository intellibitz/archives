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
 * $Id:FileSelectAction.kt 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/actions/FileSelectAction.kt $
 */

package intellibitz.sted.actions


import intellibitz.sted.launch.STEDGUI
import intellibitz.sted.ui.STEDWindow
import intellibitz.sted.util.Resources
import javax.swing.JFileChooser
import java.awt.event.ActionEvent
import java.io.File

class FileSelectAction : TableModelListenerAction() {
    constructor() : super()
    override fun actionPerformed(e: ActionEvent) {
            val cmd: String = e.getActionCommand()
            var isInput: Boolean = true
            if (Resources.ACTION_SELECT_INPUT_FILE_COMMAND.equalsIgnoreCase(cmd))
            {
                isInput = true
            }
            else
            if (Resources.ACTION_SELECT_OUTPUT_FILE_COMMAND.equalsIgnoreCase(cmd))
            {
                isInput = false
            }
            var file: FileNone
            val stedWindow: STEDWindow = getSTEDWindow()
            if (isInput)
            {
                file = stedWindow.getDesktop()
                        .getDesktopModel()
                        .getInputFile()
            }
            else
            {
                file = stedWindow.getDesktop()
                        .getDesktopModel()
                        .getOutputFile()
            }
            val jFileChooser: JFileChooserNone
            val result: IntNone
            if (file == null)
            {
                jFileChooser = JFileChooser(System.getProperty("user.dir"))
                result = jFileChooser.showOpenDialog(stedWindow)
            }
            else
            {
                jFileChooser = JFileChooser(file.getParent())
                result = jFileChooser.showSaveDialog(stedWindow)
            }
            if (result == JFileChooser.APPROVE_OPTION)
            {
                file = jFileChooser.getSelectedFile()
                if (file != null)
                {
                    STEDGUI.busy()
                    if (isInput)
                    {
                        stedWindow.getDesktop()
                                .getFontMapperDesktopFrame().setInputFile(file)
                    }
                    else
                    {
                        stedWindow.getDesktop()
                                .getFontMapperDesktopFrame().setOutputFile(file)
                    }
                    stedWindow.getDesktop()
                            .getFontMapperDesktopFrame()
                            .enableConverterIfFilesLoaded()
                    STEDGUI.relax()
                }
            }
        
        }
}
