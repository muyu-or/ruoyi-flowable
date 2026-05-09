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
          <component
            :is="mainFormComponent"
            ref="mainFormRef"
            :initial-proc-name="procName"
            :initial-material-id="initialMaterialId"
          />
        </el-col>
      </div>

      <!-- Step 2：分配班组 -->
      <div v-show="activeStep === 1">
        <el-col :span="16" :offset="4">
          <!-- 流程时间范围提示 -->
          <el-alert
            v-if="formData.procDateStart && formData.procDateEnd"
            :title="'流程时间范围：' + formData.procDateStart + ' 至 ' + formData.procDateEnd"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          />
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
              <div
                v-for="(node, index) in processNodes"
                :key="node.id"
                style="border: 1px solid #EBEEF5; border-radius: 4px; padding: 12px 16px; margin-bottom: 12px"
              >
                <el-form-item :label="node.name" style="margin-bottom: 8px">
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
                <el-form-item label="计划日期" style="margin-bottom: 0">
                  <el-date-picker
                    :value="getNodeDateRange(index)"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="yyyy-MM-dd"
                    style="width: 100%"
                    :picker-options="nodeDatePickerOptions"
                    @input="val => setNodeDateRange(index, val)"
                  />
                </el-form-item>
              </div>
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
          <el-table :data="confirmTableData" border size="small" v-table-col-width="'confirm'">
            <el-table-column label="节点名称" prop="nodeName" />
            <el-table-column label="分配班组" prop="teamName">
              <template slot-scope="scope">
                <el-tag v-if="scope.row.teamName" type="success">{{ scope.row.teamName }}</el-tag>
                <el-tag v-else type="warning">未配置</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计划开始" prop="startDate" width="120">
              <template slot-scope="scope">
                <span v-if="scope.row.startDate">{{ scope.row.startDate }}</span>
                <el-tag v-else type="warning" size="mini">未填写</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计划结束" prop="endDate" width="120">
              <template slot-scope="scope">
                <span v-if="scope.row.endDate">{{ scope.row.endDate }}</span>
                <el-tag v-else type="warning" size="mini">未填写</el-tag>
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
import { getDeployForm } from '@/api/flowable/form'
import MainForm from '@/components/taskForms/MainForm'
import { TASK_FORM_COMPONENT_MAP } from '@/components/taskForms/index'
import store from '@/store'

export default {
  name: 'ProcessSend',
  components: {
    MainForm
  },
  data() {
    return {
      activeStep: 0,
      submitLoading: false,
      deployId: '',
      procDefId: '',
      procName: '',
      version: '',
      fromPath: '', // 来源路径，用于关闭时返回正确页面
      formData: {},
      teamList: [],
      processNodes: [],
      nodeTeamMapArray: [],
      nodeDateArray: [],
      startForm: {
        businessKey: '',
        mainTeamId: null
      },
      // 主表单组件名（默认 MainForm，由 getDeployForm 动态决定）
      mainFormComponentName: 'MainForm',
      // 来自库存页面的物料信息（路由query传入）
      initialMaterialId: '',
      initialMaterialName: '',
      initialInventoryId: ''
    }
  },
  computed: {
    confirmTableData() {
      return this.processNodes.map((node, index) => {
        const teamId = this.nodeTeamMapArray[index]
        const team = this.teamList.find(t => t.id === teamId)
        const dateInfo = this.nodeDateArray[index] || {}
        return {
          nodeName: node.name,
          teamName: team ? team.teamName : '',
          startDate: dateInfo.startDate || '',
          endDate: dateInfo.endDate || ''
        }
      })
    },
    procVersionDisplay() {
      return this.version ? 'v' + this.version : ''
    },
    operatorName() {
      return this.$store.state.user.name || ''
    },
    mainFormComponent() {
      return TASK_FORM_COMPONENT_MAP[this.mainFormComponentName] || MainForm
    },
    nodeDatePickerOptions() {
      const procStart = this.formData.procDateStart
      const procEnd = this.formData.procDateEnd
      if (!procStart || !procEnd) return {}
      const minTime = new Date(procStart).getTime() - 86400000
      const maxTime = new Date(procEnd).getTime()
      return {
        disabledDate(time) {
          return time.getTime() < minTime || time.getTime() > maxTime
        }
      }
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
    this.fromPath = (this.$route.query && this.$route.query.from) || ''
    this.initialMaterialId = (this.$route.query && this.$route.query.materialId) || ''
    this.initialMaterialName = (this.$route.query && this.$route.query.materialName) || ''
    this.initialInventoryId = (this.$route.query && this.$route.query.inventoryId) || ''
    this.startForm.businessKey = this.generateBusinessKey()
    this.loadMainFormComponent()
    this.loadTeamList()
    this.loadProcessNodes()
  },
  methods: {
    /** 根据 deployId 加载配置的主表单组件 */
    loadMainFormComponent() {
      if (!this.deployId) return
      getDeployForm(this.deployId).then(res => {
        const data = res.data
        if (data && data.formComponent) {
          this.$set(this, 'mainFormComponentName', data.formComponent)
        }
      }).catch(() => {
        // 加载失败时保持默认 MainForm，不影响发起流程
      })
    },
    loadTeamList() {
      listTeam({ pageNum: 1, pageSize: 100, teamStatus: '1' }).then(res => {
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
        this.nodeDateArray = nodes.map(() => ({ startDate: '', endDate: '' }))
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
        this.$refs.mainFormRef.getFormData().then(formData => {
          this.formData = Object.assign({}, formData, {
            procVersion: this.procVersionDisplay,
            operator: this.operatorName
          })
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
        // 验证所有节点已填日期
        const allDatesConfigured = this.nodeDateArray.every(d => d.startDate && d.endDate)
        if (!allDatesConfigured) {
          this.$message.warning('请为所有节点填写计划开始和结束日期')
          return
        }
        // 验证单节点内 startDate <= endDate
        for (let i = 0; i < this.nodeDateArray.length; i++) {
          const { startDate, endDate } = this.nodeDateArray[i]
          if (startDate > endDate) {
            this.$message.warning(`节点「${this.processNodes[i].name}」的开始日期不能晚于结束日期`)
            return
          }
        }
        // 验证顺序约束：节点 i 的 startDate >= 节点 i-1 的 startDate，endDate >= 节点 i-1 的 endDate
        for (let i = 1; i < this.nodeDateArray.length; i++) {
          if (this.nodeDateArray[i].startDate < this.nodeDateArray[i - 1].startDate) {
            this.$message.warning(
              `节点「${this.processNodes[i].name}」的开始日期不能早于上一节点「${this.processNodes[i - 1].name}」的开始日期`
            )
            return
          }
          if (this.nodeDateArray[i].endDate < this.nodeDateArray[i - 1].endDate) {
            this.$message.warning(
              `节点「${this.processNodes[i].name}」的截止日期不能早于上一节点「${this.processNodes[i - 1].name}」的截止日期`
            )
            return
          }
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
          const nodeTimeMap = {}
          this.processNodes.forEach((node, index) => {
            const d = this.nodeDateArray[index]
            if (d && d.startDate && d.endDate) {
              nodeTimeMap[node.id] = { startDate: d.startDate, endDate: d.endDate }
            }
          })
          vars.nodeTimeMap = nodeTimeMap
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
      this.nodeDateArray = this.processNodes.map(() => ({ startDate: '', endDate: '' }))
      if (this.$refs.mainFormRef && this.$refs.mainFormRef.setFormData) {
        this.$refs.mainFormRef.setFormData({
          taskName: '',
          procName: this.procName || '',
          procDateRange: [],
          remark: ''
        })
      }
    },
    goBack() {
      // 关闭当前 tab，回到上一个已打开的 tab
      // 若来自外部页面（如库存），直接 go(-1) 返回浏览器历史上一页
      // 若来自已发任务流程内部，跳回 /task/process
      if (this.fromPath) {
        // 关闭当前 tab 后跳回历史上一页
        store.dispatch('tagsView/delView', this.$route).then(() => {
          this.$router.go(-1)
        })
      } else {
        this.$tab.closeOpenPage({ path: '/task/process', query: { t: Date.now() }})
      }
    },
    getNodeDateRange(index) {
      const d = this.nodeDateArray[index]
      if (d && d.startDate && d.endDate) {
        return [d.startDate, d.endDate]
      }
      return null
    },
    setNodeDateRange(index, val) {
      if (Array.isArray(val) && val.length === 2) {
        this.$set(this.nodeDateArray[index], 'startDate', val[0] || '')
        this.$set(this.nodeDateArray[index], 'endDate', val[1] || '')
      } else {
        this.$set(this.nodeDateArray[index], 'startDate', '')
        this.$set(this.nodeDateArray[index], 'endDate', '')
      }
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
