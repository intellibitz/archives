import logging
from django.template import Library
from django.conf import settings
import re

logging.info('templatetags.admin_list.py loaded!')

register = Library()

def result_headers(cl):
    yield {"text": 'No.'}
   
    if not cl.flagView and hasattr(cl.admin, 'editlist_display'):
        list_display = getattr(cl.admin, 'editlist_display')
    elif hasattr(cl.admin, 'list_display'):
        list_display = getattr(cl.admin, 'list_display')
    else:
        fields = cl.model._meta.fields
        list_display = [field.name for field in fields]
    
    headdict = cl.result_heads
    for fieldname in list_display:
        if headdict.has_key(fieldname):
            yield {"text": headdict[fieldname]}

def items_for_result(cl, result, count):
    row_class = ''  #' class="nowrap"'
    first = True

    yield ('<th%s>%s</th>' % (row_class, str(count)))

    if not cl.flagView and hasattr(cl.admin, 'editlist_display'):
        list_display = getattr(cl.admin, 'editlist_display')
    elif hasattr(cl.admin, 'list_display'):
        list_display = getattr(cl.admin, 'list_display')
    else:
        fields = cl.model._meta.fields
        list_display = [field.name for field in fields]

    for fieldname in list_display:
        if first:
            first = False
            if hasattr(cl, 'flagInList'):
                yield ('<td%s><a href="../../%s/%s">%s</a></td>' % (row_class, cl.model._meta.object_name.lower(), str(result['ID']), result[fieldname]))
            else:
                yield ('<td%s><a href="%s">%s</a></td>' % (row_class, str(result['ID']), result[fieldname]))
        else:
            if result.has_key(fieldname):
                yield ('<td%s>%s</td>' % (row_class, result[fieldname]))

def results(cl):
    count = 0;
    for res in cl.result_list:
        count += 1
        yield list(items_for_result(cl, res, count))

def result_list(cl):
    return {'cl': cl,
            'result_headers': list(result_headers(cl)),
            'results': list(results(cl))}
result_list = register.inclusion_tag("change_list_results.html")(result_list)

def item_for_searchresult(cl, searchinfo):
    fieldname = searchinfo[0]
    paramvalue = searchinfo[1]
    field = getattr(cl.model, fieldname)
    verbose_name = field.verbose_name
    if verbose_name is None:
        verbose_name = fieldname
    selected = ' selected="selected"'
    selectedNow = ''
    if paramvalue == 0:
        selectedNow = selected
    template = '<option value="%s"%s>%s</option>'
    values = [template % ('', selectedNow, '-%s-' % verbose_name)]
    for info in searchinfo[2]:
        if paramvalue == info[0]:
            selectedNow = selected
        else:
            selectedNow = ''
        values.append(template % (str(info[0]), selectedNow, info[1]))
    return ('<select id="search_%s" class="vSelectField" name="search_%s" size="1">%s</select>' % (fieldname, fieldname, ''.join(values)))

def searchresults(cl):
    for searchinfo in cl.searchinfos:
        yield item_for_searchresult(cl, searchinfo)
        
def search_form(cl):
    return {
        'cl': cl,
        'search_flag': cl.search_flag,
        'search_var': cl.search_var,
        'searchinfos': list(searchresults(cl))
    }
search_form = register.inclusion_tag('search_form.html')(search_form)

absolute_url_re = re.compile(r'^(?:http(?:s)?:/)?/', re.IGNORECASE)

def include_admin_script(script_path):
    """
    Returns an HTML script element for including a script from the admin
    media url (or other location if an absolute url is given).

    Example usage::

        {% include_admin_script "calendar.js" %}

    could return::

        <script type="text/javascript" src="/media/calendar.js">
    """
    if not absolute_url_re.match(script_path):
        script_path = '%s%s' % (settings.ADMIN_MEDIA_PREFIX, script_path)
    return '<script type="text/javascript" src="%s"></script>' % script_path
include_admin_script = register.simple_tag(include_admin_script)

def submit_row(context):
    change = context['change']
    is_popup = context['is_popup']
    return {
        'onclick_attrib': '',
        'show_delete_link': False,      #should just set status to mark delete only. can delete permanently by appengine dataview
        'show_save_as_new': not is_popup and change,
        'show_save_and_add_another': not is_popup and not change,
        'show_save': True
    }
submit_row = register.inclusion_tag('submit_line.html', takes_context=True)(submit_row)
