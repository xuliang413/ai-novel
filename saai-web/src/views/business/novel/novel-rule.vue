<template>
  <AssetWorkbench :config="novelAssetConfigs.rule" />
</template>

<script setup>
  import AssetWorkbench from '/@/components/business/novel/asset-workbench.vue';
  import { novelAssetConfigs } from './novel-asset-config';
</script>

