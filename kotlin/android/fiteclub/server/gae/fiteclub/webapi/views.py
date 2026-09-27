import logging
from django.http import Http404
from google.appengine.api import users
from util.json import *
from util.common import getDateTimeValue, MAXFETCH
from fiteclubmodel.models import Profile

logging.info('webapi.views.py loaded!')

NICKNAME_PARAM = 'nickname'
CITY_PARAM = 'city'
MOOD_PARAM = 'mood'
LOCATION_PARAM = 'location'
NEWPASSWD_PARAM = 'newpasswd'
OLDPASSWD_PARAM = 'oldpasswd'

def get_profile(request):
    '''get profile by user'''
    user = users.get_current_user()
    if not user:
        raise Http404()
    try:
        l = Profile.all().filter('user =', user).filter('status =', 1).fetch(1)
        if not l:
            return getEmptyResponse()
        jsonInstance = getJsonInstanceWithCache(l[0])
        return getJsonResponse(jsonInstance)
    except Exception, e:
        return getErrorResponse(e)

def set_profile(request):
    '''set profile by user'''
    if request.method != 'POST':
        raise Http404()
    user = users.get_current_user()
    if not user:
        raise Http404()
    try:
        l = Profile.all().filter('user =', user).filter('status =', 1).fetch(1)
        if not l:
            instance = Profile(nickname='', user=user)
        else:
            instance = l[0]
        if not request.POST.has_key(NICKNAME_PARAM):
            instance.nickname = request.POST[NICKNAME_PARAM]
        if not request.POST.has_key(CITY_PARAM):
            instance.city = request.POST[CITY_PARAM]
        if not request.POST.has_key(MOOD_PARAM):
            instance.mood = request.POST[MOOD_PARAM]
        if not request.POST.has_key(LOCATION_PARAM):
            value = request.POST[LOCATION_PARAM]
            values = value.split(',')
            value = db.GeoPt(float(values[0]), float(values[1]))
            instance.location = value
        
        instance.put()
        return okResponse(1, 'ok')
    except Exception, e:
        return getErrorResponse(e)
    
def set_password(request):
    if request.method != 'POST':
        raise Http404()
    try:
        if not request.POST.has_key(NICKNAME_PARAM):
            raise RuntimeError('no "%s" parameter' % NICKNAME_PARAM)
        if not request.POST.has_key(NEWPASSWD_PARAM):
            raise RuntimeError('no "%s" parameter' % NEWPASSWD_PARAM)
        nickname = request.POST[NICKNAME_PARAM]
        newpasswd = request.POST[NEWPASSWD_PARAM]
        if request.POST.has_key(OLDPASSWD_PARAM):
            oldpasswd = request.POST[OLDPASSWD_PARAM]
        else:
            oldpasswd = None
        raise RuntimeError('info: %s %s %s' % (nickname, newpasswd, oldpasswd))
    except Exception, e:
        return getErrorResponse(e)
