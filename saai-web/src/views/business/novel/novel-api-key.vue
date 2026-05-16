<template>
  <div class="novel-api-key-page">
    <a-card size="small" :bordered="false">
      <template #title>
        <a-space>
          <KeyOutlined />
          <span>模型密钥设置</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button @click="loadConfig" :loading="loading">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
          <a-button type="primary" @click="submitForm" :loading="saving">
            <template #icon><SaveOutlined /></template>
            保存
          </a-button>
        </a-space>
      </template>

      <a-spin :spinning="loading">
        <div class="config-summary">
          <a-statistic title="DeepSeek" :value="providerConfigured(providers[0]) ? '已配置' : '未配置'" />
          <a-statistic title="通义千问" :value="providerConfigured(providers[1]) ? '已配置' : '未配置'" />
          <a-statistic title="最近更新" :value="config.updateTime || '-'" />
        </div>

        <div class="usage-summary">
          <div v-for="item in usageCards" :key="item.key" class="usage-tile">
            <div class="usage-title">{{ item.title }}</div>
            <div class="usage-metrics">
              <a-statistic title="调用" :value="item.totalCalls" />
              <a-statistic title="成功" :value="item.successCalls" />
              <a-statistic title="失败" :value="item.failedCalls" />
              <a-statistic title="Token" :value="item.totalTokens" />
            </div>
            <div class="provider-usage">
              <a-tag v-for="provider in providerUsageList(item.byProvider)" :key="`${item.key}-${provider.name}`">
                {{ provider.name }}：{{ provider.value }}
              </a-tag>
              <span v-if="providerUsageList(item.byProvider).length === 0">暂无用量</span>
            </div>
          </div>
        </div>

        <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical" class="api-key-form">
          <div v-for="item in providers" :key="item.provider" class="provider-panel">
            <div class="provider-header">
              <div class="provider-title">
                <span>{{ item.name }}</span>
                <a-tag :color="statusColor(providerStatus(item))">{{ statusText(providerStatus(item)) }}</a-tag>
              </div>
              <a-button @click="testProvider(item)" :loading="testing[item.provider]">
                <template #icon><ExperimentOutlined /></template>
                检测
              </a-button>
            </div>

            <a-row :gutter="16">
              <a-col :xs="24" :md="10">
                <a-form-item label="当前密钥">
                  <a-input :value="providerMasked(item)" disabled>
                    <template #prefix><SafetyCertificateOutlined /></template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="10">
                <a-form-item label="新密钥" :name="item.keyField">
                  <a-input-password
                    v-model:value="formData[item.keyField]"
                    :disabled="formData[item.clearField]"
                    autocomplete="new-password"
                    allow-clear
                    placeholder="留空则不修改"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="4">
                <a-form-item label="清空">
                  <a-checkbox v-model:checked="formData[item.clearField]" @change="onClearChange(item)">清空密钥</a-checkbox>
                </a-form-item>
              </a-col>
            </a-row>

            <div v-if="testResults[item.provider]" class="test-result">
              <a-tag :color="statusColor(testResults[item.provider].status)">
                {{ statusText(testResults[item.provider].status) }}
              </a-tag>
              <span>{{ testResults[item.provider].message || '-' }}</span>
            </div>
          </div>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { ExperimentOutlined, KeyOutlined, ReloadOutlined, SafetyCertificateOutlined, SaveOutlined } from '@ant-design/icons-vue';
  import { novelUserApiKeyApi } from '/@/api/business/novel/novel-user-api-key-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const providers = [
    {
      provider: 'DEEPSEEK',
      name: 'DeepSeek',
      keyField: 'deepseekKey',
      clearField: 'clearDeepseek',
      hasField: 'hasDeepseekKey',
      maskedField: 'deepseekMasked',
      statusField: 'deepseekStatus',
    },
    {
      provider: 'TONGYI',
      name: '通义千问',
      keyField: 'qwenKey',
      clearField: 'clearQwen',
      hasField: 'hasQwenKey',
      maskedField: 'qwenMasked',
      statusField: 'qwenStatus',
    },
  ];

  const formRef = ref();
  const loading = ref(false);
  const saving = ref(false);
  const config = reactive({
    hasDeepseekKey: false,
    hasQwenKey: false,
    deepseekMasked: '',
    qwenMasked: '',
    deepseekStatus: 'MISSING',
    qwenStatus: 'MISSING',
    updateTime: '',
  });
  const usage = reactive({
    month: {},
    week: {},
    today: {},
  });

  const formData = reactive({
    deepseekKey: '',
    qwenKey: '',
    clearDeepseek: false,
    clearQwen: false,
  });

  const testing = reactive({
    DEEPSEEK: false,
    TONGYI: false,
  });
  const testResults = reactive({
    DEEPSEEK: null,
    TONGYI: null,
  });

  const formRules = {
    deepseekKey: [{ max: 1000, message: 'DeepSeek API Key 最大 1000 个字符' }],
    qwenKey: [{ max: 1000, message: '通义千问 API Key 最大 1000 个字符' }],
  };

  const usageCards = computed(() => [
    buildUsageCard('today', '今日', usage.today),
    buildUsageCard('week', '本周', usage.week),
    buildUsageCard('month', '本月', usage.month),
  ]);

  onMounted(() => {
    loadConfig();
  });

  async function loadConfig() {
    loading.value = true;
    try {
      const res = await novelUserApiKeyApi.get();
      Object.assign(config, {
        hasDeepseekKey: false,
        hasQwenKey: false,
        deepseekMasked: '',
        qwenMasked: '',
        deepseekStatus: 'MISSING',
        qwenStatus: 'MISSING',
        updateTime: '',
        ...(res.data || {}),
      });
      await loadUsage();
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  async function loadUsage() {
    const res = await novelUserApiKeyApi.usage();
    Object.assign(usage, {
      month: {},
      week: {},
      today: {},
      ...(res.data || {}),
    });
  }

  function buildUsageCard(key, title, data) {
    return {
      key,
      title,
      totalCalls: data?.totalCalls || 0,
      successCalls: data?.successCalls || 0,
      failedCalls: data?.failedCalls || 0,
      totalTokens: data?.totalTokens || 0,
      byProvider: data?.byProvider || {},
    };
  }

  function providerUsageList(byProvider) {
    return Object.entries(byProvider || {}).map(([name, value]) => ({ name, value }));
  }

  function providerConfigured(item) {
    return Boolean(config[item.hasField]);
  }

  function providerMasked(item) {
    return config[item.maskedField] || '-';
  }

  function providerStatus(item) {
    return config[item.statusField] || 'MISSING';
  }

  function statusColor(status) {
    const map = {
      CONFIGURED: 'green',
      OK: 'green',
      MISSING: 'red',
      ERROR: 'red',
      INVALID: 'orange',
    };
    return map[status] || 'default';
  }

  function statusText(status) {
    const map = {
      CONFIGURED: '已配置',
      OK: '可用',
      MISSING: '未配置',
      ERROR: '异常',
      INVALID: '不可用',
    };
    return map[status] || status || '-';
  }

  function onClearChange(item) {
    if (formData[item.clearField]) {
      formData[item.keyField] = '';
    }
  }

  function buildSavePayload() {
    const payload = {};
    if (formData.clearDeepseek) {
      payload.deepseekKey = '';
    } else if (formData.deepseekKey) {
      payload.deepseekKey = formData.deepseekKey.trim();
    }

    if (formData.clearQwen) {
      payload.qwenKey = '';
    } else if (formData.qwenKey) {
      payload.qwenKey = formData.qwenKey.trim();
    }
    return payload;
  }

  function resetSensitiveForm() {
    Object.assign(formData, {
      deepseekKey: '',
      qwenKey: '',
      clearDeepseek: false,
      clearQwen: false,
    });
  }

  async function submitForm() {
    try {
      await formRef.value.validateFields();
      const payload = buildSavePayload();
      if (!Object.keys(payload).length) {
        message.warning('没有需要保存的密钥变更');
        return;
      }
      saving.value = true;
      await novelUserApiKeyApi.save(payload);
      message.success('模型密钥已保存');
      resetSensitiveForm();
      await loadConfig();
    } catch (err) {
      if (err?.errorFields) {
        message.warning('请检查密钥长度');
      } else {
        smartSentry.captureError(err);
      }
    } finally {
      saving.value = false;
    }
  }

  async function testProvider(item) {
    testing[item.provider] = true;
    try {
      const res = await novelUserApiKeyApi.test({ provider: item.provider });
      testResults[item.provider] = res.data || null;
      if (res.data?.configured) {
        message.success(`${item.name} 已配置`);
      } else {
        message.warning(`${item.name} 未配置`);
      }
    } catch (err) {
      smartSentry.captureError(err);
    } finally {
      testing[item.provider] = false;
    }
  }
</script>

<style scoped lang="less">
  .novel-api-key-page {
    padding: 16px;
  }

  .config-summary {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: #fafafa;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
  }

  .usage-summary {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 16px;
  }

  .usage-tile {
    padding: 14px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    background: #fff;
  }

  .usage-title {
    margin-bottom: 10px;
    color: #1f2329;
    font-weight: 600;
  }

  .usage-metrics {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px 12px;
  }

  .provider-usage {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 10px;
    color: #8c8c8c;
  }

  .api-key-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .provider-panel {
    padding: 16px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    background: #fff;
  }

  .provider-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
  }

  .provider-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
  }

  .test-result {
    display: flex;
    align-items: center;
    gap: 8px;
    min-height: 32px;
    padding: 8px 12px;
    background: #fafafa;
    border-radius: 6px;
  }

  @media (max-width: 768px) {
    .novel-api-key-page {
      padding: 12px;
    }

    .config-summary {
      grid-template-columns: 1fr;
    }

    .usage-summary {
      grid-template-columns: 1fr;
    }

    .provider-header {
      align-items: flex-start;
      flex-direction: column;
    }
  }
</style>
