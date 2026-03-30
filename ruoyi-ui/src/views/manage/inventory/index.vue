<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" label-width="110px">
      <el-row :gutter="16" type="flex" align="middle">
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="物料ID" prop="materialId">
            <el-input v-model="queryParams.materialId" placeholder="请输入物料ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="物料名称" prop="materialName">
            <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="物料大类" prop="materialCategory">
            <el-select v-model="queryParams.materialCategory" placeholder="请选择物料大类" clearable style="width:100%">
              <el-option v-for="dict in dict.type.material_category" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16" type="flex" align="middle">
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="物料子类" prop="materialSubcategory">
            <el-select v-model="queryParams.materialSubcategory" placeholder="请选择物料子类" clearable style="width:100%">
              <el-option v-for="dict in dict.type.material_subcategory" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="库房区域" prop="warehouseArea">
            <el-select v-model="queryParams.warehouseArea" placeholder="请选择库区" clearable style="width:100%">
              <el-option v-for="dict in dict.type.warehouse_area" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="入库类型" prop="inboundType">
            <el-select v-model="queryParams.inboundType" placeholder="请选择入库类型" clearable style="width:100%">
              <el-option v-for="dict in dict.type.inbound_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16" type="flex" align="middle">
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="首次入库时间" prop="firstInboundTimeRange">
            <el-date-picker v-model="queryParams.firstInboundTimeRange" clearable type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="上次入库时间" prop="lastInboundTimeRange">
            <el-date-picker v-model="queryParams.lastInboundTimeRange" clearable type="daterange" value-format="yyyy-MM-dd" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8" style="text-align:right;">
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['manage:inventory:add']"
          type="primary"
          plain
          icon="el-icon-plus"
          @click="handleAdd"
        >新增入库</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['manage:inventory:remove']"
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          v-hasPermi="['manage:inventory:import']"
          type="info"
          plain
          icon="el-icon-upload2"
          @click="handleImport"
        >导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['manage:inventory:export']"
          type="warning"
          plain
          icon="el-icon-download"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['manage:inventory:edit']"
          type="success"
          plain
          icon="el-icon-price-tag"
          :disabled="multiple"
          @click="handleBatchSetCost"
        >设置单价</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="inventoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="物料ID" align="center" prop="materialId" />
      <el-table-column label="物料名称" align="center" prop="materialName" sortable />
      <el-table-column label="物料大类" align="center" prop="materialCategory" sortable>
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.material_category, scope.row.materialCategory) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物料子类" align="center" prop="materialSubcategory" sortable>
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.material_subcategory, scope.row.materialSubcategory) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前库存" align="center" prop="currentQuantity" sortable />
      <el-table-column label="成本单价" align="center" prop="unitCost" width="110">
        <template slot-scope="scope">
          <span>{{ scope.row.unitCost != null ? '¥' + Number(scope.row.unitCost).toFixed(2) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存总成本" align="center" prop="totalCost" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.totalCost != null ? '¥' + Number(scope.row.totalCost).toFixed(2) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库房区域" align="center" prop="warehouseArea" sortable>
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.warehouse_area, scope.row.warehouseArea) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入库类型" align="center" prop="inboundType" width="120" sortable>
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.inbound_type, scope.row.inboundType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存状态" align="center" prop="status" sortable>
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.inventory_status, scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="首次入库时间" align="center" prop="firstInboundTime" width="140" sortable>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.firstInboundTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上次入库时间" align="center" prop="lastInboundTime" width="140" sortable>
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastInboundTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="230">
        <template slot-scope="scope">
          <div class="action-btns">
            <el-button v-hasPermi="['manage:inventory:edit']" type="text" icon="el-icon-upload" @click="handleStockOut(scope.row)">出库</el-button>
            <el-button
              v-hasPermi="['manage:inventory:edit']"
              type="text"
              :icon="isPending(scope.row) ? 'el-icon-check' : 'el-icon-minus'"
              :style="isPending(scope.row) ? 'color:#67C23A' : 'color:#E6A23C'"
              @click="handleToggle(scope.row)"
            >{{ isPending(scope.row) ? '启用' : '禁用' }}</el-button>
            <el-button v-hasPermi="['manage:inventory:startFlow']" type="text" icon="el-icon-s-promotion" style="color:#409EFF" @click="handleStartFlow(scope.row)">发起流程</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <!-- 入库模式切换 -->
      <div v-if="!form.id" style="text-align: center; margin-bottom: 20px;">
        <el-radio-group v-model="inboundMode" size="small">
          <el-radio-button label="new">新建物料入库</el-radio-button>
          <el-radio-button label="scan">扫码/ID入库</el-radio-button>
        </el-radio-group>
      </div>

      <el-form ref="inventoryRef" :model="form" :rules="rules" label-width="80px">

        <!-- 扫码模式字段 -->
        <template v-if="inboundMode === 'scan' && !form.id">
          <el-form-item label="物料ID" prop="materialId" :rules="[{ required: true, message: '请扫描或输入物料ID', trigger: 'blur' }]">
            <el-input ref="scanInput" v-model="form.materialId" placeholder="请扫描或输入物料ID" autofocus @keyup.enter.native="handleScanEnter" />
          </el-form-item>
        </template>

        <!-- 新建物料模式字段 -->
        <template v-if="inboundMode === 'new' || form.id">
          <el-form-item label="物料名称" prop="materialName">
            <el-input v-model="form.materialName" placeholder="请输入物料名称" />
          </el-form-item>
          <el-form-item label="物料大类" prop="materialCategory">
            <el-select v-model="form.materialCategory" placeholder="请选择物料大类">
              <el-option
                v-for="dict in dict.type.material_category"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="物料子类" prop="materialSubcategory">
            <el-select v-model="form.materialSubcategory" placeholder="请选择物料子类">
              <el-option
                v-for="dict in dict.type.material_subcategory"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="库区" prop="warehouseArea">
            <el-select v-model="form.warehouseArea" placeholder="请选择库区">
              <el-option
                v-for="dict in dict.type.warehouse_area"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item label="入库数量" prop="currentQuantity">
          <el-input-number v-model="form.currentQuantity" :min="1" :step="1" controls-position="right" placeholder="请输入数量" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入库类型" prop="inboundType">
          <el-select v-model="form.inboundType" placeholder="请选择入库类型">
            <el-option
              v-for="dict in dict.type.inbound_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="成本单价">
          <el-input-number v-model="form.unitCost" :min="0" :precision="2" controls-position="right" placeholder="请输入成本单价（可选）" style="width:100%" />
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="出库" :visible.sync="stockOutOpen" width="520px" append-to-body>
      <el-form ref="stockOutRef" :model="stockOutForm" :rules="stockOutRules" label-width="100px">
        <el-form-item label="物料ID" prop="materialId">
          <el-input v-model="stockOutForm.materialId" disabled />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="stockOutForm.materialName" disabled />
        </el-form-item>
        <el-form-item label="物料大类">
          <el-input :value="displayLabel(dict.type.material_category, stockOutForm.materialCategory)" disabled />
        </el-form-item>
        <el-form-item label="物料子类">
          <el-input :value="displayLabel(dict.type.material_subcategory, stockOutForm.materialSubcategory)" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="stockOutForm.currentQuantity" disabled />
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="stockOutForm.quantity" :min="1" :max="stockOutForm.currentQuantity" controls-position="right" placeholder="请输入出库数量" style="width:100%" />
        </el-form-item>
        <el-form-item label="库区">
          <el-input :value="displayLabel(dict.type.warehouse_area, stockOutForm.warehouseArea)" disabled />
        </el-form-item>

        <el-form-item label="出库类型" prop="outboundType">
          <el-select v-model="stockOutForm.outboundType" placeholder="请选择出库类型" style="width:100%">
            <el-option v-for="d in dict.type.outbound_type" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitStockOut">确 定</el-button>
        <el-button @click="stockOutOpen=false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 库存导入对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div slot="tip" class="el-upload__tip">
          <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的库存数据
          <el-link type="info" style="font-size:12px" @click="importTemplate">下载模板</el-link>
        </div>
        <div slot="tip" class="el-upload__tip" style="color:red">提示：仅允许导入“xls”或“xlsx”格式文件！</div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 批量设置成本单价弹框 -->
    <el-dialog title="批量设置成本单价" :visible.sync="batchCostOpen" width="380px" append-to-body>
      <el-form ref="batchCostRef" :model="batchCostForm" label-width="90px">
        <el-form-item label="选中数量">
          <span style="color:#606266;">共 {{ ids.length }} 条记录</span>
        </el-form-item>
        <el-form-item label="成本单价" prop="unitCost" :rules="[{ required: true, message: '请输入成本单价', trigger: 'blur' }]">
          <el-input-number
            v-model="batchCostForm.unitCost"
            :min="0.01"
            :precision="2"
            controls-position="right"
            placeholder="请输入单价（元）"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBatchCost">确 定</el-button>
        <el-button @click="batchCostOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 发起流程：选择流程定义 -->
    <el-dialog title="选择流程" :visible.sync="flowDialogOpen" width="680px" append-to-body>
      <el-form :inline="true" :model="flowQueryParams" style="margin-bottom:8px;">
        <el-form-item label="流程名称">
          <el-input v-model="flowQueryParams.name" placeholder="请输入流程名称" clearable size="small" @keyup.enter.native="handleFlowQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleFlowQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="flowQueryParams.name='';handleFlowQuery()">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="flowLoading" :data="flowDefinitionList" border size="small">
        <el-table-column label="流程名称" align="center" prop="name" />
        <el-table-column label="版本" align="center" width="80">
          <template slot-scope="scope">
            <el-tag size="mini">v{{ scope.row.version }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流程分类" align="center" prop="category" />
        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-s-promotion" @click="handleSelectFlow(scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="flowTotal > 0"
        :total="flowTotal"
        :page.sync="flowQueryParams.pageNum"
        :limit.sync="flowQueryParams.pageSize"
        @pagination="loadFlowDefinitions"
      />
    </el-dialog>

    <!-- 批量发起流程弹窗 -->
    <el-dialog
      title="批量发起流程"
      :visible.sync="batchFlowOpen"
      width="820px"
      append-to-body
      :close-on-click-modal="false"
    >
      <div style="margin-bottom:12px;">
        <el-button type="primary" size="small" icon="el-icon-plus" :disabled="batchSubmitting" @click="handleAddGroup">添加分组</el-button>
        <span style="margin-left:12px;color:#909399;font-size:13px;">
          共发起 <b>{{ batchTotalCount }}</b> 个流程实例
        </span>
      </div>

      <div v-if="batchGroups.length === 0" style="text-align:center;padding:40px 0;color:#909399;">
        请点击"添加分组"创建分组
      </div>

      <el-card
        v-for="(group, gIdx) in batchGroups"
        :key="group.id"
        shadow="never"
        style="margin-bottom:16px;border:1px solid #DCDFE6;"
      >
        <div slot="header" style="display:flex;align-items:center;justify-content:space-between;">
          <span style="font-weight:600;">第 {{ gIdx + 1 }} 组</span>
          <el-button type="text" style="color:#F56C6C;" icon="el-icon-delete" :disabled="batchSubmitting" @click="handleRemoveGroup(gIdx)">删除本组</el-button>
        </div>

        <!-- 流程选择 -->
        <el-row :gutter="12" style="margin-bottom:12px;">
          <el-col :span="24">
            <span class="batch-label">流程：</span>
            <el-select
              v-model="group.procDef"
              value-key="id"
              placeholder="请选择流程"
              filterable
              remote
              :remote-method="(q) => handleGroupProcSearch(q, gIdx)"
              :loading="group.procDefLoading"
              style="width:320px;"
              @change="(val) => handleGroupProcChange(val, gIdx)"
            >
              <el-option
                v-for="p in group.procDefList"
                :key="p.id"
                :label="`${p.name}  v${p.version}`"
                :value="p"
              />
            </el-select>
          </el-col>
        </el-row>

        <!-- 主班组 -->
        <el-row :gutter="12" style="margin-bottom:12px;">
          <el-col :span="24">
            <span class="batch-label">主班组：</span>
            <el-select
              v-model="group.mainTeamId"
              placeholder="选择主班组，自动填入所有节点"
              clearable
              filterable
              style="width:320px;"
              @change="val => handleGroupMainTeamChange(val, gIdx)"
            >
              <el-option
                v-for="t in teamList"
                :key="t.id"
                :label="t.teamName"
                :value="t.id"
              />
            </el-select>
            <span style="margin-left:8px;font-size:12px;color:#909399;">（可选，选后自动填入所有节点班组）</span>
          </el-col>
        </el-row>

        <!-- 物料分配 -->
        <el-row style="margin-bottom:12px;">
          <el-col :span="24">
            <span class="batch-label">物料：</span>
            <el-checkbox-group v-model="group.materials">
              <el-checkbox
                v-for="row in batchAllMaterials"
                :key="row.id"
                :label="row"
                :disabled="isMaterialUsed(row.id, gIdx)"
              >
                {{ row.materialName || row.materialId }}
              </el-checkbox>
            </el-checkbox-group>
          </el-col>
        </el-row>

        <!-- 日期 + 备注 -->
        <el-row :gutter="12" style="margin-bottom:12px;">
          <el-col :span="14">
            <span class="batch-label">日期：</span>
            <el-date-picker
              v-model="group.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              size="small"
              style="width:280px;"
            />
          </el-col>
          <el-col :span="10">
            <span class="batch-label">备注：</span>
            <el-input v-model="group.remark" size="small" placeholder="选填" style="width:200px;" />
          </el-col>
        </el-row>

        <!-- 班组分配 -->
        <el-row v-if="group.processNodes.length > 0">
          <el-col :span="24">
            <span class="batch-label">班组分配：</span>
            <el-row :gutter="8" style="margin-top:6px;">
              <el-col
                v-for="(node, nIdx) in group.processNodes"
                :key="node.id"
                :span="8"
                style="margin-bottom:8px;"
              >
                <span style="font-size:12px;color:#606266;">{{ node.name }}：</span>
                <el-select
                  :value="group.nodeTeamMapArray[nIdx]"
                  placeholder="选择班组"
                  size="small"
                  style="width:140px;"
                  @change="val => $set(group.nodeTeamMapArray, nIdx, val)"
                >
                  <el-option
                    v-for="t in teamList"
                    :key="t.id"
                    :label="t.teamName"
                    :value="t.id"
                  />
                </el-select>
              </el-col>
            </el-row>
          </el-col>
        </el-row>
        <div v-if="group.nodesLoading" style="color:#909399;font-size:12px;">
          <i class="el-icon-loading" /> 加载节点中...
        </div>
      </el-card>

      <div slot="footer">
        <el-button @click="batchFlowOpen = false">取 消</el-button>
        <el-button
          type="primary"
          :loading="batchSubmitting"
          :disabled="batchGroups.length === 0"
          @click="handleBatchSubmit"
        >一键发起全部（{{ batchTotalCount }} 个）</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listInventory, delInventory, addInventory, updateInventory, stockOutInventory, scanInbound, batchSetUnitCost } from '@/api/manage/inventory'
import { listDefinition, flowXmlAndNode, startProcessWithTeam } from '@/api/flowable/definition'
import { listTeam } from '@/api/manage/team'
import { getToken } from '@/utils/auth'
// 移除：import { listPlan } from "@/api/manage/plan";

export default {
  name: 'Inventory',
  dicts: ['material_category', 'warehouse_area', 'material_subcategory', 'inventory_status', 'inbound_type', 'outbound_type'],
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
      // 保存选中的完整行数据（用于批量发起）
      selectedRows: [],
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 库存信息表格数据
      inventoryList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 入库模式: 'new' | 'scan'
      inboundMode: 'new',

      // 是否显示出库弹出层
      stockOutOpen: false,
      // 记录禁用前的原始状态
      lastStatusBeforeDisable: new Map(),

      // 发起流程弹窗
      flowDialogOpen: false,
      flowLoading: false,
      flowDefinitionList: [],
      flowTotal: 0,
      flowCurrentRow: null, // 触发发起流程的库存行
      // 批量设置单价弹窗
      batchCostOpen: false,
      batchCostForm: { unitCost: null },

      // 批量发起流程弹窗
      batchFlowOpen: false,
      batchGroups: [],
      batchSubmitting: false,
      teamList: [], // 班组列表（批量发起弹窗用）
      flowQueryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null
      },

      // 用户导入参数
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 弹出层标题（用户导入）
        title: '',
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: 'Bearer ' + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + '/manage/inventory/importData'
      },

      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        materialId: null,
        materialName: null,
        materialCategory: null,
        materialSubcategory: null,
        warehouseArea: null,
        status: null,
        inboundType: null,
        firstInboundTimeRange: null,
        lastInboundTimeRange: null
      },

      // 表单参数
      form: {},
      // 表单校验
      rules: {
        materialName: [
          { required: true, message: '物料名称不能为空', trigger: 'blur' }
        ],
        materialCategory: [
          { required: true, message: '物料大类不能为空', trigger: 'change' }
        ],
        materialSubcategory: [
          { required: true, message: '物料子类不能为空', trigger: 'change' }
        ],
        warehouseArea: [
          { required: true, message: '库区不能为空', trigger: 'change' }
        ],
        currentQuantity: [
          { required: true, message: '库存数量不能为空', trigger: 'change' },
          { validator: this.validatePositiveQuantity, trigger: 'change' }
        ],
        inboundType: [
          { required: true, message: '入库类型不能为空', trigger: 'change' }
        ],
        status: [
          { required: true, message: '库存状态不能为空', trigger: 'change' }
        ]
      },

      // 出库表单（已移除 planId）
      stockOutForm: {
        id: null,
        materialId: '',
        materialName: '',
        materialCategory: null,
        materialSubcategory: null,
        currentQuantity: 0,
        quantity: 1,
        warehouseArea: null,
        outboundType: null
      },
      // 出库校验（已移除 planId）
      stockOutRules: {
        quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }],
        outboundType: [{ required: true, message: '请选择出库类型', trigger: 'change' }]
      }
    }
  },
  computed: {
    batchAllMaterials() {
      return this.selectedRows || []
    },
    batchTotalCount() {
      return this.batchGroups.reduce((sum, g) => sum + g.materials.length, 0)
    }
  },
  created() {
    this.getList()
  },
  methods: {
    // 校验：正数数量
    validatePositiveQuantity(rule, value, callback) {
      const n = Number(value)
      if (Number.isNaN(n) || n <= 0) {
        callback(new Error('库存数量必须为大于 0 的数字'))
      } else {
        callback()
      }
    },

    // 字典显示：优先按字典 value 匹配到 label
    displayLabel(dictOptions, rawValue) {
      if (Array.isArray(dictOptions)) {
        const hit = dictOptions.find(d => d && (d.value === rawValue || String(d.value) === String(rawValue)))
        if (hit && hit.label !== undefined && hit.label !== null && hit.label !== '') {
          return hit.label
        }
      }
      if (rawValue !== undefined && rawValue !== null && String(rawValue) !== '') {
        return rawValue
      }
      return '-'
    },

    // 将字典的 value 映射为其 label
    mapValueToLabel(dictOptions, rawValue) {
      if (!Array.isArray(dictOptions)) return rawValue
      const hit = dictOptions.find(d => d && (d.value === rawValue || String(d.value) === String(rawValue)))
      return hit && hit.label !== undefined ? hit.label : rawValue
    },

    // 辅助查找
    findDictValueByLabel(dictList, label) {
      if (!Array.isArray(dictList)) return undefined
      const hit = dictList.find(d => d && d.label === label)
      return hit ? hit.value : undefined
    },

    findDictLabelByValue(dictList, value) {
      if (!Array.isArray(dictList)) return undefined
      const hit = dictList.find(d => d && String(d.value) === String(value))
      return hit ? hit.label : undefined
    },

    // 判断状态
    isPending(row) {
      const pending = this.findDictValueByLabel(this.dict.type.inventory_status, '待调整') ?? '3'
      return String(row.status) === String(pending)
    },

    // 构建查询参数
    buildQueryPayload() {
      const source = this.queryParams || {}
      const payload = {}
      const rangeMap = {
        firstInboundTimeRange: ['beginFirstInboundTime', 'endFirstInboundTime'],
        lastInboundTimeRange: ['beginLastInboundTime', 'endLastInboundTime']
      }
      Object.keys(source).forEach(key => {
        const value = source[key]
        if (value === null || value === undefined) return
        if (typeof value === 'string' && value.trim() === '') return
        if (Array.isArray(value) && value.length === 0) return

        // 日期范围字段 → 拆为 params[beginXxx] / params[endXxx]
        if (rangeMap[key] && Array.isArray(value) && value.length === 2) {
          if (!payload['params']) payload['params'] = {}
          payload['params'][rangeMap[key][0]] = value[0]
          payload['params'][rangeMap[key][1]] = value[1]
          return
        }

        const numericKeys = ['pageNum', 'pageSize']
        if (numericKeys.includes(key) && typeof value === 'string' && /^-?\d+(?:\.\d+)?$/.test(value)) {
          payload[key] = Number(value)
        } else {
          payload[key] = value
        }
      })
      return payload
    },

    /** 查询库存信息列表 */
    getList() {
      this.loading = true
      const payload = this.buildQueryPayload()
      listInventory(payload).then(response => {
        const rows =
          response.rows ||
          (response.data && response.data.rows) ||
          (response.data && response.data.records) ||
          (response.data && response.data.list) ||
          response.records ||
          response.list ||
          (Array.isArray(response.data) ? response.data : [])
        const t =
          response.total ||
          (response.data && response.data.total) ||
          (response.data && response.data.count) ||
          (Array.isArray(rows) ? rows.length : 0)

        this.inventoryList = Array.isArray(rows) ? rows : []
        this.total = Number(t) || 0
        this.loading = false
      })
    },

    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },

    /** 表单重置 */
    reset() {
      this.form = {
        id: null,
        materialId: null,
        materialName: null,
        materialCategory: null,
        materialSubcategory: null,
        currentQuantity: 1,
        unitCost: null,
        warehouseArea: null,
        inboundType: null,
        status: '1',
        firstInboundTime: null,
        lastInboundTime: null,
        lastOutboundTime: null,
        createTime: null,
        updateTime: null
      }
      this.resetForm('inventoryRef')
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryRef')
      this.handleQuery()
    },

    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.selectedRows = selection
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.inboundMode = 'new' // 默认新建模式
      this.open = true
      this.title = '新增入库'
    },

    /** 扫码回车事件 */
    handleScanEnter() {
      // 可以在这里增加自动查询物料信息显示的逻辑，目前直接聚焦确认
      // this.$refs['inventoryRef'].validateField('materialId');
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs['inventoryRef'].validate(valid => {
        if (valid) {
          const payload = { ...this.form, quantity: this.form.currentQuantity }

          if (this.form.id != null) {
            // 编辑模式
            updateInventory(payload).then(response => {
              this.$modal.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            // 新增模式
            if (this.inboundMode === 'scan') {
              // 扫码入库
              scanInbound(payload).then(response => {
                this.$modal.msgSuccess('扫码入库成功')
                this.open = false
                this.getList()
              })
            } else {
              // 新建物料入库
              addInventory(payload).then(response => {
                this.$modal.msgSuccess('新增成功')
                this.open = false
                this.getList()
              })
            }
          }
        }
      })
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const _ids = row.id || this.ids
      this.$modal.confirm('是否确认删除库存信息编号为"' + _ids + '"的数据项？').then(() => {
        return delInventory(_ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },

    /** 切换 启用/禁用：待调整 <-> 在库 */
    handleToggle(row) {
      if (!row) return
      const pending = this.findDictValueByLabel(this.dict.type.inventory_status, '待调整') ?? '3'
      const activeFallback = this.findDictValueByLabel(this.dict.type.inventory_status, '在库') ?? '1'

      if (this.isPending(row)) {
        // 启用：直接恢复，不需要二次确认
        const restore = this.lastStatusBeforeDisable.get(row.id) || activeFallback
        updateInventory({ id: row.id, status: restore }).then(() => {
          const label = this.findDictLabelByValue(this.dict.type.inventory_status, restore) || '已启用'
          this.$modal.msgSuccess(`已启用，状态为${label}`)
          this.lastStatusBeforeDisable.delete(row.id)
          this.getList()
        })
      } else {
        // 禁用：弹确认对话框
        this.$confirm(
          `确认将物料【${row.materialName || row.materialId}】设为禁用（待调整）状态吗？`,
          '禁用确认',
          {
            confirmButtonText: '确认禁用',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          this.lastStatusBeforeDisable.set(row.id, row.status)
          updateInventory({ id: row.id, status: pending }).then(() => {
            this.$modal.msgSuccess('已禁用，状态为待调整')
            this.getList()
          })
        }).catch(() => {})
      }
    },

    /** 出库操作 */
    handleStockOut(row) {
      if (!row) return
      const inStock = this.findDictValueByLabel(this.dict.type.inventory_status, '在库') ?? '1'
      if (String(row.status) !== String(inStock)) {
        this.$modal.msgWarning('仅“在库”状态可出库')
        return
      }

      // 重置并回显
      this.stockOutForm = {
        id: row.id,
        materialId: row.materialId,
        materialName: row.materialName,
        materialCategory: row.materialCategory,
        materialSubcategory: row.materialSubcategory,
        currentQuantity: row.currentQuantity || row.quantity || 0,
        quantity: 1,
        warehouseArea: row.warehouseArea,
        outboundType: null
      }

      this.stockOutOpen = true
      this.$nextTick(() => {
        this.$refs.stockOutRef && this.$refs.stockOutRef.clearValidate()
      })
    },

    /** 提交出库 */
    submitStockOut() {
      this.$refs['stockOutRef'].validate(valid => {
        if (!valid) return
        const outStock = this.findDictValueByLabel(this.dict.type.inventory_status, '已出库') ?? '2'

        const payload = {
          id: this.stockOutForm.id,
          status: outStock,
          outboundType: this.stockOutForm.outboundType,
          materialId: this.stockOutForm.materialId,
          materialName: this.stockOutForm.materialName,
          materialCategory: this.stockOutForm.materialCategory,
          materialSubcategory: this.stockOutForm.materialSubcategory,
          quantity: this.stockOutForm.quantity,
          warehouseArea: this.stockOutForm.warehouseArea
        }

        stockOutInventory(payload).then(response => {
          const message = response.msg || response.message || '出库成功'
          this.$modal.msgSuccess(message)
          this.stockOutOpen = false
          this.getList()
        }).catch(err => { console.error('submitStockOut error', err) })
      })
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('manage/inventory/export', {
        ...this.queryParams
      }, `inventory_${new Date().getTime()}.xlsx`)
    },

    /** 批量设置成本单价 */
    handleBatchSetCost() {
      this.batchCostForm.unitCost = null
      this.batchCostOpen = true
    },
    submitBatchCost() {
      this.$refs.batchCostRef.validate(valid => {
        if (!valid) return
        batchSetUnitCost({ ids: this.ids, unitCost: this.batchCostForm.unitCost }).then(() => {
          this.$modal.msgSuccess('成本单价设置成功')
          this.batchCostOpen = false
          this.getList()
        })
      })
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = '库存导入'
      this.upload.open = true
    },
    /** 下载模板操作 */
    importTemplate() {
      this.download('manage/inventory/importTemplate', {
      }, `inventory_template_${new Date().getTime()}.xlsx`)
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false
      this.upload.isUploading = false
      this.$refs.upload.clearFiles()
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + '</div>', '导入结果', { dangerouslyUseHTMLString: true })
      this.getList()
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit()
    },

    /** 点击"发起流程"按钮 */
    handleStartFlow(row) {
      if (!row) return
      const inStock = this.findDictValueByLabel(this.dict.type.inventory_status, '在库') ?? '1'
      if (String(row.status) !== String(inStock)) {
        this.$modal.msgWarning('仅"在库"状态可发起流程')
        return
      }
      this.flowCurrentRow = row
      this.flowQueryParams.pageNum = 1
      this.flowQueryParams.name = null
      this.flowDialogOpen = true
      this.loadFlowDefinitions()
    },

    /** 搜索流程 */
    handleFlowQuery() {
      this.flowQueryParams.pageNum = 1
      this.loadFlowDefinitions()
    },

    /** 加载流程定义列表 */
    loadFlowDefinitions() {
      this.flowLoading = true
      listDefinition(this.flowQueryParams).then(res => {
        this.flowDefinitionList = (res.data && res.data.records) || []
        this.flowTotal = (res.data && res.data.total) || 0
        this.flowLoading = false
      }).catch(() => {
        this.flowLoading = false
      })
    },

    /** 选中流程，跳转发起页并携带物料信息 */
    handleSelectFlow(procRow) {
      this.flowDialogOpen = false
      const inv = this.flowCurrentRow || {}
      this.$router.push({
        path: '/flowable/task/myProcess/send/index',
        query: {
          deployId: procRow.deploymentId,
          procDefId: procRow.id,
          procName: procRow.name,
          version: procRow.version,
          from: '/manage/inventory', // 标记来源，返回时跳回库存页
          materialId: inv.materialId || '',
          materialName: inv.materialName || '',
          inventoryId: inv.id || ''
        }
      })
    },

    /** 打开批量发起弹窗 */
    handleBatchStartFlow() {
      if (!this.ids.length) {
        this.$modal.msgWarning('请先勾选物料')
        return
      }
      this.batchGroups = []
      this.batchFlowOpen = true
      // 加载班组列表（若已有则跳过）
      if (!this.teamList || !this.teamList.length) {
        listTeam({ pageNum: 1, pageSize: 100, teamStatus: '1' }).then(res => {
          this.teamList = res.rows || res.data || []
        })
      }
      this.handleAddGroup()
    },

    /** 添加分组 */
    handleAddGroup() {
      const group = {
        id: Date.now(),
        procDef: null,
        mainTeamId: null,
        materials: [],
        dateRange: [],
        remark: '',
        processNodes: [],
        nodeTeamMapArray: [],
        nodesLoading: false,
        procDefList: [],
        procDefTotal: 0,
        procDefLoading: false,
        procDefPageNum: 1
      }
      const usedIds = this.batchGroups.flatMap(g => g.materials.map(m => m.id))
      group.materials = this.batchAllMaterials.filter(m => !usedIds.includes(m.id))
      this.loadGroupProcDefs(group, '')
      this.batchGroups.push(group)
    },

    /** 删除分组 */
    handleRemoveGroup(gIdx) {
      this.batchGroups.splice(gIdx, 1)
    },

    /** 判断某物料是否已被其他组使用 */
    isMaterialUsed(materialId, currentGroupIdx) {
      return this.batchGroups.some((g, idx) => {
        if (idx === currentGroupIdx) return false
        return g.materials.some(m => m.id === materialId)
      })
    },

    /** 分组内流程搜索 */
    handleGroupProcSearch(query, gIdx) {
      const group = this.batchGroups[gIdx]
      if (!group) return
      group.procDefPageNum = 1
      this.loadGroupProcDefs(group, query)
    },

    /** 加载某分组的流程定义列表 */
    loadGroupProcDefs(group, name) {
      group.procDefLoading = true
      listDefinition({ pageNum: group.procDefPageNum, pageSize: 20, name: name || null }).then(res => {
        group.procDefList = (res.data && res.data.records) || []
        group.procDefTotal = (res.data && res.data.total) || 0
        group.procDefLoading = false
      }).catch(() => {
        group.procDefLoading = false
      })
    },

    /** 选中流程后加载节点 */
    handleGroupProcChange(procDef, gIdx) {
      const group = this.batchGroups[gIdx]
      if (!group || !procDef) return
      group.processNodes = []
      group.nodeTeamMapArray = []
      group.nodesLoading = true
      flowXmlAndNode({ deployId: procDef.deploymentId }).then(res => {
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
              if (id) nodes.push({ id, name })
            }
          } catch (e) {
            console.error('解析节点失败', e)
          }
        }
        this.$set(group, 'processNodes', nodes)
        this.$set(group, 'nodeTeamMapArray', new Array(nodes.length).fill(null))
        // 若已设置主班组，自动填入新加载的节点
        if (group.mainTeamId) {
          this.$set(group, 'nodeTeamMapArray', new Array(nodes.length).fill(group.mainTeamId))
        }
        group.nodesLoading = false
      }).catch(() => {
        group.nodesLoading = false
      })
    },

    /** 主班组变化 → 自动填入该组所有节点班组 */
    handleGroupMainTeamChange(val, gIdx) {
      const group = this.batchGroups[gIdx]
      if (!group) return
      if (val) {
        this.$set(group, 'nodeTeamMapArray', new Array(group.processNodes.length).fill(val))
      }
    },

    /** 一键发起全部 */
    async handleBatchSubmit() {
      for (let i = 0; i < this.batchGroups.length; i++) {
        const g = this.batchGroups[i]
        if (!g.procDef) {
          this.$modal.msgWarning(`第 ${i + 1} 组未选择流程`)
          return
        }
        if (!g.materials.length) {
          this.$modal.msgWarning(`第 ${i + 1} 组未选择物料`)
          return
        }
      }

      this.batchSubmitting = true
      let successCount = 0
      const failList = []

      for (const group of this.batchGroups) {
        const nodeTeamMap = {}
        group.processNodes.forEach((node, idx) => {
          if (group.nodeTeamMapArray[idx]) {
            nodeTeamMap[node.id] = group.nodeTeamMapArray[idx]
          }
        })

        for (let mIdx = 0; mIdx < group.materials.length; mIdx++) {
          const material = group.materials[mIdx]
          const now = new Date()
          const businessKey = `BAT${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(now.getTime()).slice(-6)}${String(mIdx).padStart(2, '0')}`
          const variables = {
            taskName: `${material.materialName || material.materialId} - ${group.procDef.name}`,
            procName: group.procDef.name,
            procVersion: group.procDef.version,
            operator: this.$store.state.user.name || '',
            materialId: material.materialId || '',
            materialName: material.materialName || '',
            inventoryId: material.id || '',
            remark: group.remark || ''
          }
          if (group.dateRange && group.dateRange.length === 2) {
            variables.procDateStart = group.dateRange[0]
            variables.procDateEnd = group.dateRange[1]
          }

          try {
            await startProcessWithTeam({
              procDefId: group.procDef.id,
              businessKey,
              mainTeamId: group.mainTeamId || null,
              nodeTeamMap,
              variables
            })
            successCount++
          } catch (e) {
            failList.push(`${material.materialName || material.materialId}（${group.procDef.name}）`)
          }
        }
      }

      this.batchSubmitting = false
      this.batchFlowOpen = false

      if (failList.length === 0) {
        this.$modal.msgSuccess(`成功发起 ${successCount} 个流程`)
      } else {
        this.$alert(
          `成功 ${successCount} 个，失败 ${failList.length} 个：<br>${failList.join('<br>')}`,
          '发起结果',
          { dangerouslyUseHTMLString: true, type: 'warning' }
        )
      }
      this.getList()
    }
  }
}
</script>

<style scoped>
.action-btns { display: inline-flex; align-items: center; gap: 8px; white-space: nowrap; }
.mb8 { margin-bottom: 8px; }
.batch-label { font-size: 13px; color: #606266; margin-right: 6px; }
</style>
