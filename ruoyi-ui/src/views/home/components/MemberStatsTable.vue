<template>
  <el-card class="member-card" shadow="hover">
    <div slot="header" class="card-header">
      <span class="card-title"><i class="el-icon-user" /> 班组成员完成情况</span>
      <span class="member-count">共 {{ memberStatsList.length }} 人</span>
    </div>
    <el-table
      :data="memberStatsList"
      stripe
      v-table-col-width="'memberStats'"
      :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: '600' }"
      style="width: 100%"
    >
      <el-table-column label="成员" min-width="120">
        <template slot-scope="scope">
          <div class="member-cell">
            <span class="member-avatar">{{ avatarChar(scope.row.userName) }}</span>
            <span class="member-name">{{ scope.row.userName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="待办" width="70" align="center">
        <template slot-scope="scope">
          <span class="num-warn">{{ scope.row.pending || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="进行中" width="80" align="center">
        <template slot-scope="scope">
          <span class="num-primary">{{ scope.row.running || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已完成" width="80" align="center">
        <template slot-scope="scope">
          <span class="num-success">{{ scope.row.finished || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="total" label="合计" width="70" align="center" />
      <el-table-column label="完成率" width="160" align="center">
        <template slot-scope="scope">
          <div class="rate-cell">
            <el-progress
              :percentage="calcRate(scope.row)"
              :stroke-width="10"
              :show-text="false"
              :color="getColor(calcRate(scope.row))"
            />
            <span class="rate-text" :style="{ color: getColor(calcRate(scope.row)) }">{{ calcRate(scope.row) }}%</span>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
export default {
  name: 'MemberStatsTable',
  props: {
    memberStatsList: {
      type: Array,
      default: function() { return [] }
    }
  },
  methods: {
    calcRate: function(row) {
      if (!row.total || row.total === 0) return 0
      return Math.round(((row.finished || 0) / row.total) * 100)
    },
    getColor: function(pct) {
      if (pct >= 90) return '#67C23A'
      if (pct >= 60) return '#E6A23C'
      return '#F56C6C'
    },
    avatarChar: function(name) {
      return name ? name.charAt(name.length - 1) : '?'
    }
  }
}
</script>

<style scoped>
.member-card {
  border-radius: 8px;
  height: 100%;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.card-title i {
  margin-right: 6px;
  color: #67C23A;
}
.member-count {
  font-size: 12px;
  color: #909399;
}
.member-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.member-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.member-name {
  font-weight: 500;
  color: #303133;
}
.num-warn { color: #E6A23C; font-weight: 600; }
.num-primary { color: #409EFF; font-weight: 600; }
.num-success { color: #67C23A; font-weight: 600; }
.rate-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.rate-cell >>> .el-progress {
  flex: 1;
}
.rate-text {
  font-size: 13px;
  font-weight: 600;
  min-width: 38px;
  text-align: right;
}
</style>
