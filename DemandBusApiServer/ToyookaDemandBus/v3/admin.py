from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from django.contrib.auth.forms import UserChangeForm, UserCreationForm
from django.utils.translation import ugettext_lazy as _

from .models import *

class MyUserChangeForm(UserChangeForm):
    class Meta:
        model = User
        fields = '__all__'


class MyUserCreationForm(UserCreationForm):

    class Meta:
        model = User
        fields = tuple()


class MyUserAdmin(UserAdmin):
    fieldsets = (
            (None, {'fields': ('username', 'password',)}),
            (_('Personal info'), {'fields': ('first_name', 'last_name', 'area', 'last_login',)}),
            (_('Permissions'), {'fields': ('is_active','is_driver', 'is_staff', 'is_superuser',
                                           'groups', 'user_permissions')}),
            (_('Important dates'), {'fields': ('last_login', 'date_joined')}),
            )
    add_fieldsets = (
            (None, {
             'classes': ('wide',),
             'fields': ('username', 'password1', 'password2'),
             }),
            )
    form = MyUserChangeForm
    add_form = MyUserCreationForm
    list_display = ("pk", 'username', 'first_name', 'last_name','area','last_login',"is_driver", 'is_staff')
    list_filter = ('is_staff', 'is_superuser', 'is_active', 'groups')
# egister your models here.

class AreaAdmin(admin.ModelAdmin):

    list_display = ("pk", "name", "is_destination")
    

class OperatingTimeWindowAdmin(admin.ModelAdmin):
    
    list_display = ("pk","display_name","start_time", "end_time", "bus_type")
    list_filter = ("bus_type",)
    ordering = ("display_name",)


class StopLocationAdmin(admin.ModelAdmin):
    
    list_display = ("pk", "name", "area", "longitude", "latitude")
    list_filter = ("area",)
    ordering = ("id",)


class StopOutageAdmin(admin.ModelAdmin):
    
    list_display = ("stop_location","start_date", "end_date",)
    ordering = ("id",)


class ReservationAdmin(admin.ModelAdmin):
    
    list_display = ("pk", "date","user","stop_location","stop_time", "state","created", "updated", ) 
    ordering = ("-date",)


class MessageAdmin(admin.ModelAdmin):
    
    list_display = ("pk", "user", "area", "for_all_users", "start_datetime", "end_datetime", "text",)
    ordering = ("start_datetime",)


class VoiceMessageAdmin(admin.ModelAdmin):
    
    list_display = ("pk", "user", "area", "for_all_users", "start_datetime", "end_datetime", "text", "speaked")
    ordering = ("start_datetime",)


class AlertAdmin(admin.ModelAdmin):
    
    list_display = ("pk", "user", "area", "for_all_users", "start_datetime", "end_datetime", "text",)
    ordering = ("start_datetime",)

class StopTimeAdmin(admin.ModelAdmin):
    
    list_display = ("pk", "stop_location", "time", "reservation_enable_time", "reservation_disable_time", "operating_time_window",)

class BusLocationAdmin(admin.ModelAdmin):
    
    list_display = ("pk", "timestamp", "longitude", "latitude",)
 
    
admin.site.register(User, MyUserAdmin)
admin.site.register(Area, AreaAdmin)
admin.site.register(Message, MessageAdmin)
admin.site.register(VoiceMessage, VoiceMessageAdmin)
admin.site.register(Alert, AlertAdmin)
admin.site.register(Reservation, ReservationAdmin)
admin.site.register(StopLocation, StopLocationAdmin)
admin.site.register(StopOutage, StopOutageAdmin)
admin.site.register(StopTime, StopTimeAdmin)
admin.site.register(OperatingTimeWindow, OperatingTimeWindowAdmin)
admin.site.register(BusLocation, BusLocationAdmin)
