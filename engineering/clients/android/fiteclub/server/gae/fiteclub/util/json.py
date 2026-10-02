import logging
import types
from google.appengine.api import users, memcache
from django.http import HttpResponse, HttpResponseServerError
from appengine_django.models import BaseModel
from google.appengine.ext import db
from django.utils import simplejson as json
from django.core.serializers.json import DateTimeAwareJSONEncoder
from decimal import Decimal
from datetime import datetime, time, date
from util.common import getJavaTimeStamp, getDateTimeValue
from util.cache import VERSION 

logging.info('util.json.py loaded!')

class JsonText:
    def __init__(self, text):
        self.text = text

def getJsonInstance(instance):
    '''warp instance with some json keys'''
    if instance == None:
        return None
    modelClass = instance.__class__
    if hasattr(modelClass, 'get_jsonmodel'):
        modelName = modelClass.get_jsonmodel()
    else:
        modelName = modelClass._meta.object_name
    if instance.is_saved():
        id = instance.key().id()
    else:
        id = 0L
    return {'pk': id, 'model': modelName, 'fields': instance, 'retcode': 1}

def putJsonCache(instance, duration=0):
    '''if has key in cache, return json text, else encode instance to json text, put the text into cache and return it.'''
    key = instance.key()
    key = '%s%s%s' % (key.kind(), VERSION, key.id())
    try:
        ret = memcache.get(key)
    except Exception:
        ret = None
    if ret is not None:
        logging.info('get from cache, key=%s', key)
    else:
        ret = JsonText(json_encode(getJsonInstance(instance)))
        memcache.set(key, ret, duration)
        logging.info('put into cache, key=%s', key)
    return ret

def getJsonInstanceWithCache(instance):
    return putJsonCache(instance, 600)

def addObjectSet(jsonSet, queryList):
    '''Add each object in queryList to jsonSet preparing for json encode'''
    if queryList:
        for instance in queryList:
            value = putJsonCache(instance, 600)
            jsonSet.append(value)

def getJsonResponse(jsonInstance):
    response = HttpResponse(mimetype='text/json')
    response.write(json_encode(jsonInstance))
    return response

def getEmptyResponse():
    response = HttpResponse(mimetype='text/json')
    response.write('{}')
    return response

def getErrorResponse(e):
    return HttpResponseServerError("%s:%s" % (type(e), e))

def okResponse(retCode, retVal):
    instanceDict = {'retcode': retCode, 'retval': retVal}
    return getJsonResponse(instanceDict)

def json_encode(data):
    """
    convert data to json format, specially handle datetime and blob property.
    """

    def _any(data):
        ret = None
        if type(data) is types.ListType:
            ret = _list(data)
        elif type(data) is types.DictType:
            ret = _dict(data)
        elif isinstance(data, Decimal):
            # json.dumps() can't handle Decimal
            ret = str(data)
        elif isinstance(data, users.User):
            ret = str(data)
        elif isinstance(data, db.GeoPt):
            ret = str(data)
        elif isinstance(data, db.IM):
            ret = data.protocol + ' ' + data.address
        elif isinstance(data, datetime):
            # datetime to long
            ret = getJavaTimeStamp(data) 
        elif isinstance(data, BaseModel):
            ret = _model(data)
        else:
            ret = data
        return ret
    
    def _model(data):
        ret = {}
        skipfields = None
        if hasattr(data, 'get_skipfields'):
            skipfields = data.get_skipfields()
        # If we only have a model, we only want to encode the fields.
        for f in data._meta.fields:
            if skipfields:
                if f.name in skipfields:
                    continue
            jsonMethodName = 'getjson_' + f.name;
            if hasattr(data, jsonMethodName):
                ret[f.name] = _any(getattr(data, jsonMethodName)())
            elif isinstance(f, db.ReferenceProperty):
                #ReferenceProperty's json name has '_id' suffix
                refKey = getattr(data, '_' + f.name)
                if refKey:
                    ret[f.name + '_id'] = _any(refKey.id())
                else:
                    ret[f.name + '_id'] = None
            else:
                ret[f.name] = _any(getattr(data, f.name))
        #do with copy fields
        if hasattr(data, 'get_copyfields'):
            for copyInfo in data.get_copyfields():
                ret[copyInfo[0]] = _any(copyInfo[1])
        #do with specific detail methods
        for m in [method for method in dir(data) if callable(getattr(data, method)) and method.find('detail_') == 0]:
            detailName = m[7:]  #skip detail_
            qs, detailClass = getattr(data, m)()
            ret[detailName] = _any([getJsonInstance(inst) for inst in qs if inst.status == 1])

        return ret
    
    def _list(data):
        ret = []
        for v in data:
            ret.append(_any(v))
        return ret
    
    def _dict(data):
        ret = {}
        for k,v in data.items():
            ret[k] = _any(v)
        return ret
    
    if type(data) is types.ListType:
        ret = []
        for item in data:
            if isinstance(item, JsonText):
                ret.append(item.text)
            else:
                ret.append(json.dumps(_any(item), cls=DateTimeAwareJSONEncoder))
        return '[' + ', '.join(ret) + ']'
    else:
        if isinstance(data, JsonText):
            return data.text
        else:
            ret = _any(data)
            return json.dumps(ret, cls=DateTimeAwareJSONEncoder)
    
