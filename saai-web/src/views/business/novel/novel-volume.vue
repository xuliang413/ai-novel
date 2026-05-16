<template>
  <AssetWorkbench :config="novelAssetConfigs.volume" />
</template>

<script setup>
  import AssetWorkbench from '/@/components/business/novel/asset-workbench.vue';
  import { novelAssetConfigs } from './novel-asset-config';
</script>

