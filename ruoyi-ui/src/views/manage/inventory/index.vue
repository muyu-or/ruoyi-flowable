<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" label-width="110px">
      <el-row :gutter="16" type="flex" align="middle">
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="物料唯一标识码" prop="materialId">
            <el-input v-model="queryParams.materialId" placeholder="请输入物料唯一标识码" clearable @keyup.enter.native="handleQuery" />
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
          <el-form-item label="首次入库时间" prop="firstInboundTime">
            <el-date-picker clearable v-model="queryParams.firstInboundTime" type="date" value-format="yyyy-MM-dd" placeholder="请选择首次入库时间" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="上次入库时间" prop="lastInboundTime">
            <el-date-picker clearable v-model="queryParams.lastInboundTime" type="date" value-format="yyyy-MM-dd" placeholder="请选择上次入库时间" style="width:100%" />
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
          type="primary"
          plain
          icon="el-icon-plus"
          @click="handleAdd"
          v-hasPermi="['manage:inventory:add']"
        >新增入库</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:inventory:remove']"
        >删除</el-button>
      </el-col>

      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-upload2"
          @click="handleImport"
          v-hasPermi="['manage:inventory:import']"
        >导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['manage:inventory:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="inventoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="物料唯一标识码" align="center" prop="materialId" />
      <el-table-column label="物料名称" align="center" prop="materialName" />
      <el-table-column label="物料大类" align="center" prop="materialCategory">
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.material_category, scope.row.materialCategory) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物料子类" align="center" prop="materialSubcategory">
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.material_subcategory, scope.row.materialSubcategory) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="当前库存" align="center" prop="currentQuantity" />
      <el-table-column label="库房区域" align="center" prop="warehouseArea">
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.warehouse_area, scope.row.warehouseArea) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入库类型" align="center" prop="inboundType" width="120">
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.inbound_type, scope.row.inboundType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ displayLabel(dict.type.inventory_status, scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="首次入库时间" align="center" prop="firstInboundTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.firstInboundTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上次入库时间" align="center" prop="lastInboundTime" width="140">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastInboundTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <div class="action-btns">
            <el-button type="text" icon="el-icon-upload" @click="handleStockOut(scope.row)" v-hasPermi="['manage:inventory:edit']">出库</el-button>
            <el-button
              type="text"
              :icon="isPending(scope.row) ? 'el-icon-check' : 'el-icon-close'"
              @click="handleToggle(scope.row)"
              v-hasPermi="['manage:inventory:edit']"
            >{{ isPending(scope.row) ? '启用' : '禁用' }}</el-button>
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
             <el-input v-model="form.materialId" placeholder="请扫描或输入物料ID" ref="scanInput" autofocus @keyup.enter.native="handleScanEnter" />
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
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="物料子类" prop="materialSubcategory">
            <el-select v-model="form.materialSubcategory" placeholder="请选择物料子类">
              <el-option
                v-for="dict in dict.type.material_subcategory"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="库区" prop="warehouseArea">
            <el-select v-model="form.warehouseArea" placeholder="请选择库区">
              <el-option
                v-for="dict in dict.type.warehouse_area"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
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
            ></el-option>
          </el-select>
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
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">
          <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的库存数据
          <el-link type="info" style="font-size:12px" @click="importTemplate">下载模板</el-link>
        </div>
        <div class="el-upload__tip" style="color:red" slot="tip">提示：仅允许导入“xls”或“xlsx”格式文件！</div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listInventory, getInventory, delInventory, addInventory, updateInventory, stockOutInventory, scanInbound } from "@/api/manage/inventory";
import { getToken } from "@/utils/auth";
// 移除：import { listPlan } from "@/api/manage/plan";

export default {
  name: "Inventory",
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
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 库存信息表格数据
      inventoryList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 入库模式: 'new' | 'scan'
      inboundMode: 'new',

      // 是否显示出库弹出层
      stockOutOpen: false,
      // 记录禁用前的原始状态
      lastStatusBeforeDisable: new Map(),

      // 用户导入参数
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 弹出层标题（用户导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/manage/inventory/importData"
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
        firstInboundTime: null,
        lastInboundTime: null,
      },

      // 表单参数
      form: {},
      // 表单校验
      rules: {
        materialName: [
          { required: true, message: "物料名称不能为空", trigger: "blur" }
        ],
        materialCategory: [
          { required: true, message: "物料大类不能为空", trigger: "change" }
        ],
        materialSubcategory: [
          { required: true, message: "物料子类不能为空", trigger: "change" }
        ],
        warehouseArea: [
          { required: true, message: "库区不能为空", trigger: "change" }
        ],
        currentQuantity: [
          { required: true, message: "库存数量不能为空", trigger: "change" },
          { validator: this.validatePositiveQuantity, trigger: "change" }
        ],
        inboundType: [
          { required: true, message: "入库类型不能为空", trigger: "change" }
        ],
        status: [
          { required: true, message: "库存状态不能为空", trigger: "change" }
        ],
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
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 校验：正数数量
    validatePositiveQuantity(rule, value, callback) {
      const n = Number(value);
      if (Number.isNaN(n) || n <= 0) {
        callback(new Error('库存数量必须为大于 0 的数字'));
      } else {
        callback();
      }
    },

    // 字典显示：优先按字典 value 匹配到 label
    displayLabel(dictOptions, rawValue) {
      if (Array.isArray(dictOptions)) {
        const hit = dictOptions.find(d => d && (d.value === rawValue || String(d.value) === String(rawValue)));
        if (hit && hit.label !== undefined && hit.label !== null && hit.label !== '') {
          return hit.label;
        }
      }
      if (rawValue !== undefined && rawValue !== null && String(rawValue) !== '') {
        return rawValue;
      }
      return '-';
    },

    // 将字典的 value 映射为其 label
    mapValueToLabel(dictOptions, rawValue) {
      if (!Array.isArray(dictOptions)) return rawValue;
      const hit = dictOptions.find(d => d && (d.value === rawValue || String(d.value) === String(rawValue)));
      return hit && hit.label !== undefined ? hit.label : rawValue;
    },

    // 辅助查找
    findDictValueByLabel(dictList, label) {
      if (!Array.isArray(dictList)) return undefined;
      const hit = dictList.find(d => d && d.label === label);
      return hit ? hit.value : undefined;
    },

    findDictLabelByValue(dictList, value) {
      if (!Array.isArray(dictList)) return undefined;
      const hit = dictList.find(d => d && String(d.value) === String(value));
      return hit ? hit.label : undefined;
    },

    // 判断状态
    isPending(row) {
      const pending = this.findDictValueByLabel(this.dict.type.inventory_status, '待调整') ?? '3';
      return String(row.status) === String(pending);
    },

    // 构建查询参数
    buildQueryPayload() {
      const source = this.queryParams || {};
      const payload = {};
      Object.keys(source).forEach(key => {
        const value = source[key];
        if (value === null || value === undefined) return;
        if (typeof value === 'string' && value.trim() === '') return;
        if (Array.isArray(value) && value.length === 0) return;

        const numericKeys = ['pageNum', 'pageSize'];
        if (numericKeys.includes(key) && typeof value === 'string' && /^-?\d+(?:\.\d+)?$/.test(value)) {
          payload[key] = Number(value);
        } else {
          payload[key] = value;
        }
      });
      return payload;
    },

    /** 查询库存信息列表 */
    getList() {
      this.loading = true;
      const payload = this.buildQueryPayload();
      listInventory(payload).then(response => {
        const rows =
          response.rows ||
          (response.data && response.data.rows) ||
          (response.data && response.data.records) ||
          (response.data && response.data.list) ||
          response.records ||
          response.list ||
          (Array.isArray(response.data) ? response.data : []);
        const t =
          response.total ||
          (response.data && response.data.total) ||
          (response.data && response.data.count) ||
          (Array.isArray(rows) ? rows.length : 0);

        this.inventoryList = Array.isArray(rows) ? rows : [];
        this.total = Number(t) || 0;
        this.loading = false;
      });
    },

    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
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
        warehouseArea: null,
        inboundType: null,
        status: '1',
        firstInboundTime: null,
        lastInboundTime: null,
        lastOutboundTime: null,
        createTime: null,
        updateTime: null
      };
      this.resetForm("inventoryRef");
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryRef");
      this.handleQuery();
    },

    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.inboundMode = 'new'; // 默认新建模式
      this.open = true;
      this.title = "新增入库";
    },

    /** 扫码回车事件 */
    handleScanEnter() {
       // 可以在这里增加自动查询物料信息显示的逻辑，目前直接聚焦确认
       // this.$refs['inventoryRef'].validateField('materialId');
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["inventoryRef"].validate(valid => {
        if (valid) {
          const payload = { ...this.form, quantity: this.form.currentQuantity };
          
          if (this.form.id != null) {
            // 编辑模式
            updateInventory(payload).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            // 新增模式
            if (this.inboundMode === 'scan') {
              // 扫码入库
              scanInbound(payload).then(response => {
                this.$modal.msgSuccess("扫码入库成功");
                this.open = false;
                this.getList();
              });
            } else {
              // 新建物料入库
              addInventory(payload).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              });
            }
          }
        }
      });
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const _ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除库存信息编号为"' + _ids + '"的数据项？').then(() => {
        return delInventory(_ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    /** 切换 启用/禁用：待调整 <-> 在库 */
    handleToggle(row) {
      if (!row) return;
      const pending = this.findDictValueByLabel(this.dict.type.inventory_status, '待调整') ?? '3';
      const activeFallback = this.findDictValueByLabel(this.dict.type.inventory_status, '在库') ?? '1';

      if (this.isPending(row)) {
        // 恢复
        const restore = this.lastStatusBeforeDisable.get(row.id) || activeFallback;
        updateInventory({ id: row.id, status: restore }).then(() => {
          const label = this.findDictLabelByValue(this.dict.type.inventory_status, restore) || '已启用';
          this.$modal.msgSuccess(`已启用，状态为${label}`);
          this.lastStatusBeforeDisable.delete(row.id);
          this.getList();
        });
      } else {
        // 禁用
        this.lastStatusBeforeDisable.set(row.id, row.status);
        updateInventory({ id: row.id, status: pending }).then(() => {
          this.$modal.msgSuccess('已禁用，状态为待调整');
          this.getList();
        });
      }
    },

    /** 出库操作 */
    handleStockOut(row) {
      if (!row) return;
      const inStock = this.findDictValueByLabel(this.dict.type.inventory_status, '在库') ?? '1';
      if (String(row.status) !== String(inStock)) {
        this.$modal.msgWarning('仅“在库”状态可出库');
        return;
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
      };

      this.stockOutOpen = true;
      this.$nextTick(() => {
        this.$refs.stockOutRef && this.$refs.stockOutRef.clearValidate();
      });
    },

    /** 提交出库 */
    submitStockOut() {
      this.$refs['stockOutRef'].validate(valid => {
        if (!valid) return;
        const outStock = this.findDictValueByLabel(this.dict.type.inventory_status, '已出库') ?? '2';

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
        };

        stockOutInventory(payload).then(response => {
          const message = response.msg || response.message || '出库成功';
          this.$modal.msgSuccess(message);
          this.stockOutOpen = false;
          this.getList();
        }).catch(err => { console.error('submitStockOut error', err); });
      });
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('manage/inventory/export', {
        ...this.queryParams
      }, `inventory_${new Date().getTime()}.xlsx`)
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "库存导入";
      this.upload.open = true;
    },
    /** 下载模板操作 */
    importTemplate() {
      this.download('manage/inventory/importTemplate', {
      }, `inventory_template_${new Date().getTime()}.xlsx`)
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
      this.getList();
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit();
    }
  }
};
</script>

<style scoped>
.action-btns { display: inline-flex; align-items: center; gap: 8px; white-space: nowrap; }
.mb8 { margin-bottom: 8px; }
</style>
