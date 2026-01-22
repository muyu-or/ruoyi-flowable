<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="物料ID" prop="materialId">
        <el-input
          v-model="queryParams.materialId"
          placeholder="请输入物料ID"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="出库类型" prop="outboundType">
        <el-select v-model="queryParams.outboundType" placeholder="请选择出库类型" clearable size="small">
          <el-option
            v-for="dict in dict.type.outbound_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="库区" prop="warehouseArea">
        <el-select v-model="queryParams.warehouseArea" placeholder="请选择库区" clearable size="small">
          <el-option
            v-for="dict in dict.type.warehouse_area"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="出库时间" prop="outboundTime">
        <el-date-picker
          clearable
          size="small"
          v-model="queryParams.outboundTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择出库时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="操作人员" prop="operator">
        <el-input
          v-model="queryParams.operator"
          placeholder="请输入操作人员"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['manage:stockout:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:stockout:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['manage:stockout:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockoutList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="出库记录ID" align="center" prop="id" />
      <el-table-column label="物料ID" align="center" prop="materialId" />
      <el-table-column label="物料名称" align="center" prop="materialName" />
      <el-table-column label="出库类型" align="center" prop="outboundType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.outbound_type" :value="scope.row.outboundType"/>
        </template>
      </el-table-column>
      <el-table-column label="出库数量" align="center" prop="quantity" />
      <el-table-column label="库区" align="center" prop="warehouseArea">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.warehouse_area" :value="scope.row.warehouseArea"/>
        </template>
      </el-table-column>
      <el-table-column label="操作人员" align="center" prop="operator" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:stockout:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['manage:stockout:remove']"
          >删除</el-button>
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
      <el-form ref="stockoutRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="物料ID" prop="materialId">
          <el-input v-model="form.materialId" placeholder="请输入物料ID" />
        </el-form-item>
        <el-form-item label="出库类型" prop="outboundType">
          <el-select v-model="form.outboundType" placeholder="请选择出库类型">
            <el-option
              v-for="dict in dict.type.outbound_type"
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
        <el-form-item label="出库时间" prop="outboundTime">
          <el-date-picker
            clearable
            v-model="form.outboundTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择出库时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="操作人员" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入操作人员" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStockout, getStockout, delStockout, addStockout, updateStockout } from "@/api/manage/stockout";

export default {
  name: "Stockout",
  dicts: ['warehouse_area', 'outbound_type'],
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
      // 出库记录表格数据
      stockoutList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        materialId: null,
        outboundType: null,
        warehouseArea: null,
        outboundTime: null,
        operator: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        materialId: [
          { required: true, message: "物料ID不能为空", trigger: "blur" }
        ],
        outboundType: [
          { required: true, message: "出库类型不能为空", trigger: "change" }
        ],
        warehouseArea: [
          { required: true, message: "库区不能为空", trigger: "change" }
        ],
        outboundTime: [
          { required: true, message: "出库时间不能为空", trigger: "blur" }
        ],
        operator: [
          { required: true, message: "操作人员不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询出库记录列表 */
    getList() {
      this.loading = true;
      listStockout(this.queryParams).then(response => {
        // 兼容 RuoYi-Vue 不同版本，通常是 response.rows
        this.stockoutList = response.rows || response.data;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        materialId: null,
        outboundType: null,
        quantity: null,
        warehouseArea: null,
        outboundTime: null,
        operator: null,
        createTime: null
      };
      this.resetForm("stockoutRef");
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加出库记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const _id = row.id || this.ids;
      getStockout(_id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改出库记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["stockoutRef"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStockout(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addStockout(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const _ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除出库记录编号为"' + _ids + '"的数据项？').then(() => {
        return delStockout(_ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('manage/stockout/export', {
        ...this.queryParams
      }, `stockout_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
