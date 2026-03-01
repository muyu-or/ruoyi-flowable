<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="产品名称" prop="productName">
      <el-input v-model="form.productName" placeholder="请输入产品名称" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="入库数量" prop="inQuantity">
      <el-input-number
        v-model="form.inQuantity"
        :min="0"
        :precision="2"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="存放位置" prop="storageLocation">
      <el-input
        v-model="form.storageLocation"
        placeholder="请输入存放位置（如：A区-货架3）"
        :disabled="readonly"
      />
    </el-form-item>
    <el-form-item label="操作人" prop="operator">
      <el-input v-model="form.operator" placeholder="请输入操作人" :disabled="readonly" />
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
  name: 'FinalStockInForm',
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
        productName: '',
        inQuantity: 0,
        storageLocation: '',
        operator: '',
        remark: ''
      },
      rules: {
        productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
        inQuantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
        storageLocation: [{ required: true, message: '请输入存放位置', trigger: 'blur' }],
        operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
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
