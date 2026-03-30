<template>
  <div class="app-container">
    <el-card class="box-card" >
      <div slot="header" class="clearfix">
        <span class="el-icon-document">已办任务</span>
        <el-button style="float: right;" size="mini" type="danger" @click="goBack">关闭</el-button>
      </div>
      <el-tabs  tab-position="top" v-model="activeName" @tab-click="handleClick">
        <!--表单信息-->
        <el-tab-pane label="表单信息" name="1">
          <el-col :span="16" :offset="4">
            <!-- 无表单时提示 -->
            <el-empty v-if="!hasForm" description="该任务无绑定表单" :image-size="80" style="padding: 40px 0;"/>
            <v-form-render v-show="hasForm" ref="vFormRef"/>
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
                  :icon="setIcon(item)"
                  :color="setColor(item)"
                >
                  <p style="font-weight: 700">
                    {{item.taskName}}
                    <el-tag v-if="item.comment && item.comment.type === '2'" type="warning" size="mini" style="margin-left:6px">退回重审</el-tag>
                    <el-tag v-else-if="item.comment && item.comment.type === '3'" type="danger" size="mini" style="margin-left:6px">不通过</el-tag>
                  </p>
                  <el-card :body-style="{ padding: '10px' }">
                    <el-descriptions class="margin-top" :column="1" size="small" border>
                      <el-descriptions-item v-if="item.assigneeName" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user"></i>办理人</template>
                        {{item.assigneeName}}
                        <el-tag type="info" size="mini">{{item.deptName}}</el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.handlers" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user"></i>处理人员</template>
                        {{item.handlers}}
                      </el-descriptions-item>
                      <el-descriptions-item v-else-if="item.candidate" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-user"></i>班组成员</template>
                        {{item.candidate}}
                      </el-descriptions-item>
                      <el-descriptions-item v-if="item.planStartDate || item.planEndDate" label-class-name="my-label">
                        <template slot="label"><i class="el-icon-date"></i>计划时间</template>
                        {{ item.planStartDate || '?' }} ~ {{ item.planEndDate || '?' }}
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
                        <span :style="{ color: getCommentColor(item.comment.type) }">{{item.comment.comment}}</span>
                      </el-descriptions-item>
                    </el-descriptions>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-col>
        </el-tab-pane>
        <el-tab-pane label="流程图" name="3">
          <Bpmn-viewer :flowData="flowData" :procInsId="taskForm.procInsId"/>
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
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "FinishedDetail",
  components: {
    BpmnViewer,
  },
  props: {},
  data() {
    return {
      flowData: {},
      activeName: '1',
      loading: true,
      hasForm: false,
      flowRecordList: [],
      taskForm:{
        comment:"",
        procInsId: "",
        instanceId: "",
        deployId: "",
        taskId: "",
        procDefId: "",
        vars: "",
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
      if (tab.name === '3') {
        flowXmlAndNode({procInsId: this.taskForm.procInsId, deployId: this.taskForm.deployId}).then(res => {
          this.flowData = res.data;
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
    /** 加载任务表单（复用 flowTaskForm 接口，该接口已支持命名空间数据回填） */
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

        // 无字段时不显示表单
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
              // 已办任务表单全部只读
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
      const obj = { path: "/task/finished", query: { t: Date.now()} };
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
