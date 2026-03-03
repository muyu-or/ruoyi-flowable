<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="el-icon-document">待办任务</span>
        <el-tag style="margin-left:10px">发起人:{{ startUser }}</el-tag>
        <el-tag>任务节点:{{ taskName }}</el-tag>
        <el-button style="float: right;" size="mini" type="danger" @click="goBack">关闭</el-button>
      </div>
      <el-tabs v-model="activeName" tab-position="top" @tab-click="handleClick">
        <!--表单信息-->
        <el-tab-pane label="表单信息" name="1">
          <el-col :span="16" :offset="4">
            <!-- 自定义节点表单（根据 taskDefinitionKey 动态渲染） -->
            <component
              :is="currentFormComponent"
              v-if="currentFormComponent"
              ref="taskFormRef"
            />
            <!-- 降级：无自定义表单时使用 vForm 渲染 -->
            <v-form-render v-else ref="vFormRef" />
            <div style="margin-left:10%;margin-bottom: 20px;font-size: 14px;">
              <!-- 班组长：显示"审批"按钮，可做审批决定 -->
              <el-button v-if="isLeader" type="primary" @click="handleComplete">审 批</el-button>
              <!-- 班组成员：只显示"提交表单"按钮，保存数据但不推进流程 -->
              <el-button v-else type="success" @click="handleSubmitForm">提交表单</el-button>
            </div>
          </el-col>
        </el-tab-pane>

        <!--流程流转记录-->
        <el-tab-pane label="流转记录" name="2">
          <!--flowRecordList-->
          <el-col :span="16" :offset="4">
            <div class="block">
              <el-timeline>
                <el-timeline-item
                  v-for="(item,index ) in flowRecordList"
                  :key="index"
                  :icon="setIcon(item.finishTime)"
                  :color="setColor(item.finishTime)"
                >
                  <p style="font-weight: 700">{{ item.taskName }}</p>
                  <el-card :body-style="{ padding: '10px' }">
                    <el-descriptions class="margin-top" :column="1" size="small" border>
                      <el-descriptions-item v-if="item.assigneeName" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user" />办理人</template>
                        {{ item.assigneeName }}
                        <el-tag type="info" size="mini">{{ item.deptName }}</el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.candidate" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user" />候选办理</template>
                        {{ item.candidate }}
                      </el-descriptions-item>
                      <el-descriptions-item label-class-name="my-label">
                        <template slot="label"><i class="el-icon-date" />接收时间</template>
                        {{ item.createTime }}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.finishTime" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-date" />处理时间</template>
                        {{ item.finishTime }}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.duration" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-time" />耗时</template>
                        {{ item.duration }}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.comment" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-tickets" />处理意见</template>
                        {{ item.comment.comment }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-col>
        </el-tab-pane>
        <!--流程图-->
        <el-tab-pane label="流程图" name="3">
          <bpmn-viewer :flow-data="flowData" :proc-ins-id="taskForm.procInsId" />
        </el-tab-pane>
      </el-tabs>
      <!--审批任务-->
      <el-dialog :title="completeTitle" :visible.sync="completeOpen" width="60%" append-to-body>
        <el-form ref="taskForm" :model="taskForm">
          <el-form-item prop="targetKey">
            <flow-user v-if="checkSendUser" :check-type="checkType" @handleUserSelect="handleUserSelect" />
            <flow-role v-if="checkSendRole" @handleRoleSelect="handleRoleSelect" />
          </el-form-item>

          <el-form-item
            label="审批结果"
            label-width="80px"
            prop="approvalStatus"
            :rules="[{ required: true, message: '请选择审批结果', trigger: 'change' }]"
          >
            <el-radio-group v-model="taskForm.approvalStatus" @change="onApprovalStatusChange">
              <el-radio label="approved">通过</el-radio>
              <el-radio label="returned">退回重审</el-radio>
              <el-radio label="rejected">不通过</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item
            label="处理意见"
            label-width="80px"
            prop="comment"
            :rules="[{ required: true, message: '请输入处理意见', trigger: 'blur' }]"
          >
            <el-input v-model="taskForm.comment" type="textarea" placeholder="请输入处理意见" />
          </el-form-item>

          <!-- 退回重审：无需选择节点，直接重置当前节点 -->
          <!-- 原"返回上一节点"退回节点选择区域已隐藏，代码保留供后续恢复使用 -->
          <!--
          <el-form-item v-if="taskForm.approvalStatus === 'returned'" label="退回节点" label-width="80px" prop="targetKey">
            <span v-if="loadingReturnList" style="color: #909399; font-size: 12px;">加载中...</span>
            <template v-else-if="returnTaskList.length === 0">
              <span style="color: #ff6b6b; font-size: 12px;">无可退回的节点，当前已是第一个节点</span>
            </template>
            <template v-else-if="returnTaskList.length === 1">
              <el-tag type="info">{{ returnTaskList[0].name }}</el-tag>
              <span style="color: #909399; font-size: 12px; margin-left: 8px;">（已自动选中）</span>
            </template>
            <template v-else>
              <el-select v-model="taskForm.targetKey" placeholder="请选择要退回的节点">
                <el-option
                  v-for="item in returnTaskList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-form-item>
          -->
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="completeOpen = false">取 消</el-button>
          <el-button type="primary" @click="taskComplete">确 定</el-button>
        </span>
      </el-dialog>
      <!--退回流程-->
      <el-dialog :title="returnTitle" :visible.sync="returnOpen" width="40%" append-to-body>
        <el-form ref="taskForm" :model="taskForm" label-width="80px">
          <el-form-item label="退回节点" prop="targetKey">
            <el-radio-group v-model="taskForm.targetKey">
              <el-radio-button
                v-for="item in returnTaskList"
                :key="item.id"
                :label="item.id"
              >{{ item.name }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            label="退回意见"
            prop="comment"
            :rules="[{ required: true, message: '请输入意见', trigger: 'blur' }]"
          >
            <el-input v-model="taskForm.comment" style="width: 50%" type="textarea" placeholder="请输入意见" />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="returnOpen = false">取 消</el-button>
          <el-button type="primary" @click="taskReturn">确 定</el-button>
        </span>
      </el-dialog>
      <!--驳回流程-->
      <el-dialog :title="rejectTitle" :visible.sync="rejectOpen" width="40%" append-to-body>
        <el-form ref="taskForm" :model="taskForm" label-width="80px">
          <el-form-item
            label="驳回意见"
            prop="comment"
            :rules="[{ required: true, message: '请输入意见', trigger: 'blur' }]"
          >
            <el-input v-model="taskForm.comment" style="width: 50%" type="textarea" placeholder="请输入意见" />
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="rejectOpen = false">取 消</el-button>
          <el-button type="primary" @click="taskReject">确 定</el-button>
        </span>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { flowRecord } from '@/api/flowable/finished'
import FlowUser from '@/components/flow/User'
import FlowRole from '@/components/flow/Role'
import { flowXmlAndNode } from '@/api/flowable/definition'
import {
  complete,
  rejectTask,
  returnList, // 返回上一节点功能备用，当前界面不展示
  returnTask, // 返回上一节点功能备用，当前界面不展示
  redoTask,
  getNextFlowNode,
  delegate,
  flowTaskForm,
  isLeaderOfTask,
  saveFormData
} from '@/api/flowable/todo'
import BpmnViewer from '@/components/Process/viewer'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { TASK_FORM_MAP, TASK_FORM_COMPONENT_MAP } from '@/components/taskForms/index'

export default {
  name: 'Record',
  components: {
    BpmnViewer,
    FlowUser,
    FlowRole
  },
  props: {},
  data() {
    return {
      eventName: 'click',
      // 流程数据
      flowData: {},
      activeName: '1',
      // 遮罩层
      loading: true,
      flowRecordList: [], // 流程流转数据
      rules: {}, // 表单校验
      taskForm: {
        returnTaskShow: false, // 是否展示回退表单
        delegateTaskShow: false, // 是否展示回退表单
        defaultTaskShow: true, // 默认处理
        comment: '', // 意见内容
        approvalStatus: '', // 审批结果：approved/rejected
        procInsId: '', // 流程实例编号
        instanceId: '', // 流程实例编号
        deployId: '', // 流程定义编号
        taskId: '', // 流程任务编号
        procDefId: '', // 流程编号
        targetKey: '',
        variables: {}
      },
      returnTaskList: [], // 回退列表数据
      completeTitle: null,
      completeOpen: false,
      returnTitle: null,
      returnOpen: false,
      rejectOpen: false,
      rejectTitle: null,
      checkSendUser: false, // 是否展示人员选择模块
      checkSendRole: false, // 是否展示角色选择模块
      checkType: 'single', // 选择类型
      taskName: null, // 任务节点
      startUser: null, // 发起人信息,
      multiInstanceVars: '', // 会签节点
      formJson: {},
      taskDefinitionKey: '', // 当前节点 key，用于表单数据命名空间隔离
      formComponent: '', // 后端返回的自定义组件名（extensionElements 绑定机制）
      loadingReturnList: false, // 加载退回节点列表状态
      isLeader: true // 当前用户是否为班组长（默认 true，避免权限检查延迟期间按钮消失）
    }
  },
  computed: {
    currentFormComponent() {
      // 优先：后端通过 extensionElements 返回的组件名（新机制，跨流程复用）
      if (this.formComponent) {
        return TASK_FORM_COMPONENT_MAP[this.formComponent] || null
      }
      // 兼容：旧的节点 key 硬编码映射
      if (!this.taskDefinitionKey) return null
      return TASK_FORM_MAP[this.taskDefinitionKey] || null
    }
  },
  created() {
    if (this.$route.query) {
      this.taskName = this.$route.query.taskName
      this.startUser = this.$route.query.startUser
      this.taskForm.deployId = this.$route.query.deployId
      this.taskForm.taskId = this.$route.query.taskId
      this.taskForm.procInsId = this.$route.query.procInsId
      this.taskForm.executionId = this.$route.query.executionId
      this.taskForm.instanceId = this.$route.query.procInsId
      // 流程任务获取变量信息
      if (this.taskForm.taskId) {
        this.getFlowTaskForm(this.taskForm.taskId)
        this.checkIsLeader(this.taskForm.taskId)
      }
      this.getFlowRecordList(this.taskForm.procInsId, this.taskForm.deployId)
    }
  },
  methods: {
    handleClick(tab, event) {
      if (tab.name === '3') {
        flowXmlAndNode({ procInsId: this.taskForm.procInsId, deployId: this.taskForm.deployId }).then(res => {
          this.flowData = res.data
        })
      }
    },
    setIcon(val) {
      if (val) {
        return 'el-icon-check'
      } else {
        return 'el-icon-time'
      }
    },
    setColor(val) {
      if (val) {
        return '#2bc418'
      } else {
        return '#b3bdbb'
      }
    },
    // 用户信息选中数据
    handleUserSelect(selection) {
      if (selection) {
        if (selection instanceof Array) {
          const selectVal = selection.map(item => item.userId.toString())
          if (this.multiInstanceVars) {
            this.$set(this.taskForm.variables, this.multiInstanceVars, selectVal)
          } else {
            this.$set(this.taskForm.variables, 'approval', selectVal.join(','))
          }
        } else {
          this.$set(this.taskForm.variables, 'approval', selection.userId.toString())
        }
      }
    },
    // 角色信息选中数据
    handleRoleSelect(selection, roleName) {
      if (selection) {
        if (selection instanceof Array) {
          const selectVal = selection.map(item => item.roleId.toString())
          this.$set(this.taskForm.variables, 'approval', selectVal.join(','))
        } else {
          this.$set(this.taskForm.variables, 'approval', selection)
        }
      }
    },
    /** 流程流转记录 */
    getFlowRecordList(procInsId, deployId) {
      const that = this
      const params = { procInsId: procInsId, deployId: deployId }
      flowRecord(params).then(res => {
        that.flowRecordList = res.data.flowList
      }).catch(res => {
        this.goBack()
      })
    },
    /** 流程节点表单 */
    getFlowTaskForm(taskId) {
      if (!taskId) return
      flowTaskForm({ taskId: taskId }).then(res => {
        this.taskDefinitionKey = res.data._taskDefinitionKey || ''
        this.formComponent = res.data._formComponent || ''

        if (this.currentFormComponent) {
          // 有自定义表单组件：只回填数据，不走 vFormRef
          this.$nextTick(() => {
            if (this.$refs.taskFormRef) {
              const nsKey = this.taskDefinitionKey + '__formData'
              const nsData = res.data[nsKey] || {}
              this.$refs.taskFormRef.setFormData(nsData)
            }
          })
        } else {
          // 降级：走原来的 vForm 渲染逻辑
          const formJson = res.data.formJson || {}
          if (!formJson.widgetList) {
            formJson.widgetList = []
          }
          if (!formJson.formConfig) {
            formJson.formConfig = {
              modelName: 'formData', refName: 'vForm', rulesName: 'rules',
              labelWidth: 80, labelPosition: 'left', size: '',
              labelAlign: 'label-left-align', cssCode: '', customClass: [],
              functions: '', layoutType: 'PC', jsonVersion: 3
            }
          }
          this.formJson = formJson
          if (this.$refs.vFormRef) {
            this.$refs.vFormRef.setFormJson(formJson)
          }
          this.$nextTick(() => {
            if (this.$refs.vFormRef) {
              this.$refs.vFormRef.setFormData(res.data)
            }
          })
        }
      })
    },

    /** 检查当前用户是否为班组长 */
    checkIsLeader(taskId) {
      isLeaderOfTask(taskId).then(res => {
        this.isLeader = !!res.data
      }).catch(() => {
        // 异常时默认允许审批，避免阻塞用户操作
        this.isLeader = true
      })
    },

    /** 班组成员提交表单数据（不推进流程） */
    handleSubmitForm() {
      let getDataPromise
      if (this.currentFormComponent && this.$refs.taskFormRef) {
        getDataPromise = this.$refs.taskFormRef.getFormData()
      } else if (
        this.formJson &&
        Array.isArray(this.formJson.widgetList) &&
        this.formJson.widgetList.length > 0 &&
        this.$refs.vFormRef
      ) {
        getDataPromise = this.$refs.vFormRef.getFormData()
      } else {
        getDataPromise = Promise.resolve({})
      }

      getDataPromise.then(formData => {
        const variables = {}
        if (this.taskDefinitionKey) {
          this.$set(variables, this.taskDefinitionKey + '__formData', formData)
        } else {
          Object.assign(variables, formData)
        }
        const taskVo = {
          taskId: this.taskForm.taskId,
          instanceId: this.taskForm.instanceId,
          variables: variables
        }
        saveFormData(taskVo).then(() => {
          this.$modal.msgSuccess('表单已提交，等待班组长审批')
          this.goBack()
        }).catch(() => {
          this.$modal.msgError('提交表单失败')
        })
      }).catch(() => {
        this.$message.warning('请先完整填写表单')
      })
    },

    /** 委派任务 */
    handleDelegate() {
      this.taskForm.delegateTaskShow = true
      this.taskForm.defaultTaskShow = false
    },
    handleAssign() {

    },
    /** 返回页面 */
    goBack() {
      // 关闭当前标签页并返回上个页面
      const obj = { path: '/task/todo', query: { t: Date.now() }}
      this.$tab.closeOpenPage(obj)
    },
    /** 驳回任务 */
    handleReject() {
      this.rejectOpen = true
      this.rejectTitle = '驳回流程'
    },
    /** 驳回任务 */
    taskReject() {
      this.$refs['taskForm'].validate(valid => {
        if (valid) {
          rejectTask(this.taskForm).then(res => {
            this.$modal.msgSuccess(res.msg)
            this.goBack()
          })
        }
      })
    },
    /** 可退回任务列表 */
    handleReturn() {
      this.returnOpen = true
      this.returnTitle = '退回流程'
      returnList(this.taskForm).then(res => {
        this.returnTaskList = res.data
      })
    },
    /** 提交退回任务 */
    taskReturn() {
      this.$refs['taskForm'].validate(valid => {
        if (valid) {
          returnTask(this.taskForm).then(res => {
            this.$modal.msgSuccess(res.msg)
            this.goBack()
          })
        }
      })
    },
    /** 取消回退任务按钮 */
    cancelTask() {
      this.taskForm.returnTaskShow = false
      this.taskForm.defaultTaskShow = true
      this.returnTaskList = []
    },
    /** 委派任务 */
    submitDeleteTask() {
      this.$refs['taskForm'].validate(valid => {
        if (valid) {
          delegate(this.taskForm).then(response => {
            this.$modal.msgSuccess(response.msg)
            this.goBack()
          })
        }
      })
    },
    /** 取消回退任务按钮 */
    cancelDelegateTask() {
      this.taskForm.delegateTaskShow = false
      this.taskForm.defaultTaskShow = true
      this.returnTaskList = []
    },
    /** 加载审批任务弹框 */
    handleComplete() {
      this.submitForm().then(() => {
        this.completeOpen = true
        this.completeTitle = '流程审批'
      }).catch(() => {
        // 表单校验未通过，不打开审批弹框
        this.$message.warning('请先完整填写表单')
      })
    },
    /** 用户审批任务 */
    taskComplete() {
      if (!this.taskForm.variables && this.checkSendUser) {
        this.$modal.msgError('请选择流程接收人员!')
        return
      }
      if (!this.taskForm.variables && this.checkSendRole) {
        this.$modal.msgError('请选择流程接收角色组!')
        return
      }
      if (!this.taskForm.approvalStatus) {
        this.$modal.msgError('请选择审批结果!')
        return
      }
      if (!this.taskForm.comment) {
        this.$modal.msgError('请输入处理意见!')
        return
      }
      // 退回重审无需校验退回节点（直接重置当前节点）
      // 原"返回上一节点"校验逻辑保留，供后续恢复使用：
      // if (this.taskForm.approvalStatus === 'returned' && this.returnTaskList.length === 0) {
      //   this.$modal.msgError('无可退回的节点，当前已是第一个节点!')
      //   return
      // }
      // if (this.taskForm.approvalStatus === 'returned' && this.returnTaskList.length > 1 && !this.taskForm.targetKey) {
      //   this.$modal.msgError('请选择要退回的节点!')
      //   return
      // }

      if (this.taskForm.approvalStatus === 'approved') {
        this.completeTaskSuccess()
      } else if (this.taskForm.approvalStatus === 'returned') {
        this.doRedoTask()
      } else if (this.taskForm.approvalStatus === 'rejected') {
        this.doRejectTask()
      }
    },

    /** 通过审批 */
    completeTaskSuccess() {
      if (!this.taskForm.variables) {
        this.taskForm.variables = {}
      }
      this.taskForm.variables.approval_status = 'approved'
      complete(this.taskForm).then(response => {
        this.$modal.msgSuccess('审批通过，流程继续进行!')
        this.completeOpen = false
        this.goBack()
      }).catch(() => {
        this.$modal.msgError('完成任务失败')
      })
    },

    /** 退回重审：当前节点重置，由同班组人员重新处理 */
    doRedoTask() {
      const taskVo = {
        taskId: this.taskForm.taskId,
        instanceId: this.taskForm.instanceId,
        comment: this.taskForm.comment
      }
      redoTask(taskVo).then(() => {
        this.$modal.msgSuccess('已退回重审，等待同班组人员重新处理!')
        this.completeOpen = false
        this.goBack()
      }).catch(() => {
        this.$modal.msgError('退回重审失败')
      })
    },

    /** 返回上一节点（功能保留，当前界面不展示） */
    // returnTaskToNode() {
    //   const taskVo = {
    //     taskId: this.taskForm.taskId,
    //     instanceId: this.taskForm.instanceId,
    //     targetKey: this.taskForm.targetKey,
    //     comment: this.taskForm.comment
    //   }
    //   returnTask(taskVo).then(() => {
    //     this.$modal.msgSuccess('已退回到上一节点!')
    //     this.completeOpen = false
    //     this.goBack()
    //   }).catch(() => {
    //     this.$modal.msgError('退回失败')
    //   })
    // },

    /** 不通过 - 退回到流程发起节点，初始节点时直接终止流程 */
    doRejectTask() {
      rejectTask(this.taskForm).then(() => {
        this.$modal.msgSuccess('已不通过!')
        this.completeOpen = false
        this.goBack()
      }).catch(() => {
        this.$modal.msgError('操作失败')
      })
    },

    /** 审批结果选择改变事件 */
    onApprovalStatusChange() {
      // 退回重审无需加载退回节点列表，直接重置当前节点
      // 原"返回上一节点"逻辑保留：
      // if (this.taskForm.approvalStatus === 'returned' && this.returnTaskList.length === 0) {
      //   this.loadReturnTaskList()
      // }
    },

    /** 加载可退回的节点列表（返回上一节点功能备用） */
    loadReturnTaskList() {
      this.loadingReturnList = true
      const params = { taskId: this.taskForm.taskId }
      returnList(params).then(res => {
        this.returnTaskList = res.data || []
        if (this.returnTaskList.length === 0) {
          this.$message.warning('无可退回的节点，当前已是第一个节点')
          // 自动重置审批结果，避免用户卡在无法提交的状态
          this.taskForm.approvalStatus = ''
        } else if (this.returnTaskList.length === 1) {
          // 只有一个可退回节点时自动选中，无需用户手动选择
          this.taskForm.targetKey = this.returnTaskList[0].id
        }
        this.loadingReturnList = false
      }).catch(() => {
        this.$message.error('加载退回节点失败')
        this.loadingReturnList = false
      })
    },
    /** 申请流程表单数据提交 */
    submitForm() {
      // 根据当前任务或者流程设计配置的下一步节点 todo 暂时未涉及到考虑网关、表达式和多节点情况
      const params = { taskId: this.taskForm.taskId }
      return getNextFlowNode(params).then(res => {
        // 确定取表单数据的方式：
        //   1. 有自定义 Vue 组件 → taskFormRef.getFormData()
        //   2. 有 vForm 字段配置 → vFormRef.getFormData()
        //   3. 无任何表单（节点未绑定表单） → 直接返回空对象，允许通过审批
        let getDataPromise
        if (this.currentFormComponent && this.$refs.taskFormRef) {
          getDataPromise = this.$refs.taskFormRef.getFormData()
        } else if (
          this.formJson &&
          Array.isArray(this.formJson.widgetList) &&
          this.formJson.widgetList.length > 0 &&
          this.$refs.vFormRef
        ) {
          getDataPromise = this.$refs.vFormRef.getFormData()
        } else {
          // 节点未配置表单，直接用空对象，不阻塞审批流程
          getDataPromise = Promise.resolve({})
        }

        return getDataPromise.then(formData => {
          if (this.taskDefinitionKey) {
            this.$set(this.taskForm.variables, this.taskDefinitionKey + '__formData', formData)
          } else {
            Object.assign(this.taskForm.variables, formData)
          }
          this.taskForm.variables.formJson = this.formJson
          const data = res.data
          if (data) {
            if (data.dataType === 'dynamic') {
              if (data.type === 'assignee') { // 指定人员
                this.checkSendUser = true
                this.checkType = 'single'
              } else if (data.type === 'candidateUsers') { // 候选人员(多个)
                this.checkSendUser = true
                this.checkType = 'multiple'
              } else if (data.type === 'candidateGroups') { // 指定组(所属角色接收任务)
                this.checkSendRole = true
              } else { // 会签
                // 流程设计指定的 elementVariable 作为会签人员列表
                this.multiInstanceVars = data.vars
                this.checkSendUser = true
                this.checkType = 'multiple'
              }
            }
          }
        })
      })
    },
    // 动态绑定操作按钮的点击事件
    handleButtonClick(method) {
      this[method]()
    }
  }
}
</script>
<style lang="scss" scoped>
.test-form {
  margin: 15px auto;
  width: 800px;
  padding: 15px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both
}

.box-card {
  width: 100%;
  margin-bottom: 20px;
}

.el-tag + .el-tag {
  margin-left: 10px;
}

.my-label {
  background: #E1F3D8;
}
</style>
