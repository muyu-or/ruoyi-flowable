<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="操作人" prop="operator">
      <el-input v-model="form.operator" placeholder="请输入操作人" :disabled="readonly" />
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
  </el-form>
</template>

<script>
export default {
  name: 'PreprocessForm',
  props: {
    taskData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      readonly: false,
      form: {
        operator: '',
        processMethod: '',
        processDuration: 0,
        processResult: '',
        remark: ''
      },
      rules: {
        operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
        processMethod: [{ required: true, message: '请输入处理方式', trigger: 'blur' }],
        processDuration: [{ required: true, message: '请输入处理时长', trigger: 'blur' }],
        processResult: [{ required: true, message: '请选择处理结果', trigger: 'change' }]
      }
    }
  },
  methods: {
    getFormData() {
      return new Promise((resolve, reject) => {
        this.$refs.form.validate(valid => {
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
    }
  }
}
</script>
