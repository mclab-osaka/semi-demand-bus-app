<template>
    <div class="row" style="padding:8px">
        <div class="col s12">
            <h5 class="center-align">ユーザ名とパスワードを入力してログインしてください</h5>
            <h5 class="center-align red-text" v-if="error" >IDかパスワードが誤っています</h5>
        </div>

        <div class="col s12 m8 offset-m2">
            <div class="card">
                <div class="card-content">
                    <div class="row">
                        <div class="input-field col s12">
                            <form>
                                <input autocomplete="username" placeholder="ユーザ名" id="id-number" type="text" v-model="form.username">
                                <input autocomplete="current-password" placeholder="パスワード" id="password" type="password" v-model="form.password">
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col s12 center">
            <button id="login-button" class="waves-effect waves-light btn-large" v-on:click="loginButtonClicked" :disabled="sending">ログイン</button>
        </div>
    </div>
</template>

<script>
import api from '../api';
export default {
    data: function() {
        return {
            form : {
                username: "",
                password: "",
            },
            sending: false,
            error: false
        }
    },
    methods : {
        loginCallback : function(success) {
            this.sending = false;
            if (!success) {
                this.error = true;
            } else {
                this.error = false;
                this.$router.replace("/")
            }
        },
        loginButtonClicked: function() {
            this.sending = true;
            api.getToken(this.form.username, this.form.password, this.loginCallback);
        }
    }
}
</script>

<style scoped>

</style>
