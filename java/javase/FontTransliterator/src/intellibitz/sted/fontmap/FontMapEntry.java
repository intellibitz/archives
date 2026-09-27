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
 * $Id:FontMapEntry.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/FontMapEntry.java $
 */

/**
 * $Id:FontMapEntry.java 55 2007-05-19 05:55:34Z sushmu $
 * $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/src/intellibitz/sted/fontmap/FontMapEntry.java $
 */

package intellibitz.sted.fontmap;

import intellibitz.sted.util.Resources;

/**
 * Represents a FontMap Entry. All Mappings are transformed to a FontMapEntry.
 */
public class FontMapEntry
        implements ITransliterate.IEntry,
        Comparable,
        Cloneable {
    private String from;
    private String to;
    private boolean beginsWith;
    private boolean endsWith;
    private String followedBy;
    private String precededBy;
    private String conditional = Resources.ENTRY_CONDITIONAL_AND;
    private String id;
    private int status = -1;

    public FontMapEntry() {
        id = String.valueOf(Resources.getId());
    }

    public FontMapEntry(String from, String to) {
        this();
        this.from = from;
        this.to = to;
    }

    public boolean isAdded() {
        return Resources.ENTRY_STATUS_ADD == status;
    }

    public boolean isEdited() {
        return Resources.ENTRY_STATUS_EDIT == status;
    }

    public boolean isDeleted() {
        return Resources.ENTRY_STATUS_DELETE == status;
    }

    private String getConditional() {
        return conditional;
    }

    public void setConditional(String conditional) {
        if (Resources.ENTRY_CONDITIONAL_AND.equalsIgnoreCase(conditional)) {
            this.conditional = Resources.ENTRY_CONDITIONAL_AND;
        } else if (Resources.ENTRY_CONDITIONAL_OR.equalsIgnoreCase(conditional)) {
            this.conditional = Resources.ENTRY_CONDITIONAL_OR;
        } else if (Resources.ENTRY_CONDITIONAL_NOT.equalsIgnoreCase(conditional)) {
            this.conditional = Resources.ENTRY_CONDITIONAL_NOT;
        } else {
            throw new IllegalArgumentException(conditional);
        }
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isBeginsWith() {
        return beginsWith;
    }

    public void setBeginsWith(boolean beginsWith) {
        this.beginsWith = beginsWith;
    }

    public void setBeginsWith(String beginsWith) {
        this.beginsWith = Boolean.valueOf(beginsWith);
    }

    public boolean isEndsWith() {
        return endsWith;
    }

    public void setEndsWith(boolean endsWith) {
        this.endsWith = endsWith;
    }

    public void setEndsWith(String endsWith) {
        this.endsWith = Boolean.valueOf(endsWith);
    }

    public String getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(String followedBy) {
        if (Resources.EMPTY_STRING.equals(followedBy)) {
            this.followedBy = null;
        } else {
            this.followedBy = followedBy;
        }
    }

    public String getPrecededBy() {
        return precededBy;
    }

    public void setPrecededBy(String precededBy) {
        if (Resources.EMPTY_STRING.equals(precededBy)) {
            this.precededBy = null;
        } else {
            this.precededBy = precededBy;
        }
    }

    public boolean isValid() {
        return from != null
                && to != null
                && !Resources.EMPTY_STRING.equals(from)
                && !Resources.EMPTY_STRING.equals(to)
                && !from.equals(to);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    /**
     * @return true if atleast one of the rules is set
     */
    public boolean isRulesSet() {
        return beginsWith
                || endsWith
                || followedBy != null && followedBy.length() > 0
                || precededBy != null && precededBy.length() > 0;
    }

    /**
     * returns a string representation of a fontmap entry
     *
     * @return String
     */
    public String toString() {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getFrom());
        stringBuffer.append(Resources.ENTRY_TOSTRING_DELIMITER);
        stringBuffer.append(getTo());
        stringBuffer.append(Resources.ENTRY_TOSTRING_DELIMITER);
        stringBuffer.append(isBeginsWith());
        stringBuffer.append(Resources.ENTRY_TOSTRING_DELIMITER);
        stringBuffer.append(isEndsWith());
        stringBuffer.append(Resources.ENTRY_TOSTRING_DELIMITER);
        stringBuffer.append(getFollowedBy());
        stringBuffer.append(Resources.ENTRY_TOSTRING_DELIMITER);
        stringBuffer.append(getPrecededBy());
        stringBuffer.append(Resources.ENTRY_TOSTRING_DELIMITER);
        stringBuffer.append(getConditional());
        return stringBuffer.toString();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * @param tgt the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this Object.
     */
    public int compareTo(Object tgt) {
        if (FontMapEntry.class.isInstance(tgt)) {
            final FontMapEntry target = (FontMapEntry) tgt;
            if (equals(target)) {
                return 0;  //To change body of implemented methods use Options | File Templates.
            } else {
                if (!from.equals(target.getFrom())) {
                    return from.compareTo(target.getFrom());
                }
                if (!to.equals(target.getTo())) {
                    return to.compareTo(target.getTo());
                }
                if (beginsWith != target.isBeginsWith()) {
                    return 1;
                }
                if (endsWith != target.isEndsWith()) {
                    return 1;
                }
                if (!conditional.equals(target.getConditional())) {
                    return conditional.compareTo(target.getConditional());
                }
                boolean val = followedBy == null ?
                        target.getFollowedBy() == null :
                        followedBy.equals(target.getFollowedBy());
                if (!val) {
                    if (followedBy != null && target.getFollowedBy() != null) {
                        return followedBy.compareTo(target.getFollowedBy());
                    }
                    return 1;
                }
                val = precededBy == null ?
                        target.getPrecededBy() == null :
                        precededBy.equals(target.getPrecededBy());
                if (!val) {
                    if (precededBy != null && target.getPrecededBy() != null) {
                        return precededBy.compareTo(target.getPrecededBy());
                    }
                    return 1;
                }
            }
        }
        return -1;  //To change body of implemented methods use Options | File Templates.
    }

    /**
     * @param tgt
     * @return
     */
    public boolean equals(Object tgt) {
        if (this == tgt) {
            return true;
        }
        if (!(tgt instanceof FontMapEntry)) {
            return false;
        }
        final FontMapEntry target = (FontMapEntry) tgt;
        if (!from.equals(target.getFrom())) {
            return false;
        }
        if (!to.equals(target.getTo())) {
            return false;
        }
        if (beginsWith != target.isBeginsWith()) {
            return false;
        }
        if (endsWith != target.isEndsWith()) {
            return false;
        }
        if (!conditional.equals(target.getConditional())) {
            return false;
        }
//        if (status != target.getStatus()) return false;
        final boolean val = followedBy == null ?
                target.getFollowedBy() == null :
                followedBy.equals(target.getFollowedBy());
        if (!val) {
            return false;
        }
        return precededBy == null ? target.getPrecededBy() == null :
                precededBy.equals(target.getPrecededBy());
    }

    public int hashCode() {
        int result;
        result = from.hashCode();
        result = 29 * result + to.hashCode();
        result = 29 * result + conditional.hashCode();
        result = 29 * result + (beginsWith ? 1 : 0);
        result = 29 * result + (endsWith ? 1 : 0);
        result = 29 * result + (followedBy != null ? followedBy.hashCode() : 0);
        result = 29 * result + (precededBy != null ? precededBy.hashCode() : 0);
        return result;
    }

    /**
     * @see Cloneable
     */
    public Object clone() {
//            throws CloneNotSupportedException {
        try {
            final FontMapEntry cloned = (FontMapEntry) super.clone();
            cloned.id = id;
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        return null;
    }

}

