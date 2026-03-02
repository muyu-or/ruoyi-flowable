<template>
  <div class="app-container">
    <el-card class="box-card" >
      <div slot="header" class="clearfix">
        <span class="el-icon-document">已发任务</span>
        <el-button style="float: right;" size="mini" type="danger" @click="goBack">关闭</el-button>
      </div>
      <el-tabs  tab-position="top" v-model="activeName" @tab-click="handleClick">
        <!--表单信息-->
        <el-tab-pane label="表单信息" name="1">
          <el-col :span="16" :offset="4">
            <!-- 无表单时提示 -->
            <el-empty v-if="!hasForm && componentNodes.length === 0" description="该流程无绑定表单" :image-size="80" style="padding: 40px 0;"/>
            <!-- vForm 节点表单 -->
            <v-form-render v-show="hasForm" ref="vFormRef"/>
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
          <el-col :span="16" :offset="4" >
            <div class="block">
              <el-timeline>
                <el-timeline-item
                  v-for="(item,index ) in flowRecordList"
                  :key="index"
                  :icon="setIcon(item.finishTime)"
                  :color="setColor(item.finishTime)"
                >
                  <p style="font-weight: 700">{{item.taskName}}</p>
                  <el-card :body-style="{ padding: '10px' }">
                    <el-descriptions class="margin-top" :column="1" size="small" border>
                      <el-descriptions-item v-if="item.assigneeName" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user"></i>办理人</template>
                        {{item.assigneeName}}
                        <el-tag type="info" size="mini">{{item.deptName}}</el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.candidate" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user"></i>候选办理</template>
                        {{item.candidate}}
                      </el-descriptions-item>
                      <el-descriptions-item label-class-name="my-label">
                        <template slot="label"><i class="el-icon-date"></i>接收时间</template>
                        {{item.createTime}}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.finishTime" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-date"></i>处理时间</template>
                        {{item.finishTime}}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.duration"  label-class-name="my-label">
                        <template slot="label"><i class="el-icon-time"></i>耗时</template>
                        {{item.duration}}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.comment" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-tickets"></i>处理意见</template>
                        {{item.comment.comment}}
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
          <bpmn-viewer :flowData="flowData" :procInsId="taskForm.procInsId"/>
        </el-tab-pane>
    </el-tabs>
    </el-card>
  </div>
</template>

<script>
import {flowRecord} from "@/api/flowable/finished";
import {flowXmlAndNode} from "@/api/flowable/definition";
import {flowTaskForm} from "@/api/flowable/todo";
import BpmnViewer from '@/components/Process/viewer';
import { TASK_FORM_COMPONENT_MAP } from '@/components/taskForms/index';
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "MyProcessDetail",
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
      taskForm:{
        comment:"", // 意见内容
        procInsId: "", // 流程实例编号
        instanceId: "", // 流程实例编号
        deployId: "",  // 流程定义编号
        taskId: "" ,// 流程任务编号
        procDefId: "",  // 流程编号
      },
    };
  },
  created() {
    this.taskForm.deployId = this.$route.query && this.$route.query.deployId;
    this.taskForm.taskId  = this.$route.query && this.$route.query.taskId;
    this.taskForm.procInsId = this.$route.query && this.$route.query.procInsId;
    if (this.taskForm.taskId) {
      this.loadTaskForm(this.taskForm.taskId);
    }
    this.getFlowRecordList(this.taskForm.procInsId, this.taskForm.deployId);
  },
  methods: {
    handleClick(tab, event) {
      if (tab.name === '3'){
        flowXmlAndNode({procInsId:this.taskForm.procInsId,deployId:this.taskForm.deployId}).then(res => {
          this.flowData = res.data;
        })
      }
    },
    setIcon(val) {
      return val ? "el-icon-check" : "el-icon-time";
    },
    setColor(val) {
      return val ? "#2bc418" : "#b3bdbb";
    },
    /** 根据组件名查找对应的 Vue 组件 */
    getNodeComponent(formComponent) {
      return TASK_FORM_COMPONENT_MAP[formComponent] || null;
    },
    /** 加载任务表单（支持已完成流程聚合所有节点表单） */
    loadTaskForm(taskId) {
      flowTaskForm({taskId: taskId}).then(res => {
        const data = res.data || {};
        let formJson = data.formJson || {};

        // 兜底：确保 widgetList 和 formConfig 都存在
        if (!formJson.widgetList) formJson.widgetList = [];
        if (!formJson.formConfig) {
          formJson.formConfig = {
            modelName: 'formData', refName: 'vForm', rulesName: 'rules',
            labelWidth: 80, labelPosition: 'left', size: '',
            labelAlign: 'label-left-align', cssCode: '', customClass: [],
            functions: '', layoutType: 'PC', jsonVersion: 3
          };
        }

        // 处理自定义Vue组件节点
        const rawComponentNodes = Array.isArray(data._componentNodes) ? data._componentNodes : [];
        this.$set(this, 'componentNodes', rawComponentNodes);

        // 渲染自定义组件节点：nextTick 后逐一 setFormData + setReadonly
        if (rawComponentNodes.length > 0) {
          this.$nextTick(() => {
            rawComponentNodes.forEach((node, idx) => {
              const refKey = 'compNode_' + idx;
              const compRef = this.$refs[refKey];
              // $refs 可能是数组（v-for 内）
              const comp = Array.isArray(compRef) ? compRef[0] : compRef;
              if (comp) {
                if (typeof comp.setFormData === 'function') {
                  comp.setFormData(node.formData || {});
                }
                if (typeof comp.setReadonly === 'function') {
                  comp.setReadonly(true);
                }
              }
            });
          });
        }

        // vForm 节点处理
        if (!formJson.widgetList.length) {
          this.hasForm = false;
          return;
        }

        this.hasForm = true;

        // 聚合所有节点命名空间下的表单数据（{taskDefKey}__formData）
        // 并将字段值平铺，用于 setFormData 回填
        const mergedFormData = {};
        Object.keys(data).forEach(key => {
          if (key.endsWith('__formData') && data[key] && typeof data[key] === 'object') {
            Object.assign(mergedFormData, data[key]);
          }
        });
        // 兼容旧数据：顶层裸字段值也保留
        Object.assign(mergedFormData, data);

        this.$nextTick(() => {
          this.$refs.vFormRef.setFormJson(formJson);
          this.$nextTick(() => {
            this.$refs.vFormRef.setFormData(mergedFormData);
            this.$nextTick(() => {
              // 已发任务表单全部只读
              this.$refs.vFormRef.disableForm();
            });
          });
        });
      }).catch(() => {
        this.hasForm = false;
      });
    },
    /** 流程流转记录 */
    getFlowRecordList(procInsId, deployId) {
      const params = {procInsId: procInsId, deployId: deployId}
      flowRecord(params).then(res => {
        this.flowRecordList = res.data.flowList;
      }).catch(() => {
        this.goBack();
      })
    },
    /** 返回页面 */
    goBack() {
      // 关闭当前标签页并返回上个页面
      const obj = { path: "/task/process", query: { t: Date.now()} };
      this.$tab.closeOpenPage(obj);
    },
  }
};
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
