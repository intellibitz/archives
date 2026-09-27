import logging
from datetime import time, date
from django import template
from django.shortcuts import render_to_response
from django.db.models import get_model
from django.conf import settings
from django.http import HttpResponse, HttpResponseRedirect, Http404
from django.utils.html import escape
from appengine_django.models import BaseModel
from google.appengine.ext import db
from util.cache import getQueryCache, putQueryCache, removeCacheWhenAddInstance, removeCacheWhenEditInstance
from util.search import getReferenceOptions
from util.template import getReferenceAddButtonHtml, getDetailAddButtonHtml
from util.common import getDateTimeValue

logging.info('webui.gaedjango.py loaded!')

# The system will display a "Show all" link on the change list only if the
# total result count is less than or equal to this setting.
MAX_SHOW_ALL_ALLOWED = 500

# Changelist settings
SEARCH_VAR = 'q'

def getBrowseValue(model, instance):
    key = instance.key()
    rowvalue = {'ID': key.id()}
    fields = model._meta.fields
    for field in fields:
        fieldName = field.name
        dispMethod = 'display_' + fieldName;
        if hasattr(instance, dispMethod):
            value = getattr(instance, dispMethod)()
        else:
            value = getattr(instance, fieldName)
            if value is None:
                value = ''
            elif not isinstance(value, unicode):
                value = str(value)
        rowvalue[fieldName] = value
    return rowvalue

def comp(field, x, y, ordering, asc):
    x = getattr(x, ordering)
    y = getattr(y, ordering)
    if x == y:
        return 0
    if isinstance(field, db.ReferenceProperty):
        x = str(x)
        y = str(y)
    if x > y:
        return asc
    else:
        return -asc

class ChangeList(object):
    def __init__(self, request, model, flagView):
        self.model = model
        self.admin = None
        self.flagView = flagView
        if hasattr(self.model, 'Admin'):
            self.admin = self.model.Admin

        self.search_var = None
        self.search_flag = False
        self.searchinfos = []
        if hasattr(self.admin, 'search_fields'):
            self.searchinfos = self.get_search(self.admin.search_fields, request)
            if self.searchinfos:
                self.search_flag = True

        self.ordering = self.get_ordering()
        self.query = self.get_query()
        self.get_results(request)

        if hasattr(self.admin, 'verbose_name'):
            self.title = self.admin.verbose_name
        else:
            self.title = model._meta.object_name
        
    def get_search(self, search_fields, request):
        '''get search params info'''
        if not search_fields:
            return None
        searchinfos = []
        
        for fieldname in search_fields:
            searchMethod = 'search_' + fieldname;
            queryparam = request.GET.get(searchMethod, '')
            if hasattr(self.model, searchMethod):
                if queryparam == '':
                    queryparam = 0
                else:
                    queryparam = int(queryparam)
                searchinfo = (fieldname, queryparam, getattr(self.model, searchMethod)())
                searchinfos.append(searchinfo)
            #TODO: support input & foreignkey
            #else:
            #    self.search_var = SEARCH_VAR
            #    self.queryparam = request.GET.get(SEARCH_VAR, '')   # key:value;...
        
        return searchinfos

    def get_ordering(self):
        '''only support one order field now'''
        ordering = None
        if hasattr(self.admin, 'ordering'):
            ordering = self.admin.ordering
        return ordering;

    def get_query(self):
        query = self.model.all()
        if self.searchinfos:
            for searchinfo in self.searchinfos:
                queryparam = searchinfo[1]
                if queryparam != 0:
                    query.filter(searchinfo[0] + ' =', queryparam)
        return query
    
    def get_results(self, request):
        fields = self.model._meta.fields
        result_heads = {}
        for field in fields:
            fieldName = field.name
            verbose_name = field.verbose_name
            if verbose_name is None:
                verbose_name = fieldName
            result_heads[fieldName] = verbose_name
        self.result_heads = result_heads

        qs = self.query.fetch(MAX_SHOW_ALL_ALLOWED)
        if self.ordering:
            if self.ordering[0] == '-':
                asc = -1
                ordering = self.ordering[1:]
            else:
                asc = 1
                ordering = self.ordering
            field = getattr(self.model, ordering)
            if field:
                qs.sort(lambda x,y: comp(field, x, y, ordering, asc))
        result_list = []
        if qs:
            for instance in qs:
                if instance.status == 1 or not self.flagView:
                    value = putQueryCache(instance, self)
                    result_list.append(value)

        self.result_list = result_list
    
    def get_browse_value(self, instance):
        return getBrowseValue(self.model, instance)

def change_list(request, model):
    cl = ChangeList(request, model, True)
    c = template.RequestContext(request, {
        'title': cl.title,
        'cl': cl,
        })
    return render_to_response('change_list.html', context_instance=c)

class DetailList(object):
    def __init__(self, model, queryset, name, flagView=True, parentId=0):
        self.model = model
        self.admin = None
        self.name = name
        self.flagView = flagView
        if self.flagView:
            self.addhtml = None
        else:
            self.addhtml = getDetailAddButtonHtml(self.model._meta.object_name, parentId, name)
        if hasattr(self.model, 'Admin'):
            self.admin = self.model.Admin

        self.get_results(queryset)

        if hasattr(self.admin, 'verbose_name'):
            self.title = self.admin.verbose_name
        else:
            self.title = self.model._meta.object_name
        self.flagInList = True

    def get_results(self, qs):
        fields = self.model._meta.fields
        result_heads = {}
        for field in fields:
            fieldName = field.name
            verbose_name = field.verbose_name
            if verbose_name is None:
                verbose_name = fieldName
            result_heads[fieldName] = verbose_name
        self.result_heads = result_heads

        result_list = []
        for instance in qs:
            if instance.status == 1 or not self.flagView:
                value = putQueryCache(instance, self)
                result_list.append(value)
        self.result_list = result_list
    
    def get_browse_value(self, instance):
        return getBrowseValue(self.model, instance)

class ViewDetail(object):
    def __init__(self, model, objId):
        self.model = model
        self.admin = None
        if hasattr(self.model, 'Admin'):
            self.admin = self.model.Admin

        self.instance = self.model.get_by_id(objId)
        self.get_results()
        self.get_details()

        self.title = str(self.instance)
        
    def get_details(self):
        detail_list = []
        data = self.instance
        for m in [method for method in dir(data) if callable(getattr(data, method)) and method.find('detail_') == 0]:
            detailName = m[7:]  
            qs, detailClass = getattr(data, m)()
            detail_list.append(DetailList(detailClass, qs, m))
        self.detail_list = detail_list
        
    def get_results(self):
        value = putQueryCache(self.instance, self)
        
        if hasattr(self.admin, 'detail_display'):
            detail_display = getattr(self.admin, 'detail_display')
            fields = [getattr(self.model, fieldname) for fieldname in detail_display]
        else:
            fields = self.model._meta.fields

        result_list = []
        for field in fields:
            fieldName = field.name
            verbose_name = field.verbose_name
            if verbose_name is None:
                verbose_name = fieldName
            result_list.append({'label': verbose_name, 'inner': value[fieldName]})
        self.result_list = result_list

    def get_browse_value(self, instance):
        return getBrowseValue(self.model, instance)

def view_detail(request, model, objId):
    vd = ViewDetail(model, objId)
    c = template.RequestContext(request, {
        'title': vd.title,
        'results': vd.result_list,
        'details': vd.detail_list,
        })
    return render_to_response('view_form.html', context_instance=c)

def edit_list(request, model, modelName):
    cl = ChangeList(request, model, False)
    c = template.RequestContext(request, {
        'title': cl.title,
        'cl': cl,
        'flag_edit': True,
        'modelname': modelName
        })
    return render_to_response('change_list.html', context_instance=c)

class AddEditDetail(object):
    def __init__(self, model, data, errors, instance=None):
        self.model = model
        self.admin = None
        self.change = instance is not None
        self.parentField = None
        if hasattr(self.model, 'Admin'):
            self.admin = self.model.Admin
            if hasattr(self.admin, 'parent'):
                self.parentField = self.admin.parent[0]

        self.get_jsimports()
        self.get_results(data, errors, instance)
        if self.change:
            self.get_details(instance)

        if self.change:
            self.title = 'Edit '
        else:
            self.title = 'Add '
        if hasattr(self.admin, 'verbose_name'):
            self.title += self.admin.verbose_name
        else:
            self.title += model._meta.object_name
        
    def get_results(self, data, errors, instance):
        if self.change and hasattr(self.admin, 'edit_display'):
            edit_display = getattr(self.admin, 'edit_display')
            fields = [getattr(self.model, fieldname) for fieldname in edit_display]
        elif hasattr(self.admin, 'add_display'):
            add_display = getattr(self.admin, 'add_display')
            fields = [getattr(self.model, fieldname) for fieldname in add_display]
        else:
            fields = self.model._meta.fields

        result_list = []
        for field in fields:
            fieldName = field.name
            verbose_name = field.verbose_name
            if verbose_name is None:
                verbose_name = fieldName
            listValue = ['']
            if instance is not None:
                value = getattr(instance, fieldName)
                if isinstance(value, list):
                    listValue = [str(id) for id in value]
                elif isinstance(value, BaseModel):
                    listValue = [str(value.key().id())]
                elif isinstance(value, unicode):
                    listValue = [value]
                elif value is not None:
                    listValue = [str(value)]
            elif data.has_key(fieldName):
                listValue = dict.__getitem__(data, fieldName)   #all value in data is a list
                if len(listValue) <= 0:
                    listValue = ['']
            else:
                value = field.default_value()
                if isinstance(value, unicode):
                    listValue = [value]
                elif value is not None:
                    listValue = [str(value)]
            
            inputMethod = 'input_' + fieldName;
            if fieldName == self.parentField:
                if self.change:
                    dispMethod = 'display_' + fieldName;
                    if hasattr(instance, dispMethod):
                        value = getattr(instance, dispMethod)()
                    else:
                        value = getattr(instance, fieldName)
                else:
                    referenceClass = field.reference_class
                    referenceId = long(data['parent'])
                    value = referenceClass.get_by_id(referenceId)
                innerHtml = str(value)
            elif hasattr(self.model, inputMethod):
                innerHtml = getattr(self.model, inputMethod)(listValue)
            elif isinstance(field, db.TextProperty):
                innerHtml = '<textarea id="id_%s" class="vLargeTextField" name="%s" rows="10" cols="50">%s</textarea>' % (fieldName, fieldName, escape(listValue[0]))
            elif isinstance(field, db.StringProperty) and field.multiline:
                innerHtml = '<textarea id="id_%s" class="vTextField" name="%s" rows="5" cols="50">%s</textarea>' % (fieldName, fieldName, escape(listValue[0]))
            elif isinstance(field, db.ReferenceProperty):
                referenceClass = field.reference_class
                options = getReferenceOptions(referenceClass)
                template = '<option value="%s"%s>%s</option>'
                selectStr = ' selected="selected"'
                if listValue[0]:
                    values = [template % ('', '', '---------')]
                else:
                    values = [template % ('', selectStr, '---------')]
                for info in options:
                    infoStr0 = info[0]
                    if not isinstance(infoStr0, unicode):
                        infoStr0 = str(infoStr0)
                    if listValue[0] == infoStr0:
                        values.append(template % (infoStr0, selectStr, info[1]))
                    else:
                        values.append(template % (infoStr0, '', info[1]))
                addhtml = getReferenceAddButtonHtml(referenceClass._meta.object_name, fieldName)
                innerHtml = '<select id="id_%s" class="vSelectField required" name="%s" size="1">%s</select> %s' % (fieldName, fieldName, ''.join(values), addhtml)
            elif isinstance(field, db.LinkProperty) or isinstance(field, db.BlobProperty):      #BlobProperty treated as LinkProperty
                url = escape(listValue[0])
                innerHtml = '<input type="text" id="id_%s" class="vTextField" name="%s" size="50" value="%s"/>&nbsp;<a href="%s">%s</a>' \
                        % (fieldName, fieldName, url, url, url)
            elif isinstance(field, db.TimeProperty):
                innerHtml = '<input type="text" id="id_%s" class="vTimeField" name="%s" size="8" value="%s"/>' % (fieldName, fieldName, escape(listValue[0]))
            elif isinstance(field, db.DateProperty):
                innerHtml = '<input type="text" id="id_%s" class="vDateField" name="%s" size="12" value="%s"/>' % (fieldName, fieldName, escape(listValue[0]))
            else:
                innerHtml = '<input type="text" id="id_%s" class="vTextField" name="%s" size="50" value="%s"/>' % (fieldName, fieldName, escape(listValue[0]))
            if errors.has_key(fieldName):
                error = errors[fieldName]
            else:
                error = ''
            if field.required or isinstance(field, db.ReferenceProperty):
                classStr = 'class="required"'
            else:
                classStr = ''
            result = {'error': error, 'label': verbose_name, 'inner': innerHtml, 'class': classStr}
            result_list.append(result)
        self.result_list = result_list

    def get_details(self, instance):
        detail_list = []
        data = instance
        for m in [method for method in dir(data) if callable(getattr(data, method)) and method.find('detail_') == 0]:
            detailName = m[7:]  
            qs, detailClass = getattr(data, m)()
            detail_list.append(DetailList(detailClass, qs, m, False, instance.key().id()))
        self.detail_list = detail_list
        
    def get_jsimports(self):
        self.js_imports = ['core.js', 'RelatedObjectLookups.js', 'SelectBox.js' , 'SelectFilter2.js', 'calendar.js', 'DateTimeShortcuts.js']
        
def checkUnique(model, instance, objId=None):
    if not hasattr(model, 'Admin'):
        return
    admin = model.Admin
    if not hasattr(admin, 'unique'):
        return
    unique = admin.unique
    if not unique:
        return
    
    query = model.all()
    for fieldName in unique:
        query.filter(fieldName + ' =', getattr(instance, fieldName))
    count = query.count()
    if count <= 0:
        return
    if objId is not None and count == 1:
        inst = query.fetch(1)[0]
        if inst.key().id() == objId:
            return      #it's self, so skip
    raise KeyError('The field %s is not unique!' % str(unique))

def set_instance_value(model, field, listValue, kwds, errors, required = True):
    value = listValue[0]
    fieldName = field.name
    convertMethod = 'convert_' + fieldName;
    if hasattr(model, convertMethod):
        value = getattr(model, convertMethod)(listValue)
    else:
        if value == '':
            if field.required and required:
                errors[fieldName] = 'This field is required.'
            return
        if isinstance(field, db.IntegerProperty):
            value = long(value)
        elif isinstance(field, db.StringProperty):
            value = value.decode('utf-8')
        elif isinstance(field, db.BooleanProperty):
            value = bool(value)
        elif isinstance(field, db.FloatProperty):
            value = float(value)
        elif isinstance(field, db.TimeProperty):
            values = value.split(':')
            value = time(int(values[0]), int(values[1]), int(values[2]))
        elif isinstance(field, db.DateProperty):
            values = value.split('-')
            value = date(int(values[0]), int(values[1]), int(values[2]))
        elif isinstance(field, db.DateTimeProperty):
            value = getDateTimeValue(long(value))   #require javascript translate datetime to long
        elif isinstance(field, db.ReferenceProperty):
            value = long(value)
            entityName = field.reference_class.__name__
            value = db.Key.from_path(entityName, value)
        elif isinstance(field, db.ListProperty):
            value = [long(value) for value in listValue]
        elif isinstance(field, db.GeoPtProperty):
            values = value.split(',')
            value = db.GeoPt(float(values[0]), float(values[1]))
    kwds[fieldName] = value

def add_instance(model, data):
    fields = model._meta.fields
    kwds = {}
    errors = {}
    instance = None
    parentFieldName = None
    if hasattr(model, 'Admin'):
        admin = model.Admin
        if hasattr(admin, 'parent'):
            parentFieldName = admin.parent[0]
    for field in fields:
        fieldName = field.name
        listValue = ['']
        if data.has_key(fieldName):
            listValue = dict.__getitem__(data, fieldName)
            if len(listValue) <= 0:
                listValue = ['']
        elif parentFieldName == fieldName and data.has_key('parent'):
            listValue = dict.__getitem__(data, 'parent')
        try:
            set_instance_value(model, field, listValue, kwds, errors)
        except Exception, e:
            errors[fieldName] = str(e)
    if len(errors) < 1:
        try:
            instance = model(**kwds)
            checkUnique(model, instance)
            if hasattr(instance, 'validate_instance'):
                instance.validate_instance(True)
            removeCacheWhenAddInstance(model._meta.object_name, instance)
            if hasattr(instance, 'put_instance'):
                instance.put_instance(True)
            else:
                instance.put()
            logging.info('model instance added! model=%s instance=%s' % (model._meta.object_name, str(instance)))
        except Exception, e:
            errors['form_error'] = str(e)
            instance = None
    return errors, instance

def edit_instance(model, instance, data):
    fields = model._meta.fields
    kwds = {}
    errors = {}
    for field in fields:
        fieldName = field.name
        listValue = ['']
        if data.has_key(fieldName):
            listValue = dict.__getitem__(data, fieldName)
            if len(listValue) <= 0:
                listValue = ['']
        try:
            set_instance_value(model, field, listValue, kwds, errors, False)
        except Exception, e:
            errors[fieldName] = str(e)
    if len(errors) < 1:
        try:
            for name in kwds:
                setattr(instance, name, kwds[name])
            checkUnique(model, instance, instance.key().id())
            if hasattr(instance, 'validate_instance'):
                instance.validate_instance(False)
            removeCacheWhenEditInstance(model._meta.object_name, instance)
            if hasattr(instance, 'put_instance'):
                instance.put_instance(False)
            else:
                instance.put()
            logging.info('model instance modified! model=%s instance=%s' % (model._meta.object_name, str(instance)))
        except Exception, e:
            errors['form_error'] = str(e)
            instance = None
    return errors

def get_redirect_url(path, up=1):
    l = path.split('/')
    pos = -1 - up
    l = l[:pos]
    l.append('')
    return '/'.join(l)

def request_parent_url(request, model, parentID):
    if parentID is None and not request.GET.has_key('parent'):
        return None
    if not hasattr(model, 'Admin'):
        return None
    admin = model.Admin
    if not hasattr(admin, 'parent'):
        return None
    fieldName = admin.parent[0]
    field = getattr(model, fieldName)
    referenceClass = field.reference_class
    if parentID is None:
        parentID = request.GET['parent']
    else:
        parentID = str(parentID)
    return '%s%s/%s/#%s' % (get_redirect_url(request.path, 2), referenceClass._meta.object_name.lower(), parentID, admin.parent[1])

def add_item(request, model, parentID=None):
    messgae = ''
    parent_url = request_parent_url(request, model, parentID)
    if request.POST:
        new_data = request.POST.copy()
        if parentID is not None:
            new_data['parent'] = str(parentID)
        elif request.GET.has_key('parent'):
            new_data['parent'] = request.GET['parent']
        errors, new_object = add_instance(model, new_data)
        if not errors:
            if request.POST.has_key('_popup'):
                refid = str(new_object.key().id())
                refname = str(new_object)
                return HttpResponse('<script type="text/javascript">opener.dismissAddAnotherPopup(window, %s, "%s");</script>' % (refid, refname.replace('"', '\\"')))
            elif request.POST.has_key("_addanother"):
                new_data = request.GET.copy()
                messgae = 'You may add another record below.'
            else:
                if parent_url:
                    return HttpResponseRedirect(parent_url)
                else:
                    return HttpResponseRedirect(get_redirect_url(request.path))     #GAE bug, HttpResponseRedirect only support absolute url
    else:
        new_data = request.GET.copy()
        errors = {}
    logging.info('errors: %s' % str(errors))
    ad = AddEditDetail(model, new_data, errors)
    if errors.has_key('form_error'):
        form_error = errors['form_error']
    elif len(errors) > 0:
        form_error = 'Please correct the error below.'
    else:
        form_error = ''
    c = template.RequestContext(request, {
        'change': False,
        'is_popup': request.REQUEST.has_key('_popup'),
        'title': ad.title,
        'javascript_imports': ad.js_imports,
        'results': ad.result_list,
        'form_error': form_error,
        'message': messgae,
        'parent_url': parent_url
        })
    return render_to_response('addedit_form.html', context_instance=c)

def get_parent_url(path, instance):
    if not hasattr(instance, 'Admin'):
        return None, None
    admin = instance.Admin
    if not hasattr(admin, 'parent'):
        return None, None
    fieldName = admin.parent[0]
    parentInst = getattr(instance, fieldName)
    parentID = parentInst.key().id()
    return '%s%s/%s/#%s' % (get_redirect_url(path, 2), parentInst._meta.object_name.lower(), str(parentID), admin.parent[1]), parentID

def edit_item(request, model, objId):
    messgae = ''
    instance = model.get_by_id(objId)
    if instance is None:
        raise Http404()
    parent_url, parent_id = get_parent_url(request.path, instance)
    if request.POST:
        if request.POST.has_key('_saveasnew'):
            return add_item(request, model, parentID=parent_id)
        new_data = request.POST.copy()
        errors = edit_instance(model, instance, new_data)
        if not errors:
            if parent_url:
                return HttpResponseRedirect(parent_url)
            else:
                return HttpResponseRedirect(get_redirect_url(request.path))
    else:
        errors = {}
    ed = AddEditDetail(model, {}, errors, instance)
    if errors.has_key('form_error'):
        form_error = errors['form_error']
    elif len(errors) > 0:
        form_error = 'Please correct the error below.'
    else:
        form_error = ''
    c = template.RequestContext(request, {
        'change': True,
        'is_popup': request.REQUEST.has_key('_popup'),
        'title': ed.title,
        'javascript_imports': ed.js_imports,
        'results': ed.result_list,
        'details': ed.detail_list,
        'form_error': form_error,
        'message': messgae,
        'parent_url': parent_url
        })
    return render_to_response('addedit_form.html', context_instance=c)
