import axios from 'axios'
var API_END_ROOT = "http://localhost:8000/v3/"
var title = "toyooka-demandbus-user-web-page";

export default {
    getToken: function(username, pass, callback) {
        axios.post(API_END_ROOT+"token/", {"username":username, "password":pass})
            .then(response => {
                localStorage.setItem("token", JSON.stringify(response.data.token));
                callback(true)
            }).catch(error => {
                callback(false)
            })
    },
    getUser: function(callback) {
        const token = localStorage.getItem("token");
        axios.get(API_END_ROOT+"user/", { headers: { Authorization: "JWT " + toekn } })
            .then(response => {
                callback(true, response.data)
            }).catch(error => {
                consle.log(error.response.status)
                callback(false, {})
            })
    },
    login: function(callback) {
        getToken(callback)
    },
    logout: function() {
        localStorage.clear();
    },
/*

    login: function(id, pass, callback) {
        axios.post(API_END_ROOT + "token/", {"username": id , "password":pass})
            .then(response => {
                var actual_id = id;
                var token = response.data.toke;
                axios.get(API_END_ROOT + "users/", {headers:{Authorization: "JWT " + token}})
                    .then(response => {
                        var data = {
                            "token": token,
                            "pass": pass,
                            "id": actual_id,
                            "area": response.data.area
                        };
                        localStorage.setItem("data", JSON.stringify(data));
                        callback(true);
                }).catch(error => {
                    callback(false);
                });
        }).catch(error => {
            callback(false);
        });
    },
    */
    loggedIn: function() {

        var data_str = localStorage.getItem("data");
        if (!data_str) {
            return false;
        }

        var data = JSON.parse(data_str);
        var id = data.id;
        var pass = data.pass;

        if (id) {
            axios.post("https://toyooka-demandbus.higashino-lab.org:50443/v2/token/", {"username": id , "password":pass})
            .then(response => {
                var token = response.data.token;
                data.token = token;
                localStorage.setItem("data", JSON.stringify(data));
            });
            return true;
        } else {
            return false;
        }
    },
    tokenRefresh: function(callback) {

        var data_str = localStorage.getItem("data");
        if (!data_str) {
            return false;
        }

        var data = JSON.parse(data_str);
        // var data = des.decrypt(JSON.parse(data_str), title);
        var id = data.id
        var pass = data.pass

        axios.post("https://toyooka-demandbus.higashino-lab.org:50443/v2/token/", {"username": id , "password":pass})
            .then(response => {
                var token = response.data.token;
                data.token = token;
                localStorage.setItem("data", JSON.stringify(data));
                callback()
        });
    },
    getId: function() {

        var data_str = localStorage.getItem("data");
        if (!data_str) {
            return null;
        }
        var data = JSON.parse(data_str);
        // var data = des.decrypt(JSON.parse(data_str), title);
        var id = data.id
        if (id) {
            return id;
        } else {
            return null;
        }
    },
    getToken: function() {
        var data_str = localStorage.getItem("data");
        if (!data_str) {
            return null;
        }
        var data = JSON.parse(data_str);
        // var data = des.decrypt(JSON.parse(data_str), title);
        var token = data.token;
        if (token) {
            return token;
        } else {
            return null;
        }
    },
    getArea: function() {
        var data_str = localStorage.getItem("data");
        if (!data_str) {
            return null;
        }
        var data = JSON.parse(data_str);
        // var data = des.decrypt(JSON.parse(data_str), title);
        // var user = JSON.parse(data.user);
        var area = data.area;
        if (area) {
            return area;
        } else {
            return null;
        }
    },
    getPass: function() {
        var data_str = localStorage.getItem("data");
        if (!data_str) {
            return null;
        }
        var data = JSON.parse(data_str);
        // var data = des.decrypt(JSON.parse(data_str), title);
        var pass = data.pass;
        if (pass) {
            return pass;
        } else {
            return null;
        }
    }
}
