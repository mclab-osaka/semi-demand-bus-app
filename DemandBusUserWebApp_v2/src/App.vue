<template>
  <div id="app" v-bind:class="{ 'green-background' : nowReservationScreen }">
    <header>
        <nav class="deep-orange lighten-2">
            <div class="nav-wrapper">
                <a href="#/" class="flow-text" style="margin-left:16px">デマンドバス予約</a>
            </div>
        </nav>
    </header>
    <main id="main">
        <router-view></router-view>
    </main>
  </div>
</template>

<script>
export default {
    name: 'app',
    data: function() {
        return {
            "nowReservationScreen": false
        }
    },
    watch:{
        $route (to, from){
            console.log("route changed");
            console.log(to)
            console.log(from)

            if (to.name == "top") {
                this.nowReservationScreen = false;
            } else if (from.name == "top") {
                this.nowReservationScreen = true;
            }
        }
    }    
}
</script>

<style scoped>
header {
    width: 100%;
    margin: 0;
}
#app {
    padding: 0px;
    margin: 0px;
    height: 100%;
}
#main {
    padding: 0px;
    margin: 0px;
}
.green-background {
    background-color: rgb(199, 255, 204);
}
.flow-text {
    font-size: 28px;
}
</style>
