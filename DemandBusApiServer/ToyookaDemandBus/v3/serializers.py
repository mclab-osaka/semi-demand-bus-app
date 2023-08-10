# -*- coding: utf-8
from rest_framework import serializers
from datetime import date, timedelta
from .models import *


class UserSerializer(serializers.ModelSerializer):

    class Meta:
        model = User
        fields = ('id','username', 'first_name', 'last_name','email', 'area', 'is_driver', 'last_login')
        ordering = ("id",)


class OperatingTimeWindowSerializer(serializers.ModelSerializer):

    class Meta:
        model = OperatingTimeWindow
        fields = ('id', 'display_name', 'start_time', 'end_time', 'bus_type')
        ordering = ("display_name",)


class StopTimeSerializer(serializers.ModelSerializer):

    operating_time_window = OperatingTimeWindowSerializer()
    display_time = serializers.SerializerMethodField("convert_display_time")

    def convert_display_time(self, stop_time):
        return "{}時{}分".format(stop_time.time.hour, stop_time.time.minute)

    class Meta:
        model = StopTime
        fields = ('id', 'time', "display_time", 'stop_location', 'reservation_enable_time', 'reservation_disable_time', 'operating_time_window')
        ordering = ("time",)


class StopLocationSerializer(serializers.ModelSerializer):

    class Meta:
        model = StopLocation
        fields = ('id', 'name', 'area', 'longitude', 'latitude',)
        ordering = ("id",)


class ReservableStopSerializer(serializers.ModelSerializer):

    class Meta:
        model = StopLocation
        fields = ('id', 'name', 'area', 'longitude', 'latitude', 'stoptimes')
        ordering = ("id",)


class AreaSerializer(serializers.ModelSerializer):

    class Meta:
        model = Area
        fields = ('id', 'name', 'is_destination')
        ordering = ("id",)


class StopOutageSerializer(serializers.ModelSerializer):
    
    class Meta:
        model = StopOutage
        fields = ('id', 'stop_location', 'start_date', 'end_date')
        ordering = ("id",)


class NestedReservationSerializer(serializers.ModelSerializer):

    stop_time = StopTimeSerializer(read_only=True)
    stop_location = StopLocationSerializer(read_only=True)
    user = UserSerializer(read_only=True)

    display_date = serializers.SerializerMethodField("convert_display_date")

    def convert_display_date(self, reservation):
        today = date.today()
        prefix = ""
        if reservation.date == today:
            prefix = "本日"
        elif reservation.date == (today + timedelta(days=1)):
            prefix = "明日"
        elif reservation.date == (today + timedelta(days=2)):
            prefix = "明後日"

        return "{}{}月{}日".format(prefix, reservation.date.month, reservation.date.day)
        
    
    class Meta:
        model = Reservation
        fields = ('id', 'stop_location', 'stop_time', 'user', 'state', 'date', 'display_date', 'off_location_longitude', 'off_location_latitude', 'off_time', 'on_time', 'num_of_people')
        ordering = ("stop_time__time",)


class ReservationSerializer(serializers.ModelSerializer):

    class Meta:
        model = Reservation
        fields = ('id', 'stop_location', 'stop_time', 'user', 'state', 'date', 'off_location_longitude', 'off_location_latitude', 'off_time', 'on_time', 'num_of_people')
 

class MessageSerializer(serializers.ModelSerializer):

    class Meta:
        model = Message
        fields = ('id', 'user', 'area','for_all_users', 'start_datetime', 'end_datetime', 'text')
        ordering = ("start_datetime",)


class VoiceMessageSerializer(serializers.ModelSerializer):

    class Meta:
        model = VoiceMessage
        fields = ('id', 'start_datetime', 'end_datetime', 'text', "speaked")
        ordering = ("start_datetime",)


class AlertSerializer(serializers.ModelSerializer):

    class Meta:
        model = Message
        fields = ('id', 'start_datetime', 'end_datetime', 'text')
        ordering = ("start_datetime",)

class BusLocationSerializer(serializers.ModelSerializer):

    class Meta:
        model = BusLocation
        fields = ('id', 'latitude', 'longitude', 'timestamp')
        ordering = ("timestamp")


# 
# class StopOutageSerializer(serializers.ModelSerializer):
#     
#     class Meta:
#         model = StopOutage
#         fields = ('id', 'stop_location', 'start_date', 'end_date')
# 
# 
# class ReservationSerializer(serializers.ModelSerializer):
#     
#     class Meta:
#         model = Reservation
#         fields = ('id', 'stop_location', 'stop_time', 'user', 'state', 'date', 'driver', 'operating_time_window', 'off_location_longitude', 'off_location_latitude', 'off_time', 'on_time', 'num_of_people')
# 
# 
# 
# 
# class AlertSerializer(serializers.ModelSerializer):
# 
#     class Meta:
#         model = Alert
#         fields = ('id', 'start_date', 'end_date', 'text', 'area')
# 
# 
# class BusLocationSerializer(serializers.ModelSerializer):
# 
#     class Meta:
#         model = BusLocation
#         fields = ('id', 'latitude', 'longitude', 'timestamp')


# class NestedStopLocationSerializer(serializers.ModelSerializer):
# 
#     stops = StopSerializer(many=True, read_only=True)
#     class Meta:
#         model = StopLocation
#         fields = ('id', 'name', 'area', 'longitude', 'latitude','stops')
# 
# 
# 
# 
# class NestedReservationSerializer(serializers.ModelSerializer):
#     """ For Get Method"""
#     
#     stop_location = StopLocationSerializer()
#     stop = StopSerializer()
#     user = UserSerializer()
# 
#     class Meta:
#         model = Reservation
#         fields = ('id', 'stop_location', 'stop', 'user', 'state', 'date', 'driver', 'bus_type', 'num_of_people')
