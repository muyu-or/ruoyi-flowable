<template>
  <div class="report-uploader">
    <!-- 只读模式 -->
    <template v-if="readonly">
      <span v-if="!reportList || reportList.length === 0" style="color:#c0c4cc">未上传测试报告</span>
      <template v-else>
        <span
          v-for="r in reportList"
          :key="r._uid || r.id"
          style="display:inline-flex;align-items:center;margin-right:8px;margin-bottom:4px"
        >
          <el-tag type="info" size="small" style="cursor:pointer;" @click.native="handlePreview(r)">
            {{ r.reportName }}{{ r.reportCode ? '（' + r.reportCode + '）' : '' }}
          </el-tag>
          <el-button type="text" icon="el-icon-download" style="font-size:16px;padding:0 6px;" @click="handleDownload(r)" />
        </span>
      </template>
    </template>

    <!-- 编辑模式 -->
    <template v-else>
      <span
        v-for="r in reportList"
        :key="r._uid || r.id"
        style="display:inline-flex;align-items:center;margin-right:6px;margin-bottom:4px"
      >
        <el-tag
          type="info"
          size="small"
          closable
          style="cursor:pointer;"
          @click.native="handlePreview(r)"
          @close="removeReport(r)"
        >
          {{ r.reportName }}{{ r.reportCode ? '（' + r.reportCode + '）' : '' }}
        </el-tag>
        <el-button type="text" icon="el-icon-download" style="font-size:16px;padding:0 6px;" @click="handleDownload(r)" />
      </span>
      <el-upload
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :before-upload="handleBeforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        style="display:inline-block"
      >
        <el-button size="mini" icon="el-icon-upload2" :loading="uploading">上传测试报告</el-button>
      </el-upload>
      <el-dropdown v-if="templateList.length > 1" trigger="click" style="margin-left:8px" @command="handleDownloadTemplate">
        <el-button size="mini" type="success" plain icon="el-icon-download">下载模板<i class="el-icon-arrow-down el-icon--right" /></el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-for="tpl in templateList" :key="tpl.id" :command="tpl">{{ tpl.templateName }}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button v-else-if="templateList.length === 1" size="mini" type="success" plain icon="el-icon-download" style="margin-left:8px" @click="handleDownloadTemplate(templateList[0])">下载模板</el-button>
    </template>

    <!-- 预览对话框 -->
    <el-dialog title="报告预览" :visible.sync="previewOpen" width="900px" append-to-body destroy-on-close @closed="onPreviewClosed">
      <div v-if="previewType === 'loading'" v-loading="true" style="height:200px" />
      <div v-else-if="previewType === 'docx'" ref="docxContainer" style="overflow:auto;max-height:70vh" />
      <div v-else-if="previewType === 'xlsx'" style="overflow:auto;max-height:70vh">
        <div class="excel-preview" v-html="previewHtml" />
      </div>
      <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" width="100%" height="600px" frameborder="0" />
      <div v-else-if="previewType === 'image'" style="text-align:center">
        <img :src="previewUrl" style="max-width:100%;max-height:70vh">
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { previewReport } from '@/api/manage/report'
import { listTemplate } from '@/api/manage/template'
import request from '@/utils/request'
import { renderAsync } from 'docx-preview'

let uidCounter = 0

export default {
  name: 'ReportUploader',
  props: {
    value: { type: Array, default: function() { return [] } },
    readonly: { type: Boolean, default: false },
    materialName: { type: String, default: '' },
    materialQuantity: { type: Number, default: null },
    nodeName: { type: String, default: '' },
    testType: { type: String, default: '' }
  },
  data() {
    return {
      uploading: false,
      reportList: [],
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: { Authorization: 'Bearer ' + getToken() },
      previewOpen: false,
      previewType: 'other',
      previewUrl: '',
      previewHtml: '',
      templateList: []
    }
  },
  watch: {
    value: {
      handler(val) {
        if (!val || val.length === 0) {
          this.reportList = []
        } else {
          this.reportList = val.slice()
        }
      },
      immediate: true
    }
  },
  created: function() {
    this.loadTemplates()
  },
  methods: {
    loadTemplates: function() {
      if (!this.testType) return
      listTemplate({ testType: this.testType, reportStatus: 1, pageSize: 10 }).then(function(res) {
        this.templateList = (res.rows || []).filter(function(t) { return t.storagePath })
      }.bind(this)).catch(function() {
        this.templateList = []
      }.bind(this))
    },

    handleDownloadTemplate: function(tpl) {
      if (!tpl || !tpl.storagePath) {
        this.$message.warning('该模板暂无文件可下载')
        return
      }
      this.$download.resource(tpl.storagePath)
    },

    handleBeforeUpload(file) {
      const isLt50M = file.size / 1024 / 1024 < 50
      if (!isLt50M) {
        this.$message.error('上传文件不能超过 50MB')
        return false
      }
      this.uploading = true
      // 将文件名暂存以便 onSuccess 时使用
      this._pendingFileName = file.name
      return true
    },

    handleUploadSuccess(res) {
      if (res.code !== 200) {
        this.$message.error(res.msg || '文件上传失败')
        this.uploading = false
        return
      }
      // 文件已上传到服务器，只把元数据存入本地列表
      // 审批通过后由后端统一写入 report_record 表
      const storagePath = res.fileName
      const reportName = this._pendingFileName || storagePath
      const newReport = {
        _uid: '__rpt_' + (++uidCounter),
        storagePath: storagePath,
        reportName: reportName,
        materialName: this.materialName || '',
        materialQuantity: this.materialQuantity,
        nodeName: this.nodeName || '',
        uploader: this.$store.getters.name || ''
      }
      const newList = this.reportList.slice()
      newList.push(newReport)
      this.reportList = newList
      this.$emit('input', newList)
      this.$message.success('测试报告上传成功，将在审批通过后正式入库')
      this.uploading = false
      this._pendingFileName = ''
    },

    handleUploadError() {
      this.$message.error('文件上传失败')
      this.uploading = false
    },

    removeReport(row) {
      const uid = row._uid || row.id
      const newList = this.reportList.filter(r => (r._uid || r.id) !== uid)
      this.reportList = newList
      this.$emit('input', newList)
    },

    handleDownload(row) {
      if (!row.storagePath) { this.$message.error('该报告未上传文件'); return }
      this.$download.resource(row.storagePath)
    },

    handlePreview(row) {
      if (!row.storagePath) { this.$message.error('该报告未上传文件，无法预览'); return }
      this.previewOpen = true
      this.previewType = 'loading'
      const fileName = row.storagePath
      let fileType = fileName.lastIndexOf('.') > -1 ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : ''

      if (fileType === 'xlsx' || fileType === 'xls') {
        previewReport(row.storagePath).then(res => {
          this.previewHtml = res.data || ''
          this.previewType = 'xlsx'
        }).catch(() => { this.$message.error('文件预览失败'); this.previewOpen = false })
        return
      }

      const resourceUrl = '/common/download/resource?resource=' + encodeURIComponent(row.storagePath)
      request({ url: resourceUrl, method: 'get', responseType: 'blob' }).then(blob => {
        if (!fileType || fileType.length > 10) {
          const mimeMap = {
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
            'application/pdf': 'pdf', 'image/png': 'png', 'image/jpeg': 'jpg'
          }
          fileType = mimeMap[blob.type] || ''
        }
        if (fileType === 'docx') {
          this.previewType = 'docx'
          this.$nextTick(() => renderAsync(blob, this.$refs.docxContainer))
        } else if (fileType === 'pdf') {
          this.previewType = 'pdf'
          if (this.previewUrl) { window.URL.revokeObjectURL(this.previewUrl) }
          this.previewUrl = window.URL.createObjectURL(blob)
        } else if (['jpg', 'jpeg', 'png', 'gif'].includes(fileType)) {
          this.previewType = 'image'
          if (this.previewUrl) { window.URL.revokeObjectURL(this.previewUrl) }
          this.previewUrl = window.URL.createObjectURL(blob)
        } else {
          this.$download.resource(row.storagePath)
          this.previewOpen = false
        }
      }).catch(err => {
        console.error('预览失败', err)
        this.$message.error('文件加载失败')
        this.previewOpen = false
      })
    },

    onPreviewClosed() {
      if (this.previewUrl) { window.URL.revokeObjectURL(this.previewUrl); this.previewUrl = '' }
      this.previewType = 'other'
      this.previewHtml = ''
    }
  }
}
</script>

<style scoped>
.excel-preview ::v-deep table { border-collapse: collapse; width: 100%; }
.excel-preview ::v-deep td, .excel-preview ::v-deep th { border: 1px solid #dfe6ec; padding: 8px; text-align: center; }
</style>
