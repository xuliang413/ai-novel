<template>
  <div class="novel-write-page">
    <div class="workbench-header">
      <div>
        <div class="workbench-title">写作工作台</div>
        <div class="workbench-subtitle">按方案文档的安全写作闭环执行：生成、正文审阅、图谱变更确认。</div>
      </div>
      <a-space wrap>
        <a-tag v-if="selectedProjectName" color="blue">{{ selectedProjectName }}</a-tag>
        <a-tag v-if="sessionStatus" :color="sessionStatusColor(sessionStatus)">{{ sessionStatusText(sessionStatus) }}</a-tag>
        <a-button @click="loadProjects" :loading="projectLoading">
          <template #icon><ReloadOutlined /></template>
          刷新项目
        </a-button>
      </a-space>
    </div>

    <a-spin :spinning="loading" tip="处理中...">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="8">
          <a-card size="small" title="启动写作" :bordered="false">
            <a-form layout="vertical" :model="startForm">
              <a-form-item label="小说项目">
                <a-select
                  v-model:value="startForm.projectId"
                  show-search
                  allow-clear
                  :loading="projectLoading"
                  :filter-option="filterProjectOption"
                  placeholder="请选择小说项目"
                  @change="handleProjectChange"
                >
                  <a-select-option v-for="project in projectOptions" :key="project.projectId" :value="project.projectId" :label="project.projectName">
                    {{ project.projectName }} · #{{ project.projectId }}
                  </a-select-option>
                </a-select>
              </a-form-item>

              <a-row :gutter="12">
                <a-col :span="12">
                  <a-form-item label="章节序号">
                    <a-input-number v-model:value="startForm.chapterNo" :min="1" style="width: 100%" placeholder="留空自动" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="POV 角色">
                    <a-input v-model:value="startForm.pov" placeholder="留空由后端推断" />
                  </a-form-item>
                </a-col>
              </a-row>

              <a-form-item label="章节目标">
                <a-textarea v-model:value="startForm.chapterGoal" :rows="3" placeholder="本章要推进的剧情目标" />
              </a-form-item>

              <a-form-item label="候选角色">
                <a-select v-model:value="startForm.candidateCharacters" mode="tags" placeholder="输入角色名后回车" />
              </a-form-item>

              <a-form-item label="目标线索">
                <a-select v-model:value="startForm.targetClues" mode="tags" placeholder="输入线索名后回车" />
              </a-form-item>

              <a-form-item label="候选地点">
                <a-select v-model:value="startForm.candidateLocations" mode="tags" placeholder="输入地点后回车" />
              </a-form-item>

              <a-form-item label="临时要求">
                <a-select v-model:value="startForm.extraInstructions" mode="tags" placeholder="如：本章不要揭开真相" />
              </a-form-item>

              <a-space wrap>
                <a-button type="primary" @click="handleStart" :loading="starting">
                  <template #icon><PlayCircleOutlined /></template>
                  开始写作
                </a-button>
                <a-button @click="handleStartStream" :loading="streaming" :disabled="streaming">
                  <template #icon><ThunderboltOutlined /></template>
                  流式写作
                </a-button>
                <a-button v-if="streaming" danger @click="cancelStream">
                  <template #icon><StopOutlined /></template>
                  取消流式
                </a-button>
                <a-button @click="handleRecover" :loading="recovering" :disabled="!startForm.projectId">
                  <template #icon><ReloadOutlined /></template>
                  恢复会话
                </a-button>
                <a-button danger @click="handleUndo" :loading="undoing" :disabled="!startForm.projectId">
                  <template #icon><UndoOutlined /></template>
                  撤销图谱
                </a-button>
              </a-space>
            </a-form>
          </a-card>

          <a-card size="small" title="最近章节" :bordered="false" class="workbench-card">
            <a-table
              size="small"
              :data-source="chapterList"
              :columns="chapterColumns"
              :pagination="false"
              :loading="chapterLoading"
              row-key="chapterId"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'chapter'">
                  <a-button type="link" class="chapter-link" @click="fillChapter(record)">
                    第 {{ record.chapterNo }} 章
                  </a-button>
                </template>
                <template v-if="column.key === 'status'">
                  <a-tag :color="chapterStatusColor(record.status)">{{ chapterStatusText(record.status) }}</a-tag>
                </template>
              </template>
            </a-table>
            <a-empty v-if="!chapterLoading && chapterList.length === 0" description="暂无章节数据" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="16">
          <a-card size="small" title="当前会话" :bordered="false">
            <a-steps :current="currentStep" size="small" responsive>
              <a-step title="启动写作" />
              <a-step title="正文审阅" />
              <a-step title="图谱确认" />
              <a-step title="完成发布" />
            </a-steps>

            <a-descriptions size="small" :column="{ xs: 1, md: 2, xl: 4 }" bordered class="session-summary">
              <a-descriptions-item label="会话 ID">{{ sessionId || '-' }}</a-descriptions-item>
              <a-descriptions-item label="章节 ID">{{ chapter?.chapterId || '-' }}</a-descriptions-item>
              <a-descriptions-item label="章节序号">{{ chapter?.chapterNo || '-' }}</a-descriptions-item>
              <a-descriptions-item label="操作批次">{{ operationBatchId || graphPatch?.operationBatchId || '-' }}</a-descriptions-item>
            </a-descriptions>
          </a-card>

          <a-row :gutter="[16, 16]" class="workbench-card">
            <a-col :xs="24" :lg="12">
              <a-card size="small" title="写作意图" :bordered="false">
                <a-descriptions v-if="chapterIntent" size="small" :column="1" bordered>
                  <a-descriptions-item label="POV">{{ chapterIntent.pov || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="章节目标">{{ chapterIntent.chapterGoal || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="候选角色">
                    <a-space wrap>
                      <a-tag v-for="item in normalizeList(chapterIntent.candidateCharacters)" :key="candidateKey(item)" color="blue">
                        {{ candidateName(item) }}
                      </a-tag>
                    </a-space>
                  </a-descriptions-item>
                  <a-descriptions-item label="目标线索">
                    <a-space wrap>
                      <a-tag v-for="item in normalizeList(chapterIntent.targetClues)" :key="candidateKey(item)" color="orange">
                        {{ candidateName(item) }}
                      </a-tag>
                    </a-space>
                  </a-descriptions-item>
                  <a-descriptions-item label="候选地点">
                    <a-space wrap>
                      <a-tag v-for="item in normalizeList(chapterIntent.candidateLocations)" :key="candidateKey(item)" color="green">
                        {{ candidateName(item) }}
                      </a-tag>
                    </a-space>
                  </a-descriptions-item>
                </a-descriptions>
                <a-empty v-else description="启动或恢复会话后展示写作意图" />
              </a-card>
            </a-col>

            <a-col :xs="24" :lg="12">
              <a-card size="small" title="上下文预览" :bordered="false">
                <template v-if="contextPreview">
                  <div class="context-summary">
                    <span>Token 估算：{{ contextPreview.estimatedTokens ?? '-' }}</span>
                    <span>裁剪条目：{{ contextPreview.truncatedItems ?? 0 }}</span>
                  </div>
                  <a-alert v-if="contextPreview.projectSummary" type="info" :message="contextPreview.projectSummary" show-icon />
                  <a-collapse ghost class="context-collapse">
                    <a-collapse-panel key="characters" header="角色卡片">
                      <context-item-list :items="contextPreview.characterCards" />
                    </a-collapse-panel>
                    <a-collapse-panel key="clues" header="线索卡片">
                      <context-item-list :items="contextPreview.clueCards" />
                    </a-collapse-panel>
                    <a-collapse-panel key="locations" header="地点卡片">
                      <context-item-list :items="contextPreview.locationCards" />
                    </a-collapse-panel>
                  </a-collapse>
                </template>
                <a-empty v-else description="启动或恢复会话后展示上下文快照" />
              </a-card>
            </a-col>
          </a-row>

          <a-card size="small" title="正文审阅" :bordered="false" class="workbench-card">
            <template #extra>
              <a-space wrap>
                <a-tag v-if="chapter?.status" :color="chapterStatusColor(chapter.status)">{{ chapterStatusText(chapter.status) }}</a-tag>
                <a-button
                  type="primary"
                  :disabled="!canPassContentReview"
                  :loading="passingContent"
                  @click="handlePassContentReview"
                >
                  <template #icon><CheckCircleOutlined /></template>
                  通过正文审阅
                </a-button>
              </a-space>
            </template>

            <a-alert
              v-if="qualityWarnings.length"
              type="warning"
              show-icon
              class="quality-alert"
              :message="qualityWarnings.join('；')"
            />
            <a-row v-if="qualityCheck" :gutter="[12, 12]" class="quality-grid">
              <a-col :xs="12" :md="6">
                <div class="metric-cell">
                  <span class="metric-value">{{ qualityCheck.wordCount ?? '-' }}</span>
                  <span class="metric-label">字数</span>
                </div>
              </a-col>
              <a-col :xs="12" :md="6">
                <div class="metric-cell">
                  <span class="metric-value">{{ qualityCheck.povMentioned ? '是' : '否' }}</span>
                  <span class="metric-label">POV 出现</span>
                </div>
              </a-col>
              <a-col :xs="12" :md="6">
                <div class="metric-cell">
                  <span class="metric-value">{{ qualityCheck.hasChapterEnding ? '是' : '否' }}</span>
                  <span class="metric-label">章末推进</span>
                </div>
              </a-col>
              <a-col :xs="12" :md="6">
                <div class="metric-cell">
                  <span class="metric-value">{{ normalizeList(qualityCheck.newEntityHints).length }}</span>
                  <span class="metric-label">新实体提示</span>
                </div>
              </a-col>
            </a-row>

            <a-form layout="vertical" class="chapter-editor">
              <a-form-item label="章节标题">
                <a-input v-model:value="chapterEdit.title" :disabled="!chapter" placeholder="生成后可调整标题" />
              </a-form-item>
              <a-form-item label="章节摘要">
                <a-textarea v-model:value="chapterEdit.summary" :disabled="!chapter" :rows="3" placeholder="生成后可调整摘要" />
              </a-form-item>
              <a-form-item label="正文内容">
                <a-textarea v-model:value="chapterEdit.content" :disabled="!chapter" :rows="14" placeholder="生成后在这里审阅和微调正文" />
              </a-form-item>
            </a-form>
          </a-card>

          <a-card size="small" title="图谱变更确认" :bordered="false" class="workbench-card">
            <template #extra>
              <a-space wrap>
                <a-tag v-if="graphPatch?.status" color="purple">{{ graphPatch.status }}</a-tag>
                <span class="selected-count">已选 {{ selectedOperationIds.length }} 条</span>
                <a-button @click="handleBackToReview" :disabled="!canBackToReview">
                  <template #icon><ArrowLeftOutlined /></template>
                  返回审阅
                </a-button>
                <a-button type="primary" @click="handleConfirmPatch" :disabled="!canConfirmPatch" :loading="confirmingPatch">
                  <template #icon><SaveOutlined /></template>
                  确认更新
                </a-button>
              </a-space>
            </template>

            <a-alert
              v-for="warning in normalizeList(graphPatch?.warnings)"
              :key="warning"
              type="warning"
              show-icon
              class="quality-alert"
              :message="warning"
            />

            <a-table
              size="small"
              :data-source="patchOperations"
              :columns="patchColumns"
              :pagination="false"
              :row-key="operationRowKey"
              :scroll="{ x: 1100 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'target'">
                  <div class="operation-target">
                    <span>{{ record.targetName || '-' }}</span>
                    <a-typography-text type="secondary">{{ record.targetType || '-' }}</a-typography-text>
                  </div>
                </template>
                <template v-if="column.key === 'change'">
                  <div class="operation-change">
                    <span>{{ operationChangeText(record) }}</span>
                    <a-typography-text v-if="record.evidence" type="secondary">{{ record.evidence }}</a-typography-text>
                  </div>
                </template>
                <template v-if="column.key === 'riskLevel'">
                  <a-tag :color="riskColor(record.riskLevel)">{{ record.riskLevel || '-' }}</a-tag>
                </template>
                <template v-if="column.key === 'confidence'">
                  <a-tag :color="confidenceColor(record.confidence)">{{ record.confidence || '-' }}</a-tag>
                </template>
                <template v-if="column.key === 'validationStatus'">
                  <a-tag :color="validationColor(record.validationStatus)">{{ record.validationStatus || '-' }}</a-tag>
                </template>
                <template v-if="column.key === 'conflictResolution'">
                  <a-select
                    v-if="record.validationStatus === 'CONFLICT'"
                    v-model:value="record.conflictResolution"
                    size="small"
                    style="width: 120px"
                    @change="handleConflictResolutionChange(record)"
                  >
                    <a-select-option value="SKIP">跳过</a-select-option>
                    <a-select-option value="FORCE">强制执行</a-select-option>
                    <a-select-option value="REVIEW">回到审阅</a-select-option>
                  </a-select>
                  <span v-else>-</span>
                </template>
                <template v-if="column.key === 'selected'">
                  <a-switch v-model:checked="record.selected" :disabled="isOperationDisabled(record)" size="small" />
                </template>
              </template>
            </a-table>
            <a-empty v-if="patchOperations.length === 0" description="正文审阅通过后展示 GraphPatch" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup>
  import { computed, defineComponent, h, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    ArrowLeftOutlined,
    CheckCircleOutlined,
    PlayCircleOutlined,
    ReloadOutlined,
    SaveOutlined,
    StopOutlined,
    ThunderboltOutlined,
    UndoOutlined,
  } from '@ant-design/icons-vue';
  import { novelChapterApi } from '/@/api/business/novel/novel-chapter-api';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { novelWriteApi } from '/@/api/business/novel/novel-write-api';
  import LocalStorageKeyConst from '/@/constants/local-storage-key-const';
  import { localRead } from '/@/utils/local-util';

  const ContextItemList = defineComponent({
    name: 'ContextItemList',
    props: {
      items: {
        type: Array,
        default: () => [],
      },
    },
    setup(props) {
      return () => {
        if (!props.items || props.items.length === 0) {
          return h('div', { class: 'context-empty' }, '暂无数据');
        }
        return h(
          'div',
          { class: 'context-list' },
          props.items.map((item, index) =>
            h('div', { class: 'context-item', key: item.id || item.name || index }, [
              h('div', { class: 'context-item-title' }, item.name || item.title || '-'),
              h('div', { class: 'context-item-summary' }, item.summary || item.description || ''),
            ])
          )
        );
      };
    },
  });

  const loading = ref(false);
  const projectLoading = ref(false);
  const chapterLoading = ref(false);
  const starting = ref(false);
  const recovering = ref(false);
  const undoing = ref(false);
  const passingContent = ref(false);
  const confirmingPatch = ref(false);
  const streaming = ref(false);

  const projectOptions = ref([]);
  const chapterList = ref([]);
  let streamSocket = null;

  const startForm = reactive({
    projectId: undefined,
    chapterNo: undefined,
    pov: '',
    chapterGoal: '',
    candidateCharacters: [],
    targetClues: [],
    candidateLocations: [],
    extraInstructions: [],
  });

  const sessionId = ref();
  const sessionStatus = ref('');
  const operationBatchId = ref('');
  const chapter = ref();
  const chapterIntent = ref();
  const contextPreview = ref();
  const qualityCheck = ref();
  const graphPatch = ref();
  const inversePatch = ref();

  const chapterEdit = reactive({
    title: '',
    summary: '',
    content: '',
  });

  const chapterColumns = [
    { title: '章节', key: 'chapter', width: 90 },
    { title: '标题', dataIndex: 'title', ellipsis: true },
    { title: '状态', key: 'status', width: 110 },
  ];

  const patchColumns = [
    { title: '操作类型', dataIndex: 'operationType', key: 'operationType', width: 160 },
    { title: '目标', key: 'target', width: 180 },
    { title: '变化与证据', key: 'change', width: 360 },
    { title: '风险', dataIndex: 'riskLevel', key: 'riskLevel', width: 90 },
    { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 90 },
    { title: '校验', dataIndex: 'validationStatus', key: 'validationStatus', width: 120 },
    { title: '冲突处理', key: 'conflictResolution', width: 140 },
    { title: '执行', key: 'selected', width: 80 },
  ];

  const selectedProjectName = computed(() => {
    return projectOptions.value.find((item) => item.projectId === startForm.projectId)?.projectName || '';
  });

  const currentStep = computed(() => {
    if (!sessionStatus.value) {
      return 0;
    }
    if (sessionStatus.value === 'CONTENT_REVIEW') {
      return 1;
    }
    if (sessionStatus.value === 'PATCH_REVIEW') {
      return 2;
    }
    if (sessionStatus.value === 'SUCCESS' || sessionStatus.value === 'APPLYING_PATCH') {
      return 3;
    }
    return 0;
  });

  const qualityWarnings = computed(() => normalizeList(qualityCheck.value?.warnings));
  const patchOperations = computed(() => normalizeList(graphPatch.value?.operations));
  const selectedOperationIds = computed(() =>
    patchOperations.value.filter((item) => item.selected && !isOperationDisabled(item)).map((item) => item.operationId).filter(Boolean)
  );
  const conflictResolutions = computed(() => {
    const result = {};
    for (const item of patchOperations.value) {
      if (item.validationStatus === 'CONFLICT' && item.operationId && item.conflictResolution) {
        result[item.operationId] = item.conflictResolution;
      }
    }
    return result;
  });
  const canPassContentReview = computed(() => sessionId.value && chapter.value?.chapterId && sessionStatus.value === 'CONTENT_REVIEW');
  const canBackToReview = computed(() => sessionId.value && sessionStatus.value === 'PATCH_REVIEW');
  const canConfirmPatch = computed(() => sessionId.value && sessionStatus.value === 'PATCH_REVIEW' && selectedOperationIds.value.length > 0);

  function normalizeList(value) {
    return Array.isArray(value) ? value : [];
  }

  function compactList(value) {
    return normalizeList(value).map((item) => String(item).trim()).filter(Boolean);
  }

  function candidateName(item) {
    if (typeof item === 'string') {
      return item;
    }
    return item?.name || item?.targetName || item?.summary || '-';
  }

  function candidateKey(item) {
    if (typeof item === 'string') {
      return item;
    }
    return item?.id || item?.name || item?.targetName || JSON.stringify(item);
  }

  function filterProjectOption(input, option) {
    return String(option?.label || '').toLowerCase().includes(input.toLowerCase());
  }

  function operationRowKey(record, index) {
    return record.operationId || `${record.operationType || 'operation'}-${index}`;
  }

  function isOperationDisabled(record) {
    return record.validationStatus === 'BLOCKED';
  }

  function defaultOperationSelected(record) {
    if (isOperationDisabled(record)) {
      return false;
    }
    if (record.validationStatus === 'CONFLICT') {
      return record.conflictResolution === 'FORCE';
    }
    if (record.validationStatus === 'LOW_CONFIDENCE' || record.confidence === 'LOW' || record.riskLevel === 'HIGH') {
      return false;
    }
    return true;
  }

  function normalizePatch(patch) {
    if (!patch) {
      return null;
    }
    return {
      ...patch,
      operations: normalizeList(patch.operations).map((item) => ({
        ...item,
        conflictResolution: item.validationStatus === 'CONFLICT' ? item.conflictResolution || 'SKIP' : item.conflictResolution,
        selected: typeof item.selected === 'boolean' ? item.selected : defaultOperationSelected(item),
      })),
    };
  }

  function handleConflictResolutionChange(record) {
    if (record.conflictResolution === 'FORCE') {
      record.selected = true;
      return;
    }
    record.selected = false;
  }

  function setResult(data) {
    const result = data || {};
    sessionId.value = result.sessionId || undefined;
    sessionStatus.value = result.sessionStatus || '';
    operationBatchId.value = result.operationBatchId || result.graphPatch?.operationBatchId || '';
    chapter.value = result.chapter || undefined;
    chapterIntent.value = result.chapterIntent || undefined;
    contextPreview.value = result.contextPreview || undefined;
    qualityCheck.value = result.qualityCheck || undefined;
    graphPatch.value = normalizePatch(result.graphPatch);
    inversePatch.value = normalizePatch(result.inversePatch);

    chapterEdit.title = result.chapter?.title || '';
    chapterEdit.summary = result.chapter?.summary || '';
    chapterEdit.content = result.chapter?.content || '';
  }

  async function loadProjects() {
    projectLoading.value = true;
    try {
      const res = await novelProjectApi.query({ pageNum: 1, pageSize: 50 });
      projectOptions.value = res.data?.list || [];
      if (!startForm.projectId && projectOptions.value.length > 0) {
        startForm.projectId = projectOptions.value[0].projectId;
      }
      await loadChapters();
    } finally {
      projectLoading.value = false;
    }
  }

  async function loadChapters() {
    if (!startForm.projectId) {
      chapterList.value = [];
      return;
    }
    chapterLoading.value = true;
    try {
      const res = await novelChapterApi.query({ projectId: startForm.projectId, pageNum: 1, pageSize: 12 });
      chapterList.value = res.data?.list || [];
    } finally {
      chapterLoading.value = false;
    }
  }

  function handleProjectChange() {
    chapterList.value = [];
    loadChapters();
  }

  function fillChapter(record) {
    startForm.chapterNo = record.chapterNo;
    chapter.value = record;
    chapterEdit.title = record.title || '';
    chapterEdit.summary = record.summary || '';
    chapterEdit.content = record.content || '';
  }

  function buildStartPayload() {
    return {
      projectId: startForm.projectId,
      chapterNo: startForm.chapterNo || undefined,
      pov: startForm.pov || undefined,
      chapterGoal: startForm.chapterGoal || undefined,
      candidateCharacters: compactList(startForm.candidateCharacters),
      targetClues: compactList(startForm.targetClues),
      candidateLocations: compactList(startForm.candidateLocations),
      extraInstructions: compactList(startForm.extraInstructions),
    };
  }

  async function handleStart() {
    if (!startForm.projectId) {
      message.warning('请选择小说项目');
      return;
    }
    starting.value = true;
    loading.value = true;
    try {
      const res = await novelWriteApi.start(buildStartPayload());
      setResult(res.data);
      message.success('已生成正文草稿，请审阅内容');
      loadChapters();
    } finally {
      starting.value = false;
      loading.value = false;
    }
  }

  function handleStartStream() {
    if (!startForm.projectId) {
      message.warning('请选择小说项目');
      return;
    }
    if (!window.WebSocket) {
      message.error('当前浏览器不支持 WebSocket');
      return;
    }

    const token = localRead(LocalStorageKeyConst.USER_TOKEN);
    if (!token) {
      message.warning('登录 token 不存在，请重新登录后再试');
      return;
    }

    closeStreamSocket();
    streaming.value = true;
    sessionStatus.value = 'GENERATING';
    sessionId.value = undefined;
    operationBatchId.value = '';
    chapter.value = {};
    chapterIntent.value = undefined;
    contextPreview.value = undefined;
    qualityCheck.value = undefined;
    graphPatch.value = null;
    inversePatch.value = null;
    chapterEdit.title = '';
    chapterEdit.summary = '';
    chapterEdit.content = '';

    try {
      streamSocket = new WebSocket(buildWriteSocketUrl(token));
      streamSocket.onopen = () => {
        streamSocket.send(JSON.stringify({ action: 'start', ...buildStartPayload() }));
        message.success('流式写作已启动');
      };
      streamSocket.onmessage = handleStreamMessage;
      streamSocket.onerror = () => {
        message.error('流式写作连接异常');
      };
      streamSocket.onclose = () => {
        streaming.value = false;
        streamSocket = null;
      };
    } catch (err) {
      streaming.value = false;
      streamSocket = null;
      message.error(`流式写作连接失败：${err.message || err}`);
    }
  }

  function buildWriteSocketUrl(token) {
    const apiBase = import.meta.env.VITE_APP_API_URL || window.location.origin;
    const url = new URL(apiBase, window.location.origin);
    url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
    url.pathname = '/ws/novel/write';
    url.search = `token=${encodeURIComponent(token)}`;
    return url.toString();
  }

  function handleStreamMessage(event) {
    let data;
    try {
      data = JSON.parse(event.data);
    } catch (err) {
      message.warning('收到无法解析的流式消息');
      return;
    }

    if (data.sessionId) {
      sessionId.value = data.sessionId;
    }
    if (data.chapterId) {
      chapter.value = { ...(chapter.value || {}), chapterId: data.chapterId };
    }

    switch (data.eventType) {
      case 'token':
        chapterEdit.content += data.payload || '';
        break;
      case 'contentReady':
      case 'recoverDone':
        setResult(data.payload);
        streaming.value = false;
        closeStreamSocket();
        message.success(data.message || '流式生成完成，请审阅正文');
        loadChapters();
        break;
      case 'canceled':
        streaming.value = false;
        closeStreamSocket();
        message.warning(data.message || '流式生成已取消');
        break;
      case 'failed':
      case 'error':
        streaming.value = false;
        closeStreamSocket();
        message.error(data.message || '流式生成失败');
        break;
      case 'heartbeat':
        break;
      default:
        break;
    }
  }

  function cancelStream() {
    if (streamSocket && streamSocket.readyState === WebSocket.OPEN) {
      streamSocket.send(JSON.stringify({ action: 'cancel' }));
      message.info('已发送取消流式写作请求');
      return;
    }
    closeStreamSocket();
  }

  function closeStreamSocket() {
    if (!streamSocket) {
      streaming.value = false;
      return;
    }
    const socket = streamSocket;
    streamSocket = null;
    socket.onopen = null;
    socket.onmessage = null;
    socket.onerror = null;
    socket.onclose = null;
    if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
      socket.close();
    }
    streaming.value = false;
  }

  async function handleRecover() {
    if (!startForm.projectId) {
      message.warning('请选择小说项目');
      return;
    }
    recovering.value = true;
    loading.value = true;
    try {
      const res = await novelWriteApi.recover({
        projectId: startForm.projectId,
        chapterNo: startForm.chapterNo || null,
      });
      setResult(res.data);
      message.success('已恢复最近写作会话');
    } finally {
      recovering.value = false;
      loading.value = false;
    }
  }

  async function handlePassContentReview() {
    if (!canPassContentReview.value) {
      message.warning('当前没有可通过审阅的正文');
      return;
    }
    passingContent.value = true;
    loading.value = true;
    try {
      const res = await novelWriteApi.passContentReview({
        sessionId: sessionId.value,
        chapterId: chapter.value.chapterId,
        content: chapterEdit.content || undefined,
        title: chapterEdit.title || undefined,
        summary: chapterEdit.summary || undefined,
      });
      setResult(res.data);
      message.success('正文已通过审阅，请确认图谱变更');
      loadChapters();
    } finally {
      passingContent.value = false;
      loading.value = false;
    }
  }

  function handleConfirmPatch() {
    if (!canConfirmPatch.value) {
      message.warning('请至少选择一条可执行的图谱变更');
      return;
    }
    Modal.confirm({
      title: '确认更新图谱并发布章节',
      content: `将执行 ${selectedOperationIds.value.length} 条 GraphPatch 操作。确认后会写入图谱并发布章节。`,
      okText: '确认更新',
      cancelText: '取消',
      onOk: confirmPatch,
    });
  }

  async function confirmPatch() {
    confirmingPatch.value = true;
    loading.value = true;
    try {
      const payload = {
        sessionId: sessionId.value,
        operationIds: selectedOperationIds.value,
      };
      if (Object.keys(conflictResolutions.value).length) {
        payload.conflictResolutions = conflictResolutions.value;
      }
      const res = await novelWriteApi.confirmPatch(payload);
      chapter.value = res.data;
      sessionStatus.value = 'SUCCESS';
      graphPatch.value = graphPatch.value ? { ...graphPatch.value, status: 'APPLIED' } : null;
      message.success('图谱已更新，章节已发布');
      loadChapters();
    } finally {
      confirmingPatch.value = false;
      loading.value = false;
    }
  }

  async function handleBackToReview() {
    if (!canBackToReview.value) {
      return;
    }
    loading.value = true;
    try {
      const res = await novelWriteApi.backToContentReview({ sessionId: sessionId.value });
      setResult(res.data);
      message.success('已返回正文审阅，候选图谱变更已丢弃');
    } finally {
      loading.value = false;
    }
  }

  function handleUndo() {
    if (!startForm.projectId) {
      message.warning('请选择小说项目');
      return;
    }
    Modal.confirm({
      title: '撤销最近一次图谱变更',
      content: '该操作只撤销图谱变更，不删除章节正文。确认继续？',
      okText: '撤销',
      okType: 'danger',
      cancelText: '取消',
      onOk: undoPatch,
    });
  }

  async function undoPatch() {
    undoing.value = true;
    loading.value = true;
    try {
      const res = await novelWriteApi.undo({ projectId: startForm.projectId });
      if (chapter.value && res.data?.chapterStatus) {
        chapter.value.status = res.data.chapterStatus;
      }
      inversePatch.value = normalizePatch(res.data?.inversePatch);
      message.success(`已撤销批次：${res.data?.operationBatchId || '-'}`);
      loadChapters();
    } finally {
      undoing.value = false;
      loading.value = false;
    }
  }

  function sessionStatusText(status) {
    const map = {
      GENERATING: '生成中',
      CONTENT_REVIEW: '正文审阅',
      EXTRACTING_PATCH: '抽取图谱变更',
      PATCH_REVIEW: '图谱确认',
      APPLYING_PATCH: '应用图谱',
      SUCCESS: '已完成',
      FAILED: '失败',
      INTERRUPTED: '已中断',
    };
    return map[status] || status || '-';
  }

  function sessionStatusColor(status) {
    const map = {
      GENERATING: 'processing',
      CONTENT_REVIEW: 'blue',
      EXTRACTING_PATCH: 'purple',
      PATCH_REVIEW: 'orange',
      APPLYING_PATCH: 'processing',
      SUCCESS: 'green',
      FAILED: 'red',
      INTERRUPTED: 'red',
    };
    return map[status] || 'default';
  }

  function chapterStatusText(status) {
    const map = {
      DRAFT: '草稿',
      PENDING_GRAPH_CONFIRM: '待确认图谱',
      PENDING_GRAPH_UPDATE: '待同步图谱',
      PUBLISHED: '已发布',
      INTERRUPTED_DRAFT: '中断草稿',
      ARCHIVED: '已归档',
    };
    return map[status] || status || '-';
  }

  function chapterStatusColor(status) {
    const map = {
      DRAFT: 'default',
      PENDING_GRAPH_CONFIRM: 'orange',
      PENDING_GRAPH_UPDATE: 'warning',
      PUBLISHED: 'green',
      INTERRUPTED_DRAFT: 'red',
      ARCHIVED: 'default',
    };
    return map[status] || 'default';
  }

  function riskColor(value) {
    const map = { LOW: 'green', MEDIUM: 'orange', HIGH: 'red' };
    return map[value] || 'default';
  }

  function confidenceColor(value) {
    const map = { HIGH: 'green', MEDIUM: 'orange', LOW: 'red' };
    return map[value] || 'default';
  }

  function validationColor(value) {
    const map = { READY: 'green', LOW_CONFIDENCE: 'orange', CONFLICT: 'red', BLOCKED: 'red' };
    return map[value] || 'default';
  }

  function operationChangeText(record) {
    const values = [record.beforeStatus, record.beforeSummary, record.beforeValue, record.fromName].filter(Boolean);
    const nextValues = [record.afterStatus, record.afterSummary, record.afterValue, record.toName].filter(Boolean);
    if (values.length || nextValues.length) {
      return `${values.join(' / ') || '-'} -> ${nextValues.join(' / ') || '-'}`;
    }
    if (record.sourceName || record.toName) {
      return `${record.sourceName || record.targetName || '-'} -> ${record.toName || '-'}`;
    }
    return record.reason || '新增或标记出场';
  }

  onMounted(loadProjects);
  onBeforeUnmount(() => {
    closeStreamSocket();
  });
</script>

<style scoped lang="less">
  .novel-write-page {
    padding: 16px;
  }

  .workbench-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 16px;
  }

  .workbench-title {
    color: #1f2329;
    font-size: 20px;
    font-weight: 600;
    line-height: 28px;
  }

  .workbench-subtitle {
    margin-top: 4px;
    color: #646a73;
    font-size: 13px;
    line-height: 20px;
  }

  .workbench-card {
    margin-top: 16px;
  }

  .session-summary {
    margin-top: 16px;
  }

  .chapter-link {
    padding: 0;
  }

  .context-summary {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
    color: #646a73;
    font-size: 13px;
  }

  .context-collapse {
    margin-top: 8px;
  }

  .context-list {
    display: grid;
    gap: 8px;
  }

  .context-item {
    padding: 8px 10px;
    border: 1px solid #edf0f5;
    border-radius: 6px;
    background: #fafbfc;
  }

  .context-item-title {
    color: #1f2329;
    font-size: 13px;
    font-weight: 600;
  }

  .context-item-summary,
  .context-empty {
    margin-top: 4px;
    color: #646a73;
    font-size: 12px;
    line-height: 18px;
  }

  .quality-alert {
    margin-bottom: 12px;
  }

  .quality-grid {
    margin-bottom: 12px;
  }

  .metric-cell {
    display: flex;
    min-height: 64px;
    flex-direction: column;
    justify-content: center;
    padding: 10px 12px;
    border: 1px solid #edf0f5;
    border-radius: 6px;
    background: #fafbfc;
  }

  .metric-value {
    color: #1f2329;
    font-size: 18px;
    font-weight: 600;
    line-height: 24px;
  }

  .metric-label {
    margin-top: 4px;
    color: #646a73;
    font-size: 12px;
  }

  .chapter-editor :deep(.ant-input) {
    font-family: inherit;
    line-height: 1.8;
  }

  .selected-count {
    color: #646a73;
    font-size: 13px;
  }

  .operation-target,
  .operation-change {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  @media (max-width: 768px) {
    .novel-write-page {
      padding: 12px;
    }

    .workbench-header {
      flex-direction: column;
    }
  }
</style>
