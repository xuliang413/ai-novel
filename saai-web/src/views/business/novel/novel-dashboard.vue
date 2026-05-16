<template>
  <div class="novel-dashboard-page">
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="小说项目" class="smart-query-form-item">
          <a-select
            v-model:value="projectId"
            show-search
            allow-clear
            :loading="projectLoading"
            :filter-option="filterProjectOption"
            placeholder="请选择项目"
            style="width: 280px"
            @change="loadDashboard"
          >
            <a-select-option v-for="project in projectOptions" :key="project.projectId" :value="project.projectId" :label="project.projectName">
              {{ project.projectName }} · #{{ project.projectId }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="smart-query-form-item">
          <a-button type="primary" @click="loadDashboard" :loading="loading" :disabled="!projectId">
            <template #icon><ReloadOutlined /></template>
            刷新总览
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-spin :spinning="loading">
      <a-row :gutter="[12, 12]" class="summary-grid">
        <a-col v-for="item in summaryItems" :key="item.key" :xs="12" :md="6" :xl="4">
          <div class="summary-tile">
            <span class="summary-value">{{ item.value }}</span>
            <span class="summary-label">{{ item.label }}</span>
          </div>
        </a-col>
      </a-row>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="14">
          <a-card size="small" title="章节进度" :bordered="false">
            <a-table
              size="small"
              bordered
              row-key="chapterId"
              :columns="chapterColumns"
              :data-source="chapterRows"
              :pagination="{ pageSize: 10, showSizeChanger: false }"
              :scroll="{ x: 720 }"
            >
              <template #bodyCell="{ column, record, text }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="statusColor(text)">{{ text || '-' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'wordCount'">
                  {{ formatNumber(record.wordCount || 0) }}
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="10">
          <a-card size="small" title="待处理会话" :bordered="false">
            <a-table
              size="small"
              bordered
              row-key="sessionId"
              :columns="sessionColumns"
              :data-source="pendingSessions"
              :pagination="false"
              :scroll="{ x: 620 }"
            >
              <template #bodyCell="{ column, text }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="statusColor(text)">{{ text || '-' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'provider'">
                  <a-tag>{{ text || '-' }}</a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>

      <a-card size="small" title="最近记录" :bordered="false" class="history-card">
        <a-tabs size="small">
          <a-tab-pane key="logs" tab="写作日志">
            <a-table
              size="small"
              bordered
              row-key="writingLogId"
              :columns="logColumns"
              :data-source="recentLogs"
              :pagination="false"
              :scroll="{ x: 860 }"
            >
              <template #bodyCell="{ column, record, text }">
                <template v-if="column.key === 'success'">
                  <a-tag :color="record.success ? 'green' : 'red'">{{ record.success ? '成功' : '失败' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'wordCount'">
                  {{ formatNumber(record.wordCount || 0) }}
                </template>
              </template>
            </a-table>
          </a-tab-pane>
          <a-tab-pane key="patches" tab="图谱变更">
            <a-table
              size="small"
              bordered
              row-key="changeLogId"
              :columns="patchColumns"
              :data-source="recentPatches"
              :pagination="false"
              :scroll="{ x: 960 }"
            >
              <template #bodyCell="{ column, text }">
                <template v-if="column.key === 'status'">
                  <a-tag :color="statusColor(text)">{{ text || '-' }}</a-tag>
                </template>
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { ReloadOutlined } from '@ant-design/icons-vue';
  import { novelDashboardApi } from '/@/api/business/novel/novel-dashboard-api';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const summaryLabels = [
    { key: 'totalWords', label: '总字数' },
    { key: 'chapterCount', label: '章节' },
    { key: 'volumeCount', label: '分卷' },
    { key: 'characterCount', label: '角色' },
    { key: 'locationCount', label: '地点' },
    { key: 'clueCount', label: '线索' },
    { key: 'itemCount', label: '物品' },
    { key: 'eventCount', label: '事件' },
    { key: 'cheatCount', label: '金手指' },
    { key: 'aliasCount', label: '别名' },
    { key: 'ruleCount', label: '叙事规则' },
  ];

  const projectId = ref();
  const projectOptions = ref([]);
  const projectLoading = ref(false);
  const loading = ref(false);
  const assetSummary = ref({});
  const chapterRows = ref([]);
  const recentLogs = ref([]);
  const recentPatches = ref([]);
  const pendingSessions = ref([]);

  const chapterColumns = [
    { title: '章节', dataIndex: 'chapterNo', key: 'chapterNo', width: 90 },
    { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true },
    { title: '状态', dataIndex: 'status', key: 'status', width: 130 },
    { title: '字数', dataIndex: 'wordCount', key: 'wordCount', width: 110 },
  ];

  const sessionColumns = [
    { title: '会话ID', dataIndex: 'sessionId', key: 'sessionId', width: 100 },
    { title: '章节', dataIndex: 'chapterNo', key: 'chapterNo', width: 90 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 140 },
    { title: '供应商', dataIndex: 'provider', key: 'provider', width: 110 },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  ];

  const logColumns = [
    { title: '日志ID', dataIndex: 'writingLogId', key: 'writingLogId', width: 100 },
    { title: '章节', dataIndex: 'chapterNo', key: 'chapterNo', width: 90 },
    { title: '字数', dataIndex: 'wordCount', key: 'wordCount', width: 110 },
    { title: 'Token', dataIndex: 'tokenUsed', key: 'tokenUsed', width: 110 },
    { title: '供应商', dataIndex: 'provider', key: 'provider', width: 120 },
    { title: '结果', dataIndex: 'success', key: 'success', width: 90 },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  ];

  const patchColumns = [
    { title: '变更ID', dataIndex: 'changeLogId', key: 'changeLogId', width: 100 },
    { title: '章节', dataIndex: 'chapterNo', key: 'chapterNo', width: 90 },
    { title: '批次ID', dataIndex: 'operationBatchId', key: 'operationBatchId', width: 220, ellipsis: true },
    { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
    { title: '错误信息', dataIndex: 'errorMessage', key: 'errorMessage', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  ];

  const summaryItems = computed(() =>
    summaryLabels.map((item) => ({
      ...item,
      value: formatNumber(assetSummary.value?.[item.key] || 0),
    }))
  );

  onMounted(async () => {
    await loadProjects();
    if (projectId.value) {
      loadDashboard();
    }
  });

  function filterProjectOption(input, option) {
    return String(option?.label || '').toLowerCase().includes(input.toLowerCase());
  }

  async function loadProjects() {
    projectLoading.value = true;
    try {
      const res = await novelProjectApi.query({ pageNum: 1, pageSize: 100 });
      projectOptions.value = res.data?.list || [];
      if (!projectId.value && projectOptions.value.length > 0) {
        projectId.value = projectOptions.value[0].projectId;
      }
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      projectLoading.value = false;
    }
  }

  async function loadDashboard() {
    if (!projectId.value) {
      clearDashboard();
      return;
    }
    loading.value = true;
    try {
      const param = { id: projectId.value };
      const [summaryRes, chapterRes, logRes, patchRes, sessionRes] = await Promise.all([
        novelDashboardApi.assetSummary(param),
        novelDashboardApi.chapterProgress(param),
        novelDashboardApi.recentLogs(param),
        novelDashboardApi.recentPatches(param),
        novelDashboardApi.pendingSessions(param),
      ]);
      assetSummary.value = summaryRes.data || {};
      chapterRows.value = chapterRes.data || [];
      recentLogs.value = logRes.data || [];
      recentPatches.value = patchRes.data || [];
      pendingSessions.value = sessionRes.data || [];
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  function clearDashboard() {
    assetSummary.value = {};
    chapterRows.value = [];
    recentLogs.value = [];
    recentPatches.value = [];
    pendingSessions.value = [];
  }

  function statusColor(status) {
    const map = {
      SUCCESS: 'green',
      CONFIGURED: 'green',
      APPLIED: 'green',
      CONTENT_REVIEW: 'blue',
      PATCH_REVIEW: 'purple',
      APPLYING_PATCH: 'processing',
      FAILED: 'red',
      CANCELED: 'orange',
      INTERRUPTED: 'orange',
      UNDONE: 'orange',
      ERROR: 'red',
    };
    return map[status] || 'default';
  }

  function formatNumber(value) {
    return Number(value || 0).toLocaleString();
  }
</script>

<style scoped lang="less">
  .novel-dashboard-page {
    padding: 16px;
  }

  .summary-grid {
    margin-bottom: 16px;
  }

  .summary-tile {
    display: flex;
    min-height: 82px;
    flex-direction: column;
    justify-content: center;
    padding: 14px;
    border: 1px solid #edf0f5;
    border-radius: 6px;
    background: #fff;
  }

  .summary-value {
    color: #1f2329;
    font-size: 24px;
    font-weight: 600;
    line-height: 30px;
  }

  .summary-label {
    margin-top: 6px;
    color: #646a73;
    font-size: 13px;
  }

  .history-card {
    margin-top: 16px;
  }

  @media (max-width: 768px) {
    .novel-dashboard-page {
      padding: 12px;
    }
  }
</style>
