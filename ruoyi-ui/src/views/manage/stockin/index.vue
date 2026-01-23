<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" label-width="80px">
      <el-row :gutter="16" type="flex" align="middle">
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="物料ID" prop="materialId">
            <el-input
              v-model="queryParams.materialId"
              placeholder="请输入物料ID"
              clearable
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="入库类型" prop="inboundType">
            <el-select v-model="queryParams.inboundType" placeholder="请选择入库类型" clearable style="width:100%">
              <el-option
                v-for="dict in dict.type.inbound_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="库区" prop="warehouseArea">
            <el-select v-model="queryParams.warehouseArea" placeholder="请选择库区" clearable style="width:100%">
              <el-option
                v-for="dict in dict.type.warehouse_area"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16" type="flex" align="middle">
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="入库时间" prop="inboundTime">
            <el-date-picker
              clearable
              v-model="queryParams.inboundTime"
              type="date"
              value-format="yyyy-MM-dd"
              placeholder="请选择入库时间"
              style="width:100%">
            </el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="24" :md="8">
          <el-form-item label="操作人员" prop="operator">
            <el-input
              v-model="queryParams.operator"
              placeholder="请输入操作人员"
              clearable
              @keyup.enter.native="handleQuery"
            />
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
          v-hasPermi="['manage:stockin:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:stockin:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['manage:stockin:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockinList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="入库记录ID" align="center" prop="id" />
      <el-table-column label="物料ID" align="center" prop="materialId" />
      <el-table-column label="物料名称" align="center" prop="materialName" />
      <el-table-column label="入库类型" align="center" prop="inboundType">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.inbound_type" :value="scope.row.inboundType"/>
        </template>
      </el-table-column>
      <el-table-column label="库区" align="center" prop="warehouseArea">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.warehouse_area" :value="scope.row.warehouseArea"/>
        </template>
      </el-table-column>
      <el-table-column label="入库数量" align="center" prop="quantity" />
      <el-table-column label="入库时间" align="center" prop="inboundTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.inboundTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作人员" align="center" prop="operator" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:stockin:edit']"
          >修改</el-button>
          <el-button
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['manage:stockin:remove']"
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
      <el-form ref="stockinRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="物料ID" prop="materialId">
          <el-input v-model="form.materialId" placeholder="请输入物料ID" />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="form.materialName" placeholder="请输入物料名称" />
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
        <el-form-item label="入库时间" prop="inboundTime">
          <el-date-picker
            clearable
            v-model="form.inboundTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择入库时间">
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
import { listStockin, getStockin, delStockin, addStockin, updateStockin } from "@/api/manage/stockin";

export default {
  name: "Stockin",
  dicts: ['warehouse_area', 'inbound_type'],
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
      // 入库记录表格数据
      stockinList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        materialId: null,
        materialName: null,
        inboundType: null,
        warehouseArea: null,
        inboundTime: null,
        operator: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        materialId: [
          { required: true, message: "物料ID不能为空", trigger: "blur" }
        ],
        materialName: [
          { required: true, message: "物料名称不能为空", trigger: "blur" }
        ],
        inboundType: [
          { required: true, message: "入库类型不能为空", trigger: "change" }
        ],
        warehouseArea: [
          { required: true, message: "库区不能为空", trigger: "change" }
        ],
        inboundTime: [
          { required: true, message: "入库时间不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询入库记录列表 */
    getList() {
      this.loading = true;
      listStockin(this.queryParams).then(response => {
        // 兼容 RuoYi-Vue 不同版本的返回结构
        this.stockinList = response.rows || response.data;
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
        inboundType: null,
        quantity: null,
        warehouseArea: null,
        inboundTime: null,
        operator: null,
        createTime: null
      };
      this.resetForm("stockinRef");
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
      this.title = "添加入库记录";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const _id = row.id || this.ids;
      getStockin(_id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改入库记录";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["stockinRef"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStockin(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addStockin(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除入库记录编号为"' + _ids + '"的数据项？').then(() => {
        return delStockin(_ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('manage/stockin/export', {
        ...this.queryParams
      }, `stockin_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
