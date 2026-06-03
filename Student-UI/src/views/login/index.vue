<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">学生谈心谈话系统</h2>
      <el-form ref="loginForm" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入学号" prefix-icon="el-icon-user">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="el-icon-lock"
            @keyup.enter.native="handleLogin">
          </el-input>
        </el-form-item>
        <el-form-item prop="code" v-if="captchaEnabled">
          <el-input v-model="loginForm.code" placeholder="验证码" prefix-icon="el-icon-key" style="width: 63%" @keyup.enter.native="handleLogin">
          </el-input>
          <div class="login-code">
            <img :src="codeUrl" @click="getCode" class="login-code-img" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button :loading="loading" type="primary" style="width:100%;" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from '@/api/talk/studentApi'

export default {
  name: 'StudentLogin',
  data() {
    return {
      loginForm: { username: '', password: '', code: '', uuid: '' },
      rules: {
        username: [{ required: true, message: '请输入学号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      loading: false,
      codeUrl: '',
      captchaEnabled: true
    }
  },
  created() {
    this.getCode()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = 'data:image/gif;base64,' + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        this.$store.dispatch('login', {
          username: this.loginForm.username.trim(),
          password: this.loginForm.password,
          code: this.loginForm.code,
          uuid: this.loginForm.uuid
        }).then(() => {
          this.$router.push('/')
        }).catch(() => {
          this.loading = false
          if (this.captchaEnabled) {
            this.getCode()
          }
          this.$message.error('登录失败，请检查学号和密码')
        })
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  padding: 40px 35px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  color: #303133;
  margin-bottom: 30px;
  font-size: 24px;
}

.login-form .el-input__inner {
  height: 44px;
}

.login-code {
  width: 33%;
  height: 44px;
  float: right;
  text-align: center;
}

.login-code-img {
  height: 44px;
  cursor: pointer;
  border-radius: 4px;
  vertical-align: middle;
}
</style>
