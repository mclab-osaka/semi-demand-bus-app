<template>
    <div id="reservation" class="light-green lighten-5">

        <div class="description" v-if="loading">
            <span>読み込み中です。しばらくお待ちください。</span>
        </div>

        <div class="description" v-if="posting">
            <span>予約中です。しばらくお待ちください。</span>
        </div>

        <div class="row" v-if="selectingSequenceNumber==1 && !loading && !posting">
            <div class="col s12 description">
                <span>どこから、</span><span class="bold">{{displayBusType}}のバス</span><span>に乗りますか？</span>
            </div>
            <div class="col s6 m4" v-for="stopLocation in availableStops">
                <button class="waves-effect waves-light btn stop-location-button" v-on:click="selectLocation(stopLocation)"><span class="bold small-text">{{stopLocation.name}}</span></button>
            </div>
        </div>

        <div class="row" v-if="selectingSequenceNumber==2 && !loading && !posting">
            <div class="col s12 description" v-if="selectedStopLocation.available_days.length > 0">
                <span class="bold">{{selectedStopLocation.name}}発</span><span>の何日のバスに乗りますか？</span>
            </div>
            <div class="col s12 description" v-if="selectedStopLocation.available_days.length == 0">
                <span class="bold">{{selectedStopLocation.name}}</span><span>の予約可能なバスがありません</span>
            </div>
            <div class="col s12 m8 offset-m2" v-for="stopDate in selectedStopLocation.available_days">
                <button class="waves-effect waves-light btn-large center" v-on:click="selectDate(stopDate)"><span class="bold">{{stopDate.display_stop_date}}</span></button>
            </div>
        </div>

        <div class="row" v-if="selectingSequenceNumber==3 && !loading && !posting">
            <div class="col s12 description" v-if="selectedStopDate.stoptimes.length > 0">
                <span class="bold">{{selectedStopLocation.name}}発</span><span>の</span><span class="bold">{{selectedStopDate.display_stop_date}}</span><span>何時発のバスに乗りますか？</span>
            </div>
            <div class="col s12 description" v-if="selectedStopDate.stoptimes.length == 0">
                <span>{{selectedStopLocation.name}}の{{selectedStopDate.display_stop_date}}、予約できるバスはありません</span>
            </div>
            <div class="col s12 m8 offset-m2" v-for="stopTime in selectedStopDate.stoptimes">
                <button class="waves-effect waves-light btn-large center" v-on:click="selectTime(stopTime)"><span class="bold">{{stopTime.display_time}} 発</span></button>
            </div>
        </div>

        <div class="row" v-if="selectingSequenceNumber==4 && !loading && !posting">
            <div class="col s12 description">
                <span class="bold">{{selectedStopLocation.name}}</span><span>の</span><span class="bold">{{selectedStopDate.display_stop_date}}{{selectedStopTime.dipsplay_time}}発</span><span>のバスに何人で乗りますか？</span>
            </div>
            <div class="col s6 m3" v-for="n in num_of_max_people">
                <button class="waves-effect waves-light btn-large" v-on:click="selectNumOfPeople(n)"><span class="bold">{{n}}人</span></button>
            </div>
        </div>

        <div class="row">
            <div class="col s12 m6 offset-m3" v-if="!posting">
                <button class="btn-large red center" v-on:click="reset"><span>最初からやりなおす</span></button>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios'
import api from '../api'
export default {
    props: ['bus_type'],
    methods: {
        selectLocation: function(stopLocation) {
            this.selectedStopLocation = stopLocation;
            this.selectingSequenceNumber += 1;
        },
        selectDate: function(stopDate) {
            this.selectedStopDate = stopDate;
            this.selectingSequenceNumber += 1;
        },
        selectTime: function(stopTime) {
            this.selectedStopTime = stopTime;
            this.selectingSequenceNumber += 1;
        },
        selectNumOfPeople: function(numOfPeople) {
            this.selectedNumOfPeople = numOfPeople
            this.selectingSequenceNumber += 1
            this.posting = true;

            api.postReservation(this.selectedStopLocation.id, 
            this.selectedStopTime.id, this.selectedStopDate.stop_date, this.selectedNumOfPeople,
            (success) => {
                this.posting = false;
                this.$router.replace("/")
            })
        },
        reset: function() {
            this.$router.replace("/")
        },
        load_data: function() {
            this.loading = true;
            // ユーザエリア
            api.getUserAreaId((success, areaId) => {
                if (success) {
                    this.userAreaId = areaId
                    // 往路・復路
                    if (this.bus_type == "going") {
                        this.displayBusType = "行き"
                        this.targetAreaId = this.userAreaId;
    
                    } else if(this.bus_type == "returning") {
                        this.displayBusType = "帰り"
                        this.targetAreaId = 4;
                    }
                    console.log(this.bus_type)
                    api.getAvailableStops(this.targetAreaId, this.bus_type, (success, available_stops) => {
                        if (success) {
                            console.log(available_stops)
                            this.availableStops = available_stops;
                        }
                        this.loading = false
                        this.selectingSequenceNumber += 1
                    })
                }
            })
        }
    },
    data: function() {
        return {
            userAreaId: null,
            targetAreaId: null,
            displayBusType: "",
            availableStops:[],
            selectingSequenceNumber: 0,
            loading: true,
            posting: false,
            selectedStopLocation: null,
            selectedStopDate: null,
            num_of_max_people : 4,
        }
    },
    mounted() {
        this.load_data()
            setTimeout(this.reset, 5*60*1000)
    }
}
</script>

<style scoped>
#reservation {
    background-color: #444444;
}
.description {
    margin: 0px;
    padding: 0px;
}
.bold {
    font-weight: bold;
}
.reset-button {
    margin-top: 0px;
}
button {
    margin-top: 0px;
    width:100%; 
    height: 60px;
    padding: 0px;
}
.small-text {
    line-height: 150%;
    font-size: 17px;
}
span {
    font-size: 28px;
}
</style>
