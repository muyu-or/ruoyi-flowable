<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="模板编码" prop="templateCode">
        <el-input
          v-model="queryParams.templateCode"
          placeholder="请输入模板唯一编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模板名称" prop="templateName">
        <el-input
          v-model="queryParams.templateName"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="测试类型" prop="testType">
        <el-select v-model="queryParams.testType" placeholder="请选择测试类型" clearable>
          <el-option
            v-for="dict in dict.type.test_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="reportStatus">
        <el-select v-model="queryParams.reportStatus" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.report_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item style="float: right">
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          @click="handleAdd"
          v-hasPermi="['manage:template:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:template:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['manage:template:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="模板编码" align="center" prop="templateCode" />
      <el-table-column label="模板名称" align="center" prop="templateName" show-overflow-tooltip />
      <el-table-column label="测试类型" align="center" prop="testType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.test_type" :value="scope.row.testType"/>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="reportStatus">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.reportStatus"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:template:edit']"
          >修改</el-button>
          <el-button
            type="text"
            icon="el-icon-download"
            @click="handleDownloadTemplate(scope.row)"
            v-if="scope.row.storagePath"
          >下载</el-button>
          <el-button
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
          >预览</el-button>
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

    <!-- 添加或修改测试报告模板配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板编码" prop="templateCode">
          <el-input v-model="form.templateCode" placeholder="请输入模板唯一编码" />
        </el-form-item>
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="测试类型" prop="testType">
          <el-select v-model="form.testType" placeholder="请选择测试类型">
            <el-option
              v-for="dict in dict.type.test_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="模板文件" prop="storagePath">
          <file-upload v-model="form.storagePath" :isShowTip="false"/>
        </el-form-item>
        <el-form-item label="参数定义" prop="paramConfig">
          <el-input 
            v-model="form.paramConfig" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入模板参数定义(JSON格式)" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="reportStatus">
          <el-radio-group v-model="form.reportStatus">
            <el-radio
              v-for="dict in dict.type.report_status"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog title="模板预览" :visible.sync="previewOpen" width="900px" append-to-body destroy-on-close>
      <div v-if="previewType === 'loading'" v-loading="true" style="height: 200px;"></div>
      
      <div v-else-if="previewType === 'docx'" ref="docxContainer" style="overflow: auto; max-height: 70vh;"></div>
      
      <div v-else-if="previewType === 'xlsx'" style="overflow: auto; max-height: 70vh;">
        <div v-html="previewHtml" class="excel-preview"></div>
      </div>
      
      <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" width="100%" height="600px" frameborder="0"></iframe>
      
      <div v-else-if="previewType === 'image'" style="text-align: center;">
        <img :src="previewUrl" style="max-width: 100%; max-height: 70vh;"/>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTemplate, getTemplate, delTemplate, addTemplate, updateTemplate } from "@/api/manage/template";
import request from '@/utils/request'
import { renderAsync } from 'docx-preview'
import * as XLSX from 'xlsx'

export default {
  name: "Template",
  dicts: ['test_type', 'report_status'],
  data() {
    return {
      // 遮罩层
      loading: true,
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
      // 测试报告模板配置表格数据
      templateList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 预览相关
      previewOpen: false,
      previewType: 'other',
      previewUrl: '',
      previewHtml: '',
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        templateCode: null,
        templateName: null,
        testType: null,
        reportStatus: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        templateCode: [
          { required: true, message: "模板唯一编码 不能为空", trigger: "blur" }
        ],
        templateName: [
          { required: true, message: "模板名称不能为空", trigger: "blur" }
        ],
        storagePath: [
          { required: true, message: "模板文件存储路径/URL不能为空", trigger: "blur" }
        ],
        reportStatus: [
          { required: true, message: "状态不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询测试报告模板配置列表 */
    getList() {
      this.loading = true;
      listTemplate(this.queryParams).then(response => {
        this.templateList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        templateCode: null,
        templateName: null,
        testType: null,
        storagePath: null,
        paramConfig: null,
        reportStatus: 1,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加测试报告模板配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getTemplate(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改测试报告模板配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTemplate(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTemplate(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除测试报告模板配置编号为"' + ids + '"的数据项？').then(function() {
        return delTemplate(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 状态修改 */
    handleStatusChange(row) {
      let text = row.reportStatus === 1 ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.templateName + '"模板吗？').then(function() {
        return updateTemplate({ id: row.id, reportStatus: row.reportStatus });
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.reportStatus = row.reportStatus === 0 ? 1 : 0;
      });
    },
    /** 下载模板文件 */
    handleDownloadTemplate(row) {
      // 假设 common/download/resource 接口可用，或者直接打开链接
      // 这里使用通用的下载方法
      this.$download.resource(row.storagePath);
    },
    /** 预览按钮操作 */
    handlePreview(row) {
      if (!row.storagePath) {
        this.$modal.msgError("该模板未上传文件，无法预览");
        return;
      }

      // 先打开弹窗并显示加载中
      this.previewOpen = true;
      this.previewType = 'loading';

      // 构建资源请求URL
      const resourceUrl = '/common/download/resource?resource=' + encodeURIComponent(row.storagePath);

      // 请求文件 Blob
      request({
        url: resourceUrl,
        method: 'get',
        responseType: 'blob'
      }).then(blob => {
        // 优先从文件路径后缀判断，其次从blob的MIME type判断
        const fileName = row.storagePath;
        let fileType = '';
        if (fileName.lastIndexOf('.') > -1) {
          fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        }
        // 若路径无后缀，则从blob的MIME type推断
        if (!fileType || fileType.length > 10) {
          const mimeMap = {
            'application/pdf': 'pdf',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
            'application/msword': 'doc',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
            'application/vnd.ms-excel': 'xls',
            'image/png': 'png',
            'image/jpeg': 'jpg',
            'image/gif': 'gif'
          };
          fileType = mimeMap[blob.type] || '';
        }
        // 根据类型处理
        if (fileType === 'docx') {
          this.previewType = 'docx';
          this.$nextTick(() => {
            renderAsync(blob, this.$refs.docxContainer).then(() => {
              console.log("docx rendered");
            });
          });
        } else if (fileType === 'xlsx' || fileType === 'xls') {
          this.previewType = 'xlsx';
          const reader = new FileReader();
          reader.onload = (e) => {
            const data = new Uint8Array(e.target.result);
            const workbook = XLSX.read(data, { type: 'array' });
            const firstSheetName = workbook.SheetNames[0];
            const worksheet = workbook.Sheets[firstSheetName];
            // 转换为HTML
            this.previewHtml = XLSX.utils.sheet_to_html(worksheet);
          };
          reader.readAsArrayBuffer(blob);
        } else if (fileType === 'pdf') {
          this.previewType = 'pdf';
          this.previewUrl = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
        } else if (['jpg', 'jpeg', 'png', 'gif'].includes(fileType)) {
          this.previewType = 'image';
          this.previewUrl = window.URL.createObjectURL(blob);
        } else {
          // 其他格式，直接下载
          this.previewType = 'other';
          this.$download.resource(row.storagePath);
          this.previewOpen = false; // 关闭弹窗
        }
      }).catch(err => {
        this.$modal.msgError("文件加载失败");
        this.previewOpen = false;
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('manage/template/export', {
        ...this.queryParams
      }, `template_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.excel-preview ::v-deep table {
  border-collapse: collapse;
  width: 100%;
}
.excel-preview ::v-deep td, .excel-preview ::v-deep th {
  border: 1px solid #dfe6ec;
  padding: 8px;
  text-align: center;
}
</style>
