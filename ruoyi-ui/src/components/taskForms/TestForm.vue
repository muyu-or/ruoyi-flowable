<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="150px" label-position="right">
    <el-form-item label="表面打点情况" prop="surfaceDots">
      <el-radio-group v-model="form.surfaceDots" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="附着力" prop="adhesion">
      <el-radio-group v-model="form.adhesion" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="膜层表面粗糙度" prop="coatingRoughness">
      <el-radio-group v-model="form.coatingRoughness" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="光谱测试" prop="spectrumTest">
      <el-radio-group v-model="form.spectrumTest" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="面形测试" prop="surfaceShapeTest">
      <el-radio-group v-model="form.surfaceShapeTest" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="CRD测试" prop="crdTest">
      <el-radio-group v-model="form.crdTest" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="损伤测试" prop="damageTest">
      <el-radio-group v-model="form.damageTest" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="弱吸收测试" prop="weakAbsorptionTest">
      <el-radio-group v-model="form.weakAbsorptionTest" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="温升测试" prop="temperatureRiseTest">
      <el-radio-group v-model="form.temperatureRiseTest" :disabled="readonly" class="inline-radio">
        <el-radio label="pass">通过</el-radio>
        <el-radio label="fail">不通过</el-radio>
        <el-radio label="na">不适用</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="检测人" prop="inspector">
      <el-input v-model="form.inspector" :disabled="true" />
    </el-form-item>
    <el-form-item label="备注" prop="remark">
      <el-input
        v-model="form.remark"
        type="textarea"
        placeholder="请输入备注"
        :disabled="readonly"
      />
    </el-form-item>
    <el-form-item label="检测报告">
      <report-uploader
        v-model="form.reports"
        :readonly="readonly"
        :material-name="extraContext.materialName"
        :material-quantity="extraContext.materialQuantity"
        node-name="检测"
        test-type="检测"
      />
    </el-form-item>
  </el-form>
</template>

<script>
import ReportUploader from './ReportUploader.vue'
export default {
  name: 'TestForm',
  components: { ReportUploader },
  data() {
    return {
      readonly: false,
      extraContext: { materialName: '', materialQuantity: null },
      form: {
        surfaceDots: '',
        adhesion: '',
        coatingRoughness: '',
        spectrumTest: '',
        surfaceShapeTest: '',
        crdTest: '',
        damageTest: '',
        weakAbsorptionTest: '',
        temperatureRiseTest: '',
        inspector: '',
        remark: '',
        reportIds: [],
        reports: []
      },
      rules: {}
    }
  },
  created() {
    this.form.inspector = this.$store.state.user.name || ''
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
                materialName: this.extraContext.materialName || r.materialName || '',
                materialQuantity: this.extraContext.materialQuantity != null ? this.extraContext.materialQuantity : r.materialQuantity
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
    },
    setExtraContext(ctx) {
      this.extraContext = ctx || { materialName: '', materialQuantity: null }
    }
  }
}
</script>

<style scoped>
.inline-radio {
  white-space: nowrap;
}
.inline-radio .el-radio {
  margin-right: 20px;
}
</style>