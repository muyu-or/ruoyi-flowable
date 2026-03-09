<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" size="small">
    <el-form-item label="任务名称" prop="taskName">
      <el-input v-model="form.taskName" placeholder="请输入任务名称" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="流程名称" prop="procName">
      <el-input v-model="form.procName" placeholder="请输入流程名称" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="流程日期" prop="procDateRange">
      <el-date-picker
        v-model="form.procDateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        style="width: 100%"
        :disabled="readonly"
      />
    </el-form-item>
    <el-form-item label="操作人">
      <el-input :value="operatorName" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="备注">
      <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注（选填）" :disabled="readonly" />
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: 'MainForm',
  props: {
    // 流程名称可由父组件传入预填
    initialProcName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      readonly: false,
      form: {
        taskName: '',
        procName: '',
        procDateRange: [],
        remark: ''
      },
      rules: {
        taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        procName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
        procDateRange: [{ required: true, message: '请选择流程日期范围', trigger: 'change' }]
      }
    }
  },
  computed: {
    operatorName() {
      return (this.$store.state.user && this.$store.state.user.name) || ''
    }
  },
  watch: {
    initialProcName: {
      immediate: true,
      handler(val) {
        if (val) {
          this.$set(this.form, 'procName', val)
        }
      }
    }
  },
  methods: {
    /** 获取表单数据（含校验），返回 Promise */
    getFormData() {
      if (this.readonly) {
        return Promise.resolve(Object.assign({}, this.form))
      }
      return new Promise((resolve, reject) => {
        this.$refs.formRef.validate((valid) => {
          if (valid) {
            const data = Object.assign({}, this.form)
            // 拆分日期区间为独立字段
            if (Array.isArray(data.procDateRange) && data.procDateRange.length === 2) {
              data.procDateStart = data.procDateRange[0]
              data.procDateEnd = data.procDateRange[1]
            }
            delete data.procDateRange
            resolve(data)
          } else {
            reject(new Error('表单校验失败'))
          }
        })
      })
    },
    /** 回填表单数据 */
    setFormData(data) {
      if (!data) return
      const keys = Object.keys(this.form)
      for (const key of keys) {
        if (Object.prototype.hasOwnProperty.call(data, key)) {
          this.$set(this.form, key, data[key])
        }
      }
      // 兼容：procDateStart/procDateEnd 回填到 procDateRange
      if (data.procDateStart && data.procDateEnd) {
        this.$set(this.form, 'procDateRange', [data.procDateStart, data.procDateEnd])
      }
    },
    /** 设置只读模式 */
    setReadonly(val) {
      this.readonly = val
    }
  }
}
</script>
