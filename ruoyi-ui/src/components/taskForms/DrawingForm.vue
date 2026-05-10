<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
    <el-form-item label="图纸名称" prop="drawingName">
      <el-input v-model="form.drawingName" placeholder="请输入图纸名称" :disabled="readonly" />
    </el-form-item>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="直径" prop="diameter">
          <el-input v-model="form.diameter" placeholder="请输入直径（可带单位）" :disabled="readonly" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="长" prop="length">
          <el-input v-model="form.length" placeholder="请输入长度（可带单位）" :disabled="readonly" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="宽" prop="width">
          <el-input v-model="form.width" placeholder="请输入宽度（可带单位）" :disabled="readonly" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="高" prop="height">
          <el-input v-model="form.height" placeholder="请输入高度（可带单位）" :disabled="readonly" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="光洁度" prop="smoothness">
          <el-input v-model="form.smoothness" placeholder="请输入光洁度" :disabled="readonly" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="面型" prop="surfaceType">
          <el-input v-model="form.surfaceType" placeholder="请输入面型" :disabled="readonly" />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="项目任务书">
      <report-uploader
        v-model="form.reports"
        :readonly="readonly"
        :material-name="extraContext.materialName"
        :material-quantity="extraContext.materialQuantity"
        node-name="项目任务书"
        test-type="项目任务书"
      />
    </el-form-item>
  </el-form>
</template>

<script>
import ReportUploader from './ReportUploader.vue'
export default {
  name: 'DrawingForm',
  components: { ReportUploader },
  data() {
    return {
      readonly: false,
      extraContext: { materialName: '', materialQuantity: null },
      form: {
        drawingName: '',
        diameter: '',
        length: '',
        width: '',
        height: '',
        smoothness: '',
        surfaceType: '',
        reportIds: [],
        reports: []
      },
      rules: {
        drawingName: [
          { required: true, message: '请输入图纸名称', trigger: 'blur' }
        ]
      }
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
