<template>
  <div class="novel-asset-page">
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

        <a-form-item label="关键词" class="smart-query-form-item">
          <a-input v-model:value="queryForm.keyword" allow-clear placeholder="名称或摘要" style="width: 180px" @pressEnter="onSearch" />
        </a-form-item>

        <a-form-item v-if="typeField" :label="typeField.label" class="smart-query-form-item">
          <a-select v-model:value="queryForm.type" allow-clear placeholder="全部类型" style="width: 160px" @change="onSearch">
            <a-select-option v-for="item in typeOptions" :key="item.code" :value="item.code">{{ item.name }}</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item v-if="statusField" :label="statusField.label" class="smart-query-form-item">
          <a-select v-model:value="queryForm.status" allow-clear placeholder="全部状态" style="width: 160px" @change="onSearch">
            <a-select-option v-for="item in statusOptions" :key="item.code" :value="item.code">{{ item.name }}</a-select-option>
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
        <div class="smart-table-operate-block">
          <a-button type="primary" @click="openCreate" :disabled="!queryForm.projectId">
            <template #icon><PlusOutlined /></template>
            新建{{ config.title }}
          </a-button>
        </div>
        <div class="smart-table-setting-block">
          <a-button @click="queryList" :loading="tableLoading" :disabled="!queryForm.projectId">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>
      </a-row>

      <a-alert v-if="!queryForm.projectId" class="asset-alert" type="info" show-icon message="请选择小说项目后查看资料" />

      <a-table
        size="small"
        bordered
        :columns="columns"
        :data-source="tableData"
        :loading="tableLoading"
        :pagination="false"
        :row-key="config.idKey"
        :scroll="{ x: 1120 }"
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.key === 'name'">
            <a-button type="link" class="asset-name-link" @click="openView(record)">{{ text || '未命名' }}</a-button>
          </template>
          <template v-else-if="column.key === 'type'">
            <a-tag>{{ dictName(config.typeDict, text) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(text)">{{ dictName(config.statusDict, text) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'revealed'">
            <a-tag :color="text ? 'green' : 'default'">{{ text ? '已揭示' : '未揭示' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'summary'">
            <span class="asset-summary">{{ text || '-' }}</span>
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
          @change="queryList"
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
            <a-col v-for="field in visibleFields" :key="field.key" :xs="24" :md="field.full ? 24 : 12">
              <a-form-item :label="field.label" :name="field.key">
                <a-input-number
                  v-if="field.type === 'number'"
                  v-model:value="formData[field.key]"
                  :disabled="isReadonlyField(field)"
                  style="width: 100%"
                  :min="field.min"
                />
                <a-select
                  v-else-if="field.type === 'select'"
                  v-model:value="formData[field.key]"
                  :disabled="isReadonlyField(field)"
                  allow-clear
                >
                  <a-select-option v-for="item in getDictOptions(field.dict)" :key="item.code" :value="item.code">
                    {{ item.name }}
                  </a-select-option>
                </a-select>
                <a-switch
                  v-else-if="field.type === 'boolean'"
                  v-model:checked="formData[field.key]"
                  :disabled="isReadonlyField(field)"
                  checked-children="是"
                  un-checked-children="否"
                />
                <a-textarea
                  v-else-if="field.type === 'textarea'"
                  v-model:value="formData[field.key]"
                  :disabled="isReadonlyField(field)"
                  :rows="field.rows || 4"
                  :placeholder="field.placeholder"
                />
                <a-input
                  v-else
                  v-model:value="formData[field.key]"
                  :disabled="isReadonlyField(field)"
                  :placeholder="field.placeholder"
                />
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
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { novelAssetApi } from '/@/api/business/novel/novel-asset-api';
  import { novelDictApi } from '/@/api/business/novel/novel-dict-api';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    config: {
      type: Object,
      required: true,
    },
  });

  const projectOptions = ref([]);
  const dictMap = reactive({});
  const projectLoading = ref(false);
  const tableLoading = ref(false);
  const detailLoading = ref(false);
  const saving = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  const queryFormState = {
    projectId: undefined,
    keyword: '',
    type: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };

  const queryForm = reactive({ ...queryFormState });
  const drawerVisible = ref(false);
  const drawerMode = ref('view');
  const formRef = ref();
  const formData = reactive({});

  const api = computed(() => novelAssetApi[props.config.assetType]);
  const typeField = computed(() => props.config.fields.find((field) => field.queryType === 'type'));
  const statusField = computed(() => props.config.fields.find((field) => field.queryType === 'status'));
  const typeOptions = computed(() => getDictOptions(typeField.value?.dict));
  const statusOptions = computed(() => getDictOptions(statusField.value?.dict));
  const visibleFields = computed(() => props.config.fields.filter((field) => !field.hidden));
  const drawerTitle = computed(() => {
    const action = drawerMode.value === 'create' ? '新建' : drawerMode.value === 'edit' ? '编辑' : '查看';
    return `${action}${props.config.title}`;
  });

  const columns = computed(() => {
    const list = [
      { title: props.config.nameLabel || '名称', dataIndex: props.config.nameKey, key: 'name', width: 180, ellipsis: true },
    ];
    if (typeField.value) {
      list.push({ title: typeField.value.label, dataIndex: typeField.value.key, key: 'type', width: 140 });
    }
    if (statusField.value) {
      list.push({ title: statusField.value.label, dataIndex: statusField.value.key, key: 'status', width: 140 });
    }
    for (const field of props.config.tableExtraFields || []) {
      list.push({ title: field.label, dataIndex: field.key, key: field.key, width: field.width || 120 });
    }
    list.push({ title: '摘要', dataIndex: props.config.summaryKey || 'summary', key: 'summary', ellipsis: true });
    list.push({ title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 });
    list.push({ title: '操作', key: 'action', fixed: 'right', width: 150 });
    return list;
  });

  const formRules = computed(() => {
    const rules = {};
    for (const field of props.config.fields) {
      const fieldRules = [];
      if (field.required) {
        fieldRules.push({ required: true, message: `请输入${field.label}` });
      }
      if (field.max) {
        fieldRules.push({ max: field.max, message: `${field.label}最多 ${field.max} 个字符` });
      }
      if (fieldRules.length) {
        rules[field.key] = fieldRules;
      }
    }
    return rules;
  });

  watch(
    () => props.config.assetType,
    () => {
      Object.assign(queryForm, queryFormState);
      tableData.value = [];
      total.value = 0;
      loadDictionaries();
    }
  );

  onMounted(async () => {
    await Promise.all([loadProjects(), loadDictionaries()]);
    if (queryForm.projectId) {
      queryList();
    }
  });

  function getDictOptions(dictName) {
    return dictName ? dictMap[dictName] || [] : [];
  }

  function dictName(dictName, code) {
    if (!code) {
      return '-';
    }
    const target = getDictOptions(dictName).find((item) => item.code === code);
    return target?.name || code;
  }

  function statusColor(status) {
    if (!status) {
      return 'default';
    }
    if (['ACTIVE', 'ALIVE', 'AVAILABLE', 'PUBLISHED', 'RESOLVED'].includes(status)) {
      return 'green';
    }
    if (['PAUSED', 'PENDING_GRAPH_CONFIRM', 'PLANTED', 'ACTIVATED', 'SEALED'].includes(status)) {
      return 'orange';
    }
    if (['ARCHIVED', 'DECEASED', 'DESTROYED', 'LOST', 'INTERRUPTED_DRAFT'].includes(status)) {
      return 'red';
    }
    return 'blue';
  }

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

  async function loadDictionaries() {
    const dictNames = [...new Set(props.config.fields.map((field) => field.dict).filter(Boolean))];
    await Promise.all(
      dictNames.map(async (dictName) => {
        if (dictMap[dictName]) {
          return;
        }
        try {
          const res = await novelDictApi[dictName]();
          dictMap[dictName] = res.data || [];
        } catch (err) {
          smartSentry.captureError(err);
          dictMap[dictName] = [];
        }
      })
    );
  }

  function onProjectChange() {
    queryForm.pageNum = 1;
    queryList();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryList();
  }

  function resetQuery() {
    const projectId = queryForm.projectId;
    const pageSize = queryForm.pageSize;
    Object.assign(queryForm, { ...queryFormState, projectId, pageSize });
    queryList();
  }

  async function queryList() {
    if (!queryForm.projectId || !api.value) {
      tableData.value = [];
      total.value = 0;
      return;
    }
    tableLoading.value = true;
    try {
      const res = await api.value.query(queryForm);
      tableData.value = res.data?.list || [];
      total.value = res.data?.total || 0;
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      tableLoading.value = false;
    }
  }

  function resetForm() {
    Object.keys(formData).forEach((key) => delete formData[key]);
    for (const field of props.config.fields) {
      formData[field.key] = field.defaultValue;
    }
    formData.projectId = queryForm.projectId;
  }

  function openCreate() {
    drawerMode.value = 'create';
    resetForm();
    drawerVisible.value = true;
  }

  function openView(record) {
    drawerMode.value = 'view';
    loadDetail(record[props.config.idKey]);
  }

  function openEdit(record) {
    drawerMode.value = 'edit';
    loadDetail(record[props.config.idKey]);
  }

  async function loadDetail(id) {
    resetForm();
    drawerVisible.value = true;
    detailLoading.value = true;
    try {
      const res = await api.value.detail({ id });
      Object.assign(formData, res.data || {});
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      detailLoading.value = false;
    }
  }

  function isReadonlyField(field) {
    return drawerMode.value === 'view' || field.readonly || (drawerMode.value === 'edit' && field.createOnly);
  }

  function closeDrawer() {
    drawerVisible.value = false;
  }

  function buildPayload() {
    const payload = {};
    if (drawerMode.value === 'create') {
      payload.projectId = queryForm.projectId;
    } else {
      payload[props.config.idKey] = formData[props.config.idKey];
    }
    for (const field of props.config.fields) {
      if (field.readonly || field.createOnly) {
        continue;
      }
      if (drawerMode.value === 'create' && field.key === props.config.idKey) {
        continue;
      }
      payload[field.key] = formData[field.key];
    }
    return payload;
  }

  async function submitForm() {
    try {
      await formRef.value.validateFields();
      saving.value = true;
      const payload = buildPayload();
      if (drawerMode.value === 'create') {
        await api.value.add(payload);
      } else {
        await api.value.update(payload);
      }
      message.success('保存成功');
      closeDrawer();
      queryList();
    } catch (err) {
      if (err?.errorFields) {
        message.warning('请检查表单信息');
      } else {
        smartSentry.captureError(err);
      }
    } finally {
      saving.value = false;
    }
  }

  function confirmArchive(record) {
    Modal.confirm({
      title: `归档${props.config.title}`,
      content: `确认归档「${record[props.config.nameKey] || '未命名'}」？`,
      okText: '归档',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => archiveAsset(record[props.config.idKey]),
    });
  }

  async function archiveAsset(id) {
    try {
      await api.value.archive({ id });
      message.success('归档成功');
      queryList();
    } catch (err) {
      smartSentry.captureError(err);
    }
  }
</script>

<style scoped lang="less">
  .novel-asset-page {
    padding: 16px;
  }

  .asset-alert {
    margin-bottom: 12px;
  }

  .asset-name-link {
    padding: 0;
  }

  .asset-summary {
    display: inline-block;
    max-width: 520px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  @media (max-width: 768px) {
    .novel-asset-page {
      padding: 12px;
    }
  }
</style>

