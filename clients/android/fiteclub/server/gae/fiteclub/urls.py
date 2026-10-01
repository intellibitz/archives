import logging
from django.conf.urls.defaults import *
from django.conf import settings

logging.info('urls.py loaded!')

if settings.USE_I18N:
    i18n_view = 'django.views.i18n.javascript_catalog'
else:
    i18n_view = 'django.views.i18n.null_javascript_catalog'

urlpatterns = patterns('',
    (r'^jsi18n/$', i18n_view, {'packages': 'django.conf'}),

    (r'^webedit/([a-z]+)/add/$', 'webui.views.add_page'),
    (r'^webedit/([a-z]+)/(\d+)/$', 'webui.views.edit_page'),
    (r'^webedit/([a-z]+)/$', 'webui.views.editlist_page'),

    (r'^api/get/profile/$', 'webapi.views.get_profile'),
    (r'^api/set/profile/$', 'webapi.views.set_profile'),

    (r'^secure/passwd/$', 'webapi.views.set_password'),
)
