"""
WSGI config for ToyookaDemandBus project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/2.0/howto/deployment/wsgi/
"""
import os
import sys
import site

from django.core.wsgi import get_wsgi_application

site.addsitedir('/home/ubuntu/ToyookaAppApiServer-v4/ENV/lib/python3.5/site-packages')
sys.path.append('/home/ubuntu/ToyookaAppApiServer-v4/ToyookaDemandBus')
sys.path.append('/home/ubuntu/ToyookaAppApiServer-v4/ToyookaDemandBus/ToyookaDemandBus')

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "ToyookaDemandBus.settings")

application = get_wsgi_application()
