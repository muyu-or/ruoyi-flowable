<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="操作人" prop="operator">
      <el-input v-model="form.operator" :disabled="true" />
    </el-form-item>
    <el-form-item label="处理方式" prop="processMethod">
      <el-input v-model="form.processMethod" placeholder="请输入处理方式" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="处理时长(分钟)" prop="processDuration">
      <el-input-number
        v-model="form.processDuration"
        :min="0"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="处理结果" prop="processResult">
      <el-radio-group v-model="form.processResult" :disabled="readonly">
        <el-radio label="normal">正常</el-radio>
        <el-radio label="abnormal">异常</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="备注/异常描述" prop="remark">
      <el-input
        v-model="form.remark"
        type="textarea"
        placeholder="请输入备注/异常描述"
        :disabled="readonly"
      />
    </el-form-item>
    <el-form-item label="测试报告">
      <report-uploader
        v-model="form.reports"
        :readonly="readonly"
        :material-name="extraContext.materialName"
        :material-quantity="extraContext.materialQuantity"
        node-name="预处理"
      />
    </el-form-item>
  </el-form>
</template>

<script>
import ReportUploader from './ReportUploader.vue'
export default {
  name: 'PreprocessForm',
  components: { ReportUploader },
  data() {
    return {
      readonly: false,
      extraContext: { materialName: '', materialQuantity: null },
      form: {
        operator: '',
        processMethod: '',
        processDuration: 0,
        processResult: '',
        remark: '',
        reportIds: [],
        reports: []
      },
      rules: {
        processMethod: [{ required: true, message: '请输入处理方式', trigger: 'blur' }],
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
        }],
        processResult: [{ required: true, message: '请选择处理结果', trigger: 'change' }]
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
