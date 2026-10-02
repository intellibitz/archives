import logging
from google.appengine.api import memcache

logging.info('util.cache.py loaded!')

VERSION = '_v1_'
DEFAULT_DURATION = 600

def getQueryCache(className, id):
    key = '%s%schangelist_%s' % (className.decode('utf-8'), VERSION, id)
    try:
        ret = memcache.get(key)
    except Exception:
        ret = None
    if ret is not None:
        logging.info('get from cache, key=%s', key)
    return ret

def putQueryCache(instance, cl):
    key = instance.key()
    key = '%s%schangelist_%s' % (key.kind(), VERSION, key.id())
    try:
        ret = memcache.get(key)
    except Exception:
        ret = None
    if ret is not None:
        logging.info('get from cache, key=%s', key)
    else:
        ret = cl.get_browse_value(instance)
        memcache.set(key, ret, DEFAULT_DURATION)
        logging.info('put into cache, key=%s', key)
    return ret

def getSearchOptionCache(className):
    key = '%s%ssearchoption' % (className, VERSION)
    try:
        ret = memcache.get(key)
    except Exception:
        ret = None
    if ret is not None:
        logging.info('get from cache, key=%s', key)
    return ret

def putSearchOptionCache(className, result_list):
    key = '%s%ssearchoption' % (className, VERSION)
    memcache.set(key, result_list, DEFAULT_DURATION)
    logging.info('put into cache, key=%s', key)

def removeSearchOptionCache(className):
    key = '%s%ssearchoption' % (className, VERSION)
    try:
        memcache.delete(key)
    except Exception:
        pass

def removeQueryCache(instance):
    key = instance.key()
    key = '%s%schangelist_%s' % (key.kind(), VERSION, key.id())
    try:
        memcache.delete(key)
    except Exception:
        pass

def removeCacheWhenAddInstance(className, instance):
    removeSearchOptionCache(className)

def removeCacheWhenEditInstance(className, instance):
    removeQueryCache(instance)
    removeSearchOptionCache(className)
    if not hasattr(instance, 'Admin'):
        return
    admin = instance.Admin
    if not hasattr(admin, 'parent'):
        return
    fieldName = admin.parent[0]
    parentInst = getattr(instance, fieldName)
    removeCacheWhenEditInstance(parentInst._meta.object_name, parentInst)

