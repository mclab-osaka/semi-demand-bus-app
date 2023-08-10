from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from django.contrib.auth.forms import UserChangeForm, UserCreationForm
from django.utils.translation import ugettext_lazy as _

from .models import *

class ReadOnlyAdmin(admin.ModelAdmin):
    """
        ModelAdmin class that prevents modifications through the admin.
            The changelist and the detail view work, but a 403 is returned
                if one actually tries to edit an object.
                        Source: https://gist.github.com/aaugustin/1388243
                            """
    actions = None

# We cannot call super().get_fields(request, obj) because that method calls
    # get_readonly_fields(request, obj), causing infinite recursion. Ditto for
        # super().get_form(request, obj). So we  assume the default ModelForm.
    def get_readonly_fields(self, request, obj=None):
        return self.fields or [f.name for f in self.model._meta.fields]

    def has_add_permission(self, request):
        return False

    # Allow viewing objects but not actually changing them.
    def has_change_permission(self, request, obj=None):
        return (request.method in ['GET', 'HEAD'] and super().has_change_permission(request, obj))

    def has_delete_permission(self, request, obj=None):
        return False


class ReadOnlyAdminMixin(object):
    """Disables all editing capabilities."""

    def __init__(self, *args, **kwargs):
        super(ReadOnlyAdminMixin, self).__init__(*args, **kwargs)
        self.readonly_fields = self.model._meta.get_all_field_names()

    def get_actions(self, request):
        actions = super(ReadOnlyAdminMixin, self).get_actions(request)

        del actions["delete_selected"]
        return actions

    def has_add_permission(self, request):
        return False

    def has_delete_permission(self, request, obj=None):
        return False

    def save_model(self, request, obj, form, change):
        pass

    def delete_model(self, request, obj):
        pass

    def save_related(self, request, form, formsets, change):
        pass


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
    add_fieldsets = ()
    form = MyUserChangeForm
    add_form = MyUserCreationForm
    list_display = ("pk", 'username', 'first_name', 'last_name','area',"is_driver", 'is_staff')
    list_filter = ('is_staff', 'is_superuser', 'is_active', 'groups')
    readonly_fields = ("pk", 'username', 'first_name', 'last_name','area',"is_driver", 'is_staff')
 

    def has_add_permission(self, request):
        return False

class AreaAdmin(ReadOnlyAdmin):

    list_display = ("pk", "name", "is_destination")
    

class OperatingTimeWindowAdmin(ReadOnlyAdmin):
    
    list_display = ("pk","display_name","start_time", "end_time", "bus_type")
    list_filter = ("bus_type",)
    ordering = ("display_name",)


class StopLocationAdmin(ReadOnlyAdmin):
    
    list_display = ("pk", "name", "area", "longitude", "latitude")
    list_filter = ("area",)
    ordering = ("id",)


class StopOutageAdmin(ReadOnlyAdmin):
    
    list_display = ("stop_location","start_date", "end_date",)
    ordering = ("id",)


class ReservationAdmin(ReadOnlyAdmin):
    
    list_display = ("pk","user", "created", "updated", "num_of_people", "stop_location","stop_time", "state", "date",) 
    ordering = ("-pk",)


class MessageAdmin(ReadOnlyAdmin):
    
    list_display = ("pk", "user", "area", "for_all_users", "start_datetime", "end_datetime", "text",)
    ordering = ("-start_datetime",)


class VoiceMessageAdmin(ReadOnlyAdmin):
    
    list_display = ("pk", "user", "area", "for_all_users", "start_datetime", "end_datetime", "text", "speaked")
    ordering = ("start_datetime",)


class AlertAdmin(ReadOnlyAdmin):
    
    list_display = ("pk", "user", "area", "for_all_users", "start_datetime", "end_datetime", "text",)
    ordering = ("start_datetime",)

class StopTimeAdmin(ReadOnlyAdmin):
    
    list_display = ("pk", "stop_location", "time", "reservation_enable_time", "reservation_disable_time", "operating_time_window",)

class BusLocationAdmin(ReadOnlyAdmin):
    
    list_display = ("pk", "timestamp", "longitude", "latitude",)
 
    
# admin.site.register(User, MyUserAdmin)
admin.site.register(Area, AreaAdmin)
admin.site.register(Message, MessageAdmin)
# admin.site.register(VoiceMessage, VoiceMessageAdmin)
admin.site.register(Alert, AlertAdmin)
admin.site.register(Reservation, ReservationAdmin)
admin.site.register(StopLocation, StopLocationAdmin)
admin.site.register(StopOutage, StopOutageAdmin)
admin.site.register(StopTime, StopTimeAdmin)
admin.site.register(OperatingTimeWindow, OperatingTimeWindowAdmin)
# admin.site.register(BusLocation, BusLocationAdmin)
