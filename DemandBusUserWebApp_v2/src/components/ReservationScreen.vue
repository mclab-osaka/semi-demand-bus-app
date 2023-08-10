<template>
<div id="reservation-screen">

    <div class="row top-message">
        <p>{{topMessage}}</p>
    </div>

    <!-- 1 Selecting Date -->
    <div class="button-list" v-if="selectingSequenceNumber==1">
        <div class="row center" v-for="stopDate in selectedStopLocation.available_days" :key="stopDate.display_date">
            <button class="col m5 s12 waves-effect waves-light btn-large center" v-on:click="selectDate(stopDate)"><span>{{stopDate.display_stop_date}}</span> </button>
        </div>
    </div>

    <!-- 2 Selecting Time -->
    <div class="button-list" v-if="selectingSequenceNumber==2">
        <div class="row center" v-for="stopTime in selectedStopDate.stoptimes" :key="stopTime.display_time">
            <button class="col m12 s12 waves-effect waves-light btn-large center" v-on:click="selectTime(stopTime)"><span>{{stopTime.display_time}} 発</span></button>
        </div>
    </div>

    <!-- reset button -->
    <div class="cancel-button-wrapper">
        <div class="row center">
        <button class="btn-large red" v-on:click="reset" :disabled="selectingSequenceNumber >= 3">
            <span>最初からやりなおす</span>
        </button>
        </div>
    </div>

</div>
</template>

<script>
import api from '../api'
export default {
    data: function() {
        return {
            selectedStopLocation: null,
            selectedStopDate: null,
            selectedNumOfPeople: 1,
            selectingSequenceNumber: 0,
            errorMessage: null,
        }
    },
    methods: {
        load_data: function() {
            console.log("load data");
            api.getUserAreaId((success, areaId) => {
                if (success) {
                    this.bus_type = "going";
                    if (!areaId) { areaId = 1; }

                    api.getAvailableStops(areaId, this.bus_type, (success, available_stops) => {
                        if (success) {
                            console.log(areaId, this.bus_type, available_stops);
                            if (available_stops.length == 0) {
                                this.errorMessage = "このユーザーには地区が設定されていないか，地区に停留所が存在しません";
                            } else {
                                this.selectedStopLocation = available_stops[0];
                                this.selectingSequenceNumber = 1;
                            }

                        } else {
                            console.log("failed for loading available stops");
                            this.errorMessage = "利用可能な停留所が見つかりませんでした"
                        }
                    });
                } else {
                    this.errorMessage = "このユーザーには地区が設定されていません"
                }
            });
        },
        selectDate: function(selectedStopDate) {
            this.selectedStopDate = selectedStopDate;
            this.selectingSequenceNumber = 2;
        },
        selectTime: function(selectedStopTime) {
            // ToDo: Stop Reset Timer;
            this.selectedStopTime = selectedStopTime;
            this.selectedNumOfPeople = 1;
            this.selectingSequenceNumber = 3;
            this.reserve();
        },
        reserve: function() {
            api.postReservation(
                this.selectedStopLocation.id, 
                this.selectedStopTime.id, 
                this.selectedStopDate.stop_date, 
                this.selectedNumOfPeople,
                (success) => {
                    setTimeout(() => {
                        this.$router.replace("/")
                    }, 2*1000);
            });
        },
        reset: function() {
            this.$router.replace("/")
        },
    },
    computed: {
        topMessage: function() {
            if (this.errorMessage) {
                return this.errorMessage;
            } else if (this.selectingSequenceNumber == 0) {
                return "読み込み中です。しばらくお待ちください。"
            } else if (this.selectingSequenceNumber == 1 && this.selectedStopLocation.available_days.length > 0) {
                return "何日の " + this.selectedStopLocation.name + " 発のバスを予約しますか"
            } else if (this.selectingSequenceNumber == 1 && this.selectedStopLocation.available_days.length <= 0) {
                return "現在予約可能なバスはありません。時刻表・運休等をご確認ください。"
            } else if (this.selectingSequenceNumber == 2 && this.selectedStopDate.stoptimes.length > 0) {
                return this.selectedStopDate.display_stop_date + " " + this.selectedStopLocation.name + " の " + "何時発のバスを予約しますか？" 
            } else if (this.selectingSequenceNumber == 2 && this.selectedStopDate.stoptimes.length <= 0) {
                return this.selectedStopDate.display_stop_date + "で予約可能なバスはありません。時刻表・運休等をご確認ください。"
            } else if (this.selectingSequenceNumber >= 3) {
                return this.selectedStopLocation.name + " " + this.selectedStopDate.display_stop_date + " " + this.selectedStopTime.display_time + "で予約中です。しばらくお待ちください。"
            }
        }
    },
    mounted() {
        this.load_data();
    }
}
</script>

<style scoped>
#reservation-screen {
    margin: 0px;
    padding: 0px;
    font-size: 24px;
}

.top-message {
    margin: 8px;
    font-size: 28px;
    font-weight: bold;
}

button {
    font-size: 30px;
    font-weight: bold;
}

.button-list {
    margin: 8px;
}

.cancel-button-wrapper {
    position: absolute;
    width: 100%;
    bottom: 0px;
}

.cancel-button-wrapper button {
    margin: 0px auto 0px auto;
}

</style>


