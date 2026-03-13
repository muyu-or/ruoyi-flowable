<template>
  <el-dialog
    :title="'启动流程 - ' + (processDefinition && processDefinition.name || '')"
    :visible.sync="visible"
    width="650px"
    append-to-body
    @close="handleClose"
  >
    <div v-loading="loading" style="padding: 20px 0">
      <!-- 流程基本信息 -->
      <el-form :model="formData" label-width="120px" size="small">
        <el-form-item label="流程定义">
          <span>{{ processDefinition && processDefinition.name }}</span>
        </el-form-item>
        <el-form-item label="业务单号">
          <el-input v-model="formData.businessKey" readonly>
            <template slot="append">
              <el-tooltip content="自动生成，无需修改" placement="top">
                <i class="el-icon-info" />
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="主班组">
          <el-select
            v-model="formData.mainTeamId"
            placeholder="选择主班组，自动填入各节点"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="team in teamList"
              :key="team.id"
              :label="team.teamName"
              :value="team.id"
            />
          </el-select>
          <div style="font-size: 12px; color: #909399; margin-top: 4px">
            选择后自动填入所有节点，各节点可单独调整
          </div>
        </el-form-item>
      </el-form>

      <!-- 流程节点班组配置 -->
      <div v-if="processNodes && processNodes.length > 0" style="margin-top: 20px">
        <h4 style="margin-bottom: 15px; border-bottom: 1px solid #ddd; padding-bottom: 10px">
          节点班组分配（{{ processNodes.length }}个任务节点）
        </h4>
        <div style="margin-top: 15px">
          <div v-for="(node, index) in processNodes" :key="node.id" style="margin-bottom: 15px">
            <el-row :gutter="20">
              <el-col :span="10">
                <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
                  <div style="font-weight: 500; margin-bottom: 5px">{{ node.name }}</div>
                  <div style="font-size: 12px; color: #909399">ID: {{ node.id }}</div>
                </div>
              </el-col>
              <el-col :span="14">
                <el-select
                  v-model="nodeTeamMapArray[index]"
                  placeholder="选择处理班组"
                  clearable
                  filterable
                >
                  <el-option
                    v-for="team in teamList"
                    :key="team.id"
                    :label="team.teamName"
                    :value="team.id"
                  />
                </el-select>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>

      <!-- 节点信息提示 -->
      <el-alert
        v-if="!processNodes || processNodes.length === 0"
        title="暂无需配置的用户任务节点"
        type="info"
        :closable="false"
        style="margin-top: 20px"
      />
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">启动流程</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { listTeam } from '@/api/manage/team'
import { startProcessWithTeam, flowXmlAndNode } from '@/api/flowable/definition'

export default {
  name: 'ProcessStartDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    processDefinition: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      loading: false,
      submitLoading: false,
      teamList: [],
      processNodes: [],
      nodeTeamMapArray: [], // 改用数组存储，确保响应式
      formData: {
        procDefId: null,
        businessKey: null,
        mainTeamId: null,
        variables: {}
      }
    }
  },
  watch: {
    visible(newVal) {
      if (newVal && this.processDefinition) {
        this.initDialog()
      }
    },
    processDefinition: {
      handler(newVal) {
        if (newVal && this.visible) {
          this.initDialog()
        }
      },
      deep: true
    },
    // 主班组变化时，自动将所有节点填入该班组（已单独修改的节点也会被覆盖，用户可再次调整）
    'formData.mainTeamId'(newVal) {
      if (newVal) {
        this.nodeTeamMapArray = this.processNodes.map(() => newVal)
      }
    }
  },
  methods: {
    /** 初始化对话框 */
    initDialog() {
      this.loading = true
      this.formData.procDefId = this.processDefinition.id || this.processDefinition.processDefinitionId
      this.formData.nodeTeamMap = {}
      // 自动生成业务单号：AUTO-YYYYMMDD-6位随机数
      this.formData.businessKey = this.generateBusinessKey()

      // 并行加载班组列表和流程节点
      Promise.all([
        this.loadTeamList(),
        this.loadProcessNodes()
      ]).finally(() => {
        this.loading = false
      })
    },

    /** 获取班组列表 */
    loadTeamList() {
      return listTeam({ pageNum: 1, pageSize: 100, teamStatus: '1' }).then(res => {
        this.teamList = res.rows || res.data || []
      }).catch(err => {
        this.$message.error('加载班组列表失败')
        console.error(err)
      })
    },

    /** 获取流程的所有用户任务节点 */
    loadProcessNodes() {
      return flowXmlAndNode({ deployId: this.processDefinition.deploymentId }).then(res => {
        // API 返回格式：{ data: { nodeData: [...], xmlData: "..." } }
        const data = res.data || res
        const nodes = []

        // 方式1: 从 xmlData 中解析 XML 获取 UserTask 节点
        if (data.xmlData && typeof data.xmlData === 'string') {
          try {
            // 从 XML 字符串中提取 userTask 元素
            const userTaskPattern = /<userTask\s+id="([^"]+)"\s+name="([^"]*)"/g
            let match
            while ((match = userTaskPattern.exec(data.xmlData)) !== null) {
              nodes.push({
                id: match[1], // 节点 ID
                name: match[2] // 节点名称
              })
            }
          } catch (e) {
            console.error('解析 XML 失败', e)
          }
        }

        // 初始化 processNodes 和 nodeTeamMapArray（改用数组）
        this.processNodes = nodes
        this.nodeTeamMapArray = new Array(nodes.length).fill(null) // 初始化数组，每个位置是 null

        if (nodes.length === 0) {
          console.warn('未找到任何 UserTask 节点', data)
        }
      }).catch(err => {
        this.$message.error('加载流程节点失败')
        console.error(err)
      })
    },

    /** 提交启动流程 */
    handleSubmit() {
      // 验证：至少为一个节点分配班组（可以没有主班组）
      const hasNodeConfig = this.nodeTeamMapArray.some(v => v)

      if (!hasNodeConfig) {
        this.$message.warning('请至少为一个节点分配班组')
        return
      }

      // 构建 nodeTeamMap（从数组转换为对象）
      const nodeTeamMap = {}
      this.processNodes.forEach((node, index) => {
        if (this.nodeTeamMapArray[index]) {
          nodeTeamMap[node.id] = this.nodeTeamMapArray[index]
        }
      })

      this.submitLoading = true
      const requestData = {
        procDefId: this.formData.procDefId,
        businessKey: this.formData.businessKey || null,
        mainTeamId: this.formData.mainTeamId || null,
        nodeTeamMap: nodeTeamMap,
        variables: this.formData.variables
      }

      startProcessWithTeam(requestData)
        .then(res => {
          this.$message.success(res.msg || '流程启动成功')
          this.$emit('success')
          this.handleClose()
        })
        .catch(err => {
          this.$message.error(err.message || '流程启动失败')
        })
        .finally(() => {
          this.submitLoading = false
        })
    },

    /** 关闭对话框 */
    handleClose() {
      this.$emit('update:visible', false)
      this.resetForm()
    },

    /** 重置表单 */
    resetForm() {
      this.formData = {
        procDefId: null,
        businessKey: null,
        mainTeamId: null,
        nodeTeamMap: {},
        variables: {}
      }
      this.processNodes = []
      this.nodeTeamMapArray = []
    },

    /** 自动生成业务单号：AUTO-YYYYMMDD-6位随机数字 */
    generateBusinessKey() {
      const now = new Date()
      const date = now.getFullYear().toString() +
        String(now.getMonth() + 1).padStart(2, '0') +
        String(now.getDate()).padStart(2, '0')
      const random = String(Math.floor(Math.random() * 1000000)).padStart(6, '0')
      return 'AUTO-' + date + '-' + random
    }
  }
}
</script>

<style scoped>
h4 {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}
</style>
