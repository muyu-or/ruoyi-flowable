<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="测试项目" prop="testItems">
      <el-input v-model="form.testItems" placeholder="请输入测试项目" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="测试数值" prop="testValue">
      <el-input
        v-model="form.testValue"
        placeholder="请输入测试数值（含单位，如：99.5%）"
        :disabled="readonly"
      />
    </el-form-item>
    <el-form-item label="是否合格" prop="qualified">
      <el-radio-group v-model="form.qualified" :disabled="readonly">
        <el-radio label="yes">合格</el-radio>
        <el-radio label="no">不合格</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="检测人" prop="inspector">
      <el-input v-model="form.inspector" placeholder="请输入检测人" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="报告编号" prop="reportNo">
      <el-input v-model="form.reportNo" placeholder="请输入报告编号" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="备注" prop="remark">
      <el-input
        v-model="form.remark"
        type="textarea"
        placeholder="请输入备注"
        :disabled="readonly"
      />
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: 'TestForm',
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
        testItems: '',
        testValue: '',
        qualified: '',
        inspector: '',
        reportNo: '',
        remark: ''
      },
      rules: {
        testItems: [{ required: true, message: '请输入测试项目', trigger: 'blur' }],
        testValue: [{ required: true, message: '请输入测试数值', trigger: 'blur' }],
        qualified: [{ required: true, message: '请选择是否合格', trigger: 'change' }],
        inspector: [{ required: true, message: '请输入检测人', trigger: 'blur' }],
        reportNo: [{ required: true, message: '请输入报告编号', trigger: 'blur' }]
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
