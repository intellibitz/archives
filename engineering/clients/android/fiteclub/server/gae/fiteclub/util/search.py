import logging
from appengine_django.models import BaseModel
from google.appengine.ext import db, search
from util.cache import getSearchOptionCache, putSearchOptionCache
from util.template import getDictSelectHtml

logging.info('util.search.py loaded!')

STATUS_CHOICES_DICT = {
    1: 'normal',
    2: 'frozen',
    3: 'deleted'
}

def getSearchOptions(modelClass):
    className = modelClass._meta.object_name
    result_list = getSearchOptionCache(className)
    if result_list is not None:
        return result_list
    
    query = modelClass.all()
    
    if hasattr(modelClass, 'name'):
        query.order('name')
    qs = query.fetch(500)
    result_list = []
    if qs:
        for instance in qs:
            if instance.status == 1:
                if hasattr(modelClass, 'name'):
                    value = (instance.key().id(), instance.name)
                else:
                    value = (instance.key().id(), str(instance))
                result_list.append(value)
    putSearchOptionCache(className, result_list)
    return result_list

def getReferenceOptions(modelClass):
    return getSearchOptions(modelClass)

class BaseStatusModel(BaseModel):
    status = db.IntegerProperty(choices=set(STATUS_CHOICES_DICT.keys()), default=1)
    created = db.DateTimeProperty(auto_now_add=True)
    modified = db.DateTimeProperty(auto_now=True)   

    def display_status(self):
        return STATUS_CHOICES_DICT[self.status]

    @staticmethod
    def input_status(listValue):
        return getDictSelectHtml(STATUS_CHOICES_DICT, int(listValue[0]), 'status')

class BaseSearchModel(BaseStatusModel):
    """act as a SearchableModel"""
    def _populate_internal_entity(self):
        """Wraps db.Model._populate_internal_entity() and injects SearchableEntity."""
        if self._entity is not None:                                    #I think it's a bug for SearchableModel: 
            if not isinstance(self._entity, search.SearchableEntity):   #    self._entity must always be SearchableEntity type, or modified data will make 
                self._entity = search.SearchableEntity(self._entity)    #     _FULL_TEXT_INDEX_PROPERTY's content invalid
        return db.Model._populate_internal_entity(self, _entity_class=search.SearchableEntity)

    @classmethod
    def all(cls):
        """Returns a SearchableModel.Query for this kind."""
        return search.SearchableModel.Query(cls)

