import logging
from django.conf import settings

logging.info('util.template.py loaded!')

def getMultiSelectHtml(options, listValue, fieldName, verboseName, clsName):
    addhtml = '<script type="text/javascript">addEvent(window, "load", function(e) { SelectFilter.init("id_%s", "%s", 0, "%s"); });</script>' % (fieldName, verboseName, settings.ADMIN_MEDIA_PREFIX)
    addhtml = '%s %s' % (addhtml, getReferenceAddButtonHtml(clsName, fieldName))
    template = '<option value="%s"%s>%s</option>'
    selectStr = ' selected="selected"'
    values = []
    for info in options:
        if str(info[0]) in listValue:
            values.append(template % (str(info[0]), selectStr, info[1]))
        else:
            values.append(template % (str(info[0]), '', info[1]))
    return '<select id="id_%s" class="vSelectMultipleField" name="%s" size="20" multiple="multiple">%s</select> %s' % (fieldName, fieldName, ''.join(values), addhtml)

def getDictSelectHtml(dict, value, fieldName):
    template = '<option value="%s"%s>%s</option>'
    selectStr = ' selected="selected"'
    values = []
    for key in dict:
        if key == value:
            values.append(template % (str(key), selectStr, str(dict[key])))
        else:
            values.append(template % (str(key), '', str(dict[key])))
    return '<select id="id_%s" class="vSelectField required" name="%s" size="1">%s</select>' % (fieldName, fieldName, ''.join(values))

def getReferenceAddButtonHtml(clsName, fieldName):
    return '<a href="../../%s/add/" class="add-another" id="add_id_%s" onclick="return showAddAnotherPopup(this);"> <img src="%sicon_addlink.gif" width="10" height="10" alt="Add Another"/></a>' \
                % (clsName.lower(), fieldName, settings.ADMIN_MEDIA_PREFIX)

def getDetailAddButtonHtml(clsName, parentId, fieldName):
    return '<a href="../../%s/add?parent=%s" class="add-another" id="add_id_%s"> <img src="%sicon_addlink.gif" width="10" height="10" alt="Add Another"/></a>' \
                % (clsName.lower(), str(parentId), fieldName, settings.ADMIN_MEDIA_PREFIX)
