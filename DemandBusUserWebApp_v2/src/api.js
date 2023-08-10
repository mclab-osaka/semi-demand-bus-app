import axios from 'axios'
// var API_END_ROOT = "http://localhost:8000/api/"
var API_END_ROOT = "https://bus-yoyaku.higashino-lab.org/api/v4/"
var title = "toyooka-demandbus-user-web-page";

export default {
    getToken: function(username, pass, callback) {
        const body = {username: username, password: pass}
        console.log(body)
        axios.post(API_END_ROOT+"token/", body)
            .then(response => {
                console.log(response)
                localStorage.setItem("token", response.data.token);
                callback(true)
            }).catch(error => {
                console.log(error)
                callback(false)
            })
    },
    getUser: function(callback) {
        const token = localStorage.getItem("token");
        axios.get(API_END_ROOT+"user/", { headers: { Authorization: "JWT " + token } })
            .then(response => {
                callback(true, response.data)
            }).catch(error => {
                consle.log(error.response.status)
                // ToDo: Set Null to Token
                callback(false, {})
            })
    },
    getReservation: function(callback) {
        const token = localStorage.getItem("token");
        axios.get(API_END_ROOT+"user/", { headers: { Authorization: "JWT " + token } })
            .then(response => {
                callback(true, response.data)
            }).catch(error => {
                consle.log(error.response.status)
                callback(false, {})
            })

    },
    getMainScreenData: function(callback) {
        const token = localStorage.getItem("token");
        const body = { headers: { Authorization: "JWT " + token} }
        console.log(body)

        axios.all([axios.get(API_END_ROOT+"user/", body), axios.get(API_END_ROOT+"message/", body), axios.get(API_END_ROOT+"alert/", body), axios.get(API_END_ROOT+"reservation/", body)])
            .then(axios.spread((userResponse, messageResponse, alertResponse, reservationResponse) => {
                callback(true, userResponse.data, messageResponse.data, alertResponse.data, reservationResponse.data)
            })).catch(error => {
                console.log(error);
                console.log(error.response.status)
                callback(false, {}, {}, {}, {}, {})
            })
    },
    getAvailableStops: function(area_id, bus_type, callback) {
        console.log(area_id)
        const token = localStorage.getItem("token");
        axios.get(API_END_ROOT+"reservable-stop/"+area_id+"/"+bus_type, { headers: { Authorization: "JWT " + token } })
            .then(response => {
                callback(true, response.data)
            }).catch(error => {
                console.log(error.response.status)
                callback(false, {})
            })
    },
    getUserAreaId: function(callback) {
        const token = localStorage.getItem("token");
         axios.get(API_END_ROOT+"user/", { headers: { Authorization: "JWT " + token } })
            .then(response => {
                console.log(response.data.area)
                callback(true, response.data.area)
            }).catch(error => {
                console.log(error)
                callback(false, -1)
            })
    },
    postReservation: function(stopLocationId, stopTimeId, stopDate, numOfPeople, callback) {
        const token = localStorage.getItem("token");
        const reservation = {
            stop_location: stopLocationId,
            stop_time: stopTimeId,
            date: stopDate,
            num_of_people: numOfPeople,
        }
        console.log(reservation)
        axios.post(API_END_ROOT + "reservation/", reservation, {"headers": { Authorization: "JWT " + token }})
        .then(respone => {
            callback(true)
            console.log(response)
        }).catch(error => {
            callback(false)
        })
    },
    patchReservation: function(reservationId, callback) {
        const token = localStorage.getItem("token");
        axios.patch(API_END_ROOT + "reservation/"+reservationId, {"state":"canceled"}, {"headers": { Authorization: "JWT " + token }})
        .then(respone => {
            callback(true)
        }).catch(error => {
            callback(false)
        })
    },
    login: function(username, password, callback) {
        getToken(username, password, callback)
    },
    logout: function() {
        localStorage.clear()
    },
    isLoggedIn: function() {
        const token = localStorage.getItem("token");

        if(token) {
            return true
        } else {
            return false
        }
    },
}
