<template>
  <div class="login-wrapper">
    <div class="login-container">
      <div class="login-left">
        <div class="brand-overlay"></div>
        <div class="brand-content">
          <div class="brand-badge">
            <svg viewBox="0 0 80 80" width="80" height="80">
              <defs>
                <linearGradient id="badgeGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#4facfe;stop-opacity:1" />
                  <stop offset="100%" style="stop-color:#00f2fe;stop-opacity:1" />
                </linearGradient>
              </defs>
              <circle cx="40" cy="40" r="38" fill="none" stroke="url(#badgeGrad)" stroke-width="2"/>
              <path d="M20 55 L20 25 L40 15 L60 25 L60 55 L40 65 Z" fill="none" stroke="url(#badgeGrad)" stroke-width="1.5" stroke-linejoin="round"/>
              <line x1="30" y1="38" x2="50" y2="38" stroke="url(#badgeGrad)" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="30" y1="45" x2="50" y2="45" stroke="url(#badgeGrad)" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="30" y1="52" x2="44" y2="52" stroke="url(#badgeGrad)" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="brand-title">德州学院学生谈心谈话管理系统</h1>
          <p class="brand-slogan">用心沟通 · 以情育人</p>
        </div>
        <div class="brand-decorations">
          <svg class="deco deco-book" viewBox="0 0 60 60" width="60" height="60" opacity="0.15">
            <path d="M15 10 L15 50 L30 45 L45 50 L45 10 L30 15 Z" fill="white" stroke="white" stroke-width="0.5"/>
            <line x1="30" y1="15" x2="30" y2="45" stroke="white" stroke-width="0.5"/>
          </svg>
          <svg class="deco deco-bubble" viewBox="0 0 60 60" width="50" height="50" opacity="0.12">
            <rect x="8" y="12" width="44" height="30" rx="15" fill="white"/>
            <polygon points="18,42 14,50 30,42" fill="white"/>
          </svg>
          <svg class="deco deco-graduate" viewBox="0 0 60 60" width="55" height="55" opacity="0.13">
            <path d="M5 25 L30 12 L55 25 L30 38 Z" fill="white"/>
            <line x1="5" y1="25" x2="5" y2="40" stroke="white" stroke-width="2" stroke-linecap="round"/>
            <rect x="3" y="55" width="54" height="3" rx="1.5" fill="white" opacity="0.5"/>
          </svg>
        </div>
      </div>
      <div class="login-right">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>学生谈心谈话管理系统</p>
        </div>
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
              <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" auto-complete="off" placeholder="密码" @keyup.enter.native="handleLogin">
              <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="code" v-if="captchaEnabled">
            <el-input v-model="loginForm.code" auto-complete="off" placeholder="验证码" style="width: 63%" @keyup.enter.native="handleLogin">
              <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
            </el-input>
            <div class="login-code">
              <img :src="codeUrl" @click="getCode" class="login-code-img"/>
            </div>
          </el-form-item>
          <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
          <el-form-item style="width:100%;">
            <el-button :loading="loading" size="medium" type="primary" style="width:100%;" @click.native.prevent="handleLogin">
              <span v-if="!loading">登 录</span>
              <span v-else>登 录 中...</span>
            </el-button>
            <div style="float: right;" v-if="register">
              <router-link class="link-type" :to="'/register'">立即注册</router-link>
            </div>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <span>{{ footerContent }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      codeUrl: "",
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
        password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      captchaEnabled: true,
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get("username")
      const password = Cookies.get("password")
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 })
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove("username")
            Cookies.remove("password")
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 100vh;
  background: url('https://boot-img.xuexi.cn/contribute_img/20200602095624/1253151820366349.jpg') center/cover;
  position: relative;
}

.login-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(240, 242, 245, 0.6);
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
}

.login-container {
  display: flex;
  width: 960px;
  min-height: 540px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12), 0 4px 16px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
}

.login-left {
  position: relative;
  flex: 0 0 45%;
  background: linear-gradient(160deg, #0a2540 0%, #0f3b5c 40%, #1a5276 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.brand-overlay {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(79, 172, 254, 0.12) 0%, transparent 70%);
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 0 40px;
}

.brand-badge {
  margin-bottom: 24px;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 16px 0;
  letter-spacing: 2px;
  line-height: 1.5;
}

.brand-slogan {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.75);
  margin: 0;
  letter-spacing: 4px;
  font-weight: 300;
}

.brand-decorations {
  position: absolute;
  bottom: 30px;
  left: 30px;
  right: 30px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  z-index: 2;
}

.deco-book {
  align-self: flex-start;
}

.deco-bubble {
  align-self: center;
  margin-bottom: 10px;
}

.deco-graduate {
  align-self: flex-end;
}

.login-right {
  flex: 0 0 55%;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 50px 55px;
}

.login-header {
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 26px;
  font-weight: 600;
  color: #1a2a3a;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  .el-input {
    height: 44px;
    input {
      height: 44px;
      border-radius: 8px;
      background: #f7f8fa;
      border-color: #e4e7ed;
      padding-left: 40px;
      font-size: 14px;
      transition: all 0.3s;
      &:focus {
        background: #ffffff;
        border-color: #1a5276;
        box-shadow: 0 0 0 3px rgba(26, 82, 118, 0.08);
      }
    }
  }
  .input-icon {
    height: 44px;
    width: 16px;
    margin-left: 6px;
  }
  .el-form-item {
    margin-bottom: 22px;
  }
  .el-checkbox {
    color: #606266;
    font-size: 13px;
  }
  .el-button {
    height: 46px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 4px;
    background: linear-gradient(135deg, #1a5276 0%, #2a6fa8 100%);
    border: none;
    box-shadow: 0 4px 12px rgba(26, 82, 118, 0.3);
    transition: all 0.3s ease;
    &:hover {
      background: linear-gradient(135deg, #1e5f8a 0%, #3080ba 100%);
      box-shadow: 0 6px 20px rgba(26, 82, 118, 0.4);
      transform: translateY(-1px);
    }
    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 8px rgba(26, 82, 118, 0.3);
    }
  }
}

.login-code {
  width: 33%;
  height: 44px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
    border-radius: 6px;
  }
}

.login-code-img {
  height: 44px;
}

.login-footer {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
  margin-top: 10px;
  letter-spacing: 1px;
}

.link-type {
  color: #1a5276;
  font-size: 13px;
  &:hover {
    color: #2a6fa8;
  }
}

@media screen and (max-width: 768px) {
  .login-container {
    width: 92%;
    flex-direction: column;
    min-height: auto;
  }
  .login-left {
    flex: 0 0 auto;
    padding: 36px 20px;
    .brand-title {
      font-size: 18px;
    }
    .brand-slogan {
      font-size: 13px;
    }
    .brand-badge {
      margin-bottom: 14px;
      svg {
        width: 56px;
        height: 56px;
      }
    }
    .brand-decorations {
      display: none;
    }
  }
  .login-right {
    padding: 30px 28px;
  }
  .login-header h2 {
    font-size: 22px;
  }
}
</style>
