<template>
  <div class="novel-graph-health-page">
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
          >
            <a-select-option v-for="project in projectOptions" :key="project.projectId" :value="project.projectId" :label="project.projectName">
              {{ project.projectName }} · #{{ project.projectId }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="smart-query-form-item">
          <a-button type="primary" @click="loadHealth" :loading="loading" :disabled="!projectId">
            <template #icon><SearchOutlined /></template>
            检查图谱
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" title="图谱健康检查" :bordered="false">
      <a-alert
        type="info"
        show-icon
        class="health-alert"
        message="当前后端只提供节点数量健康检查，关系图渲染和 GraphPatch 历史需要后端补充查询接口。"
      />

      <a-row :gutter="[12, 12]">
        <a-col v-for="item in healthItems" :key="item.type" :xs="12" :md="8" :xl="6">
          <div class="health-tile">
            <span class="health-count">{{ item.count }}</span>
            <span class="health-label">{{ item.type }}</span>
          </div>
        </a-col>
      </a-row>
      <a-empty v-if="healthItems.length === 0" description="请选择项目并执行检查" />
    </a-card>
  </div>
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { SearchOutlined } from '@ant-design/icons-vue';
  import { novelGraphApi } from '/@/api/business/novel/novel-graph-api';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const projectId = ref();
  const projectOptions = ref([]);
  const healthMap = ref({});
  const projectLoading = ref(false);
  const loading = ref(false);

  const healthItems = computed(() =>
    Object.entries(healthMap.value || {})
      .map(([type, count]) => ({ type, count }))
      .sort((a, b) => a.type.localeCompare(b.type))
  );

  onMounted(loadProjects);

  function filterProjectOption(input, option) {
    return String(option?.label || '').toLowerCase().includes(input.toLowerCase());
  }

  async function loadProjects() {
    projectLoading.value = true;
    try {
      const res = await novelProjectApi.query({ pageNum: 1, pageSize: 50 });
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

  async function loadHealth() {
    if (!projectId.value) {
      return;
    }
    loading.value = true;
    try {
      const res = await novelGraphApi.health({ id: projectId.value });
      healthMap.value = res.data || {};
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }
</script>

<style scoped lang="less">
  .novel-graph-health-page {
    padding: 16px;
  }

  .health-alert {
    margin-bottom: 16px;
  }

  .health-tile {
    display: flex;
    min-height: 86px;
    flex-direction: column;
    justify-content: center;
    padding: 14px;
    border: 1px solid #edf0f5;
    border-radius: 6px;
    background: #fafbfc;
  }

  .health-count {
    color: #1f2329;
    font-size: 24px;
    font-weight: 600;
    line-height: 30px;
  }

  .health-label {
    margin-top: 6px;
    color: #646a73;
    font-size: 13px;
  }

  @media (max-width: 768px) {
    .novel-graph-health-page {
      padding: 12px;
    }
  }
</style>

