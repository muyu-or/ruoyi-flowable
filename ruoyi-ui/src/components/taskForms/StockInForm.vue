<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="物料名称" prop="materialName">
      <el-input
        v-model="form.materialName"
        placeholder="请输入物料名称"
        :disabled="readonly"
      />
    </el-form-item>

    <el-form-item label="物料大类" prop="materialCategory">
      <el-select
        v-model="form.materialCategory"
        placeholder="请选择物料大类"
        :disabled="readonly"
        style="width: 100%"
      >
        <el-option
          v-for="dict in dict.type.material_category"
          :key="dict.value"
          :label="dict.label"
          :value="dict.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="物料子类" prop="materialSubcategory">
      <el-select
        v-model="form.materialSubcategory"
        placeholder="请选择物料子类"
        :disabled="readonly"
        style="width: 100%"
      >
        <el-option
          v-for="dict in dict.type.material_subcategory"
          :key="dict.value"
          :label="dict.label"
          :value="dict.value"
        />
      </el-select>
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

    <el-form-item label="入库类型" prop="inboundType">
      <el-select
        v-model="form.inboundType"
        placeholder="请选择入库类型"
        :disabled="readonly"
        style="width: 100%"
      >
        <el-option
          v-for="dict in dict.type.inbound_type"
          :key="dict.value"
          :label="dict.label"
          :value="dict.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="入库数量" prop="quantity">
      <el-input-number
        v-model="form.quantity"
        :min="1"
        :precision="2"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>

    <el-form-item label="检测说明" prop="checkDescription">
      <el-input
        v-model="form.checkDescription"
        type="textarea"
        placeholder="请输入检测说明"
        :rows="3"
        :disabled="readonly"
      />
    </el-form-item>

    <el-form-item label="操作人" prop="operator">
      <el-input v-model="form.operator" :disabled="true" />
    </el-form-item>

    <el-form-item label="备注" prop="remark">
      <el-input
        v-model="form.remark"
        type="textarea"
        placeholder="请输入备注"
        :rows="3"
        :disabled="readonly"
      />
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: 'StockInForm',
  dicts: ['material_category', 'material_subcategory', 'warehouse_area', 'inbound_type'],
  data() {
    return {
      readonly: false,
      form: {
        materialName: '',
        materialCategory: '',
        materialSubcategory: '',
        warehouseArea: '',
        inboundType: '',
        quantity: null,
        checkDescription: '',
        operator: '',
        remark: ''
      },
      rules: {
        materialName: [
          { required: true, message: '请输入物料名称', trigger: 'blur' }
        ],
        materialCategory: [
          { required: true, message: '请选择物料大类', trigger: 'change' }
        ],
        materialSubcategory: [
          { required: true, message: '请选择物料子类', trigger: 'change' }
        ],
        warehouseArea: [
          { required: true, message: '请选择库区', trigger: 'change' }
        ],
        inboundType: [
          { required: true, message: '请选择入库类型', trigger: 'change' }
        ],
        quantity: [
          {
            required: true,
            validator: (rule, value, callback) => {
              if (value === undefined || value === null || value <= 0) {
                callback(new Error('请输入大于0的入库数量'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        checkDescription: [
          { required: true, message: '请输入检测说明', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.form.operator = this.$store.state.user.name || ''
  },
  methods: {
    getFormData() {
      if (this.readonly) {
        return Promise.resolve(Object.assign({}, this.form))
      }
      return new Promise((resolve, reject) => {
        this.$refs.formRef.validate((valid) => {
          if (valid) {
            resolve(Object.assign({}, this.form))
          } else {
            reject(new Error('表单校验失败'))
          }
        })
      })
    },
    setFormData(data) {
      if (!data) return
      const keys = Object.keys(this.form)
      for (const key of keys) {
        if (Object.prototype.hasOwnProperty.call(data, key)) {
          this.$set(this.form, key, data[key])
        }
      }
    },
    setReadonly(val) {
      this.readonly = val
    }
  }
}
</script>
