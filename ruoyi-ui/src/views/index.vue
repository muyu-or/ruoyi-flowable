<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2>欢迎使用自动化生产线管理系统</h2>
            <p>系统提供生产流程管理、库存跟踪、班组管理、测试报告等功能，助力企业实现智能化生产管理。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-divider />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>系统功能</span>
          </div>
          <div class="body">
            <p><i class="el-icon-s-platform"></i> 流程管理：可视化流程设计与审批</p>
            <p><i class="el-icon-s-order"></i> 库存管理：物料入库、出库实时跟踪</p>
            <p><i class="el-icon-s-custom"></i> 班组管理：生产班组人员配置</p>
            <p><i class="el-icon-document"></i> 测试报告：检测数据记录与模板管理</p>
            <p><i class="el-icon-bell"></i> 预警系统：节点超时自动预警通知</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>快捷入口</span>
          </div>
          <div class="body">
            <p><a href="#/flowable/definition"><i class="el-icon-s-management"></i> 流程定义</a></p>
            <p><a href="#/manage/inventory"><i class="el-icon-s-grid"></i> 库存列表</a></p>
            <p><a href="#/manage/team"><i class="el-icon-s-team"></i> 班组管理</a></p>
            <p><a href="#/manage/report"><i class="el-icon-document-checked"></i> 测试报告</a></p>
            <p><a href="#/bi/dashboard"><i class="el-icon-data-line"></i> BI大屏</a></p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>系统信息</span>
          </div>
          <div class="body">
            <p><i class="el-icon-time"></i> 版本：v1.0.0</p>
            <p><i class="el-icon-user"></i> 当前用户：{{ userName }}</p>
            <p><i class="el-icon-date"></i> 登录时间：{{ loginTime }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "index",
  data() {
    return {
      version: "1.0.0",
    };
  },
  computed: {
    userName() {
      return this.$store.state.user.name || '未知'
    },
    loginTime() {
      const now = new Date()
      return now.toLocaleDateString() + ' ' + now.toLocaleTimeString()
    }
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank");
    },
  },
};
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }
  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }
  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .welcome-card {
    background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
    color: #fff;
    .welcome-content {
      padding: 30px;
      h2 {
        color: #fff;
        margin-bottom: 15px;
      }
      p {
        color: rgba(255, 255, 255, 0.9);
      }
    }
  }

  .update-log {
    .body {
      p {
        margin: 12px 0;
        i {
          margin-right: 8px;
          color: #409EFF;
        }
        a {
          color: #409EFF;
          text-decoration: none;
        }
      }
    }
  }
}
</style>