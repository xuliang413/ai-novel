<!--
  * AI 小说 M1 写作审阅
  *
  * 支持完整 M1 写作闭环：
  *   启动写作 → 正文审阅 → GraphPatch 确认 → 发布
  *   恢复 / 撤销
-->
<template>
  <a-spin :spinning="loading" tip="处理中...">
    <a-row :gutter="[16, 16]">
      <!-- 左列 -->
      <a-col :span="12">
        <!-- 卡1: 启动写作 -->
        <a-card size="small" title="启动新章节" :bordered="true">
          <a-form layout="vertical" :model="startForm">
            <a-row :gutter="[12, 0]">
              <a-col :span="8">
                <a-form-item label="项目 ID">
                  <a-input-number v-model:value="startForm.projectId" :min="1" style="width:100%" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="章节序号">
                  <a-input-number v-model:value="startForm.chapterNo" :min="1" style="width:100%" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="POV 角色">
                  <a-input v-model:value="startForm.pov" placeholder="可选" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="章节目标">
              <a-textarea v-model:value="startForm.chapterGoal" placeholder="可选，如：让林澈在旧钟楼发现逆行钟声的新证据" :rows="2" />
            </a-form-item>
            <a-button type="primary" @click="handleStart" :loading="starting">
              <template #icon><EditOutlined /></template>
              启动写作
            </a-button>
            <a-divider type="vertical" />
            <a-button @click="handleRecover" :loading="recovering">
              <template #icon><ReloadOutlined /></template>
              恢复最近会话
            </a-button>
          </a-form>
        </a-card>

        <!-- 卡2: 章节意图与上下文 -->
        <a-card size="small" title="章节意图与上下文" :bordered="true" v-if="chapterIntent || contextPreview">
          <a-descriptions v-if="chapterIntent" size="small" :column="1" bordered>
            <a-descriptions-item label="POV">{{ chapterIntent.pov }}</a-descriptions-item>
            <a-descriptions-item label="章节目标">{{ chapterIntent.chapterGoal }}</a-descriptions-item>
            <a-descriptions-item label="候选角色" v-if="chapterIntent.candidateCharacters">
              <a-tag v-for="c in chapterIntent.candidateCharacters" :key="c.id" color="blue">{{ c.name }}({{ c.type }})</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="候选地点" v-if="chapterIntent.candidateLocations">
              <a-tag v-for="l in chapterIntent.candidateLocations" :key="l.id" color="green">{{ l.name }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="目标线索" v-if="chapterIntent.targetClues">
              <a-tag v-for="cl in chapterIntent.targetClues" :key="cl.id" color="orange">{{ cl.name }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>

          <a-collapse v-if="contextPreview" :bordered="false" style="margin-top:8px">
            <a-collapse-panel key="1" header="上下文预览（Token 估算: {{ contextPreview.estimatedTokens }}）">
              <div v-if="contextPreview.projectSummary" style="margin-bottom:8px">
                <strong>项目摘要：</strong>{{ contextPreview.projectSummary }}
              </div>
              <div v-if="contextPreview.characterCards && contextPreview.characterCards.length">
                <strong>角色卡：</strong>
                <div v-for="item in contextPreview.characterCards" :key="item.id" style="margin:4px 0;padding-left:12px">
                  <a-tag>{{ item.type }}</a-tag> {{ item.name }}：{{ item.summary }}
                </div>
              </div>
              <div v-if="contextPreview.clueCards && contextPreview.clueCards.length">
                <strong>线索卡：</strong>
                <div v-for="item in contextPreview.clueCards" :key="item.id" style="margin:4px 0;padding-left:12px">
                  <a-tag color="orange">{{ item.type }}</a-tag> {{ item.name }}：{{ item.summary }}
                </div>
              </div>
              <div v-if="contextPreview.locationCards && contextPreview.locationCards.length">
                <strong>地点卡：</strong>
                <div v-for="item in contextPreview.locationCards" :key="item.id" style="margin:4px 0;padding-left:12px">
                  <a-tag color="green">{{ item.type }}</a-tag> {{ item.name }}：{{ item.summary }}
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>

        <!-- 卡3: 正文审阅 & 质检 -->
        <a-card size="small" :title="'正文草稿' + (chapter ? ' — ' + chapter.title : '')" :bordered="true" v-if="chapter">
          <template #extra>
            <a-tag v-if="chapter.status" :color="statusColor(chapter.status)">{{ chapter.status }}</a-tag>
            <a-tag v-if="sessionStatus" :color="sessionStatusColor(sessionStatus)">{{ sessionStatus }}</a-tag>
          </template>

          <div v-if="qualityCheck" style="margin-bottom:12px">
            <a-descriptions size="small" :column="3" bordered>
              <a-descriptions-item label="字数">{{ qualityCheck.wordCount }}</a-descriptions-item>
              <a-descriptions-item label="POV 提及">
                <CheckCircleOutlined v-if="qualityCheck.povMentioned" style="color:#52c41a" />
                <CloseCircleOutlined v-else style="color:#ff4d4f" />
              </a-descriptions-item>
              <a-descriptions-item label="章末推进">
                <CheckCircleOutlined v-if="qualityCheck.hasChapterEnding" style="color:#52c41a" />
                <CloseCircleOutlined v-else style="color:#ff4d4f" />
              </a-descriptions-item>
            </a-descriptions>
            <a-alert v-if="qualityCheck.warnings && qualityCheck.warnings.length" type="warning" style="margin-top:8px">
              <template #message>
                <span v-for="(w, i) in qualityCheck.warnings" :key="i">{{ w }}<br v-if="i < qualityCheck.warnings.length-1" /></span>
              </template>
            </a-alert>
          </div>

          <div style="max-height:300px;overflow:auto;background:#fafafa;padding:12px;border-radius:4px;white-space:pre-wrap;font-size:13px;line-height:1.8">
            {{ chapter.content || '（无内容）' }}
          </div>
        </a-card>

        <!-- 卡4: GraphPatch 确认 -->
        <a-card size="small" title="图谱变更确认" :bordered="true" v-if="graphPatch && graphPatch.operations && graphPatch.operations.length">
          <template #extra>
            <a-tag v-if="graphPatch.warnings && graphPatch.warnings.length" color="warning">{{ graphPatch.warnings[0] }}</a-tag>
          </template>

          <a-table
            :data-source="graphPatch.operations"
            :columns="patchColumns"
            :pagination="false"
            size="small"
            row-key="operationId"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'confidence'">
                <a-tag :color="record.confidence === 'HIGH' ? 'green' : record.confidence === 'MEDIUM' ? 'orange' : 'red'">{{ record.confidence }}</a-tag>
              </template>
              <template v-if="column.key === 'change'">
                <span v-if="record.operationType === 'UPDATE_CHAPTER_SUMMARY' || record.operationType === 'RESTORE_CHAPTER_SUMMARY'">
                  {{ record.beforeStatus }} → {{ record.afterStatus }}
                </span>
                <span v-else-if="record.operationType === 'ADVANCE_CLUE' || record.operationType === 'RESTORE_CLUE'">
                  {{ record.targetName }}: {{ record.beforeStatus }} → {{ record.afterStatus }}
                </span>
                <span v-else>
                  {{ record.targetName }} 出场
                </span>
              </template>
              <template v-if="column.key === 'selected'">
                <a-switch v-model:checked="record.selected" :disabled="record.validationStatus === 'LOW_CONFIDENCE'" size="small" />
              </template>
            </template>
          </a-table>

          <div style="margin-top:12px;text-align:right">
            <a-space>
              <a-button @click="handleBackToReview" v-if="sessionId && sessionStatus === 'PATCH_REVIEW'">
                返回审阅
              </a-button>
              <a-button type="primary" @click="handleConfirmPatch" :loading="confirming" v-if="sessionId && sessionStatus === 'PATCH_REVIEW'">
                确认并发布
              </a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>

      <!-- 右列 -->
      <a-col :span="12">
        <!-- 当前状态卡 -->
        <a-card size="small" title="当前会话" :bordered="true">
          <a-descriptions size="small" :column="2" bordered>
            <a-descriptions-item label="会话 ID">{{ sessionId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="会话状态">
              <a-tag v-if="sessionStatus" :color="sessionStatusColor(sessionStatus)">{{ sessionStatus }}</a-tag>
              <span v-else>-</span>
            </a-descriptions-item>
            <a-descriptions-item label="章节 ID">{{ (chapter && chapter.chapterId) || '-' }}</a-descriptions-item>
            <a-descriptions-item label="章节序号">{{ (chapter && chapter.chapterNo) || '-' }}</a-descriptions-item>
            <a-descriptions-item label="章节状态">
              <a-tag v-if="chapter && chapter.status" :color="statusColor(chapter.status)">{{ chapter.status }}</a-tag>
              <span v-else>-</span>
            </a-descriptions-item>
            <a-descriptions-item label="操作批次">{{ sessionOperationBatchId || '-' }}</a-descriptions-item>
          </a-descriptions>

          <a-divider type="horizontal" />

          <a-space>
            <a-button type="primary" ghost @click="handleRecover" :loading="recovering" v-if="!sessionId">恢复最近会话</a-button>
            <a-button type="primary" ghost @click="handleRecover" :loading="recovering" v-if="sessionId">重新加载</a-button>
            <a-button danger @click="handleUndo" :loading="undoing">撤销最近图谱变更</a-button>
          </a-space>
          <div style="margin-top:8px;color:#888;font-size:12px">
            撤销后章节状态将变为 PENDING_GRAPH_UPDATE，正文保留。
          </div>
        </a-card>

        <!-- 操作日志 -->
        <a-card size="small" title="操作说明" :bordered="true" style="margin-top:16px">
          <a-steps direction="vertical" size="small" :current="currentStep">
            <a-step title="启动写作" description="填写项目、章节、POV 和目标，生成草稿与上下文预览" />
            <a-step title="正文审阅" description="检查生成内容与质检报告，确认后点"通过审阅"" />
            <a-step title="图谱变更确认" description="勾选要执行的操作，确认后将写入 Neo4j 并发布章节" />
            <a-step title="已发布" description="图谱变更已应用，可通过撤销回退" />
          </a-steps>
        </a-card>
      </a-col>
    </a-row>
  </a-spin>
</template>

<script setup>
import { ref, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { EditOutlined, ReloadOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons-vue';
import { novelWriteApi } from '/@/api/business/novel/novel-write-api';

const loading = ref(false);
const starting = ref(false);
const confirming = ref(false);
const recovering = ref(false);
const undoing = ref(false);

const startForm = ref({
  projectId: 2,
  chapterNo: null,
  pov: '林澈',
  chapterGoal: '让林澈在旧钟楼发现逆行钟声的新证据',
});

const sessionId = ref(null);
const sessionStatus = ref('');
const sessionOperationBatchId = ref('');
const chapter = ref(null);
const chapterIntent = ref(null);
const contextPreview = ref(null);
const qualityCheck = ref(null);
const graphPatch = ref(null);
const inversePatch = ref(null);

const patchColumns = [
  { title: '操作类型', dataIndex: 'operationType', key: 'operationType', width: 180 },
  { title: '目标', dataIndex: 'targetName', key: 'targetName' },
  { title: '变更', key: 'change', width: 200 },
  { title: '置信度', dataIndex: 'confidence', key: 'confidence', width: 80 },
  { title: '勾选', key: 'selected', width: 60 },
];

const currentStep = computed(() => {
  if (!sessionStatus.value) return 0;
  if (sessionStatus.value === 'CONTENT_REVIEW') return 1;
  if (sessionStatus.value === 'PATCH_REVIEW') return 2;
  if (sessionStatus.value === 'SUCCESS' || sessionStatus.value === 'APPLYING_PATCH') return 3;
  return 0;
});

function statusColor(status) {
  const map = { DRAFT: 'default', PENDING_GRAPH_CONFIRM: 'orange', PENDING_GRAPH_UPDATE: 'warning', PUBLISHED: 'green', INTERRUPTED_DRAFT: 'red' };
  return map[status] || 'default';
}

function sessionStatusColor(status) {
  const map = { GENERATING: 'processing', CONTENT_REVIEW: 'blue', EXTRACTING_PATCH: 'purple', PATCH_REVIEW: 'orange', APPLYING_PATCH: 'processing', SUCCESS: 'green', FAILED: 'red', INTERRUPTED: 'red' };
  return map[status] || 'default';
}

function setResult(data) {
  sessionId.value = data.sessionId || null;
  sessionStatus.value = data.sessionStatus || '';
  sessionOperationBatchId.value = data.operationBatchId || '';
  chapter.value = data.chapter || null;
  chapterIntent.value = data.chapterIntent || null;
  contextPreview.value = data.contextPreview || null;
  qualityCheck.value = data.qualityCheck || null;
  graphPatch.value = data.graphPatch || null;
  inversePatch.value = data.inversePatch || null;
}

async function handleStart() {
  if (!startForm.value.projectId || !startForm.value.chapterNo) {
    message.warning('请填写项目 ID 和章节序号');
    return;
  }
  starting.value = true;
  loading.value = true;
  try {
    const res = await novelWriteApi.start({
      projectId: startForm.value.projectId,
      chapterNo: startForm.value.chapterNo,
      pov: startForm.value.pov || undefined,
      chapterGoal: startForm.value.chapterGoal || undefined,
    });
    if (res && res.ok) {
      setResult(res.data);
      message.success('章节草稿已生成，请审阅正文。');
    } else {
      message.error(res?.msg || '启动失败');
    }
  } catch (e) {
    message.error('请求失败：' + (e.message || e));
  } finally {
    starting.value = false;
    loading.value = false;
  }
}

async function handleRecover() {
  if (!startForm.value.projectId) {
    message.warning('请填写项目 ID');
    return;
  }
  recovering.value = true;
  loading.value = true;
  try {
    const res = await novelWriteApi.recover({
      projectId: startForm.value.projectId,
      chapterNo: startForm.value.chapterNo || null,
    });
    if (res && res.ok) {
      setResult(res.data);
      message.success('已恢复最近会话。');
    } else {
      message.error(res?.msg || '恢复失败');
    }
  } catch (e) {
    message.error('请求失败：' + (e.message || e));
  } finally {
    recovering.value = false;
    loading.value = false;
  }
}

async function handleBackToReview() {
  if (!sessionId.value) return;
  loading.value = true;
  try {
    const res = await novelWriteApi.backToContentReview({ sessionId: sessionId.value });
    if (res && res.ok) {
      setResult(res.data);
      message.success('已返回正文审阅，候选 GraphPatch 已丢弃。');
    } else {
      message.error(res?.msg || '返回失败');
    }
  } catch (e) {
    message.error('请求失败：' + (e.message || e));
  } finally {
    loading.value = false;
  }
}

async function handleConfirmPatch() {
  if (!sessionId.value) return;
  const selectedOps = graphPatch.value?.operations?.filter(op => op.selected) || [];
  if (selectedOps.length === 0) {
    message.warning('请至少勾选一项操作');
    return;
  }

  Modal.confirm({
    title: '确认发布',
    content: `将执行 ${selectedOps.length} 项图谱操作并发布章节，确认？`,
    okText: '确认发布',
    cancelText: '取消',
    onOk: async () => {
      confirming.value = true;
      loading.value = true;
      try {
        const res = await novelWriteApi.confirmPatch({ sessionId: sessionId.value });
        if (res && res.ok) {
          chapter.value = res.data;
          sessionStatus.value = 'SUCCESS';
          message.success('章节已发布，图谱变更已写入。');
        } else {
          message.error(res?.msg || '发布失败');
        }
      } catch (e) {
        message.error('请求失败：' + (e.message || e));
      } finally {
        confirming.value = false;
        loading.value = false;
      }
    },
  });
}

async function handleUndo() {
  if (!startForm.value.projectId) {
    message.warning('请填写项目 ID');
    return;
  }
  Modal.confirm({
    title: '确认撤销',
    content: '将撤销最近一次图谱变更（正文保留），确认？',
    okText: '确认撤销',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      undoing.value = true;
      loading.value = true;
      try {
        const res = await novelWriteApi.undo({ projectId: startForm.value.projectId });
        if (res && res.ok) {
          message.success('图谱变更已撤销，章节状态：' + (res.data.chapterStatus || '已更新'));
          if (chapter.value) {
            chapter.value.status = res.data.chapterStatus;
          }
        } else {
          message.warning(res?.msg || '撤销失败');
        }
      } catch (e) {
        message.error('请求失败：' + (e.message || e));
      } finally {
        undoing.value = false;
        loading.value = false;
      }
    },
  });
}
</script>
