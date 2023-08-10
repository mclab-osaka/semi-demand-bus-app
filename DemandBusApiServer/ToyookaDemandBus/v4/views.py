import django_filters
from datetime import datetime, timedelta, date
from django.shortcuts import render
from django.http import Http404
from django.db.models import Q
from .serializers import *
from .permissions import *
from .models import *
from rest_framework import permissions
from rest_framework.response import Response
from rest_framework import generics, filters
from rest_framework.views import APIView
from rest_framework import status

WEEK_DAYS = ["月","火","水","木","金","土","日"]


class UserDetailView(APIView):

    def get(self, request, format=None):

        if not request.user.is_authenticated:
            print("not authorized")
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        serializer = UserSerializer(request.user, {'last_login': datetime.now()})
        if serializer.is_valid():
            serializer.save()
        else:
            serializer = UserSerializer(request.user)

        return Response(serializer.data)


    def patch(self, request, format=None):

        if not request.user.is_authenticated:
            print("not authorized")
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        data = request.data
        data['last_login'] = datetime.now()

        serializer = UserSerializer(request.user, data=data, partial=True)
        if not serializer.is_valid():
            return Response(status=status.HTTP_400_BAD_REQUEST)

        serializer.save()
        return Response(serializer.data)


class OperatingTimeWindowListView(generics.ListAPIView):

    permission_classes = (IsAuthenticated,)
    queryset = OperatingTimeWindow.objects.all()
    serializer_class = OperatingTimeWindowSerializer


class StopLocationListView(generics.ListAPIView):

    permission_classes = (IsAuthenticated,)
    queryset = StopLocation.objects.all()
    serializer_class = StopLocationSerializer
    filter_fields = ("area", )


def is_day_available(target_day, stopoutages):
    # 曜日チェック
    # if target_day.weekday() not in [0, 2, 4]:
    #     print(target_day, "Not Available Week Day")
    #     return False
    weekly_out_of_services = WeeklyOutOfService.objects.all()
    weekly_out_of_service_days = [ woos.day_of_week for woos in weekly_out_of_services ]
    if target_day.weekday() in weekly_out_of_service_days:
        return False

    for stop_outage in stopoutages:
        if stop_outage.start_date <= target_day <= stop_outage.end_date:
            print(target_day, "In StopOutage")
            return False
    return True


class ReservableStopListView(generics.ListAPIView):

    permission_classes = (IsAuthenticated,)

    def get(self, request, area_pk, bus_type):

        print("bus_type", bus_type)
        print("area_pk", area_pk)

        if bus_type == "returning":
            stop_locations = StopLocation.objects.filter(area__is_destination=True)
        else:
            stop_locations = StopLocation.objects.filter(area=area_pk)

        serializer = StopLocationSerializer(stop_locations, many=True)

        print("stop_location length: ", len(stop_locations))

        for i, stop_location in enumerate(stop_locations):

            print("stop_location", i, stop_location)

            today = date.today()
            days = [
                ("今日", today),
                ("明日", today+timedelta(days=1)),
                ("明後日", today+timedelta(days=2)),
                ("", today+timedelta(days=3)),
                ("", today+timedelta(days=4)),
                ("", today+timedelta(days=5)),
                ("", today+timedelta(days=6)),
            ]

            available_days_list = []
            for day_name, target_day in days:

                if not is_day_available(target_day, stop_location.stopoutages.all()):
                    print(day_name, target_day, "is not available")
                    continue

                stop_time_list = []
                for stop_time in stop_location.stoptimes.all():

                    print()
                    print("user request bus_type: " + str(bus_type))
                    print("stop_time.operating_time_window.bus_type: " + str(stop_time.operating_time_window.bus_type))

                    if not stop_time.operating_time_window.bus_type == bus_type:
                        continue

                    reservation_disable_datetime = datetime(target_day.year, target_day.month, target_day.day, stop_time.reservation_disable_time.hour, stop_time.reservation_disable_time.minute, stop_time.reservation_disable_time.second)
                    if reservation_disable_datetime <= datetime.now():
                        continue

                    stop_time_serializer = StopTimeSerializer(stop_time)
                    stop_time_list.append(stop_time_serializer.data)

                if not stop_time_list:
                    continue

                available_days_list.append({
                    "stop_date": target_day,
                    "display_stop_date": "{} {}月{}日（{}）".format(day_name, target_day.month, target_day.day, WEEK_DAYS[target_day.weekday()]),
                    "stoptimes": stop_time_list
                })

            serializer.data[i]["available_days"] = available_days_list

        return Response(serializer.data)

    # permission_classes = (HasAccessPermission,)

class ReservationListView(generics.ListAPIView):
    # ドライバーからのアクセスの場合，今日の全予約・今日の各タイムウィンドウごとの予約・明日以降全予約を返す
    # ユーザからのアクセスの場合，そのユーザの行きの予約・帰りの予約を返す

    permission_classes = (IsAuthenticated,)

    def get(self, request):

        # ドライバーの場合は，[ {"OperatingTimeWindow": {}, "reservations": [], } ]
        if request.user.is_staff or request.user.is_driver:
            print("driver, staff")
            response = []

            # 1. 今日の全予約 と 各タイムウィンドウの予約を response配列に入れる
            today = date.today()
            today_reservations = Reservation.objects.filter(date=today).filter(Q(state='reserved') | Q(state='on_bus')).all()

            operating_time_windows = OperatingTimeWindow.objects.order_by("pk").all()
            res_in_today = []
            for operating_time_window in operating_time_windows:
                res_in_this_otw = []

                for reservation in today_reservations:
                    if reservation.stop_time and reservation.stop_time.operating_time_window == operating_time_window:
                        res_serializer = NestedReservationSerializer(reservation)
                        res_in_this_otw.append(res_serializer.data)

                response.append({
                    "time_window_name": "本日: "+operating_time_window.display_name,
                    "reservations": sorted(res_in_this_otw, key=lambda res : res["stop_time"]["time"])
                })
                res_in_today += res_in_this_otw

            response.insert(0, {
                "time_window_name": "本日の全予約",
                "reservations": sorted(res_in_today, key=lambda res: res["stop_time"]["time"])
            })

            # 2. 明日以降の全予約をresponse配列の最後に入れる
            tomorrow_reservations = Reservation.objects.filter(date__gt=today).filter(Q(state='reserved') | Q(state='on_bus')).all()
            tomorrow_res_serializer = NestedReservationSerializer(tomorrow_reservations, many=True)
            response.append({
                "time_window_name": "明日以降の全予約",
                "reservations": sorted(tomorrow_res_serializer.data, key=lambda res: res["date"])
            })
            return Response(response)

        else:
            # response = { "going": None, "returning": None }
            # going_reservations = Reservation.objects.filter(user=request.user, stop_time__operating_time_window__bus_type="going", state="reserved").all()
            # returning_reservations = Reservation.objects.filter(user=request.user, stop_time__operating_time_window__bus_type="returning", state="reserved").all()

            # if going_reservations:
            #     going_reservation_serializer = NestedReservationSerializer(going_reservations[0])
            #     response['going'] = going_reservation_serializer.data

            # if returning_reservations:
            #     returning_reservation_serializer = NestedReservationSerializer(returning_reservations[0])
            #     response['returning'] = returning_reservation_serializer.data
            response = []
            reservations = Reservation.objects.filter(user=request.user, state="reserved").all()
            reservation_serializer = NestedReservationSerializer(reservations, many=True)
            response = reservation_serializer.data

            return Response(response)

    def post(self, request):

        request.data["user"] = request.user.pk
        serializer = ReservationSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            # TODO: PUSH
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ReservationDetailView(APIView):

    permission_classes = (IsAuthenticated,)

    def get_object(self, pk):
        print(pk)
        try:
            return Reservation.objects.get(pk=pk)
        except:
            raise Http404

    def get(self, request, reservation_pk):

        if not (request.user and request.user.is_authenticated):
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        reservation = self.get_object(reservation_pk)
        
        if not (request.user == reservation.user or request.user.is_driver or request.user.is_staff):
            return Response(status=status.HTTP_401_UNAUTHORIZED)
        serializer = NestedReservationSerializer(reservation)

        return Response(serializer.data)

    def patch(self, request, reservation_pk):

        if not (request.user and request.user.is_authenticated):
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        # TODO : push notification

        reservation = self.get_object(reservation_pk)
        
        if not (request.user == reservation.user or request.user.is_staff or request.user.is_driver):
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        serializer = ReservationSerializer(reservation, data=request.data, partial=True)
        if not serializer.is_valid():
            return Response(status=status.HTTP_400_BAD_REQUEST)
        serializer.save()

        return Response(serializer.data)


class MessageListView(APIView):
    # そのユーザにアクティブなメッセージを返す

    def get(self, request):

        if not (request.user and request.user.is_authenticated):
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        now = datetime.now()
        messages = Message.objects.filter(
            (Q(user=request.user) | Q(area=request.user.area) | Q(for_all_users=True))).filter(start_datetime__lte=now).filter(end_datetime__gte=now).all()
        serializer = MessageSerializer(messages, many=True)

        if messages:
            response = serializer.data[-1]
        else:
            response = None

        return Response(response)

    def post(self, request):

        # if not (request.user and request.user.is_authenticated):
            #return Response(status=status.HTTP_401_UNAUTHORIZED)
        print(request.data)
        if "start_datetime" not in request.data:
            request.data["start_datetime"] = datetime.now()

        if "end_datetime" not in request.data:
            print("end_datetime not in")
            now = datetime.now()
            end_datetime = datetime(now.year, now.month, now.day, hour=23,minute=59,second=59)
            request.data["end_datetime"] = end_datetime

        serializer = MessageSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(status=status.HTTP_400_BAD_REQUEST)
        serializer.save()

        return Response(serializer.data)



class AlertListView(APIView):
    # そのユーザにアクティブなアラートを返す

    def get(self, request):

        if not (request.user and request.user.is_authenticated):
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        now = datetime.now()
        alerts = Alert.objects.filter(
            (Q(user=request.user) | Q(area=request.user.area) | Q(for_all_users=True))).filter(start_datetime__lte=now).filter(end_datetime__gte=now).all()
        serializer = AlertSerializer(alerts, many=True)

        if alerts:
            response = serializer.data[-1]
        else:
            response = None

        return Response(response)


    def post(self, request):

        if not (request.user and request.user.is_authenticated):
            return Response(status=status.HTTP_401_UNAUTHORIZED)

        serializer = AlertSerializer(reservation, data=request.data)
        if not serializer.is_valid():
            return Response(status=status.HTTP_400_BAD_REQUEST)
        serializer.save()

        return Response(serializer.data)


# 
# class VoiceMessageListView(APIView):
# 
#     def get(self, request):
# 
#         if not (request.user and request.user.is_authenticated):
#             return Response(status=status.HTTP_401_UNAUTHORIZED)
# 
#         now = datetime.now()
#         alerts = VoiceMessage.objects.filter(
#             (Q(user=request.user) | Q(area=request.user.area) | Q(for_all_users=True))).filter(start_datetime__lte=now).filter(end_datetime__gte=now).filter(speaked=False).all()
#         serializer = VoiceMessageSerializer(alerts, many=True)
# 
#         if alerts:
#             response = serializer.data[0]
#         else:
#             response = None
# 
#         return Response(response)
# 
# 
#     def post(self, request):
# 
#         if not (request.user and request.user.is_authenticated):
#             return Response(status=status.HTTP_401_UNAUTHORIZED)
# 
#             serializer = VoiceMessageSerializer(reservation, data=request.data)
#         if not serializer.is_valid():
#             return Response(status=status.HTTP_400_BAD_REQUEST)
#         serializer.save()
# 
#         return Response(serializer.data)

class BusLocationListView(generics.CreateAPIView):

    permission_classes = (IsAuthenticated,)
    queryset = BusLocation.objects.all()
    serializer_class = BusLocationSerializer 
