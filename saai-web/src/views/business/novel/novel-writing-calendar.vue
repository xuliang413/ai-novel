<template>
  <div class="novel-writing-calendar-page">
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
            @change="loadCalendar"
          >
            <a-select-option v-for="project in projectOptions" :key="project.projectId" :value="project.projectId" :label="project.projectName">
              {{ project.projectName }} · #{{ project.projectId }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="月份" class="smart-query-form-item">
          <a-date-picker v-model:value="monthValue" picker="month" style="width: 160px" @change="loadCalendar" />
        </a-form-item>
        <a-form-item class="smart-query-form-item">
          <a-button type="primary" @click="loadCalendar" :loading="loading" :disabled="!projectId">
            <template #icon><ReloadOutlined /></template>
            刷新日历
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="16">
        <a-card size="small" :bordered="false">
          <template #title>
            <a-space>
              <CalendarOutlined />
              <span>写作日历</span>
            </a-space>
          </template>
          <a-spin :spinning="loading">
            <div class="weekday-row">
              <span v-for="weekday in weekdays" :key="weekday">{{ weekday }}</span>
            </div>
            <div class="calendar-grid">
              <div
                v-for="cell in calendarCells"
                :key="cell.key"
                class="calendar-cell"
                :class="[cell.blank ? 'is-blank' : '', heatClass(cell.wordCount)]"
              >
                <template v-if="!cell.blank">
                  <span class="cell-day">{{ cell.day }}</span>
                  <span class="cell-word">{{ formatNumber(cell.wordCount) }}</span>
                  <span class="cell-chapter">{{ cell.chapterCount || 0 }} 章</span>
                </template>
              </div>
            </div>
          </a-spin>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card size="small" title="连续写作" :bordered="false">
          <div class="streak-grid">
            <a-statistic title="连续天数" :value="streakInfo.streakDays || 0" suffix="天" />
            <a-statistic title="本月天数" :value="streakInfo.monthDays || 0" suffix="天" />
            <a-statistic title="本月字数" :value="streakInfo.monthWords || 0" />
          </div>
        </a-card>

        <a-card size="small" title="手动打卡" :bordered="false" class="checkin-card">
          <a-form ref="checkinFormRef" :model="checkinForm" :rules="checkinRules" layout="vertical">
            <a-form-item label="今日字数" name="wordCount">
              <a-input-number v-model:value="checkinForm.wordCount" :min="0" :max="1000000" style="width: 100%" />
            </a-form-item>
            <a-form-item label="完成章节" name="chapterCount">
              <a-input-number v-model:value="checkinForm.chapterCount" :min="0" :max="1000" style="width: 100%" />
            </a-form-item>
            <a-button type="primary" block @click="submitCheckin" :loading="checkinLoading" :disabled="!projectId">
              <template #icon><EditOutlined /></template>
              打卡
            </a-button>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
  import dayjs from 'dayjs';
  import { computed, onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { CalendarOutlined, EditOutlined, ReloadOutlined } from '@ant-design/icons-vue';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { novelWritingCalendarApi } from '/@/api/business/novel/novel-writing-calendar-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const weekdays = ['一', '二', '三', '四', '五', '六', '日'];
  const projectId = ref();
  const projectOptions = ref([]);
  const projectLoading = ref(false);
  const loading = ref(false);
  const checkinLoading = ref(false);
  const monthValue = ref(dayjs());
  const calendarRows = ref([]);
  const checkinFormRef = ref();
  const streakInfo = reactive({
    streakDays: 0,
    monthDays: 0,
    monthWords: 0,
  });
  const checkinForm = reactive({
    wordCount: 0,
    chapterCount: 0,
  });

  const checkinRules = {
    wordCount: [{ type: 'number', min: 0, message: '今日字数不能小于 0' }],
    chapterCount: [{ type: 'number', min: 0, message: '完成章节不能小于 0' }],
  };

  const recordMap = computed(() => {
    const map = new Map();
    for (const row of calendarRows.value) {
      map.set(row.date, row);
    }
    return map;
  });

  const calendarCells = computed(() => {
    const current = monthValue.value || dayjs();
    const start = current.startOf('month');
    const daysInMonth = current.daysInMonth();
    const firstWeekday = start.day() === 0 ? 7 : start.day();
    const cells = [];
    for (let i = 1; i < firstWeekday; i++) {
      cells.push({ key: `blank-${i}`, blank: true });
    }
    for (let day = 1; day <= daysInMonth; day++) {
      const date = start.date(day).format('YYYY-MM-DD');
      const record = recordMap.value.get(date) || {};
      cells.push({
        key: date,
        date,
        day,
        wordCount: record.wordCount || 0,
        chapterCount: record.chapterCount || 0,
      });
    }
    return cells;
  });

  onMounted(async () => {
    await loadProjects();
    if (projectId.value) {
      loadCalendar();
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

  async function loadCalendar() {
    if (!projectId.value) {
      calendarRows.value = [];
      Object.assign(streakInfo, { streakDays: 0, monthDays: 0, monthWords: 0 });
      return;
    }
    loading.value = true;
    try {
      const month = monthValue.value || dayjs();
      const [calendarRes, streakRes] = await Promise.all([
        novelWritingCalendarApi.query({
          projectId: projectId.value,
          year: month.year(),
          month: month.month() + 1,
        }),
        novelWritingCalendarApi.streak({ id: projectId.value }),
      ]);
      calendarRows.value = calendarRes.data || [];
      Object.assign(streakInfo, streakRes.data || {});
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  async function submitCheckin() {
    try {
      await checkinFormRef.value.validateFields();
      checkinLoading.value = true;
      await novelWritingCalendarApi.checkin({
        projectId: projectId.value,
        wordCount: checkinForm.wordCount || 0,
        chapterCount: checkinForm.chapterCount || 0,
      });
      message.success('今日写作已打卡');
      Object.assign(checkinForm, { wordCount: 0, chapterCount: 0 });
      await loadCalendar();
    } catch (err) {
      if (err?.errorFields) {
        message.warning('请检查打卡信息');
      } else {
        smartSentry.captureError(err);
      }
    } finally {
      checkinLoading.value = false;
    }
  }

  function heatClass(wordCount) {
    if (!wordCount) {
      return 'heat-0';
    }
    if (wordCount < 500) {
      return 'heat-1';
    }
    if (wordCount < 1500) {
      return 'heat-2';
    }
    if (wordCount < 3000) {
      return 'heat-3';
    }
    return 'heat-4';
  }

  function formatNumber(value) {
    return Number(value || 0).toLocaleString();
  }
</script>

<style scoped lang="less">
  .novel-writing-calendar-page {
    padding: 16px;
  }

  .weekday-row,
  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    gap: 8px;
  }

  .weekday-row {
    margin-bottom: 8px;
    color: #646a73;
    font-size: 12px;
    text-align: center;
  }

  .calendar-cell {
    display: flex;
    min-height: 94px;
    flex-direction: column;
    justify-content: space-between;
    padding: 10px;
    border: 1px solid #edf0f5;
    border-radius: 6px;
  }

  .calendar-cell.is-blank {
    border-color: transparent;
    background: transparent;
  }

  .heat-0 {
    background: #fafafa;
  }

  .heat-1 {
    background: #e8f4ff;
  }

  .heat-2 {
    background: #b7dcff;
  }

  .heat-3 {
    background: #69b1ff;
  }

  .heat-4 {
    color: #fff;
    background: #0958d9;
  }

  .cell-day {
    font-weight: 600;
  }

  .cell-word {
    font-size: 18px;
    font-weight: 600;
    line-height: 22px;
  }

  .cell-chapter {
    font-size: 12px;
  }

  .streak-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .checkin-card {
    margin-top: 16px;
  }

  @media (max-width: 768px) {
    .novel-writing-calendar-page {
      padding: 12px;
    }

    .calendar-grid {
      gap: 6px;
    }

    .calendar-cell {
      min-height: 76px;
      padding: 8px;
    }

    .cell-word {
      font-size: 14px;
    }
  }
</style>
