<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true" label-width="68px">
      <el-form-item label="表单名称" prop="formName">
        <el-input
          v-model="queryParams.formName"
          placeholder="请输入表单名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-s-grid"
          size="mini"
          @click="handleRegisterComponent"
        >注册自定义表单</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['flowable:form:remove']"
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="formList" border v-table-col-width="'main'" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="表单主键" align="center" prop="formId" />
      <el-table-column label="表单名称" align="center" prop="formName" />
      <el-table-column label="类型" align="center" width="100">
        <template slot-scope="scope">
          <el-tag v-if="!scope.row.formType || scope.row.formType === 'vform'" type="primary" size="small">vForm</el-tag>
          <el-tag v-else type="success" size="small">自定义组件</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="组件名" align="center" prop="formComponent" width="160">
        <template slot-scope="scope">
          <span v-if="scope.row.formComponent">{{ scope.row.formComponent }}</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            class="view-btn"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            v-if="!scope.row.formType || scope.row.formType === 'vform'"
            v-hasPermi="['flowable:form:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            class="edit-btn"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-hasPermi="['flowable:form:remove']"
            size="mini"
            type="text"
            icon="el-icon-delete"
            class="delete-btn"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改流程表单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="form.formName" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="表单内容">
          <editor v-model="form.formContent" :min-height="192" />
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

    <!--表单详情-->
    <el-dialog :title="formTitle" :visible.sync="formRenderOpen" width="60%" append-to-body>
      <v-form-render ref="vFormRef" :form-data="formData" />
    </el-dialog>

    <!--表单设计器-->
    <el-dialog
      custom-class="dialogClass"
      :visible.sync="dialogVisible"
      :close-on-press-escape="false"
      :fullscreen="true"
      :before-close="handleClose"
      append-to-body
    >
      <v-form-designer ref="vfDesigner" :designer-config="designerConfig">
        <!-- 自定义按钮插槽演示 -->
        <template #customSaveButton>
          <el-button type="text" @click="saveFormJson"><i class="el-icon-s-promotion" />保存</el-button>
        </template>
      </v-form-designer>
    </el-dialog>

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

    <!-- 自定义Vue组件预览 -->
    <el-dialog :title="componentPreviewTitle" :visible.sync="componentPreviewOpen" width="60%" append-to-body>
      <component
        :is="componentPreviewName"
        v-if="componentPreviewOpen && componentPreviewName"
        ref="componentPreviewRef"
      />
    </el-dialog>

    <!-- 注册自定义表单弹框 -->
    <el-dialog title="注册自定义表单" :visible.sync="registerOpen" width="480px" append-to-body>
      <el-form ref="registerForm" :model="registerForm" :rules="registerRules" label-width="100px">
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="registerForm.formName" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="表单组件" prop="formComponent">
          <el-select v-model="registerForm.formComponent" placeholder="请选择自定义表单组件" style="width:100%">
            <el-option
              v-for="item in formComponentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="registerForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="registerOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitRegister">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listForm, delForm, addForm, updateForm } from '@/api/flowable/form'
import Editor from '@/components/Editor'
import { TASK_FORM_COMPONENT_MAP } from '@/components/taskForms/index'
export default {
  name: 'Form',
  components: {
    Editor,
    ...TASK_FORM_COMPONENT_MAP
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      dialogVisible: false,
      designerConfig: {
        exportCodeButton: false // 是否显示导出代码按钮
      },
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 流程表单表格数据
      formList: [],
      // 弹出层标题
      title: '',
      formRenderOpen: false,
      formTitle: '',
      formOpen: false,
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        formName: null,
        formContent: null
      },
      // 表单参数
      form: {
        formId: null,
        formName: null,
        formContent: null,
        remark: null
      },
      // 表单校验
      rules: {},
      formData: {},
      // 自定义组件预览
      componentPreviewOpen: false,
      componentPreviewTitle: '',
      componentPreviewName: '',
      // 注册自定义表单弹框
      registerOpen: false,
      registerForm: {
        formName: '',
        formComponent: '',
        remark: ''
      },
      registerRules: {
        formName: [{ required: true, message: '请输入表单名称', trigger: 'blur' }],
        formComponent: [{ required: true, message: '请选择表单组件', trigger: 'change' }]
      },
      // 自定义组件选项
      formComponentOptions: [
        { label: 'MainForm（主表单）', value: 'MainForm' },
        { label: 'StockInForm（原料检测入库）', value: 'StockInForm' },
        { label: 'StockOutForm（出库）', value: 'StockOutForm' },
        { label: 'PreprocessForm（预处理）', value: 'PreprocessForm' },
        { label: 'VacuumForm（真空处理-旧）', value: 'VacuumForm' },
        { label: 'BakingForm（烘烤镀膜-旧）', value: 'BakingForm' },
        { label: 'TestForm（检测）', value: 'TestForm' },
        { label: 'FinalStockInForm（产品入库）', value: 'FinalStockInForm' },
        { label: 'DrawingForm（图纸下发）', value: 'DrawingForm' },
        { label: 'CoatingFixtureForm（镀膜工装设计）', value: 'CoatingFixtureForm' },
        { label: 'FilmDesignForm（膜系设计）', value: 'FilmDesignForm' },
        { label: 'VacuumBakingForm（开机真空烘烤）', value: 'VacuumBakingForm' }
      ]
    }
  },
  created() {
    this.getList()
  },
  activated() {
    const time = this.$route.query.t
    if (time != null) {
      this.getList()
    }
  },
  methods: {
    /** 查询流程表单列表 */
    getList() {
      this.loading = true
      listForm(this.queryParams).then(response => {
        this.formList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 表单重置
    reset() {
      this.form = {
        formId: null,
        formName: null,
        formContent: null,
        createTime: null,
        updateTime: null,
        createBy: null,
        updateBy: null,
        remark: null
      }
      this.resetForm('form')
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.formId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleDetail(row) {
      if (!row.formType || row.formType === 'vform') {
        this.formRenderOpen = true
        this.formTitle = '表单详情 — ' + row.formName
        this.$nextTick(() => {
          this.$refs.vFormRef.setFormJson(JSON.parse(row.formContent))
          this.$nextTick(() => {
            this.$refs.vFormRef.disableForm()
          })
        })
      } else {
        this.componentPreviewName = row.formComponent
        this.componentPreviewTitle = '表单预览 — ' + row.formName
        this.componentPreviewOpen = true
        this.$nextTick(() => {
          if (this.$refs.componentPreviewRef && this.$refs.componentPreviewRef.setReadonly) {
            this.$refs.componentPreviewRef.setReadonly(true)
          }
        })
      }
    },
    /** 新增按钮操作 */
    handleAdd() {
      // this.dialogVisible = true;
      this.$router.push({ path: '/flowable/task/flowForm/index' })
    },
    // 保存表单数据
    saveFormJson() {
      const formJson = this.$refs.vfDesigner.getFormJson()
      this.form.formContent = JSON.stringify(formJson)
      this.formOpen = true
    },
    // 取消按钮
    cancel() {
      this.formOpen = false
      this.reset()
    },
    handleClose(done) {
      this.$confirm('确定要关闭吗？关闭未保存的修改都会丢失？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        done()
      }).catch(() => {})
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      // this.form = row;
      // this.dialogVisible = true;
      // this.$nextTick(() => {
      //   // 加载表单json数据
      //   this.$refs.vfDesigner.setFormJson(JSON.parse(row.formContent))
      // })
      this.$router.push({ path: '/flowable/task/flowForm/index', query: { formId: row.formId }})
    },
    /** 重置表单 */
    resetFormData() {
      this.$refs.vFormRef.resetForm()
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.formId != null) {
            updateForm(this.form).then(response => {
              this.$modal.msgSuccess('修改成功')
              this.formOpen = false
              this.getList()
            }).catch(() => {
              this.$modal.msgError('修改失败，请重试')
            })
          } else {
            addForm(this.form).then(response => {
              this.$modal.msgSuccess('新增成功')
              this.formOpen = false
              this.getList()
            }).catch(() => {
              this.$modal.msgError('新增失败，请重试')
            })
          }
          this.dialogVisible = false
        }
      })
    },
    /** 提交按钮 */
    submitFormData() {
      this.$refs.vFormRef.getFormData().then(formData => {
        // Form Validation OK
        console.log(JSON.stringify(formData))
      }).catch(error => {
        // Form Validation failed
        this.$modal.msgError(error)
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const formIds = row.formId || this.ids
      this.$confirm('是否确认删除表单编号为"' + formIds + '"的数据项?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delForm(formIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    /** 打开注册自定义表单弹框 */
    handleRegisterComponent() {
      this.registerForm = { formName: '', formComponent: '', remark: '' }
      this.registerOpen = true
    },
    /** 提交注册自定义表单 */
    submitRegister() {
      this.$refs['registerForm'].validate(valid => {
        if (!valid) return
        const payload = {
          formName: this.registerForm.formName,
          formType: 'component',
          formComponent: this.registerForm.formComponent,
          formContent: '',
          remark: this.registerForm.remark
        }
        addForm(payload).then(() => {
          this.$modal.msgSuccess('注册成功')
          this.registerOpen = false
          this.getList()
        }).catch(() => {
          this.$modal.msgError('注册失败，请重试')
        })
      })
    }
  }
}
</script>

<style scoped>
.test-form {
  margin: 15px auto;
  width: 800px;
  padding: 15px;
}
/deep/ .dialogClass .el-dialog__header {
  padding: 0;
}
/deep/ .dialogClass .el-dialog__body {
  padding: 0;
}
</style>
