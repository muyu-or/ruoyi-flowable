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

      <!-- Step 1：填写主表单 -->
      <div v-show="activeStep === 0">
        <el-col :span="16" :offset="4">
          <el-form ref="mainFormRef" :model="mainForm" :rules="mainFormRules" label-width="120px" size="small">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="mainForm.taskName" placeholder="请输入任务名称" />
            </el-form-item>
            <el-form-item label="流程名称" prop="procName">
              <el-input v-model="mainForm.procName" placeholder="请输入流程名称" />
            </el-form-item>
            <el-form-item label="流程版本">
              <el-input :value="procVersionDisplay" readonly />
            </el-form-item>
            <el-form-item label="流程日期" prop="procDateRange">
              <el-date-picker
                v-model="mainForm.procDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="操作人">
              <el-input :value="operatorName" readonly />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="mainForm.remark" type="textarea" :rows="3" placeholder="请输入备注（选填）" />
            </el-form-item>
          </el-form>
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
      version: '',
      mainForm: {
        taskName: '',
        procName: '',
        procDateRange: [],
        remark: ''
      },
      mainFormRules: {
        taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        procName: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
        procDateRange: [{ required: true, message: '请选择流程日期范围', trigger: 'change' }]
      },
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
    },
    procVersionDisplay() {
      return this.version ? 'v' + this.version : ''
    },
    operatorName() {
      return this.$store.state.user.name || ''
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
    this.version = (this.$route.query && this.$route.query.version) || ''
    this.mainForm.procName = this.procName
    this.startForm.businessKey = this.generateBusinessKey()
    this.loadTeamList()
    this.loadProcessNodes()
  },
  methods: {
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
          try {
            const parser = new DOMParser()
            const doc = parser.parseFromString(data.xmlData, 'application/xml')
            const userTasks = doc.getElementsByTagName('userTask')
            for (let i = 0; i < userTasks.length; i++) {
              const el = userTasks[i]
              const id = el.getAttribute('id')
              const name = el.getAttribute('name') || ''
              if (id) {
                nodes.push({ id, name })
              }
            }
          } catch (parseError) {
            console.error('解析 BPMN XML 失败', parseError)
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
        if (!this.$refs.mainFormRef) {
          this.$message.warning('表单未加载完成，请稍后重试')
          return
        }
        this.$refs.mainFormRef.validate((valid) => {
          if (valid) {
            this.formData = {
              ...this.mainForm,
              procVersion: this.procVersionDisplay,
              operator: this.operatorName,
              procDateRange: this.mainForm.procDateRange
            }
            this.activeStep++
          } else {
            this.$message.warning('请填写完整表单信息')
          }
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
        variables: (() => {
          const vars = Object.assign({}, this.formData)
          if (Array.isArray(vars.procDateRange) && vars.procDateRange.length === 2) {
            vars.procDateStart = vars.procDateRange[0]
            vars.procDateEnd = vars.procDateRange[1]
          }
          delete vars.procDateRange
          return vars
        })()
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
      this.mainForm = {
        taskName: '',
        procName: this.procName,
        procDateRange: [],
        remark: ''
      }
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
