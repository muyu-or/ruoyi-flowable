<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="物料名称" prop="materialId">
      <el-select
        v-model="form.materialId"
        placeholder="请选择物料"
        :disabled="readonly"
        style="width: 100%"
        filterable
        @change="handleMaterialChange"
      >
        <el-option
          v-for="item in inventoryList"
          :key="item.materialId"
          :label="item.materialName"
          :value="item.materialId"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="物料ID">
      <el-input v-model="form.materialId" :disabled="true" placeholder="选择物料后自动填入" />
    </el-form-item>

    <el-form-item label="物料大类">
      <el-input v-model="form.materialCategory" :disabled="true" placeholder="选择物料后自动填入" />
    </el-form-item>

    <el-form-item label="当前库存">
      <el-input v-model="form.currentQuantity" :disabled="true" placeholder="选择物料后自动填入" />
    </el-form-item>

    <el-form-item label="库区">
      <el-input v-model="form.warehouseArea" :disabled="true" placeholder="选择物料后自动填入" />
    </el-form-item>

    <el-form-item label="出库数量" prop="outQuantity">
      <el-input-number
        v-model="form.outQuantity"
        :min="1"
        :precision="2"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>

    <el-form-item label="出库类型" prop="outboundType">
      <el-select
        v-model="form.outboundType"
        placeholder="请选择出库类型"
        :disabled="readonly"
        style="width: 100%"
      >
        <el-option
          v-for="d in dict.type.outbound_type"
          :key="d.value"
          :label="d.label"
          :value="d.value"
        />
      </el-select>
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
    <el-form-item label="测试报告">
      <report-uploader
        v-model="form.reports"
        :readonly="readonly"
        :material-name="form.materialName"
        :material-quantity="form.outQuantity"
        node-name="出库"
        test-type="出库"
      />
    </el-form-item>
  </el-form>
</template>

<script>
import { listInventory } from '@/api/manage/inventory'
import ReportUploader from './ReportUploader.vue'

export default {
  name: 'StockOutForm',
  components: { ReportUploader },
  dicts: ['outbound_type'],
  props: {
    initialMaterialId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      readonly: false,
      inventoryList: [],
      form: {
        materialId: '',
        materialName: '',
        materialCategory: '',
        currentQuantity: 0,
        warehouseArea: '',
        outQuantity: null,
        outboundType: '',
        operator: '',
        remark: '',
        reportIds: [],
        reports: []
      },
      rules: {
        materialId: [
          { required: true, message: '请选择物料', trigger: 'change' }
        ],
        outQuantity: [
          {
            required: true,
            validator: (rule, value, callback) => {
              if (value === undefined || value === null || value <= 0) {
                callback(new Error('请输入大于0的出库数量'))
              } else if (this.form.currentQuantity > 0 && value > this.form.currentQuantity) {
                callback(new Error(`出库数量不能超过当前库存 ${this.form.currentQuantity}`))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ],
        outboundType: [
          { required: true, message: '请选择出库类型', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.loadInventoryList()
    this.form.operator = this.$store.state.user.name || ''
  },
  methods: {
    loadInventoryList() {
      listInventory({ pageNum: 1, pageSize: 200 }).then(res => {
        this.inventoryList = res.rows || []
        // 如果从库存页面传入了物料ID，自动选中
        if (this.initialMaterialId) {
          this.$set(this.form, 'materialId', this.initialMaterialId)
          this.handleMaterialChange(this.initialMaterialId)
        }
      }).catch(() => {
        this.$message.error('加载库存列表失败')
      })
    },
    handleMaterialChange(materialId) {
      const item = this.inventoryList.find(i => i.materialId === materialId)
      if (item) {
        this.$set(this.form, 'materialName', item.materialName || '')
        this.$set(this.form, 'materialCategory', item.materialCategory || '')
        this.$set(this.form, 'currentQuantity', item.currentQuantity || 0)
        this.$set(this.form, 'warehouseArea', item.warehouseArea || '')
      } else {
        this.$set(this.form, 'materialName', '')
        this.$set(this.form, 'materialCategory', '')
        this.$set(this.form, 'currentQuantity', 0)
        this.$set(this.form, 'warehouseArea', '')
      }
    },
    getFormData() {
      if (this.readonly) return Promise.resolve({ ...this.form })
      return new Promise((resolve, reject) => {
        this.$refs.formRef.validate((valid) => {
          if (valid) resolve({ ...this.form })
          else reject(new Error('表单校验失败'))
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
