<template>
  <div class="task-detail-container">
    <!-- 任务信息卡片 -->
    <el-card class="box-card" shadow="hover">
      <div slot="header" class="clearfix">
        <span class="task-title">{{ task.name }}</span>
        <span class="task-status">
          <el-tag type="info">任务ID: {{ task.id }}</el-tag>
        </span>
      </div>

      <!-- 前置变量展示（来自前面的节点） -->
      <el-divider content-position="left">
        <strong>流程信息（前置节点传入的变量）</strong>
      </el-divider>
      <el-descriptions :column="2" border v-if="Object.keys(previousVariables).length > 0">
        <el-descriptions-item v-for="(value, key) in previousVariables" :key="key" :label="formatLabel(key)">
          {{ formatValue(value) }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无前置变量" :image-size="80"></el-empty>

      <!-- 当前节点表单 -->
      <el-divider content-position="left">
        <strong>审批表单（当前节点需要填写）</strong>
      </el-divider>
      <el-form :model="formData" ref="taskForm" label-width="140px" class="task-form">
        <!-- 通用字段 -->
        <el-form-item label="审批意见" required>
          <el-input
            v-model="formData.approvalComment"
            type="textarea"
            placeholder="请输入审批意见"
            rows="4"
          />
        </el-form-item>

        <!-- 动态字段 - 根据流程定义动态生成 -->
        <el-form-item v-for="field in dynamicFields" :key="field.key" :label="field.label">
          <!-- 单选下拉框 -->
          <el-select
            v-if="field.type === 'select'"
            v-model="formData[field.key]"
            :placeholder="field.placeholder || '请选择'"
          >
            <el-option
              v-for="option in field.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>

          <!-- 输入框 -->
          <el-input
            v-else-if="field.type === 'input'"
            v-model="formData[field.key]"
            :placeholder="field.placeholder"
          />

          <!-- 多选 -->
          <el-checkbox-group
            v-else-if="field.type === 'checkbox'"
            v-model="formData[field.key]"
          >
            <el-checkbox
              v-for="option in field.options"
              :key="option.value"
              :label="option.value"
            >
              {{ option.label }}
            </el-checkbox>
          </el-checkbox-group>

          <!-- 日期选择 -->
          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="formData[field.key]"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <el-divider></el-divider>
      <el-row :gutter="20" class="action-buttons">
        <el-col :span="4">
          <el-button
            type="primary"
            size="medium"
            :loading="submitLoading"
            @click="submitTask"
          >
            完成任务
          </el-button>
        </el-col>
        <el-col :span="4">
          <el-button
            type="warning"
            size="medium"
            :loading="rejectLoading"
            @click="rejectTask"
          >
            驳回
          </el-button>
        </el-col>
        <el-col :span="4">
          <el-button
            type="info"
            size="medium"
            @click="goBack"
          >
            返回
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getProcessVariables, completeTask, rejectTask } from '@/api/flowable/task'

export default {
  name: 'TaskDetail',
  data() {
    return {
      task: {
        id: '',
        name: '',
        instanceId: ''
      },
      previousVariables: {},
      formData: {
        approvalComment: '',
        approval_status: 'approved'
      },
      dynamicFields: [
        {
          key: 'approval_status',
          label: '审批结果',
          type: 'select',
          options: [
            { label: '批准', value: 'approved' },
            { label: '拒绝', value: 'rejected' }
          ]
        }
      ],
      submitLoading: false,
      rejectLoading: false
    }
  },
  mounted() {
    this.loadTaskData()
  },
  methods: {
    /**
     * 加载任务数据和前置变量
     */
    loadTaskData() {
      const taskId = this.$route.params.taskId
      if (!taskId) {
        this.$message.error('缺少任务ID参数')
        return
      }

      this.task.id = taskId
      this.loadProcessVariables(taskId)
    },

    /**
     * 获取前置变量
     * 这些是来自前面节点的数据，用于在当前节点显示和传递
     */
    loadProcessVariables(taskId) {
      this.$message.loading('加载任务信息中...')
      getProcessVariables(taskId)
        .then(response => {
          if (response.code === 0) {
            // response.data 包含所有流程变量
            const variables = response.data || {}

            // 分离前置变量（来自前面节点，不在当前表单中的字段）
            this.previousVariables = this.extractPreviousVariables(variables)

            // 如果有任务名称，更新显示
            if (variables.taskName) {
              this.task.name = variables.taskName
            }

            this.$message.closeAll()
          } else {
            this.$message.error(response.msg || '加载任务信息失败')
          }
        })
        .catch(error => {
          this.$message.error('加载任务信息失败: ' + error.message)
        })
    },

    /**
     * 提取前置变量
     * 过滤掉当前表单字段，返回来自前面节点的数据
     */
    extractPreviousVariables(allVariables) {
      const formKeys = new Set(['approvalComment', 'approval_status'])
      const result = {}

      for (const [key, value] of Object.entries(allVariables)) {
        if (!formKeys.has(key) && key !== 'formJson') {
          result[key] = value
        }
      }

      return result
    },

    /**
     * 完成任务并传递变量给下一节点
     *
     * 流程变量传递逻辑：
     * 1. 前置变量（previousVariables）自动保留并传递
     * 2. 当前表单数据（formData）添加到变量中
     * 3. 网关根据 approval_status 判断流程走向
     */
    submitTask() {
      this.submitLoading = true

      const taskVo = {
        taskId: this.task.id,
        instanceId: this.task.instanceId,
        comment: this.formData.approvalComment,
        variables: {
          // 保留前置变量（会自动合并）
          ...this.previousVariables,
          // 添加当前节点的新变量
          ...this.formData,
          // 添加完成时间戳
          taskCompletedTime: new Date().getTime(),
          taskCompletedBy: this.$store.state.user.userId
        }
      }

      completeTask(taskVo)
        .then(response => {
          if (response.code === 0) {
            this.$message.success('任务完成成功')

            // 根据审批结果显示不同的提示
            if (this.formData.approval_status === 'approved') {
              this.$message.success('您的审批已批准')
            } else if (this.formData.approval_status === 'rejected') {
              this.$message.warning('您的审批已拒绝')
            }

            // 返回待办任务列表
            setTimeout(() => {
              this.$router.push('/flowable/task/todoList')
            }, 1000)
          } else {
            this.$message.error(response.msg || '任务完成失败')
          }
        })
        .catch(error => {
          this.$message.error('任务完成失败: ' + error.message)
        })
        .finally(() => {
          this.submitLoading = false
        })
    },

    /**
     * 驳回任务
     */
    rejectTask() {
      this.$confirm('确定要驳回该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.rejectLoading = true

        const taskVo = {
          taskId: this.task.id,
          instanceId: this.task.instanceId,
          comment: this.formData.approvalComment || '任务已驳回'
        }

        rejectTask(taskVo)
          .then(response => {
            if (response.code === 0) {
              this.$message.success('任务已驳回')
              setTimeout(() => {
                this.$router.push('/flowable/task/todoList')
              }, 1000)
            } else {
              this.$message.error(response.msg || '驳回失败')
            }
          })
          .catch(error => {
            this.$message.error('驳回失败: ' + error.message)
          })
          .finally(() => {
            this.rejectLoading = false
          })
      }).catch(() => {
        // 用户取消驳回
      })
    },

    /**
     * 返回上一页
     */
    goBack() {
      this.$router.go(-1)
    },

    /**
     * 格式化标签名称（驼峰转中文）
     */
    formatLabel(key) {
      const labelMap = {
        'applicantName': '申请人',
        'applicantId': '申请人ID',
        'startTime': '申请时间',
        'description': '描述',
        'content': '内容',
        'amount': '金额',
        'quantity': '数量'
      }
      return labelMap[key] || key
    },

    /**
     * 格式化值的显示
     */
    formatValue(value) {
      if (value === null || value === undefined) {
        return '-'
      }
      if (typeof value === 'object') {
        return JSON.stringify(value)
      }
      return value
    }
  }
}
</script>

<style scoped lang="scss">
.task-detail-container {
  padding: 20px;
  background-color: #f0f2f5;

  .box-card {
    background-color: #fff;
    margin-bottom: 20px;

    .task-title {
      font-size: 18px;
      font-weight: bold;
      color: #333;
    }

    .task-status {
      float: right;
    }

    .task-form {
      padding: 20px 0;

      ::v-deep .el-form-item {
        margin-bottom: 18px;
      }
    }

    .action-buttons {
      display: flex;
      justify-content: flex-start;
      padding: 20px 0;

      ::v-deep .el-button {
        width: 120px;
      }
    }
  }
}
</style>
