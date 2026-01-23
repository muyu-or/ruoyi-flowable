<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="班组名称" prop="teamName">
        <el-input
          v-model="queryParams.teamName"
          placeholder="请输入班组名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="班组长姓名" prop="leaderName">
        <el-input
          v-model="queryParams.leaderName"
          placeholder="请输入班组长姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="teamStatus">
        <el-select v-model="queryParams.teamStatus" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.team_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item style="float: right">
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          @click="handleAdd"
          v-hasPermi="['manage:team:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['manage:team:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          @click="handleExport"
          v-hasPermi="['manage:team:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="teamList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="班组名称" align="center" prop="teamName" />
      <el-table-column label="班组长姓名" align="center" prop="leaderName" />
      <el-table-column label="状态" align="center" prop="teamStatus">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.teamStatus"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['manage:team:query']"
          >查看</el-button>
          <el-button
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['manage:team:edit']"
          >修改</el-button>
          <el-button
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['manage:team:remove']"
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

    <!-- 添加或修改产线班组对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="班组名称" prop="teamName">
          <el-input v-model="form.teamName" placeholder="请输入班组名称" />
        </el-form-item>
        <el-form-item label="班组长姓名" prop="leaderName">
          <el-input v-model="form.leaderName" placeholder="请选择班组长" readonly>
            <el-button slot="append" icon="el-icon-search" @click="handleSelectUser('leader')"></el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="状态" prop="teamStatus">
          <el-radio-group v-model="form.teamStatus">
            <el-radio
              v-for="dict in dict.type.team_status"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班组成员">
          <el-button type="primary" size="mini" icon="el-icon-plus" @click="handleSelectUser('member')" style="margin-bottom: 10px;">添加成员</el-button>
          <el-table :data="selectedMembers" border style="width: 100%">
            <el-table-column prop="nickName" label="用户名称" align="center"/>
            <el-table-column prop="roleNames" label="角色" align="center" :show-overflow-tooltip="true"/>
            <el-table-column prop="dept.deptName" label="部门" align="center"/>
            <el-table-column label="操作" align="center" width="80">
              <template slot-scope="scope">
                 <el-tag v-if="scope.row.userId === form.leaderId" type="warning" size="mini">班组长</el-tag>
                 <el-button v-else type="text" icon="el-icon-delete" @click="handleRemoveMember(scope.$index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看产线班组详情对话框 -->
    <el-dialog title="班组详情" :visible.sync="openView" width="800px" append-to-body>
      <el-form ref="viewForm" :model="viewForm" label-width="100px" size="mini">
        <el-row>
          <el-col :span="12">
            <el-form-item label="班组名称：">{{ viewForm.teamName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班组长：">{{ viewForm.leaderName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态：">
              <dict-tag :options="dict.type.team_status" :value="viewForm.teamStatus"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注：">{{ viewForm.remark }}</el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="center">班组成员信息</el-divider>
        
        <el-table :data="viewForm.userList" border style="width: 100%">
          <el-table-column prop="nickName" label="用户名称" align="center">
             <template slot-scope="scope">
                {{ scope.row.nickName }}
                <el-tag v-if="scope.row.userId === viewForm.leaderId" type="warning" size="mini" style="margin-left: 5px">班组长</el-tag>
             </template>
          </el-table-column>
          <el-table-column prop="roleNames" label="角色" align="center" :show-overflow-tooltip="true"/>
          <el-table-column prop="dept.deptName" label="部门" align="center"/>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="openView = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 用户选择弹窗 -->
    <el-dialog :title="userSelectType === 'leader' ? '选择班组长' : '添加班组成员'" :visible.sync="openUser" width="1000px" append-to-body>
      <el-form :model="userQueryParams" ref="userQueryForm" :inline="true">
        <el-form-item label="归属部门" prop="deptId">
          <treeselect v-model="userQueryParams.deptId" :options="deptOptions" :show-count="true" placeholder="请选择归属部门" style="width: 200px" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userQueryParams.roleId" placeholder="请选择角色" clearable style="width: 150px">
            <el-option
              v-for="item in roleOptions"
              :key="item.roleId"
              :label="item.roleName"
              :value="item.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名称" prop="userName">
          <el-input v-model="userQueryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter.native="handleUserQuery" style="width: 150px"/>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleUserQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetUserQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="userList" :row-class-name="tableRowClassName">
        <el-table-column label="用户姓名" align="center" prop="nickName" />
        <el-table-column label="角色" align="center" prop="roleNames" :show-overflow-tooltip="true" />
        <el-table-column label="部门" align="center" prop="dept.deptName" />

        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="scope">
            <div v-if="userSelectType === 'member' && scope.row.userId === form.leaderId">
               <el-tag type="warning" size="mini">班组长</el-tag>
               <span v-if="isUserSelected(scope.row.userId)" style="margin-left: 5px; color: #1890ff; font-size: 12px;">已选</span>
               <el-button v-else size="mini" type="text" icon="el-icon-check" @click="handleUserSelect(scope.row)">选择</el-button>
            </div>
            <div v-else>
               <el-button v-if="isUserSelected(scope.row.userId)" size="mini" type="text" icon="el-icon-close" class="text-danger" @click="handleUserUnselect(scope.row)">取消</el-button>
               <el-button v-else size="mini" type="text" icon="el-icon-check" @click="handleUserSelect(scope.row)">选择</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination
        v-show="userTotal>0"
        :total="userTotal"
        :page.sync="userQueryParams.pageNum"
        :limit.sync="userQueryParams.pageSize"
        @pagination="getUserList"
      />
    </el-dialog>
  </div>
</template>

<script>
import { listTeam, getTeam, delTeam, addTeam, updateTeam, listTeamUser } from "@/api/manage/team";
import { deptTreeSelect } from "@/api/system/user";
import { optionSelect } from "@/api/system/role";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Team",
  dicts: ['team_status'],
  components: { Treeselect },
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
      // 产线班组表格数据
      teamList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 选人弹窗
      openUser: false,
      // 查看弹窗
      openView: false,
      // 查看详情数据
      viewForm: {},
      // 选人类型：leader=班组长, member=成员
      userSelectType: '',
      // 用户列表
      userList: [],
      // 用户总数
      userTotal: 0,
      // 部门树选项
      deptOptions: undefined,
      // 角色选项
      roleOptions: [],
      // 用户查询参数
      userQueryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        deptId: undefined,
        roleId: undefined
      },
      // 选中的成员列表（对象）
      selectedMembers: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        teamName: null,
        leaderName: null,
        teamStatus: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        teamName: [
          { required: true, message: "班组名称不能为空", trigger: "blur" }
        ],
        leaderName: [
          { required: true, message: "班组长姓名不能为空", trigger: "blur" }
        ],
        leaderId: [
          { required: true, message: "班组长ID 不能为空", trigger: "blur" }
        ],
        teamStatus: [
          { required: true, message: "状态 不能为空", trigger: "change" }
        ],
      }
    };
  },
  created() {
    this.getList();
    this.getDeptTree();
    this.getRoleList();
  },
  methods: {
    /** 查询部门下拉树结构 */
    getDeptTree() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data;
      });
    },
    /** 查询角色列表 */
    getRoleList() {
      optionSelect().then(response => {
        this.roleOptions = response.data;
      });
    },
    /** 查询产线班组列表 */
    getList() {
      this.loading = true;
      listTeam(this.queryParams).then(response => {
        this.teamList = response.rows;
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
        teamName: null,
        leaderName: null,
        leaderId: null,
        teamStatus: 1,
        userIds: [],
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      };
      this.selectedMembers = [];
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加产线班组";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getTeam(id).then(response => {
        this.form = response.data;
        // 回显成员
        if (response.data.userList) {
           this.selectedMembers = response.data.userList;
        }
        // 处理状态回显（字符串转数字）
        if (this.form.teamStatus !== null) {
           this.form.teamStatus = parseInt(this.form.teamStatus);
        }
        this.open = true;
        this.title = "修改产线班组";
      });
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset();
      const id = row.id || this.ids
      getTeam(id).then(response => {
        this.viewForm = response.data;
        // 处理成员列表，将班组长置顶
        if (this.viewForm.userList && this.viewForm.userList.length > 0) {
           this.viewForm.userList.sort((a, b) => {
             if (a.userId === this.viewForm.leaderId) return -1;
             if (b.userId === this.viewForm.leaderId) return 1;
             return 0;
           });
        }
        this.openView = true;
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 处理userIds
          this.form.userIds = this.selectedMembers.map(u => u.userId);
          
          if (this.form.id != null) {
            updateTeam(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTeam(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 打开选人弹窗 */
    handleSelectUser(type) {
      this.userSelectType = type;
      this.openUser = true;
      this.resetUserQuery(); // 打开时重置查询条件
    },
    /** 查询用户列表 */
    getUserList() {
      listTeamUser(this.userQueryParams).then(response => {
          this.userList = response.rows;
          this.userTotal = response.total;
      });
    },
    /** 选人弹窗-搜索 */
    handleUserQuery() {
      this.userQueryParams.pageNum = 1;
      this.getUserList();
    },
    /** 选人弹窗-重置 */
    resetUserQuery() {
      this.userQueryParams = {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        deptId: undefined,
        roleId: undefined
      };
      this.getUserList();
    },
    /** 判断用户是否已选 */
    isUserSelected(userId) {
      if (this.userSelectType === 'leader') {
         return this.form.leaderId === userId;
      }
      return this.selectedMembers.some(u => u.userId === userId);
    },
    /** 设置行高亮样式 */
    tableRowClassName({row, rowIndex}) {
      if (this.isUserSelected(row.userId)) {
        return 'success-row';
      }
      return '';
    },
    /** 取消选择 */
    handleUserUnselect(row) {
      if (this.userSelectType === 'leader') {
          // 班组长选择模式下一般是直接替换，不提供“取消”按钮，或者取消意味着清空
          this.form.leaderId = null;
          this.form.leaderName = null;
          // 注意：如果取消了班组长，是否也要从成员列表移除？
          // 这里保持简单，只清空显示
      } else {
          // 成员模式
          const index = this.selectedMembers.findIndex(u => u.userId === row.userId);
          if (index > -1) {
             this.selectedMembers.splice(index, 1);
             this.$modal.msgSuccess("已移除成员：" + row.nickName);
          }
      }
    },
    /** 选人操作 */
    handleUserSelect(row) {
      if (this.userSelectType === 'leader') {
        this.form.leaderId = row.userId;
        this.form.leaderName = row.nickName;
        this.openUser = false;
        
        // 如果新选的班组长已经是成员，不需要做额外处理，因为“标记”是显示层逻辑
        // 如果新选的班组长不在成员列表中，是否自动添加为成员？
        // 通常班组长也是成员之一，这里可以选择自动加入
        if (!this.selectedMembers.some(m => m.userId === row.userId)) {
           this.selectedMembers.push(row);
        }
      } else {
        // 添加成员（去重）
        if (!this.selectedMembers.some(m => m.userId === row.userId)) {
          this.selectedMembers.push(row);
        }
        this.$modal.msgSuccess("已添加成员：" + row.nickName);
      }
    },
    /** 移除成员 */
    handleRemoveMember(index) {
      this.selectedMembers.splice(index, 1);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除产线班组编号为"' + ids + '"的数据项？').then(function() {
        return delTeam(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('manage/team/export', {
        ...this.queryParams
      }, `team_${new Date().getTime()}.xlsx`)
    },
    /** 状态修改 */
    handleStatusChange(row) {
      // 1=启用/正常, 0=停用
      let text = row.teamStatus === "1" ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.teamName + '"班组吗？').then(function() {
        return updateTeam({ id: row.id, teamStatus: row.teamStatus });
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.teamStatus = row.teamStatus === "1" ? "0" : "1";
      });
    }
  }
};
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.text-danger {
  color: #ff4949;
}
::v-deep .el-table .success-row {
  background: #f0f9eb;
}
</style>
