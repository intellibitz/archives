import logging
from datetime import datetime, timedelta
from math import *

logging.info('util.common.py loaded!')

MAXFETCH = 500
BASEURL = 'http://fyteklub.appspot.com/'
DATETIME_START = datetime.utcfromtimestamp(0)
MAX_DETAIL_COUNT = 500

def getDateTimeValue(value):
    '''translate datetime from long to datetime'''
    value = float(value) / 1000
    return datetime.utcfromtimestamp(value)

def getJavaTimeStamp(data):
    delta = data - DATETIME_START
    return delta.days*86400000 + delta.seconds*1000 + int(delta.microseconds/1000)
    
