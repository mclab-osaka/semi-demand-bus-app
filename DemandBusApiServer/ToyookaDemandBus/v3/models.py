# Create your models here.
# -*- coding: utf-8 -*-
from django.db import models
from django.contrib.auth.models import AbstractUser
from django.contrib.auth.base_user import BaseUserManager
from django.utils.timezone import now

STATE_CANCELED = "canceled"
STATE_RESERVED = "reserved"
STATE_ABSENCE = "absence"
STATE_ON_BUS = "on_bus"
STATE_OFF_BUS = "off_bus"


STATE_SET = (
    (STATE_CANCELED, "canceled(破棄)"), 
    (STATE_RESERVED, "reserved(予約完了)"), 
    (STATE_ON_BUS, "on_bus(乗車中)"), 
    (STATE_OFF_BUS, "off_bus(降車完了)"),
    (STATE_ABSENCE, "absence(不在)"),)


BUS_GOING = "going"
BUS_RETURNING = "returning"
BUS_TYPE = ((BUS_GOING, "going(往路)"), (BUS_RETURNING, "returning(復路)"))


class User(AbstractUser):

    area = models.ForeignKey("Area", blank=True, null=True, on_delete=models.SET_NULL, related_name="users")
    is_driver = models.BooleanField(default=False)

    def __str__(self):
        return self.username + " (" + self.first_name + " " + self.last_name + ")"

    class Meta:
        verbose_name = "ユーザ"
        verbose_name_plural = "ユーザリスト"


class Area(models.Model):

    name = models.CharField(max_length=1024)
    is_destination = models.BooleanField(default=False)

    def __str__(self):
        return self.name

    class Meta:
        verbose_name = "地区"
        verbose_name_plural = "地区リスト"


class OperatingTimeWindow(models.Model):

    display_name = models.CharField(max_length=1024)
    start_time = models.TimeField(auto_now=False)
    end_time = models.TimeField(auto_now=False)
    bus_type = models.CharField(choices=BUS_TYPE, default=BUS_GOING, max_length=32)

    def __str__(self):
        return self.display_name

    class Meta:
        verbose_name = "運行時間"
        verbose_name_plural = "運行時間リスト"


class StopLocation(models.Model):

    name = models.CharField(max_length=1024)
    area = models.ForeignKey("Area", blank=True, null=True, on_delete=models.CASCADE, related_name="stoplocations")
    longitude = models.FloatField()
    latitude = models.FloatField()

    def __str__(self):
        return self.name

    class Meta:
        verbose_name = "停留所"
        verbose_name_plural = "停留所リスト"


class StopTime(models.Model):

    stop_location = models.ForeignKey("StopLocation", on_delete=models.CASCADE, related_name="stoptimes")
    time = models.TimeField(auto_now=False)
    reservation_enable_time = models.TimeField(auto_now=False)
    reservation_disable_time = models.TimeField(auto_now=False)
    operating_time_window = models.ForeignKey("OperatingTimeWindow", blank=True, null=True, on_delete=models.CASCADE, related_name="stoptimes")

    def __str__(self):
        return "{} ({})".format(self.stop_location.name, self.time)

    class Meta:
        verbose_name = "停留時刻"
        verbose_name_plural = "停留時刻リスト"



class StopOutage(models.Model):

    stop_location = models.ForeignKey("StopLocation", on_delete=models.CASCADE, related_name="stopoutages")
    start_date = models.DateField(auto_now=False)
    end_date = models.DateField(auto_now=False)

    def __str__(self):
        return "{}運休".format(self.stop_location.name)

    class Meta:
        verbose_name = "運休"
        verbose_name_plural = "運休リスト"



class Reservation(models.Model):

    user = models.ForeignKey("User", related_name="reservations", on_delete=models.CASCADE)
    stop_location = models.ForeignKey("StopLocation", related_name="reservations", null=True, on_delete=models.SET_NULL)
    stop_time = models.ForeignKey("StopTime", related_name="reservations", null=True, on_delete=models.SET_NULL)
    state = models.CharField(choices=STATE_SET, default=STATE_RESERVED, max_length=128)

    date = models.DateField(auto_now=False)
    created = models.DateTimeField(auto_now_add=True)
    updated = models.DateTimeField(auto_now=True)

    off_location_longitude = models.FloatField(default=0.0)
    off_location_latitude = models.FloatField(default=0.0)
    off_time = models.TimeField(blank=True, null=True)
    on_time = models.TimeField(blank=True, null=True)
    num_of_people = models.IntegerField(default=1)

    def __str__(self):
        return "{}".format(self.id)

    class Meta:
        verbose_name = "予約"
        verbose_name_plural = "予約リスト"


class Message(models.Model):

    user = models.ForeignKey("User", blank=True, null=True, on_delete=models.SET_NULL)
    area = models.ForeignKey("Area", blank=True, null=True, on_delete=models.SET_NULL)
    for_all_users = models.BooleanField(default=False)

    start_datetime = models.DateTimeField(auto_now=False, default=now)
    end_datetime = models.DateTimeField(auto_now=False, default=now)
    text = models.CharField(max_length=1024)

    def __str__(self):
        return "{}({})".format(self.text, self.user)

    class Meta:
        verbose_name = "メッセージ"
        verbose_name_plural = "メッセージリスト"


class VoiceMessage(models.Model):

    user = models.ForeignKey("User", blank=True, null=True, on_delete=models.SET_NULL)
    area = models.ForeignKey("Area", blank=True, null=True, on_delete=models.SET_NULL)
    for_all_users = models.BooleanField(default=False)

    start_datetime = models.DateTimeField(auto_now=False, default=now)
    end_datetime = models.DateTimeField(auto_now=False, default=now)
    text = models.CharField(max_length=1024)

    speaked = models.BooleanField(default=False)

    def __str__(self):
        return "{}({})".format(self.text, self.user)

    class Meta:
        verbose_name = "音声メッセージ"
        verbose_name_plural = "音声メッセージリスト"



class Alert(models.Model):

    user = models.ForeignKey("User", blank=True, null=True, on_delete=models.SET_NULL)
    area = models.ForeignKey("Area", blank=True, null=True, on_delete=models.SET_NULL)
    for_all_users = models.BooleanField(default=False)

    start_datetime = models.DateTimeField(auto_now=False, default=now)
    end_datetime = models.DateTimeField(auto_now=False, default=now)
    text = models.CharField(max_length=1024)

    def __str__(self):
        return "{}({})".format(self.text, self.user)

    class Meta:
        verbose_name = "アラート"
        verbose_name_plural = "アラートリスト"


class BusLocation(models.Model):

    timestamp = models.DateTimeField(auto_now=True)
    longitude = models.FloatField(default=0.0)
    latitude = models.FloatField(default=0.0)

    def __str__(self):
        return "{} ({},{})".format(self.timestamp, self.longitude, self.latitude)

    class Meta:
        verbose_name = "位置ログ"
        verbose_name_plural = "位置ログ"
