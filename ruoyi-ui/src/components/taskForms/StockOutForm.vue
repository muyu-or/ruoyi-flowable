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
    <el-form-item label="出库数量" prop="outQuantity">
      <el-input-number
        v-model="form.outQuantity"
        :min="0"
        :precision="2"
        :disabled="readonly"
        style="width: 100%"
      />
    </el-form-item>
    <el-form-item label="领用人" prop="recipient">
      <el-input v-model="form.recipient" placeholder="请输入领用人" :disabled="readonly" />
    </el-form-item>
    <el-form-item label="用途" prop="purpose">
      <el-input v-model="form.purpose" placeholder="请输入用途" :disabled="readonly" />
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
  name: 'StockOutForm',
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
        outQuantity: 0,
        recipient: '',
        purpose: '',
        remark: ''
      },
      rules: {
        materialName: [{ required: true, message: '请选择原料名称', trigger: 'change' }],
        outQuantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }],
        recipient: [{ required: true, message: '请输入领用人', trigger: 'blur' }],
        purpose: [{ required: true, message: '请输入用途', trigger: 'blur' }]
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
        this.$message.error('加载库存列表失败')
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
