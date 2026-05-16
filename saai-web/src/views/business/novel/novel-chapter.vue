<template>
  <div class="novel-chapter-page">
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="小说项目" class="smart-query-form-item">
          <a-select
            v-model:value="queryForm.projectId"
            show-search
            allow-clear
            :loading="projectLoading"
            :filter-option="filterProjectOption"
            placeholder="请选择项目"
            style="width: 260px"
            @change="onProjectChange"
          >
            <a-select-option v-for="project in projectOptions" :key="project.projectId" :value="project.projectId" :label="project.projectName">
              {{ project.projectName }} · #{{ project.projectId }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="章节状态" class="smart-query-form-item">
          <a-select v-model:value="queryForm.status" allow-clear placeholder="全部状态" style="width: 180px" @change="onSearch">
            <a-select-option v-for="status in chapterStatusOptions" :key="status.value" :value="status.value">
              {{ status.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item class="smart-query-form-item">
          <a-button-group>
            <a-button type="primary" @click="onSearch" :disabled="!queryForm.projectId">
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
        <div class="chapter-table-title">
          <span>章节管理</span>
          <a-typography-text type="secondary">正文从章节详情加载，P0 仅编辑标题、摘要和状态。</a-typography-text>
        </div>
        <div class="smart-table-setting-block">
          <a-button @click="queryChapterList" :loading="tableLoading" :disabled="!queryForm.projectId">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>
      </a-row>

      <a-alert
        v-if="!queryForm.projectId"
        type="info"
        show-icon
        message="请选择小说项目后查看章节列表"
        class="chapter-alert"
      />

      <a-table
        size="small"
        bordered
        row-key="chapterId"
        :loading="tableLoading"
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.key === 'chapterNo'">
            <a-button type="link" class="chapter-link" @click="openDetail(record)">第 {{ text }} 章</a-button>
          </template>
          <template v-else-if="column.key === 'title'">
            <div class="chapter-title-cell">
              <span>{{ text || '未命名章节' }}</span>
              <a-typography-text type="secondary">{{ record.summary || '暂无摘要' }}</a-typography-text>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="chapterStatusColor(text)">{{ chapterStatusText(text) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="smart-table-operate">
              <a-button type="link" @click="openDetail(record)">查看</a-button>
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
          @change="queryChapterList"
          :show-total="(total) => `共 ${total} 条`"
        />
      </div>
    </a-card>

    <a-drawer
      :open="drawerVisible"
      :width="920"
      :title="drawerMode === 'edit' ? '编辑章节' : '章节详情'"
      :destroy-on-close="true"
      :footer-style="{ textAlign: 'right' }"
      @close="closeDrawer"
    >
      <a-spin :spinning="detailLoading">
        <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
          <a-row :gutter="16">
            <a-col :xs="24" :md="8">
              <a-form-item label="章节 ID">
                <a-input v-model:value="formData.chapterId" disabled />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="8">
              <a-form-item label="章节序号">
                <a-input-number v-model:value="formData.chapterNo" disabled style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="8">
              <a-form-item label="状态" name="status">
                <a-select v-model:value="formData.status" :disabled="drawerMode !== 'edit'" allow-clear>
                  <a-select-option v-for="status in chapterStatusOptions" :key="status.value" :value="status.value">
                    {{ status.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="章节标题" name="title">
            <a-input v-model:value="formData.title" :disabled="drawerMode !== 'edit'" placeholder="请输入章节标题" />
          </a-form-item>

          <a-form-item label="章节摘要" name="summary">
            <a-textarea v-model:value="formData.summary" :disabled="drawerMode !== 'edit'" :rows="4" placeholder="请输入章节摘要" />
          </a-form-item>

          <a-form-item label="正文内容">
            <a-textarea v-model:value="formData.content" disabled :rows="16" placeholder="暂无正文内容" />
          </a-form-item>
        </a-form>
      </a-spin>

      <template #footer>
        <a-space>
          <a-button @click="closeDrawer">关闭</a-button>
          <a-button v-if="drawerMode === 'edit'" type="primary" @click="submitEdit" :loading="saving">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { novelChapterApi } from '/@/api/business/novel/novel-chapter-api';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const chapterStatusOptions = [
    { value: 'DRAFT', label: '草稿' },
    { value: 'PENDING_GRAPH_CONFIRM', label: '待确认图谱' },
    { value: 'PENDING_GRAPH_UPDATE', label: '待同步图谱' },
    { value: 'PUBLISHED', label: '已发布' },
    { value: 'INTERRUPTED_DRAFT', label: '中断草稿' },
  ];

  const queryFormState = {
    projectId: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };

  const queryForm = reactive({ ...queryFormState });
  const projectOptions = ref([]);
  const projectLoading = ref(false);
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  const columns = [
    { title: '章节', dataIndex: 'chapterNo', key: 'chapterNo', width: 100, sorter: true },
    { title: '标题 / 摘要', dataIndex: 'title', key: 'title', ellipsis: true },
    { title: '状态', dataIndex: 'status', key: 'status', width: 140 },
    { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    { title: '操作', key: 'action', fixed: 'right', width: 150 },
  ];

  const drawerVisible = ref(false);
  const drawerMode = ref('view');
  const detailLoading = ref(false);
  const saving = ref(false);
  const formRef = ref();

  const defaultFormData = {
    chapterId: undefined,
    projectId: undefined,
    chapterNo: undefined,
    title: '',
    summary: '',
    content: '',
    status: undefined,
  };

  const formData = reactive({ ...defaultFormData });

  const formRules = {
    title: [{ max: 200, message: '章节标题最多 200 个字符' }],
    summary: [{ max: 2000, message: '章节摘要最多 2000 个字符' }],
    status: [{ max: 50, message: '章节状态最多 50 个字符' }],
  };

  onMounted(async () => {
    await loadProjects();
    if (queryForm.projectId) {
      queryChapterList();
    }
  });

  function filterProjectOption(input, option) {
    return String(option?.label || '').toLowerCase().includes(input.toLowerCase());
  }

  async function loadProjects() {
    projectLoading.value = true;
    try {
      const res = await novelProjectApi.query({ pageNum: 1, pageSize: 50 });
      projectOptions.value = res.data?.list || [];
      if (!queryForm.projectId && projectOptions.value.length > 0) {
        queryForm.projectId = projectOptions.value[0].projectId;
      }
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      projectLoading.value = false;
    }
  }

  function onProjectChange() {
    queryForm.pageNum = 1;
    queryChapterList();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryChapterList();
  }

  function resetQuery() {
    const pageSize = queryForm.pageSize;
    const projectId = queryForm.projectId;
    Object.assign(queryForm, { ...queryFormState, pageSize, projectId });
    queryChapterList();
  }

  async function queryChapterList() {
    if (!queryForm.projectId) {
      tableData.value = [];
      total.value = 0;
      return;
    }
    tableLoading.value = true;
    try {
      const res = await novelChapterApi.query(queryForm);
      tableData.value = res.data?.list || [];
      total.value = res.data?.total || 0;
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      tableLoading.value = false;
    }
  }

  function openDetail(record) {
    drawerMode.value = 'view';
    loadDetail(record.chapterId);
  }

  function openEdit(record) {
    drawerMode.value = 'edit';
    loadDetail(record.chapterId);
  }

  async function loadDetail(chapterId) {
    Object.assign(formData, defaultFormData);
    drawerVisible.value = true;
    detailLoading.value = true;
    try {
      const res = await novelChapterApi.detail({ id: chapterId });
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

  async function submitEdit() {
    try {
      await formRef.value.validateFields();
      saving.value = true;
      await novelChapterApi.update({
        chapterId: formData.chapterId,
        title: formData.title || undefined,
        summary: formData.summary || undefined,
        status: formData.status || undefined,
      });
      message.success('章节已保存');
      closeDrawer();
      queryChapterList();
    } catch (err) {
      if (err?.errorFields) {
        message.warning('请检查章节信息');
      } else {
        smartSentry.captureError(err);
      }
    } finally {
      saving.value = false;
    }
  }

  function confirmArchive(record) {
    Modal.confirm({
      title: '归档章节',
      content: `确认归档第 ${record.chapterNo} 章「${record.title || '未命名章节'}」？`,
      okText: '归档',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => archiveChapter(record.chapterId),
    });
  }

  async function archiveChapter(chapterId) {
    try {
      await novelChapterApi.archive({ id: chapterId });
      message.success('章节已归档');
      queryChapterList();
    } catch (err) {
      smartSentry.captureError(err);
    }
  }

  function chapterStatusText(status) {
    const target = chapterStatusOptions.find((item) => item.value === status);
    return target?.label || status || '-';
  }

  function chapterStatusColor(status) {
    const map = {
      DRAFT: 'default',
      PENDING_GRAPH_CONFIRM: 'orange',
      PENDING_GRAPH_UPDATE: 'warning',
      PUBLISHED: 'green',
      INTERRUPTED_DRAFT: 'red',
    };
    return map[status] || 'default';
  }
</script>

<style scoped lang="less">
  .novel-chapter-page {
    padding: 16px;
  }

  .chapter-alert {
    margin-bottom: 12px;
  }

  .chapter-table-title {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #1f2329;
    font-size: 15px;
    font-weight: 600;
  }

  .chapter-link {
    padding: 0;
  }

  .chapter-title-cell {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  .chapter-title-cell span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.ant-drawer-body textarea[disabled]) {
    color: #1f2329;
    cursor: default;
  }

  @media (max-width: 768px) {
    .novel-chapter-page {
      padding: 12px;
    }

    .chapter-table-title {
      align-items: flex-start;
      flex-direction: column;
      gap: 4px;
    }
  }
</style>
