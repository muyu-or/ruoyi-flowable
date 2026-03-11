<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true" label-width="80px">
      <el-form-item label="报告名称" prop="reportName">
        <el-input v-model="queryParams.reportName" placeholder="请输入报告名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="物料名称" prop="materialName">
        <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item style="float: right">
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['manage:report:add']" type="primary" plain icon="el-icon-plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['manage:report:remove']" type="danger" plain icon="el-icon-delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="reportList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="报告编码" align="center" prop="reportCode" width="180" />
      <el-table-column label="报告名称" align="center" prop="reportName" show-overflow-tooltip />
      <el-table-column label="物料名称" align="center" prop="materialName" width="140" show-overflow-tooltip />
      <el-table-column label="物料数量" align="center" prop="materialQuantity" width="100" />
      <el-table-column label="节点名称" align="center" prop="nodeName" width="120" />
      <el-table-column label="上传人" align="center" prop="uploader" width="100" />
      <el-table-column label="审批人" align="center" prop="createBy" width="100" />
      <el-table-column label="上传时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button v-hasPermi="['manage:report:query']" type="text" icon="el-icon-view" @click="handlePreview(scope.row)">预览</el-button>
          <el-button type="text" icon="el-icon-download" @click="handleDownload(scope.row)">下载</el-button>
          <el-button v-hasPermi="['manage:report:remove']" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="报告名称" prop="reportName">
          <el-input v-model="form.reportName" placeholder="请输入报告名称" />
        </el-form-item>
        <el-form-item label="报告文件" prop="storagePath">
          <file-upload v-model="form.storagePath" :is-show-tip="false" :limit="1" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog title="报告预览" :visible.sync="previewOpen" width="900px" append-to-body destroy-on-close>
      <div v-if="previewType === 'loading'" v-loading="true" style="height: 200px;" />
      <div v-else-if="previewType === 'docx'" ref="docxContainer" style="overflow: auto; max-height: 70vh;" />
      <div v-else-if="previewType === 'xlsx'" style="overflow: auto; max-height: 70vh;">
        <div class="excel-preview" v-html="previewHtml" />
      </div>
      <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" width="100%" height="600px" frameborder="0" />
      <div v-else-if="previewType === 'image'" style="text-align: center;">
        <img :src="previewUrl" style="max-width: 100%; max-height: 70vh;">
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listReport, addReport, updateReport, delReport, previewReport } from '@/api/manage/report'
import request from '@/utils/request'
import { renderAsync } from 'docx-preview'

export default {
  name: 'Report',
  dicts: [],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      reportList: [],
      title: '',
      open: false,
      previewOpen: false,
      previewType: 'other',
      previewUrl: '',
      previewHtml: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        reportName: null,
        materialName: null
      },
      form: {},
      rules: {
        reportName: [{ required: true, message: '报告名称不能为空', trigger: 'blur' }],
        storagePath: [{ required: true, message: '请上传报告文件', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listReport(this.queryParams).then(res => {
        this.reportList = res.rows
        this.total = res.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = { id: null, reportName: null, testType: null, storagePath: null, remark: null }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '上传测试报告'
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          const fn = this.form.id ? updateReport : addReport
          fn(this.form).then(() => {
            this.$modal.msgSuccess(this.form.id ? '修改成功' : '上传成功')
            this.open = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('确认删除选中的报告吗？').then(() => {
        return delReport(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleDownload(row) {
      this.$download.resource(row.storagePath)
    },
    handlePreview(row) {
      if (!row.storagePath) {
        this.$modal.msgError('该报告未上传文件，无法预览')
        return
      }
      this.previewOpen = true
      this.previewType = 'loading'

      const fileName = row.storagePath
      let fileType = ''
      if (fileName.lastIndexOf('.') > -1) {
        fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
      }

      if (fileType === 'xlsx' || fileType === 'xls') {
        previewReport(row.storagePath).then(res => {
          this.previewHtml = res.data || ''
          this.previewType = 'xlsx'
        }).catch(() => {
          this.$modal.msgError('文件预览失败')
          this.previewOpen = false
        })
        return
      }

      const resourceUrl = '/common/download/resource?resource=' + encodeURIComponent(row.storagePath)
      request({ url: resourceUrl, method: 'get', responseType: 'blob' }).then(blob => {
        if (!fileType || fileType.length > 10) {
          const mimeMap = {
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
            'application/pdf': 'pdf',
            'image/png': 'png',
            'image/jpeg': 'jpg'
          }
          fileType = mimeMap[blob.type] || ''
        }
        if (fileType === 'docx') {
          this.previewType = 'docx'
          this.$nextTick(() => renderAsync(blob, this.$refs.docxContainer))
        } else if (fileType === 'pdf') {
          this.previewType = 'pdf'
          this.previewUrl = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
        } else if (['jpg', 'jpeg', 'png', 'gif'].includes(fileType)) {
          this.previewType = 'image'
          this.previewUrl = window.URL.createObjectURL(blob)
        } else {
          this.$download.resource(row.storagePath)
          this.previewOpen = false
        }
      }).catch(err => {
        console.error('预览失败', err)
        this.$modal.msgError('文件加载失败')
        this.previewOpen = false
      })
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.excel-preview ::v-deep table { border-collapse: collapse; width: 100%; }
.excel-preview ::v-deep td, .excel-preview ::v-deep th {
  border: 1px solid #dfe6ec; padding: 8px; text-align: center;
}
</style>
