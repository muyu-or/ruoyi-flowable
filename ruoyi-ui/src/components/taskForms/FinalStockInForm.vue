<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
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
    <el-form-item label="库区" prop="warehouseArea">
      <el-select
        v-model="form.warehouseArea"
        placeholder="请选择库区"
        :disabled="readonly"
        style="width: 100%"
      >
        <el-option
          v-for="dict in dict.type.warehouse_area"
          :key="dict.value"
          :label="dict.label"
          :value="dict.value"
        />
      </el-select>
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
  dicts: ['warehouse_area'],
  data() {
    return {
      readonly: false,
      form: {
        productName: '',
        inQuantity: 0,
        warehouseArea: '',
        operator: '',
        remark: ''
      },
      rules: {
        productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
        inQuantity: [{
          validator: (rule, value, callback) => {
            if (value === undefined || value === null || value <= 0) {
              callback(new Error('请输入大于0的入库数量'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }],
        warehouseArea: [{ required: true, message: '请选择库区', trigger: 'change' }],
        operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
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
