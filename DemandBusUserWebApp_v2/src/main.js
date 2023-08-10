import Vue from 'vue'
import Router from 'vue-router'

import api from './api'

import MainScreen from '@/components/MainScreen'
import TopScreen from '@/components/TopScreen'
import LoginScreen from '@/components/LoginScreen'
import ReservationScreen from '@/components/ReservationScreen'

import App from './App'

Vue.use(Router)
Vue.config.productionTip = false

var router = new Router({
    routes: [
        {
            path: '/',
            name: 'top',
            component: TopScreen,
            beforeEnter: function(to, from, next) {
                if (!api.isLoggedIn()) {
                    next("/login");
                } else {
                    next()
                }
            }
        },
        {
            path: '/login',
            name: 'login',
            component: LoginScreen,
            beforeEnter: function(to, from, next) {
                if (api.isLoggedIn()) {
                    next("/");
                } else {
                    next()
                }
            }
        },
        {
            path: '/logout',
            name: 'logout',
            beforeEnter: function(to, from, next) {
                api.logout();
                next("/login");
            }
        },
        {
            path: '/reservation-going',
            name: 'reservation-going',
            component: ReservationScreen,
            props: {bus_type: 'going' },
            beforeEnter: function(to, from, next) {
                if (!api.isLoggedIn()) {
                    next("/login");
                } else {
                    next()
                }
            }
        },
        {
            path: '/reservation-returning',
            name: 'reservation-returning',
            component: ReservationScreen,
            props: {bus_type: 'returning' },
            beforeEnter: function(to, from, next) {
                if (!api.isLoggedIn()) {
                    next("/login");
                } else {
                    next()
                }
            }
        }
    ]
});

new Vue({
  el: '#app',
  router : router,
  template: '<App/>',
  components: { App, TopScreen, MainScreen, LoginScreen }
})
