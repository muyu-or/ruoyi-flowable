<template>
  <el-tooltip v-if="isAdmin" content="重置列宽" placement="top">
    <el-button
      size="mini"
      circle
      icon="el-icon-refresh-right"
      @click="handleReset"
    />
  </el-tooltip>
</template>

<script>
import { clearColumnsWidth } from '@/utils/tableColWidth'

export default {
  name: 'TableColWidthReset',
  props: {
    tableId: {
      type: String,
      default: 'main'
    }
  },
  computed: {
    isAdmin() {
      const roles = this.$store.getters && this.$store.getters.roles
      return roles && roles.includes('admin')
    }
  },
  methods: {
    async handleReset() {
      try {
        await this.$modal.confirm('确定要重置当前表格的列宽为默认值吗？')
        const success = await clearColumnsWidth(this, this.tableId)
        if (success) {
          this.$modal.msgSuccess('重置成功，请刷新页面查看效果')
          // 触发父组件刷新表格
          this.$emit('reset')
        } else {
          this.$modal.msgError('重置失败')
        }
      } catch (e) {
        // 用户取消
      }
    }
  }
}
</script>
