<template>
	<div id="main">
        <div class="loading-message" v-if="loading">
            <div class="col s12 top-message">
                <span>読み込み中です。しばらくお待ちください。</span>
            </div>
        </div>

        <div class="row" v-if="!loading">

            <div class="col s12 top-message">
                <span>こんにちは {{user.last_name}} さん</span>
            </div>

            <div class="col s12 message" v-if="!goingRes && !returningRes">
                <span>{{message.text}}</span>
            </div>

            <div class="col s12 m12 l10 offset-l1 center">
                <div class="row">

                    <div v-if="!goingRes">
                        <div class="col s12 m6 center">
                            <button class="waves-effect waves-light btn-large reservation-button" v-on:click="reserveGoing">
                                <span class="bold">行きのバス</span><span>を予約する</span>
                            </button>
                        </div>
                    </div>

                    <div v-if="goingRes">
                        <div class="col s12 m5 card-panel teal lighten-5 reservation-card">
                            <div class="left-align">
                                <span class="red-text bold small-text">行きのバスを予約済み</span>
                                <ul>
                                    <li><span class="small-text">場所: </span> 
                                        <span class="bold small-text">{{goingRes.stop_location.name}}</span></li>
                                    <li><span class="small-text">時刻: </span> 
                                        <span class="bold small-text">{{goingRes.display_date}} {{goingRes.stop_time.display_time}}</span></li>
                                    <li><span class="small-text">人数: </span> 
                                        <span class="bold small-text">{{goingRes.num_of_people}}人</span></li>
                                </ul>
                            </div>
                            <button class="btn red cancel-button center" v-on:click="cancelGoingRes" :disabled="sending_going_res">
                                <span class="bold small-text">この予約を取り消す</span>
                            </button>
                        </div>
                    </div>

                    <div v-if="!returningRes">
                        <div class="col s12 m6">
                            <button class="waves-effect waves-light btn-large reservation-button" v-on:click="reserveReturning">
                                <span class="bold">帰りのバス</span><span>を予約する</span>
                            </button>
                        </div>
                    </div>

                    <div v-if="returningRes">
                        <div class="col s12 m5 center card-panel teal lighten-5 reservation-card">
                            <div class="left-align">
                                <span class="red-text bold small-text">帰りのバスを予約済み</span>
                                <ul>
                                    <li><span class="small-text">場所: </span> 
                                        <span class="bold small-text">{{returningRes.stop_location.name}}</span></li>
                                    <li><span class="small-text">時刻: </span> 
                                        <span class="bold small-text">{{returningRes.display_date}} {{returningRes.stop_time.display_time}}</span></li>
                                    <li><span class="small-text">人数: </span> 
                                        <span class="bold small-text">{{returningRes.num_of_people}}人</span></li>
                                </ul>
                            </div>
                            <button class="btn red center" v-on:click="cancelReturningRes" :disabled="sending_returning_res">
                                <span class="bold small-text">この予約を取り消す</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
                
            <div class="col s12 alert card-panel red lighten-5" v-if="(!goingRes) | (!returningRes)">
                <span class="bold red-text">{{alertMessage.text}}</span>
            </div>
        </div>
    </div>
</template>

<script>
import api from '../api';
export default {
    data: function() {
        return  {
            user: {},
            loading: true,
            sending_going_res: false,
            sending_returning_res: false,
            message: null,
            alertMessage: null, 
            goingRes: null,
            returningRes : null,
            updateCount: 0,
        }
    },
    methods: {
        reserveGoing : function() {
            this.sending_going_res = true;
            this.$router.replace("/reservation-going")
        },
        reserveReturning: function() {
            this.sending_returning_res = true;
            this.$router.replace("/reservation-returning")
        },
        cancelGoingRes: function() {
            this.sending_going_res = true;
            api.patchReservation(this.goingRes.id, (success) => {
                if (success) {
                    this.goingRes = null;
                }
                this.sending_going_res = false;
            })
        },
        cancelReturningRes: function() {
            this.sending_going_res = true;
            api.patchReservation(this.returningRes.id, (success) => {
                if (success) {
                    this.returningRes = null;
                }
                this.sending_returning_res = false;
            })
        },
        loadData: function() {
            api.getMainScreenData(this.mainScreenDataCallback)
        },
        update: function() {
            console.log(this.updateCount)

            if (this.updateCount <= 0) {
                this.loadData()
                
                const now = new Date();
                const hour = now.getHours();
                if (0<=hour && hour<=5) {
                    this.updateCount = 20;
                } else{
                    this.updateCount = 1
                }
            } else {
                this.updateCount -= 1;
            }
        },
        mainScreenDataCallback: function(success, user, message, alertMessage, reservations) {
            if (success) {
                console.log(reservations)
                this.user = user;
                console.log(this.user)
                this.message = message;
                console.log(this.message)
                this.alertMessage = alertMessage;
                console.log(this.alertMessage)
                this.goingRes = reservations.going;
                console.log(this.goingRes)
                this.returningRes = reservations.returning;
                console.log(this.returningRes)
            }
            this.loading = false;
        }
    },
    created() {
        this.loading = true;
        this.loadData()
        setInterval(this.update, 15*1000);
    }
}
</script>

<style scoped>
.top-message {
    margin: 8px;
}
.message {
    margin: 8px;
}
.reservation-card {
    padding: 12px;
    margin: 4px 16px 4px 16px;
}
.reservation-button {
    margin: 8px 0px 8px 0px;
    padding: 0px 8px 0px 8px;
    width: 100%;
}
.cancel-button {
}
.alert {
    margin: 0px;
    padding: 10px;
    width: 100%;
    position: absolute;
    bottom: 0;
}
.bold {
    font-weight: bold;
}
.small-text {
    font-size: 20px;
}
ul {
    margin: 4px;
}
span {
    font-size: 24px;
}
</style>
