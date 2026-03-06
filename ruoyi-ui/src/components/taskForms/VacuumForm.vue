<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
    <el-form-item label="设备编号" prop="deviceNo">
      <el-input v-model="form.deviceNo" placeholder="请输入设备编号" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="真空度(Pa)" prop="vacuumLevel">
      <el-input-number
        v-model="form.vacuumLevel"
        :min="0"
        :precision="2"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="处理时长(分钟)" prop="processDuration">
      <el-input-number
        v-model="form.processDuration"
        :min="0"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="操作人" prop="operator">
      <el-input v-model="form.operator" :disabled="true" />
    </el-form-item>
    <el-form-item label="异常记录" prop="abnormalRecord">
      <el-input
        v-model="form.abnormalRecord"
        type="textarea"
        placeholder="无异常则留空"
        :disabled="readonly"
      />
    </el-form-item>
    <el-form-item label="测试报告">
      <report-uploader
        v-model="form.reports"
        :readonly="readonly"
        :material-name="extraContext.materialName"
        :material-quantity="extraContext.materialQuantity"
        node-name="真空处理"
      />
    </el-form-item>
  </el-form>
</template>

<script>
import ReportUploader from './ReportUploader.vue'
export default {
  name: 'VacuumForm',
  components: { ReportUploader },
  data() {
    return {
      readonly: false,
      extraContext: { materialName: '', materialQuantity: null },
      form: {
        deviceNo: '',
        vacuumLevel: 0,
        processDuration: 0,
        operator: '',
        abnormalRecord: '',
        reportIds: [],
        reports: []
      },
      rules: {
        deviceNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
        vacuumLevel: [{
          required: true,
          validator: (rule, value, callback) => {
            if (value === undefined || value === null || value <= 0) {
              callback(new Error('请输入大于0的真空度'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }],
        processDuration: [{
          required: true,
          validator: (rule, value, callback) => {
            if (value === undefined || value === null || value <= 0) {
              callback(new Error('请输入大于0的处理时长'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }]
      }
    }
  },
  created() {
    this.form.operator = this.$store.state.user.name || ''
  },
  methods: {
    getFormData() {
      if (this.readonly) {
        return Promise.resolve({ ...this.form })
      }
      return new Promise((resolve, reject) => {
        this.$refs.formRef.validate(valid => {
          if (valid) {
            resolve({ ...this.form })
          } else {
            reject(new Error('表单校验失败'))
          }
        })
      })
    },
    setFormData(data) {
      if (!data) return
      Object.keys(this.form).forEach(key => {
        if (Object.prototype.hasOwnProperty.call(data, key)) {
          this.$set(this.form, key, data[key])
        }
      })
    },
    setReadonly(val) {
      this.readonly = val
    },
    setExtraContext(ctx) {
      this.extraContext = ctx || { materialName: '', materialQuantity: null }
    }
  }
}
</script>
