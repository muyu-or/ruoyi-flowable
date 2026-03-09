<template>
  <div class="report-selector">
    <!-- 只读模式 -->
    <template v-if="readonly">
      <span v-if="selectedReports.length === 0" style="color:#c0c4cc">未关联测试报告</span>
      <template v-else>
        <span
          v-for="r in selectedReports"
          :key="r.id"
          style="display:inline-flex;align-items:center;margin-right:8px;margin-bottom:4px"
        >
          <el-tag
            type="info"
            size="small"
            style="cursor:pointer;"
            @click.native="handlePreview(r)"
          >{{ r.reportName }}（{{ r.reportCode }}）</el-tag>
          <el-button
            type="text"
            icon="el-icon-download"
            style="font-size:16px;padding:0 6px;"
            @click="handleDownload(r)"
          />
        </span>
      </template>
    </template>

    <!-- 编辑模式 -->
    <template v-else>
      <span
        v-for="r in selectedReports"
        :key="r.id"
        style="display:inline-flex;align-items:center;margin-right:6px;margin-bottom:4px"
      >
        <el-tag
          type="info"
          size="small"
          closable
          style="cursor:pointer;"
          @click.native="handlePreview(r)"
          @close="removeReport(r.id)"
        >{{ r.reportName }}（{{ r.reportCode }}）</el-tag>
        <el-button
          type="text"
          icon="el-icon-download"
          style="font-size:16px;padding:0 6px;"
          @click="handleDownload(r)"
        />
      </span>
      <el-button size="mini" icon="el-icon-paperclip" @click="openDialog">选择测试报告</el-button>
    </template>

    <!-- 选择弹窗 -->
    <el-dialog
      title="选择测试报告"
      :visible.sync="dialogVisible"
      width="700px"
      append-to-body
      @open="loadReports"
    >
      <!-- 搜索栏 -->
      <el-form :inline="true" size="small" style="margin-bottom:12px">
        <el-form-item label="报告名称">
          <el-input v-model="searchParams.reportName" placeholder="请输入报告名称" clearable />
        </el-form-item>
        <el-form-item label="报告编码">
          <el-input v-model="searchParams.reportCode" placeholder="请输入报告编码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadReports">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 报告列表 -->
      <el-table
        ref="reportTable"
        v-loading="tableLoading"
        :data="reportList"
        max-height="360"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="reportCode" label="报告编码" width="140" />
        <el-table-column prop="reportName" label="报告名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="testType" label="测试类型" width="100" />
        <el-table-column prop="createBy" label="上传人" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="160" />
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" size="mini" @click="handlePreview(scope.row)">预览</el-button>
            <el-button type="text" icon="el-icon-download" size="mini" @click="handleDownload(scope.row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>

      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelection">确定</el-button>
      </span>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      title="报告预览"
      :visible.sync="previewOpen"
      width="900px"
      append-to-body
      destroy-on-close
      @closed="onPreviewClosed"
    >
      <div v-if="previewType === 'loading'" v-loading="true" style="height:200px" />
      <div v-else-if="previewType === 'docx'" ref="docxContainer" style="overflow:auto;max-height:70vh" />
      <div v-else-if="previewType === 'xlsx'" style="overflow:auto;max-height:70vh">
        <div class="excel-preview" v-html="previewHtml" />
      </div>
      <iframe
        v-else-if="previewType === 'pdf'"
        :src="previewUrl"
        width="100%"
        height="600px"
        frameborder="0"
      />
      <div v-else-if="previewType === 'image'" style="text-align:center">
        <img :src="previewUrl" style="max-width:100%;max-height:70vh">
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listReport } from '@/api/manage/report'
import { previewReport } from '@/api/manage/report'
import request from '@/utils/request'
import { renderAsync } from 'docx-preview'

export default {
  name: 'ReportSelector',
  props: {
    value: {
      type: Array,
      default: function() { return [] }
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      tableLoading: false,
      reportList: [],
      tempSelected: [],
      selectedReports: [],
      searchParams: { reportName: '', reportCode: '' },
      previewOpen: false,
      previewType: 'other',
      previewUrl: '',
      previewHtml: ''
    }
  },
  watch: {
    value: {
      handler(newIds) {
        if (!newIds || newIds.length === 0) {
          this.selectedReports = []
          return
        }
        const currentIds = this.selectedReports.map(r => r.id)
        const isSame =
          newIds.length === currentIds.length &&
          newIds.every(id => currentIds.includes(id))
        if (!isSame) {
          this.loadSelectedReportDetails(newIds)
        }
      },
      immediate: true
    }
  },
  methods: {
    openDialog() {
      this.dialogVisible = true
    },

    loadReports() {
      this.tableLoading = true
      listReport({
        reportName: this.searchParams.reportName,
        reportCode: this.searchParams.reportCode,
        pageNum: 1,
        pageSize: 100
      }).then(res => {
        this.reportList = res.rows || []
        this.$nextTick(() => {
          if (!this.$refs.reportTable) return
          // 优先用 tempSelected 中的 ID（用户本次弹窗中已勾但未确认的）
          // 若 tempSelected 为空，则用 value（父组件已确认的 ID）作为初始回填
          const activeIds = this.tempSelected.length > 0
            ? this.tempSelected.map(r => r.id)
            : (this.value || [])
          this.reportList.forEach(row => {
            if (activeIds.includes(row.id)) {
              this.$refs.reportTable.toggleRowSelection(row, true)
            }
          })
        })
      }).catch(() => {
        this.$message.error('加载报告列表失败')
      }).finally(() => {
        this.tableLoading = false
      })
    },

    resetSearch() {
      this.searchParams = { reportName: '', reportCode: '' }
      this.loadReports()
    },

    handleSelectionChange(selection) {
      this.tempSelected = selection
    },

    confirmSelection() {
      this.selectedReports = this.tempSelected.slice()
      const ids = this.selectedReports.map(r => r.id)
      this.$emit('input', ids)
      this.dialogVisible = false
    },

    removeReport(id) {
      this.selectedReports = this.selectedReports.filter(r => r.id !== id)
      const ids = this.selectedReports.map(r => r.id)
      this.$emit('input', ids)
    },

    loadSelectedReportDetails(ids) {
      if (!ids || ids.length === 0) {
        this.selectedReports = []
        return
      }
      listReport({ pageNum: 1, pageSize: 1000 }).then(res => {
        const all = res.rows || []
        this.selectedReports = all.filter(r => ids.includes(r.id))
      }).catch(() => {
        this.$message.warning('报告信息加载失败，请刷新重试')
      })
    },

    handleDownload(row) {
      if (!row.storagePath) {
        this.$message.error('该报告未上传文件')
        return
      }
      this.$download.resource(row.storagePath)
    },

    handlePreview(row) {
      if (!row.storagePath) {
        this.$message.error('该报告未上传文件，无法预览')
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
          this.$message.error('文件预览失败')
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
      if (this.previewUrl) {
        window.URL.revokeObjectURL(this.previewUrl)
        this.previewUrl = ''
      }
      this.previewType = 'other'
      this.previewHtml = ''
    }
  }
}
</script>

<style scoped>
.excel-preview ::v-deep table { border-collapse: collapse; width: 100%; }
.excel-preview ::v-deep td,
.excel-preview ::v-deep th {
  border: 1px solid #dfe6ec;
  padding: 8px;
  text-align: center;
}
</style>
