<template>
  <div>
    <el-form label-width="80px" size="small" @submit.native.prevent>
      <el-form-item label="流程表单">
        <el-select v-model="bpmnFormData.formKey" clearable class="m-2" placeholder="挂载节点表单" style="width:100%" @change="updateElementFormKey">
          <el-option
              v-for="item in formList"
              :key="item.value"
              :label="item.formName"
              :value="item.formId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="自定义表单">
        <el-select v-model="bpmnFormData.formComponent" clearable placeholder="选择自定义组件" style="width:100%" @change="updateFormComponent">
          <el-option
              v-for="item in formComponentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>

import { listAllForm } from '@/api/flowable/form'
import {StrUtil} from "@/utils/StrUtil";
export default {
  name: "FormPanel",
  /** 组件传值  */
  props : {
    id: {
      type: String,
      required: true
    },
  },
  data() {
    return {
      formList: [],
      formComponentOptions: [
        { label: 'MainForm（主表单）',            value: 'MainForm' },
        { label: 'StockInForm（原料检测入库）',  value: 'StockInForm' },
        { label: 'StockOutForm（出库）',         value: 'StockOutForm' },
        { label: 'PreprocessForm（预处理）',     value: 'PreprocessForm' },
        { label: 'VacuumForm（真空处理）',       value: 'VacuumForm' },
        { label: 'BakingForm（烘烤镀膜）',       value: 'BakingForm' },
        { label: 'TestForm（检测）',             value: 'TestForm' },
        { label: 'FinalStockInForm（产品入库）', value: 'FinalStockInForm' }
      ],
      bpmnFormData: {
        formKey: '',
        formComponent: ''
      }
    }
  },

  /** 传值监听 */
  watch: {
    id: {
      handler(newVal) {
        if (StrUtil.isNotBlank(newVal)) {
          this.getListForm();
          this.resetFlowForm();
          this.resetFormComponent();
        }
      },
      immediate: true, // 立即生效
    },
  },
  created() {

  },
  methods: {

    // 方法区
    resetFlowForm() {
      this.bpmnFormData.formKey = this.modelerStore.element.businessObject.formKey;
    },

    updateElementFormKey(val) {
      if (StrUtil.isBlank(val)) {
        delete this.modelerStore.element.businessObject[`formKey`]
      } else {
        this.modelerStore.modeling.updateProperties(this.modelerStore.element, {'formKey': val});
      }
    },

    // 获取表单信息
    getListForm() {
      listAllForm().then(res => {
        res.data.forEach(item => {
          item.formId = item.formId.toString();
        })
        this.formList = res.data;
      })
    },

    // 回显：从节点 extensionElements 中读取已保存的组件名
    resetFormComponent() {
      const existing = this.modelerStore.element.businessObject
        ?.extensionElements?.values
        ?.find(ex => ex.$type === 'flowable:FormComponent')
      this.$set(this.bpmnFormData, 'formComponent', existing ? existing.value : '')
    },

    // 写入：将组件名保存到节点 extensionElements
    updateFormComponent(val) {
      const otherExtensions = this.modelerStore.element.businessObject
        ?.extensionElements?.values
        ?.filter(ex => ex.$type !== 'flowable:FormComponent') ?? []

      if (!val) {
        if (otherExtensions.length === 0) {
          this.modelerStore.modeling.updateProperties(this.modelerStore.element, {
            extensionElements: null
          })
        } else {
          const extensions = this.modelerStore.moddle.create('bpmn:ExtensionElements', {
            values: otherExtensions
          })
          this.modelerStore.modeling.updateProperties(this.modelerStore.element, {
            extensionElements: extensions
          })
        }
      } else {
        const newElem = this.modelerStore.moddle.create('flowable:FormComponent', { value: val })
        const extensions = this.modelerStore.moddle.create('bpmn:ExtensionElements', {
          values: otherExtensions.concat([newElem])
        })
        this.modelerStore.modeling.updateProperties(this.modelerStore.element, {
          extensionElements: extensions
        })
      }
    },
  }
}


</script>
