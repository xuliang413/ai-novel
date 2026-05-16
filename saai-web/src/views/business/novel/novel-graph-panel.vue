<template>
  <div class="novel-graph-panel-page">
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
        <a-form-item label="图谱类型" class="smart-query-form-item">
          <a-radio-group v-model:value="graphType" option-type="button" button-style="solid" @change="loadGraph">
            <a-radio-button v-for="item in graphTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-form-item class="smart-query-form-item">
          <a-button type="primary" @click="loadGraph" :loading="loading" :disabled="!projectId">
            <template #icon><ReloadOutlined /></template>
            刷新图谱
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :xl="18">
        <a-card size="small" :bordered="false">
          <template #title>
            <a-space>
              <ShareAltOutlined />
              <span>{{ graphTitle }}</span>
            </a-space>
          </template>
          <a-spin :spinning="loading">
            <div class="graph-stage">
              <div ref="chartRef" class="graph-chart"></div>
              <a-empty v-if="!loading && graphData.nodes.length === 0" class="graph-empty" description="暂无图谱数据" />
            </div>
          </a-spin>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="6">
        <a-card size="small" title="图例" :bordered="false">
          <div v-if="graphData.groups.length" class="legend-list">
            <div v-for="group in graphData.groups" :key="group.group" class="legend-item">
              <span class="legend-color" :style="{ background: group.color || '#8c8c8c' }"></span>
              <span>{{ group.name || group.group }}</span>
              <a-tag>{{ group.shape || '-' }}</a-tag>
            </div>
          </div>
          <a-empty v-else description="暂无图例" />
        </a-card>

        <a-card size="small" title="图谱信息" :bordered="false" class="side-card">
          <a-descriptions size="small" :column="1" bordered>
            <a-descriptions-item label="节点">{{ graphData.nodes.length }}</a-descriptions-item>
            <a-descriptions-item label="关系">{{ graphData.edges.length }}</a-descriptions-item>
            <a-descriptions-item label="类型">{{ graphData.graphType || '-' }}</a-descriptions-item>
          </a-descriptions>
          <a-alert
            v-for="warning in graphData.warnings"
            :key="warning"
            class="graph-warning"
            type="warning"
            show-icon
            :message="warning"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
  import * as echarts from 'echarts';
  import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
  import { ReloadOutlined, ShareAltOutlined } from '@ant-design/icons-vue';
  import { novelGraphApi } from '/@/api/business/novel/novel-graph-api';
  import { novelProjectApi } from '/@/api/business/novel/novel-project-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const graphTypeOptions = [
    { label: '角色关系', value: 'characterRelation', api: novelGraphApi.characterRelation },
    { label: '线索推进', value: 'clueAdvancement', api: novelGraphApi.clueAdvancement },
    { label: '地点人物', value: 'locationCharacter', api: novelGraphApi.locationCharacter },
    { label: '物品流转', value: 'itemFlow', api: novelGraphApi.itemFlow },
  ];

  const projectId = ref();
  const projectOptions = ref([]);
  const graphType = ref('characterRelation');
  const projectLoading = ref(false);
  const loading = ref(false);
  const chartRef = ref();
  let chartInstance = null;

  const graphData = reactive({
    graphType: '',
    projectId: undefined,
    nodes: [],
    edges: [],
    groups: [],
    legends: [],
    filters: [],
    warnings: [],
  });

  const graphTitle = computed(() => graphTypeOptions.find((item) => item.value === graphType.value)?.label || '图谱面板');

  onMounted(async () => {
    window.addEventListener('resize', resizeChart);
    await loadProjects();
    await nextTick();
    ensureChart();
    if (projectId.value) {
      loadGraph();
    }
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart);
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
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

  async function loadGraph() {
    if (!projectId.value) {
      resetGraphData();
      renderGraph();
      return;
    }
    loading.value = true;
    try {
      const option = graphTypeOptions.find((item) => item.value === graphType.value);
      const res = await option.api({ id: projectId.value });
      Object.assign(graphData, {
        graphType: '',
        projectId: projectId.value,
        nodes: [],
        edges: [],
        groups: [],
        legends: [],
        filters: [],
        warnings: [],
        ...(res.data || {}),
      });
      await nextTick();
      renderGraph();
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  function resetGraphData() {
    Object.assign(graphData, {
      graphType: '',
      projectId: undefined,
      nodes: [],
      edges: [],
      groups: [],
      legends: [],
      filters: [],
      warnings: [],
    });
  }

  function ensureChart() {
    if (!chartRef.value) {
      return null;
    }
    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value);
    }
    return chartInstance;
  }

  function renderGraph() {
    const chart = ensureChart();
    if (!chart) {
      return;
    }
    const groupMap = new Map((graphData.groups || []).map((item) => [item.group, item]));
    const nodes = (graphData.nodes || []).map((node) => {
      const group = groupMap.get(node.group) || {};
      return {
        id: node.id,
        name: node.name || node.id,
        value: node.type,
        category: String(node.group),
        symbol: shapeToSymbol(group.shape),
        symbolSize: 44,
        itemStyle: { color: group.color || '#5470c6' },
        properties: node.properties || {},
      };
    });
    const links = (graphData.edges || []).map((edge) => ({
      source: edge.source,
      target: edge.target,
      name: edge.label,
      label: { show: true, formatter: edge.label || '' },
      lineStyle: { color: '#8c8c8c', curveness: 0.18 },
      properties: edge.properties || {},
    }));

    chart.setOption(
      {
        tooltip: {
          trigger: 'item',
          formatter: formatTooltip,
        },
        legend: {
          top: 0,
          type: 'scroll',
          data: graphData.groups.map((item) => String(item.group)),
          formatter: (name) => groupMap.get(Number(name))?.name || name,
        },
        series: [
          {
            type: 'graph',
            layout: 'force',
            roam: true,
            draggable: true,
            top: 42,
            label: {
              show: true,
              position: 'right',
              color: '#1f2329',
            },
            edgeLabel: {
              show: true,
              color: '#646a73',
              fontSize: 11,
            },
            force: {
              repulsion: 180,
              edgeLength: [80, 180],
            },
            data: nodes,
            links,
            categories: graphData.groups.map((item) => ({
              name: String(item.group),
              itemStyle: { color: item.color || '#5470c6' },
            })),
            lineStyle: {
              width: 1.5,
              opacity: 0.75,
            },
            emphasis: {
              focus: 'adjacency',
            },
          },
        ],
      },
      true
    );
    resizeChart();
  }

  function shapeToSymbol(shape) {
    const map = {
      circle: 'circle',
      square: 'rect',
      triangle: 'triangle',
      diamond: 'diamond',
      dot: 'circle',
      star: 'pin',
      hexagon: 'roundRect',
      pentagon: 'roundRect',
      box: 'rect',
    };
    return map[shape] || 'circle';
  }

  function formatTooltip(param) {
    const data = param.data || {};
    const title = escapeHtml(data.name || data.label || data.id || '');
    const type = escapeHtml(data.value || data.name || '');
    const properties = data.properties || {};
    const rows = Object.entries(properties)
      .slice(0, 8)
      .map(([key, value]) => `<div>${escapeHtml(key)}：${escapeHtml(String(value ?? '-'))}</div>`)
      .join('');
    return `<div class="graph-tooltip"><strong>${title}</strong><div>${type}</div>${rows}</div>`;
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function resizeChart() {
    chartInstance?.resize();
  }
</script>

<style scoped lang="less">
  .novel-graph-panel-page {
    padding: 16px;
  }

  .graph-chart {
    width: 100%;
    height: 620px;
  }

  .graph-stage {
    position: relative;
  }

  .graph-empty {
    position: absolute;
    right: 0;
    left: 0;
    top: 220px;
    pointer-events: none;
  }

  .legend-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .legend-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .legend-color {
    width: 12px;
    height: 12px;
    flex: 0 0 12px;
    border-radius: 50%;
  }

  .side-card {
    margin-top: 16px;
  }

  .graph-warning {
    margin-top: 12px;
  }

  @media (max-width: 768px) {
    .novel-graph-panel-page {
      padding: 12px;
    }

    .graph-chart {
      height: 440px;
    }
  }
</style>
