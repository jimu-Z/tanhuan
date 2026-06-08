<template>
  <div class="appointment-create-page">
    <h3 class="page-title">预约谈话</h3>

    <el-card class="form-card" shadow="never">
      <el-form ref="appointmentForm" :model="form" :rules="rules" label-width="80px" label-position="top">
        <el-form-item label="预约教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="请选择辅导员/班主任" clearable style="width: 100%;" :loading="counselorLoading">
            <el-option v-for="c in counselorList" :key="c.teacherId" :label="c.name" :value="c.teacherId" />
          </el-select>
        </el-form-item>

        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker v-model="form.appointmentTime" type="datetime" placeholder="请选择预约时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="预约地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入预约地点（选填）" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="预约原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请简述预约原因（选填）" maxlength="300" show-word-limit />
        </el-form-item>

        <el-form-item style="margin-top: 24px;">
          <el-button type="primary" :loading="submitLoading" style="width: 100%;" @click="handleSubmit">提交预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getCounselors, addAppointment } from '@/api/talk/appointmentApi'

export default {
  name: 'AppointmentCreate',
  data() {
    return {
      counselorList: [],
      counselorLoading: false,
      submitLoading: false,
      form: {
        teacherId: null,
        appointmentTime: '',
        location: '',
        reason: ''
      },
      rules: {
        teacherId: [{ required: true, message: '请选择预约教师', trigger: 'change' }],
        appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
      }
    }
  },
  computed: {
    deptId() {
      return this.$store.state.user.deptId || ''
    }
  },
  mounted() {
    this.fetchCounselors()
  },
  methods: {
    fetchCounselors() {
      if (!this.deptId) {
        this.$message.warning('未关联学院，无法加载辅导员列表')
        this.counselorList = []
        return
      }
      this.counselorLoading = true
      getCounselors(this.deptId).then(res => {
        this.counselorList = res.data || res.rows || res || []
      }).catch(() => {
        this.counselorList = []
      }).finally(() => {
        this.counselorLoading = false
      })
    },
    handleSubmit() {
      this.$refs.appointmentForm.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        addAppointment(this.form).then(() => {
          this.$message.success('预约提交成功')
          this.form = {
            teacherId: null,
            appointmentTime: '',
            location: '',
            reason: ''
          }
          this.$refs.appointmentForm.resetFields()
        }).catch(() => {
          this.$message.error('预约提交失败')
        }).finally(() => {
          this.submitLoading = false
        })
      })
    }
  }
}
</script>

<style scoped>
.appointment-create-page {
  padding: 16px;
}

.page-title {
  font-size: 20px;
  color: #303133;
  font-weight: 600;
  margin: 0 0 20px 0;
  padding-left: 12px;
  border-left: 3px solid #2a6fa8;
  line-height: 1;
}

.form-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

@media (max-width: 768px) {
  .appointment-create-page {
    padding: 10px;
  }
}
</style>
