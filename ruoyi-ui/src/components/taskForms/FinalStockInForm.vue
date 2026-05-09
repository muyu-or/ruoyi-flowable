<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="产品名称" prop="productName">
      <el-input v-model="form.productName" placeholder="请输入产品名称" :disabled="readonly" />
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
    <el-form-item label="成本单价" prop="unitCost">
      <el-input-number
        v-model="form.unitCost"
        :min="0"
        :precision="2"
        :disabled="readonly"
        placeholder="请输入成本单价（元，选填）"
        style="width: 100%"
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
        :disabled="readonly"
      />
    </el-form-item>
    <el-form-item label="产品入库报告">
      <report-uploader
        v-model="form.reports"
        :readonly="readonly"
        :material-name="form.productName"
        :material-quantity="form.inQuantity"
        node-name="产品入库"
        test-type="产品入库"
      />
    </el-form-item>
  </el-form>
</template>

<script>
import ReportUploader from './ReportUploader.vue'
export default {
  name: 'FinalStockInForm',
  components: { ReportUploader },
  dicts: ['warehouse_area', 'material_category', 'material_subcategory'],
  data() {
    return {
      readonly: false,
      form: {
        productName: '',
        materialCategory: '',
        materialSubcategory: '',
        inQuantity: 0,
        warehouseArea: '',
        unitCost: null,
        operator: '',
        remark: '',
        reportIds: [],
        reports: []
      },
      rules: {
        productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
        materialCategory: [{ required: true, message: '请选择物料大类', trigger: 'change' }],
        materialSubcategory: [{ required: true, message: '请选择物料子类', trigger: 'change' }],
        inQuantity: [{
          required: true,
          validator: (rule, value, callback) => {
            if (value === undefined || value === null || value <= 0) {
              callback(new Error('请输入大于0的入库数量'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }],
        warehouseArea: [{ required: true, message: '请选择库区', trigger: 'change' }]
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
            const result = { ...this.form }
            if (Array.isArray(result.reports)) {
              result.reports = result.reports.map(r => Object.assign({}, r, {
                materialName: this.form.productName || r.materialName || '',
                materialQuantity: this.form.inQuantity != null ? this.form.inQuantity : r.materialQuantity
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
    }
  }
}
</script>
