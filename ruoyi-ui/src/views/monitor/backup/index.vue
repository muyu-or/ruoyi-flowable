<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入设备名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="录像日期" prop="recordDateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item label="备份状态" prop="backupStatus">
        <el-select v-model="queryParams.backupStatus" placeholder="请选择备份状态" clearable>
          <el-option
            v-for="dict in dict.type.monitor_backup_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['monitor:backup:add']"
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['monitor:backup:edit']"
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['monitor:backup:remove']"
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['monitor:backup:export']"
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="backupList" border v-table-col-width="'main'" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="设备名称" align="center" prop="deviceName" :show-overflow-tooltip="true" />
      <el-table-column label="录像日期" align="center" prop="recordDate" width="110" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="160" />
      <el-table-column label="结束时间" align="center" prop="endTime" width="160" />
      <el-table-column label="文件名" align="center" prop="fileName" :show-overflow-tooltip="true" />
      <el-table-column label="文件大小" align="center" prop="fileSize" width="120">
        <template slot-scope="scope">
          <span v-if="scope.row.fileSize != null">{{ formatFileSize(scope.row.fileSize) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备份状态" align="center" prop="backupStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.monitor_backup_status" :value="scope.row.backupStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['monitor:backup:edit']"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-hasPermi="['monitor:backup:remove']"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改录像备份对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="关联设备" prop="deviceId">
              <el-select
                v-model="form.deviceId"
                placeholder="请选择设备"
                filterable
                style="width: 100%"
                @change="handleDeviceChange"
              >
                <el-option
                  v-for="item in deviceOptions"
                  :key="item.id"
                  :label="item.deviceName + ' (' + item.deviceNo + ')'"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="录像日期" prop="recordDate">
              <el-date-picker
                v-model="form.recordDate"
                type="date"
                value-format="yyyy-MM-dd"
                placeholder="请选择录像日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文件名" prop="fileName">
              <el-input v-model="form.fileName" placeholder="请输入文件名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="文件大小(B)" prop="fileSize">
              <el-input-number v-model="form.fileSize" :min="0" controls-position="right" placeholder="字节" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="文件路径" prop="filePath">
              <el-input v-model="form.filePath" placeholder="请输入文件存储路径" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备份状态" prop="backupStatus">
              <el-select v-model="form.backupStatus" placeholder="请选择备份状态" style="width: 100%">
                <el-option
                  v-for="dict in dict.type.monitor_backup_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listBackup, getBackup, delBackup, addBackup, updateBackup } from '@/api/monitor/backup'
import { listDevice } from '@/api/monitor/device'

export default {
  name: 'MonitorBackup',
  dicts: ['monitor_backup_status'],
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
      // 录像备份表格数据
      backupList: [],
      // 设备下拉选项
      deviceOptions: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceName: undefined,
        backupStatus: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        deviceId: [
          { required: true, message: '请选择关联设备', trigger: 'change' }
        ],
        recordDate: [
          { required: true, message: '请选择录像日期', trigger: 'change' }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getDeviceOptions()
  },
  methods: {
    /** 查询录像备份列表 */
    getList() {
      this.loading = true
      const queryParams = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        queryParams.params = {
          beginRecordDate: this.dateRange[0],
          endRecordDate: this.dateRange[1]
        }
      }
      listBackup(queryParams).then(response => {
        this.backupList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 加载设备下拉选项 */
    getDeviceOptions() {
      listDevice({ pageNum: 1, pageSize: 9999 }).then(response => {
        this.deviceOptions = response.rows
      })
    },
    /** 设备选择变化时自动填充设备名称 */
    handleDeviceChange(val) {
      const device = this.deviceOptions.find(d => d.id === val)
      if (device) {
        this.$set(this.form, 'deviceName', device.deviceName)
      }
    },
    /** 格式化文件大小 */
    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        deviceId: undefined,
        deviceName: undefined,
        recordDate: undefined,
        startTime: undefined,
        endTime: undefined,
        fileName: undefined,
        filePath: undefined,
        fileSize: undefined,
        backupStatus: '0',
        remark: undefined
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
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加录像备份'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getBackup(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改录像备份'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateBackup(this.form).then(response => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addBackup(this.form).then(response => {
              this.$modal.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除录像备份编号为"' + ids + '"的数据项？').then(function() {
        return delBackup(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('monitor/backup/export', {
        ...this.queryParams
      }, `backup_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
