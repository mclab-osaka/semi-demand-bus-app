# -*- coding:utf-8
from rest_framework.permissions import BasePermission

class IsAuthenticated(BasePermission):
    
    def has_permission(self, request, view):
        return request.user and request.user.is_authenticated
