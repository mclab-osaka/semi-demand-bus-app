#-*- coding: utf-8
from django.urls import path
from rest_framework import routers
from rest_framework_jwt.views import obtain_jwt_token, refresh_jwt_token
from .views import *
# from .views import ReservationViewSet, UserViewSet, DriverViewSet, StopViewSet, StopLocationViewSet, MessageViewSet, InformationViewSet, StopOutageViewSet, BusLocationViewSet

urlpatterns = [
    path('user/', UserDetailView.as_view()),
    path('reservation/', ReservationListView.as_view()),
    path('reservation/<int:reservation_pk>', ReservationDetailView.as_view()),
    path('alert/', AlertListView.as_view()),
    path('message/', MessageListView.as_view()),
    path('bus-location/', BusLocationListView.as_view()),
    path('voice-message/', VoiceMessageListView.as_view()),
    path('operating-time-window/', OperatingTimeWindowListView.as_view()),
    path('stop-location/', StopLocationListView.as_view()),
    path('reservable-stop/<int:area_pk>/<slug:bus_type>', ReservableStopListView.as_view()),
    path('token/', obtain_jwt_token),
    path('refresh-token/', refresh_jwt_token)
]
