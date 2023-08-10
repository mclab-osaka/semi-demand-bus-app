"""
WSGI config for ToyookaDemandBus project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/2.0/howto/deployment/wsgi/
"""


import os
import sys
import site

site.addsitedir('/home/ubuntu/ToyookaDemandBus/ENV/lib/python3.5/site-packages')
sys.path.append('/home/ubuntu/ToyookaDemandBus')
sys.path.append('/home/ubuntu/ToyookaDemandBus/ToyookaDemandBus')

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "ToyookaDemandBus.settings")

from django.core.wsgi import get_wsgi_application
application = get_wsgi_application()
