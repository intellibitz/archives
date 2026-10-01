import logging
from google.appengine.api import users, images
from django.shortcuts import render_to_response
from django.http import HttpResponse, Http404
from gaedjango import change_list, view_detail, edit_list, add_item, edit_item
from util.globals import MODELCLASS_DICT, WEBEDIT_DICT

logging.info('webui.views.py loaded!')

def get_modelclass(clsName):
    if WEBEDIT_DICT.has_key(clsName):
        return WEBEDIT_DICT[clsName]
    return MODELCLASS_DICT[clsName]

def list_page(request, clsName):
    logging.info('list_page start! clsName=' + clsName)
    try:
        modelClass = MODELCLASS_DICT[clsName]
    except KeyError:
        raise Http404()
    #if modelClass == Chef:
    #    raise Http404()
    return change_list(request, modelClass)

def editlist_page(request, clsName):
    logging.info('editlist_page start! clsName=' + clsName)
    try:
        modelClass = get_modelclass(clsName)
    except KeyError:
        raise Http404()
    return edit_list(request, modelClass, clsName)

def view_page(request, clsName, objId):
    try:
        modelClass = MODELCLASS_DICT[clsName]
    except KeyError:
        raise Http404()
    #if modelClass == Chef:
    #    raise Http404()
    return view_detail(request, modelClass, int(objId))

def add_page(request, clsName):
    try:
        modelClass = get_modelclass(clsName)
    except KeyError:
        raise Http404()
    return add_item(request, modelClass)

def edit_page(request, clsName, objId):
    try:
        modelClass = get_modelclass(clsName)
    except KeyError:
        raise Http404()
    return edit_item(request, modelClass, int(objId))

