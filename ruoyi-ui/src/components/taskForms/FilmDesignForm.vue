<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
    <el-form-item label="任务节点名称" prop="taskNodeName">
      <el-input v-model="form.taskNodeName" placeholder="请输入任务节点名称" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="膜系设计报告">
      <report-uploader
        v-model="form.reports"
        :readonly="readonly"
        :material-name="extraContext.materialName"
        :material-quantity="extraContext.materialQuantity"
        node-name="膜系设计"
        test-type="膜系设计"
      />
    </el-form-item>
  </el-form>
</template>

<script>
import ReportUploader from './ReportUploader.vue'
export default {
  name: 'FilmDesignForm',
  components: { ReportUploader },
  data() {
    return {
      readonly: false,
      extraContext: { materialName: '', materialQuantity: null },
      form: {
        taskNodeName: '膜系设计',
        reportIds: [],
        reports: []
      },
      rules: {}
    }
  },
  methods: {
    getFormData() {
      if (this.readonly) {
        return Promise.resolve({ ...this.form })
      }
      return new Promise((resolve, reject) => {
        this.$refs.formRef.validate(valid => {
          if (valid) {
            const result = { ...this.form }
            if (Array.isArray(result.reports)) {
              result.reports = result.reports.map(r => Object.assign({}, r, {
                materialName: this.extraContext.materialName || r.materialName || '',
                materialQuantity: this.extraContext.materialQuantity != null ? this.extraContext.materialQuantity : r.materialQuantity
              }))
            }
            resolve(result)
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