<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="120px">
    <el-form-item label="原料名称" prop="materialName">
      <el-select
        v-model="form.materialName"
        placeholder="请选择原料"
        :disabled="readonly"
        style="width: 100%"
      >
        <el-option
          v-for="item in inventoryList"
          :key="item.inventoryName"
          :label="item.inventoryName"
          :value="item.inventoryName"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="批次号" prop="batchNo">
      <el-input v-model="form.batchNo" placeholder="请输入批次号" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="入库数量" prop="quantity">
      <el-input-number
        v-model="form.quantity"
        :min="0"
        :precision="2"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="检测结果" prop="checkResult">
      <el-radio-group v-model="form.checkResult" :disabled="readonly">
        <el-radio label="qualified">合格</el-radio>
        <el-radio label="unqualified">不合格</el-radio>
      </el-radio-group>
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
import { listInventory } from '@/api/manage/inventory'

export default {
  name: 'StockInForm',
  props: {
    taskData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      readonly: false,
      inventoryList: [],
      form: {
        materialName: '',
        batchNo: '',
        quantity: 0,
        checkResult: '',
        remark: ''
      },
      rules: {
        materialName: [{ required: true, message: '请选择原料名称', trigger: 'change' }],
        batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
        quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
        checkResult: [{ required: true, message: '请选择检测结果', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadInventoryList()
  },
  methods: {
    loadInventoryList() {
      listInventory().then(res => {
        this.inventoryList = res.rows || []
      }).catch(() => {
        this.$message.error('加载原料列表失败')
      })
    },
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
