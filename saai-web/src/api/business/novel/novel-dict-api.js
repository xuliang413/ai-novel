import { postRequest } from '/@/lib/axios';

const endpoints = [
  'project-status',
  'project-genre',
  'chapter-status',
  'writing-status',
  'generation-provider',
  'character-status',
  'character-role',
  'clue-type',
  'clue-status',
  'location-type',
  'item-type',
  'item-status',
  'cheat-type',
  'alias-type',
  'narrative-rule-type',
  'graph-node',
  'graph-relation',
  'graph-change-status',
  'graph-patch-operation-type',
];

export const novelDictApi = endpoints.reduce((api, endpoint) => {
  api[endpoint] = () => postRequest(`/novel/dict/${endpoint}`);
  return api;
}, {});

