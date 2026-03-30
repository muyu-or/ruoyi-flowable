<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span class="el-icon-document">已发任务</span>
        <el-button style="float: right;" size="mini" type="danger" @click="goBack">关闭</el-button>
      </div>
      <el-tabs v-model="activeName" tab-position="top" @tab-click="handleClick">
        <!--表单信息-->
        <el-tab-pane label="表单信息" name="1">
          <el-col :span="16" :offset="4">
            <!-- 无表单时提示 -->
            <el-empty v-if="!hasForm && componentNodes.length === 0" description="该流程无绑定表单" :image-size="80" style="padding: 40px 0;" />
            <!-- vForm 节点表单 -->
            <v-form-render v-show="hasForm" ref="vFormRef" />
            <!-- 自定义Vue组件节点（每个节点一个分区） -->
            <div v-for="(node, idx) in componentNodes" :key="node.taskDefKey" style="margin-top: 20px;">
              <div style="font-size: 14px; font-weight: 600; color: #303133; border-left: 3px solid #409EFF; padding-left: 10px; margin-bottom: 12px;">
                {{ node.taskName }}
              </div>
              <component
                :is="getNodeComponent(node.formComponent)"
                :ref="'compNode_' + idx"
                style="margin-bottom: 8px;"
              />
            </div>
          </el-col>
        </el-tab-pane>
        <!--流程流转记录-->
        <el-tab-pane label="流转记录" name="2">
          <el-col :span="16" :offset="4">
            <div class="block">
              <el-timeline>
                <el-timeline-item
                  v-for="(item,index ) in flowRecordList"
                  :key="index"
                  :icon="setIcon(item)"
                  :color="setColor(item)"
                >
                  <p style="font-weight: 700">
                    {{ item.taskName }}
                    <el-tag v-if="item.comment && item.comment.type === '2'" type="warning" size="mini" style="margin-left:6px">退回重审</el-tag>
                    <el-tag v-else-if="item.comment && item.comment.type === '3'" type="danger" size="mini" style="margin-left:6px">不通过</el-tag>
                  </p>
                  <el-card :body-style="{ padding: '10px' }">
                    <el-descriptions class="margin-top" :column="1" size="small" border>
                      <el-descriptions-item v-if="item.assigneeName" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user" />办理人</template>
                        {{ item.assigneeName }}
                        <el-tag type="info" size="mini">{{ item.deptName }}</el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.handlers" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user" />处理人员</template>
                        {{ item.handlers }}
                      </el-descriptions-item>
                      <el-descriptions-item v-else-if="item.candidate" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user" />班组成员</template>
                        {{ item.candidate }}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.planStartDate || item.planEndDate" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-date" />计划时间</template>
                        {{ item.planStartDate || '?' }} ~ {{ item.planEndDate || '?' }}
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
                        <span :style="{ color: getCommentColor(item.comment.type) }">{{ item.comment.comment }}</span>
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
    </el-card>
  </div>
</template>

<script>
import { flowRecord } from '@/api/flowable/finished'
import { flowXmlAndNode } from '@/api/flowable/definition'
import { flowTaskFormByProcInst } from '@/api/flowable/todo'
import BpmnViewer from '@/components/Process/viewer'
import { TASK_FORM_COMPONENT_MAP } from '@/components/taskForms/index'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'

export default {
  name: 'MyProcessDetail',
  components: {
    BpmnViewer
  },
  props: {},
  data() {
    return {
      // 模型xml数据
      flowData: {},
      activeName: '1',
      // 遮罩层
      loading: true,
      hasForm: false,
      // 自定义Vue组件节点列表（已完成流程聚合）
      componentNodes: [],
      flowRecordList: [], // 流程流转数据
      taskForm: {
        comment: '', // 意见内容
        procInsId: '', // 流程实例编号
        instanceId: '', // 流程实例编号
        deployId: '', // 流程定义编号
        taskId: '', // 流程任务编号
        procDefId: '' // 流程编号
      }
    }
  },
  created() {
    this.taskForm.deployId = this.$route.query && this.$route.query.deployId
    this.taskForm.taskId = this.$route.query && this.$route.query.taskId
    this.taskForm.procInsId = this.$route.query && this.$route.query.procInsId
    // 优先用 procInsId 聚合已完成节点；procInsId 不存在时降级用 taskId
    if (this.taskForm.procInsId) {
      this.loadTaskForm(this.taskForm.procInsId)
    } else if (this.taskForm.taskId) {
      this.loadTaskFormByTaskId(this.taskForm.taskId)
    }
    this.getFlowRecordList(this.taskForm.procInsId, this.taskForm.deployId)
  },
  methods: {
    handleClick(tab, event) {
      if (tab.name === '3') {
        flowXmlAndNode({ procInsId: this.taskForm.procInsId, deployId: this.taskForm.deployId }).then(res => {
          this.flowData = res.data
        })
      }
    },
    setIcon(item) {
      if (!item.finishTime) return 'el-icon-time'
      if (item.comment) {
        if (item.comment.type === '2') return 'el-icon-refresh-left'
        if (item.comment.type === '3') return 'el-icon-close'
      }
      return 'el-icon-check'
    },
    setColor(item) {
      if (!item.finishTime) return '#b3bdbb'
      if (item.comment) {
        if (item.comment.type === '2') return '#e6a23c'
        if (item.comment.type === '3') return '#f56c6c'
      }
      return '#2bc418'
    },
    getCommentColor(type) {
      if (type === '2') return '#e6a23c'
      if (type === '3') return '#f56c6c'
      return ''
    },
    /** 根据组件名查找对应的 Vue 组件 */
    getNodeComponent(formComponent) {
      return TASK_FORM_COMPONENT_MAP[formComponent] || null
    },
    /** 加载任务表单（按 procInsId 聚合所有已完成节点，支持实时查看进行中流程） */
    loadTaskForm(procInsId) {
      flowTaskFormByProcInst(procInsId).then(res => {
        this.renderFormData(res.data || {})
      }).catch(() => {
        this.hasForm = false
      })
    },
    /** 降级方法：按 taskId 加载（procInsId 不可用时兜底） */
    loadTaskFormByTaskId(taskId) {
      // 复用 flowTaskForm 接口，逻辑与 loadTaskForm 相同
      import('@/api/flowable/todo').then(({ flowTaskForm }) => {
        flowTaskForm({ taskId }).then(res => {
          this.renderFormData(res.data || {})
        }).catch(() => {
          this.hasForm = false
        })
      })
    },
    /** 公共渲染逻辑：将后端返回的聚合数据渲染到表单 */
    renderFormData(data) {
      const formJson = data.formJson || {}

      // 兜底：确保 widgetList 和 formConfig 都存在
      if (!formJson.widgetList) formJson.widgetList = []
      if (!formJson.formConfig) {
        formJson.formConfig = {
          modelName: 'formData', refName: 'vForm', rulesName: 'rules',
          labelWidth: 80, labelPosition: 'left', size: '',
          labelAlign: 'label-left-align', cssCode: '', customClass: [],
          functions: '', layoutType: 'PC', jsonVersion: 3
        }
      }

      // 处理自定义Vue组件节点
      const rawComponentNodes = Array.isArray(data._componentNodes) ? data._componentNodes : []
      this.$set(this, 'componentNodes', rawComponentNodes)

      // 渲染自定义组件节点：nextTick 后逐一 setFormData + setReadonly
      if (rawComponentNodes.length > 0) {
        this.$nextTick(() => {
          rawComponentNodes.forEach((node, idx) => {
            const refKey = 'compNode_' + idx
            const compRef = this.$refs[refKey]
            // $refs 可能是数组（v-for 内）
            const comp = Array.isArray(compRef) ? compRef[0] : compRef
            if (comp) {
              if (typeof comp.setFormData === 'function') {
                comp.setFormData(node.formData || {})
              }
              if (typeof comp.setReadonly === 'function') {
                comp.setReadonly(true)
              }
            }
          })
        })
      }

      // vForm 节点处理
      if (!formJson.widgetList.length) {
        this.hasForm = false
        return
      }

      this.hasForm = true

      // 聚合所有节点命名空间下的表单数据并平铺，用于 setFormData 回填
      const mergedFormData = {}
      Object.keys(data).forEach(key => {
        if (key.endsWith('__formData') && data[key] && typeof data[key] === 'object') {
          Object.assign(mergedFormData, data[key])
        }
      })
      // 兼容旧数据：顶层裸字段值也保留
      Object.assign(mergedFormData, data)

      this.$nextTick(() => {
        this.$refs.vFormRef.setFormJson(formJson)
        this.$nextTick(() => {
          this.$refs.vFormRef.setFormData(mergedFormData)
          this.$nextTick(() => {
            // 已发任务表单全部只读
            this.$refs.vFormRef.disableForm()
          })
        })
      })
    },
    /** 流程流转记录 */
    getFlowRecordList(procInsId, deployId) {
      const params = { procInsId: procInsId, deployId: deployId }
      flowRecord(params).then(res => {
        this.flowRecordList = res.data.flowList
      }).catch(() => {
        this.goBack()
      })
    },
    /** 返回页面 */
    goBack() {
      // 关闭当前标签页并返回上个页面
      const obj = { path: '/task/process', query: { t: Date.now() }}
      this.$tab.closeOpenPage(obj)
    }
  }
}
</script>
<style lang="scss" scoped>
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
