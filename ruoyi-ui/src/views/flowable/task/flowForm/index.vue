<template>
  <div>
  <v-form-designer ref="vfDesigner" :designer-config="designerConfig">
    <!-- 保存按钮 -->
    <template #customSaveButton>
      <el-button type="text" @click="saveFormJson"><i class="el-icon-s-promotion" />保存</el-button>
    </template>
  </v-form-designer>

  <!--系统表单信息-->
  <el-dialog :title="formTitle" :visible.sync="formOpen" width="500px" append-to-body>
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="表单名称" prop="formName">
        <el-input v-model="form.formName" placeholder="请输入表单名称" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div>
  </el-dialog>
  </div>
</template>

<script>
import {addForm, getForm, updateForm} from "@/api/flowable/form";
import { StrUtil } from '@/utils/StrUtil'

export default {
  name: "flowForm",
  data() {
    return {
      formTitle: "",
      formOpen: false,
      // 表单校验
      rules: {
        formName: [
          { required: true, message: "表单名称不能为空", trigger: "blur" }
        ]
      },
      // 表单参数
      form: {
        formId: null,
        formName: null,
        formContent: null,
        remark: null
      },
      designerConfig: {
        generateSFCButton: false,
        exportCodeButton: false,  //是否显示导出代码按钮
        toolbarMaxWidth: 320,
        toolbarMinWidth: 300,  //设计器工具按钮栏最小宽度（单位像素）
        formHeader: false,
      },
    }
  },
  mounted() {
    const formId = this.$route.query && this.$route.query.formId;
    if (StrUtil.isNotBlank(formId)) {
      getForm(formId).then(res => {
        this.$nextTick(() => {
          // 加载表单json数据 - 添加容错处理
          try {
            let formJson = res.data.formContent;
            // 如果是字符串，则 parse；如果已是对象，直接使用
            if (typeof formJson === 'string') {
              formJson = JSON.parse(formJson);
            }
            // 验证必要字段
            if (!formJson || !formJson.widgetList || !formJson.formConfig) {
              console.warn('表单格式不完整，使用默认空表单');
              formJson = {
                "widgetList": [],
                "formConfig": {
                  "modelName": "formData",
                  "refName": "vForm",
                  "rulesName": "rules",
                  "labelWidth": 80,
                  "labelPosition": "left",
                  "size": "",
                  "labelAlign": "label-left-align",
                  "cssCode": "",
                  "customClass": "",
                  "functions": "",
                  "layoutType": "PC",
                  "onFormCreated": "",
                  "onFormMounted": "",
                  "onFormDataChange": "",
                  "onFormValidate": ""
                }
              };
            }
            this.$refs.vfDesigner.setFormJson(formJson);
          } catch (error) {
            console.error('加载表单失败:', error);
            this.$message.error('表单格式错误，请重新设计表单');
            // 使用默认空表单
            this.$refs.vfDesigner.setFormJson({
              "widgetList": [],
              "formConfig": {
                "modelName": "formData",
                "refName": "vForm",
                "rulesName": "rules",
                "labelWidth": 80,
                "labelPosition": "left",
                "size": "",
                "labelAlign": "label-left-align",
                "cssCode": "",
                "customClass": "",
                "functions": "",
                "layoutType": "PC",
                "onFormCreated": "",
                "onFormMounted": "",
                "onFormDataChange": "",
                "onFormValidate": ""
              }
            });
          }
        })
        this.form = res.data;
      })
    }else {
      this.$nextTick(() => {
        // 加载表单json数据
        this.$refs.vfDesigner.setFormJson({"widgetList":[],"formConfig":{"modelName":"formData","refName":"vForm","rulesName":"rules","labelWidth":80,"labelPosition":"left","size":"","labelAlign":"label-left-align","cssCode":"","customClass":"","functions":"","layoutType":"PC","onFormCreated":"","onFormMounted":"","onFormDataChange":"","onFormValidate":""}})
      })
    }
  },
  methods:{
    // 保存表单数据
    saveFormJson() {
      let formJson = this.$refs.vfDesigner.getFormJson()

      // 调试：打印获取的数据
      console.log('保存前的表单数据:', formJson)
      console.log('表单数据类型:', typeof formJson)

      // 验证必要字段
      if (!formJson || typeof formJson !== 'object') {
        this.$message.error('表单数据获取失败，请确保设计器正确加载')
        console.error('表单格式错误：formJson 不是对象')
        return
      }

      if (!formJson.widgetList || !Array.isArray(formJson.widgetList)) {
        this.$message.error('表单缺少 widgetList 字段')
        console.error('缺少 widgetList 或不是数组')
        return
      }

      if (!formJson.formConfig || typeof formJson.formConfig !== 'object') {
        this.$message.error('表单缺少 formConfig 字段')
        console.error('缺少 formConfig 或不是对象')
        return
      }

      // 确保数据完整，然后序列化
      try {
        // 使用完整的 JSON.stringify，不省略任何字段
        const contentStr = JSON.stringify(formJson)
        console.log('序列化后的长度:', contentStr.length)
        console.log('序列化后的内容:', contentStr)

        // 设置表单数据
        this.form.formContent = contentStr

        // 验证设置是否成功
        console.log('form.formContent 已设置:', this.form.formContent.length, '字符')

        this.formOpen = true
      } catch (error) {
        console.error('序列化失败:', error)
        this.$message.error('表单数据序列化失败：' + error.message)
      }
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 再次验证 formContent
          if (!this.form.formContent) {
            this.$message.error('表单内容为空，请先保存表单')
            return
          }

          console.log('提交时的 formContent:', this.form.formContent.length, '字符')

          if (this.form.formId != null) {
            updateForm(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.formOpen = false;
            }).catch(error => {
              console.error('更新表单失败:', error)
              this.$message.error('更新表单失败')
            });
          } else {
            addForm(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.formOpen = false;
            }).catch(error => {
              console.error('新增表单失败:', error)
              this.$message.error('新增表单失败')
            });
          }
          // 关闭当前标签页并返回上个页面
          const obj = { path: "/flowable/form", query: { t: Date.now()} };
          this.$tab.closeOpenPage(obj);
        }
      });
    },
    // 取消按钮
    cancel() {
      this.formOpen = false;
      this.reset();
    },
  }
}
</script>

<style lang="scss" scoped>
body {
  margin: 0;  /* 如果页面出现垂直滚动条，则加入此行CSS以消除之 */
}
.el-container.main-container{
  background: #fff;
  margin-left: 0 !important;
}

</style>
