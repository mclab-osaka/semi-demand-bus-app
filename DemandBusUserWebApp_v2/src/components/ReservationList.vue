<template>
<div class="row">
    <p class="top-message">{{topMessage}}</p>
    <div v-for="reservation in reservations" :key="reservation.name">
        <div class="row center card-panel teal lighten-5 bold reservation-card">
            {{reservation.display_date}} {{reservation.stop_time.display_time}} <br/>
            {{reservation.stop_location.name}} 発 <br>
            <button class="waves-effect waves-light btn red" v-on:click="cancelButtonClicked(reservation)" :disabled="canceling">
                <span v-if="!reservation.canceling">この予約を取り消す</span>
            </button>
        </div>
    </div>
</div>
</template>

<script>
import api from '../api';
export default {
    props: ['reservations'],
    data: function() {
        return {
            canceling: false,
        }
    },
    methods: {
        cancelButtonClicked: function(reservation) {
            this.canceling = true;

            api.patchReservation(reservation.id, (success) => {
                console.log("Cancel Reservation Success");
                this.$emit("update");
                this.canceling = false;
            })
        }
    },
    computed: {
        topMessage: function() {
            if (this.reservations && this.reservations.length == 0) {
                return "予約はありません。"
            } else if (this.reservations && this.reservations.length > 0) {
                return this.reservations.length + "件の予約があります。"
            }
        }
    },
    created: function() {
        console.log("created");
        console.log(this.reservations);

    },
    mounted: function() {
        console.log("mounted");
        console.log(this.reservations);

    }
}
</script>

<style scoped>
.top-message {
    font-size: 24px;
    padding: 0px;
    margin: 0px;
}
.reservation-card {
    margin: 8px 8px 16px 8px;
    padding: 8px;
    font-size: 24px;
}
button span {
    font-size: 20px;
    font-weight: bold;
}

</style>
