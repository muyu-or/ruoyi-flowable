<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="el-icon-document">发起任务</span>
        <el-button style="float: right;" size="mini" type="danger" @click="goBack">关闭</el-button>
      </div>

      <!-- 步骤条 -->
      <el-steps :active="activeStep" finish-status="success" align-center style="margin-bottom: 30px">
        <el-step title="填写表单" icon="el-icon-edit" />
        <el-step title="分配班组" icon="el-icon-s-custom" />
        <el-step title="确认提交" icon="el-icon-circle-check" />
      </el-steps>

      <!-- Step 1：填写业务表单 -->
      <div v-show="activeStep === 0">
        <el-col :span="16" :offset="4">
          <v-form-render ref="vFormRef" :form-data="formRenderData" />
        </el-col>
      </div>

      <!-- Step 2：分配班组 -->
      <div v-show="activeStep === 1">
        <el-col :span="16" :offset="4">
          <!-- 业务单号（只读） -->
          <el-form :model="startForm" label-width="120px" size="small">
            <el-form-item label="业务单号">
              <el-input :value="startForm.businessKey" readonly>
                <template slot="append">
                  <el-tooltip content="自动生成，无需修改" placement="top">
                    <i class="el-icon-info" />
                  </el-tooltip>
                </template>
              </el-input>
            </el-form-item>

            <!-- 主班组：选中后自动批量填入各节点 -->
            <el-form-item label="主班组">
              <el-select
                v-model="startForm.mainTeamId"
                placeholder="选择主班组，自动填入所有节点"
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

          <!-- 节点班组逐一配置 -->
          <div v-if="processNodes.length > 0" style="margin-top: 10px">
            <h4 style="margin-bottom: 15px; border-bottom: 1px solid #ddd; padding-bottom: 10px; font-size: 14px; font-weight: 600; color: #303133">
              节点班组分配（{{ processNodes.length }} 个任务节点）
            </h4>
            <el-form label-width="120px" size="small">
              <el-form-item
                v-for="(node, index) in processNodes"
                :key="node.id"
                :label="node.name"
              >
                <el-select
                  :value="nodeTeamMapArray[index]"
                  placeholder="选择处理班组"
                  clearable
                  filterable
                  style="width: 100%"
                  @change="val => $set(nodeTeamMapArray, index, val)"
                >
                  <el-option
                    v-for="team in teamList"
                    :key="team.id"
                    :label="team.teamName"
                    :value="team.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <el-alert
            v-else
            title="暂无需配置的用户任务节点"
            type="info"
            :closable="false"
            style="margin-top: 20px"
          />
        </el-col>
      </div>

      <!-- Step 3：确认提交 -->
      <div v-show="activeStep === 2">
        <el-col :span="16" :offset="4">
          <el-descriptions title="发起信息确认" :column="1" border size="small" style="margin-bottom: 20px">
            <el-descriptions-item label="业务单号">{{ startForm.businessKey }}</el-descriptions-item>
            <el-descriptions-item label="流程名称">{{ procName }}</el-descriptions-item>
          </el-descriptions>
          <el-table :data="confirmTableData" border size="small">
            <el-table-column label="节点名称" prop="nodeName" />
            <el-table-column label="分配班组" prop="teamName">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.teamName" type="success">{{ scope.row.teamName }}</el-tag>
                <el-tag v-else type="warning">未配置</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </div>

      <!-- 底部按钮 -->
      <el-col :span="16" :offset="4" style="margin-top: 24px; text-align: center">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 2" type="primary" :loading="submitLoading" @click="submitAll">提 交</el-button>
        <el-button @click="resetAll">重 置</el-button>
      </el-col>
    </el-card>
  </div>
</template>

<script>
import { startProcessWithTeam, flowXmlAndNode } from '@/api/flowable/definition'
import { flowFormData } from '@/api/flowable/process'
import { listTeam } from '@/api/manage/team'

export default {
  name: 'ProcessSend',
  data() {
    return {
      activeStep: 0,
      submitLoading: false,
      deployId: '',
      procDefId: '',
      procName: '',
      formRenderData: {},
      formJson: {},
      formData: {},
      teamList: [],
      processNodes: [],
      nodeTeamMapArray: [],
      startForm: {
        businessKey: '',
        mainTeamId: null
      }
    }
  },
  computed: {
    confirmTableData() {
      return this.processNodes.map((node, index) => {
        const teamId = this.nodeTeamMapArray[index]
        const team = this.teamList.find(t => t.id === teamId)
        return {
          nodeName: node.name,
          teamName: team ? team.teamName : ''
        }
      })
    }
  },
  watch: {
    'startForm.mainTeamId'(newVal) {
      if (newVal) {
        this.nodeTeamMapArray = this.processNodes.map(() => newVal)
      }
    }
  },
  created() {
    this.deployId = this.$route.query && this.$route.query.deployId
    this.procDefId = this.$route.query && this.$route.query.procDefId
    this.procName = (this.$route.query && this.$route.query.procName) || ''
    this.startForm.businessKey = this.generateBusinessKey()
    this.getFlowFormData(this.deployId)
    this.loadTeamList()
    this.loadProcessNodes()
  },
  methods: {
    getFlowFormData(deployId) {
      flowFormData({ deployId }).then(res => {
        this.formJson = res.data
        this.$nextTick(() => {
          if (this.$refs.vFormRef) {
            this.$refs.vFormRef.setFormJson(res.data)
          }
        })
      }).catch((err) => {
        console.error('流程表单加载失败', err)
        this.$message.error('流程表单加载失败，请重试')
        this.goBack()
      })
    },
    loadTeamList() {
      listTeam({ pageNum: 1, pageSize: 100 }).then(res => {
        this.teamList = res.rows || res.data || []
      })
    },
    loadProcessNodes() {
      flowXmlAndNode({ deployId: this.deployId }).then(res => {
        const data = res.data || res
        const nodes = []
        if (data.xmlData && typeof data.xmlData === 'string') {
          const userTaskPattern = /<userTask\s+id="([^"]+)"\s+name="([^"]*)"/g
          let match
          while ((match = userTaskPattern.exec(data.xmlData)) !== null) {
            nodes.push({ id: match[1], name: match[2] })
          }
        }
        this.processNodes = nodes
        this.nodeTeamMapArray = new Array(nodes.length).fill(null)
      }).catch((err) => {
        console.error('流程节点加载失败', err)
        this.$message.error('流程节点加载失败，请刷新重试')
      })
    },
    nextStep() {
      if (this.activeStep === 0) {
        this.$refs.vFormRef.getFormData().then(data => {
          this.formData = data
          this.activeStep++
        }).catch(() => {
          this.$message.warning('请填写完整表单信息')
        })
      } else if (this.activeStep === 1) {
        const allConfigured = this.nodeTeamMapArray.every(v => v)
        if (!allConfigured) {
          this.$message.warning('请为所有节点分配班组')
          return
        }
        this.activeStep++
      }
    },
    prevStep() {
      this.activeStep--
    },
    submitAll() {
      const nodeTeamMap = {}
      this.processNodes.forEach((node, index) => {
        if (this.nodeTeamMapArray[index]) {
          nodeTeamMap[node.id] = this.nodeTeamMapArray[index]
        }
      })
      const requestData = {
        procDefId: this.procDefId,
        businessKey: this.startForm.businessKey || null,
        mainTeamId: this.startForm.mainTeamId || null,
        nodeTeamMap,
        variables: Object.assign({ formJson: this.formJson }, this.formData)
      }
      this.submitLoading = true
      startProcessWithTeam(requestData).then(res => {
        this.$modal.msgSuccess(res.msg || '流程启动成功')
        this.goBack()
      }).catch((err) => {
        this.$message.error((err && err.message) || '流程启动失败，请重试')
      }).finally(() => {
        this.submitLoading = false
      })
    },
    resetAll() {
      this.activeStep = 0
      this.formData = {}
      this.startForm.mainTeamId = null
      this.startForm.businessKey = this.generateBusinessKey()
      this.nodeTeamMapArray = new Array(this.processNodes.length).fill(null)
      this.$nextTick(() => {
        this.$refs.vFormRef && this.$refs.vFormRef.resetForm()
      })
    },
    goBack() {
      const obj = { path: '/task/process', query: { t: Date.now() }}
      this.$tab.closeOpenPage(obj)
    },
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

<style lang="scss" scoped>
.box-card {
  width: 100%;
  margin-bottom: 20px;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
</style>
