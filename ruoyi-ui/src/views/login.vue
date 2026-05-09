<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{title}}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          auto-complete="off"
          placeholder="账号"
        >
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      loginForm: {
        username: "admin",
        password: "123456",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关（已禁用）
      captchaEnabled: false,
      // 注册开关
      register: false,
      redirect: undefined
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCookie();
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            if (this.captchaEnabled) {
              this.getCode();
            }
          });
        }
      });
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/img_20260428_124000_faee30.jpg");
  background-size: cover;
  background-position: center;
  position: relative;

  /* 背景遮罩层增强 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(30, 42, 58, 0.7) 0%, rgba(45, 74, 111, 0.6) 100%);
    z-index: 1;
  }
}

.title {
  margin: 0px auto 35px auto;
  text-align: center;
  color: #fff;
  font-size: 33px;
  font-weight: 600;
  letter-spacing: 3px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.login-form {
  border-radius: 16px;
  background: linear-gradient(145deg, rgba(30, 42, 58, 0.92) 0%, rgba(45, 74, 111, 0.85) 100%);
  backdrop-filter: blur(12px);
  width: 450px;
  padding: 45px 40px 25px 40px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.4),
              0 0 0 1px rgba(255, 255, 255, 0.1),
              inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  z-index: 2;
  position: relative;

  /* 输入框样式增强 */
  .el-input {
    height: 48px;

    input {
      height: 48px;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 8px;
      color: #fff;
      font-size: 17px;
      padding-left: 40px;
      transition: all 0.3s ease;

      &::placeholder {
        color: rgba(255, 255, 255, 0.5);
        font-size: 13px;
      }

      &:focus {
        background: rgba(255, 255, 255, 0.12);
        border-color: rgba(74, 155, 140, 0.6);
        box-shadow: 0 0 0 3px rgba(74, 155, 140, 0.2);
      }

      &:hover {
        border-color: rgba(255, 255, 255, 0.3);
      }
    }
  }

  .input-icon {
    height: 48px;
    width: 18px;
    margin-left: 6px;
    color: rgba(255, 255, 255, 0.6);
    transition: color 0.3s ease;
  }

  .el-input:focus-within .input-icon {
    color: rgba(74, 155, 140, 0.9);
  }

  /* 表单项间距 */
  .el-form-item {
    margin-bottom: 24px;
  }

  /* 记住密码样式 */
  .el-checkbox {
    color: rgba(255, 255, 255, 0.7);
    font-size: 13px;

    .el-checkbox__label {
      color: rgba(255, 255, 255, 0.7);
      font-size: 13px;
    }

    .el-checkbox__inner {
      background-color: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.3);

      &:hover {
        border-color: rgba(74, 155, 140, 0.6);
      }
    }

    &.is-checked .el-checkbox__inner {
      background-color: #4A9B8C;
      border-color: #4A9B8C;
    }
  }

  /* 登录按钮样式 - 青绿渐变 */
  .el-button--primary {
    background: linear-gradient(135deg, #4A9B8C 0%, #3A8B7C 50%, #2A7B6C 100%);
    border: none;
    height: 48px;
    font-size: 17px;
    font-weight: 600;
    letter-spacing: 8px;
    border-radius: 8px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(74, 155, 140, 0.4);

    &:hover {
      background: linear-gradient(135deg, #5BAC9D 0%, #4A9B8C 50%, #3A8B7C 100%);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(74, 155, 140, 0.5);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 4px 15px rgba(74, 155, 140, 0.4);
    }
  }

  /* 注册链接样式 */
  .link-type {
    color: rgba(74, 155, 140, 0.8);
    font-size: 13px;

    &:hover {
      color: #73B5A8;
      text-decoration: underline;
    }
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
}

.login-code {
  width: 33%;
  height: 48px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
    border-radius: 8px;
    height: 48px;
    border: 1px solid rgba(255, 255, 255, 0.2);
  }
}

.login-code-img {
  height: 48px;
}

/* 响应式调整 */
@media (max-width: 500px) {
  .login-form {
    width: 90%;
    padding: 30px 25px 20px 25px;
  }

  .title {
    font-size: 24px;
  }
}
</style>
