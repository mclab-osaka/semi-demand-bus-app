<template>
<div id="top-screen">

    <div class="row center message" v-if="!loadSuccessed">
        <p>読み込み中です</p>
    </div>

    <div class="row message" v-if="loadSuccessed">
        <span>こんにちは {{user.last_name}} さん </span> <br/>
        <span class="info">{{message.text}}</span>
    </div>

    <reservation-list @update="loadData" class="reservations" :reservations="reservations" v-if="loadSuccessed"></reservation-list>

    <div class="reservation center" v-if="loadSuccessed && reservations.length < 2">
        <button class="waves-effect waves-light btn-large" v-on:click="reserveButtonClicked">
            <span v-if="reservations.length < 1">バスを予約する</span>
            <span v-if="reservations.length >= 1">さらにバスを予約する</span>
        </button>
    </div>


    <div class="alert red lighten-5" v-if="loadSuccessed && alertMessage">
        <p class="red-text">{{alertMessage.text}}</p>
    </div>
</div>
</template>

<script>
import api from '../api';
import ReservationList from "./ReservationList.vue"
export default {
    components : {
        ReservationList
    },
    data: function() {
        return  {
            user: {},
            message: null,
            alertMessage: null, 
            reservations: [],
            loadSuccessed: false,
        }
    },
    methods: {
        mainScreenDataCallback: function(success, user, message, alertMessage, reservations) {
            if (success) {
                console.log(reservations);
                this.reservations = reservations;
                console.log(user);
                this.user = user;
                console.log(message);
                this.message = message;
                console.log(alertMessage);
                this.alertMessage = alertMessage;
                this.loadSuccessed = true;
            }
        },
        loadData: function() {
            api.getMainScreenData(this.mainScreenDataCallback)
        },
        reserveButtonClicked: function() {
            this.$router.replace("/reservation-going")
        }
    },
    created: function() {
        this.loadData();
        setInterval(this.update, 20 * 1000);
    }
}
</script>

<style scoped>
#top-screen {
    margin: 0px;
    padding: 0px;
}
.message {
    font-size: 24px;
    margin: 8px;
}
.message .info {
    text-decoration: underline;
}
.reservations {
    margin: 8px;
}
.alert {
    margin: 0px;
    padding: 0px;
    text-align: center;
    font-size: 20px;
    font-weight: bold;
    position: absolute;
    bottom: 0;
    width:100%;
    min-height: 100px;
}
button { 
    font-weight: bold;
    font-size: 24px;
}

</style>
