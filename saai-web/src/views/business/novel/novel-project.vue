<template>
  <div class="novel-project-page">
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="项目名称" class="smart-query-form-item">
          <a-input v-model:value="queryForm.projectName" allow-clear placeholder="输入项目名称" style="width: 220px" @pressEnter="onSearch" />
        </a-form-item>
        <a-form-item label="项目状态" class="smart-query-form-item">
          <a-select v-model:value="queryForm.status" allow-clear placeholder="全部状态" style="width: 160px" @change="onSearch">
            <a-select-option v-for="item in statusOptions" :key="item.code" :value="item.code">{{ item.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item class="smart-query-form-item">
          <a-button-group>
            <a-button type="primary" @click="onSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="resetQuery">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-button-group>
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" :bordered="false">
      <a-row class="smart-table-btn-block">
        <div class="smart-table-operate-block">
          <a-button type="primary" @click="openCreate">
            <template #icon><PlusOutlined /></template>
            新建项目
          </a-button>
        </div>
        <div class="smart-table-setting-block">
          <a-button @click="queryProjectList" :loading="tableLoading">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>
      </a-row>

      <a-table
        size="small"
        bordered
        row-key="projectId"
        :columns="columns"
        :data-source="tableData"
        :loading="tableLoading"
        :pagination="false"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.key === 'projectName'">
            <a-button type="link" class="project-name-link" @click="openView(record)">{{ text }}</a-button>
          </template>
          <template v-else-if="column.key === 'genre'">
            <a-tag>{{ dictName(genreOptions, text) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(text)">{{ dictName(statusOptions, text) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'summary'">
            <span class="project-summary">{{ text || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="smart-table-operate">
              <a-button type="link" @click="openView(record)">查看</a-button>
              <a-button type="link" @click="openEdit(record)">编辑</a-button>
              <a-button type="link" danger @click="confirmArchive(record)">归档</a-button>
            </div>
          </template>
        </template>
      </a-table>

      <div class="smart-query-table-page">
        <a-pagination
          show-size-changer
          show-quick-jumper
          show-less-items
          :page-size-options="PAGE_SIZE_OPTIONS"
          :default-page-size="queryForm.pageSize"
          v-model:current="queryForm.pageNum"
          v-model:pageSize="queryForm.pageSize"
          :total="total"
          @change="queryProjectList"
          :show-total="(total) => `共 ${total} 条`"
        />
      </div>
    </a-card>

    <a-drawer
      :open="drawerVisible"
      :width="860"
      :title="drawerTitle"
      :destroy-on-close="true"
      :footer-style="{ textAlign: 'right' }"
      @close="closeDrawer"
    >
      <a-spin :spinning="detailLoading">
        <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
          <a-row :gutter="16">
            <a-col :xs="24" :md="12">
              <a-form-item label="项目名称" name="projectName">
                <a-input v-model:value="formData.projectName" :disabled="drawerMode === 'view'" placeholder="请输入项目名称" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="小说类型" name="genre">
                <a-select v-model:value="formData.genre" :disabled="drawerMode === 'view'" allow-clear>
                  <a-select-option v-for="item in genreOptions" :key="item.code" :value="item.code">{{ item.name }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="主角" name="protagonist">
                <a-input v-model:value="formData.protagonist" :disabled="drawerMode === 'view'" placeholder="主角名称" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="目标字数" name="targetWords">
                <a-input-number v-model:value="formData.targetWords" :disabled="drawerMode === 'view'" :min="0" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="项目状态" name="status">
                <a-select v-model:value="formData.status" :disabled="drawerMode === 'view'" allow-clear>
                  <a-select-option v-for="item in formStatusOptions" :key="item.code" :value="item.code">{{ item.name }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="项目简介" name="summary">
                <a-textarea v-model:value="formData.summary" :disabled="drawerMode === 'view'" :rows="6" placeholder="作品设定、卖点和主要冲突" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-spin>

      <template #footer>
        <a-space>
          <a-button @click="closeDrawer">关闭</a-button>
          <a-button v-if="drawerMode !== 'view'" type="primary" @click="submitForm" :loading="saving">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { novelDictApi } from '/@/api/business/novel/novel-dict-api';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const queryFormState = {
    projectName: '',
    status: undefined,
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };

  const queryForm = reactive({ ...queryFormState });
  const tableData = ref([]);
  const total = ref(0);
  const tableLoading = ref(false);
  const detailLoading = ref(false);
  const saving = ref(false);
  const statusOptions = ref([]);
  const genreOptions = ref([]);
  const drawerVisible = ref(false);
  const drawerMode = ref('view');
  const formRef = ref();

  const defaultFormData = {
    projectId: undefined,
    projectName: '',
    genre: undefined,
    summary: '',
    protagonist: '',
    targetWords: undefined,
    status: 'ACTIVE',
  };

  const formData = reactive({ ...defaultFormData });
  const drawerTitle = computed(() => (drawerMode.value === 'create' ? '新建项目' : drawerMode.value === 'edit' ? '编辑项目' : '项目详情'));
  const formStatusOptions = computed(() => {
    if (drawerMode.value === 'view') {
      return statusOptions.value;
    }
    return statusOptions.value.filter((item) => item.code !== 'ARCHIVED');
  });

  const columns = [
    { title: '项目名称', dataIndex: 'projectName', key: 'projectName', width: 180, ellipsis: true },
    { title: '类型', dataIndex: 'genre', key: 'genre', width: 120 },
    { title: '主角', dataIndex: 'protagonist', key: 'protagonist', width: 120, ellipsis: true },
    { title: '目标字数', dataIndex: 'targetWords', key: 'targetWords', width: 110 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
    { title: '简介', dataIndex: 'summary', key: 'summary', ellipsis: true },
    { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
    { title: '操作', key: 'action', fixed: 'right', width: 150 },
  ];

  const formRules = {
    projectName: [
      { required: true, message: '请输入项目名称' },
      { max: 100, message: '项目名称最多 100 个字符' },
    ],
    summary: [{ max: 2000, message: '项目简介最多 2000 个字符' }],
    protagonist: [{ max: 100, message: '主角名称最多 100 个字符' }],
  };

  onMounted(async () => {
    await loadDictionaries();
    queryProjectList();
  });

  async function loadDictionaries() {
    try {
      const [statusRes, genreRes] = await Promise.all([novelDictApi['project-status'](), novelDictApi['project-genre']()]);
      statusOptions.value = statusRes.data || [];
      genreOptions.value = genreRes.data || [];
    } catch (err) {
      smartSentry.captureError(err);
    }
  }

  function dictName(options, code) {
    if (!code) {
      return '-';
    }
    return options.find((item) => item.code === code)?.name || code;
  }

  function statusColor(status) {
    const map = { ACTIVE: 'green', PAUSED: 'orange', ARCHIVED: 'red' };
    return map[status] || 'default';
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryProjectList();
  }

  function resetQuery() {
    const pageSize = queryForm.pageSize;
    Object.assign(queryForm, { ...queryFormState, pageSize });
    queryProjectList();
  }

  async function queryProjectList() {
    tableLoading.value = true;
    try {
      const res = await novelProjectApi.query(queryForm);
      tableData.value = res.data?.list || [];
      total.value = res.data?.total || 0;
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      tableLoading.value = false;
    }
  }

  function resetForm() {
    Object.assign(formData, defaultFormData);
  }

  function openCreate() {
    drawerMode.value = 'create';
    resetForm();
    drawerVisible.value = true;
  }

  function openView(record) {
    drawerMode.value = 'view';
    loadDetail(record.projectId);
  }

  function openEdit(record) {
    drawerMode.value = 'edit';
    loadDetail(record.projectId);
  }

  async function loadDetail(projectId) {
    resetForm();
    drawerVisible.value = true;
    detailLoading.value = true;
    try {
      const res = await novelProjectApi.detail({ id: projectId });
      Object.assign(formData, res.data || {});
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      detailLoading.value = false;
    }
  }

  function closeDrawer() {
    drawerVisible.value = false;
  }

  async function submitForm() {
    try {
      await formRef.value.validateFields();
      saving.value = true;
      if (drawerMode.value === 'create') {
        await novelProjectApi.add({
          projectName: formData.projectName,
          genre: formData.genre,
          summary: formData.summary,
          protagonist: formData.protagonist,
          targetWords: formData.targetWords,
          status: formData.status,
        });
      } else {
        await novelProjectApi.update({
          projectId: formData.projectId,
          projectName: formData.projectName,
          genre: formData.genre,
          summary: formData.summary,
          protagonist: formData.protagonist,
          targetWords: formData.targetWords,
          status: formData.status,
        });
      }
      message.success('项目已保存');
      closeDrawer();
      queryProjectList();
    } catch (err) {
      if (err?.errorFields) {
        message.warning('请检查项目信息');
      } else {
        smartSentry.captureError(err);
      }
    } finally {
      saving.value = false;
    }
  }

  function confirmArchive(record) {
    Modal.confirm({
      title: '归档项目',
      content: `确认归档「${record.projectName}」？`,
      okText: '归档',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => archiveProject(record.projectId),
    });
  }

  async function archiveProject(projectId) {
    try {
      await novelProjectApi.archive({ id: projectId });
      message.success('项目已归档');
      queryProjectList();
    } catch (err) {
      smartSentry.captureError(err);
    }
  }
</script>

<style scoped lang="less">
  .novel-project-page {
    padding: 16px;
  }

  .project-name-link {
    padding: 0;
  }

  .project-summary {
    display: inline-block;
    max-width: 520px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  @media (max-width: 768px) {
    .novel-project-page {
      padding: 12px;
    }
  }
</style>
